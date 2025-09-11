package com.example.repairstoremanager.ui.screens.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.repairstoremanager.data.model.Note
import com.example.repairstoremanager.viewmodel.NotesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    navController: NavHostController,
    noteId: String? = null,
    viewModel: NotesViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Note.COLOR_DEFAULT) }
    var tags by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val isEditing = !noteId.isNullOrEmpty()
    var isLoading by remember { mutableStateOf(false) }
    val isDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    // Load note if editing
    LaunchedEffect(noteId) {
        if (isEditing && noteId != null) {
            isLoading = true
            try {
                // Use coroutine context to avoid UI freezing
                withContext(Dispatchers.IO) {
                    val note = viewModel.getNoteById(noteId)
                    note?.let {
                        title = it.title
                        content = it.content
                        selectedColor = it.color
                        tags = it.tags.joinToString(",")
                    }
                }
            } catch (e: Exception) {
                // Handle error - you might want to show a snackbar here
            } finally {
                isLoading = false
            }
        }
    }

    // Auto-focus on title field only when not editing or when data is loaded
    LaunchedEffect(title, isEditing, isLoading) {
        if (title.isEmpty() && !isEditing && !isLoading) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (!isEditing) "Add Note" else "Edit Note",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    if (!isLoading) {
                        IconButton(
                            onClick = {
                                val noteTags = tags.split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }

                                val note = Note(
                                    id = noteId ?: "",
                                    title = title,
                                    content = content,
                                    color = selectedColor,
                                    tags = noteTags
                                )

                                if (!isEditing) {
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
                            },
                            enabled = title.isNotEmpty() || content.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Save",
                                tint = if (title.isNotEmpty() || content.isNotEmpty()) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading note...", color = MaterialTheme.colorScheme.onSurface)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Color selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Color:",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val colors = listOf(
                        Note.COLOR_DEFAULT to Note.getBackgroundColor(Note.COLOR_DEFAULT, isDarkTheme),
                        Note.COLOR_BLUE to Note.getBackgroundColor(Note.COLOR_BLUE, isDarkTheme),
                        Note.COLOR_GREEN to Note.getBackgroundColor(Note.COLOR_GREEN, isDarkTheme),
                        Note.COLOR_YELLOW to Note.getBackgroundColor(Note.COLOR_YELLOW, isDarkTheme),
                        Note.COLOR_RED to Note.getBackgroundColor(Note.COLOR_RED, isDarkTheme),
                        Note.COLOR_PURPLE to Note.getBackgroundColor(Note.COLOR_PURPLE, isDarkTheme)
                    )

                    colors.forEach { (colorValue, color) ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = colorValue }
                                .then(
                                    if (selectedColor == colorValue) {
                                        Modifier
                                            .padding(3.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            )
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    }
                }

                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .focusRequester(focusRequester),
                    decorationBox = { innerTextField ->
                        if (title.isEmpty()) {
                            Text(
                                "Title",
                                style = TextStyle(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                        }
                        innerTextField()
                    }
                )

                BasicTextField(
                    value = content,
                    onValueChange = { content = it },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(200.dp),
                    decorationBox = { innerTextField ->
                        if (content.isEmpty()) {
                            Text(
                                "Start typing...",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                        }
                        innerTextField()
                    }
                )

                // Tags input
                BasicTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    decorationBox = { innerTextField ->
                        if (tags.isEmpty()) {
                            Text(
                                "Tags (comma separated)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }
    }
}