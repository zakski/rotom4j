package com.szadowsz.gui.component.text;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.config.text.RTextConstants;
import com.szadowsz.gui.input.keys.RKeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.font.TextHitInfo;
import java.util.LinkedList;

public abstract class RTextEditable extends RTextBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(RTextEditable.class);

    // Padding for text controls
    protected static final int TPAD2	= 2;
    protected static final int TPAD4	= 4;
    protected static final int TPAD6	= 6;
    protected static final int TPAD8	= 8;

    protected RText promptText = null;

    // Caret position
    protected float caretX, caretY;
    protected boolean showCaret;

    // Used for identifying selection and cursor position
    protected RText.TextLayoutHitInfo startTLHI = new RText.TextLayoutHitInfo();
    protected RText.TextLayoutHitInfo endTLHI = new RText.TextLayoutHitInfo();

    // The width to break a line
    protected int wrapWidth = Integer.MAX_VALUE;

    // The scrollbars available
    protected final int scrollbarPolicy;
    protected boolean autoHide;

    // Stuff to manage text selections
    protected int endChar = -1;
    protected int startChar = -1;
    protected int charPos = endChar;
    protected int nbr;
    protected int adjust;
    protected boolean textChanged = false, selectionChanged = false;

    /* Is the component enabled to generate mouse and keyboard events */
    protected boolean isEditEnabled = true;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui          the gui for the window that the component is drawn under
     * @param path         the path in the component tree
     * @param parentFolder the parent component folder reference // TODO consider if needed
     */
    protected RTextEditable(RotomGui gui, String path, RFolder parentFolder, int sbPolicy) {
        super(gui, path, parentFolder);
        scrollbarPolicy = sbPolicy;
//        scrollbarPolicy = scrollbars;
//        autoHide = ((scrollbars & SCROLLBARS_AUTOHIDE) == SCROLLBARS_AUTOHIDE);
//        caretFlasher = new GTimer(theApplet, this, "flashCaret", 400);
//        caretFlasher.start();
//        opaque = true;
//        cursorOver = TEXT;

    }

    /**
     * Support UTF8 encoding
     *
     * @param ascii UTF8 code
     * @return true if the character can be displayed
     */
    protected boolean isDisplayable(int ascii) { // TODO
        return !(ascii < 32 || ascii == 127);
    }

    /**
     * Calculate the caret (text insertion point)
     *
     * @param tlhi
     */
    protected void calculateCaretPos(RText.TextLayoutHitInfo tlhi) {
        float temp[] = tlhi.tli.layout.getCaretInfo(tlhi.thi);
        caretX = temp[0];
        caretY = tlhi.tli.yPosInPara;
    }

    /**
     * Move caret left by one character.
     *
     * @param currPos the current position of the caret
     * @return true if caret moved, false otherwise
     */
    protected boolean moveCaretLeft(RText.TextLayoutHitInfo currPos) {
        TextHitInfo nthi = currPos.tli.layout.getNextLeftHit(currPos.thi);
        if (nthi == null) {
            return false;
        } else {
            // Move the caret to the left of current position
            currPos.thi = nthi;
        }
        return true;
    }

    /**
     * Move caret right by one character.
     *
     * @param currPos the current position of the caret
     * @return true if caret moved else false
     */
    protected boolean moveCaretRight(RText.TextLayoutHitInfo currPos) {
        TextHitInfo nthi = currPos.tli.layout.getNextRightHit(currPos.thi);
        if (nthi == null) {
            return false;
        } else {
            currPos.thi = nthi;
        }
        return true;
    }

    /**
     * Move caret to home position
     *
     * @param currPos the current position of the caret
     * @return true if caret moved else false
     */
    protected boolean moveCaretStartOfLine(RText.TextLayoutHitInfo currPos) {
        if (currPos.thi.getCharIndex() == 0)
            return false; // already at start of line
        currPos.thi = currPos.tli.layout.getNextLeftHit(1);
        return true;
    }

    /**
     * Move caret to the end of the line that has the current caret position
     *
     * @param currPos the current position of the caret
     * @return true if caret moved else false
     */
    protected boolean moveCaretEndOfLine(RText.TextLayoutHitInfo currPos) {
        if (currPos.thi.getCharIndex() == currPos.tli.nbrChars - 1)
            return false; // already at end of line
        currPos.thi = currPos.tli.layout.getNextRightHit(currPos.tli.nbrChars - 1);
        return true;
    }

    /**
     * Update fields when text has changed
     *
     * @return
     */
    protected boolean changeText() {
        stext.removeConsecutiveBlankLines();
        RText.TextLayoutInfo tli;
        TextHitInfo thi = null, thiRight = null;

        charPos += adjust;
        LOGGER.info("{} charPos adjusted to {}",name,charPos);
        // Force layouts to be updated
        String pt = stext.getPlainText();
        if (pt.contains("\n\n\n")) {
            LOGGER.warn("Double blank line");
        }
        stext.getLines(buffer.getNative());

        // Try to get text layout info for the current position
        tli = stext.getTLIforCharNo(charPos);
        if (tli == null) {
            // If unable to get a layout for pos then reset everything
            endTLHI = null;
            startTLHI = null;
            caretX = caretY = 0;
            return false;
        }
        // We have a text layout so we can do something
        // First find the position in line
        int posInLine = charPos - tli.startCharIndex;

        // Get some hit info so we can see what is happening
        try {
            thiRight = tli.layout.getNextRightHit(posInLine);
        } catch (Exception excp) {
            thiRight = null;
        }

        if (posInLine <= 0) { // At start of line
            thi = tli.layout.getNextLeftHit(thiRight);
        } else if (posInLine >= tli.nbrChars) { // End of line
            thi = tli.layout.getNextRightHit(tli.nbrChars - 1);
        } else { // Character in line;
            thi = tli.layout.getNextLeftHit(thiRight);
        }

        endTLHI.setInfo(tli, thi);
        // Cursor at end of paragraph graphic
        calculateCaretPos(endTLHI);

        buffer.invalidateBuffer();
        return true;
    }

    protected void beforeKeyTypedEvent() {
        textChanged = false;

        // Get selection details
        endChar = endTLHI.tli.startCharIndex + endTLHI.thi.getInsertionIndex();
        startChar = (startTLHI != null) ? startTLHI.tli.startCharIndex + startTLHI.thi.getInsertionIndex()
                : endChar;
        LOGGER.info("text field {} selection info [{},{}]",name,startChar,endChar);

        charPos = endChar;
        nbr = 0;
        adjust = 0;
        if (endChar != startChar) { // Have we some text selected?
            if (startChar < endChar) { // Forward selection
                charPos = startChar;
                nbr = endChar - charPos;
            } else if (startChar > endChar) { // Backward selection
                charPos = endChar;
                nbr = startChar - charPos;
            }
        }
    }

    protected void afterKeyTypedEvent() {
        if (textChanged) {
            changeText();
            LOGGER.info("invalidated text field {} buffer",name);
            buffer.invalidateBuffer();
            getParentFolder().getWindow().redrawBuffer();
            textChanged = false;
        }
    }

    /**
     * Get the prompt text used in this control.
     *
     * @return the prompt text without styling
     */
    public String getPromptText() {
        return promptText.getPlainText();
    }

    /**
     * Get the text that has been selected (highlighted) by the user. <br>
     *
     * @return the selected text without styling
     */
    public String getSelectedText() {
        if (!hasSelection()) {
            return "";
        }
        RText.TextLayoutHitInfo startSelTLHI;
        RText.TextLayoutHitInfo endSelTLHI;
        if (endTLHI.compareTo(startTLHI) == -1) {
            startSelTLHI = endTLHI;
            endSelTLHI = startTLHI;
        } else {
            startSelTLHI = startTLHI;
            endSelTLHI = endTLHI;
        }
        int ss = startSelTLHI.tli.startCharIndex + startSelTLHI.thi.getInsertionIndex();
        int ee = endSelTLHI.tli.startCharIndex + endSelTLHI.thi.getInsertionIndex();
        return stext.getPlainText().substring(ss, ee);
    }

    /**
     * Get the Line wrap width
     *
     * @return the wrapWidth
     */
    public int getWrapWidth() {
        return wrapWidth;
    }

    /**
     * Find out if some text is selected (highlighted)
     *
     * @return true if some text is selected else false
     */
    public boolean hasSelection() {
        return (startTLHI.tli != null && endTLHI.tli != null && startTLHI.compareTo(endTLHI) != 0);
    }

    /**
     * @return true if this control is keyboard enabled
     */
    public boolean isEditEnabled() {
        return isEditEnabled;
    }

    /**
     * Determines whether the text can be edited using the keyboard or mouse. It
     * still allows the text to be modified by the sketch code. <br>
     * If text editing is being disabled and the control has focus then it is forced
     * to give up that focus. <br>
     * This might be useful if you want to use a GTextArea control to display large
     * amounts of text that needs scrolling (so cannot use a GLabel) but must not
     * change e.g. a user instruction guide.
     *
     * @param enableTextEdit false to disable keyboard input
     */
    public void setEditEnabled(boolean enableTextEdit) {
        // If we are disabling this then make sure it does not have focus
        if (!enableTextEdit && isFocused) {
            setFocus(false);
        }
        isEditEnabled = enableTextEdit;
    }

    public void setJustify(boolean justify) {
        stext.setJustify(justify);
        buffer.invalidateBuffer();
    }

    /**
     * Set the prompt text for this control. When the text control is empty the
     * prompt text (italic) is displayed instead. .
     *
     * @param ptext prompt text
     */
    public void setPromptText(String ptext) {
        if (ptext == null || ptext.isEmpty())
            promptText = null;
        else {
            promptText = new RText(ptext, wrapWidth);
            promptText.addAttribute(RTextConstants.POSTURE, RTextConstants.POSTURE_OBLIQUE);
        }
        if (stext == null || stext.getPlainText().isEmpty()) {
            buffer.invalidateBuffer();
        }
    }

    /**
     * Allows the user to provide their own styled text for this component
     *
     * @param ss the styled string to display
     */
    public void setStyledText(RText ss) {
        if (ss != null) {
            stext = ss;
            stext.setWrapWidth((int) size.x - TPAD4);
            buffer.invalidateBuffer();
        }
    }

    /**
     * Set the Line wrap width
     *
     * @param wrapWidth the wrapWidth to set
     */
    public void setWrapWidth(int wrapWidth) {
        this.wrapWidth = wrapWidth;
    }

    /**
     * Clear all styles from the entire text.
     */
    public void clearStyles() {
        if (promptText != null) {
            promptText.clearAttributes();
            buffer.invalidateBuffer();
        }
        if (stext != null) {
            stext.clearAttributes();
            buffer.invalidateBuffer();
        }
    }

    @Override
    public void keyPressedFocused(RKeyEvent keyEvent) {
        if (!isVisible || !isEditEnabled || !isFocused) {
            return;
        } else {
            // Key Presses are only cared about for chords so we just eat the event
            keyEvent.consume();
            if (textChanged) {
                changeText();
            }
        }
    }

    @Override
    public void keyPressedOver(RKeyEvent keyEvent, float mouseX, float mouseY) {
        keyPressedFocused(keyEvent);
    }

//
//    /**
//     * Give the focus to this component but only after allowing the current
//     * component with focus to release it gracefully. <br>
//     * Always cancel the keyFocusIsWith irrespective of the component type. Fire
//     * focus events for the GTextField and GTextArea controls
//     */
//    protected void takeFocus() {
//        // If focus is not yet with this control fire a gets focus event
//        if (focusIsWith != this) {
//            // If the focus is with another control then tell
//            // that control to lose focus
//            if (focusIsWith != null)
//                focusIsWith.loseFocus(this);
//            fireEvent(this, GEvent.GETS_FOCUS);
//        }
//        focusIsWith = this;
//    }

    /**
     * Determines whether this component is to have focus or not. <br>
     */
    public void setFocus(boolean focus) {
        // Only do something if we don't have the focus
        if (!isFocused && focus) {
            isDragged = false;
            // Make sure we have some text
            if (stext == null || stext.length() == 0)
                stext.setText(" ", wrapWidth);
            LinkedList<RText.TextLayoutInfo> lines = stext.getLines(buffer.getNative());
            startTLHI = new RText.TextLayoutHitInfo(lines.getFirst(), null);
            startTLHI.thi = startTLHI.tli.layout.getNextLeftHit(1);

            endTLHI = new RText.TextLayoutHitInfo(lines.getLast(), null);
            int lastChar = endTLHI.tli.layout.getCharacterCount();
            endTLHI.thi = startTLHI.tli.layout.getNextRightHit(lastChar - 1);

            calculateCaretPos(endTLHI);
            buffer.invalidateBuffer();
        }
        super.setFocus(focus);
    }
//
//    /**
//     * Adds the text attribute to a range of characters on a particular line. If
//     * charEnd is past the EOL then the attribute will be applied to the
//     * end-of-line.
//     *
//     * @param attr      the text attribute to add
//     * @param value     value of the text attribute
//     * @param charStart the position of the first character to apply the attribute
//     * @param charEnd   the position after the last character to apply the attribute
//     */
//    public void addStyle(TextAttribute attr, Object value, int charStart, int charEnd) {
//        if (stext != null) {
//            stext.addAttribute(attr, value, charStart, charEnd);
//            buffer.invalidateBuffer();
//        }
//    }
//
//    /**
//     * Adds the text attribute to a range of characters on a particular line. If
//     * charEnd is past the EOL then the attribute will be applied to the
//     * end-of-line.
//     *
//     * @param attr  the text attribute to add
//     * @param value value of the text attribute
//     */
//    public void addStyle(TextAttribute attr, Object value) {
//        if (stext != null) {
//            stext.addAttribute(attr, value);
//            buffer.invalidateBuffer();
//        }
//    }
//
//    /**
//     * Clears all text attribute from a range of characters starting at position
//     * charStart and ending with the character preceding charEnd.
//     *
//     *
//     * @param charStart the position of the first character to apply the attribute
//     * @param charEnd   the position after the last character to apply the attribute
//     */
//    public void clearStyles(int charStart, int charEnd) {
//        if (stext != null) {
//            stext.clearAttributes(charStart, charEnd);
//            buffer.invalidateBuffer();
//        }
//    }
//
//    /**
//     * Set the font for this control.
//     *
//     * @param font the java.awt.Font to use
//     */
//    public void setFont(Font font) {
//        if (font != null && font != localFont && buffer != null) {
//            localFont = font;
//            ptx = pty = 0;
//            setScrollbarValues(ptx, pty);
//            buffer.invalidateBuffer();
//        }
//    }
//
//    // SELECTED / HIGHLIGHTED TEXT
//
//    /**
//     * If some text has been selected then set the style. If there is no selection
//     * then the text is unchanged.
//     *
//     * @param style set the style of some selected text
//     * @param value a value associated with this style
//     */
//    public void setSelectedTextStyle(TextAttribute style, Object value) {
//        if (!hasSelection())
//            return;
//        StyledString.TextLayoutHitInfo startSelTLHI;
//        StyledString.TextLayoutHitInfo endSelTLHI;
//        if (endTLHI.compareTo(startTLHI) == -1) {
//            startSelTLHI = endTLHI;
//            endSelTLHI = startTLHI;
//        } else {
//            startSelTLHI = startTLHI;
//            endSelTLHI = endTLHI;
//        }
//        int ss = startSelTLHI.tli.startCharIndex + startSelTLHI.thi.getInsertionIndex();
//        int ee = endSelTLHI.tli.startCharIndex + endSelTLHI.thi.getInsertionIndex();
//        stext.addAttribute(style, value, ss, ee);
//
//        // We have modified the text style so the end of the selection may have
//        // moved, so it needs to be recalculated. The start will be unaffected.
//        stext.getLines(buffer.g2);
//        endSelTLHI.tli = stext.getTLIforCharNo(ee);
//        int cn = ee - endSelTLHI.tli.startCharIndex;
//        if (cn == 0) // start of line
//            endSelTLHI.thi = endSelTLHI.tli.layout.getNextLeftHit(1);
//        else
//            endSelTLHI.thi = endSelTLHI.tli.layout.getNextRightHit(cn - 1);
//        buffer.invalidateBuffer();
//    }
//
//    /**
//     * Clear any styles applied to the selected text.
//     */
//    public void clearSelectionStyle() {
//        if (!hasSelection())
//            return;
//        StyledString.TextLayoutHitInfo startSelTLHI;
//        StyledString.TextLayoutHitInfo endSelTLHI;
//        if (endTLHI.compareTo(startTLHI) == -1) {
//            startSelTLHI = endTLHI;
//            endSelTLHI = startTLHI;
//        } else {
//            startSelTLHI = startTLHI;
//            endSelTLHI = endTLHI;
//        }
//        int ss = startSelTLHI.tli.startCharIndex + startSelTLHI.thi.getInsertionIndex();
//        int ee = endSelTLHI.tli.startCharIndex + endSelTLHI.thi.getInsertionIndex();
//        stext.clearAttributes(ss, ee);
//
//        // We have modified the text style so the end of the selection may have
//        // moved, so it needs to be recalculated. The start will be unaffected.
//        stext.getLines(buffer.g2);
//        endSelTLHI.tli = stext.getTLIforCharNo(ee);
//        int cn = ee - endSelTLHI.tli.startCharIndex;
//        if (cn == 0) // start of line
//            endSelTLHI.thi = endSelTLHI.tli.layout.getNextLeftHit(1);
//        else
//            endSelTLHI.thi = endSelTLHI.tli.layout.getNextRightHit(cn - 1);
//        buffer.invalidateBuffer();
//    }
//
//    /**
//     * Used internally to set the scrollbar values as the text changes.
//     *
//     * @param sx
//     * @param sy
//     */
//    void setScrollbarValues(float sx, float sy) {
//        if (vsb != null) {
//            float sTextHeight = stext.getTextAreaHeight();
//            if (sTextHeight < th)
//                vsb.setValue(0.0f, 1.0f);
//            else
//                vsb.setValue(sy / sTextHeight, th / sTextHeight);
//        }
//        // If needed update the horizontal scrollbar
//        if (hsb != null) {
//            float sTextWidth = stext.getMaxLineLength();
//            if (stext.getMaxLineLength() < tw)
//                hsb.setValue(0, 1);
//            else
//                hsb.setValue(sx / sTextWidth, tw / sTextWidth);
//        }
//    }
//
//    /**
//     * Sets the local colour scheme for this control
//     */
//    public void setLocalColorScheme(int cs) {
//        super.setLocalColorScheme(cs);
//        if (hsb != null)
//            hsb.setLocalColorScheme(localColorScheme);
//        if (vsb != null)
//            vsb.setLocalColorScheme(localColorScheme);
//    }
//
//    /*
//     * Do not call this directly. A timer calls this method as and when required.
//     */
//    public void flashCaret(GTimer timer) {
//        showCaret = !showCaret;
//    }
//
//    /*
//     * Do not call this method directly, G4P uses it to handle input from the
//     * horizontal scrollbar.
//     */
//    public void hsbEventHandler(GScrollbar scrollbar, GEvent event) {
//        keepCursorInView = false;
//        ptx = hsb.getValue() * (stext.getMaxLineLength() + 4);
//        buffer.invalidateBuffer();
//    }
//
//    /*
//     * Do not call this method directly, G4P uses it to handle input from the
//     * vertical scrollbar.
//     */
//    public void vsbEventHandler(GScrollbar scrollbar, GEvent event) {
//        keepCursorInView = false;
//        pty = vsb.getValue() * (stext.getTextAreaHeight() + 1.5f * stext.getMaxLineHeight());
//        buffer.invalidateBuffer();
//    }
//
//    /**
//     * Get the amount to scroll the text per frame when scrolling text to keep the
//     * insertion point on screen.
//     *
//     * @return the amount to scroll in pixels.
//     */
//    protected float getScrollAmount() {
//        float cps = ksm.calcCPS();
//        float f = PApplet.map(cps, 0.1f, 20, 1, cps * localFont.getSize());
//        f = PApplet.constrain(f, 4, cps * localFont.getSize());
//        return f;
//    }
//}
}
