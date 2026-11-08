package com.example.model

import androidx.compose.ui.graphics.Color

enum class KeyboardThemeId(val displayName: String, val description: String) {
    // African Country Themes (Cameroon is default)
    CAMEROON("Cameroun 🇨🇲", "Couleurs nationales Vert-Rouge-Jaune avec l'étoile d'or"),
    SENEGAL("Sénégal 🇸🇳", "Tricolore Vert-Jaune-Rouge orné de l'étoile verte"),
    COTE_D_IVOIRE("Côte d'Ivoire 🇨🇮", "Orange chaud, blanc pur et vert émeraude"),
    CONGO_DRC("RD Congo 🇨🇩", "Bleu ciel profond, bande rouge-jaune et étoile dorée"),
    NIGERIA("Nigéria 🇳🇬", "Vert forêt emblématique et blanc pur moderne"),
    MALI("Mali 🇲🇱", "Couleurs panafricaines Vert, Or lumineux et Rouge"),
    SOUTH_AFRICA("Afrique du Sud 🇿🇦", "La nation arc-en-ciel aux touches vert, or, rouge et bleu"),
    MOROCCO("Maroc 🇲🇦", "Rouge royal majestueux avec accents vert pentagramme"),

    // Classic & Modern Themes
    GBOARD_BLUE("Bleu MBoard", "Touches bleues modernes et contrastées"),
    DARK_AMOLED("Sombre AMOLED", "Noir absolu économe en énergie avec fort contraste"),
    CLEAN_LIGHT("Blanc Minimaliste", "Élégant, clair et lumineux"),
    LAVENDER_PASTEL("Lavande Pastel", "Douceur violette et finitions modernes"),
    MINT_EMERALD("Menthe Fraîche", "Vert émeraude revigorant et touches douces"),
    NEON_CYBER("Nuit Cyberpunk", "Bleu nuit avec accents néon turquoise et rose"),
    SUNSET_CORAL("Coucher de Soleil", "Dégradé chaud corail, orange et ambre")
}

data class KeyboardThemeColors(
    val id: KeyboardThemeId,
    val background: Color,
    val toolbarBackground: Color,
    val keyBackground: Color,
    val keyTextColor: Color,
    val keySubTextColor: Color,
    val specialKeyBackground: Color,
    val specialKeyTextColor: Color,
    val actionKeyBackground: Color,
    val actionKeyTextColor: Color,
    val suggestionTextColor: Color,
    val popupBackground: Color,
    val isDark: Boolean = true
)

object KeyboardThemes {
    // 🇨🇲 Cameroun (Default Theme)
    val Cameroon = KeyboardThemeColors(
        id = KeyboardThemeId.CAMEROON,
        background = Color(0xFF0C2417),
        toolbarBackground = Color(0xFF07190F),
        keyBackground = Color(0xFF163E26),
        keyTextColor = Color(0xFFFFFFFF),
        keySubTextColor = Color(0xFFFCD116),
        specialKeyBackground = Color(0xFF9E1020),
        specialKeyTextColor = Color(0xFFFCD116),
        actionKeyBackground = Color(0xFFCE1126),
        actionKeyTextColor = Color(0xFFFCD116),
        suggestionTextColor = Color(0xFFFCD116),
        popupBackground = Color(0xFF163E26),
        isDark = true
    )

    // 🇸🇳 Sénégal
    val Senegal = KeyboardThemeColors(
        id = KeyboardThemeId.SENEGAL,
        background = Color(0xFF0F2618),
        toolbarBackground = Color(0xFF081A0F),
        keyBackground = Color(0xFF1B442A),
        keyTextColor = Color(0xFFFFFFFF),
        keySubTextColor = Color(0xFFFDEF42),
        specialKeyBackground = Color(0xFFB52626),
        specialKeyTextColor = Color(0xFFFDEF42),
        actionKeyBackground = Color(0xFF00853F),
        actionKeyTextColor = Color(0xFFFDEF42),
        suggestionTextColor = Color(0xFFFDEF42),
        popupBackground = Color(0xFF1B442A),
        isDark = true
    )

    // 🇨🇮 Côte d'Ivoire
    val CoteDIvoire = KeyboardThemeColors(
        id = KeyboardThemeId.COTE_D_IVOIRE,
        background = Color(0xFF1C1A17),
        toolbarBackground = Color(0xFF12100E),
        keyBackground = Color(0xFF2C2824),
        keyTextColor = Color(0xFFFFFFFF),
        keySubTextColor = Color(0xFFF77F00),
        specialKeyBackground = Color(0xFF1E3A2B),
        specialKeyTextColor = Color(0xFF48CAE4),
        actionKeyBackground = Color(0xFFF77F00),
        actionKeyTextColor = Color(0xFFFFFFFF),
        suggestionTextColor = Color(0xFF009639),
        popupBackground = Color(0xFF2C2824),
        isDark = true
    )

    // 🇨🇩 République Démocratique du Congo
    val CongoDrc = KeyboardThemeColors(
        id = KeyboardThemeId.CONGO_DRC,
        background = Color(0xFF0A192F),
        toolbarBackground = Color(0xFF051020),
        keyBackground = Color(0xFF132A4A),
        keyTextColor = Color(0xFFFFFFFF),
        keySubTextColor = Color(0xFFF7D117),
        specialKeyBackground = Color(0xFF9E1B28),
        specialKeyTextColor = Color(0xFFF7D117),
        actionKeyBackground = Color(0xFFCE1021),
        actionKeyTextColor = Color(0xFFF7D117),
        suggestionTextColor = Color(0xFF007FFF),
        popupBackground = Color(0xFF132A4A),
        isDark = true
    )

    // 🇳🇬 Nigéria
    val Nigeria = KeyboardThemeColors(
        id = KeyboardThemeId.NIGERIA,
        background = Color(0xFF0D2117),
        toolbarBackground = Color(0xFF07140E),
        keyBackground = Color(0xFF143B27),
        keyTextColor = Color(0xFFFFFFFF),
        keySubTextColor = Color(0xFF98DFB6),
        specialKeyBackground = Color(0xFF0E2E1E),
        specialKeyTextColor = Color(0xFFFFFFFF),
        actionKeyBackground = Color(0xFF008751),
        actionKeyTextColor = Color(0xFFFFFFFF),
        suggestionTextColor = Color(0xFF00B060),
        popupBackground = Color(0xFF143B27),
        isDark = true
    )

    // 🇲🇱 Mali
    val Mali = KeyboardThemeColors(
        id = KeyboardThemeId.MALI,
        background = Color(0xFF1C2212),
        toolbarBackground = Color(0xFF12170B),
        keyBackground = Color(0xFF2E381E),
        keyTextColor = Color(0xFFFFFFFF),
        keySubTextColor = Color(0xFFFCD116),
        specialKeyBackground = Color(0xFF9B1D20),
        specialKeyTextColor = Color(0xFFFCD116),
        actionKeyBackground = Color(0xFF14B53A),
        actionKeyTextColor = Color(0xFFFFFFFF),
        suggestionTextColor = Color(0xFFFCD116),
        popupBackground = Color(0xFF2E381E),
        isDark = true
    )

    // 🇿🇦 Afrique du Sud
    val SouthAfrica = KeyboardThemeColors(
        id = KeyboardThemeId.SOUTH_AFRICA,
        background = Color(0xFF15191C),
        toolbarBackground = Color(0xFF0D1012),
        keyBackground = Color(0xFF242C31),
        keyTextColor = Color(0xFFFFFFFF),
        keySubTextColor = Color(0xFFFFB612),
        specialKeyBackground = Color(0xFF1E382B),
        specialKeyTextColor = Color(0xFF007749),
        actionKeyBackground = Color(0xFFDE3831),
        actionKeyTextColor = Color(0xFFFFFFFF),
        suggestionTextColor = Color(0xFFFFB612),
        popupBackground = Color(0xFF242C31),
        isDark = true
    )

    // 🇲🇦 Maroc
    val Morocco = KeyboardThemeColors(
        id = KeyboardThemeId.MOROCCO,
        background = Color(0xFF261214),
        toolbarBackground = Color(0xFF1A0A0C),
        keyBackground = Color(0xFF3F1D20),
        keyTextColor = Color(0xFFFFFFFF),
        keySubTextColor = Color(0xFF006233),
        specialKeyBackground = Color(0xFF183822),
        specialKeyTextColor = Color(0xFF48C774),
        actionKeyBackground = Color(0xFFC1272D),
        actionKeyTextColor = Color(0xFF00FF7F),
        suggestionTextColor = Color(0xFF48C774),
        popupBackground = Color(0xFF3F1D20),
        isDark = true
    )

    // Classic & Modern Themes
    val GBoardBlue = KeyboardThemeColors(
        id = KeyboardThemeId.GBOARD_BLUE,
        background = Color(0xFF1E2124),
        toolbarBackground = Color(0xFF181A1D),
        keyBackground = Color(0xFF2E3238),
        keyTextColor = Color(0xFFFFFFFF),
        keySubTextColor = Color(0xFF9AA0A6),
        specialKeyBackground = Color(0xFF262A2E),
        specialKeyTextColor = Color(0xFFE8EAED),
        actionKeyBackground = Color(0xFF1A73E8),
        actionKeyTextColor = Color(0xFFFFFFFF),
        suggestionTextColor = Color(0xFF8AB4F8),
        popupBackground = Color(0xFF3C4043),
        isDark = true
    )

    val DarkAmoled = KeyboardThemeColors(
        id = KeyboardThemeId.DARK_AMOLED,
        background = Color(0xFF000000),
        toolbarBackground = Color(0xFF080808),
        keyBackground = Color(0xFF181818),
        keyTextColor = Color(0xFFEEEEEE),
        keySubTextColor = Color(0xFF757575),
        specialKeyBackground = Color(0xFF121212),
        specialKeyTextColor = Color(0xFFCCCCCC),
        actionKeyBackground = Color(0xFF00E5FF),
        actionKeyTextColor = Color(0xFF000000),
        suggestionTextColor = Color(0xFF00E5FF),
        popupBackground = Color(0xFF252525),
        isDark = true
    )

    val CleanLight = KeyboardThemeColors(
        id = KeyboardThemeId.CLEAN_LIGHT,
        background = Color(0xFFECEFF1),
        toolbarBackground = Color(0xFFE0E0E0),
        keyBackground = Color(0xFFFFFFFF),
        keyTextColor = Color(0xFF202124),
        keySubTextColor = Color(0xFF70757A),
        specialKeyBackground = Color(0xFFCFD8DC),
        specialKeyTextColor = Color(0xFF37474F),
        actionKeyBackground = Color(0xFF1976D2),
        actionKeyTextColor = Color(0xFFFFFFFF),
        suggestionTextColor = Color(0xFF1976D2),
        popupBackground = Color(0xFFFFFFFF),
        isDark = false
    )

    val LavenderPastel = KeyboardThemeColors(
        id = KeyboardThemeId.LAVENDER_PASTEL,
        background = Color(0xFF231C30),
        toolbarBackground = Color(0xFF1A1524),
        keyBackground = Color(0xFF352B47),
        keyTextColor = Color(0xFFF3E8FF),
        keySubTextColor = Color(0xFFA78BFA),
        specialKeyBackground = Color(0xFF2B223B),
        specialKeyTextColor = Color(0xFFD8B4FE),
        actionKeyBackground = Color(0xFF9333EA),
        actionKeyTextColor = Color(0xFFFFFFFF),
        suggestionTextColor = Color(0xFFC084FC),
        popupBackground = Color(0xFF4C3D66),
        isDark = true
    )

    val MintEmerald = KeyboardThemeColors(
        id = KeyboardThemeId.MINT_EMERALD,
        background = Color(0xFF13221E),
        toolbarBackground = Color(0xFF0D1714),
        keyBackground = Color(0xFF1D352F),
        keyTextColor = Color(0xFFECFDF5),
        keySubTextColor = Color(0xFF6EE7B7),
        specialKeyBackground = Color(0xFF162924),
        specialKeyTextColor = Color(0xFFA7F3D0),
        actionKeyBackground = Color(0xFF059669),
        actionKeyTextColor = Color(0xFFFFFFFF),
        suggestionTextColor = Color(0xFF34D399),
        popupBackground = Color(0xFF24443C),
        isDark = true
    )

    val NeonCyber = KeyboardThemeColors(
        id = KeyboardThemeId.NEON_CYBER,
        background = Color(0xFF0D1117),
        toolbarBackground = Color(0xFF070A0E),
        keyBackground = Color(0xFF161B22),
        keyTextColor = Color(0xFF58A6FF),
        keySubTextColor = Color(0xFF8B949E),
        specialKeyBackground = Color(0xFF1F242C),
        specialKeyTextColor = Color(0xFFFF7B72),
        actionKeyBackground = Color(0xFFF778BA),
        actionKeyTextColor = Color(0xFF000000),
        suggestionTextColor = Color(0xFF7EE787),
        popupBackground = Color(0xFF21262D),
        isDark = true
    )

    val SunsetCoral = KeyboardThemeColors(
        id = KeyboardThemeId.SUNSET_CORAL,
        background = Color(0xFF261916),
        toolbarBackground = Color(0xFF1C1210),
        keyBackground = Color(0xFF3D2520),
        keyTextColor = Color(0xFFFFF7ED),
        keySubTextColor = Color(0xFFFDBA74),
        specialKeyBackground = Color(0xFF2E1C18),
        specialKeyTextColor = Color(0xFFFED7AA),
        actionKeyBackground = Color(0xFFEA580C),
        actionKeyTextColor = Color(0xFFFFFFFF),
        suggestionTextColor = Color(0xFFFB923C),
        popupBackground = Color(0xFF52322C),
        isDark = true
    )

    fun getTheme(id: KeyboardThemeId): KeyboardThemeColors {
        return when (id) {
            KeyboardThemeId.CAMEROON -> Cameroon
            KeyboardThemeId.SENEGAL -> Senegal
            KeyboardThemeId.COTE_D_IVOIRE -> CoteDIvoire
            KeyboardThemeId.CONGO_DRC -> CongoDrc
            KeyboardThemeId.NIGERIA -> Nigeria
            KeyboardThemeId.MALI -> Mali
            KeyboardThemeId.SOUTH_AFRICA -> SouthAfrica
            KeyboardThemeId.MOROCCO -> Morocco
            KeyboardThemeId.GBOARD_BLUE -> GBoardBlue
            KeyboardThemeId.DARK_AMOLED -> DarkAmoled
            KeyboardThemeId.CLEAN_LIGHT -> CleanLight
            KeyboardThemeId.LAVENDER_PASTEL -> LavenderPastel
            KeyboardThemeId.MINT_EMERALD -> MintEmerald
            KeyboardThemeId.NEON_CYBER -> NeonCyber
            KeyboardThemeId.SUNSET_CORAL -> SunsetCoral
        }
    }
}
