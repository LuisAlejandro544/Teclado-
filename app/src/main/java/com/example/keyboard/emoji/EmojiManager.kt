package com.example.keyboard.emoji

import android.graphics.Paint
import androidx.core.graphics.PaintCompat

enum class EmojiCategory(val label: String, val icon: String) {
    SMILEYS("Caras", "😀"),
    ANIMALS("Animales", "🐶"),
    FOOD("Comida", "🍔"),
    ACTIVITIES("Deportes", "⚽"),
    TRAVEL("Lugares", "🚗"),
    OBJECTS_SYMBOLS("Objetos", "💡")
}

object EmojiManager {

    private val paint = Paint()
    private var supportedEmojiCache: Map<EmojiCategory, List<String>>? = null
    private val recentEmojis = mutableListOf("😀", "😂", "❤️", "👍", "🔥", "🎉", "✨", "🙌")

    private val allEmojisByCategory: Map<EmojiCategory, List<String>> = mapOf(
        EmojiCategory.SMILEYS to listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
            "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
            "😋", "😛", "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🤫", "🤔",
            "🤐", "🤨", "😐", "😑", "😶", "😏", "😒", "🙄", "😬", "🤥",
            "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢", "🤮", "🤧",
            "🥵", "🥶", "🥴", "😵", "🤯", "🤠", "🥳", "😎", "🤓", "🧐",
            "😕", "😟", "🙁", "😮", "😯", "😲", "😳", "🥺", "😦", "😧",
            "😨", "😰", "😥", "😢", "😭", "😱", "😖", "😣", "😞", "😓",
            "😩", "😫", "🥱", "😤", "😡", "😠", "🤬", "😈", "👿", "💀",
            "💩", "🤡", "👻", "👽", "🤖", "🎃", "😺", "😸", "😹", "😻",
            "👋", "🤚", "✋", "🖖", "👌", "🤌", "🤏", "✌️", "🤞", "🤟",
            "🤘", "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️", "👍", "👎",
            "✊", "👊", "🤛", "🤜", "👏", "🙌", "👐", "🤲", "🤝", "🙏",
            "💪", "🦾", "👂", "👃", "🧠", "👀", "👁️", "👅", "👄", "💋"
        ),
        EmojiCategory.ANIMALS to listOf(
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯",
            "🦁", "🐮", "🐷", "🐽", "🐸", "🐵", "🙈", "🙉", "🙊", "🐒",
            "🐔", "🐧", "🐦", "🐤", "🐣", "🐥", "🦆", "🦅", "🦉", "🦇",
            "🐺", "🐗", "🐴", "🦄", "🐝", "🐛", "🦋", "🐌", "🐞", "🐜",
            "🦟", "🦗", "🕷️", "🦂", "🐢", "🐍", "🦎", "🦖", "🦕", "🐙",
            "🦑", "🦐", "🦞", "🦀", "🐡", "🐠", "🐟", "🐬", "🐳", "🐋",
            "🦈", "🐊", "🐅", "🐆", "🦓", "🦍", "🦧", "🐘", "🦛", "🦏",
            "🐪", "🐫", "🦒", "🦘", "🐂", "🐄", "🐎", "🐖", "🐏", "🐑",
            "🦙", "🐐", "🦌", "🐕", "🐩", "🐈", "🐓", "🦃", "🦚", "🦜",
            "🦢", "🦩", "🕊️", "🐇", "🦝", "🦨", "🦡", "🦦", "🦥", "🐁",
            "🌲", "🌳", "🌴", "🌱", "🌿", "☘️", "🍀", "🎍", "🪴", "🍃",
            "🍂", "🍁", "🍄", "🐚", "💐", "🌷", "🌹", "🥀", "🌺", "🌸",
            "🌼", "🌻", "🌞", "🌝", "🌛", "🌚", "🌕", "🌙", "⭐", "🌟",
            "✨", "⚡", "☄️", "💥", "🔥", "🌪️", "🌈", "☀️", "🌤️", "⛅",
            "☁️", "🌧️", "🌩️", "❄️", "☃️", "💨", "💧", "💦", "🌊"
        ),
        EmojiCategory.FOOD to listOf(
            "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐",
            "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🍆", "🥑",
            "🥦", "🥬", "🥒", "🌶️", "🌽", "🥕", "🫒", "🧄", "🧅", "🥔",
            "🍠", "🥐", "🥯", "🍞", "🥖", "🥨", "🧀", "🥚", "🍳", "🧈",
            "🥞", "🧇", "🥓", "🥩", "🍗", "🍖", "🌭", "🍔", "🍟", "🍕",
            "🥪", "🥙", "🧆", "🌮", "🌯", "🥗", "🥘", "🥫", "🍝", "🍜",
            "🍲", "🍛", "🍣", "🍱", "🥟", "🍤", "🍙", "🍚", "🍘", "🍡",
            "🍧", "🍨", "🍦", "🥧", "🧁", "🍰", "🎂", "🍮", "🍭", "🍬",
            "🍫", "🍿", "🍩", "🍪", "🌰", "🥜", "🍯", "🥛", "🍼", "☕",
            "🍵", "🧃", "🥤", "🧋", "🍺", "🍻", "🥂", "🍷", "🥃", "🍸",
            "🍹", "🧉", "🍾", "🧊", "🥄", "🍴", "🍽️", "🥣", "🥢", "🧂"
        ),
        EmojiCategory.ACTIVITIES to listOf(
            "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱",
            "🏓", "🏸", "🏒", "🏑", "🏏", "⛳", "🏹", "🎣", "🤿", "🥊",
            "🥋", "🛹", "🛼", "⛸️", "🎿", "🏂", "🏋️", "🤼", "🤸", "⛹️",
            "🤺", "🏌️", "🏇", "🧘", "🏄", "🏊", "🤽", "🚣", "🧗", "🚵",
            "🚴", "🏆", "🥇", "🥈", "🥉", "🏅", "🎖️", "🎫", "🎟️", "🎪",
            "🎭", "🎨", "🎬", "🎤", "🎧", "🎼", "🎹", "🥁", "🎷", "🎺",
            "🎸", "🎻", "🎲", "♟️", "🎯", "🎳", "🎮", "🎰", "🧩"
        ),
        EmojiCategory.TRAVEL to listOf(
            "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "🚐",
            "🛻", "🚚", "🚛", "🚜", "🛴", "🚲", "🛵", "🏍️", "🚨", "🚔",
            "🚍", "🚘", "🚖", "🚡", "🚠", "🚃", "🚋", "🚞", "🚅", "🚆",
            "🚇", "🚊", "🚉", "🚁", "🛩️", "✈️", "🛫", "🛬", "💺", "🛰️",
            "🚀", "🛸", "⛵", "🚤", "🚢", "⚓", "⛽", "🚧", "🚦", "🚥",
            "🗺️", "🗿", "🗽", "🗼", "🏰", "🏟️", "🎡", "🎢", "🏖️", "🏝️",
            "Desert", "🏜️", "🌋", "⛰️", "🏔️", "🏕️", "⛺", "🏠", "🏡", "🏢",
            "🏣", "🏤", "🏥", "🏦", "🏨", "🏪", "🏫", "🏬", "🏭", "⛪"
        ),
        EmojiCategory.OBJECTS_SYMBOLS to listOf(
            "💡", "🔦", "🕯️", "📱", "📲", "💻", "⌨️", "🖥️", "🖨️", "🖱️",
            "📷", "📸", "📹", "🎥", "📞", "☎️", "📟", "📺", "📻", "🎙️",
            "⏱️", "⏰", "🕰️", "⏳", "📡", "🔋", "🔌", "💵", "💶", "🪙",
            "💰", "💳", "💎", "⚖️", "🔧", "🔨", "🛠️", "⚙️", "🔒", "🔓",
            "🔑", "🗝️", "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍",
            "🤎", "💔", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝",
            "☮️", "✝️", "☪️", "🕉️", "☸️", "✡️", "☯️", "♈", "♉", "♊",
            "♋", "♌", "♍", "♎", "♏", "♐", "♑", "♒", "♓", "🆔",
            "☢️", "☣️", "📴", "📳", "❌", "⭕", "🛑", "⛔", "📛", "🚫",
            "💯", "💢", "♨️", "❗", "❕", "❓", "❔", "‼️", "⁉️", "⚠️",
            "♻️", "✅", "❇️", "✳️", "❎", "🌐", "💠", "💤", "🏧", "♿",
            "🅿️", "🚺", "🚹", "🚼", "🚻", "🚾", "📶", "🔤", "🔢", "#️⃣",
            "*️⃣", "0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣",
            "8️⃣", "9️⃣", "🔟", "▶️", "⏩", "⏭️", "⏯️", "◀️", "⏪", "⏮️",
            "🔼", "🔽", "⏸️", "⏹️", "⏺️", "✖️", "➕", "➖", "➗", "♾️",
            "✔️", "©", "®", "™"
        )
    )

    /**
     * Verifies whether the current device font can render the given emoji character,
     * preventing empty rectangles or tofu characters.
     */
    fun isEmojiSupported(emoji: String): Boolean {
        if (emoji.isBlank()) return false
        return try {
            PaintCompat.hasGlyph(paint, emoji)
        } catch (_: Throwable) {
            true
        }
    }

    /**
     * Returns the list of emojis for a given category that are guaranteed to be supported
     * and renderable by the current device.
     */
    fun getSupportedEmojis(category: EmojiCategory): List<String> {
        val cache = supportedEmojiCache ?: computeSupportedEmojis().also { supportedEmojiCache = it }
        return cache[category] ?: emptyList()
    }

    fun getTotalSupportedCount(): Int {
        val cache = supportedEmojiCache ?: computeSupportedEmojis().also { supportedEmojiCache = it }
        return cache.values.sumOf { it.size }
    }

    fun getRecentEmojis(): List<String> {
        return recentEmojis.filter { isEmojiSupported(it) }
    }

    fun addRecentEmoji(emoji: String) {
        if (emoji.isBlank()) return
        recentEmojis.remove(emoji)
        recentEmojis.add(0, emoji)
        if (recentEmojis.size > 24) {
            recentEmojis.removeAt(recentEmojis.lastIndex)
        }
    }

    private fun computeSupportedEmojis(): Map<EmojiCategory, List<String>> {
        val result = mutableMapOf<EmojiCategory, List<String>>()
        for ((cat, list) in allEmojisByCategory) {
            val supported = list.filter { isEmojiSupported(it) }
            result[cat] = if (supported.isNotEmpty()) supported else list.take(10) // fallback
        }
        return result
    }
}
