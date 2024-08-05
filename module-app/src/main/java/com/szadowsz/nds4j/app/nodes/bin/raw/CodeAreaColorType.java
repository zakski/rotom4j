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

import java.util.Optional;

/**
 * Enumeration of color types for main group.
 *
 * @author ExBin Project (https://exbin.org)
 */
public enum CodeAreaColorType {

    TEXT_COLOR("textColor", CodeAreaColorGroup.MAIN),
    TEXT_BACKGROUND("textBackground", CodeAreaColorGroup.MAIN),
    SELECTION_COLOR("selectionColor", CodeAreaColorGroup.SELECTION),
    SELECTION_BACKGROUND("selectionBackground", CodeAreaColorGroup.SELECTION),
    SELECTION_MIRROR_COLOR("selectionMirrorColor", CodeAreaColorGroup.SELECTION),
    SELECTION_MIRROR_BACKGROUND("selectionMirrorBackground", CodeAreaColorGroup.SELECTION),
    ALTERNATE_COLOR("alternateColor", CodeAreaColorGroup.MAIN),
    ALTERNATE_BACKGROUND("alternateBackground", CodeAreaColorGroup.MAIN),
    CURSOR_COLOR("cursorColor", null),
    CURSOR_NEGATIVE_COLOR("cursorNegativeColor", null),
    LINE("decoration.line", null);

    private final String typeId;
    private final CodeAreaColorGroup group;

    CodeAreaColorType(String typeId, CodeAreaColorGroup group) {
        this.typeId = typeId;
        this.group = group;
    }

    /**
     * Returns unique string identifier.
     * <p>
     * Custom implementations should start with full package name to avoid
     * collisions.
     *
     * @return unique identification ID key
     */
    public String getId() {
        return typeId;
    }

    /**
     * Returns group which this color belongs to or empty.
     *
     * @return group
     */
    public Optional<CodeAreaColorGroup> getGroup() {
        return Optional.ofNullable(group);
    }
}
