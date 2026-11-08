package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DictionaryDao {

    @Query("SELECT * FROM dictionary_words WHERE word LIKE :prefix || '%' ORDER BY frequency DESC, lastUsedTimestamp DESC LIMIT :limit")
    suspend fun getWordsStartingWith(prefix: String, limit: Int = 10): List<DictionaryWord>

    @Query("SELECT * FROM dictionary_words WHERE word = :exactWord LIMIT 1")
    suspend fun findWord(exactWord: String): DictionaryWord?

    @Query("SELECT * FROM dictionary_words WHERE shortcut = :shortcut LIMIT 1")
    suspend fun findByShortcut(shortcut: String): DictionaryWord?

    @Query("SELECT * FROM dictionary_words ORDER BY frequency DESC LIMIT :limit")
    suspend fun getTopWords(limit: Int = 50): List<DictionaryWord>

    @Query("SELECT * FROM dictionary_words WHERE isUserLearned = 1 ORDER BY lastUsedTimestamp DESC")
    suspend fun getUserLearnedWords(): List<DictionaryWord>

    @Query("SELECT COUNT(*) FROM dictionary_words")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<DictionaryWord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: DictionaryWord): Long

    @Update
    suspend fun updateWord(word: DictionaryWord)

    @Query("UPDATE dictionary_words SET frequency = frequency + 1, lastUsedTimestamp = :timestamp WHERE word = :word")
    suspend fun incrementWordFrequency(word: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM dictionary_words WHERE word = :word")
    suspend fun deleteWord(word: String)

    @Query("DELETE FROM dictionary_words WHERE isUserLearned = 1")
    suspend fun clearUserLearnedWords()

    // N-grams (Bigram next-word prediction)
    @Query("SELECT nextWord FROM ngram_predictions WHERE prevWord = :prevWord ORDER BY weight DESC LIMIT :limit")
    suspend fun getNextWords(prevWord: String, limit: Int = 5): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNgrams(ngrams: List<NgramWord>)

    @Query("UPDATE ngram_predictions SET weight = weight + 1 WHERE prevWord = :prevWord AND nextWord = :nextWord")
    suspend fun incrementNgramWeight(prevWord: String, nextWord: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreNgram(ngram: NgramWord): Long
}
