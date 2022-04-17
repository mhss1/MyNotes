package com.mhss.app.mynotes.database

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DeleteNoteWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notesRepository: NoteRepositoryImpl
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val id = inputData.getInt("id", 0)
        notesRepository.deleteNoteById(id)
        return Result.success()
    }


}