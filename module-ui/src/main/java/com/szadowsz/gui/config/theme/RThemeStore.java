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

    private static final Map<String, RTheme> paletteMap = new HashMap<>();

    public static final String DEFAULT_THEME = "default";

    private RThemeStore() {
        // NOOP
    }

    /**
     *
     */
    public static void init() {
        RTheme basic = new RTheme(0xFF0B0B0B, 0xFFB0B0B0, 0xFF2F2F2F, 0xFFFFFFFF, 0xFF787878);
        paletteMap.put(DEFAULT_THEME, basic);
    }

    /**
     *
     * @param themeName
     * @param type Color Type To Get
     * @return
     */
    public static Color getColor(String themeName, RColorType type) {
        RTheme theme = paletteMap.get(themeName);
        return switch (type) {
             case WINDOW_BORDER -> theme.getWindowBorder();

             case NORMAL_BACKGROUND -> theme.getNormal().getBackground();
             case NORMAL_FOREGROUND -> theme.getNormal().getForeground();

             case FOCUS_BACKGROUND -> theme.getFocused().getBackground();
             case FOCUS_FOREGROUND -> theme.getFocused().getForeground();

             case SELECTED_BACKGROUND -> theme.getSelected().getBackground();
             case SELECTED_FOREGROUND -> theme.getSelected().getForeground();

             case ACTIVE_BACKGROUND -> theme.getActive().getBackground();
             case ACTIVE_FOREGROUND -> theme.getActive().getForeground();

             case CURSOR_NEGATIVE  -> theme.getCursor().getBackground();
             case CURSOR           -> theme.getCursor().getForeground();

            case DISABLED_BACKGROUND -> theme.getDisabled().getBackground();
            case DISABLED_FOREGROUND -> theme.getDisabled().getForeground();

        };
    }

    /**
     *
     * @param type Color Type To Get
     * @return
     */
    public static Color getColor(RColorType type) {
        return getColor(DEFAULT_THEME,type);
    }

    public static int getGlobalSchemeNum() {
        return 0; // TODO STUB
    }

    /**
     * Get The RGBA int value of the Specified Color from The Specified Theme
     *
     * @param theme
     * @param type Color Type To Get
     * @return
     */
    public static int getRGBA(String theme, RColorType type) {
        return getColor(theme,type).getRGB();
    }

    /**
     * Get The RGBA int value of the Specified Color from The Default Theme
     * @param type Color Type To Get
     * @return
     */
    public static int getRGBA(RColorType type) {
        return getColor(DEFAULT_THEME,type).getRGB();
    }

    public static RTheme getTheme(int localTheme) {
        return null;
    }
}
