/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna).
 *
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010-2020 Martin Berglund
 */

package com.googlecode.lanterna.terminal.swing;

import com.googlecode.lanterna.TextColor;

import java.awt.*;
import java.util.Objects;

/**
 * This class specifies the palette of colors the terminal will use for the normally available 8 + 1 ANSI colors but
 * also their 'bright' versions with are normally enabled through bold mode. There are several palettes available, all
 * based on popular terminal emulators. All colors are defined in the AWT format.
 * @author Martin
 */
@SuppressWarnings("WeakerAccess")
public class TerminalEmulatorPalette {
    /**
     * Values taken from gnome-terminal on Ubuntu
     */
    public static final TerminalEmulatorPalette GNOME_TERMINAL =
            new TerminalEmulatorPalette(
                    new Color(211, 215, 207),
                    new Color(238, 238, 236),
                    new Color(46, 52, 54),
                    new Color(46, 52, 54),
                    new Color(85, 87, 83),
                    new Color(204, 0, 0),
                    new Color(239, 41, 41),
                    new Color(78, 154, 6),
                    new Color(138, 226, 52),
                    new Color(196, 160, 0),
                    new Color(252, 233, 79),
                    new Color(52, 101, 164),
                    new Color(114, 159, 207),
                    new Color(117, 80, 123),
                    new Color(173, 127, 168),
                    new Color(6, 152, 154),
                    new Color(52, 226, 226),
                    new Color(211, 215, 207),
                    new Color(238, 238, 236));

    /**
     * Values taken from <a href="http://en.wikipedia.org/wiki/ANSI_escape_code">
     * wikipedia</a>, these are supposed to be the standard VGA palette.
     */
    public static final TerminalEmulatorPalette STANDARD_VGA =
            new TerminalEmulatorPalette(
                    new Color(170, 170, 170),
                    new Color(255, 255, 255),
                    new Color(0, 0, 0),
                    new Color(0, 0, 0),
                    new Color(85, 85, 85),
                    new Color(170, 0, 0),
                    new Color(255, 85, 85),
                    new Color(0, 170, 0),
                    new Color(85, 255, 85),
                    new Color(170, 85, 0),
                    new Color(255, 255, 85),
                    new Color(0, 0, 170),
                    new Color(85, 85, 255),
                    new Color(170, 0, 170),
                    new Color(255, 85, 255),
                    new Color(0, 170, 170),
                    new Color(85, 255, 255),
                    new Color(170, 170, 170),
                    new Color(255, 255, 255));

    /**
     * Values taken from <a href="http://en.wikipedia.org/wiki/ANSI_escape_code">
     * wikipedia</a>, these are supposed to be what Windows XP cmd is using.
     */
    public static final TerminalEmulatorPalette WINDOWS_XP_COMMAND_PROMPT =
            new TerminalEmulatorPalette(
                    new Color(192, 192, 192),
                    new Color(255, 255, 255),
                    new Color(0, 0, 0),
                    new Color(0, 0, 0),
                    new Color(128, 128, 128),
                    new Color(128, 0, 0),
                    new Color(255, 0, 0),
                    new Color(0, 128, 0),
                    new Color(0, 255, 0),
                    new Color(128, 128, 0),
                    new Color(255, 255, 0),
                    new Color(0, 0, 128),
                    new Color(0, 0, 255),
                    new Color(128, 0, 128),
                    new Color(255, 0, 255),
                    new Color(0, 128, 128),
                    new Color(0, 255, 255),
                    new Color(192, 192, 192),
                    new Color(255, 255, 255));

    /**
     * Values taken from <a href="http://en.wikipedia.org/wiki/ANSI_escape_code">
     * wikipedia</a>, these are supposed to be what terminal.app on MacOSX is using.
     */
    public static final TerminalEmulatorPalette MAC_OS_X_TERMINAL_APP =
            new TerminalEmulatorPalette(
                    new Color(203, 204, 205),
                    new Color(233, 235, 235),
                    new Color(0, 0, 0),
                    new Color(0, 0, 0),
                    new Color(129, 131, 131),
                    new Color(194, 54, 33),
                    new Color(252,57,31),
                    new Color(37, 188, 36),
                    new Color(49, 231, 34),
                    new Color(173, 173, 39),
                    new Color(234, 236, 35),
                    new Color(73, 46, 225),
                    new Color(88, 51, 255),
                    new Color(211, 56, 211),
                    new Color(249, 53, 248),
                    new Color(51, 187, 200),
                    new Color(20, 240, 240),
                    new Color(203, 204, 205),
                    new Color(233, 235, 235));

    /**
     * Values taken from <a href="http://en.wikipedia.org/wiki/ANSI_escape_code">
     * wikipedia</a>, these are supposed to be what putty is using.
     */
    public static final TerminalEmulatorPalette PUTTY =
            new TerminalEmulatorPalette(
                    new Color(187, 187, 187),
                    new Color(255, 255, 255),
                    new Color(0, 0, 0),
                    new Color(0, 0, 0),
                    new Color(85, 85, 85),
                    new Color(187, 0, 0),
                    new Color(255, 85, 85),
                    new Color(0, 187, 0),
                    new Color(85, 255, 85),
                    new Color(187, 187, 0),
                    new Color(255, 255, 85),
                    new Color(0, 0, 187),
                    new Color(85, 85, 255),
                    new Color(187, 0, 187),
                    new Color(255, 85, 255),
                    new Color(0, 187, 187),
                    new Color(85, 255, 255),
                    new Color(187, 187, 187),
                    new Color(255, 255, 255));

    /**
     * Values taken from <a href="http://en.wikipedia.org/wiki/ANSI_escape_code">
     * wikipedia</a>, these are supposed to be what xterm is using.
     */
    public static final TerminalEmulatorPalette XTERM =
            new TerminalEmulatorPalette(
                    new Color(229, 229, 229),
                    new Color(255, 255, 255),
                    new Color(0, 0, 0),
                    new Color(0, 0, 0),
                    new Color(127, 127, 127),
                    new Color(205, 0, 0),
                    new Color(255, 0, 0),
                    new Color(0, 205, 0),
                    new Color(0, 255, 0),
                    new Color(205, 205, 0),
                    new Color(255, 255, 0),
                    new Color(0, 0, 238),
                    new Color(92, 92, 255),
                    new Color(205, 0, 205),
                    new Color(255, 0, 255),
                    new Color(0, 205, 205),
                    new Color(0, 255, 255),
                    new Color(229, 229, 229),
                    new Color(255, 255, 255));

    /**
     * Default colors the SwingTerminal is using if you don't specify anything
     */
    public static final TerminalEmulatorPalette DEFAULT = GNOME_TERMINAL;

    private final Color defaultColor;
    private final Color defaultBrightColor;
    private final Color defaultBackgroundColor;
    private final Color normalBlack;
    private final Color brightBlack;
    private final Color normalRed;
    private final Color brightRed;
    private final Color normalGreen;
    private final Color brightGreen;
    private final Color normalYellow;
    private final Color brightYellow;
    private final Color normalBlue;
    private final Color brightBlue;
    private final Color normalMagenta;
    private final Color brightMagenta;
    private final Color normalCyan;
    private final Color brightCyan;
    private final Color normalWhite;
    private final Color brightWhite;

    /**
     * Creates a new palette with all colors specified up-front
     * @param defaultColor Default color which no specific color has been selected
     * @param defaultBrightColor Default color which no specific color has been selected but bold is enabled
     * @param defaultBackgroundColor Default color to use for the background when no specific color has been selected
     * @param normalBlack Color for normal black
     * @param brightBlack Color for bright black
     * @param normalRed Color for normal red
     * @param brightRed Color for bright red
     * @param normalGreen Color for normal green
     * @param brightGreen Color for bright green
     * @param normalYellow Color for normal yellow
     * @param brightYellow Color for bright yellow
     * @param normalBlue Color for normal blue
     * @param brightBlue Color for bright blue
     * @param normalMagenta Color for normal magenta
     * @param brightMagenta Color for bright magenta
     * @param normalCyan Color for normal cyan
     * @param brightCyan Color for bright cyan
     * @param normalWhite Color for normal white
     * @param brightWhite Color for bright white
     */
    public TerminalEmulatorPalette(
            Color defaultColor,
            Color defaultBrightColor,
            Color defaultBackgroundColor,
            Color normalBlack,
            Color brightBlack,
            Color normalRed,
            Color brightRed,
            Color normalGreen,
            Color brightGreen,
            Color normalYellow,
            Color brightYellow,
            Color normalBlue,
            Color brightBlue,
            Color normalMagenta,
            Color brightMagenta,
            Color normalCyan,
            Color brightCyan,
            Color normalWhite,
            Color brightWhite) {
        this.defaultColor = defaultColor;
        this.defaultBrightColor = defaultBrightColor;
        this.defaultBackgroundColor = defaultBackgroundColor;
        this.normalBlack = normalBlack;
        this.brightBlack = brightBlack;
        this.normalRed = normalRed;
        this.brightRed = brightRed;
        this.normalGreen = normalGreen;
        this.brightGreen = brightGreen;
        this.normalYellow = normalYellow;
        this.brightYellow = brightYellow;
        this.normalBlue = normalBlue;
        this.brightBlue = brightBlue;
        this.normalMagenta = normalMagenta;
        this.brightMagenta = brightMagenta;
        this.normalCyan = normalCyan;
        this.brightCyan = brightCyan;
        this.normalWhite = normalWhite;
        this.brightWhite = brightWhite;
    }

    /**
     * Returns the AWT color from this palette given an ANSI color and two hints for if we are looking for a background
     * color and if we want to use the bright version.
     * @param color Which ANSI color we want to extract
     * @param isForeground Is this color we extract going to be used as a background color?
     * @param useBrightTones If true, we should return the bright version of the color
     * @return AWT color extracted from this palette for the input parameters
     */
    public Color get(TextColor.ANSI color, boolean isForeground, boolean useBrightTones) {
        if(useBrightTones) {
            switch(color) {
                case BLACK:
                case BLACK_BRIGHT:
                    return brightBlack;
                case BLUE:
                case BLUE_BRIGHT:
                    return brightBlue;
                case CYAN:
                case CYAN_BRIGHT:
                    return brightCyan;
                case DEFAULT:
                    return isForeground ? defaultBrightColor : defaultBackgroundColor;
                case GREEN:
                case GREEN_BRIGHT:
                    return brightGreen;
                case MAGENTA:
                case MAGENTA_BRIGHT:
                    return brightMagenta;
                case RED:
                case RED_BRIGHT:
                    return brightRed;
                case WHITE:
                case WHITE_BRIGHT:
                    return brightWhite;
                case YELLOW:
                case YELLOW_BRIGHT:
                    return brightYellow;
            }
        }
        else {
            switch(color) {
                case BLACK:
                    return normalBlack;
                case BLACK_BRIGHT:
                    return brightBlack;
                case BLUE:
                    return normalBlue;
                case BLUE_BRIGHT:
                    return brightBlue;
                case CYAN:
                    return normalCyan;
                case CYAN_BRIGHT:
                    return brightCyan;
                case DEFAULT:
                    return isForeground ? defaultColor : defaultBackgroundColor;
                case GREEN:
                    return normalGreen;
                case GREEN_BRIGHT:
                    return brightGreen;
                case MAGENTA:
                    return normalMagenta;
                case MAGENTA_BRIGHT:
                    return brightMagenta;
                case RED:
                    return normalRed;
                case RED_BRIGHT:
                    return brightRed;
                case WHITE:
                    return normalWhite;
                case WHITE_BRIGHT:
                    return brightWhite;
                case YELLOW:
                    return normalYellow;
                case YELLOW_BRIGHT:
                    return brightYellow;
            }
        }
        throw new IllegalArgumentException("Unknown text color " + color);
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final TerminalEmulatorPalette other = (TerminalEmulatorPalette) obj;
        if(!Objects.equals(this.defaultColor, other.defaultColor)) {
            return false;
        }
        if(!Objects.equals(this.defaultBrightColor, other.defaultBrightColor)) {
            return false;
        }
        if(!Objects.equals(this.defaultBackgroundColor, other.defaultBackgroundColor)) {
            return false;
        }
        if(!Objects.equals(this.normalBlack, other.normalBlack)) {
            return false;
        }
        if(!Objects.equals(this.brightBlack, other.brightBlack)) {
            return false;
        }
        if(!Objects.equals(this.normalRed, other.normalRed)) {
            return false;
        }
        if(!Objects.equals(this.brightRed, other.brightRed)) {
            return false;
        }
        if(!Objects.equals(this.normalGreen, other.normalGreen)) {
            return false;
        }
        if(!Objects.equals(this.brightGreen, other.brightGreen)) {
            return false;
        }
        if(!Objects.equals(this.normalYellow, other.normalYellow)) {
            return false;
        }
        if(!Objects.equals(this.brightYellow, other.brightYellow)) {
            return false;
        }
        if(!Objects.equals(this.normalBlue, other.normalBlue)) {
            return false;
        }
        if(!Objects.equals(this.brightBlue, other.brightBlue)) {
            return false;
        }
        if(!Objects.equals(this.normalMagenta, other.normalMagenta)) {
            return false;
        }
        if(!Objects.equals(this.brightMagenta, other.brightMagenta)) {
            return false;
        }
        if(!Objects.equals(this.normalCyan, other.normalCyan)) {
            return false;
        }
        if(!Objects.equals(this.brightCyan, other.brightCyan)) {
            return false;
        }
        if(!Objects.equals(this.normalWhite, other.normalWhite)) {
            return false;
        }
        return Objects.equals(this.brightWhite, other.brightWhite);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.defaultColor != null ? this.defaultColor.hashCode() : 0);
        hash = 47 * hash + (this.defaultBrightColor != null ? this.defaultBrightColor.hashCode() : 0);
        hash = 47 * hash + (this.defaultBackgroundColor != null ? this.defaultBackgroundColor.hashCode() : 0);
        hash = 47 * hash + (this.normalBlack != null ? this.normalBlack.hashCode() : 0);
        hash = 47 * hash + (this.brightBlack != null ? this.brightBlack.hashCode() : 0);
        hash = 47 * hash + (this.normalRed != null ? this.normalRed.hashCode() : 0);
        hash = 47 * hash + (this.brightRed != null ? this.brightRed.hashCode() : 0);
        hash = 47 * hash + (this.normalGreen != null ? this.normalGreen.hashCode() : 0);
        hash = 47 * hash + (this.brightGreen != null ? this.brightGreen.hashCode() : 0);
        hash = 47 * hash + (this.normalYellow != null ? this.normalYellow.hashCode() : 0);
        hash = 47 * hash + (this.brightYellow != null ? this.brightYellow.hashCode() : 0);
        hash = 47 * hash + (this.normalBlue != null ? this.normalBlue.hashCode() : 0);
        hash = 47 * hash + (this.brightBlue != null ? this.brightBlue.hashCode() : 0);
        hash = 47 * hash + (this.normalMagenta != null ? this.normalMagenta.hashCode() : 0);
        hash = 47 * hash + (this.brightMagenta != null ? this.brightMagenta.hashCode() : 0);
        hash = 47 * hash + (this.normalCyan != null ? this.normalCyan.hashCode() : 0);
        hash = 47 * hash + (this.brightCyan != null ? this.brightCyan.hashCode() : 0);
        hash = 47 * hash + (this.normalWhite != null ? this.normalWhite.hashCode() : 0);
        hash = 47 * hash + (this.brightWhite != null ? this.brightWhite.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "SwingTerminalPalette{" +
                "defaultColor=" + defaultColor +
                ", defaultBrightColor=" + defaultBrightColor +
                ", defaultBackgroundColor=" + defaultBackgroundColor +
                ", normalBlack=" + normalBlack +
                ", brightBlack=" + brightBlack +
                ", normalRed=" + normalRed +
                ", brightRed=" + brightRed +
                ", normalGreen=" + normalGreen +
                ", brightGreen=" + brightGreen +
                ", normalYellow=" + normalYellow +
                ", brightYellow=" + brightYellow +
                ", normalBlue=" + normalBlue +
                ", brightBlue=" + brightBlue +
                ", normalMagenta=" + normalMagenta +
                ", brightMagenta=" + brightMagenta +
                ", normalCyan=" + normalCyan +
                ", brightCyan=" + brightCyan +
                ", normalWhite=" + normalWhite +
                ", brightWhite=" + brightWhite + '}';
    }
}
