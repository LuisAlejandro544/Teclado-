package com.example.keyboard

enum class KeyboardMode {
    LETTERS,
    NUMBERS_SYMBOLS,
    MORE_SYMBOLS,
    EMOJIS,
    CLIPBOARD
}

enum class ShiftState {
    OFF,        // Lowercase
    ON,         // Uppercase for next character
    CAPS_LOCK   // Locked uppercase
}

sealed class KeyAction {
    data class Text(val text: String) : KeyAction()
    data class InsertEmoji(val emoji: String) : KeyAction()
    data class CommitSuggestion(val word: String) : KeyAction()
    data class PasteClipboard(val text: String) : KeyAction()
    object Shift : KeyAction()
    object Backspace : KeyAction()
    object SwitchToSymbols : KeyAction()
    object SwitchToMoreSymbols : KeyAction()
    object SwitchToLetters : KeyAction()
    object SwitchToEmojis : KeyAction()
    object SwitchToClipboard : KeyAction()
    object ToggleNumberRow : KeyAction()
    object Space : KeyAction()
    object Enter : KeyAction()
    object HideKeyboard : KeyAction()
    object StartVoiceInput : KeyAction()
    object StopVoiceInput : KeyAction()
    object CancelVoiceInput : KeyAction()
}

/**
 * Mapping of base keys to accented characters and special quick symbols for Long-Press Popups.
 */
object SpecialCharactersMap {
    private val map: Map<String, List<String>> = mapOf(
        "a" to listOf("á", "à", "ä", "â", "ã", "å", "æ", "ª", "1"),
        "e" to listOf("é", "è", "ë", "ê", "ē", "2"),
        "i" to listOf("í", "ì", "ï", "î", "ī", "3"),
        "o" to listOf("ó", "ò", "ö", "ô", "õ", "ø", "º", "4"),
        "u" to listOf("ú", "ù", "ü", "û", "ū", "5"),
        "n" to listOf("ñ", "ń", "6"),
        "c" to listOf("ç", "ć", "č"),
        "d" to listOf("ð", "ď"),
        "s" to listOf("ß", "ś", "š", "$"),
        "z" to listOf("ź", "ż", "ž"),
        "y" to listOf("ý", "ÿ"),
        "q" to listOf("1"),
        "w" to listOf("2"),
        "r" to listOf("4"),
        "t" to listOf("5"),
        "p" to listOf("0"),
        "g" to listOf("ğ"),
        "k" to listOf("ķ"),
        "l" to listOf("ł"),
        "?" to listOf("¿"),
        "!" to listOf("¡"),
        "$" to listOf("€", "£", "¥", "¢", "₹"),
        "%" to listOf("‰"),
        "-" to listOf("—", "–", "_"),
        "." to listOf("...", "…", "·")
    )

    fun getVariants(char: String, isUppercase: Boolean): List<String>? {
        val lower = char.lowercase()
        val list = map[lower] ?: return null
        return if (isUppercase) {
            list.map { if (it.length == 1 && it[0].isLetter()) it.uppercase() else it }
        } else {
            list
        }
    }
}
