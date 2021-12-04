package com.mhss.app.mynotes.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes_table WHERE id = :id")
    suspend fun deleteNoteById(id: Int)

    @Query("UPDATE notes_table SET deleted = 1 WHERE deleted = 0")
    suspend fun moveAllNotesToTrash()

    @Query("DELETE FROM notes_table WHERE deleted = 1")
    suspend fun deleteNotesInTrash()

    @Query("SELECT * from notes_table WHERE deleted = 0 ORDER BY date DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * from notes_table WHERE favorite = 1 AND deleted = 0")
    fun getAllFavoriteNotes(): Flow<List<Note>>

    @Query("SELECT * from notes_table WHERE deleted = 1 ")
    fun getAllDeletedNotes(): Flow<List<Note>>

    @Query("SELECT * from notes_table WHERE title LIKE :query OR note LIKE :query AND deleted = 0")
    suspend fun getNote(query: String): List<Note>


}