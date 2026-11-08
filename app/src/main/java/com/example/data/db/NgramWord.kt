package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ngram_predictions",
    indices = [
        Index(value = ["prevWord", "nextWord"], unique = true),
        Index(value = ["prevWord", "weight"])
    ]
)
data class NgramWord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val prevWord: String,
    val nextWord: String,
    val weight: Int = 1
)
