package com.old.ui.input;

import com.old.ui.constants.GlobalReferences;

/**
 * Utility Input Class to hide the mouse while dragging
 */
public class MouseHiding {
    public static boolean shouldHideWhenDragging = true;
    public static boolean shouldConfineToWindow = false;
    private static int mouseHidePosX;
    private static int mouseHidePosY;
    private static boolean isMouseHidden = false;

//    public static void updateSettings() {
//        GlobalReferences.gui.pushFolder("mouse");
//        shouldHideWhenDragging = GlobalReferences.gui.toggle("hide when dragged", shouldHideWhenDragging);
//        shouldConfineToWindow = GlobalReferences.gui.toggle("confine to window", shouldConfineToWindow);
//        GlobalReferences.appWindow.confinePointer(shouldConfineToWindow);
//        GlobalReferences.gui.popFolder();
//        if(isMouseHidden){
//            GlobalReferences.app.mouseX = mouseHidePosX;
//            GlobalReferences.app.mouseY = mouseHidePosY;
//        }
//    }

    /**
     * If Set, Hide the Mouse while Dragging
     */
    public static void tryHideMouseForDragging() {
        if(!shouldHideWhenDragging){
            return;
        }
        GlobalReferences.app.noCursor();
        if(!isMouseHidden){
            mouseHidePosX = GlobalReferences.app.mouseX;
            mouseHidePosY = GlobalReferences.app.mouseY;
        }
        isMouseHidden = true;
    }

    /**
     * If Set, Reveal the Mouse after Dragging
     */
    public static void tryRevealMouseAfterDragging() {
        if(!shouldHideWhenDragging && !isMouseHidden){
            return;
        }
        if(isMouseHidden){
            resetMousePos();
        }
        isMouseHidden = false;
        GlobalReferences.app.cursor();
    }

    /**
     * Reset the mouse position
     */
    private static void resetMousePos() {
        GlobalReferences.appWindow.warpPointer(mouseHidePosX, mouseHidePosY);
    }
}
