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
package com.szadowsz.nds4j.app.nodes.bin.core.capability;

import com.szadowsz.nds4j.app.nodes.bin.core.CodeAreaSelection;
import com.szadowsz.nds4j.app.nodes.bin.core.SelectionRange;
import com.szadowsz.nds4j.app.nodes.bin.core.basic.SelectionChangedListener;

/**
 * Support for selection capability.
 *
 * @author ExBin Project (https://exbin.org)
 */
public interface SelectionCapable {

    /**
     * Returns current selection.
     *
     * @return selection range or empty selection range
     */
    
    SelectionRange getSelection();

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
     * Clears selection range - sets empty selection.
     */
    void clearSelection();

    /**
     * Returns true if there is active selection for clipboard handling.
     *
     * @return true if non-empty selection is active
     */
    boolean hasSelection();

    /**
     * Returns selection handler.
     *
     * @return code area selection handler
     */
    
    CodeAreaSelection getSelectionHandler();

    /**
     * Adds selection change listener.
     *
     * @param selectionChangedListener selection change listener
     */
    void addSelectionChangedListener(SelectionChangedListener selectionChangedListener);

    /**
     * Removes selection change listener.
     *
     * @param selectionChangedListener selection change listener
     */
    void removeSelectionChangedListener(SelectionChangedListener selectionChangedListener);
}
