package com.old.gui.config.theme;

/**
 * Pre-defined themes
 */
public enum RThemeType { // TODO LazyGui
    CURRENT; // the default theme used
    // TODO Expand list or change from Enum

    static RTheme getPalette(RThemeType query) {
        switch (query) {
            case CURRENT: {
                return new RTheme(0xFF787878,
                        0xFF0B0B0B,
                        0xFF2F2F2F,
                        0xFFB0B0B0,
                        0xFFFFFFFF);
            }
        }
        return null;
    }
}
