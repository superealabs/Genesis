package utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.labs.utils.StringUtils.*;

public class StringUtilsTest {

    @Test
    public void testBaseFormat_SimpleString() {
        // Test avec une chaîne simple sans caractères spéciaux
        assertEquals("test string", baseFormat("testString"));
    }

    @Test
    public void testBaseFormat_WithUnderscores() {
        // Test avec des underscores qui doivent être remplacés par des espaces
        assertEquals("test string example", baseFormat("test_string_example"));
    }

    @Test
    public void testBaseFormat_WithMixedCase() {
        // Test avec des majuscules au milieu des mots
        assertEquals("test string with mixed case", baseFormat("testStringWithMixedCase"));
    }

    @Test
    public void testBaseFormat_ComplexMix() {
        // Test avec une combinaison d'underscores et de majuscules
        assertEquals("user first name with id", baseFormat("user_firstName_WithId"));
    }

    @Test
    public void testBaseFormat_EmptyString() {
        // Test avec une chaîne vide
        assertEquals("", baseFormat(""));
    }

    @Test
    public void testFormatReadable_SimpleString() {
        // Test de formatReadable avec une chaîne simple
        assertEquals("Test string", formatReadable("testString"));
    }

    @Test
    public void testFormatReadable_WithUnderscores() {
        // Test de formatReadable avec des underscores
        assertEquals("Test string example", formatReadable("test_string_example"));
    }

    @Test
    public void testFormatReadable_ComplexMix() {
        // Test de formatReadable avec un mélange complexe
        assertEquals("User first name with id", formatReadable("user_firstName_WithId"));
    }

    @Test
    public void testFormatReadableLowerCase_SimpleString() {
        // Test de formatReadableLowerCase avec une chaîne simple
        assertEquals("test string", formatReadableLowerCase("testString"));
    }

    @Test
    public void testFormatReadableLowerCase_WithUnderscores() {
        // Test de formatReadableLowerCase avec des underscores
        assertEquals("test string example", formatReadableLowerCase("test_string_example"));
    }

    @Test
    public void testFormatReadableLowerCase_ComplexMix() {
        // Test de formatReadableLowerCase avec un mélange complexe
        assertEquals("user first name with id", formatReadableLowerCase("user_firstName_WithId"));
    }

    @Test
    public void testFormatReadableLowerCase_AlreadyLowerCase() {
        // Test avec une chaîne déjà en minuscules
        assertEquals("test string", formatReadableLowerCase("test_string"));
    }
}