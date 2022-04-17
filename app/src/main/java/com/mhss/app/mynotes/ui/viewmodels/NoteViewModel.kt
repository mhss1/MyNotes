package com.mhss.app.mynotes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.mynotes.database.Note
import com.mhss.app.mynotes.database.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(private val repository: NoteRepository) : ViewModel() {

    val allNotes = repository.allNotes
    val allFavoriteNotes = repository.allFavoriteNotes
    val allDeletedNotes = repository.allDeletedNotes

    private var _isNoteFavorite: Boolean? = null
    val isNoteFavorite
        get() = _isNoteFavorite

    fun setNoteFavorite(favorite: Boolean) {
        _isNoteFavorite = favorite
    }


    fun getNote(query: String, getList: (List<Note>) -> Unit) = viewModelScope.launch {
        getList(repository.getNote("%${query}%"))
    }

    fun insertNote(note: Note) = viewModelScope.launch {
        repository.insertNote(note)
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        repository.deleteNote(note)
    }

    fun deleteNotesInTrash() = viewModelScope.launch {
        repository.deleteNotesInTrash()
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        repository.updateNote(note)
    }

    fun moveAllNotesToTrash() = viewModelScope.launch {
        repository.moveAllNotesToTrash()
    }
}