package com.example.repairstoremanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repairstoremanager.data.model.Note
import com.example.repairstoremanager.data.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotesViewModel(
    private val notesRepository: NotesRepository = NotesRepository()
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _notes.value = notesRepository.getNotes()
            } catch (e: Exception) {
                _error.value = "Failed to load notes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getNoteById(noteId: String, onResult: (Note?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val note = notesRepository.getNoteById(noteId)
                onResult(note)
            } catch (e: Exception) {
                _error.value = "Failed to load note: ${e.message}"
                onResult(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addNote(note: Note, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = notesRepository.addNote(note)
                if (result.isSuccess) {
                    // Instead of reloading all notes, add the new note to the current list
                    val currentNotes = _notes.value.toMutableList()
                    val newNote = note.copy(id = result.getOrNull() ?: "")
                    currentNotes.add(0, newNote) // Add to top
                    _notes.value = currentNotes
                }
                onResult(result)
            } catch (e: Exception) {
                _error.value = "Failed to add note: ${e.message}"
                onResult(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateNote(note: Note, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = notesRepository.updateNote(note)
                if (result.isSuccess) {
                    // Update the specific note in the list
                    val currentNotes = _notes.value.toMutableList()
                    val index = currentNotes.indexOfFirst { it.id == note.id }
                    if (index != -1) {
                        // Preserve createdAt timestamp when updating
                        val updatedNote = note.copy(createdAt = currentNotes[index].createdAt)
                        currentNotes[index] = updatedNote
                        _notes.value = currentNotes
                    }
                }
                onResult(result)
            } catch (e: Exception) {
                _error.value = "Failed to update note: ${e.message}"
                onResult(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNote(noteId: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = notesRepository.deleteNote(noteId)
                if (result.isSuccess) {
                    // Remove the note from the current list
                    val currentNotes = _notes.value.toMutableList()
                    currentNotes.removeAll { it.id == noteId }
                    _notes.value = currentNotes
                }
                onResult(result)
            } catch (e: Exception) {
                _error.value = "Failed to delete note: ${e.message}"
                onResult(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }
}