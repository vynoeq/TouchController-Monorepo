package top.fifthlight.armorstand.ui.component

import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.util.CommonColors
import top.fifthlight.blazerod.model.Metadata
import java.net.URI
import java.net.URISyntaxException
import java.util.function.Consumer
import kotlin.math.max

class MetadataWidget(
    private val minecraft: Minecraft,
    textClickHandler: (Style) -> Unit,
    x: Int = 0,
    y: Int = 0,
    width: Int = 0,
    height: Int = 0,
    metadata: Metadata? = null,
) : AbstractContainerWidget(x, y, width, height, CommonComponents.EMPTY) {
    companion object {
        private const val GAP = 8
    }

    private val textRenderer = minecraft.font
    private val entries = listOf<Entry>(
        Entry.TitleAndVersionEntry(textRenderer, textClickHandler),
        Entry.AuthorListEntry(textRenderer, textClickHandler),
        Entry.CopyrightEntry(textRenderer, textClickHandler),
        Entry.LinksEntry(textRenderer, textClickHandler),
        Entry.CommentsEntry(textRenderer, textClickHandler),
        Entry.LicenseEntry(textRenderer, textClickHandler),
        Entry.PermissionsEntry(textRenderer, textClickHandler),
    )

    override fun contentHeight(): Int = entries.filter { it.visible }.let { entries ->
        entries.sumOf { it.height } + GAP * (entries.size - 1)
    }

    override fun scrollRate(): Double = minecraft.font.lineHeight * 4.0

    override fun renderWidget(
        graphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        deltaTicks: Float,
    ) {
        refreshScrollAmount()
        graphics.enableScissor(x, y, right, bottom)
        val entryWidth = scrollBarX() - 4 - x
        val visibleAreaTop = scrollAmount().toInt()
        val visibleAreaBottom = scrollAmount().toInt() + height
        var currentYOffset = 0
        for (entry in entries) {
            if (!entry.visible) {
                continue
            }
            val entryTop = currentYOffset
            val entryBottom = currentYOffset + entry.height
            entry.refreshPositions(x, y + entryTop - visibleAreaTop, entryWidth)
            currentYOffset += entry.height + GAP
            if (entryBottom < visibleAreaTop || entryTop > visibleAreaBottom) {
                continue
            }
            entry.render(
                graphics,
                mouseX,
                mouseY,
                x,
                y + entryTop - visibleAreaTop,
                entryWidth,
                deltaTicks,
            )
        }
        graphics.disableScissor()
        renderScrollbar(graphics)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        for (entry in entries) {
            if (!entry.visible) {
                continue
            }
            if (entry.mouseClicked(mouseX, mouseY, button)) {
                return true
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun updateWidgetNarration(builder: NarrationElementOutput) {
        // TODO for better accessibility
    }

    var metadata: Metadata? = metadata
        set(value) {
            field = value
            entries.forEach { it.update(value) }
        }

    override fun children(): List<GuiEventListener> = entries.filter { it.visible }

    @Suppress("PropertyName")
    private sealed class Entry(
        val font: Font,
        val textClickHandler: (Style) -> Unit,
    ) : GuiEventListener, LayoutElement {
        abstract fun update(metadata: Metadata?)
        abstract val visible: Boolean
        abstract fun refreshPositions(x: Int, y: Int, width: Int)
        protected var _x: Int = 0
        protected var _y: Int = 0
        protected var _width: Int = 0
        protected var _height: Int = 0
        protected var _focused: Boolean = false

        override fun setFocused(focused: Boolean) {
            _focused = focused
        }

        override fun isFocused() = _focused

        override fun setX(x: Int) {
            _x = x
        }

        override fun setY(y: Int) {
            _y = y
        }

        override fun getX() = _x
        override fun getY() = _y
        override fun getWidth() = _width
        override fun getHeight() = _height
        override fun getRectangle(): ScreenRectangle = ScreenRectangle(_x, _y, _width, _height)

        override fun visitWidgets(consumer: Consumer<AbstractWidget>) = Unit

        abstract fun render(
            graphics: GuiGraphics,
            mouseX: Int,
            mouseY: Int,
            x: Int,
            y: Int,
            width: Int,
            deltaTicks: Float,
        )

        class AuthorListEntry(
            textRenderer: Font,
            textClickHandler: (Style) -> Unit,
            val padding: Int = 8,
            val margin: Int = 8,
            val gap: Int = 8,
            val surface: Surface = Surface.color(0x88000000u) + Surface.border(0xAA000000u),
        ) : Entry(textRenderer, textClickHandler) {
            private class AuthorEntry(
                val name: Component,
                val role: Component? = null,
                val contact: List<Component>,
                val comment: Component? = null,
                var nameHeight: Int = 0,
                var nameWidth: Int = 0,
                var roleWidth: Int = 0,
                var titleLineHeight: Int = 0,
                var contactsLineHeight: IntArray,
                var commentsHeight: Int = 0,
                var totalHeight: Int = 0,
            )

            private var authors: List<AuthorEntry> = listOf()

            override val visible: Boolean
                get() = authors.isNotEmpty()

            override fun update(metadata: Metadata?) {
                authors = metadata?.authors?.map { author ->
                    AuthorEntry(
                        name = Component.literal(author.name),
                        role = author.role?.let { Component.literal(it) },
                        contact = author.contact?.map { (type, value) ->
                            @Suppress("HttpUrlsUsage")
                            val content = when {
                                value.startsWith("https://", ignoreCase = true) ||
                                        value.startsWith("http://", ignoreCase = true) -> value.urlText()

                                type.equals("email", ignoreCase = true) ||
                                        type.equals("e-mail", ignoreCase = true) -> value.emailText()

                                else -> Component.literal(value)
                            }
                            Component.translatable(
                                "armorstand.metadata.author.contact",
                                type,
                                content,
                            )
                        } ?: listOf(),
                        comment = author.comment?.let { Component.literal(it) },
                        contactsLineHeight = IntArray(author.contact?.size ?: 0),
                    )
                } ?: listOf()
            }

            override fun refreshPositions(x: Int, y: Int, width: Int) {
                val realWidth = width - padding * 2
                var totalHeight = 0
                for ((index, author) in authors.withIndex()) {
                    var authorHeight = 0

                    val role = author.role
                    if (role != null) {
                        val roleWidth = font.width(role)
                        val nameWidth = realWidth - roleWidth - 8
                        val nameHeight = font.wordWrapHeight(author.name, nameWidth)
                        val titleLineHeight = max(nameHeight, font.lineHeight)
                        author.nameWidth = nameWidth
                        author.nameHeight = nameHeight
                        author.roleWidth = roleWidth
                        author.titleLineHeight = titleLineHeight
                        authorHeight += titleLineHeight
                    } else {
                        val nameHeight = font.wordWrapHeight(author.name, realWidth)
                        val titleLineHeight = max(nameHeight, font.lineHeight)
                        author.nameWidth = realWidth
                        author.nameHeight = nameHeight
                        author.titleLineHeight = titleLineHeight
                        authorHeight += titleLineHeight
                    }

                    for ((index, contact) in author.contact.withIndex()) {
                        val contactHeight = font.wordWrapHeight(contact, realWidth)
                        author.contactsLineHeight[index] = contactHeight
                        authorHeight += gap + contactHeight
                    }

                    val comment = author.comment
                    if (comment != null) {
                        val commentHeight = font.wordWrapHeight(comment, realWidth)
                        author.commentsHeight = commentHeight
                        authorHeight += gap + commentHeight
                    } else {
                        author.commentsHeight = 0
                    }

                    author.totalHeight = authorHeight
                    totalHeight += authorHeight + padding * 2
                    if (index != 0) {
                        totalHeight += margin
                    }
                }
                _x = x
                _y = y
                _width = width
                _height = totalHeight
            }

            override fun render(
                graphics: GuiGraphics,
                mouseX: Int,
                mouseY: Int,
                x: Int,
                y: Int,
                width: Int,
                deltaTicks: Float,
            ) {
                val currentX = x + padding
                val realWidth = width - padding * 2
                var currentY = y

                for (author in authors) {
                    surface.draw(graphics, x, currentY, width, author.totalHeight + padding * 2)
                    currentY += padding
                    graphics.drawWordWrap(
                        font,
                        author.name,
                        currentX,
                        currentY,
                        author.nameWidth,
                        CommonColors.WHITE,
                        false,
                    )
                    author.role?.let { role ->
                        graphics.drawWordWrap(
                            font,
                            role,
                            currentX + realWidth - author.roleWidth,
                            currentY,
                            author.roleWidth,
                            CommonColors.WHITE,
                            false,
                        )
                    }
                    currentY += author.titleLineHeight

                    for ((index, contact) in author.contact.withIndex()) {
                        currentY += gap
                        graphics.drawWordWrap(
                            font,
                            contact,
                            currentX,
                            currentY,
                            realWidth,
                            CommonColors.WHITE,
                            false,
                        )
                        currentY += author.contactsLineHeight[index]
                    }

                    author.comment?.let { comment ->
                        currentY += gap
                        graphics.drawWordWrap(
                            font,
                            comment,
                            currentX,
                            currentY,
                            realWidth,
                            CommonColors.WHITE,
                            false,
                        )
                        currentY += author.commentsHeight
                    }

                    currentY += padding + margin
                }
            }

            override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
                val authors = authors.takeIf { it.isNotEmpty() } ?: return false
                val realWidth = width - padding * 2
                var currentY = 0
                val offsetX = mouseX.toInt() - x
                val offsetY = mouseY.toInt() - y
                if (offsetX !in padding until realWidth) {
                    return false
                }
                if (offsetY !in padding until (height - padding)) {
                    return false
                }

                for (author in authors) {
                    currentY += padding + author.titleLineHeight

                    for ((index, contact) in author.contact.withIndex()) {
                        currentY += gap
                        val contactHeight = author.contactsLineHeight[index]
                        if (offsetY in currentY until currentY + contactHeight) {
                            val lineOffsetY = offsetY - currentY
                            val lineIndex = lineOffsetY / font.lineHeight
                            val textLines = font.split(contact, realWidth)
                            val textLine = textLines.getOrNull(lineIndex) ?: return false
                            val style = font.splitter.componentStyleAtWidth(textLine, offsetX - padding) ?: return false
                            textClickHandler(style)
                        }
                        currentY += contactHeight
                    }

                    author.comment?.let { comment ->
                        currentY += gap + author.commentsHeight
                    }

                    currentY += padding + margin
                }
                return super.mouseClicked(mouseX, mouseY, button)
            }
        }

        abstract class TextListEntry(
            font: Font,
            textClickHandler: (Style) -> Unit,
            val padding: Int = 8,
            val gap: Int = 8,
            val surface: Surface = Surface.color(0x88000000u) + Surface.border(0xAA000000u),
        ) : Entry(font, textClickHandler) {
            private var texts: List<Component>? = null
            abstract fun getTexts(metadata: Metadata?): List<Component>?

            override val visible: Boolean
                get() = texts?.isNotEmpty() ?: false

            private var textHeights = Array(0) { 0 }
            override fun update(metadata: Metadata?) {
                val newTexts = getTexts(metadata)
                texts = newTexts
                textHeights = Array(newTexts?.size ?: 0) { 0 }
            }

            override fun refreshPositions(x: Int, y: Int, width: Int) {
                var totalHeight = 0
                val realWidth = width - padding * 2
                texts?.let { texts ->
                    for (i in 0 until texts.size) {
                        val textHeight = this@TextListEntry.font.wordWrapHeight(texts[i], realWidth)
                        textHeights[i] = textHeight
                        totalHeight += textHeight
                    }
                }
                _x = x
                _y = y
                _width = width
                _height = totalHeight + padding * 2 + gap * (texts?.size?.let { it - 1 } ?: 0)
            }

            override fun render(
                graphics: GuiGraphics,
                mouseX: Int,
                mouseY: Int,
                x: Int,
                y: Int,
                width: Int,
                deltaTicks: Float,
            ) {
                surface.draw(graphics, x, y, width, height)
                val realWidth = width - padding * 2
                texts?.let { texts ->
                    var currentY = y + padding
                    for ((index, text) in texts.withIndex()) {
                        graphics.drawWordWrap(
                            this@TextListEntry.font,
                            text,
                            x + padding,
                            currentY,
                            realWidth,
                            CommonColors.WHITE,
                            false,
                        )
                        currentY += textHeights[index] + gap
                    }
                }
            }

            override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
                val texts = texts ?: return false
                if (mouseX.toInt() !in (x + padding) until (x + width - padding)) {
                    return false
                }
                if (mouseY.toInt() !in (y + padding) until (y + height - padding)) {
                    return false
                }

                val offsetX = mouseX.toInt() - (x + padding)
                val offsetY = mouseY.toInt() - (y + padding)
                var textY = 0
                for ((index, height) in textHeights.withIndex()) {
                    if (offsetY in textY until textY + height) {
                        val lineOffsetY = offsetY - textY
                        val lineIndex = lineOffsetY / this@TextListEntry.font.lineHeight
                        val text = texts[index]
                        val textLines = this@TextListEntry.font.split(text, width - padding * 2)
                        val textLine = textLines.getOrNull(lineIndex) ?: return false
                        val style = font.splitter.componentStyleAtWidth(textLine, offsetX - padding) ?: return false
                        textClickHandler(style)
                        break
                    }
                    textY += height + gap
                }

                return super.mouseClicked(mouseX, mouseY, button)
            }
        }

        class TitleAndVersionEntry(
            textRenderer: Font,
            textClickHandler: (Style) -> Unit,
        ) : TextListEntry(textRenderer, textClickHandler) {
            override fun getTexts(metadata: Metadata?) = metadata?.let {
                listOfNotNull(
                    metadata.title?.let {
                        Component.translatable("armorstand.metadata.title", metadata.title)
                    } ?: run {
                        Component.translatable("armorstand.metadata.title.unknown")
                    },
                    metadata.version?.let { Component.translatable("armorstand.metadata.version", it) },
                )
            }
        }

        class CopyrightEntry(
            textRenderer: Font,
            textClickHandler: (Style) -> Unit,
        ) : TextListEntry(textRenderer, textClickHandler) {
            override fun getTexts(metadata: Metadata?) = metadata?.let { metadata ->
                listOfNotNull(
                    metadata.copyrightInformation
                        ?.takeIf(String::isNotBlank)
                        ?.let { copyrightInformation ->
                            Component.translatable(
                                "armorstand.metadata.copyright_information",
                                copyrightInformation
                            )
                        },
                    metadata.references
                        ?.filter { it.isNotBlank() }
                        ?.takeIf(List<String>::isNotEmpty)
                        ?.let { references ->
                            Component.translatable("armorstand.metadata.references", references.joinToString(", "))
                        }
                ).takeIf { list -> list.isNotEmpty() }
            }
        }

        class LinksEntry(
            textRenderer: Font,
            textClickHandler: (Style) -> Unit,
        ) : TextListEntry(textRenderer, textClickHandler) {
            override fun getTexts(metadata: Metadata?) = listOfNotNull(
                metadata?.linkHome
                    ?.takeIf(String::isNotBlank)
                    ?.let { Component.translatable("armorstand.metadata.link_home", it.urlText()) },
                metadata?.linkDonate
                    ?.takeIf(String::isNotBlank)
                    ?.let { Component.translatable("armorstand.metadata.link_donate", it.urlText()) },
            )
        }

        class CommentsEntry(
            textRenderer: Font,
            textClickHandler: (Style) -> Unit,
        ) : TextListEntry(textRenderer, textClickHandler) {
            override fun getTexts(metadata: Metadata?) = listOfNotNull(
                metadata?.comment
                    ?.takeIf(String::isNotBlank)
                    ?.replace("\r\n", "\n")
                    ?.let { Component.translatable("armorstand.metadata.comments", it) }
            )
        }

        class LicenseEntry(
            textRenderer: Font,
            textClickHandler: (Style) -> Unit,
        ) : TextListEntry(textRenderer, textClickHandler) {
            override fun getTexts(metadata: Metadata?) = listOfNotNull(
                metadata?.licenseType
                    ?.takeIf(String::isNotBlank)
                    ?.let { Component.translatable("armorstand.metadata.license_type", it) },
                metadata?.licenseDescription
                    ?.takeIf(String::isNotBlank)
                    ?.let { Component.translatable("armorstand.metadata.license_description", it) },
                metadata?.licenseUrl
                    ?.takeIf(String::isNotBlank)
                    ?.let { Component.translatable("armorstand.metadata.license_url", it.urlText()) },
                metadata?.thirdPartyLicenses
                    ?.takeIf(String::isNotBlank)
                    ?.let { Component.translatable("armorstand.metadata.third_party_licenses", it) },
                metadata?.specLicenseUrl
                    ?.takeIf(String::isNotBlank)
                    ?.let { Component.translatable("armorstand.metadata.spec_license_url", it.urlText()) },
            )
        }

        class PermissionsEntry(
            textRenderer: Font,
            textClickHandler: (Style) -> Unit,
        ) : TextListEntry(textRenderer, textClickHandler) {
            override fun getTexts(metadata: Metadata?) = listOfNotNull(
                metadata?.allowedUser?.let {
                    when (it) {
                        Metadata.AllowedUser.ONLY_AUTHOR -> Component.translatable("armorstand.metadata.allowed_user.only_author")
                        Metadata.AllowedUser.EXPLICITLY_LICENSED_PERSON -> Component.translatable("armorstand.metadata.allowed_user.explicitly_licensed_person")
                        Metadata.AllowedUser.EVERYONE -> Component.translatable("armorstand.metadata.allowed_user.everyone")
                    }
                },
                metadata?.allowViolentUsage?.let {
                    if (it) {
                        Component.translatable("armorstand.metadata.violent_usage.allow")
                    } else {
                        Component.translatable("armorstand.metadata.violent_usage.disallow")
                    }
                },
                metadata?.allowSexualUsage?.let {
                    if (it) {
                        Component.translatable("armorstand.metadata.sexual_usage.allow")
                    } else {
                        Component.translatable("armorstand.metadata.sexual_usage.disallow")
                    }
                },
                metadata?.commercialUsage?.let {
                    when (it) {
                        Metadata.CommercialUsage.DISALLOW -> Component.translatable("armorstand.metadata.commercial_usage.disallow")
                        Metadata.CommercialUsage.ALLOW -> Component.translatable("armorstand.metadata.commercial_usage.allow")
                        Metadata.CommercialUsage.PERSONAL_NON_PROFIT -> Component.translatable("armorstand.metadata.commercial_usage.personal_non_profit")
                        Metadata.CommercialUsage.PERSONAL_PROFIT -> Component.translatable("armorstand.metadata.commercial_usage.personal_profit")
                        Metadata.CommercialUsage.CORPORATION -> Component.translatable("armorstand.metadata.commercial_usage.corporation")
                    }
                },
                metadata?.allowPoliticalOrReligiousUsage?.let {
                    if (it) {
                        Component.translatable("armorstand.metadata.political_or_religious_usage.allow")
                    } else {
                        Component.translatable("armorstand.metadata.political_or_religious_usage.disallow")
                    }
                },
                metadata?.allowAntisocialOrHateUsage?.let {
                    if (it) {
                        Component.translatable("armorstand.metadata.antisocial_or_hate_usage.allow")
                    } else {
                        Component.translatable("armorstand.metadata.antisocial_or_hate_usage.disallow")
                    }
                },
                metadata?.creditNotation?.let {
                    when (it) {
                        Metadata.CreditNotation.REQUIRED -> Component.translatable("armorstand.metadata.credit_notation.required")
                        Metadata.CreditNotation.UNNECESSARY -> Component.translatable("armorstand.metadata.credit_notation.unnecessary")
                    }
                },
                metadata?.allowRedistribution?.let {
                    if (it) {
                        Component.translatable("armorstand.metadata.redistribution.allow")
                    } else {
                        Component.translatable("armorstand.metadata.redistribution.disallow")
                    }
                },
                metadata?.modificationPermission?.let {
                    when (it) {
                        Metadata.ModificationPermission.PROHIBITED -> Component.translatable("armorstand.metadata.modification_permission.prohibited")
                        Metadata.ModificationPermission.ALLOW_MODIFICATION -> Component.translatable("armorstand.metadata.modification_permission.allow_modification")
                        Metadata.ModificationPermission.ALLOW_MODIFICATION_REDISTRIBUTION -> Component.translatable("armorstand.metadata.modification_permission.allow_modification_redistribution")
                    }
                },
                metadata?.permissionUrl
                    ?.takeIf(String::isNotBlank)
                    ?.let { Component.translatable("armorstand.metadata.permission_url", it.urlText()) },
            )
        }
    }
}

private fun String.urlText(uri: URI): Component = Component.literal(this)
    .withStyle(
        Style.EMPTY
            .withColor(ChatFormatting.BLUE)
            .withUnderlined(true)
            .withClickEvent { ClickEvent.Action.OPEN_URL }
    )

private fun String.emailText(): Component {
    val uri = try {
        URI("mailto:$this")
    } catch (e: URISyntaxException) {
        return Component.literal(this)
    }
    return Component.literal(this).withStyle(
        Style.EMPTY
            .withColor(ChatFormatting.BLUE)
            .withUnderlined(true)
            .withClickEvent(ClickEvent.OpenUrl(uri))
    )
}

private fun String.urlText() = try {
    urlText(URI(this))
} catch (e: URISyntaxException) {
    Component.literal(this)
}
