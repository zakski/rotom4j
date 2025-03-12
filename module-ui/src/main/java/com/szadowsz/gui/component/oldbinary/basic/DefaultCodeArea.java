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
package com.szadowsz.gui.component.oldbinary.basic;

import com.szadowsz.gui.component.oldbinary.capability.BackgroundPaintCapable;
import com.szadowsz.gui.component.oldbinary.capability.CaretCapable;
import com.szadowsz.gui.component.oldbinary.capability.CharsetCapable;
import com.szadowsz.gui.component.oldbinary.capability.ClipboardCapable;
import com.szadowsz.gui.component.oldbinary.capability.CodeCharactersCaseCapable;
import com.szadowsz.gui.component.oldbinary.capability.CodeTypeCapable;
import com.szadowsz.gui.component.oldbinary.capability.RowWrappingCapable;
import com.szadowsz.gui.component.oldbinary.capability.ScrollingCapable;
import com.szadowsz.gui.component.oldbinary.capability.SelectionCapable;
import com.szadowsz.gui.component.oldbinary.capability.ViewModeCapable;
import com.szadowsz.gui.component.oldbinary.capability.AntialiasingCapable;
import com.szadowsz.gui.component.oldbinary.capability.BasicColorsCapable;
import com.szadowsz.gui.component.oldbinary.capability.FontCapable;
import com.szadowsz.gui.component.oldbinary.capability.BasicScrollingCapable;
import com.szadowsz.gui.component.oldbinary.capability.EditModeCapable;

/**
 * Code area default component interface.
 *
 * @author ExBin Project (https://exbin.org)
 */
public interface DefaultCodeArea extends SelectionCapable, CaretCapable, BasicScrollingCapable, ScrollingCapable, ViewModeCapable,
        CodeTypeCapable, EditModeCapable, CharsetCapable, CodeCharactersCaseCapable, FontCapable,
        BackgroundPaintCapable, RowWrappingCapable, ClipboardCapable, BasicColorsCapable, AntialiasingCapable {
}
