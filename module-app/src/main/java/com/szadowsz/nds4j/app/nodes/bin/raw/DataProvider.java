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

import com.szadowsz.nds4j.file.bin.core.BinaryData;

import java.nio.charset.Charset;
import java.util.Optional;

/**
 * Data provider.
 *
 * @author ExBin Project (https://exbin.org)
 */
public interface DataProvider {

    /**
     * Returns current caret position.
     *
     * @return caret position
     */
    CodeAreaCaretPosition getActiveCaretPosition();
    
    /**
     * Returns currently active operation as set or enforced by current edit
     * mode.
     *
     * @return active edit operation
     */
    EditOperation getActiveOperation();

    /**
     * Returns currently active caret section.
     *
     * @return code area section
     */
    CodeAreaSection getActiveSection();

    /**
     * Returns current background paint mode.
     *
     * @return background paint mode
     */
    BackgroundPaintMode getBackgroundPaintMode();

    /**
     * Returns basic profile for colors.
     *
     * @return colors profile
     */
    CodeAreaColorsProfile getBasicColors();

    /**
     * Returns currently used charset.
     *
     * @return charset
     */
    Charset getCharset();

    /**
     * Returns mode for default clipboard actions like cut, copy, paste, delete.
     *
     * @return clipboard handling mode
     */
    ClipboardHandlingMode getClipboardHandlingMode();

    /**
     * Returns handler for caret.
     *
     * @return caret handler
     */
    CodeAreaCaret getCodeAreaCaret();

    /**
     * Returns current code characters case.
     *
     * @return code characters case
     */
    CodeCharactersCase getCodeCharactersCase();

    /**
     * Returns current caret code offset.
     *
     * @return code offset
     */
    int getCodeOffset();
    
    /**
     * Returns current code type.
     *
     * @return code type
     */
    CodeType getCodeType();

    /**
     * Returns data or null.
     *
     * @return binary data
     */
    BinaryData getContentData();

    /**
     * Returns current caret data position.
     *
     * @return data position
     */
    long getDataPosition();

    /**
     * Returns size of data or 0 if no data is present.
     *
     * @return size of data
     */
    long getDataSize();

    /**
     * Returns edit mode.
     *
     * @return edit mode
     */
    EditMode getEditMode();

    /**
     * Returns currently enforced edit operation.
     *
     * @return edit operation
     */
    EditOperation getEditOperation();

    /**
     * Returns horizotal scrollbar visibility mode.
     *
     * @return scrollbar visibility mode
     */
    ScrollBarVisibility getHorizontalScrollBarVisibility();

    /**
     * Returns horizontal scrolling unit.
     *
     * @return horizontal scrolling unit
     */
    HorizontalScrollUnit getHorizontalScrollUnit();

    /**
     * Returns maximum number of bytes per row.
     *
     * @return bytes per row
     */
    int getMaxBytesPerRow();

    /**
     * Returns maximum length of position section of the code area.
     *
     * @return maximum length
     */
    int getMaxRowPositionLength();

    /**
     * Returns minimum length of position section of the code area.
     *
     * @return minimum length
     */
    int getMinRowPositionLength();
    
    /**
     * Returns cursor shape type for given position.
     *
     * @param positionX x-coordinate
     * @param positionY y-coordinate
     * @return cursor type from java.awt.Cursor
     */
    int getMouseCursorShape(int positionX, int positionY);

    /**
     * Returns row wrapping mode.
     *
     * @return row wrapping mode
     */
    RowWrappingMode getRowWrapping();
    /**
     * Returns current scrolling position.
     *
     * @return scroll position
     */
    CodeAreaScrollPosition getScrollPosition();

    /**
     * Returns current selection.
     *
     * @return selection range or empty selection range
     */
    SelectionRange getSelection();
   
    /**
     * Returns selection handler.
     *
     * @return code area selection handler
     */
    CodeAreaSelection getSelectionHandler();
    
    /**
     * Returns vertical scrollbar visibility mode.
     *
     * @return scrollbar visibility mode
     */
    ScrollBarVisibility getVerticalScrollBarVisibility();

    /**
     * Returns vertical scrolling unit.
     *
     * @return vertical scrolling unit
     */
    VerticalScrollUnit getVerticalScrollUnit();

    /**
     * Returns current view mode.
     *
     * @return view mode
     */
    CodeAreaViewMode getViewMode();

    /**
     * Returns size of the byte group.
     *
     * @return size of the byte group
     */
    int getWrappingBytesGroupSize();
  
    /**
     * Returns true if there is active selection for clipboard handling.
     *
     * @return true if non-empty selection is active
     */
    boolean hasSelection();

    /**
     * Returns if cursor should be visible in other sections.
     *
     * @return true if cursor should be mirrored
     */
    boolean isShowMirrorCursor();

    /**
     * Sets current caret position to given position.
     *
     * @param caretPosition caret position
     */
    void setActiveCaretPosition(CodeAreaCaretPosition caretPosition);

    /**
     * Sets current caret position to given data position.
     *
     * @param dataPosition data position
     */
    void setActiveCaretPosition(long dataPosition);

    /**
     * Sets current caret position to given data position and offset.
     *
     * @param dataPosition data position
     * @param codeOffset code offset
     */
    void setActiveCaretPosition(long dataPosition, int codeOffset);

    /**
     * Sets current background paint mode.
     *
     * @param borderPaintMode background paint mode
     */
    void setBackgroundPaintMode(BackgroundPaintMode borderPaintMode);


    /**
     * Sets basic profile for colors.
     *
     * @param colorsProfile colors profile
     */
    void setBasicColors(CodeAreaColorsProfile colorsProfile);
  
    /**
     * Sets charset to use for characters decoding.
     *
     * @param charset charset
     */
    void setCharset(Charset charset);

    /**
     * Sets handle mode for default clipboard actions like cut, copy, paste,
     * delete.
     *
     * @param handlingMode clipboard handling mode
     */
    void setClipboardHandlingMode(ClipboardHandlingMode handlingMode);

    /**
     * Sets current code characters case.
     *
     * @param codeCharactersCase code characters case
     */
    void setCodeCharactersCase(CodeCharactersCase codeCharactersCase);

    /**
     * Sets current code type.
     *
     * @param codeType code type
     */
    void setCodeType(CodeType codeType);

    /**
     * Sets edit mode.
     *
     * @param editMode edit mode
     */
    void setEditMode(EditMode editMode);


    /**
     * Sets currently enforced edit operation.
     *
     * @param editOperation edit operation
     */
    void setEditOperation(EditOperation editOperation);


    /**
     * Sets horizotal scrollbar visibility mode.
     *
     * @param horizontalScrollBarVisibility scrollbar visibility mode
     */
    void setHorizontalScrollBarVisibility(ScrollBarVisibility horizontalScrollBarVisibility);

    /**
     * Sets horizontal scrolling unit.
     *
     * @param horizontalScrollUnit horizontal scrolling unit
     */
    void setHorizontalScrollUnit(HorizontalScrollUnit horizontalScrollUnit);

    /**
     * Sets maximum number of bytes per row.
     *
     * @param maxBytesPerRow bytes per row
     */
    void setMaxBytesPerRow(int maxBytesPerRow);

    /**
     * Sets maximum length of position section of the code area.
     *
     * @param maxRowPositionLength maximum length
     */
    void setMaxRowPositionLength(int maxRowPositionLength);

    /**
     * Sets minimum length of position section of the code area.
     *
     * @param minRowPositionLength minimum length
     */
    void setMinRowPositionLength(int minRowPositionLength);

    /**
     * Sets row wrapping mode.
     *
     * @param rowWrapping row wrapping mode
     */
    void setRowWrapping(RowWrappingMode rowWrapping);

    /**
     * Sets current scrolling position.
     *
     * @param scrollPosition scrolling position
     */
    void setScrollPosition(CodeAreaScrollPosition scrollPosition);

    /**
     * Sets current selection.
     *
     * @param selection selection range or empty selection range
     */
    void setSelection(SelectionRange selection);

    /**
     * Sets current selection range from start to end including the start and
     * not including the end position.
     *
     * @param start selection start position
     * @param end selection end position without actual end position itself
     */
    void setSelection(long start, long end);

    /**
     * Sets if cursor should be visible in other sections.
     *
     * @param showMirrorCursor true if cursor should be mirrored
     */
    void setShowMirrorCursor(boolean showMirrorCursor);

    /**
     * Sets vertical scrollbar visibility mode.
     *
     * @param verticalScrollBarVisibility scrollbar visibility mode
     */
    void setVerticalScrollBarVisibility(ScrollBarVisibility verticalScrollBarVisibility);


    /**
     * Sets vertical scrolling unit.
     *
     * @param verticalScrollUnit vertical scrolling unit
     */
    void setVerticalScrollUnit(VerticalScrollUnit verticalScrollUnit);

    /**
     * Sets current view mode.
     *
     * @param viewMode view mode
     */
    void setViewMode(CodeAreaViewMode viewMode);

    /**
     * Sets size of the byte group.
     *
     * @param groupSize size of the byte group
     */
    void setWrappingBytesGroupSize(int groupSize);

    /**
     * Adds caret movement listener.
     *
     * @param caretMovedListener listener
     */
    void addCaretMovedListener(CodeAreaCaretListener caretMovedListener);

    /**
     * Adds edit mode change listener.
     *
     * @param editModeChangedListener edit mode change listener
     */
    void addEditModeChangedListener(EditModeChangedListener editModeChangedListener);

    /**
     * Adds scrolling listener.
     *
     * @param scrollingListener scrolling listener
     */
    void addScrollingListener(ScrollingListener scrollingListener);

    /**
     * Adds selection change listener.
     *
     * @param selectionChangedListener selection change listener
     */
    void addSelectionChangedListener(SelectionChangedListener selectionChangedListener);
   
    /**
     * Removes caret movement listener.
     *
     * @param caretMovedListener listener
     */
    void removeCaretMovedListener(CodeAreaCaretListener caretMovedListener);

    /**
     * Removes edit mode change listener.
     *
     * @param editModeChangedListener edit mode change listener
     */
    void removeEditModeChangedListener(EditModeChangedListener editModeChangedListener);

    /**
     * Removes scrolling listener.
     *
     * @param scrollingListener scrolling listener
     */
    void removeScrollingListener(ScrollingListener scrollingListener);

    /**
     * Removes selection change listener.
     *
     * @param selectionChangedListener selection change listener
     */
    void removeSelectionChangedListener(SelectionChangedListener selectionChangedListener);
    
    /**
     * Clears selection range - sets empty selection.
     */
    void clearSelection();

    /**
     * Computes position for movement action.
     *
     * @param position source position
     * @param direction movement direction
     * @return target position
     */
    CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition position, MovementDirection direction);

    /**
     * Computes scrolling position for given direction.
     *
     * @param startPosition start position
     * @param direction scrolling direction
     * @return scrolling position
     */
    CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition startPosition, ScrollingDirection direction);

    /**
     * Scrolls scrolling area as centered as possible for current cursor
     * position.
     */
    void centerOnCursor();

    /**
     * Scrolls scrolling area as centered as possible for given caret position.
     *
     * @param caretPosition caret position
     */
    void centerOnPosition(CodeAreaCaretPosition caretPosition);

    /**
     * Computes closest caret position for given relative component position.
     *
     * @param positionX x-coordinate
     * @param positionY y-coordinate
     * @param overflowMode overflow mode
     * @return mouse position
     */
    CodeAreaCaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, CaretOverlapMode overflowMode);

    /**
     * Reveals scrolling area for current cursor position.
     */
    void revealCursor();

    /**
     * Reveals scrolling area for given caret position.
     *
     * @param caretPosition caret position
     */
    void revealPosition(CodeAreaCaretPosition caretPosition);
}
