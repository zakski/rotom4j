package com.szadowsz.ui;
/**
 * GUI System Settings Representation
 */
public class NDSGuiSettings {
    // Use a Toolbar as the root Folder, rather than a freestanding window
    private boolean useToolbarAsRoot;


    public boolean getUseToolbarAsRoot(){
        return useToolbarAsRoot;
    }

    /**
     *  Whether to use a Toolbar as the Root Folder, rather than a Freestanding Window
     *
     * @param useToolbarAsRoot whether to use a toolbar as the root folder, rather than a freestanding window
     * @return this settings object for chaining statements easily
     */
    public NDSGuiSettings setUseToolbarAsRoot(boolean useToolbarAsRoot) {
        this.useToolbarAsRoot = useToolbarAsRoot;
        return this;
    }
}
