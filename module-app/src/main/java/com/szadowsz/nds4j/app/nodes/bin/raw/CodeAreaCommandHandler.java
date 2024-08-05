/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.szadowsz.nds4j.app.nodes.bin.raw;

import com.szadowsz.nds4j.app.nodes.bin.raw.swing.CodeAreaSwingUtils;
import com.szadowsz.nds4j.file.bin.core.BinaryData;
import com.szadowsz.nds4j.file.bin.core.ByteArrayData;
import com.szadowsz.nds4j.file.bin.core.ByteArrayEditableData;
import com.szadowsz.nds4j.file.bin.core.EditableBinaryData;
import com.szadowsz.nds4j.file.bin.core.paged.PagedData;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default binary editor command handler.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaCommandHandler  {

    public static final String BINED_CLIPBOARD_MIME = "application/x-bined";
    public static final String BINED_CLIPBOARD_MIME_FULL = BINED_CLIPBOARD_MIME + "; class=" + BinaryData.class.getCanonicalName();
    public static final int LAST_CONTROL_CODE = 31;
    private static final char DELETE_CHAR = (char) 0x7f;

    private final int metaMask = CodeAreaSwingUtils.getMetaMaskDown();

    private final CodeAreaCore codeArea;
    private EnterKeyHandlingMode enterKeyHandlingMode = EnterKeyHandlingMode.PLATFORM_SPECIFIC;
    private TabKeyHandlingMode tabKeyHandlingMode = TabKeyHandlingMode.PLATFORM_SPECIFIC;
    private final boolean codeTypeSupported;
    private final boolean viewModeSupported;

    private Clipboard clipboard;
    private boolean canPaste = false;
    private CodeAreaSwingUtils.ClipboardData currentClipboardData = null;
    private DataFlavor binedDataFlavor;
    private DataFlavor binaryDataFlavor;

    public CodeAreaCommandHandler(CodeAreaControl codeArea) {
        this.codeArea = codeArea;
        codeTypeSupported = true;
        viewModeSupported = true;

        clipboard = CodeAreaSwingUtils.getClipboard();
        try {
            clipboard.addFlavorListener((FlavorEvent e) -> {
                updateCanPaste();
            });
            binedDataFlavor = new DataFlavor(BinaryData.class, BINED_CLIPBOARD_MIME_FULL);
            try {
                binaryDataFlavor = new DataFlavor(CodeAreaUtils.MIME_CLIPBOARD_BINARY);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(CodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            updateCanPaste();
        } catch (IllegalStateException ex) {
            canPaste = false;
        } catch (java.awt.HeadlessException ex) {
            Logger.getLogger(CodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static CodeAreaCommandHandlerFactory createDefaultCodeAreaCommandHandlerFactory() {
        return CodeAreaCommandHandler::new;
    }

    /**
     * Notifies command handler about end of sequence of append-able commands.
     */
    public void undoSequenceBreak() {
        // Do nothing
    }

    /**
     * Keyboard key was pressed.
     *
     * @param keyEvent key event
     */
    public void keyPressed(KeyEvent keyEvent) {
        if (!codeArea.isEnabled()) {
            return;
        }

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_LEFT: {
                move(isSelectingMode(keyEvent), MovementDirection.LEFT);
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_RIGHT: {
                move(isSelectingMode(keyEvent), MovementDirection.RIGHT);
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_UP: {
                move(isSelectingMode(keyEvent), MovementDirection.UP);
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_DOWN: {
                move(isSelectingMode(keyEvent), MovementDirection.DOWN);
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_HOME: {
                if ((keyEvent.getModifiersEx() & metaMask) > 0) {
                    move(isSelectingMode(keyEvent), MovementDirection.DOC_START);
                } else {
                    move(isSelectingMode(keyEvent), MovementDirection.ROW_START);
                }
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_END: {
                if ((keyEvent.getModifiersEx() & metaMask) > 0) {
                    move(isSelectingMode(keyEvent), MovementDirection.DOC_END);
                } else {
                    move(isSelectingMode(keyEvent), MovementDirection.ROW_END);
                }
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_PAGE_UP: {
                scroll(ScrollingDirection.PAGE_UP);
                move(isSelectingMode(keyEvent), MovementDirection.PAGE_UP);
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_PAGE_DOWN: {
                scroll(ScrollingDirection.PAGE_DOWN);
                move(isSelectingMode(keyEvent), MovementDirection.PAGE_DOWN);
                undoSequenceBreak();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_INSERT: {
                EditMode editMode = codeArea.getEditMode();
                if (editMode == EditMode.EXPANDING || editMode == EditMode.CAPPED) {
                    EditOperation editOperation = codeArea.getEditOperation();
                    switch (editOperation) {
                        case INSERT: {
                            codeArea.setEditOperation(EditOperation.OVERWRITE);
                            keyEvent.consume();
                            break;
                        }
                        case OVERWRITE: {
                            codeArea.setEditOperation(EditOperation.INSERT);
                            keyEvent.consume();
                            break;
                        }
                    }
                }
                break;
            }
            case KeyEvent.VK_TAB: {
                tabPressed(isSelectingMode(keyEvent));
                if (tabKeyHandlingMode != TabKeyHandlingMode.IGNORE) {
                    keyEvent.consume();
                }
                break;
            }
            case KeyEvent.VK_ENTER: {
                enterPressed();
                if (enterKeyHandlingMode != EnterKeyHandlingMode.IGNORE) {
                    keyEvent.consume();
                }
                break;
            }
            case KeyEvent.VK_DELETE: {
                EditMode editMode = codeArea.getEditMode();
                if (editMode == EditMode.EXPANDING) {
                    deletePressed();
                    keyEvent.consume();
                }
                break;
            }
            case KeyEvent.VK_BACK_SPACE: {
                EditMode editMode = codeArea.getEditMode();
                if (editMode == EditMode.EXPANDING) {
                    backSpacePressed();
                    keyEvent.consume();
                }
                break;
            }
            default: {
                if (codeArea.getClipboardHandlingMode() == ClipboardHandlingMode.PROCESS) {
                    if ((keyEvent.getModifiersEx() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.VK_C) {
                        copy();
                        keyEvent.consume();
                        break;
                    } else if ((keyEvent.getModifiersEx() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.VK_X) {
                        cut();
                        keyEvent.consume();
                        break;
                    } else if ((keyEvent.getModifiersEx() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.VK_V) {
                        paste();
                        keyEvent.consume();
                        break;
                    } else if ((keyEvent.getModifiersEx() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.VK_A) {
                        codeArea.selectAll();
                        keyEvent.consume();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Keyboard key was typed.
     *
     * @param keyEvent key event
     */
    public void keyTyped(KeyEvent keyEvent) {
        char keyValue = keyEvent.getKeyChar();
        // TODO Add support for high unicode codes
        if (keyValue == KeyEvent.CHAR_UNDEFINED) {
            return;
        }
        if (!checkEditAllowed()) {
            return;
        }

        CodeAreaSection section = codeArea.getActiveSection();
        if (section == CodeAreaSection.CODE_MATRIX) {
            int modifiersEx = keyEvent.getModifiersEx();
            if (modifiersEx == 0 || modifiersEx == KeyEvent.SHIFT_DOWN_MASK) {
                pressedCharAsCode(keyValue);
            }
        } else {
            char keyChar = keyValue;
            if (keyChar > LAST_CONTROL_CODE && keyValue != DELETE_CHAR) {
                pressedCharInPreview(keyChar);
            }
        }
    }

    private void pressedCharAsCode(char keyChar) {
        CodeAreaCaretPosition caretPosition = codeArea.getActiveCaretPosition();
        long dataPosition = caretPosition.getDataPosition();
        int codeOffset = caretPosition.getCodeOffset();
        CodeType codeType = getCodeType();
        boolean validKey = CodeAreaUtils.isValidCodeKeyValue(keyChar, codeOffset, codeType);
        if (validKey) {
            EditMode editMode = codeArea.getEditMode();
            if (codeArea.hasSelection() && editMode != EditMode.INPLACE) {
                deleteSelection();
                undoSequenceBreak();
            }

            int value;
            if (keyChar >= '0' && keyChar <= '9') {
                value = keyChar - '0';
            } else {
                value = Character.toLowerCase(keyChar) - 'a' + 10;
            }

            BinaryData data = codeArea.getContentData();
            EditOperation editOperation = codeArea.getActiveOperation();
            if (editMode == EditMode.EXPANDING && editOperation == EditOperation.INSERT) {
                if (codeOffset > 0) {
                    byte byteRest = data.getByte(dataPosition);
                    switch (codeType) {
                        case BINARY: {
                            byteRest = (byte) (byteRest & (0xff >> codeOffset));
                            break;
                        }
                        case DECIMAL: {
                            byteRest = (byte) (byteRest % (codeOffset == 1 ? 100 : 10));
                            break;
                        }
                        case OCTAL: {
                            byteRest = (byte) (byteRest % (codeOffset == 1 ? 64 : 8));
                            break;
                        }
                        case HEXADECIMAL: {
                            byteRest = (byte) (byteRest & 0xf);
                            break;
                        }
                        default:
                            throw CodeAreaUtils.getInvalidTypeException(codeType);
                    }
                    if (byteRest > 0) {
                        ((EditableBinaryData) data).insert(dataPosition + 1, 1);
                        ((EditableBinaryData) data).setByte(dataPosition, (byte) (data.getByte(dataPosition) - byteRest));
                        ((EditableBinaryData) data).setByte(dataPosition + 1, byteRest);
                    }
                } else {
                    ((EditableBinaryData) data).insert(dataPosition, 1);
                }
                setCodeValue(value);
            } else {
                if (editMode == EditMode.EXPANDING && editOperation == EditOperation.OVERWRITE && dataPosition == codeArea.getDataSize()) {
                    ((EditableBinaryData) data).insert(dataPosition, 1);
                }
                if (editMode != EditMode.INPLACE || dataPosition < codeArea.getDataSize()) {
                    setCodeValue(value);
                }
            }
            codeArea.notifyDataChanged();
            move(SelectingMode.NONE, MovementDirection.RIGHT);
            revealCursor();
        }
    }

    private void pressedCharInPreview(char keyChar) {
        if (isValidChar(keyChar)) {
            EditMode editMode = codeArea.getEditMode();
            CodeAreaCaretPosition caretPosition = codeArea.getActiveCaretPosition();

            long dataPosition = caretPosition.getDataPosition();
            byte[] bytes = charToBytes(keyChar);
            if (editMode == EditMode.INPLACE) {
                int length = bytes.length;
                if (dataPosition + length > codeArea.getDataSize()) {
                    return;
                }
            }
            if (codeArea.hasSelection() && editMode != EditMode.INPLACE) {
                undoSequenceBreak();
                deleteSelection();
            }

            BinaryData data = codeArea.getContentData();
            EditOperation editOperation = codeArea.getActiveOperation();
            if ((editMode == EditMode.EXPANDING && editOperation == EditOperation.OVERWRITE) || editMode == EditMode.INPLACE) {
                if (dataPosition < codeArea.getDataSize()) {
                    int length = bytes.length;
                    if (dataPosition + length > codeArea.getDataSize()) {
                        length = (int) (codeArea.getDataSize() - dataPosition);
                    }
                    ((EditableBinaryData) data).remove(dataPosition, length);
                }
            }
            ((EditableBinaryData) data).insert(dataPosition, bytes);
            codeArea.notifyDataChanged();
            codeArea.getCodeAreaCaret().setCaretPosition(dataPosition + bytes.length - 1);
            move(SelectingMode.NONE, MovementDirection.RIGHT);
            revealCursor();
        }
    }

    private void setCodeValue(int value) {
        CodeAreaCaretPosition caretPosition = codeArea.getActiveCaretPosition();
        long dataPosition = caretPosition.getDataPosition();
        int codeOffset = caretPosition.getCodeOffset();
        BinaryData data = codeArea.getContentData();
        CodeType codeType = getCodeType();
        byte byteValue = data.getByte(dataPosition);
        byte outputValue = CodeAreaUtils.setCodeValue(byteValue, value, codeOffset, codeType);
        ((EditableBinaryData) data).setByte(dataPosition, outputValue);
    }

    /**
     * Enter key was pressed.
     */
    public void enterPressed() {
        if (!checkEditAllowed()) {
            return;
        }

        if (codeArea.getActiveSection() == CodeAreaSection.TEXT_PREVIEW) {
            String sequence = enterKeyHandlingMode.getSequence();
            if (!sequence.isEmpty()) {
                pressedCharInPreview(sequence.charAt(0));
                if (sequence.length() == 2) {
                    pressedCharInPreview(sequence.charAt(1));
                }
            }
        }
    }

    /**
     * Tab key was pressed.
     */
    public void tabPressed() {
        tabPressed(SelectingMode.NONE);
    }
        
    public void tabPressed(SelectingMode selectingMode) {
        if (!checkEditAllowed()) {
            return;
        }

        if (tabKeyHandlingMode == TabKeyHandlingMode.PLATFORM_SPECIFIC || tabKeyHandlingMode == TabKeyHandlingMode.CYCLE_TO_NEXT_SECTION || tabKeyHandlingMode == TabKeyHandlingMode.CYCLE_TO_PREVIOUS_SECTION) {
            if (viewModeSupported && codeArea.getViewMode() == CodeAreaViewMode.DUAL) {
                move(selectingMode, MovementDirection.SWITCH_SECTION);
                undoSequenceBreak();
                revealCursor();
            }
        } else if (codeArea.getActiveSection() == CodeAreaSection.TEXT_PREVIEW) {
            String sequence = tabKeyHandlingMode == TabKeyHandlingMode.INSERT_TAB ? "\t" : "  ";
            pressedCharInPreview(sequence.charAt(0));
            if (sequence.length() == 2) {
                pressedCharInPreview(sequence.charAt(1));
            }
        }
    }

    /**
     * Backspace key was pressed.
     */
    public void backSpacePressed() {
        if (!checkEditAllowed()) {
            return;
        }
        BinaryData data = codeArea.getContentData();

        if (codeArea.hasSelection()) {
            deleteSelection();
            codeArea.notifyDataChanged();
        } else {
            CodeAreaCaret caret =  codeArea.getCodeAreaCaret();
            long dataPosition = codeArea.getDataPosition();
            if (dataPosition > 0 && dataPosition <= codeArea.getDataSize()) {
                caret.setCodeOffset(0);
                move(SelectingMode.NONE, MovementDirection.LEFT);
                caret.setCodeOffset(0);
                ((EditableBinaryData) data).remove(dataPosition - 1, 1);
                codeArea.notifyDataChanged();
                codeArea.setActiveCaretPosition(caret.getCaretPosition());
                revealCursor();
                clearSelection();
            }
        }
    }

    /**
     * Delete key was pressed.
     */
    public void deletePressed() {
        if (!checkEditAllowed()) {
            return;
        }

        if (codeArea.hasSelection()) {
            deleteSelection();
            codeArea.notifyDataChanged();
            revealCursor();
        } else {
            BinaryData data = codeArea.getContentData();
            CodeAreaCaret caret =   codeArea.getCodeAreaCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition < codeArea.getDataSize()) {
                ((EditableBinaryData) data).remove(dataPosition, 1);
                codeArea.notifyDataChanged();
                if (caret.getCodeOffset() > 0) {
                    caret.setCodeOffset(0);
                }
                codeArea.setActiveCaretPosition(caret.getCaretPosition());
                clearSelection();
                revealCursor();
            }
        }
    }

    private void deleteSelection() {
        BinaryData data = codeArea.getContentData();
        if (!(data instanceof EditableBinaryData)) {
            throw new IllegalStateException("Data is not editable");
        }

        SelectionRange selection = codeArea.getSelection();
        if (selection.isEmpty()) {
            return;
        }

        EditMode editMode = codeArea.getEditMode();
        long first = selection.getFirst();
        long last = selection.getLast();
        long length = last - first + 1;
        if (editMode == EditMode.INPLACE) {
            ((EditableBinaryData) data).fillData(first, length);
        } else {
            ((EditableBinaryData) data).remove(first, length);
        }
        codeArea.setActiveCaretPosition(first);
        clearSelection();
        revealCursor();
    }

    /**
     * Deletes selection.
     */
    public void delete() {
        if (!checkEditAllowed()) {
            return;
        }

        deleteSelection();
        codeArea.notifyDataChanged();
    }

    /**
     * Copies selection to clipboard.
     */
    public void copy() {
        SelectionRange selection = codeArea.getSelection();
        if (!selection.isEmpty()) {
            BinaryData data = codeArea.getContentData();

            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = data.copy(first, last - first + 1);

            Charset charset = codeArea.getCharset();
            CodeAreaSwingUtils.BinaryDataClipboardData binaryData = new CodeAreaSwingUtils.BinaryDataClipboardData(copy, binedDataFlavor, binaryDataFlavor, charset);
            setClipboardContent(binaryData);
        }
    }

    /**
     * Copies selection to clipboard as code string.
     */
    public void copyAsCode() {
        SelectionRange selection = codeArea.getSelection();
        if (!selection.isEmpty()) {
            BinaryData data = codeArea.getContentData();

            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = data.copy(first, last - first + 1);

            CodeType codeType = codeArea.getCodeType();
            CodeCharactersCase charactersCase = codeArea.getCodeCharactersCase();
            CodeAreaSwingUtils.CodeDataClipboardData binaryData = new CodeAreaSwingUtils.CodeDataClipboardData(copy, binaryDataFlavor, codeType, charactersCase);
            setClipboardContent(binaryData);
        }
    }

    private void setClipboardContent(CodeAreaSwingUtils.ClipboardData content) {
        clearClipboardData();
        try {
            currentClipboardData = content;
            clipboard.setContents(content, content);
        } catch (IllegalStateException ex) {
            // Clipboard not available - ignore and clear
            clearClipboardData();
        }
    }

    private void clearClipboardData() {
        if (currentClipboardData != null) {
            currentClipboardData.dispose();
            currentClipboardData = null;
        }
    }

    /**
     * Cuts selection to clipboard.
     */
    public void cut() {
        if (!checkEditAllowed()) {
            return;
        }

        EditMode editMode = codeArea.getEditMode();
        SelectionRange selection = codeArea.getSelection();
        if (!selection.isEmpty()) {
            copy();
            if (editMode == EditMode.EXPANDING) {
                deleteSelection();
                codeArea.notifyDataChanged();
            }
        }
    }

    /**
     * Pastes content of clipboard to cursor area.
     */
    public void paste() {
        if (!checkEditAllowed()) {
            return;
        }

        try {
            if (clipboard.isDataFlavorAvailable(binedDataFlavor)) {
                try {
                    Object clipboardData = clipboard.getData(binedDataFlavor);
                    if (clipboardData instanceof BinaryData) {
                        pasteBinaryData((BinaryData) clipboardData);
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    Logger.getLogger(CodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (clipboard.isDataFlavorAvailable(binaryDataFlavor)) {
                try {
                    Object clipboardData = clipboard.getData(binaryDataFlavor);
                    if (clipboardData instanceof InputStream) {
                        PagedData pastedData = new PagedData();
                        pastedData.insert(0, (InputStream) clipboardData, -1);
                        pasteBinaryData((BinaryData) pastedData);
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    Logger.getLogger(CodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                try {
                    Object clipboardData = clipboard.getData(DataFlavor.stringFlavor);
                    if (clipboardData instanceof String) {
                        CodeAreaCaret caret = codeArea.getCodeAreaCaret();
                        long dataPosition = caret.getDataPosition();

                        byte[] bytes = ((String) clipboardData).getBytes(Charset.forName(CodeAreaSwingUtils.DEFAULT_ENCODING));
                        BinaryData pastedData = new ByteArrayData(bytes);
                        pasteBinaryData(pastedData);
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    Logger.getLogger(CodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IllegalStateException ex) {
            // Clipboard not available - ignore
        }
    }

    private void pasteBinaryData(BinaryData pastedData) {
        BinaryData data = codeArea.getContentData();
        EditMode editMode = codeArea.getEditMode();
        EditOperation editOperation = codeArea.getActiveOperation();

        if (codeArea.hasSelection()) {
            deleteSelection();
            codeArea.notifyDataChanged();
        }

        CodeAreaCaret caret = codeArea.getCodeAreaCaret();
        long dataPosition = caret.getDataPosition();

        long dataSize = pastedData.getDataSize();
        long toRemove = dataSize;
        if (editMode == EditMode.INPLACE) {
            if (dataPosition + toRemove > codeArea.getDataSize()) {
                toRemove = codeArea.getDataSize() - dataPosition;
            }
            ((EditableBinaryData) data).replace(dataPosition, pastedData, 0, toRemove);
        } else {
            if (editMode == EditMode.EXPANDING && editOperation == EditOperation.OVERWRITE) {
                if (dataPosition + toRemove > codeArea.getDataSize()) {
                    toRemove = codeArea.getDataSize() - dataPosition;
                }
                ((EditableBinaryData) data).remove(dataPosition, toRemove);
            }

            ((EditableBinaryData) data).insert(dataPosition, pastedData);
            caret.setCaretPosition(caret.getDataPosition() + dataSize);
            updateSelection(SelectingMode.NONE, caret.getCaretPosition());
        }

        caret.setCodeOffset(0);
        codeArea.setActiveCaretPosition(caret.getCaretPosition());
        undoSequenceBreak();
        codeArea.notifyDataChanged();
        revealCursor();
        clearSelection();
    }

    /**
     * Pastes content of clipboard to cursor area analyzing string code.
     */
    public void pasteFromCode() {
        if (!checkEditAllowed()) {
            return;
        }

        BinaryData data = codeArea.getContentData();
        EditMode editMode = codeArea.getEditMode();
        EditOperation editOperation = codeArea.getActiveOperation();
        try {
            if (!clipboard.isDataFlavorAvailable(binaryDataFlavor) && !clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                return;
            }
        } catch (IllegalStateException ex) {
            return;
        }

        try {
            if (clipboard.isDataFlavorAvailable(binaryDataFlavor)) {
                paste();
            } else if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                if (codeArea.hasSelection()) {
                    deleteSelection();
                    codeArea.notifyDataChanged();
                }

                Object insertedData;
                try {
                    insertedData = clipboard.getData(DataFlavor.stringFlavor);
                    if (insertedData instanceof String) {
                        CodeAreaCaret caret = codeArea.getCodeAreaCaret();
                        long dataPosition = caret.getDataPosition();

                        CodeType codeType = getCodeType();
                        ByteArrayEditableData pastedData = new ByteArrayEditableData();
                        CodeAreaUtils.insertHexStringIntoData((String) insertedData, pastedData, codeType);

                        long length = pastedData.getDataSize();
                        long toRemove = length;
                        if ((editMode == EditMode.EXPANDING && editOperation == EditOperation.OVERWRITE) || editMode == EditMode.INPLACE) {
                            if (dataPosition + toRemove > codeArea.getDataSize()) {
                                toRemove = codeArea.getDataSize() - dataPosition;
                            }
                            ((EditableBinaryData) data).remove(dataPosition, toRemove);
                        }
                        if (editMode == EditMode.INPLACE && length > toRemove) {
                            ((EditableBinaryData) data).insert(caret.getDataPosition(), pastedData, 0, toRemove);
                            caret.setCaretPosition(caret.getDataPosition() + toRemove);
                            updateSelection(SelectingMode.NONE, caret.getCaretPosition());
                        } else {
                            ((EditableBinaryData) data).insert(caret.getDataPosition(), pastedData);
                            caret.setCaretPosition(caret.getDataPosition() + length);
                            updateSelection(SelectingMode.NONE, caret.getCaretPosition());
                        }

                        caret.setCodeOffset(0);
                        codeArea.setActiveCaretPosition(caret.getCaretPosition());
                        undoSequenceBreak();
                        codeArea.notifyDataChanged();
                        revealCursor();
                        clearSelection();
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    Logger.getLogger(CodeAreaCommandHandler.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IllegalStateException ex) {
            // Clipboard not available - ignore
        }
    }

    /**
     * Returns true if paste action is possible.
     *
     * @return true if paste is possible
     */
    public boolean canPaste() {
        return canPaste;
    }

    public EnterKeyHandlingMode getEnterKeyHandlingMode() {
        return enterKeyHandlingMode;
    }

    public void setEnterKeyHandlingMode(EnterKeyHandlingMode enterKeyHandlingMode) {
        this.enterKeyHandlingMode = enterKeyHandlingMode;
    }

    public TabKeyHandlingMode getTabKeyHandlingMode() {
        return tabKeyHandlingMode;
    }

    public void setTabKeyHandlingMode(TabKeyHandlingMode tabKeyHandlingMode) {
        this.tabKeyHandlingMode = tabKeyHandlingMode;
    }

    /**
     * Expands selection to all data.
     */
    public void selectAll() {
        long dataSize = codeArea.getDataSize();
        if (dataSize > 0) {
            codeArea.setSelection(0, dataSize);
        }
    }

    /**
     * Clears data selection.
     */
    public void clearSelection() {
        long dataPosition = codeArea.getActiveCaretPosition().getDataPosition();
        codeArea.setSelection(dataPosition, dataPosition);
    }

    public void updateSelection(SelectingMode selectingMode, CodeAreaCaretPosition caretPosition) {
        long dataPosition = codeArea.getDataPosition();
        SelectionRange selection = codeArea.getSelection();
        if (selectingMode == SelectingMode.SELECTING) {
            codeArea.setSelection(selection.getStart(), dataPosition);
        } else {
            codeArea.setSelection(dataPosition, dataPosition);
        }
    }

    private void updateCanPaste() {
        canPaste = CodeAreaSwingUtils.canPaste(clipboard, binaryDataFlavor);
    }

    /**
     * Moves caret with mouse event.
     *
     * @param positionX relative position X
     * @param positionY relative position Y
     * @param selecting selection selecting
     */
    public void moveCaret(int positionX, int positionY, SelectingMode selecting) {
        CodeAreaCaretPosition caretPosition = codeArea.mousePositionToClosestCaretPosition(positionX, positionY, CaretOverlapMode.PARTIAL_OVERLAP);
        codeArea.setActiveCaretPosition(caretPosition);
        updateSelection(selecting, caretPosition);

        undoSequenceBreak();
        codeArea.repaint();
    }

    public void move(SelectingMode selectingMode, MovementDirection direction) {
        CodeAreaCaretPosition caretPosition = codeArea.getActiveCaretPosition();
        CodeAreaCaretPosition movePosition = codeArea.computeMovePosition(caretPosition, direction);
        if (!caretPosition.equals(movePosition)) {
            codeArea.setActiveCaretPosition(movePosition);
            updateSelection(selectingMode, movePosition);
        }
    }

    public void scroll(ScrollingDirection direction) {
        CodeAreaScrollPosition sourcePosition = codeArea.getScrollPosition();
        CodeAreaScrollPosition scrollPosition = codeArea.computeScrolling(sourcePosition, direction);
        if (!sourcePosition.equals(scrollPosition)) {
            codeArea.setScrollPosition(scrollPosition);
            codeArea.resetPainter();
        }
    }

    /**
     * Performs scrolling.
     *
     * @param scrollSize number of scroll units (positive or negative)
     * @param orientation scrollbar orientation
     */
    public void wheelScroll(int scrollSize, ScrollbarOrientation orientation) {
        switch (orientation) {
            case HORIZONTAL: {
                if (scrollSize > 0) {
                    scroll(ScrollingDirection.LEFT);
                } else if (scrollSize < 0) {
                    scroll(ScrollingDirection.RIGHT);
                }

                break;
            }
            case VERTICAL: {
                if (scrollSize > 0) {
                    scroll(ScrollingDirection.DOWN);
                } else if (scrollSize < 0) {
                    scroll(ScrollingDirection.UP);
                }
                break;
            }
        }
    }

    public boolean isValidChar(char value) {
        return codeArea.getCharset().canEncode();
    }

    public byte[] charToBytes(char value) {
        ByteBuffer buffer = codeArea.getCharset().encode(Character.toString(value));
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes, 0, bytes.length);
        return bytes;
    }

    private CodeType getCodeType() {
        if (codeTypeSupported) {
            return codeArea.getCodeType();
        }

        return CodeType.HEXADECIMAL;
    }

    private void revealCursor() {
        codeArea.revealCursor();
        codeArea.repaint();
    }

    /**
     * Checks whether edit is allowed.
     *
     * @return true if allowed
     */
    public boolean checkEditAllowed() {
        return codeArea.isEditable();
    }

    private static SelectingMode isSelectingMode(KeyEvent keyEvent) {
        return (keyEvent.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) > 0 ? SelectingMode.SELECTING : SelectingMode.NONE;
    }

    public enum ScrollbarOrientation {
        HORIZONTAL, VERTICAL
    }

    public enum SelectingMode {
        NONE, SELECTING
    }

    public interface CodeAreaCommandHandlerFactory {
        CodeAreaCommandHandler createCommandHandler(CodeAreaCore codeArea);
    }
}
