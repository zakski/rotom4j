package com.szadowsz.gui.config.theme;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Storage Class For Current and Preset GUI Themes
 */
public class RThemeStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(RThemeStore.class);

    private RThemeStore() {
        // NOOP
    }

    public static void init() {
    }

    // TODO LazyGui
    public static int getColor(RThemeColorType type) {
        // TODO consider Theme/Internal Window/ GUI or External Window / Global granularity - Do we need one per Gui?
        return switch (type) {
            case WINDOW_BORDER -> paletteMap.get(RThemeType.CURRENT).windowBorder;
            case NORMAL_BACKGROUND -> paletteMap.get(RThemeType.CURRENT).normalBackground;
            case FOCUS_BACKGROUND -> paletteMap.get(RThemeType.CURRENT).focusBackground;
            case NORMAL_FOREGROUND -> paletteMap.get(RThemeType.CURRENT).normalForeground;
            case FOCUS_FOREGROUND -> paletteMap.get(RThemeType.CURRENT).focusForeground;
            default -> 0xFFFF0000; // TODO remove if uneeded
        };
    }
}
