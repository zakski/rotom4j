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

    public static boolean areSameColors(Color color, Color comparedColor) {
        return (color == null && comparedColor == null) || (color != null && color.equals(comparedColor));
    }

    /**
     * Returns true if provided character is valid for given code type and
     * position.
     *
     * @param keyValue keyboard key value
     * @param codeOffset current code offset
     * @param codeType current code type
     * @return true if key value value is valid
     */
    public static boolean isValidCodeKeyValue(char keyValue, int codeOffset, CodeType codeType) {
        boolean validKey = false;
        switch (codeType) {
            case BINARY: {
                validKey = keyValue >= '0' && keyValue <= '1';
                break;
            }
            case DECIMAL: {
                validKey = codeOffset == 0
                        ? keyValue >= '0' && keyValue <= '2'
                        : keyValue >= '0' && keyValue <= '9';
                break;
            }
            case OCTAL: {
                validKey = codeOffset == 0
                        ? keyValue >= '0' && keyValue <= '3'
                        : keyValue >= '0' && keyValue <= '7';
                break;
            }
            case HEXADECIMAL: {
                validKey = (keyValue >= '0' && keyValue <= '9')
                        || (keyValue >= 'a' && keyValue <= 'f') || (keyValue >= 'A' && keyValue <= 'F');
                break;
            }
            default:
                throw getInvalidTypeException(codeType);
        }
        return validKey;
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

    /**
     * Returns modified byte value after single code value is applied.
     *
     * @param byteValue original byte value
     * @param value code value
     * @param codeOffset code offset
     * @param codeType code type
     * @return modified byte value
     */
    public static byte setCodeValue(byte byteValue, int value, int codeOffset, CodeType codeType) {
        switch (codeType) {
            case BINARY: {
                int bitMask = 0x80 >> codeOffset;
                byteValue = (byte) (byteValue & (0xff - bitMask) | (value << (7 - codeOffset)));
                break;
            }
            case DECIMAL: {
                int newValue = byteValue & 0xff;
                switch (codeOffset) {
                    case 0: {
                        newValue = (newValue % 100) + value * 100;
                        if (newValue > 255) {
                            newValue = 200;
                        }
                        break;
                    }
                    case 1: {
                        newValue = (newValue / 100) * 100 + value * 10 + (newValue % 10);
                        if (newValue > 255) {
                            newValue -= 200;
                        }
                        break;
                    }
                    case 2: {
                        newValue = (newValue / 10) * 10 + value;
                        if (newValue > 255) {
                            newValue -= 200;
                        }
                        break;
                    }
                }

                byteValue = (byte) newValue;
                break;
            }
            case OCTAL: {
                int newValue = byteValue & 0xff;
                switch (codeOffset) {
                    case 0: {
                        newValue = (newValue % 64) + value * 64;
                        break;
                    }
                    case 1: {
                        newValue = (newValue / 64) * 64 + value * 8 + (newValue % 8);
                        break;
                    }
                    case 2: {
                        newValue = (newValue / 8) * 8 + value;
                        break;
                    }
                }

                byteValue = (byte) newValue;
                break;
            }
            case HEXADECIMAL: {
                if (codeOffset == 1) {
                    byteValue = (byte) ((byteValue & 0xf0) | value);
                } else {
                    byteValue = (byte) ((byteValue & 0xf) | (value << 4));
                }
                break;
            }
            default:
                throw getInvalidTypeException(codeType);
        }

        return byteValue;
    }

}
