package com.example.model

enum class KeyboardLayoutType(val displayName: String, val description: String) {
    AZERTY("Français (AZERTY)", "Disposition standard française avec accès rapide aux accents"),
    QWERTY("English (QWERTY)", "Standard international layout"),
    QWERTZ("Deutsch (QWERTZ)", "Standard-Tastaturlayout für Deutsch")
}

enum class KeyboardMode {
    ALPHA,
    NUMBERS_SYMBOLS,
    MORE_SYMBOLS,
    EMOJI,
    CLIPBOARD,
    TEXT_EDITING,
    KIDS,
    HANDWRITING,
    CALCULATOR,
    TRANSLATOR
}

enum class ShiftState {
    OFF,
    ON,
    CAPS_LOCK
}

sealed class KeyAction {
    data class Character(val primary: String, val shift: String? = null, val accents: List<String> = emptyList()) : KeyAction()
    object Shift : KeyAction()
    object Backspace : KeyAction()
    object Space : KeyAction()
    object Enter : KeyAction()
    data class SwitchMode(val targetMode: KeyboardMode) : KeyAction()
    object Emoji : KeyAction()
    object Clipboard : KeyAction()
    object TextEditing : KeyAction()
    object SwitchLanguage : KeyAction()
    object HideKeyboard : KeyAction()
    object VoiceInput : KeyAction()
}

data class KeyModel(
    val action: KeyAction,
    val label: String,
    val subLabel: String? = null,
    val weight: Float = 1.0f,
    val isSpecial: Boolean = false
)

object KeyboardLayouts {
    val NUMBER_ROW = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")

    val AZERTY_ACCENTS: Map<String, List<String>> = mapOf(
        "a" to listOf("à", "â", "æ", "á", "ã", "ä", "å"),
        "e" to listOf("é", "è", "ê", "ë", "€", "ē"),
        "i" to listOf("î", "ï", "í", "ì", "ī"),
        "o" to listOf("ô", "œ", "ö", "ò", "ó", "õ"),
        "u" to listOf("ù", "û", "ü", "ú", "ū"),
        "c" to listOf("ç", "©"),
        "n" to listOf("ñ"),
        "s" to listOf("ß", "$"),
        "y" to listOf("ÿ")
    )

    val QWERTY_ACCENTS: Map<String, List<String>> = mapOf(
        "e" to listOf("é", "è", "ê", "ë"),
        "a" to listOf("á", "à", "â", "ä", "ã"),
        "i" to listOf("í", "ì", "î", "ï"),
        "o" to listOf("ó", "ò", "ô", "ö"),
        "u" to listOf("ú", "ù", "û", "ü"),
        "n" to listOf("ñ"),
        "c" to listOf("ç")
    )

    fun getDefaultOrder(layoutType: KeyboardLayoutType): String {
        return when (layoutType) {
            KeyboardLayoutType.AZERTY -> "a,z,e,r,t,y,u,i,o,p|q,s,d,f,g,h,j,k,l,m|w,x,c,v,b,n,é,è,ç"
            KeyboardLayoutType.QWERTY -> "q,w,e,r,t,y,u,i,o,p|a,s,d,f,g,h,j,k,l|z,x,c,v,b,n,m"
            KeyboardLayoutType.QWERTZ -> "q,w,e,r,t,z,u,i,o,p|a,s,d,f,g,h,j,k,l|y,x,c,v,b,n,m"
        }
    }

    fun getAlphaRows(
        layoutType: KeyboardLayoutType,
        shift: ShiftState,
        customOrder: String? = null
    ): List<List<KeyModel>> {
        val isShifted = shift != ShiftState.OFF
        if (!customOrder.isNullOrBlank()) {
            val custom = parseCustomOrder(customOrder, isShifted, layoutType)
            if (custom != null) return custom
        }
        return when (layoutType) {
            KeyboardLayoutType.AZERTY -> getAzertyRows(isShifted, shift == ShiftState.CAPS_LOCK)
            KeyboardLayoutType.QWERTY -> getQwertyRows(isShifted, shift == ShiftState.CAPS_LOCK)
            KeyboardLayoutType.QWERTZ -> getQwertzRows(isShifted, shift == ShiftState.CAPS_LOCK)
        }
    }

    private fun parseCustomOrder(
        customOrder: String,
        isShifted: Boolean,
        layoutType: KeyboardLayoutType
    ): List<List<KeyModel>>? {
        return try {
            val rowStrings = customOrder.split("|")
            if (rowStrings.size != 3) return null

            val accentsMap = if (layoutType == KeyboardLayoutType.AZERTY) AZERTY_ACCENTS else QWERTY_ACCENTS

            val r1Letters = rowStrings[0].split(",").map { it.trim().lowercase() }
            val r2Letters = rowStrings[1].split(",").map { it.trim().lowercase() }
            val r3Letters = rowStrings[2].split(",").map { it.trim().lowercase() }

            val r1 = r1Letters.mapIndexed { idx, char ->
                val numHint = if (idx < 10) (if (idx == 9) "0" else "${idx + 1}") else null
                val display = if (isShifted) char.uppercase() else char
                KeyModel(
                    action = KeyAction.Character(
                        primary = display,
                        accents = accentsMap[char]?.map { if (isShifted) it.uppercase() else it } ?: emptyList()
                    ),
                    label = display,
                    subLabel = numHint,
                    weight = 1.0f
                )
            }

            val r2 = r2Letters.map { char ->
                val display = if (isShifted) char.uppercase() else char
                KeyModel(
                    action = KeyAction.Character(
                        primary = display,
                        accents = accentsMap[char]?.map { if (isShifted) it.uppercase() else it } ?: emptyList()
                    ),
                    label = display,
                    weight = 1.0f
                )
            }

            val r3 = mutableListOf<KeyModel>()
            r3.add(KeyModel(action = KeyAction.Shift, label = "⇧", weight = 1.35f, isSpecial = true))
            r3Letters.forEach { char ->
                val display = if (isShifted) char.uppercase() else char
                r3.add(
                    KeyModel(
                        action = KeyAction.Character(
                            primary = display,
                            accents = accentsMap[char]?.map { if (isShifted) it.uppercase() else it } ?: emptyList()
                        ),
                        label = display,
                        weight = 1.0f
                    )
                )
            }
            r3.add(KeyModel(action = KeyAction.Backspace, label = "⌫", weight = 1.35f, isSpecial = true))

            listOf(r1, r2, r3)
        } catch (e: Exception) {
            null
        }
    }

    private fun getAzertyRows(isShifted: Boolean, isCapsLocked: Boolean): List<List<KeyModel>> {
        val row1Letters = listOf("a", "z", "e", "r", "t", "y", "u", "i", "o", "p")
        val row2Letters = listOf("q", "s", "d", "f", "g", "h", "j", "k", "l", "m")
        val row3Letters = listOf("w", "x", "c", "v", "b", "n", "é", "è", "ç")

        val r1 = row1Letters.mapIndexed { idx, char ->
            val numHint = if (idx < 10) (if (idx == 9) "0" else "${idx + 1}") else null
            val display = if (isShifted) char.uppercase() else char
            KeyModel(
                action = KeyAction.Character(
                    primary = display,
                    accents = AZERTY_ACCENTS[char]?.map { if (isShifted) it.uppercase() else it } ?: emptyList()
                ),
                label = display,
                subLabel = numHint,
                weight = 1.0f
            )
        }

        val r2 = row2Letters.map { char ->
            val display = if (isShifted) char.uppercase() else char
            KeyModel(
                action = KeyAction.Character(
                    primary = display,
                    accents = AZERTY_ACCENTS[char]?.map { if (isShifted) it.uppercase() else it } ?: emptyList()
                ),
                label = display,
                weight = 1.0f
            )
        }

        val r3 = mutableListOf<KeyModel>()
        r3.add(KeyModel(action = KeyAction.Shift, label = "⇧", weight = 1.35f, isSpecial = true))
        row3Letters.forEach { char ->
            val display = if (isShifted) char.uppercase() else char
            r3.add(
                KeyModel(
                    action = KeyAction.Character(
                        primary = display,
                        accents = AZERTY_ACCENTS[char]?.map { if (isShifted) it.uppercase() else it } ?: emptyList()
                    ),
                    label = display,
                    weight = 1.0f
                )
            )
        }
        r3.add(KeyModel(action = KeyAction.Backspace, label = "⌫", weight = 1.35f, isSpecial = true))

        return listOf(r1, r2, r3)
    }

    private fun getQwertyRows(isShifted: Boolean, isCapsLocked: Boolean): List<List<KeyModel>> {
        val row1Letters = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
        val row2Letters = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
        val row3Letters = listOf("z", "x", "c", "v", "b", "n", "m")

        val r1 = row1Letters.mapIndexed { idx, char ->
            val numHint = if (idx == 9) "0" else "${idx + 1}"
            val display = if (isShifted) char.uppercase() else char
            KeyModel(
                action = KeyAction.Character(
                    primary = display,
                    accents = QWERTY_ACCENTS[char]?.map { if (isShifted) it.uppercase() else it } ?: emptyList()
                ),
                label = display,
                subLabel = numHint,
                weight = 1.0f
            )
        }

        val r2 = row2Letters.map { char ->
            val display = if (isShifted) char.uppercase() else char
            KeyModel(
                action = KeyAction.Character(
                    primary = display,
                    accents = QWERTY_ACCENTS[char]?.map { if (isShifted) it.uppercase() else it } ?: emptyList()
                ),
                label = display,
                weight = 1.0f
            )
        }

        val r3 = mutableListOf<KeyModel>()
        r3.add(KeyModel(action = KeyAction.Shift, label = "⇧", weight = 1.5f, isSpecial = true))
        row3Letters.forEach { char ->
            val display = if (isShifted) char.uppercase() else char
            r3.add(
                KeyModel(
                    action = KeyAction.Character(
                        primary = display,
                        accents = QWERTY_ACCENTS[char]?.map { if (isShifted) it.uppercase() else it } ?: emptyList()
                    ),
                    label = display,
                    weight = 1.0f
                )
            )
        }
        r3.add(KeyModel(action = KeyAction.Backspace, label = "⌫", weight = 1.5f, isSpecial = true))

        return listOf(r1, r2, r3)
    }

    private fun getQwertzRows(isShifted: Boolean, isCapsLocked: Boolean): List<List<KeyModel>> {
        val row1Letters = listOf("q", "w", "e", "r", "t", "z", "u", "i", "o", "p")
        val row2Letters = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
        val row3Letters = listOf("y", "x", "c", "v", "b", "n", "m")

        val r1 = row1Letters.mapIndexed { idx, char ->
            val numHint = if (idx == 9) "0" else "${idx + 1}"
            val display = if (isShifted) char.uppercase() else char
            KeyModel(
                action = KeyAction.Character(primary = display),
                label = display,
                subLabel = numHint,
                weight = 1.0f
            )
        }

        val r2 = row2Letters.map { char ->
            val display = if (isShifted) char.uppercase() else char
            KeyModel(
                action = KeyAction.Character(primary = display),
                label = display,
                weight = 1.0f
            )
        }

        val r3 = mutableListOf<KeyModel>()
        r3.add(KeyModel(action = KeyAction.Shift, label = "⇧", weight = 1.5f, isSpecial = true))
        row3Letters.forEach { char ->
            val display = if (isShifted) char.uppercase() else char
            r3.add(
                KeyModel(
                    action = KeyAction.Character(primary = display),
                    label = display,
                    weight = 1.0f
                )
            )
        }
        r3.add(KeyModel(action = KeyAction.Backspace, label = "⌫", weight = 1.5f, isSpecial = true))

        return listOf(r1, r2, r3)
    }

    fun getSymbols1Rows(): List<List<KeyModel>> {
        val r1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").map {
            KeyModel(KeyAction.Character(it), it)
        }
        val r2 = listOf("@", "#", "€", "$", "%", "&", "-", "+", "(", ")").map {
            KeyModel(KeyAction.Character(it), it)
        }
        val r3 = mutableListOf<KeyModel>()
        r3.add(KeyModel(KeyAction.SwitchMode(KeyboardMode.MORE_SYMBOLS), "=\\<", weight = 1.4f, isSpecial = true))
        listOf("*", "\"", "'", ":", ";", "!", "?").forEach {
            r3.add(KeyModel(KeyAction.Character(it), it))
        }
        r3.add(KeyModel(KeyAction.Backspace, "⌫", weight = 1.4f, isSpecial = true))

        return listOf(r1, r2, r3)
    }

    fun getSymbols2Rows(): List<List<KeyModel>> {
        val r1 = listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆").map {
            KeyModel(KeyAction.Character(it), it)
        }
        val r2 = listOf("£", "¥", "$", "¢", "^", "°", "=", "{", "}", "\\").map {
            KeyModel(KeyAction.Character(it), it)
        }
        val r3 = mutableListOf<KeyModel>()
        r3.add(KeyModel(KeyAction.SwitchMode(KeyboardMode.NUMBERS_SYMBOLS), "?123", weight = 1.4f, isSpecial = true))
        listOf("%", "©", "®", "™", "[", "]", "<", ">").forEach {
            r3.add(KeyModel(KeyAction.Character(it), it))
        }
        r3.add(KeyModel(KeyAction.Backspace, "⌫", weight = 1.4f, isSpecial = true))

        return listOf(r1, r2, r3)
    }
}
