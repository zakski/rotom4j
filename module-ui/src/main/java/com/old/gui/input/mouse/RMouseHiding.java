package com.old.gui.input.mouse;

import com.jogamp.newt.opengl.GLWindow;
import processing.core.PApplet;

/**
 * Utility Input Class to hide the mouse while dragging
 */
public class RMouseHiding {
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
    public static void tryHideMouseForDragging(PApplet sketch) {
        if(!shouldHideWhenDragging){
            return;
        }
        sketch.noCursor();
        if(!isMouseHidden){
            mouseHidePosX = sketch.mouseX;
            mouseHidePosY = sketch.mouseY;
        }
        isMouseHidden = true;
    }

    /**
     * If Set, Reveal the Mouse after Dragging
     */
    public static void tryRevealMouseAfterDragging(PApplet sketch) {
        if(!shouldHideWhenDragging && !isMouseHidden){
            return;
        }
        if(isMouseHidden){
            resetMousePos(sketch);
        }
        isMouseHidden = false;
        sketch.cursor();
    }

    /**
     * Reset the mouse position
     */
    private static void resetMousePos(PApplet sketch) {
        ((GLWindow) sketch.getSurface().getNative()).warpPointer(mouseHidePosX, mouseHidePosY);
    }
}
