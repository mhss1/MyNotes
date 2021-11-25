package com.mhss.app.mynotes.database

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class EmptyTrashWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notesRepository: NoteRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        notesRepository.deleteNotesInTrash()
        return Result.success()
    }


}