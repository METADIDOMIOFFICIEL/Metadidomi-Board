package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dictionary_words",
    indices = [
        Index(value = ["word"], unique = true),
        Index(value = ["locale", "frequency"])
    ]
)
data class DictionaryWord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val frequency: Int = 1,
    val locale: String = "fr",
    val shortcut: String? = null,
    val isUserLearned: Boolean = false,
    val lastUsedTimestamp: Long = System.currentTimeMillis()
)
