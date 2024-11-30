package com.szadowsz.gui.component.oldbined;


import com.szadowsz.binary.BinaryData;
import com.szadowsz.binary.EmptyBinaryData;
import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.oldbinary.CodeAreaUtils;
import com.szadowsz.gui.component.oldbinary.DataChangedListener;
import com.szadowsz.gui.component.oldbined.capabilities.SelectionCapable;
import com.szadowsz.gui.component.oldbined.command.CodeAreaCommandHandler;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.component.text.RTextArea;

import java.util.ArrayList;
import java.util.List;

/**
 * Binary data viewer/editor component.
 */
public class RBinedAreaCore extends RTextArea implements DataProvider { // TODO Determine Exact Relationship

    protected BinaryData contentData = EmptyBinaryData.INSTANCE;

    protected CodeAreaCommandHandler commandHandler;

    protected final List<DataChangedListener> dataChangedListeners = new ArrayList<>();

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui          the gui for the window that the component is drawn under
     * @param path         the path in the component tree
     * @param parentFolder the parent component folder reference // TODO consider if needed
     */
    protected RBinedAreaCore(RotomGui gui, String path, RFolder parentFolder, CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        super(gui, path, parentFolder);
        this.commandHandler = createCommandHandler(CodeAreaUtils.requireNonNull(commandHandlerFactory));
        init();
    }

    protected CodeAreaCommandHandler createCommandHandler(CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        return commandHandlerFactory.createCommandHandler(this);
    }

    private void init() {
//        enableEvents(AWTEvent.KEY_EVENT_MASK);
//        setName("CodeArea");
//        setCaret(new RCodeAreaCore.SimulatedCaret());
//        setFocusable(true);
//        setFocusTraversalKeysEnabled(false);
//        registerControlListeners();
    }

    public CodeAreaCommandHandler getCommandHandler() {
        return commandHandler;
    }

    @Override
    public BinaryData getContentData() {
        return contentData;
    }

    @Override
    public long getDataSize() {
        return contentData.getDataSize();
    }


    public void setCommandHandler(CodeAreaCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    /**
     * Copies selection to clipboard.
     */
    public void copy() {
        commandHandler.copy();
    }

    public void copyAsCode() {
        commandHandler.copyAsCode();
    }

    /**
     * Cuts selection to clipboard.
     */
    public void cut() {
        commandHandler.cut();
    }

    /**
     * Pastes content of the clipboard.
     */
    public void paste() {
        commandHandler.paste();
    }

    public void pasteFromCode() {
        commandHandler.pasteFromCode();
    }

    /**
     * Deletes selected section.
     */
    public void delete() {
        commandHandler.delete();
    }

    /**
     * Expands selection to all data.
     */
    public void selectAll() {
        commandHandler.selectAll();
    }

    /**
     * Returns true if content of the clipboard is valid for paste operation.
     *
     * @return true if paste can proceed
     */
    public boolean canPaste() {
        return commandHandler.canPaste();
    }

    /**
     * Returns true if selection is not empty.
     *
     * @return true if selection is not empty
     */
    public boolean hasSelection() {
        if (this instanceof SelectionCapable) {
            return ((SelectionCapable) this).hasSelection();
        }

        return false;
    }

    /**
     * Clears data selection.
     */
    public void clearSelection() {
        commandHandler.clearSelection();
    }

    /**
     * Notifies component, that the internal data was changed.
     */
    public void notifyDataChanged() {
        dataChangedListeners.forEach(DataChangedListener::dataChanged);
    }
}
