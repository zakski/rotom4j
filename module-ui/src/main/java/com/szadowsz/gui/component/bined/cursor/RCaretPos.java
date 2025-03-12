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
package com.szadowsz.gui.component.bined.cursor;


import com.szadowsz.gui.component.bined.settings.RCodeAreaSection;

import java.util.Objects;
import java.util.Optional;

/**
 * Specifies caret position as combination of data position, section and code
 * offset of code representation.
 *
 * @author ExBin Project (https://exbin.org)
 */
public final class RCaretPos {

    private long dataPosition = 0;
    private int codeOffset = 0;
    private RCodeAreaSection section = null;

    public RCaretPos() {
    }

    public RCaretPos(long dataPosition, int codeOffset, RCodeAreaSection section) {
        this.dataPosition = dataPosition;
        this.codeOffset = codeOffset;
        this.section = section;
    }

    /**
     * Returns character offset position in the code on current position.
     *
     * @return code offset
     */
    public int getCodeOffset() {
        return codeOffset;
    }

    /**
     * Returns specific byte position in the document.
     *
     * @return data position
     */
    public long getDataPosition() {
        return dataPosition;
    }

    /**
     * Returns active code area section.
     *
     * @return section
     */
    public Optional<RCodeAreaSection> getSection() {
        return Optional.ofNullable(section);
    }

    /**
     * Resets caret position.
     */
    public void reset() {
        this.dataPosition = 0;
        this.codeOffset = 0;
        this.section = null;
    }

    public void setCodeOffset(int codeOffset) {
        this.codeOffset = codeOffset;
    }

    public void setDataPosition(long dataPosition) {
        this.dataPosition = dataPosition;
    }

    public void setSection(RCodeAreaSection section) {
        this.section = section;
    }

    /**
     * Sets caret position according to given position.
     *
     * @param position source position
     */
    public void setCaretPosition(RCaretPos position) {
        dataPosition = position.getDataPosition();
        codeOffset = position.getCodeOffset();
        section = position.getSection().orElse(null);
    }

   @Override
    public int hashCode() {
        return Objects.hash(dataPosition, codeOffset, section);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final RCaretPos other = (RCaretPos) obj;
        return Objects.equals(this.dataPosition, other.dataPosition)
                && Objects.equals(this.codeOffset, other.codeOffset)
                && Objects.equals(this.section, other.section);
    }
}