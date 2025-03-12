package com.szadowsz.gui.component.group.drawable.tab;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.group.RGroupDrawable;
import com.szadowsz.gui.input.mouse.RActivateByType;
import com.szadowsz.gui.input.mouse.RMouseAction;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RDirection;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLinearLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RTabHeader extends RGroupDrawable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RTabHeader.class);

    private final RTabManager tabManager;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    protected RTabHeader(RotomGui gui, String path, RTabManager parent) {
        super(gui, path, parent);
        tabManager = parent;
        layout = new RLinearLayout(this, RDirection.HORIZONTAL);
    }

    @Override
    public void setLayout(RLayoutBase layout) {
    }

    public void addTab(String name, RMouseAction action) {
        RButton button = new RButton(gui, path + "/" + name, this);
        button.registerAction(RActivateByType.RELEASE, action);
        children.add(button);
        resetBuffer(); // RESET-VALID: we should resize the buffer if a new tab is added
    }
}
