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
package com.szadowsz.nds4j.app.nodes.bin.raw.swing;

import com.szadowsz.nds4j.app.nodes.bin.raw.operation.BinaryDataAbstractCommand;

/**
 * Abstract class for operation on code area component.
 *
 * @author ExBin Project (https://exbin.org)
 */
public abstract class CodeAreaCommand extends BinaryDataAbstractCommand {

    protected final CodeAreaSwing codeArea;

    public CodeAreaCommand(CodeAreaSwing codeArea) {
        this.codeArea = codeArea;
    }

    /**
     * Returns type of the command.
     *
     * @return command type
     */
    public abstract CodeAreaCommandType getType();

    @Override
    public String getName() {
        return getType().getName();
    }
}
