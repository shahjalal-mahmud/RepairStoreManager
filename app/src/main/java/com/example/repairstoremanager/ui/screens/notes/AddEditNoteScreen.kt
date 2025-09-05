package com.example.repairstoremanager.ui.screens.notes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.repairstoremanager.data.model.Note
import com.example.repairstoremanager.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    navController: NavHostController,
    noteId: String? = null,
    viewModel: NotesViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Load note if editing
    LaunchedEffect(noteId) {
        if (!noteId.isNullOrEmpty()) {
            val note = viewModel.notes.value.find { it.id == noteId }
            note?.let {
                title = it.title
                content = it.content
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == null) "Add Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val note = Note(
                            id = noteId ?: "",
                            title = title,
                            content = content
                        )
                        if (noteId == null) {
                            viewModel.addNote(note) { result ->
                                if (result.isSuccess) {
                                    navController.popBackStack()
                                }
                            }
                        } else {
                            viewModel.updateNote(note) { result ->
                                if (result.isSuccess) {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = TextStyle(
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = MaterialTheme.typography.titleLarge.fontWeight
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) {
                        Text(
                            "Title",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            )

            BasicTextField(
                value = content,
                onValueChange = { content = it },
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                decorationBox = { innerTextField ->
                    if (content.isEmpty()) {
                        Text(
                            "Start typing...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}