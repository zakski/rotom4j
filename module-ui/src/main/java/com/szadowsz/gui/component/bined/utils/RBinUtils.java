package com.szadowsz.gui.component.bined.utils;


import com.szadowsz.gui.component.bined.settings.CodeCharactersCase;
import com.szadowsz.gui.component.bined.settings.CodeType;

import java.awt.*;
import java.util.Objects;

public class RBinUtils {

    public static final char[] UPPER_HEX_CODES = "0123456789ABCDEF".toCharArray();
    public static final char[] LOWER_HEX_CODES = "0123456789abcdef".toCharArray();

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
        return false;
    }

    /**
     * Converts long value to code of given base and length limit.
     * <p>
     * Optionally fills rest of the value with zeros.
     *
     * @param target        target characters array (output parameter)
     * @param targetOffset  offset position in target array
     * @param value         value value
     * @param base          target numerical base, supported values are 1 to 16
     * @param lengthLimit   length limit
     * @param fillZeros     flag if rest of the value should be filled with zeros
     * @param characterCase upper case for values greater than 9
     * @return offset of characters position
     */
    public static int longToBaseCode(char[] target, int targetOffset, long value, int base, int lengthLimit, boolean fillZeros, CodeCharactersCase characterCase) {
        char[] codes = characterCase == CodeCharactersCase.UPPER ? UPPER_HEX_CODES : LOWER_HEX_CODES;
        for (int i = lengthLimit - 1; i >= 0; i--) {
            target[targetOffset + i] = codes[(int) (value % base)];
            value = value / base;
            if (!fillZeros && value == 0) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Converts byte value to sequence of characters of given code type.
     *
     * @param dataByte byte value
     * @param codeType code type
     * @param targetData target array of characters (output parameter)
     * @param targetPosition target position in array of characters
     * @param charCase case type for alphabetical characters
     */
    public static void byteToCharsCode(byte dataByte, CodeType codeType, char[] targetData, int targetPosition, CodeCharactersCase charCase) {
        char[] hexCharacters = charCase == CodeCharactersCase.UPPER ? UPPER_HEX_CODES : LOWER_HEX_CODES;
        switch (codeType) {
            case BINARY: {
                int bitMask = 0x80;
                for (int i = 0; i < 8; i++) {
                    int codeValue = (dataByte & bitMask) > 0 ? 1 : 0;
                    targetData[targetPosition + i] = hexCharacters[codeValue];
                    bitMask = bitMask >> 1;
                }
                break;
            }
            case DECIMAL: {
                int value = dataByte & 0xff;
                int codeValue0 = value / 100;
                targetData[targetPosition] = hexCharacters[codeValue0];
                int codeValue1 = (value / 10) % 10;
                targetData[targetPosition + 1] = hexCharacters[codeValue1];
                int codeValue2 = value % 10;
                targetData[targetPosition + 2] = hexCharacters[codeValue2];
                break;
            }
            case OCTAL: {
                int value = dataByte & 0xff;
                int codeValue0 = value / 64;
                targetData[targetPosition] = hexCharacters[codeValue0];
                int codeValue1 = (value / 8) & 7;
                targetData[targetPosition + 1] = hexCharacters[codeValue1];
                int codeValue2 = value % 8;
                targetData[targetPosition + 2] = hexCharacters[codeValue2];
                break;
            }
            case HEXADECIMAL: {
                int codeValue0 = (dataByte >> 4) & 0xf;
                targetData[targetPosition] = hexCharacters[codeValue0];
                int codeValue1 = dataByte & 0xf;
                targetData[targetPosition + 1] = hexCharacters[codeValue1];
                break;
            }
            default:
                throw getInvalidTypeException(codeType);
        }
    }

}
