package top.fifthlight.tools.texteditor.ui.component

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentMapOf
import top.fifthlight.tools.texteditor.state.TranslateEntry
import top.fifthlight.tools.texteditor.state.TranslateState

@Composable
fun TranslateTable(
    state: TranslateState,
    onInsertEntry: (Int, TranslateEntry) -> Unit,
    onDeleteEntry: (Int) -> Unit,
    onUpdateKey: (Int, String) -> Unit,
    onUpdateText: (Int, String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 8.dp)
        ) {
            Text(
                modifier = Modifier.width(128.dp),
                text = "Tools",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "Key",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "Value",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            val listState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = LocalScrollbarStyle.current.thickness),
                state = listState,
            ) {
                itemsIndexed(
                    items = state.entries,
                    key = { index, item ->
                        when (item) {
                            TranslateEntry.Spacing -> index
                            is TranslateEntry.Text -> item
                        }
                    },
                ) { index, item ->
                    if (index != 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }

                    @Composable
                    fun ToolButtons() {
                        Row(
                            modifier = Modifier.width(128.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                        ) {
                            var expanded by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = {
                                    expanded = true
                                },
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Add text entry") },
                                    onClick = {
                                        onInsertEntry(
                                            index + 1,
                                            TranslateEntry.Text(key = "", texts = persistentMapOf())
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Add spacing entry") },
                                    onClick = {
                                        onInsertEntry(index + 1, TranslateEntry.Spacing)
                                    }
                                )
                            }
                            IconButton(
                                onClick = {
                                    onDeleteEntry(index)
                                },
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }

                    when (item) {
                        TranslateEntry.Spacing -> Row(
                            modifier = Modifier
                                .height(48.dp)
                                .fillMaxSize(),
                        ) {
                            ToolButtons()

                            VerticalDivider(color = MaterialTheme.colorScheme.outline)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("-- Spacing --")
                            }
                        }

                        is TranslateEntry.Text -> Row(
                            modifier = Modifier
                                .height(48.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ToolButtons()

                            val textColor = MaterialTheme.colorScheme.onSurface
                            val cursorColor = TextFieldDefaults.colors().cursorColor

                            VerticalDivider(color = MaterialTheme.colorScheme.outline)
                            BasicTextField(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp),
                                value = item.key,
                                onValueChange = {
                                    onUpdateKey(index, it)
                                },
                                textStyle = LocalTextStyle.current.copy(
                                    color = textColor,
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(cursorColor),
                            )

                            VerticalDivider(color = MaterialTheme.colorScheme.outline)

                            (state.chosenLanguage?.let { item.texts[it] } ?: "").let { value ->
                                BasicTextField(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp),
                                    value = value,
                                    onValueChange = {
                                        state.chosenLanguage?.let { lang -> onUpdateText(index, lang, it) }
                                    },
                                    textStyle = LocalTextStyle.current.copy(
                                        color = textColor,
                                    ),
                                    singleLine = true,
                                    decorationBox = { inner ->
                                        Box(
                                            modifier = Modifier.background(
                                                color = if (value.isEmpty()) MaterialTheme.colorScheme.errorContainer else Color.Transparent,
                                            ),
                                        ) {
                                            inner()
                                        }
                                    },
                                    cursorBrush = SolidColor(cursorColor),
                                )
                            }
                        }
                    }
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState = listState),
                style = LocalScrollbarStyle.current.copy(
                    hoverColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unhoverColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    }
}
