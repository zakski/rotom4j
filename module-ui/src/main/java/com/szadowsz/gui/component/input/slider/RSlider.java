package com.szadowsz.gui.component.input.slider;

import com.jogamp.newt.event.KeyEvent;
import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.config.RDelayStore;
import com.szadowsz.gui.config.RFontStore;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.RShaderStore;
import com.szadowsz.gui.config.theme.RThemeColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.RClipboard;
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
 * 1D slider component.
 * */
public class RSlider extends RComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RSlider.class);

    // TODO LazyGui
    protected static final ArrayList<Float> precisionRange = new RArrayListBuilder<Float>()
            .add(0.0001f)
            .add(0.001f)
            .add(0.01f)
            .add(0.1f)
            .add(1f)
            .add(10.0f)
            .add(100.0f).build();

    // TODO LazyGui
    protected final ArrayList<Character> numpadChars = new RArrayListBuilder<Character>()
            .add('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
            .build();

    protected static final String SQUIGGLY_EQUALS = "≈ "; // TODO LazyGui

    protected static final String shaderPathDefault = "sliderBackground.glsl"; // TODO LazyGui

    protected static final String REGEX_FRACTION_SEPARATOR = "[.,]"; // TODO LazyGui
    protected static final String REGEX_ANY_NUMBER_SERIES = "[0-9]*"; // TODO LazyGui
    protected static final String FRACTIONAL_FLOAT_REGEX = REGEX_ANY_NUMBER_SERIES + REGEX_FRACTION_SEPARATOR + REGEX_ANY_NUMBER_SERIES; // TODO LazyGui

    protected float value; // TODO LazyGui

    protected int precisionIndex;  // TODO LazyGui
    protected float precisionValue;  // TODO LazyGui

    protected float valueDefault; // TODO LazyGui

    protected boolean isConstrained;  // TODO LazyGui
    protected float valueMin; // TODO LazyGui
    protected float valueMax; // TODO LazyGui

    protected boolean showPercentIndicator;  // TODO LazyGui

    protected boolean isVertical; // TODO LazyGui
    protected float mouseDeltaX, mouseDeltaY; // TODO LazyGui

    protected float backgroundScroll; // TODO LazyGui

    protected String numpadBufferValue = ""; // TODO LazyGui
    protected int numpadInputAppendLastMillis = -1; // TODO LazyGui
    protected boolean wasNumpadInputActive = false; // TODO LazyGui

    protected String stringValueWhenDragStarted = null; // TODO LazyGui

    // true by default locally to be overridden by the global setting LayoutStore.shouldDisplaySquigglyEquals() that is false by default
    protected boolean displaySquigglyEquals = true; // TODO LazyGui

    public RSlider(RotomGui gui, String path, RGroup parent, float defaultValue, float min, float max, boolean constrained) {  // TODO LazyGui
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

    public RSlider(RotomGui gui, String path, RGroup parent, float defaultValue, float min, float max, float precision, boolean constrained) {  // TODO LazyGui
        this(gui, path, parent,defaultValue,min,max,constrained);
        int index = precisionRange.indexOf(precision);
        if (index>=0){
            precisionIndex = index;
        }
    }

    public RSlider(RotomGui gui, String path, RGroup parent, float defaultValue, float min, float max, boolean constrained, boolean displaySquigglyEquals){  // TODO LazyGui
        this(gui, path, parent, defaultValue, min, max, constrained);
        this.displaySquigglyEquals = displaySquigglyEquals;
    }

    protected String getDisplayValue() { // TODO LazyGui
        // the numpadBufferValue flickers back to the old value for one frame if we just rely on "isNumpadActive()"
        // so we keep displaying the buffer for 1 more frame with "wasNumpadInputActiveLastFrame"
        if (isNumpadInputActive() || wasNumpadInputActive) {
            return numpadBufferValue;
        }
        if (Float.isNaN(value)) {
            return "NaN";
        }
        String valueToDisplay;
        boolean isFractionalPrecision = precisionValue % 1f > 0;
        if (isFractionalPrecision) {
            valueToDisplay = nf(value, 0, getFractionalDigitLength(String.valueOf(precisionValue)));
        } else {
            valueToDisplay = nf(round(value), 0, 0);
        }
        if (displaySquigglyEquals && RLayoutStore.shouldDisplaySquigglyEquals()) {
            String valueWithoutRounding = nf(value, 0, 0);
            boolean precisionRoundingHidesInformation = valueToDisplay.length() < valueWithoutRounding.length();
            if (precisionRoundingHidesInformation) {
                valueToDisplay = SQUIGGLY_EQUALS + valueToDisplay;
            }
        }
        // java float literals use . so we also use . to be consistent
        valueToDisplay = valueToDisplay.replaceAll(",", ".");
        return valueToDisplay;
    }

    protected int getFractionalDigitLength(String value) { // TODO LazyGui
        if (value.contains(".") || value.contains(",")) {
            return value.split(REGEX_FRACTION_SEPARATOR)[1].length();
        }
        return 0;
    }

    protected boolean isNumpadInputActive() { // TODO LazyGui
        return numpadInputAppendLastMillis != -1 &&
                gui.getSketch().millis() <= numpadInputAppendLastMillis + RDelayStore.getKeyboardBufferDelayMillis();
    }

    protected boolean isNumpadInReplaceMode() {
        return numpadInputAppendLastMillis == -1 ||
                gui.getSketch().millis() - numpadInputAppendLastMillis > RDelayStore.getKeyboardBufferDelayMillis();
    }

    protected void setNumpadInputActiveStarted() { // TODO LazyGui
        numpadInputAppendLastMillis = gui.getSketch().millis();
    }

    protected void setPrecisionByIndex(int newPrecisionIndex) {
        if (!validatePrecision(newPrecisionIndex)) {
            return;
        }
        precisionIndex = constrain(newPrecisionIndex, 0, precisionRange.size() - 1); // TODO LazyGui
        precisionValue = precisionRange.get(precisionIndex);
    }

    protected void setSensiblePrecision(String value) { // TODO LazyGui
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

    protected void setValue(float floatToSet) { // TODO LazyGui
        value = floatToSet;
        onValueChange();
    }

    protected boolean validatePrecision(int newPrecisionIndex) { // TODO LazyGui
        return (newPrecisionIndex < precisionRange.size()) &&
                (newPrecisionIndex >= 0);
    }

    protected void drawBackgroundScroller(PGraphics pg, boolean constrainedThisFrame) { // TODO LazyGui
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
        pg.fill(RThemeStore.getRGBA(RThemeColorType.FOCUS_BACKGROUND));
        pg.noStroke();
        pg.rect(1, 0, (size.x - 1) * percentIndicatorNorm, size.y);
        pg.resetShader();

        if (shouldShowPercentIndicator) {
            pg.stroke(RThemeStore.getRGBA(RThemeColorType.WINDOW_BORDER));
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
        drawTextRight(pg, getDisplayValue() + (isNumpadInputActive() ? "_" : ""), true);
    }

    protected boolean constrainValue() { // TODO LazyGui
        boolean constrained = false;
        if (isConstrained) {
            if (value > valueMax || value < valueMin) {
                constrained = true;
            }
            value = constrain(value, valueMin, valueMax);
        }
        return constrained;
    }

    protected void onValueChange() { // TODO LazyGui
        constrainValue();
    }

    public void initSliderBackgroundShader() {
        RShaderStore.getOrLoadShader(gui,shaderPathDefault);
    }

    protected void updateBackgroundShader(PGraphics pg) { // TODO LazyGui
        PShader shader = RShaderStore.getOrLoadShader(gui,shaderPathDefault);
        shader.set("scrollX", backgroundScroll);
        Color bgColor = RThemeStore.getColor(RThemeColorType.NORMAL_BACKGROUND);
        Color fgColor = RThemeStore.getColor(RThemeColorType.FOCUS_BACKGROUND);
        shader.set("colorA", bgColor.getRed(),  bgColor.getGreen(), bgColor.getBlue());
        shader.set("colorB", fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue());
        shader.set("precisionNormalized", norm(precisionIndex, 0, precisionRange.size()));
        pg.shader(shader);
    }

    protected void updateValueMouseInteraction() {
        float mouseDelta = isVertical ? mouseDeltaY : mouseDeltaX;
        if (mouseDelta != 0) {
            float delta = mouseDelta * precisionValue;
            setValue(value - delta);
            mouseDeltaX = 0;
            mouseDeltaY = 0;
        }
    }

    protected void appendNumberToBufferValue(Integer input, boolean inReplaceMode) {
        String inputString = String.valueOf(input);
        setNumpadInputActiveStarted();
        if (inReplaceMode) {
            numpadBufferValue = inputString;
        }
        numpadBufferValue += inputString;
    }

    protected boolean parseNumpadBuffer(String toParseAsFloat) {
        float parsed;
        try {
            parsed = Float.parseFloat(toParseAsFloat);
        } catch (NumberFormatException formatException) {
            println(formatException.getMessage(), formatException);
            return false;
        }
        setValue(parsed);
        onValueChangeEnd();
        return true;
    }

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

    @Override
    public float suggestWidth() {
        return RFontStore.calcMainTextWidth(name,RLayoutStore.getCell()) +
                Math.max(RLayoutStore.getCell() * 2, RFontStore.calcMainTextWidth(getValueAsString(),RLayoutStore.getCell()));
    }

    /**
     * Handle a pressed key while over the node
     *
     * @param e the pressed key
     * @param x x position
     * @param y y position
     */
    public void keyPressedOverComponent(RKeyEvent e, float x, float y) { // TODO LazyGui
         // TODO Difference between has focus and just mouse over
        super.keyPressedOverComponent(e, x, y);
        if (e.getKey() == 'r') {
            if (!Float.isNaN(value)) {
                setValue(value);
            }
            e.consume();
        }
        readNumpadInput(e);
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C) { // TODO LazyGui better Key handling
            String value = getDisplayValue().replaceAll(SQUIGGLY_EQUALS, "");
            if (value.endsWith(".")) {
                value += "0";
            }
            RClipboard.copy(value);
            e.consume();
        }
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_V) { // TODO LazyGui better Key handling
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
    public void mousePressed(RMouseEvent e) {
        super.mousePressed(e);
        stringValueWhenDragStarted = getValueAsString();
    }

    @Override
    public void mouseReleasedAnywhere(RMouseEvent e) {
        super.mouseReleasedAnywhere(e);
        if(stringValueWhenDragStarted != null && !stringValueWhenDragStarted.equals(getValueAsString())){
            onValueChangeEnd();
        }
        stringValueWhenDragStarted = null;
    }

    @Override
    public void mouseDragContinues(RMouseEvent e) {
        super.mouseDragContinues(e);
        mouseDeltaX = e.getPrevX() - e.getX();
        mouseDeltaY = e.getPrevY() - e.getY();
        if (isVertical) {
            LOGGER.debug("Mouse DeltaY for Slider {} [{} = {} - {}]", name, mouseDeltaY, e.getPrevY(), e.getY());
        } else {
            LOGGER.debug("Mouse DeltaX for Slider {} [{} = {} - {}]", name, mouseDeltaX, e.getPrevX(), e.getX());
        }
        updateValues();
        e.consume();
    }

    @Override
    public void updateValues(){
        if (isDragged || isMouseOver) {
           updateValueMouseInteraction();
        }
        updateNumpad();
    }
}
