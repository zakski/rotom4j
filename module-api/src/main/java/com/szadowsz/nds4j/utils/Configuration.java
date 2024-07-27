package com.szadowsz.nds4j.utils;

import com.szadowsz.nds4j.ref.RomFormat;

public class Configuration {

    private static boolean showCellBounds;

    private static boolean showGuidelines;

    private static boolean renderTransparent;

    private static boolean renderWithBackground;

    private static RomFormat romFormat;

    private Configuration(){}


    public static synchronized RomFormat getExpectedRom(){
        return romFormat;
    }

    public static synchronized boolean isShowCellBounds() {
        return showCellBounds;
    }

    public static synchronized boolean isShowGuidelines() {
        return showGuidelines;
    }

    public static synchronized boolean isRenderTransparent() {
        return renderTransparent;
    }

    public static synchronized boolean isBackground() {
        return renderWithBackground;
    }

    public static synchronized void setShowCellBounds(boolean toggle) {
        showCellBounds = toggle;
    }

    public static synchronized void setExpectedRom(RomFormat rom) {
        romFormat = rom;
    }

    public static synchronized void setShowGuidelines(boolean toggle) {
        showGuidelines = toggle;
    }

    public static synchronized void setRenderTransparent(boolean toggle) {
        renderTransparent = toggle;
    }

    public static synchronized void setRenderBackground(boolean toggle) {
        renderWithBackground = toggle;
    }

}
