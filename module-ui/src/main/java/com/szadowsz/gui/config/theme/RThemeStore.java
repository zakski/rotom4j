package com.szadowsz.gui.config.theme;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage Class For Current and Preset GUI Themes
 */
public class RThemeStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(RThemeStore.class);

    private static final Map<RThemeType, RTheme> paletteMap = new HashMap<>();

    private RThemeStore() {
        // NOOP
    }

    public static void init() {
    }

    // TODO LazyGui
    public static Color getColor(RThemeColorType type) {
        // TODO consider Theme/Internal Window/ GUI or External Window / Global granularity - Do we need one per Gui?
        return switch (type) {
            case WINDOW_BORDER -> paletteMap.get(RThemeType.CURRENT).windowBorder;
            case NORMAL_BACKGROUND -> paletteMap.get(RThemeType.CURRENT).normalBackground;
            case FOCUS_BACKGROUND -> paletteMap.get(RThemeType.CURRENT).focusBackground;
            case NORMAL_FOREGROUND -> paletteMap.get(RThemeType.CURRENT).normalForeground;
            case FOCUS_FOREGROUND -> paletteMap.get(RThemeType.CURRENT).focusForeground;
         };
    }

    // TODO LazyGui
    public static int getRGBA(RThemeColorType type) {
        return getColor(type).getRGB();
    }

        public static int getGlobalSchemeNum() { // TODO G4P
        return 0; // TODO STUB
    }

    public static RTheme getTheme(int localTheme) {
        return null;  // TODO STUB
    }
}
