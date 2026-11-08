package com.example.data

import android.content.Context
import com.example.data.db.DictionaryWord
import com.example.data.db.MBoardDatabase
import com.example.data.db.NgramWord
import com.example.model.EmojiData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

data class SuggestionResult(
    val words: List<String>,
    val emoji: String? = null,
    val isAutoCorrect: Boolean = false
)

data class TextIssue(
    val start: Int,
    val end: Int,
    val suggestion: String
)

class SuggestionEngine(
    private val preferences: MBoardPreferences,
    private val context: Context? = null
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val database: MBoardDatabase? = context?.let { MBoardDatabase.getInstance(it) }

    // Fast in-memory cache populated from DB and lexicons
    private val memoryWords = mutableListOf<String>()
    private val shortcutsMap = mutableMapOf<String, String>()
    private val nextWordMemory = mutableMapOf<String, MutableList<String>>()

    init {
        loadStaticLexicon()
        // Asynchronously synchronize with Room DB
        scope.launch {
            initFromDatabase()
        }
    }

    private fun loadStaticLexicon() {
        val staticLexicon = listOf(
            "bonjour", "bonne", "bonsoir", "merci", "beaucoup", "salut", "comment", "allez", "vous",
            "vas-tu", "pourquoi", "quand", "quel", "quelle", "toujours", "jamais", "parfois", "souvent",
            "demain", "aujourd'hui", "hier", "maintenant", "bientôt", "après", "avant", "avec", "sans",
            "pour", "dans", "sur", "sous", "vers", "chez", "pendant", "depuis", "alors", "donc", "mais",
            "aussi", "encore", "bien", "très", "trop", "assez", "plus", "moins", "tout", "tous", "toute",
            "toutes", "rien", "personne", "chose", "monde", "temps", "heure", "jour", "nuit", "semaine",
            "mois", "année", "travail", "maison", "ami", "famille", "message", "photo", "projet", "accord",
            "d'accord", "parfait", "super", "génial", "bravo", "félicitations", "désolé", "pardon",
            "s'il", "plaît", "cordialement", "amicalement", "bises", "bisous", "rendez-vous",
            "oui", "non", "peut-être", "exactement", "certainement", "effectivement", "ensemble",
            "possible", "impossible", "rapide", "facile", "difficile", "nouveau", "nouvelle", "grand",
            "petit", "important", "urgent", "attention", "regarde", "écoute", "compris", "entendu",
            "hello", "thanks", "thank", "you", "very", "much", "please", "welcome", "good", "morning",
            "night", "afternoon", "how", "are", "what", "where", "when", "why", "who", "which",
            "there", "their", "here", "today", "tomorrow", "yesterday", "always", "never",
            "sometimes", "really", "great", "awesome", "perfect", "cool", "fine", "okay", "alright"
        )
        memoryWords.addAll(staticLexicon)

        shortcutsMap["bjr"] = "bonjour"
        shortcutsMap["mrc"] = "merci"
        shortcutsMap["bcp"] = "beaucoup"
        shortcutsMap["slt"] = "salut"
        shortcutsMap["cmt"] = "comment"
        shortcutsMap["vs"] = "vous"
        shortcutsMap["avc"] = "avec"
        shortcutsMap["pr"] = "pour"
        shortcutsMap["dmn"] = "demain"
        shortcutsMap["ajd"] = "aujourd'hui"
        shortcutsMap["mtn"] = "maintenant"
        shortcutsMap["dak"] = "d'accord"
        shortcutsMap["bnt"] = "bientôt"
        shortcutsMap["rdv"] = "rendez-vous"
        shortcutsMap["dsl"] = "désolé"
        shortcutsMap["msg"] = "message"
        shortcutsMap["taf"] = "travail"
        shortcutsMap["tjs"] = "toujours"
        shortcutsMap["jms"] = "jamais"
        shortcutsMap["vmt"] = "vraiment"
        shortcutsMap["cdlt"] = "cordialement"
        shortcutsMap["svp"] = "s'il vous plaît"
        shortcutsMap["stp"] = "s'il te plaît"
        shortcutsMap["thx"] = "thanks"
        shortcutsMap["pls"] = "please"
        shortcutsMap["tmrw"] = "tomorrow"
        shortcutsMap["omw"] = "on my way"

        // Next word bigrams
        nextWordMemory["je"] = mutableListOf("suis", "vais", "pense", "veux", "peux", "voudrais")
        nextWordMemory["tu"] = mutableListOf("es", "vas", "peux", "veux", "sais", "fais")
        nextWordMemory["il"] = mutableListOf("est", "va", "faut", "peut", "fait")
        nextWordMemory["elle"] = mutableListOf("est", "va", "veut", "peut")
        nextWordMemory["nous"] = mutableListOf("sommes", "allons", "pouvons", "avons")
        nextWordMemory["vous"] = mutableListOf("êtes", "allez", "pouvez", "voulez", "avez")
        nextWordMemory["merci"] = mutableListOf("beaucoup", "infiniment", "d'avance", "bien")
        nextWordMemory["bonjour"] = mutableListOf("à tous", "comment allez-vous", "j'espère que tu vas bien")
        nextWordMemory["d'accord"] = mutableListOf("merci", "parfait", "ça marche", "à tout à l'heure")
        nextWordMemory["à"] = mutableListOf("demain", "bientôt", "tout à l'heure", "ce soir")
        nextWordMemory["en"] = mutableListOf("route", "train de", "retard", "effet")
        nextWordMemory["s'il"] = mutableListOf("vous plaît", "te plaît")
        nextWordMemory["bonne"] = mutableListOf("journée", "soirée", "nuit", "continuation")
        nextWordMemory["cordialement"] = mutableListOf("et bonne journée", "bien à vous")
        nextWordMemory["thank"] = mutableListOf("you", "you so much", "you very much")
        nextWordMemory["good"] = mutableListOf("morning", "night", "luck", "job")
        nextWordMemory["see"] = mutableListOf("you soon", "you later", "you tomorrow")
    }

    private suspend fun initFromDatabase() {
        database?.let { db ->
            try {
                val dao = db.dictionaryDao()
                val count = dao.getCount()
                if (count == 0) {
                    MBoardDatabase.populateInitialDictionary(dao)
                }
                val topWords = dao.getTopWords(200)
                synchronized(memoryWords) {
                    topWords.forEach { word ->
                        if (!memoryWords.contains(word.word)) {
                            memoryWords.add(0, word.word)
                        }
                        word.shortcut?.let { sc ->
                            shortcutsMap[sc] = word.word
                        }
                    }
                }
                val predictionKeys = synchronized(nextWordMemory) { nextWordMemory.keys.toList() }
                predictionKeys.forEach { previousWord ->
                    val storedPredictions = dao.getNextWords(previousWord, 3)
                    if (storedPredictions.isNotEmpty()) {
                        synchronized(nextWordMemory) {
                            nextWordMemory[previousWord] = storedPredictions.toMutableList()
                        }
                    }
                }
            } catch (e: Exception) {
                // Keep memory fallback
            }
        }
    }

    fun getSuggestions(currentPrefix: String, lastWord: String?): SuggestionResult {
        val cleanPrefix = currentPrefix.trim().lowercase()

        // Emoji contextual match
        val emojiMatch = EmojiData.KEYWORD_EMOJIS[cleanPrefix]?.firstOrNull()
            ?: lastWord?.let { EmojiData.KEYWORD_EMOJIS[it.lowercase()]?.firstOrNull() }

        // 1. If prefix is empty: predict next word based on lastWord (Gboard N-gram model)
        if (cleanPrefix.isEmpty()) {
            if (lastWord != null && preferences.nextWordPrediction) {
                val cleanLast = lastWord.lowercase().trim()
                val nextWords = synchronized(nextWordMemory) { nextWordMemory[cleanLast]?.toList() }
                if (!nextWords.isNullOrEmpty()) {
                    return SuggestionResult(
                        words = nextWords.take(3),
                        emoji = emojiMatch
                    )
                }
            }
            return SuggestionResult(
                words = listOf("Bonjour", "Merci", "D'accord", "Comment", "Salut", "Oui", "Non", "Super", "Parfait", "À bientôt"),
                emoji = emojiMatch
            )
        }

        // 2. Shortcut expansion (e.g. bjr -> bonjour)
        val expandedShortcut = shortcutsMap[cleanPrefix]
        val candidateList = mutableListOf<String>()

        if (expandedShortcut != null) {
            candidateList.add(expandedShortcut)
        }

        // 3. User learned words have top priority
        val userLearned = preferences.getLearnedWords().filter { it.startsWith(cleanPrefix) }
        candidateList.addAll(userLearned)

        // 4. Exact prefix matching from memory / dictionary
        val prefixMatches = synchronized(memoryWords) {
            memoryWords.filter { it.startsWith(cleanPrefix) && it != cleanPrefix }
        }
        candidateList.addAll(prefixMatches)

        // Typo correction using edit distance when no exact match is available.
        var isAutoCorrected = false
        if (candidateList.size <= 2 && cleanPrefix.length >= 3 && preferences.autoCorrection) {
            val typoCandidate = getSpellingSuggestion(cleanPrefix)
            if (typoCandidate != null && !candidateList.contains(typoCandidate)) {
                candidateList.add(0, typoCandidate)
                isAutoCorrected = true
            }
        }

        val unique = candidateList.distinct().take(12)
        val finalWords = if (unique.isEmpty()) {
            listOf(cleanPrefix)
        } else {
            unique
        }

        return SuggestionResult(
            words = finalWords,
            emoji = emojiMatch,
            isAutoCorrect = isAutoCorrected
        )
    }

    fun getSpellingSuggestion(word: String): String? {
        val query = word.trim().lowercase()
        if (query.length < 3) return null

        val isKnownWord = synchronized(memoryWords) {
            memoryWords.any { it.equals(query, ignoreCase = true) }
        } || preferences.getLearnedWords().any { it.equals(query, ignoreCase = true) }
        if (isKnownWord) return null

        return findTypoCorrection(query)
    }

    fun findGrammarIssue(text: String): TextIssue? {
        val rules = listOf(
            Regex("\\bje\\s+est$", RegexOption.IGNORE_CASE) to "je suis",
            Regex("\\btu\\s+est$", RegexOption.IGNORE_CASE) to "tu es",
            Regex("\\bil\\s+sont$", RegexOption.IGNORE_CASE) to "il est",
            Regex("\\belle\\s+sont$", RegexOption.IGNORE_CASE) to "elle est",
            Regex("\\bnous\\s+est$", RegexOption.IGNORE_CASE) to "nous sommes",
            Regex("\\bvous\\s+est$", RegexOption.IGNORE_CASE) to "vous êtes",
            Regex("\\bils\\s+est$", RegexOption.IGNORE_CASE) to "ils sont",
            Regex("\\belles\\s+est$", RegexOption.IGNORE_CASE) to "elles sont",
            Regex("\\bje\\s+sont$", RegexOption.IGNORE_CASE) to "je suis",
            Regex("\\btu\\s+sont$", RegexOption.IGNORE_CASE) to "tu es",
            Regex("\\b(?:il|elle)\\s+sommes$", RegexOption.IGNORE_CASE) to "est",
            Regex("\\bi\\s+is$", RegexOption.IGNORE_CASE) to "I am",
            Regex("\\b(?:you|we|they)\\s+is$", RegexOption.IGNORE_CASE) to "are",
            Regex("\\b(?:he|she|it)\\s+are$", RegexOption.IGNORE_CASE) to "is",
            Regex("\\b(?:i|you|we|they)\\s+has$", RegexOption.IGNORE_CASE) to "have",
            Regex("\\b(?:he|she|it)\\s+have$", RegexOption.IGNORE_CASE) to "has"
        )

        for ((pattern, suggestion) in rules) {
            val match = pattern.find(text) ?: continue
            return TextIssue(match.range.first, match.range.last + 1, suggestion)
        }
        return null
    }

    // Dynamic learning: stores and increments word weight in Room DB
    fun learnWord(word: String, prevWord: String? = null) {
        val clean = word.trim().lowercase()
        if (clean.length < 2) return

        preferences.addLearnedWord(clean)
        synchronized(memoryWords) {
            if (!memoryWords.contains(clean)) {
                memoryWords.add(0, clean)
            }
        }

        // If prevWord exists, update n-gram prediction in memory & database
        if (prevWord != null) {
            val cleanPrev = prevWord.trim().lowercase()
            synchronized(nextWordMemory) {
                val list = nextWordMemory.getOrPut(cleanPrev) { mutableListOf() }
                if (!list.contains(clean)) {
                    list.add(0, clean)
                }
            }
        }

        scope.launch {
            database?.let { db ->
                try {
                    val dao = db.dictionaryDao()
                    val existing = dao.findWord(clean)
                    if (existing != null) {
                        dao.incrementWordFrequency(clean)
                    } else {
                        dao.insertWord(
                            DictionaryWord(
                                word = clean,
                                frequency = 5,
                                locale = if (clean.matches(Regex("^[a-zA-Zàâäéèêëîïôöùûüç]+$"))) "fr" else "en",
                                isUserLearned = true
                            )
                        )
                    }

                    if (prevWord != null) {
                        val cleanPrev = prevWord.trim().lowercase()
                        dao.insertOrIgnoreNgram(
                            NgramWord(prevWord = cleanPrev, nextWord = clean, weight = 1)
                        )
                        dao.incrementNgramWeight(cleanPrev, clean)
                    }
                } catch (e: Exception) {
                    // Ignore DB errors
                }
            }
        }
    }

    // Levenshtein distance for fuzzy typo correction
    private fun findTypoCorrection(query: String): String? {
        var bestMatch: String? = null
        var minDistance = 2 // max tolerated edit distance

        synchronized(memoryWords) {
            for (candidate in memoryWords) {
                if (kotlin.math.abs(candidate.length - query.length) > 1) continue
                val dist = levenshtein(query, candidate)
                if (dist < minDistance) {
                    minDistance = dist
                    bestMatch = candidate
                }
            }
        }
        return bestMatch
    }

    private fun levenshtein(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    dp[i - 1][j] + 1,
                    min(dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
                )
            }
        }
        return dp[s1.length][s2.length]
    }
}
