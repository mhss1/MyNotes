package com.mhss.app.mynotes.di

import android.content.Context
import androidx.room.Room
import com.mhss.app.mynotes.database.NoteRepositoryImpl
import com.mhss.app.mynotes.database.NoteRepository
import com.mhss.app.mynotes.database.NotesDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideNotesDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, NotesDatabase::class.java, NotesDatabase.DB_NAME)
            .build()

    @Singleton
    @Provides
    fun provideDoa(db: NotesDatabase) = db.notesDao()

    @Module
    @InstallIn(SingletonComponent::class)
    interface RepositoryModule {
        @Binds
        fun noteRepository(repository: NoteRepositoryImpl): NoteRepository
    }
}