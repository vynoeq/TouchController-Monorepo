package top.fifthlight.touchcontroller.common.ui.entitypicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import top.fifthlight.combine.item.widget.Item
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.placement.fillMaxWidth
import top.fifthlight.combine.modifier.placement.minHeight
import top.fifthlight.combine.modifier.placement.padding
import top.fifthlight.combine.modifier.scroll.verticalScroll
import top.fifthlight.combine.widget.layout.Column
import top.fifthlight.combine.widget.layout.Row
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.touchcontroller.common.gal.entity.EntityItemProvider
import top.fifthlight.touchcontroller.common.gal.entity.EntityType
import top.fifthlight.touchcontroller.common.gal.entity.EntityTypeProvider
import top.fifthlight.touchcontroller.common.gal.gamestate.GameState
import top.fifthlight.touchcontroller.common.ui.widget.ListButton

@Composable
fun EntityPicker(
    modifier: Modifier = Modifier,
    onEntityChosen: (EntityType) -> Unit,
) {
    val inGame = GameState.inGame
    val totalEntityTypes = remember(inGame) {
        EntityTypeProvider.allTypes.map { type ->
            val icon = if (inGame) {
                EntityItemProvider.getEntityIconItem(type)
            } else {
                null
            }
            Pair(icon, type)
        }
    }

    Column(
        modifier = Modifier.padding(8)
            .verticalScroll()
            .then(modifier),
    ) {
        for ((icon, entityType) in totalEntityTypes) {
            ListButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEntityChosen(entityType) },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minHeight(16),
                    horizontalArrangement = Arrangement.spacedBy(4),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (icon != null) {
                        Item(item = icon)
                    }
                    Text(
                        modifier = Modifier
                            .weight(1f),
                        text = entityType.name,
                    )
                }
            }
        }
    }
}
