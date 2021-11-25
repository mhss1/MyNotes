package com.mhss.app.mynotes.database

import javax.inject.Inject

class NoteRepository @Inject constructor(private val notesDao: NotesDao) {

    val allNotes = notesDao.getAllNotes()
    val allFavoriteNotes = notesDao.getAllFavoriteNotes()
    val allDeletedNotes = notesDao.getAllDeletedNotes()


    suspend fun insertNote(note: Note){
        notesDao.insertNote(note)
    }

    suspend fun updateNote(note: Note) {
        notesDao.updateNote(note)
    }

    suspend fun deleteNote(note: Note){
        notesDao.deleteNote(note)
    }

    suspend fun deleteNotesInTrash(){
        notesDao.deleteNotesInTrash()
    }

    suspend fun getNote(query: String): List<Note> {
        return notesDao.getNote(query)
    }

    suspend fun moveAllNotesToTrash() {
        notesDao.moveAllNotesToTrash()
    }
}