package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DictionaryWord::class, NgramWord::class],
    version = 1,
    exportSchema = false
)
abstract class MBoardDatabase : RoomDatabase() {

    abstract fun dictionaryDao(): DictionaryDao

    companion object {
        @Volatile
        private var INSTANCE: MBoardDatabase? = null

        fun getInstance(context: Context): MBoardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MBoardDatabase::class.java,
                    "mboard_dictionary.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate database asynchronously
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    populateInitialDictionary(database.dictionaryDao())
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialDictionary(dao: DictionaryDao) {
            val initialWords = listOf(
                // French high-frequency vocabulary
                DictionaryWord(word = "bonjour", frequency = 120, locale = "fr", shortcut = "bjr"),
                DictionaryWord(word = "merci", frequency = 110, locale = "fr", shortcut = "mrc"),
                DictionaryWord(word = "beaucoup", frequency = 95, locale = "fr", shortcut = "bcp"),
                DictionaryWord(word = "salut", frequency = 90, locale = "fr", shortcut = "slt"),
                DictionaryWord(word = "comment", frequency = 85, locale = "fr", shortcut = "cmt"),
                DictionaryWord(word = "allez", frequency = 70, locale = "fr"),
                DictionaryWord(word = "vous", frequency = 130, locale = "fr", shortcut = "vs"),
                DictionaryWord(word = "avec", frequency = 100, locale = "fr", shortcut = "avc"),
                DictionaryWord(word = "dans", frequency = 105, locale = "fr"),
                DictionaryWord(word = "pour", frequency = 115, locale = "fr", shortcut = "pr"),
                DictionaryWord(word = "demain", frequency = 80, locale = "fr", shortcut = "dmn"),
                DictionaryWord(word = "aujourd'hui", frequency = 85, locale = "fr", shortcut = "ajd"),
                DictionaryWord(word = "maintenant", frequency = 75, locale = "fr", shortcut = "mtn"),
                DictionaryWord(word = "d'accord", frequency = 90, locale = "fr", shortcut = "dak"),
                DictionaryWord(word = "parfait", frequency = 85, locale = "fr"),
                DictionaryWord(word = "super", frequency = 80, locale = "fr"),
                DictionaryWord(word = "bientôt", frequency = 75, locale = "fr", shortcut = "bnt"),
                DictionaryWord(word = "rendez-vous", frequency = 70, locale = "fr", shortcut = "rdv"),
                DictionaryWord(word = "désolé", frequency = 65, locale = "fr", shortcut = "dsl"),
                DictionaryWord(word = "message", frequency = 60, locale = "fr", shortcut = "msg"),
                DictionaryWord(word = "travail", frequency = 60, locale = "fr", shortcut = "taf"),
                DictionaryWord(word = "maison", frequency = 60, locale = "fr"),
                DictionaryWord(word = "famille", frequency = 55, locale = "fr"),
                DictionaryWord(word = "temps", frequency = 65, locale = "fr"),
                DictionaryWord(word = "heure", frequency = 65, locale = "fr"),
                DictionaryWord(word = "semaine", frequency = 55, locale = "fr"),
                DictionaryWord(word = "toujours", frequency = 70, locale = "fr", shortcut = "tjs"),
                DictionaryWord(word = "jamais", frequency = 60, locale = "fr", shortcut = "jms"),
                DictionaryWord(word = "vraiment", frequency = 65, locale = "fr", shortcut = "vmt"),
                DictionaryWord(word = "aussi", frequency = 75, locale = "fr"),
                DictionaryWord(word = "encore", frequency = 70, locale = "fr"),
                DictionaryWord(word = "peut-être", frequency = 65, locale = "fr"),
                DictionaryWord(word = "cordialement", frequency = 60, locale = "fr", shortcut = "cdlt"),
                DictionaryWord(word = "amicalement", frequency = 50, locale = "fr"),
                DictionaryWord(word = "bisous", frequency = 60, locale = "fr"),
                DictionaryWord(word = "bonne", frequency = 80, locale = "fr"),
                DictionaryWord(word = "soirée", frequency = 70, locale = "fr"),
                DictionaryWord(word = "journée", frequency = 75, locale = "fr"),
                DictionaryWord(word = "nuit", frequency = 65, locale = "fr"),
                DictionaryWord(word = "semaine", frequency = 55, locale = "fr"),
                DictionaryWord(word = "projet", frequency = 50, locale = "fr"),
                DictionaryWord(word = "possible", frequency = 55, locale = "fr"),
                DictionaryWord(word = "important", frequency = 50, locale = "fr"),
                DictionaryWord(word = "urgent", frequency = 45, locale = "fr"),
                DictionaryWord(word = "attention", frequency = 45, locale = "fr"),

                // English high-frequency vocabulary
                DictionaryWord(word = "hello", frequency = 100, locale = "en"),
                DictionaryWord(word = "thanks", frequency = 95, locale = "en", shortcut = "thx"),
                DictionaryWord(word = "thank", frequency = 90, locale = "en"),
                DictionaryWord(word = "please", frequency = 85, locale = "en", shortcut = "pls"),
                DictionaryWord(word = "welcome", frequency = 75, locale = "en"),
                DictionaryWord(word = "awesome", frequency = 70, locale = "en"),
                DictionaryWord(word = "perfect", frequency = 75, locale = "en"),
                DictionaryWord(word = "tomorrow", frequency = 70, locale = "en", shortcut = "tmrw"),
                DictionaryWord(word = "yesterday", frequency = 65, locale = "en"),
                DictionaryWord(word = "meeting", frequency = 60, locale = "en"),
                DictionaryWord(word = "project", frequency = 55, locale = "en"),
                DictionaryWord(word = "together", frequency = 50, locale = "en")
            )
            dao.insertWords(initialWords)

            val initialNgrams = listOf(
                NgramWord(prevWord = "bonjour", nextWord = "à tous", weight = 15),
                NgramWord(prevWord = "bonjour", nextWord = "comment allez-vous", weight = 12),
                NgramWord(prevWord = "merci", nextWord = "beaucoup", weight = 30),
                NgramWord(prevWord = "merci", nextWord = "infiniment", weight = 18),
                NgramWord(prevWord = "merci", nextWord = "d'avance", weight = 16),
                NgramWord(prevWord = "merci", nextWord = "bien", weight = 14),
                NgramWord(prevWord = "d'accord", nextWord = "merci", weight = 20),
                NgramWord(prevWord = "d'accord", nextWord = "ça marche", weight = 18),
                NgramWord(prevWord = "d'accord", nextWord = "parfait", weight = 15),
                NgramWord(prevWord = "je", nextWord = "suis", weight = 25),
                NgramWord(prevWord = "je", nextWord = "vais", weight = 22),
                NgramWord(prevWord = "je", nextWord = "pense", weight = 18),
                NgramWord(prevWord = "je", nextWord = "peux", weight = 17),
                NgramWord(prevWord = "je", nextWord = "voudrais", weight = 16),
                NgramWord(prevWord = "tu", nextWord = "es", weight = 24),
                NgramWord(prevWord = "tu", nextWord = "vas", weight = 20),
                NgramWord(prevWord = "tu", nextWord = "peux", weight = 18),
                NgramWord(prevWord = "nous", nextWord = "sommes", weight = 20),
                NgramWord(prevWord = "nous", nextWord = "allons", weight = 18),
                NgramWord(prevWord = "vous", nextWord = "êtes", weight = 22),
                NgramWord(prevWord = "vous", nextWord = "pouvez", weight = 19),
                NgramWord(prevWord = "bonne", nextWord = "journée", weight = 28),
                NgramWord(prevWord = "bonne", nextWord = "soirée", weight = 25),
                NgramWord(prevWord = "bonne", nextWord = "nuit", weight = 22),
                NgramWord(prevWord = "à", nextWord = "demain", weight = 25),
                NgramWord(prevWord = "à", nextWord = "bientôt", weight = 24),
                NgramWord(prevWord = "à", nextWord = "tout à l'heure", weight = 22),
                NgramWord(prevWord = "s'il", nextWord = "vous plaît", weight = 28),
                NgramWord(prevWord = "s'il", nextWord = "te plaît", weight = 25),
                NgramWord(prevWord = "en", nextWord = "route", weight = 20),
                NgramWord(prevWord = "en", nextWord = "retard", weight = 18),
                NgramWord(prevWord = "cordialement", nextWord = "et bonne journée", weight = 15),
                NgramWord(prevWord = "thank", nextWord = "you", weight = 30),
                NgramWord(prevWord = "thank", nextWord = "you so much", weight = 25),
                NgramWord(prevWord = "good", nextWord = "morning", weight = 25),
                NgramWord(prevWord = "good", nextWord = "night", weight = 20),
                NgramWord(prevWord = "see", nextWord = "you soon", weight = 20)
            )
            dao.insertNgrams(initialNgrams)
        }
    }
}
