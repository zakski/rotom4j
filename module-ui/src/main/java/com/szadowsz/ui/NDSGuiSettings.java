package com.szadowsz.ui;

import com.szadowsz.ui.constants.theme.Theme;
import com.szadowsz.ui.constants.theme.ThemeStore;
import com.szadowsz.ui.constants.theme.ThemeType;
import com.szadowsz.ui.input.MouseHiding;
import com.szadowsz.ui.store.FontStore;
import com.szadowsz.ui.store.JsonSaveStore;
import com.szadowsz.ui.store.LayoutStore;

import static com.szadowsz.ui.constants.GlobalReferences.gui;

/**
 * GUI System Settings Representation
 */
public class NDSGuiSettings {
    // Use a Toolbar as the root Folder, rather than a freestanding window
    private boolean useToolbarAsRoot = true;

    private boolean autosaveOnExitEnabled;
    private boolean autosaveLockGuardEnabled;
    private boolean mouseShouldHideWhenDragging;
    private boolean mouseShouldConfineToWindow;
    private boolean autosuggestWindowWidth;
    private long autosaveLockGuardMillisLimit;
    private float cellSize;
    private int mainFontSize, sideFontSize;
    private boolean startWithGuiHidden = false;
    private boolean hideBuiltInFolders = false;
    private boolean hideRadioValue = false;
    private Theme themeCustom = null;
    private ThemeType themePreset = null;
    private String pathToSpecificSaveToLoadOnStartup = null;
    private String pathToSpecificSaveToLoadOnStartupOnce = null;
    private String sketchNameOverride = null;
    private int smoothingValue;
    private boolean showSquigglyEquals = false;
    private String customGuiDataFolder = null;

    /**
     * Constructor, call this before any other function here.
     * Meant to be used like this in setup():
     * * <pre>
     *  *      gui = new LazyGui(this, new LazyGuiSettings()
     *  *          .setLoadLatestSaveOnStartup(false)
     *  *          .setAutosaveOnExit(false)
     *  *          // ...
     *  *      );
     *  * </pre>
     */
    public NDSGuiSettings() {
        initializeDefaultsFromGlobalConstants();
    }

    void initializeDefaultsFromGlobalConstants() {
        this.autosaveOnExitEnabled = JsonSaveStore.autosaveOnExitEnabled;
        this.autosaveLockGuardEnabled = JsonSaveStore.autosaveLockGuardEnabled;
        this.autosaveLockGuardMillisLimit = JsonSaveStore.autosaveLockGuardMillisLimit;
        this.mouseShouldHideWhenDragging = MouseHiding.shouldHideWhenDragging;
        this.mouseShouldConfineToWindow = MouseHiding.shouldConfineToWindow;
        this.cellSize = LayoutStore.cell;
        this.startWithGuiHidden = LayoutStore.isGuiHidden();
        this.smoothingValue = LayoutStore.getSmoothingValue();
        this.autosuggestWindowWidth = LayoutStore.getAutosuggestWindowWidth();
        this.mainFontSize = FontStore.mainFontSizeDefault;
        this.sideFontSize = FontStore.sideFontSizeDefault;
    }

    void applyEarlyStartupSettings() {
        if (customGuiDataFolder != null) {
            JsonSaveStore.setCustomGuiDataFolder(customGuiDataFolder);
        }
        JsonSaveStore.autosaveOnExitEnabled = autosaveOnExitEnabled;
        JsonSaveStore.autosaveLockGuardEnabled = autosaveLockGuardEnabled;
        JsonSaveStore.autosaveLockGuardMillisLimit = autosaveLockGuardMillisLimit;
        MouseHiding.shouldHideWhenDragging = mouseShouldHideWhenDragging;
        MouseHiding.shouldConfineToWindow = mouseShouldConfineToWindow;
        LayoutStore.cell = cellSize;
        LayoutStore.setAutosuggestWindowWidth(autosuggestWindowWidth);
        LayoutStore.setSmoothingValue(smoothingValue);
        FontStore.mainFontSizeDefault = mainFontSize;
        FontStore.sideFontSizeDefault = sideFontSize;
        if (themeCustom != null) {
            ThemeStore.setCustomPaletteAndMakeDefaultBeforeInit(themeCustom);
        } else if (themePreset != null) {
            ThemeStore.selectThemeByTypeBeforeInit(themePreset);
        }
        LayoutStore.setIsGuiHidden(startWithGuiHidden);
        LayoutStore.setHideRadioValue(hideRadioValue);
        LayoutStore.setDisplaySquigglyEquals(showSquigglyEquals);
        if (sketchNameOverride != null) {
            LayoutStore.setOverridingSketchName(sketchNameOverride);
        }
    }

    void applyLateStartupSettings() {
        if (hideBuiltInFolders) {
            gui.hide(NDSGui.optionsFolderName);
            gui.hide(NDSGui.savesFolderName);
        }
    }


    /**
     * Get whether to use a Toolbar as the Root Folder
     *
     * @return whether to use a Toolbar as the Root Folder, rather than a Freestanding Window
     */
    public boolean getUseToolbarAsRoot(){
        return useToolbarAsRoot;
    }


    String getSpecificSaveToLoadOnStartup() {
        return pathToSpecificSaveToLoadOnStartup;
    }

    String getSpecificSaveToLoadOnStartupOnce() {
        return pathToSpecificSaveToLoadOnStartupOnce;
    }

    boolean getShowSquigglyEqualsInsideSliders() {
        return showSquigglyEquals;
    }

    /**
     *  Set whether to use a Toolbar as the Root Folder, rather than a Freestanding Window
     *
     * @param useToolbarAsRoot whether to use a toolbar as the root folder, rather than a freestanding window
     * @return this settings object for chaining statements easily
     */
    public NDSGuiSettings setUseToolbarAsRoot(boolean useToolbarAsRoot) {
        this.useToolbarAsRoot = useToolbarAsRoot;
        return this;
    }

    /**
     * Sets one theme from the preset options on startup.
     *
     * @param themePreset selected preset, one of "dark", "light", "pink", "blue"
     * @return this settings object for chaining statements easily
     */
    public NDSGuiSettings setThemePreset(String themePreset) {
        ThemeType foundTheme = ThemeType.getValue(themePreset);
        if (foundTheme != null) {
            this.themePreset = foundTheme;
        }
        return this;
    }

    /**
     * Sets a custom theme defined by individual hex colors.
     *
     * @param windowBorderColor     color of the window border
     * @param normalBackgroundColor normal background color
     * @param focusBackgroundColor  focus background color
     * @param normalForegroundColor normal foreground color
     * @param focusForegroundColor  focus foreground color
     * @return this settings object for chaining statements easily
     */
    public NDSGuiSettings setThemeCustom(int windowBorderColor, int normalBackgroundColor, int focusBackgroundColor, int normalForegroundColor, int focusForegroundColor) {
        this.themeCustom = new Theme(windowBorderColor, normalBackgroundColor, focusBackgroundColor, normalForegroundColor, focusForegroundColor);
        return this;
    }


    /**
     * Should the built-in folders start hidden?
     * That is the automatically created gui folders like "options" and "saves".
     * They will still exist and will try to load their values from json
     * (unless overriden with other constructor settings)
     * and the gui will still use their current values, they will just hide from the user.
     * You can reveal them again after initialization with <code>gui.show("options")</code> and <code>gui.show("saves")</code>
     *
     * @param shouldHideFolders whether the built-in folders should start hidden
     * @return this settings object for chaining statements easily
     * @see #setAutosaveOnExit(boolean)
     */
    public NDSGuiSettings setHideBuiltInFolders(boolean shouldHideFolders) {
        this.hideBuiltInFolders = shouldHideFolders;
        return this;
    }

    /**
     * Should the gui start hidden? Toggle hiding with the 'H' hotkey.
     *
     * @param shouldHideGui whether the gui should start hidden
     * @return this settings object for chaining statements easily
     */
    public NDSGuiSettings setStartGuiHidden(boolean shouldHideGui) {
        this.startWithGuiHidden = shouldHideGui;
        return this;
    }

    /**
     * Loads a specific save file on startup, tries to look inside the save folder for the file first before assuming the user gave the absolute path.
     * Also disables loading the latest save on startup.
     *
     * @param fileName name of the save file inside the save folder to load in the format "1" or "1.json" or a full absolute path to it anywhere on disk
     * @return this settings object for chaining statements easily
     * @see #setLoadSpecificSaveOnStartupOnce(String)
     */
    public NDSGuiSettings setLoadSpecificSaveOnStartup(String fileName) {
        this.pathToSpecificSaveToLoadOnStartup = fileName;
        return this;
    }

    /**
     * Loads a specific save file on startup if the gui finds its save folder empty.
     * Can be useful for fine-tuning global initial settings for all your gui sketches.
     * Does not disable loading latest save on startup.
     *
     * @param fileName name of the save file to load in the format "1" or "1.json" in the save folder or a full absolute path to it anywhere on disk
     * @return this settings object for chaining statements easily
    * @see #setLoadSpecificSaveOnStartup(String)
     */
    public NDSGuiSettings setLoadSpecificSaveOnStartupOnce(String fileName) {
        this.pathToSpecificSaveToLoadOnStartupOnce = fileName;
        return this;
    }

    /**
     * Should the GUI try to autosave its state before closing gracefully?
     *
     * @param autosaveEnabled should the sketch try to save when closing
     * @return this settings object for chaining statements easily
     */
    public NDSGuiSettings setAutosaveOnExit(boolean autosaveEnabled) {
        this.autosaveOnExitEnabled = autosaveEnabled;
        return this;
    }

    /**
     * When the lock guard is enabled it checks whether the last frame took too long and does not autosave if it did.
     * Autosaving isn't always a good idea - this protects the user against cases when the sketch gets stuck in an endless loop that may have been caused by selecting some dangerous gui values.
     *
     * @param autosaveLockGuardEnabled should the autosave be guarded against saving bad
     * @return this settings object for chaining statements easily
     * @see #setAutosaveLockGuardMillisLimit(int)
     */
    public NDSGuiSettings setAutosaveLockGuardEnabled(boolean autosaveLockGuardEnabled) {
        this.autosaveLockGuardEnabled = autosaveLockGuardEnabled;
        return this;
    }

    /**
     * The millis limit for the last frame for the autosave lock guard to take effect and block autosaving.
     * Only has an effect when autosave lock guard is enabled.
     *
     * @param autosaveLockGuardMillisLimit last frame limit in millis
     * @return this settings object for chaining statements easily
     * @see #setAutosaveLockGuardEnabled(boolean)
     */
    public NDSGuiSettings setAutosaveLockGuardMillisLimit(int autosaveLockGuardMillisLimit) {
        this.autosaveLockGuardMillisLimit = autosaveLockGuardMillisLimit;
        return this;
    }

    /**
     * Should the mouse be hidden when dragging a slider or a plot?
     * Hiding the mouse can give the user a more immersive feeling, but it can also be disorienting.
     * The mouse can still hit the corners of your screen when hidden.
     * On mouse released - the hidden mouse position resets to where the dragging started.
     *
     * @param mouseShouldHideWhenDragging should the mouse hide when dragging an element like a slider?
     * @return this settings object for chaining statements easily
     */
    public NDSGuiSettings setMouseHideWhenDragging(boolean mouseShouldHideWhenDragging) {
        this.mouseShouldHideWhenDragging = mouseShouldHideWhenDragging;
        return this;
    }

    /**
     * Should the mouse be locked inside the sketch window? You can still exit the sketch with ESC.
     *
     * @param mouseShouldConfineToWindow confine mouse to sketch window
     * @return this settings object for chaining statements easily
     */
    public NDSGuiSettings setMouseConfineToWindow(boolean mouseShouldConfineToWindow) {
        this.mouseShouldConfineToWindow = mouseShouldConfineToWindow;
        return this;
    }

    /**
     * This sets the cell size that all gui controls use to draw themselves.
     * Also sets the distance between guide grid dots in the background.
     *
     * @param cellSize global cell size
     * @return this settings object for chaining statements easily
     */
    public NDSGuiSettings setCellSize(float cellSize) {
        this.cellSize = cellSize;
        return this;
    }

    /**
     * This sets the main font size used everywhere by the gui.
     * If you'd like to also change the text color, use a custom theme - text falls under foreground color there.
     *
     * @param mainFontSize main font size
     * @return this settings object for chaining statements easily
     * @see #setThemeCustom(int, int, int, int, int)
     */
    public NDSGuiSettings setMainFontSize(int mainFontSize) {
        this.mainFontSize = mainFontSize;
        return this;
    }

    /**
     * This sets the usually smaller side font size used in a few places by the gui.
     * If you'd like to also change the text color, use a custom theme - text falls under foreground color there.
     *
     * @param sideFontSize side font size
     * @return this settings object for chaining statements easily
     * @see #setThemeCustom(int, int, int, int, int)
     */
    public NDSGuiSettings setSideFontSize(int sideFontSize) {
        this.sideFontSize = sideFontSize;
        return this;
    }

    /**
     * Overrides what the root window title displays.
     * It shows the name of your sketch by default, but you can set a custom value here.
     *
     * @param sketchNameOverride name to display in root window title
     * @return this settings object for chaining statements easily
     */
    public NDSGuiSettings setSketchNameOverride(String sketchNameOverride) {
        this.sketchNameOverride = sketchNameOverride;
        return this;
    }

    /**
     * Override the GUI trying to make window widths fit its contents snugly based on longest text in the row at window opening time.
     * Setting this to false disables this behavior and sets all windows to some default size fitting for the folder type.
     *
     * @param shouldAutosuggest should the windows try to auto-detect optimal width?
     * @return this settings object for chaining statements easily
     */
    public NDSGuiSettings setAutosuggestWindowWidth(boolean shouldAutosuggest) {
        this.autosuggestWindowWidth = shouldAutosuggest;
        return this;
    }

    /**
     * Override the default antialiasing level of `smooth(4)` for the GUI canvas.
     * A value of 0 will set `noSmooth()` instead.
     * This will not affect the smoothing level of your entire sketch - it will only affect the GUI, which will still be drawn as an image on top of your potentially differently smoothed sketch.
     *
     * @param smoothValue the value to be passed to `smooth()` for the gui canvas - if 0 then `noSmooth()` is used
     * @return this settings object for chaining statements easily
     * @see <a href="https://processing.org/reference/smooth_.html">smooth()</a>
     * @see <a href="https://github.com/processing/processing4/issues/694">a value of 8 breaks PGraphics on some machines</a>
     */
    public NDSGuiSettings setSmooth(int smoothValue) {
        this.smoothingValue = smoothValue;
        return this;
    }

    /**
     * Hide the selected value text on the right of the radio row and replace it with a generic folder icon.
     * This can be useful when you want to use radio buttons with longer string values.
     * The option strings will still be visible once you open the radio folder.
     * This applies globally to all radio buttons and the value text is visible by default.
     *
     * @param hideRadioValue whether to hide the radio value text
     * @return this settings object for chaining statements easily
     */
    public NDSGuiSettings setHideRadioValue(boolean hideRadioValue) {
        this.hideRadioValue = hideRadioValue;
        return this;
    }

    /**
     * Show the approximately equals double tilde '≈' in sliders when visually shown values are not exactly equal to the actual float values used by the program.
     * This happens when the display value is rounded by the currently selected slider precision (controlled by the mouse wheel).
     * This is false by default, assuming people care more about the horizontal space that would be taken up by '≈ ' than the exact precision of the values.
     * Even when true, this doesn't show it everywhere, (for example in plot rows where multiple slider values are combined, because the horizontal space is very limited there), for more details about the implementation see <pre>SliderNode.displaySquigglyEquals</pre> and the related SliderNode constructor that sets it.
     *
     * @param showSquigglyEquals whether to show the squiggly equals '≈' in sliders where the underlying values are not exactly what is shown
     * @return this settings object for chaining statements easily
     */
    public NDSGuiSettings setShowSquigglyEqualsInsideSliders(boolean showSquigglyEquals) {
        this.showSquigglyEquals = showSquigglyEquals;
        return this;
    }

    /**
     * Set a custom data folder for the gui to use for json saves and png screenshots.
     * Can be either absolute path or relative to the sketch data folder.
     * It is inside the individual sketch data folder by default under /gui/.
     * Multiple sketches can use the same path, their data will still be separated by their sketch names.
     * The data folder will be created if it doesn't exist.
     *
     * @param customPath the custom path to set, "gui" by default
     * @return this settings object for chaining statements easily
     */
    public NDSGuiSettings setCustomGuiDataFolder(String customPath) {
        this.customGuiDataFolder = customPath;
        return this;
    }
}
