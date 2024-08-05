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

import com.szadowsz.nds4j.app.nodes.bin.core.ScrollingListener;
import com.szadowsz.nds4j.file.bin.core.BinaryData;
import com.szadowsz.nds4j.file.bin.core.EmptyBinaryData;

import java.util.ArrayList;
import java.util.List;


public abstract class CodeAreaCore implements CodeAreaControl {

    private final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();

    private BinaryData contentData = EmptyBinaryData.INSTANCE;

    private CodeAreaCommandHandler commandHandler;

    private final List<DataChangedListener> dataChangedListeners = new ArrayList<>();

    /**
     * Creates new instance with provided command handler factory method.
     *
     * @param commandHandlerFactory command handler or null for default handler
     */
    public CodeAreaCore(CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        this.commandHandler = createCommandHandler(commandHandlerFactory);
    }

    private CodeAreaCommandHandler createCommandHandler(CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        return commandHandlerFactory.createCommandHandler(this);
    }

    public CodeAreaCommandHandler getCommandHandler() {
        return commandHandler;
    }

    public void setCommandHandler(CodeAreaCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public void copy() {
        commandHandler.copy();
    }

    public void copyAsCode() {
        commandHandler.copyAsCode();
    }

    @Override
    public void cut() {
        commandHandler.cut();
    }

    @Override
    public void paste() {
        commandHandler.paste();
    }

    public void pasteFromCode() {
        commandHandler.pasteFromCode();
    }

    @Override
    public void delete() {
        commandHandler.delete();
    }

    @Override
    public void selectAll() {
        commandHandler.selectAll();
    }

    @Override
    public void clearSelection() {
        commandHandler.clearSelection();
    }

    @Override
    public boolean canPaste() {
        return commandHandler.canPaste();
    }

    @Override
    public boolean hasSelection() {
        return this.hasSelection();
    }

    public boolean isEditable() {
        return false;
    }

    @Override
    public BinaryData getContentData() {
        return contentData;
    }

    public void setContentData(BinaryData contentData) {
        this.contentData = contentData == null ? EmptyBinaryData.INSTANCE : contentData;
        notifyDataChanged();
    }

    @Override
    public long getDataSize() {
        return contentData.getDataSize();
    }

    /**
     * Notifies component, that the internal data was changed.
     */
    public void notifyDataChanged() {
        dataChangedListeners.forEach(DataChangedListener::dataChanged);
    }

    public void addDataChangedListener(DataChangedListener dataChangedListener) {
        dataChangedListeners.add(dataChangedListener);
    }

    public void removeDataChangedListener(DataChangedListener dataChangedListener) {
        dataChangedListeners.remove(dataChangedListener);
    }

    public abstract void resetPainter();

    public abstract void updateLayout();

    public void updateScrollPosition(CodeAreaScrollPosition scrollPosition) {
        if (!scrollPosition.equals(this.scrollPosition)) {
            this.scrollPosition.setScrollPosition(scrollPosition);
        }
    }

    public abstract void notifyScrolled();
}
