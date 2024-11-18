/*
  Part of the G4P library for Processing
  	http://www.lagers.org.uk/g4p/index.html
	http://sourceforge.net/projects/g4p/files/?source=navbar

  Copyright (c) 2012 Peter Lager

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
 */
package com.szadowsz.gui.component.text;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.input.clip.RClipboard;
import com.szadowsz.gui.input.keys.RKeyChord;
import com.szadowsz.gui.input.keys.RKeyEvent;
import processing.core.PApplet;

import java.awt.font.TextHitInfo;
import java.util.LinkedList;

import static java.awt.event.KeyEvent.*;

/**
 * Text area component.
 * <p>
 * It allows the user to enter and edit multiple lines of text.
 */
public class RTextArea extends RTextEditable {
    // TODO Component Stub : WIP

    private static final char EOL = '\n';

    protected boolean newline = false, backspace = false;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui          the gui for the window that the component is drawn under
     * @param path         the path in the component tree
     * @param parentFolder the parent component folder reference // TODO consider if needed
     */
    public RTextArea(RotomGui gui, String path, RFolder parentFolder) {
        super(gui, path, parentFolder, 0);
    }

    @Override
    protected boolean moveCaretStartOfLine(RText.TextLayoutHitInfo currPos) {
        if (currPos.thi.getCharIndex() == 0)
            return false; // already at start of line
        currPos.thi = currPos.tli.layout.getNextLeftHit(1);
        return true;
    }


    @Override
    protected boolean moveCaretEndOfLine(RText.TextLayoutHitInfo currPos) {
        if (currPos.thi.getCharIndex() == currPos.tli.nbrChars - 1) {
            return false; // already at end of line
        }
        currPos.thi = currPos.tli.layout.getNextRightHit(currPos.tli.nbrChars - 1);
        return true;
    }

    protected boolean moveCaretStartOfText(RText.TextLayoutHitInfo currPos) {
        if (currPos.tli.lineNo == 0 && currPos.thi.getCharIndex() == 0) {
            return false; // already at start of text
        }
        currPos.tli = stext.getTLIforLineNo(0);
        currPos.thi = currPos.tli.layout.getNextLeftHit(1);
        return true;
    }

    protected boolean moveCaretEndOfText(RText.TextLayoutHitInfo currPos) {
        if (currPos.tli.lineNo == stext.getNbrLines() - 1 && currPos.thi.getCharIndex() == currPos.tli.nbrChars - 1) {
            return false; // already at end of text
        }
        currPos.tli = stext.getTLIforLineNo(stext.getNbrLines() - 1);
        currPos.thi = currPos.tli.layout.getNextRightHit(currPos.tli.nbrChars - 1);
        return true;
    }

    protected boolean moveCaretUp(RText.TextLayoutHitInfo currPos) {
        if (currPos.tli.lineNo == 0) {
            return false;
        }
        RText.TextLayoutInfo ntli = stext.getTLIforLineNo(currPos.tli.lineNo - 1);
        TextHitInfo nthi = ntli.layout.hitTestChar(caretX, 0);
        currPos.tli = ntli;
        currPos.thi = nthi;
        return true;
    }

    protected boolean moveCaretDown(RText.TextLayoutHitInfo currPos) {
        if (currPos.tli.lineNo == stext.getNbrLines() - 1)
            return false;
        RText.TextLayoutInfo ntli = stext.getTLIforLineNo(currPos.tli.lineNo + 1);
        TextHitInfo nthi = ntli.layout.hitTestChar(caretX, 0);
        currPos.tli = ntli;
        currPos.thi = nthi;
        return true;
    }

    /**
     * Move caret left by one character. If necessary move to the end of the line
     * above
     *
     * @return true if caret was moved else false
     */
    @Override
    protected boolean moveCaretLeft(RText.TextLayoutHitInfo currPos) {
        RText.TextLayoutInfo ntli;
        TextHitInfo nthi = currPos.tli.layout.getNextLeftHit(currPos.thi);
        if (nthi == null) {
            // Move the caret to the end of the previous line
            if (currPos.tli.lineNo == 0)
                // Can't goto previous line because this is the first line
                return false;
            else {
                // Move to end of previous line
                ntli = stext.getTLIforLineNo(currPos.tli.lineNo - 1);
                nthi = ntli.layout.getNextRightHit(ntli.nbrChars - 1);
                currPos.tli = ntli;
                currPos.thi = nthi;
            }
        } else {
            // Move the caret to the left of current position
            currPos.thi = nthi;
        }
        return true;
    }

    /**
     * Move caret right by one character. If necessary move to the start of the next
     * line
     *
     * @return true if caret was moved else false
     */
    @Override
    protected boolean moveCaretRight(RText.TextLayoutHitInfo currPos) {
        RText.TextLayoutInfo ntli;
        TextHitInfo nthi = currPos.tli.layout.getNextRightHit(currPos.thi);
        if (nthi == null) {
            // Move the caret to the start of the next line the previous line
            if (currPos.tli.lineNo >= stext.getNbrLines() - 1)
                // Can't goto next line because this is the last line
                return false;
            else {
                // Move to start of next line
                ntli = stext.getTLIforLineNo(currPos.tli.lineNo + 1);
                nthi = ntli.layout.getNextLeftHit(1);
                currPos.tli = ntli;
                currPos.thi = nthi;
            }
        } else {
            // Move the caret to the right of current position
            currPos.thi = nthi;
        }
        return true;
    }

    /**
     * Move the insertion point (caret) to the specified line and character. If the
     * position is invalid then the caret is not moved. The text will be scrolled so
     * that the caret position is visible.
     *
     * @param lineNo the line number (starts at 0)
     * @param charNo the character position on the line (starts at 0)
     */
    protected void moveCaretTo(int lineNo, int charNo) {
        try {
            RText.TextLayoutHitInfo tlhi = stext.getTLHIforCharPosition(lineNo, charNo);
            if (tlhi != null) {
                startTLHI.copyFrom(tlhi);
                endTLHI.copyFrom(tlhi);
                calculateCaretPos(tlhi);
                showCaret = true;
            }
        } catch (Exception ignored) {
        }
    }


    /**
     * Get the current caret position. <br>
     * <p>
     * If the parameter is a 2 element int array then it will be populated with the
     * line number [0] and character no [1] of the caret's current position. <br>
     * <p>
     * The method will always return a 2 element array with the current caret
     * position { line no, char no } <br>
     * <p>
     * If the current caret position is undefined then it will return the array {
     * -1, -1 }
     *
     * @param cpos array to be populated with caret position
     * @return a two element int array holding the caret position.
     */
    public int[] getCaretPos(int[] cpos) {
        if (cpos == null || cpos.length != 2)
            cpos = new int[2];
        if (endTLHI == null || endTLHI.tli == null || endTLHI.thi == null) {
            cpos[0] = cpos[1] = -1;
        } else {
            cpos[0] = endTLHI.tli.lineNo;
            cpos[1] = endTLHI.thi.getCharIndex();
        }
        return cpos;
    }

    /**
     * Get the current caret position. <br>
     * <p>
     * The method will always return a 2 element array with the current caret
     * position { line no, char no } <br>
     * <p>
     * If the current caret position is undefined then it will return the array {
     * -1, -1 }
     *
     * @return a two element int array holding the caret position.
     */
    public int[] getCaretPos() {
        return getCaretPos(null);
    }

    /**
     * Get the text on a particular line in the text area. <br>
     * The line does not need to be visible and the line numbers always start at 0.
     * <br>
     * The result is not dependent on what is visible at any particular time but on
     * the overall position in text area control. <br>
     * If the line number is invalid then an empty string is returned. <br>
     * Trailing EOL characters are removed.
     *
     * @param lineNo the text area line number we want
     * @return the plain text in a display line
     */
    public String getText(int lineNo) {
        // Get the latest lines of text
        LinkedList<RText.TextLayoutInfo> lines = stext.getLines(buffer.getNative());
        if (lineNo < 0 || lineNo >= lines.size())
            return "";
        RText.TextLayoutInfo tli = lines.get(lineNo);
        String s = stext.getPlainText(tli.startCharIndex, tli.startCharIndex + tli.nbrChars);
        // Strip off trailing EOL
        int p = s.length() - 1;
        while (p > 0 && s.charAt(p) == EOL)
            p--;
        return (p == s.length() - 1) ? s : s.substring(0, p + 1);
    }

    /**
     * Get the length of text on a particular line in the text area. <br>
     * The line does not need to be visible and the line numbers always start at 0.
     * <br>
     * The result is not dependent on what is visible at any particular time but on
     * the overall position in text area control. <br>
     * If ignoreEOL is true then EOL characters are not included in the count.
     *
     * @param lineNo    the text area line number we want
     * @param ignoreEOL if true do not include trailing end=of-line characters
     * @return the length of the line, or &lt;0) if the line number is invalid
     */
    public int getTextLength(int lineNo, boolean ignoreEOL) {
        // Get the latest lines of text
        LinkedList<RText.TextLayoutInfo> lines = stext.getLines(buffer.getNative());
        if (lineNo < 0 || lineNo >= lines.size())
            return -1;
        RText.TextLayoutInfo tli = lines.get(lineNo);
        // String s = stext.getPlainText(tli.startCharIndex, tli.startCharIndex +
        // tli.nbrChars);
        String s = stext.getPlainText();
        int len = tli.nbrChars;
        if (ignoreEOL) {
            // Strip off trailing EOL
            int p = tli.startCharIndex + tli.nbrChars - 1;
            while (p > tli.startCharIndex && s.charAt(p) == EOL) {
                p--;
                len--;
            }
        }
        return len;
    }


    /**
     * Set the text to display and adjust any scrollbars
     *
     * @param text      text to display
     * @param wrapWidth the wrap width
     */
    public void setText(String text, int wrapWidth) {
        if (text != null) {
            // Change empty string to a 'space' character
            text = !text.isEmpty() ? text : " ";
            stext.setText(text, wrapWidth);
            setStyledText(stext);
            buffer.invalidateBuffer();
        }
    }

    /**
     * Set the text to display and adjust any scrollbars
     *
     * @param lines an array of Strings representing the text to display
     */
    public void setText(String[] lines) {
        if (lines != null && lines.length > 0) {
            setText(String.join("\n",lines), wrapWidth);
        }
    }

    /**
     * Set the text to display and adjust any scrollbars
     *
     * @param lines     an array of Strings representing the text to display
     * @param wrapWidth the wrap width
     */
    public void setText(String[] lines, int wrapWidth) {
        if (lines != null && lines.length > 0) {
            setText(PApplet.join(lines, "\n"), wrapWidth);
        }
    }

    /**
     * Get the text as a String array. (splitting on line breaks).
     *
     * @return the associated plain text as a String array split on line breaks
     */
    public String[] getLines() {
        return stext.getPlainTextAsArray();
    }


    @Override
    public void setStyledText(RText st) {
        stext = st;
        if (stext.getWrapWidth() == Integer.MAX_VALUE) {
            stext.setWrapWidth(wrapWidth);
        } else {
            wrapWidth = stext.getWrapWidth();
        }
        stext.getLines(buffer.getNative());
        if (stext.getNbrLines() > 0) {
            endTLHI.tli = stext.getLines(buffer.getNative()).getFirst();
            endTLHI.thi = endTLHI.tli.layout.getNextLeftHit(1);
            startTLHI.copyFrom(endTLHI);
            calculateCaretPos(endTLHI);
        }
        buffer.invalidateBuffer();
    }

    /**
     * Add text to the end of the current text. This is useful for a logging' type
     * activity. <br>
     * <p>
     * No events will be generated and the caret will be moved to the end of any
     * appended text. <br>
     *
     * @param text the text to append
     * @return true if some characters were added
     */
    public boolean appendText(String text) {
        if (text == null || text.equals("") || stext.insertCharacters(text, stext.length(), true, false) == 0)
            return false;
        LinkedList<RText.TextLayoutInfo> lines = stext.getLines(buffer.getNative());
        endTLHI.tli = lines.getLast();
        endTLHI.thi = endTLHI.tli.layout.getNextRightHit(endTLHI.tli.nbrChars - 1);
        startTLHI.copyFrom(endTLHI);
        calculateCaretPos(endTLHI);
        buffer.invalidateBuffer();
        return true;
    }

    /**
     * Insert text at the display position specified. <br>
     * <p>
     * The area line number starts at 0 and includes any lines scrolled off the top.
     * So if three lines have been scrolled off the top the first visible line is
     * number 3. <br>
     * <p>
     * No events will be generated and the caret will be moved to the end of any
     * inserted text. <br>
     *
     * @param text         the text to insert
     * @param lineNo       the area line number
     * @param charNo       the character position to insert text in display line
     * @param startWithEOL if true,inserted text will start on newline
     * @param endWithEOL   if true, text after inserted text will start on new line
     * @return true if some characters were inserted
     */
    public boolean insertText(String text, int lineNo, int charNo, boolean startWithEOL, boolean endWithEOL) {
        if (text != null && !text.isEmpty()) {
            int pos = stext.getPos(lineNo, charNo);
            int change = stext.insertCharacters(text, lineNo, charNo, startWithEOL, endWithEOL);
            // displayCaretPos("Caret starts at ");
            if (change != 0) {
                // Move caret to end of insert if possible
                pos += change;
                RText.TextLayoutHitInfo tlhi = stext.getTLHIforCharPosition(pos);
                if (tlhi != null) {
                    endTLHI.copyFrom(tlhi);
                    moveCaretLeft(endTLHI);
                    startTLHI.copyFrom(endTLHI);
                    // displayCaretPos("Caret ends at ");
                    calculateCaretPos(tlhi);
                    showCaret = true;
                }
                buffer.invalidateBuffer();
                return true;
            }
        }
        return false;
    }

    /**
     * Insert text at the display position specified. <br>
     * <p>
     * The area line number starts at 0 and includes any lines scrolled off the top.
     * So if three lines have been scrolled off the top the first visible line is
     * number 3. <br>
     * <p>
     * No events will be generated and the caret will be moved to the end of any
     * inserted text. <br>
     *
     * @param text   the text to insert
     * @param lineNo the area line number
     * @param charNo the character position to insert text in display line
     * @return true if some characters were inserted
     */
    public boolean insertText(String text, int lineNo, int charNo) {
        return insertText(text, lineNo, charNo, false, false);
    }

    /**
     * Insert text at the current caret position. If the current caret position is
     * undefined the text will be inserted at the beginning of the text. <br>
     * <p>
     * No events will be generated and the caret will be moved to the end of any
     * inserted text. <br>
     *
     * @param text         the text to insert
     * @param startWithEOL if true,inserted text will start on newline
     * @param endWithEOL   if true, text after inserted text will start on new line
     * @return true if some characters were inserted
     */
    public boolean insertText(String text, boolean startWithEOL, boolean endWithEOL) {
        int lineNo = 0, charNo = 0;
        if (endTLHI.tli != null && endTLHI.thi != null) {
            lineNo = endTLHI.tli.lineNo;
            charNo = endTLHI.thi.getCharIndex();
        }
        return insertText(text, lineNo, charNo, startWithEOL, endWithEOL);
    }

    /**
     * Insert text at the current caret position. If the current caret position is
     * undefined the text will be inserted at the beginning of the text. <br>
     * <p>
     * No events will be generated and the caret will be moved to the end of any
     * inserted text. <br>
     *
     * @param text the text to insert
     * @return true if some characters were inserted
     */
    public boolean insertText(String text) {
        return insertText(text, false, false);
    }

    @Override
    public void keyChordPressedOver(RKeyEvent keyEvent, float mouseX, float mouseY) {
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
            case VK_UP:
                moveCaretUp(endTLHI);
                keyEvent.consume();
                break;
            case VK_DOWN:
                moveCaretDown(endTLHI);
                keyEvent.consume();
                break;
            case VK_HOME:
                if (keyEvent.hasChord(new RKeyChord(new int[]{VK_CONTROL, VK_HOME}))) { // move to start of text
                    moveCaretStartOfText(endTLHI);
                } else {// Move to start of line
                    moveCaretStartOfLine(endTLHI);
                }
                keyEvent.consume();
                break;
            case VK_END:
                if (keyEvent.hasChord(new RKeyChord(new int[]{VK_CONTROL, VK_END}))) { // move to end of text
                    moveCaretEndOfText(endTLHI);
                }else {// Move to end of line
                    moveCaretEndOfLine(endTLHI);
                }
                keyEvent.consume();
                break;
            case VK_A:
                if (keyEvent.hasChord(new RKeyChord(new int[]{VK_CONTROL, VK_A}))) {
                    moveCaretStartOfText(startTLHI);
                    moveCaretEndOfText(endTLHI);
                    // Make shift down so that the start caret position is not
                    // moved to match end caret position.
                    moveCursor = false;
                    keyEvent.consume();
                }
                break;
            case VK_C:
                if (keyEvent.hasChord(new RKeyChord(new int[]{VK_CONTROL, VK_C}))) {
                    RClipboard.copy(getSelectedText());
                    keyEvent.consume();
                }
                break;
            case VK_V:
                if (keyEvent.hasChord(new RKeyChord(new int[]{VK_CONTROL, VK_V}))) {
                    String p = RClipboard.paste();
                    if (!p.isEmpty()) {
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
            default: // NOOP
        }

        if (keyEvent.isConsumed()) {
            calculateCaretPos(endTLHI);
            // ****************************************************************
            // If we have moved to the end of a paragraph marker
            if (caretX > stext.getWrapWidth()) {
                switch (keyEvent.getKeyCode()) {
                    case VK_LEFT:
                    case VK_UP:
                    case VK_DOWN:
                    case VK_END:
                        moveCaretLeft(endTLHI);
                        break;
                    case VK_RIGHT:
                        if (!moveCaretRight(endTLHI)){
                            moveCaretLeft(endTLHI);
                        }
                }
                // Calculate new caret position
                // calculateCaretPos(startTLHI);
                calculateCaretPos(endTLHI);
            }
            // ****************************************************************

            calculateCaretPos(endTLHI);

            if (moveCursor) {
                startTLHI.copyFrom(endTLHI);
            }
            afterKeyTypedEvent();
            buffer.invalidateBuffer();
        }
    }

    public void keyTypedOver(RKeyEvent keyEvent, float mouseX, float mouseY) {
        beforeKeyTypedEvent();
        newline = false;
        backspace = false;
        if (isDisplayable(keyEvent.getKey())) {
            if (hasSelection()) {
                stext.deleteCharacters(charPos, nbr);
            }
            stext.insertCharacters("" + keyEvent.getKey(), charPos);
            adjust = 1;
            textChanged = true;
            keyEvent.consume();
        } else {
            switch (keyEvent.getKeyCode()) {
                case VK_BACK_SPACE:
                    if (hasSelection()) {
                        stext.deleteCharacters(charPos, nbr);
                        adjust = 0;
                        textChanged = true;
                        keyEvent.consume();
                    } else if (stext.deleteCharacters(charPos - 1, 1)) {
                        adjust = -1;
                        textChanged = true;
                        backspace = true;
                        keyEvent.consume();
                    }
                case VK_DELETE:
                    if (hasSelection()) {
                        stext.deleteCharacters(charPos, nbr);
                        adjust = 0;
                        textChanged = true;
                        keyEvent.consume();
                    } else if (stext.deleteCharacters(charPos, 1)) {
                        adjust = 0;
                        textChanged = true;
                        keyEvent.consume();
                    }
                case VK_ENTER:
                    if (stext.insertEOL(charPos)) {
                        adjust = 1;
                        textChanged = true;
                        newline = true;
                        keyEvent.consume();
                    }
                default:
            }
        }
        // If we have emptied the text then recreate a one character string (space)
        if (stext.length() == 0) {
            stext.insertCharacters(" ", 0);
            adjust++;
            textChanged = true;
        }
        if (keyEvent.isConsumed()) {
            startTLHI.copyFrom(endTLHI);
            buffer.invalidateBuffer();
            afterKeyTypedEvent();
        }
    }
}
