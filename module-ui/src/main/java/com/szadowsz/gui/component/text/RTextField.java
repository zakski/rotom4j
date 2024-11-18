package com.szadowsz.gui.component.text;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.component.utils.RComponentScrollbar;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.config.text.RTextConstants;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.clip.RClipboard;
import com.szadowsz.gui.input.keys.RKeyChord;
import com.szadowsz.gui.input.keys.RKeyEvent;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.awt.PGraphicsJava2D;
import processing.core.PGraphics;
import processing.core.PVector;

import java.awt.font.TextLayout;
import java.util.LinkedList;

import static java.awt.event.KeyEvent.*;
import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.LEFT;

/**
 * Standard text field component.
 * <p>
 * It allows the user to enter and edit a single line of text.
 */
public class RTextField extends RTextEditable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RTextField.class);


    // <0 any text : 0 = integer : >0 float
    private int filter = -1;
    private boolean isValid = false;
    private boolean showAsValid = true;

    private float floatLow = -Float.MAX_VALUE;
    private float floatHigh = Float.MAX_VALUE;
    private float floatInvalid = 0;
    private float floatValue = 0;

    private int intLow = Integer.MIN_VALUE;
    private int intHigh = Integer.MAX_VALUE;
    private int intInvalid = 0;
    private int intValue = 0;


    private final int maxLength;
    RComponentScrollbar hsb; // horizontal scroll

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui          the gui for the window that the component is drawn under
     * @param path         the path in the component tree
     * @param parentFolder the parent component folder reference // TODO consider if needed
     * @param maxLength
     * @param sbPolicy
     */
    protected RTextField(RotomGui gui, String path, RFolder parentFolder, int maxLength, int sbPolicy) {
        super(gui, path, parentFolder, sbPolicy);
        this.maxLength = maxLength;
        hsb = new RComponentScrollbar(this, new PVector(0, getHeight()), size.copy(), 0, 0);
    }

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui          the gui for the window that the component is drawn under
     * @param path         the path in the component tree
     * @param parentFolder the parent component folder reference // TODO consider if needed
     */
    public RTextField(RotomGui gui, String path, RFolder parentFolder) {
        this(gui, path, parentFolder, Integer.MAX_VALUE, 0);
    }

    @Override
    protected boolean changeText() {
        if (!super.changeText())
            return false;
        startTLHI.copyFrom(endTLHI);
        return true;
    }

    /**
     * Cancels any selection. So the selection box will disappear.
     */
    protected void cancelSelection() {
        startTLHI.cancelInfo();
        endTLHI.cancelInfo();
    }

    protected void testValidity(String text) {
        switch (filter) {
            case RTextConstants.INTEGER:
                try {
                    intValue = Integer.parseInt(text);
                    isValid = intValue >= intLow && intValue <= intHigh;
                } catch (NumberFormatException nfe) {
                    isValid = false;
                }
                break;
            case RTextConstants.DECIMAL:
            case RTextConstants.EXPONENT:
                try {
                    floatValue = Float.parseFloat(text);
                    isValid = floatValue >= floatLow && floatValue <= floatHigh;
                } catch (NullPointerException | NumberFormatException ex) {
                    isValid = false;
                }
                break;
            default:
                isValid = true;
        }
        showAsValid = isValid || text.isEmpty() || text.trim().isEmpty();
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        fillBackground(pg);
        pg.image(buffer.draw(),0,0);
        // Draw caret if text display area
        if (isFocused && showCaret && endTLHI.tli != null) {
            float[] cinfo = endTLHI.tli.layout.getCaretInfo(endTLHI.thi);
            float x_left = /*-ptx +*/ cinfo[0];
            float y_top = /*-pty +*/ endTLHI.tli.yPosInPara;
            float y_bot = y_top - cinfo[3] + cinfo[5];
            if (x_left >= 0 && x_left <= size.x && y_top >= 0 && y_bot <= size.y) {
                pg.strokeWeight(1.5f);
                pg.stroke(isValid ? RThemeStore.getRGBA(RColorType.CURSOR) : RThemeStore.getRGBA(RColorType.CURSOR_NEGATIVE));
                pg.line(x_left, Math.max(0, y_top), x_left, Math.min(size.y, y_bot));
            }
        }
    }

    /**
     * If the field is displaying a valid integer within the specified range return
     * it. Otherwise the default int value.
     */
    public int getValueAsInt() {
        return isValid ? intValue : intInvalid;
    }

    /**
     * If the field is displaying a valid float within the specified range return
     * it. Otherwise the default float value.
     */
    public float getValueAsFloat() {
        return isValid ? floatValue : floatInvalid;
    }

    /**
     * Will cause the text field to show when an invalid float has been entered into
     * the field. <br>
     * An invalid float is any text that cannot be converted into a float or is
     * outside the range indicated by the first two parameters. <br>
     * If {@code getValueF()} is called then it will return the current valid float
     * or if invalid the value passed in the third parameter. <br>
     * Note: at least one of the parameters must be of type {@code float} <br>
     *
     * @param low           the lowest valid float
     * @param high          the highest valid float
     * @param default_value the value to be returned by {@code getValueF()}for
     *                      invalid user input
     */
    public void setNumeric(float low, float high, float default_value) {
        filter = RTextConstants.DECIMAL;
        floatLow = Math.min(low, high);
        floatHigh = Math.max(low, high);
        floatInvalid = default_value;
        testValidity(stext.getPlainText());
    }

    /**
     * Will cause the text field to show when an invalid integer has been entered
     * into the field. <br>
     * An invalid integer is any text that cannot be converted into an integer or is
     * outside the range indicated by the first two parameters. <br>
     * If {@code getValueI()} is called then it will return the current valid
     * integer or if invalid the value passed in the third parameter. <br>
     * Note: all the parameters must be of type {@code int} <br>
     *
     * @param low           the lowest valid integer
     * @param high          the highest valid integer
     * @param default_value the value to be returned by {@code getValueI()} for invalid user input
     */
    public void setNumeric(int low, int high, int default_value) {
        filter = RTextConstants.INTEGER;
        intLow = Math.min(low, high);
        intHigh = Math.max(low, high);
        intInvalid = default_value;
        testValidity(stext.getPlainText());
    }

    /**
     * Sets the filter to be used, valid parameter values are RTextConstants.INTEGER,
     * RTextConstants.DECIMAL or RTextConstants.EXPONENT and the accepted range will be based on range of
     * valid values for the Java data types {@code int} or {@code float}. <br>
     * if the parameter is null or missing then it cancels the filter and all text
     * values are considered valid.
     *
     * @param f the filter to apply (can be null or omitted)
     */
    public void setNumericType(int... f) {
        int n = f == null || f.length == 0 ? -1 : f[0];
        switch (n) {
            case RTextConstants.INTEGER:
                filter = RTextConstants.INTEGER;
                intLow = Integer.MIN_VALUE;
                intHigh = Integer.MAX_VALUE;
                intInvalid = 0;
                break;
            case RTextConstants.DECIMAL:
            case RTextConstants.EXPONENT:
                filter = RTextConstants.DECIMAL;
                floatLow = -Float.MAX_VALUE;
                floatHigh = Float.MAX_VALUE;
                floatInvalid = 0;
                break;
            default:
                filter = -1;
        }
        testValidity(stext.getPlainText());
    }

    /**
     * Set the styled text for this textfield after ensuring that all EOL characters
     * have been removed.
     *
     * @param ss the styled text to be displayed
     */
    public void setStyledText(RText ss) {
        cancelSelection();
        stext = ss.convertToSingleLineText();
        stext.getLines(buffer.getNative());
        if (stext.getNbrLines() > 0) {
            endTLHI.tli = stext.getLines(buffer.getNative()).getFirst();
            endTLHI.thi = endTLHI.tli.layout.getNextLeftHit(1);
            startTLHI.copyFrom(endTLHI);
            calculateCaretPos(endTLHI);
            // keepCursorInView = true;
        }
        // If needed update the horizontal scrollbar
        if (hsb != null) {
            if (stext.getMaxLineLength() < size.x) {
                hsb.setVisible(false);
            } else {
                hsb.setVisible(true);
            }
        }
        testValidity(stext.getPlainText());
        buffer.invalidateBuffer();
    }

    /**
     * Set the text to be displayed.
     *
     * @param text the text to be displayed
     */
    public void setText(String text) {
        if (text != null) {
            cancelSelection();
            stext.setText(text, Integer.MAX_VALUE);
            hsb.invalidateBuffer();
            buffer.invalidateBuffer();
            testValidity(stext.getPlainText());
        }
    }


    @Override
    public void keyChordPressedOver(RKeyEvent keyEvent, float mouseX, float mouseY) {
        LOGGER.info("text field {} Chord Check",name);
        if (!isVisible || !isEditEnabled || !isFocused || endTLHI == null) {
            return;
        }

        beforeKeyTypedEvent();
        boolean moveCursor = true;

        switch (keyEvent.getKeyCode()) {
            case VK_LEFT:
                moveCaretLeft(endTLHI);
                keyEvent.consume();
                break;
            case VK_RIGHT:
                moveCaretRight(endTLHI);
                keyEvent.consume();
                break;
            case VK_HOME:
                moveCaretStartOfLine(endTLHI);
                keyEvent.consume();
                buffer.invalidateBuffer();
                break;
            case VK_END:
                moveCaretEndOfLine(endTLHI);
                keyEvent.consume();
                break;
            case VK_A:
                if (keyEvent.hasChord(new RKeyChord(new int[]{VK_CONTROL, VK_A}))) {
                    moveCaretStartOfLine(startTLHI);
                    moveCaretEndOfLine(endTLHI);
                    // Make shift down so that the start caret position is not
                    // moved to match end caret position.
                    moveCursor = false;
                    keyEvent.consume();
                    buffer.invalidateBuffer();
                }
                break;
            case 'C':
                if (keyEvent.hasChord(new RKeyChord(new int[]{VK_CONTROL, VK_C}))) {
                    RClipboard.copy(getSelectedText());
                    keyEvent.consume();
                }
                break;
            case 'V':
                if (keyEvent.hasChord(new RKeyChord(new int[]{VK_CONTROL, VK_V}))) { // Ctrl + V paste selected text
                    String p = RClipboard.paste();
                    p.replaceAll("\n", "");
                    if (p.length() > 0) {
                        // delete selection and add
                        if (hasSelection())
                            stext.deleteCharacters(charPos, nbr);
                        stext.insertCharacters(p, charPos);
                        adjust = p.length();
                        textChanged = true;
                        keyEvent.consume();
                    }
                }
                break;
            default:
                break;
        }
        calculateCaretPos(endTLHI);
        if (keyEvent.isConsumed()) {
            if (moveCursor) {
                startTLHI.copyFrom(endTLHI);
            }
            buffer.invalidateBuffer();
            afterKeyTypedEvent();
        }
    }

    @Override
    public void keyTypedOver(RKeyEvent keyEvent, float mouseX, float mouseY) {
        LOGGER.info("text field {} Typed Check",name);
        beforeKeyTypedEvent();
        if (isDisplayable(keyEvent.getKey())) {
            if (hasSelection()) {
                stext.deleteCharacters(charPos, nbr);
            }
            LOGGER.info("text field {} Insert Char {} at {}",name,keyEvent.getKey(),charPos);
            stext.insertCharacters("" + keyEvent.getKey(), charPos);
            adjust = 1;
            textChanged = true;
        } else {
            switch (keyEvent.getKeyCode()) {
                case VK_BACK_SPACE:
                    if (hasSelection()) {
                        stext.deleteCharacters(charPos, nbr);
                        adjust = 0;
                        textChanged = true;
                    } else if (stext.deleteCharacters(charPos - 1, 1)) {
                        adjust = -1;
                        textChanged = true;
                    }
                case VK_DELETE:
                    if (hasSelection()) {
                        stext.deleteCharacters(charPos, nbr);
                        adjust = 0;
                        textChanged = true;
                    } else if (stext.deleteCharacters(charPos, 1)) {
                        adjust = 0;
                        textChanged = true;
                    }
                case VK_ENTER:
                    //case VK_RETURN:
                    setFocus(false);
            }
        }
        // If we have emptied the text then recreate a one character string (space)
        if (stext.length() == 0) {
            stext.insertCharacters(" ", 0);
            adjust++;
            textChanged = true;
        }
        afterKeyTypedEvent();
    }

    @Override
    public void mousePressed(RMouseEvent mouseEvent, float mouseY) {
        super.mousePressed(mouseEvent, mouseY);
        // If there is just a space then select it so it gets deleted on first key press
        if (stext.getPlainText().isEmpty()) {
            stext.setText(" ");
        }
        if (stext.getPlainText().equals(" ")) {
            LinkedList<RText.TextLayoutInfo> lines = stext.getLines(buffer.getNative());
            startTLHI = new RText.TextLayoutHitInfo(lines.getFirst(), null);
            startTLHI.thi = startTLHI.tli.layout.getNextLeftHit(1);
            endTLHI = new RText.TextLayoutHitInfo(lines.getLast(), null);
            int lastChar = endTLHI.tli.layout.getCharacterCount();
            endTLHI.thi = startTLHI.tli.layout.getNextRightHit(lastChar - 1);
        } else {
            endTLHI = stext.calculateFromXY(buffer.getNative(), pos.x, pos.y);
            startTLHI = new RText.TextLayoutHitInfo(endTLHI);
        }
        calculateCaretPos(endTLHI);
        buffer.invalidateBuffer();
    }

    @Override
    public void mouseDragged(RMouseEvent mouseEvent) {
        super.mouseDragged(mouseEvent);
        if (isFocused) {
            super.mouseDragged(mouseEvent);
            if (hsb.isDragging()) {
                hsb.mouseDragged(mouseEvent);
            } else {
                endTLHI = stext.calculateFromXY(buffer.getNative(), pos.x, pos.y);
                calculateCaretPos(endTLHI);
                buffer.invalidateBuffer();
            }
        }
    }

    @Override
    public void mouseReleasedOverComponent(RMouseEvent mouseEvent, float mouseY) {
        super.mouseReleasedOverComponent(mouseEvent, mouseY);
    }

    @Override
    void display(PGraphicsJava2D buffer) {
        LOGGER.debug("{} [{},{}]",name,getPosX(),getPosY());
        fillForeground(buffer);

        // Get Height Alignment
        float textY = alignHeight();

        buffer.pushMatrix();

        // Make sure font is set
        buffer.textFont(RFontStore.getSideFont());

        // Break Text into lines
        LinkedList<RText.TextLayoutInfo> lines = stext.getLines(buffer);

        buffer.textAlign(LEFT, CENTER); // consistent alignment just for Processing's sake

        LOGGER.debug("{} align with {}[{}]",name,textAlignV,getPosY()+textY);
        buffer.translate(0, textY); // translate to text start position
        for (RText.TextLayoutInfo lineInfo : lines) {
            TextLayout layout = lineInfo.layout;
            LOGGER.debug("{} ascent [{}]",name,layout.getAscent());
            buffer.translate(0, layout.getAscent()); // move the recommended distance above the baseline for singled spaced text.
            float textX = alignWidth(layout);
            strokeForeground(buffer);
            layout.draw(buffer.g2, textX, 0);
            LOGGER.debug("{} descent + leading [{}]",name,layout.getDescent() + layout.getLeading());
            buffer.translate(0, layout.getDescent() + layout.getLeading());
        }
        buffer.popMatrix();
    }
}
