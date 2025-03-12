package com.szadowsz.gui.component.input.slider;

import com.jogamp.newt.event.KeyEvent;
import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RSingle;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.config.RDelayStore;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.RShaderStore;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.clip.RClipboard;
import com.szadowsz.gui.input.keys.RKeyEvent;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.utils.RArrayListBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.opengl.PShader;

import java.awt.*;
import java.util.ArrayList;

import static processing.core.PApplet.*;


/**
 * 1D float slider component.
 * */
public class RSlider extends RSingle {
    private static final Logger LOGGER = LoggerFactory.getLogger(RSlider.class);

    // Current supported precisions
    protected static final ArrayList<Float> precisionRange = new RArrayListBuilder<Float>()
            .add(0.0001f)
            .add(0.001f)
            .add(0.01f)
            .add(0.1f)
            .add(1f)
            .add(10.0f)
            .add(100.0f).build();

    // Current supported numpad characters
    protected final ArrayList<Character> numpadChars = new RArrayListBuilder<Character>()
            .add('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
            .build();

    protected static final String SQUIGGLY_EQUALS = "≈ "; // TODO Not sure if needed

    protected static final String shaderPathDefault = "sliderBackground.glsl"; // Default Shader Resource File Name

    // REGEX constants
    protected static final String REGEX_FRACTION_SEPARATOR = "[.,]";
    protected static final String REGEX_ANY_NUMBER_SERIES = "[0-9]*";
    protected static final String FRACTIONAL_FLOAT_REGEX = REGEX_ANY_NUMBER_SERIES + REGEX_FRACTION_SEPARATOR + REGEX_ANY_NUMBER_SERIES;

    protected float value; // The Current Value of the slider

    // Precision Configuration
    protected int precisionIndex;

    // Default/Min/Max Values of the slider
    protected float valueDefault;
    protected float valueMin;
    protected float valueMax;

    // Mouse Movement to change the slider value
    protected float mouseDeltaX, mouseDeltaY;

    // Configuration Toggles
    protected boolean isConstrained;  // if we clamp to min and max values
    protected boolean isVertical; // if the slider is vertical or horizontal
    protected boolean showPercentIndicator;


    protected float backgroundScroll; // TODO consider need

    // Transient Numpad information TODO consider need
    protected String numpadBufferValue = "";
    protected int numpadInputAppendLastMillis = -1;
    protected boolean wasNumpadInputActive = false;

    // Initial value as string before drag changes the value
    protected String stringValueWhenDragStarted = null;

    public RSlider(RotomGui gui, String path, RGroup parent, float defaultValue, float min, float max, boolean constrained) {
        super(gui, path, parent);
        valueDefault = defaultValue;
        if (!Float.isNaN(defaultValue)) {
            value = defaultValue;
        }
        valueMin = min;
        valueMax = max;
        isConstrained = constrained && max != Float.MAX_VALUE && min != -Float.MAX_VALUE;
        setSensiblePrecision(nf(value, 0, 0));
    }

    public RSlider(RotomGui gui, String path, RGroup parent, float defaultValue, float min, float max, boolean constrained, float suggestPrecision){
        this(gui, path, parent, defaultValue, min, max, constrained);
        setPrecisionByIndex(precisionRange.indexOf(suggestPrecision));
    }

    /**
     * Convert float value to string value that we will write to the graphics buffer
     * @return the string to display
     */
    protected String getDisplayValue() {
        // the numpadBufferValue flickers back to the old value for one frame if we just rely on "isNumpadActive()"
        // so we keep displaying the buffer for 1 more frame with "wasNumpadInputActiveLastFrame"
        if (isNumpadInputActive() || wasNumpadInputActive) {
            return numpadBufferValue;
        }
        if (Float.isNaN(value)) {
            return "NaN";
        }
        String valueToDisplay;
        boolean isFractionalPrecision = precisionRange.get(precisionIndex) % 1f > 0;
        if (isFractionalPrecision) {
            valueToDisplay = nf(value, 0, getFractionalDigitLength(String.valueOf(precisionRange.get(precisionIndex))));
        } else {
            valueToDisplay = nf(round(value), 0, 0);
        }
        // java float literals use . so we also use . to be consistent
        valueToDisplay = valueToDisplay.replaceAll(",", ".");
        return valueToDisplay;
    }

    /**
     * Calculate how many fractional digits the current value has
     *
     * @param value the value as a string
     * @return the number of fractional digits
     */
    protected int getFractionalDigitLength(String value) {
        if (value.contains(".") || value.contains(",")) {
            return value.split(REGEX_FRACTION_SEPARATOR)[1].length();
        }
        return 0;
    }

    /**
     * Is the numpad currently active
     *
     * @return true if active, false otherwise
     */
    protected boolean isNumpadInputActive() {
        return numpadInputAppendLastMillis != -1 &&
                gui.getSketch().millis() <= numpadInputAppendLastMillis + RDelayStore.getKeyboardBufferDelayMs();
    }

    /**
     * Is the numpad currently in number replacement mode
     *
     * @return true if the current value should be replaced, false if it should be apprended to
     */
    protected boolean isNumpadInReplaceMode() {
        return numpadInputAppendLastMillis == -1 ||
                gui.getSketch().millis() - numpadInputAppendLastMillis > RDelayStore.getKeyboardBufferDelayMs();
    }

    /**
     * Set the timestamp of when the numpad was last active
     */
    protected void setNumpadInputActiveStarted() {
        numpadInputAppendLastMillis = gui.getSketch().millis();
    }

    /**
     * Set new precision, based on a valid index that points to an allowed precision value
     * @param newPrecisionIndex the index to check against
     */
    protected void setPrecisionByIndex(int newPrecisionIndex) {
        if (!validatePrecision(newPrecisionIndex)) {
            return;
        }
        precisionIndex = constrain(newPrecisionIndex, 0, precisionRange.size() - 1);
    }

    /**
     * Set default precision based on initial provided value
     *
     * @param value the value provided in the constructor
     */
    protected void setSensiblePrecision(String value) {
        if(isConstrained && (valueMax - valueMin) <= 1){
            setPrecisionByIndex(precisionRange.indexOf(0.01f));
            return;
        }
        if (value.equals("0") || value.equals("0.0")) {
            setPrecisionByIndex(precisionRange.indexOf(0.1f));
            return;
        }
        if (value.matches(FRACTIONAL_FLOAT_REGEX)) {
            int fractionalDigitLength = getFractionalDigitLength(value);
            setPrecisionByIndex(4 - fractionalDigitLength);
            return;
        }
        setPrecisionByIndex(precisionRange.indexOf(1f));
    }

    /**
     * Method to set the value of the slider
     *
     * @param floatToSet the value to set
     */
    protected void setValue(float floatToSet) {
        float previous = value;
        if (floatToSet > valueMax || floatToSet < valueMin) {
            LOGGER.info("Slider {} was set to {} by user - cannot be outside range [{} - {}]",
                    getName(),
                    floatToSet,
                    valueMin,
                    valueMax
            );
        }
        this.value = floatToSet;
        boolean constrained = constrainValue();
        if (!constrained || (value > previous && value == valueMax) || (value < previous && value == valueMin)) {
            onValueChange(); // post-change processing
        }
    }

    /**
     * Validate the new precision value to set is valid
     *
     * @param newPrecisionIndex the index to confirm is in range
     * @return true if valid, false otherwise
     */
    protected boolean validatePrecision(int newPrecisionIndex) {
        return (newPrecisionIndex < precisionRange.size()) &&
                (newPrecisionIndex >= 0);
    }

    /**
     * Method to draw the slider background
     *
     * @param pg the graphics to draw upon
     * @param constrainedThisFrame whether we had to clam the value this frame
     */
    protected void drawBackgroundScroller(PGraphics pg, boolean constrainedThisFrame) {
        // TODO evaluate if rendering slider values correctly, look at how we did scrollbar
        if (!constrainedThisFrame) {
            backgroundScroll -= isVertical ? mouseDeltaY : mouseDeltaX;
        }
        float percentIndicatorNorm = 1f;
        boolean shouldShowPercentIndicator = isConstrained && showPercentIndicator;
        if (shouldShowPercentIndicator) {
            percentIndicatorNorm = constrain(norm(value, valueMin, valueMax), 0, 1);
            backgroundScroll = 0;
        }

        updateBackgroundShader(pg);
        pg.fill(RThemeStore.getRGBA(RColorType.FOCUS_BACKGROUND));
        pg.noStroke();
        pg.rect(1, 0, (size.x - 1) * percentIndicatorNorm, size.y);
        pg.resetShader();

        if (shouldShowPercentIndicator) {
            pg.stroke(RThemeStore.getRGBA(RColorType.WINDOW_BORDER));
            pg.strokeWeight(2);
            float lineX = (size.x - 1) * percentIndicatorNorm;
            pg.line(lineX, 0, lineX, size.y);
        }
    }

    @Override
    protected void drawBackground(PGraphics pg) {
        boolean constrainedThisFrame = constrainValue();
        if (isDragged || isMouseOver) {
            drawBackgroundScroller(pg, constrainedThisFrame);
        }
        mouseDeltaX = 0;
        mouseDeltaY = 0;
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        fillForeground(pg);
        drawTextLeft(pg, name);
        drawTextRight(pg, getDisplayValue() + (isNumpadInputActive() ? "_" : ""), false);
    }

    /**
     * If we expect a max/min value, clamp the current value to these limits, if it exceeds them
     *
     * @return true if we clamped the value, false otherwise
     */
    protected boolean constrainValue() {
        boolean constrained = false;
        if (isConstrained) {
            if (value > valueMax || value < valueMin) {
                constrained = true;
            }
            value = constrain(value, valueMin, valueMax);
        }
        if (constrained) {
            LOGGER.info("Slider {} was constrained to {} - cannot be outside range [{} - {}]",
                    getName(),
                    value,
                    valueMin,
                    valueMax
            );
        }
        return constrained;
    }

    /**
     * How to behave when the slider value changes
     */
    protected void onValueChange() {
        redrawBuffers();
    }

    /**
     * Update the background shader based on the focus colours
     *
     * @param pg the graphics to draw the shader onto
     */
    protected void updateBackgroundShader(PGraphics pg) {
        PShader shader = RShaderStore.getOrLoadShader(gui,shaderPathDefault);
        shader.set("scrollX", backgroundScroll);
        Color bgColor = RThemeStore.getColor(RColorType.NORMAL_BACKGROUND);
        Color fgColor = RThemeStore.getColor(RColorType.FOCUS_BACKGROUND);
        shader.set("colorA", bgColor.getRed(),  bgColor.getGreen(), bgColor.getBlue());
        shader.set("colorB", fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue());
        shader.set("precisionNormalized", norm(precisionIndex, 0, precisionRange.size()));
        pg.shader(shader);
    }

    /**
     * Update the value based on the relevant recorded mouse drag delta
     */
    protected void updateValueMouseInteraction() {
        float mouseDelta = isVertical ? mouseDeltaY : mouseDeltaX;
        if (mouseDelta != 0) {
            LOGGER.debug("Mouse Delta for Slider {} [{}]", name, mouseDelta);
            float delta = mouseDelta * precisionRange.get(precisionIndex);
            LOGGER.debug("Slider Delta for Slider {} [{} - {}]", name, delta, value);
            setValue(value - delta);
            LOGGER.debug("Value for Slider {} [{}]", name, value);
            mouseDeltaX = 0;
            mouseDeltaY = 0;
        }
    }

    /**
     * Numpad number append/replace processing
     *
     * @param input the value to append/replace
     * @param inReplaceMode whether we append to or replace the existing number
     */
    protected void appendNumberToBufferValue(Integer input, boolean inReplaceMode) {
        String inputString = String.valueOf(input);
        setNumpadInputActiveStarted();
        if (inReplaceMode) {
            numpadBufferValue = inputString;
        }
        numpadBufferValue += inputString;
    }

    /**
     * Convert numpad buffer string to float, and set as value
     *
     * @param toParseAsFloat the string to convert
     * @return true if successful, false otherwise
     */
    protected boolean parseNumpadBuffer(String toParseAsFloat) {
        float parsed;
        try {
            parsed = Float.parseFloat(toParseAsFloat);
        } catch (NumberFormatException formatException) {
            LOGGER.error(formatException.getMessage(), formatException);
            return false;
        }
        setValue(parsed);
        onValueChangeEnd();
        return true;
    }

    /**
     * Numpad input key processing
     *
     * @param e the key event to process
     */
    protected void readNumpadInput(RKeyEvent e) {
        boolean inReplaceMode = isNumpadInReplaceMode();
        if (numpadChars.contains(e.getKey())) {
            appendNumberToBufferValue(Integer.valueOf(String.valueOf(e.getKey())), inReplaceMode);
            e.consume();
        }
        switch (e.getKey()) {
            case '.':
            case ',':
                setNumpadInputActiveStarted();
                if (numpadBufferValue.isEmpty()) {
                    numpadBufferValue += "0";
                }
                if (!numpadBufferValue.endsWith(".")) {
                    numpadBufferValue += ".";
                }
                e.consume();
                break;
            case '+':
            case '-':
                if (inReplaceMode) {
                    numpadBufferValue = "" + e.getKey();
                }
                setNumpadInputActiveStarted();
                e.consume();
                break;
        }
    }

    /**
     * Update the slider based on active numpad changes
     */
    protected void updateNumpad() {
        if (!isNumpadInputActive() && wasNumpadInputActive) {
            if (numpadBufferValue.endsWith(".")) {
                numpadBufferValue += "0";
            }
            if (parseNumpadBuffer(numpadBufferValue)) {
                setSensiblePrecision(numpadBufferValue);
            }
        }
        wasNumpadInputActive = isNumpadInputActive();
    }

    /**
     * Method to ensure any expected background shaders are loaded in the Store, as expected
     */
    public void initSliderBackgroundShader() {
        RShaderStore.getOrLoadShader(gui,shaderPathDefault);
    }


    @Override
    public float suggestWidth() {
        return RFontStore.calcMainTextWidth(name, RLayoutStore.getCell()) +
                Math.max(RLayoutStore.getCell() * 2, RFontStore.calcMainTextWidth(getValueAsString(),RLayoutStore.getCell()));
    }

    @Override
    public void keyPressedOver(RKeyEvent e, float x, float y) {
         // TODO Difference between has focus and just mouse over
        super.keyPressedOver(e, x, y);
        if (e.getKey() == 'r') {
            if (!Float.isNaN(value)) {
                setValue(value);
            }
            e.consume();
        }
        readNumpadInput(e);
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C) { // TODO better Key handling
            String value = getDisplayValue().replaceAll(SQUIGGLY_EQUALS, "");
            if (value.endsWith(".")) {
                value += "0";
            }
            RClipboard.copy(value);
            e.consume();
        }
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_V) { // TODO better Key handling
            String clipboardString = RClipboard.paste();

            try {
                float clipboardValue = Float.parseFloat(clipboardString);
                if (!Float.isNaN(clipboardValue)) {
                    setValue(clipboardValue);
                } else {
                    throw new NumberFormatException("Could not parse float from this clipboard string: " + clipboardString);
                }
            } catch (NumberFormatException nfe) {
                LOGGER.warn("Could not parse float from this clipboard string: {}", clipboardString);
            }
            e.consume();
        }
    }

    @Override
    public void mousePressed(RMouseEvent mouseEvent, float mouseY) {
        LOGGER.debug("Mouse Pressed for Slider {}", name);
        super.mousePressed(mouseEvent,mouseY);
        stringValueWhenDragStarted = getValueAsString();
    }

    @Override
    public void mouseReleasedOverComponent(RMouseEvent mouseEvent, float mouseY) {
        mouseReleasedAnywhere(mouseEvent,mouseY);
    }

    @Override
    public void mouseReleasedAnywhere(RMouseEvent mouseEvent, float mouseY) {
        if(stringValueWhenDragStarted != null && !stringValueWhenDragStarted.equals(getValueAsString())){
            onValueChangeEnd();
        }
        stringValueWhenDragStarted = null;
        super.mouseReleasedAnywhere(mouseEvent,mouseY);
    }

    @Override
    public void mouseDragged(RMouseEvent mouseEvent) {
        super.mouseDragged(mouseEvent);
        mouseDeltaX = mouseEvent.getPrevX() - mouseEvent.getX();
        mouseDeltaY = mouseEvent.getPrevY() - mouseEvent.getY();
        if (isVertical) {
            LOGGER.debug("Mouse DeltaY for Slider {} [{} = {} - {}]", name, mouseDeltaY, mouseEvent.getPrevY(), mouseEvent.getY());
        } else {
            LOGGER.debug("Mouse DeltaX for Slider {} [{} = {} - {}]", name, mouseDeltaX, mouseEvent.getPrevX(), mouseEvent.getX());
        }
        redrawBuffers(); // REDRAW-VALID: we should redraw the buffer as dragging changes the slider value
        mouseEvent.consume();
    }

    @Override
    public void updateValues(){
        if (isDragged || isMouseOver) {
           updateValueMouseInteraction();
        }
        updateNumpad();
    }

    public float getValueAsFloat() {
        return value;
    }

    @Override
    public String getValueAsString() {
        return getDisplayValue();
    }

    public void setValueFromParent(float value) {
        if (value > valueMax || value < valueMin) {
            LOGGER.info("Slider {} was set to {} by parent {} - cannot be outside range [{} - {}]",
                    getName(),
                    value,
                    getParent().getName(),
                    valueMin,
                    valueMax
            );
        }
        this.value = value;
        onValueChange();
    }
}
