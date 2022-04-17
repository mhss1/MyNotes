package com.mhss.app.mynotes.database

import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(private val notesDao: NotesDao) : NoteRepository {

    override val allNotes = notesDao.getAllNotes()
    override val allFavoriteNotes = notesDao.getAllFavoriteNotes()
    override val allDeletedNotes = notesDao.getAllDeletedNotes()


    override suspend fun insertNote(note: Note){
        notesDao.insertNote(note)
    }

    override suspend fun updateNote(note: Note) {
        notesDao.updateNote(note)
    }

    override suspend fun deleteNote(note: Note){
        notesDao.deleteNote(note)
    }

    override suspend fun deleteNoteById(id: Int){
        notesDao.deleteNoteById(id)
    }

    override suspend fun deleteNotesInTrash(){
        notesDao.deleteNotesInTrash()
    }

    override suspend fun getNote(query: String): List<Note> {
        return notesDao.getNote(query)
    }

    override suspend fun moveAllNotesToTrash() {
        notesDao.moveAllNotesToTrash()
    }
}