package com.szadowsz.nds4j.app.nodes.bin.raw.swing;

import com.szadowsz.nds4j.app.nodes.bin.raw.*;
import com.szadowsz.nds4j.app.nodes.bin.raw.swing.capability.FontCapable;
import com.szadowsz.nds4j.file.bin.core.BinaryData;

import javax.accessibility.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;

/**
 * Binary viewer/editor component.
 *
 * @author ExBin Project (https://exbin.org)
 */
public abstract class CodeAreaSwing extends JTextComponent implements CodeAreaControl, FontCapable, Accessible {

    protected CodeAreaCore core;
    protected CodeAreaCommandHandler commandHandler;

    /**
     * Creates new instance
     */
    public CodeAreaSwing(CodeAreaCore core) {
        super();
        this.core = core;
        commandHandler = core.getCommandHandler();
        init();
    }

    private void init() {
        enableEvents(AWTEvent.KEY_EVENT_MASK);
        setName("CodeArea");
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        registerControlListeners();
    }

    private void registerControlListeners() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                updateLayout();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                commandHandler.keyTyped(keyEvent);
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                commandHandler.keyPressed(keyEvent);
            }
        });

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    public void updateLayout(){
        core.updateLayout();
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new CodeAreaSwing.AccessibleComponent();
        }
        return accessibleContext;
    }

    public CodeAreaCommandHandler getCommandHandler() {
        return commandHandler;
    }

    public void setContentData(BinaryData contentData) {
        core.setContentData(contentData);
        repaint();
    }

    public class AccessibleComponent extends AccessibleJComponent {

        /**
         * Gets the role of this object.
         *
         * @return an instance of AccessibleRole describing the role of the
         * object (AccessibleRole.TEXT)
         * @see AccessibleRole
         */
        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.TEXT;
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            states.add(AccessibleState.MULTI_LINE);
            if (CodeAreaSwing.this.isEditable()) {
                states.add(AccessibleState.EDITABLE);
            }
            return states;
        }
    }
    /**
     * Updates scroll position.
     *
     * @param scrollPosition scroll position
     */
    public void updateScrollPosition(CodeAreaScrollPosition scrollPosition) {
        if (!scrollPosition.equals(this.core.getScrollPosition())) {
            core.updateScrollPosition(scrollPosition);
            repaint();
            core.notifyScrolled();
        }
    }

    public void addDataChangedListener(DataChangedListener dataChangedListener) {
        core.addDataChangedListener(dataChangedListener);
    }

    public void removeDataChangedListener(DataChangedListener dataChangedListener) {
        core.removeDataChangedListener(dataChangedListener);
    }
}
