package com.szadowsz.gui.component.bined.utils;


import com.szadowsz.gui.component.bined.settings.CodeCharactersCase;
import com.szadowsz.gui.component.bined.settings.CodeType;

import java.awt.*;
import java.util.Objects;

public class RBinUtils {

    public static final String NULL_FIELD_ERROR = "Field cannot be null";

    private RBinUtils() {
        // NOOP
    }

    public static <T> T requireNonNull(T object) {
        return Objects.requireNonNull(object, NULL_FIELD_ERROR);
    }

    public static <T> T requireNonNull(T object, String message) {
        return Objects.requireNonNull(object, message);
    }

    public static void requireNonNull(Object... objects) {
        for (Object object : objects) {
            Objects.requireNonNull(object, NULL_FIELD_ERROR);
        }
    }

    public static IllegalStateException getInvalidTypeException(Enum<?> enumObject) {
        return new IllegalStateException("Unexpected " + enumObject.getDeclaringClass().getName() + " value " + enumObject.name());
    }

    public static boolean areSameColors(Color color, Color renderColor) {
    }

    public static void longToBaseCode(char[] headerChars, int codePos, long index, int base, int i, boolean b, CodeCharactersCase codeCharactersCase) {
    }

    public static void byteToCharsCode(byte dataByte, CodeType codeType, char[] rowCharacters, int byteRowPos, CodeCharactersCase codeCharactersCase) {
    }
}
