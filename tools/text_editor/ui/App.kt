package top.fifthlight.tools.texteditor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.fifthlight.tools.texteditor.model.TranslateViewModel
import top.fifthlight.tools.texteditor.ui.component.TranslateTable
import java.awt.Window
import java.nio.file.Path
import javax.swing.JFileChooser


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    window: Window,
    initialPath: Path? = null,
    viewModel: TranslateViewModel = remember { TranslateViewModel() },
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(initialPath) {
        initialPath?.let { viewModel.loadDirectory(it) }
    }
    val dialog = remember {
        JFileChooser().also {
            it.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        }
    }

    fun openDialog(window: Window) {
        dialog.dialogType = JFileChooser.OPEN_DIALOG
        if (dialog.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
            viewModel.loadDirectory(dialog.selectedFile.toPath())
        }
    }

    fun saveDialog(window: Window) {
        dialog.dialogType = JFileChooser.SAVE_DIALOG
        if (dialog.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
            viewModel.saveDirectory(dialog.selectedFile.toPath())
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Text editor")
                            if (state.currentDirectory != null) {
                                Text(
                                    text = state.currentDirectory!! + if (state.hasUnsavedChanges) " *" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    actions = {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = {
                                expanded = !expanded
                            },
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.menuAnchor(
                                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                ),
                                readOnly = true,
                                value = state.chosenLanguage ?: "",
                                onValueChange = { },
                                label = { Text("Language") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = expanded,
                                    )
                                },
                                colors = ExposedDropdownMenuDefaults.textFieldColors()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                            ) {
                                for (language in state.languages) {
                                    DropdownMenuItem(
                                        text = { Text(language) },
                                        onClick = {
                                            viewModel.chooseLanguage(language)
                                            expanded = false
                                        },
                                    )
                                }
                            }
                        }

                        TextButton(onClick = {
                            openDialog(window)
                        }) {
                            Text("Open")
                        }

                        TextButton(
                            onClick = {
                                saveDialog(window)
                            },
                            enabled = state.entries.isNotEmpty()
                        ) {
                            Text("Save")
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (state.entries.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Open directory to continue",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = {
                            openDialog(window)
                        },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Open directory")
                    }
                }
            } else {
                TranslateTable(
                    state = state,
                    onInsertEntry = { index, entry -> viewModel.insertEntry(index, entry) },
                    onDeleteEntry = { index -> viewModel.deleteEntry(index) },
                    onUpdateKey = { index, key -> viewModel.updateKey(index, key) },
                    onUpdateText = { index, language, text ->
                        viewModel.updateText(index, language, text)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}
