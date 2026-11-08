package com.example.model

enum class FontStyle(val id: String, val displayName: String, val previewText: String) {
    NORMAL("normal", "Normale", "Abc"),
    BOLD_SANS("bold_sans", "Gras", "𝗔𝗯𝗰"),
    ITALIC_SANS("italic_sans", "Italique", "𝘈𝘣𝘤"),
    BOLD_ITALIC("bold_italic", "Gras Italique", "𝘼𝙗𝙘"),
    BOLD_SERIF("bold_serif", "Sérif Gras", "𝐀𝐛𝐜"),
    SCRIPT("script", "Manuscrit", "𝓐𝓫𝓬"),
    FRAKTUR("fraktur", "Gothique", "𝕬𝖇𝖈"),
    DOUBLE_STRUCK("double_struck", "Double Trait", "𝔸𝕓𝕔"),
    MONOSPACE("monospace", "Machine", "𝙰𝚋𝚌"),
    CIRCLED("circled", "Bulles", "Ⓐⓑⓒ"),
    BLACK_CIRCLED("black_circled", "Bulles Noires", "🅐🅑🅒"),
    SQUARED("squared", "Carrés", "🄰🄱🄲"),
    BLACK_SQUARED("black_squared", "Carrés Noirs", "🅰🅱🅲"),
    SMALL_CAPS("small_caps", "Petites Caps", "ᴀʙᴄ"),
    UPSIDE_DOWN("upside_down", "Inversé", "ɔqɐ"),
    STRIKETHROUGH("strikethrough", "Barré", "A̶b̶c̶"),
    UNDERLINE("underline", "Souligné", "A̲b̲c̲"),
    DOUBLE_UNDERLINE("double_underline", "Double Trait", "A̳b̳c̳"),
    FULLWIDTH("fullwidth", "Esthétique", "Ａｂｃ")
}

object FontTransformer {

    fun transform(text: String, style: FontStyle): String {
        if (style == FontStyle.NORMAL || text.isEmpty()) return text

        return when (style) {
            FontStyle.NORMAL -> text
            FontStyle.BOLD_SANS -> mapChars(text, BOLD_SANS_UPPER, BOLD_SANS_LOWER, BOLD_SANS_DIGITS)
            FontStyle.ITALIC_SANS -> mapChars(text, ITALIC_SANS_UPPER, ITALIC_SANS_LOWER)
            FontStyle.BOLD_ITALIC -> mapChars(text, BOLD_ITALIC_UPPER, BOLD_ITALIC_LOWER)
            FontStyle.BOLD_SERIF -> mapChars(text, BOLD_SERIF_UPPER, BOLD_SERIF_LOWER, BOLD_SERIF_DIGITS)
            FontStyle.SCRIPT -> mapChars(text, SCRIPT_UPPER, SCRIPT_LOWER)
            FontStyle.FRAKTUR -> mapChars(text, FRAKTUR_UPPER, FRAKTUR_LOWER)
            FontStyle.DOUBLE_STRUCK -> mapChars(text, DOUBLE_STRUCK_UPPER, DOUBLE_STRUCK_LOWER, DOUBLE_STRUCK_DIGITS)
            FontStyle.MONOSPACE -> mapChars(text, MONOSPACE_UPPER, MONOSPACE_LOWER, MONOSPACE_DIGITS)
            FontStyle.CIRCLED -> mapChars(text, CIRCLED_UPPER, CIRCLED_LOWER, CIRCLED_DIGITS)
            FontStyle.BLACK_CIRCLED -> mapChars(text, BLACK_CIRCLED_UPPER, BLACK_CIRCLED_UPPER, BLACK_CIRCLED_DIGITS)
            FontStyle.SQUARED -> mapChars(text, SQUARED_UPPER, SQUARED_UPPER)
            FontStyle.BLACK_SQUARED -> mapChars(text, BLACK_SQUARED_UPPER, BLACK_SQUARED_UPPER)
            FontStyle.SMALL_CAPS -> mapSmallCaps(text)
            FontStyle.UPSIDE_DOWN -> mapUpsideDown(text)
            FontStyle.STRIKETHROUGH -> addCombiningChar(text, '\u0336')
            FontStyle.UNDERLINE -> addCombiningChar(text, '\u0332')
            FontStyle.DOUBLE_UNDERLINE -> addCombiningChar(text, '\u0333')
            FontStyle.FULLWIDTH -> mapFullwidth(text)
        }
    }

    private fun mapChars(
        text: String,
        upper: List<String>,
        lower: List<String>,
        digits: List<String>? = null
    ): String {
        val sb = StringBuilder()
        for (char in text) {
            when {
                char in 'A'..'Z' -> {
                    val idx = char - 'A'
                    sb.append(upper.getOrElse(idx) { char.toString() })
                }
                char in 'a'..'z' -> {
                    val idx = char - 'a'
                    sb.append(lower.getOrElse(idx) { char.toString() })
                }
                digits != null && char in '0'..'9' -> {
                    val idx = char - '0'
                    sb.append(digits.getOrElse(idx) { char.toString() })
                }
                else -> sb.append(char)
            }
        }
        return sb.toString()
    }

    private fun addCombiningChar(text: String, combiningChar: Char): String {
        val sb = StringBuilder()
        for (char in text) {
            sb.append(char)
            if (!char.isWhitespace()) {
                sb.append(combiningChar)
            }
        }
        return sb.toString()
    }

    private fun mapSmallCaps(text: String): String {
        val map = mapOf(
            'a' to "ᴀ", 'b' to "ʙ", 'c' to "ᴄ", 'd' to "ᴅ", 'e' to "ᴇ", 'f' to "ꜰ",
            'g' to "ɢ", 'h' to "ʜ", 'i' to "ɪ", 'j' to "ᴊ", 'k' to "ᴋ", 'l' to "ʟ",
            'm' to "ᴍ", 'n' to "ɴ", 'o' to "ᴏ", 'p' to "ᴘ", 'q' to "ꞯ", 'r' to "ʀ",
            's' to "ꜱ", 't' to "ᴛ", 'u' to "ᴜ", 'v' to "ᴠ", 'w' to "ᴡ", 'x' to "x",
            'y' to "ʏ", 'z' to "ᴢ"
        )
        val sb = StringBuilder()
        for (char in text) {
            val lower = char.lowercaseChar()
            sb.append(map[lower] ?: char.toString())
        }
        return sb.toString()
    }

    private fun mapUpsideDown(text: String): String {
        val normal = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789?!.,'\"&_"
        val upside = "ɐqɔpǝɟƃɥᴉɾʞlɯudodbɹsʇnʌʍxʎz∀ꓭƆᗡƎℲ⅁HIſʞꞀWNOԀÒᴚS⟘∩ΛMX⅄Z0ƖᄅƐㄣϛ9ㄥ86¿¡˙'‚„⅋‾"
        val sb = StringBuilder()
        for (char in text.reversed()) {
            val idx = normal.indexOf(char)
            if (idx != -1 && idx < upside.length) {
                sb.append(upside[idx])
            } else {
                sb.append(char)
            }
        }
        return sb.toString()
    }

    private fun mapFullwidth(text: String): String {
        val sb = StringBuilder()
        for (char in text) {
            val code = char.code
            if (code in 33..126) {
                sb.append((code + 65248).toChar())
            } else if (code == 32) {
                sb.append('\u3000') // Ideographic space
            } else {
                sb.append(char)
            }
        }
        return sb.toString()
    }

    // Mathematical Sans-Serif Bold (U+1D5D4 - U+1D607)
    private val BOLD_SANS_UPPER = (0x1D5D4..0x1D5ED).map { String(Character.toChars(it)) }
    private val BOLD_SANS_LOWER = (0x1D5EE..0x1D607).map { String(Character.toChars(it)) }
    private val BOLD_SANS_DIGITS = (0x1D7EC..0x1D7F5).map { String(Character.toChars(it)) }

    // Mathematical Sans-Serif Italic (U+1D608 - U+1D63B)
    private val ITALIC_SANS_UPPER = (0x1D608..0x1D621).map { String(Character.toChars(it)) }
    private val ITALIC_SANS_LOWER = (0x1D622..0x1D63B).map { String(Character.toChars(it)) }

    // Mathematical Sans-Serif Bold Italic (U+1D63C - U+1D66F)
    private val BOLD_ITALIC_UPPER = (0x1D63C..0x1D655).map { String(Character.toChars(it)) }
    private val BOLD_ITALIC_LOWER = (0x1D656..0x1D66F).map { String(Character.toChars(it)) }

    // Mathematical Serif Bold (U+1D400 - U+1D433)
    private val BOLD_SERIF_UPPER = (0x1D400..0x1D419).map { String(Character.toChars(it)) }
    private val BOLD_SERIF_LOWER = (0x1D41A..0x1D433).map { String(Character.toChars(it)) }
    private val BOLD_SERIF_DIGITS = (0x1D7CE..0x1D7D7).map { String(Character.toChars(it)) }

    // Mathematical Bold Script (U+1D4D0 - U+1D503)
    private val SCRIPT_UPPER = (0x1D4D0..0x1D4E9).map { String(Character.toChars(it)) }
    private val SCRIPT_LOWER = (0x1D4EA..0x1D503).map { String(Character.toChars(it)) }

    // Mathematical Bold Fraktur (U+1D56C - U+1D59F)
    private val FRAKTUR_UPPER = (0x1D56C..0x1D585).map { String(Character.toChars(it)) }
    private val FRAKTUR_LOWER = (0x1D586..0x1D59F).map { String(Character.toChars(it)) }

    // Double Struck / Blackboard Bold
    private val DOUBLE_STRUCK_UPPER = listOf(
        "𝔸", "𝔹", "ℂ", "𝔻", "𝔼", "𝔽", "𝔾", "ℍ", "𝕀", "𝕁", "𝕂", "𝕃", "𝕄",
        "ℕ", "𝕆", "ℙ", "ℚ", "ℝ", "𝕊", "𝕋", "𝕌", "𝕍", "𝕎", "𝕏", "𝕐", "ℤ"
    )
    private val DOUBLE_STRUCK_LOWER = listOf(
        "𝕒", "𝕓", "𝕔", "𝕕", "𝕖", "𝕗", "𝕘", "𝕙", "𝕚", "𝕛", "𝕜", "𝕝", "𝕞",
        "𝕟", "𝕠", "𝕡", "𝕢", "𝕣", "𝕤", "𝕥", "𝕦", "𝕧", "𝕨", "𝕩", "𝕪", "𝕫"
    )
    private val DOUBLE_STRUCK_DIGITS = (0x1D7D8..0x1D7E1).map { String(Character.toChars(it)) }

    // Monospace (U+1D670 - U+1D6A3)
    private val MONOSPACE_UPPER = (0x1D670..0x1D689).map { String(Character.toChars(it)) }
    private val MONOSPACE_LOWER = (0x1D68A..0x1D6A3).map { String(Character.toChars(it)) }
    private val MONOSPACE_DIGITS = (0x1D7F6..0x1D7FF).map { String(Character.toChars(it)) }

    // Circled (U+24B6 - U+24E9)
    private val CIRCLED_UPPER = (0x24B6..0x24CF).map { String(Character.toChars(it)) }
    private val CIRCLED_LOWER = (0x24D0..0x24E9).map { String(Character.toChars(it)) }
    private val CIRCLED_DIGITS = listOf("⓪", "①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨")

    // Black Circled (U+1F150 - U+1F169)
    private val BLACK_CIRCLED_UPPER = listOf(
        "🅐", "🅑", "🅒", "🅓", "🅔", "🅕", "🅖", "🅗", "🅘", "🅙", "🅚", "🅛", "🅜",
        "🅝", "🅞", "🅟", "🅠", "🅡", "🅢", "🅣", "🅤", "🅥", "🅦", "🅡", "🅨", "🅩"
    )
    private val BLACK_CIRCLED_DIGITS = listOf("⓿", "❶", "❷", "❸", "❹", "❺", "❻", "❼", "❽", "❾")

    // Squared (U+1F130 - U+1F149)
    private val SQUARED_UPPER = listOf(
        "🄰", "🄱", "🄲", "🄳", "🄴", "🄵", "🄶", "🄷", "🄸", "🄹", "🄺", "🄻", "🄼",
        "🄽", "🄾", "🄿", "🅀", "🅁", "🅂", "🅃", "🅄", "🅅", "🅆", "🅇", "🅈", "🅉"
    )

    // Black Squared / Inverted Squared (U+1F170 - U+1F189)
    private val BLACK_SQUARED_UPPER = listOf(
        "🅰", "🅱", "🅲", "🅳", "🅴", "🅵", "🅶", "🅷", "🅸", "🅹", "🅺", "🅻", "🅼",
        "🅽", "🅾", "🅿", "🆀", "🆁", "🆂", "🆃", "🆄", "🆅", "🆆", "🆇", "🆈", "🆉"
    )
}
