package com.example

import com.example.keyboard.SpecialCharactersMap
import com.example.keyboard.nativebridge.NativeKeyboardBridge
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testSpecialCharactersMap() {
        val aVariantsLower = SpecialCharactersMap.getVariants("a", isUppercase = false)
        assertNotNull(aVariantsLower)
        assertTrue(aVariantsLower!!.contains("á"))
        assertTrue(aVariantsLower.contains("à"))

        val aVariantsUpper = SpecialCharactersMap.getVariants("a", isUppercase = true)
        assertNotNull(aVariantsUpper)
        assertTrue(aVariantsUpper!!.contains("Á"))

        val nVariants = SpecialCharactersMap.getVariants("n", isUppercase = false)
        assertNotNull(nVariants)
        assertTrue(nVariants!!.contains("ñ"))
    }

    @Test
    fun testSuggestionsEngine() {
        val suggestions = NativeKeyboardBridge.getSuggestions("hol", 3)
        assertTrue(suggestions.isNotEmpty())
        assertTrue(suggestions.any { it.startsWith("hol", ignoreCase = true) })
    }

    @Test
    fun testAutocorrectAccentResolution() {
        val correction = NativeKeyboardBridge.getAutocorrect("tambien")
        assertEquals("también", correction)

        val arbolCorrection = NativeKeyboardBridge.getAutocorrect("arbol")
        assertEquals("árbol", arbolCorrection)
    }
}
