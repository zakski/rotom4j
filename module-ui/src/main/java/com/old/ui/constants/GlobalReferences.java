package com.old.ui.constants;

import com.jogamp.newt.opengl.GLWindow;
import com.old.ui.NDSGui;
import processing.core.PApplet;

public class GlobalReferences {

    public static PApplet app;
    public static NDSGui gui;
    public static GLWindow appWindow;

    public static void init(NDSGui gui, PApplet app){
        GlobalReferences.app = app;
        appWindow = (GLWindow) app.getSurface().getNative();
        GlobalReferences.gui = gui;
    }
}
