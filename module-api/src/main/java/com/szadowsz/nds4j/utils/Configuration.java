package com.szadowsz.nds4j.utils;

public class Configuration {

    private static boolean showCellBounds = true;

    private static boolean showGuidelines = false;

    private static boolean renderTransparent = true;

    private Configuration(){}


    public static synchronized boolean isShowCellBounds() {
        return showCellBounds;
    }

    public static synchronized boolean isShowGuidelines() {
        return showGuidelines;
    }

    public static synchronized boolean isRenderTransparent() {
        return renderTransparent;
    }

    public static synchronized void setShowCellBounds(boolean toggle) {
        showCellBounds = toggle;
    }

    public static synchronized void setShowGuidelines(boolean toggle) {
        showGuidelines = toggle;
    }

    public static synchronized void setRenderTransparent(boolean toggle) {
        renderTransparent = toggle;
    }
}
