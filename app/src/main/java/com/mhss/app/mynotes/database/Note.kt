package com.mhss.app.mynotes.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mhss.app.mynotes.R
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "notes_table")
data class Note(
    var title: String = "",
    var note: String ="",
    var date: Long = 0,
    var deleted: Boolean = false,
    var favorite: Boolean = false,
    var color: Int = R.color.note_gray,
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null
) : Parcelable