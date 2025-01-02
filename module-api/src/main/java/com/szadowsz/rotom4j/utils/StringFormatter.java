/*
 * Copyright (c) 2023 Turtleisaac.
 *
 * This file is part of Nds4j.
 *
 * Nds4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nds4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nds4j. If not, see <https://www.gnu.org/licenses/>.
 */

package com.szadowsz.rotom4j.utils;

/**
 * Utility Class to make sure Strings are Formatted Correctly
 */
public class StringFormatter {

    /**
     * Method to return the index as a formatted string with the appropriate amount of 0 padding
     *
     * @param i index
     * @param cnt total count
     * @param prefix desired prefix
     * @param suffix desired suffix
     * @return formatted string
     */
    public static String formatOutputString(int i, int cnt, String prefix, String suffix) {
        StringBuilder sb = new StringBuilder("" + i);
        if (cnt < 10) {
            while (sb.length() < 2)
                sb.insert(0, "0");
        } else if (cnt < 100) {
            while (sb.length() < 3)
                sb.insert(0, "0");
        } else if (cnt < 1000) {
            while (sb.length() < 4)
                sb.insert(0, "0");
        } else if (cnt < 10000) {
            while (sb.length() < 5)
                sb.insert(0, "0");
        }
        sb.insert(0, prefix).append(suffix);
        return sb.toString();
    }
}
