package com.szadowsz.gui.component.binary;


import com.szadowsz.gui.component.binary.auxiliary.binary_data.BinaryData;
import com.szadowsz.gui.component.binary.auxiliary.binary_data.EmptyBinaryData;
import com.szadowsz.gui.component.binary.basic.*;
import com.szadowsz.gui.component.binary.capability.SelectionCapable;
import com.szadowsz.gui.component.binary.swing.CodeAreaCommandHandler;
import com.szadowsz.gui.component.text.RTextBase;

import javax.accessibility.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import static com.sun.java.accessibility.util.AWTEventMonitor.addFocusListener;

/**
 * Binary data viewer/editor component.
 */
//public abstract class RCodeAreaCore extends RTextBase implements CodeAreaControl, Accessible { // TODO Determine Exact Relationship
public abstract class RCodeAreaCore extends JTextComponent implements CodeAreaControl, Accessible { // TODO Determine Exact Relationship
    // TODO Component Stub : WIP

    protected BinaryData contentData = EmptyBinaryData.INSTANCE;

    protected CodeAreaCommandHandler commandHandler;

    protected final List<DataChangedListener> dataChangedListeners = new ArrayList<>();

    /**
     * Creates new instance with provided command handler factory method.
     *
     * @param commandHandlerFactory command handler or null for default handler
     */
    public RCodeAreaCore(CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        super();
        this.commandHandler = createCommandHandler(CodeAreaUtils.requireNonNull(commandHandlerFactory));
        init();
    }

    private void init() {
        enableEvents(AWTEvent.KEY_EVENT_MASK);
        setName("CodeArea");
        setCaret(new SimulatedCaret());
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        registerControlListeners();
    }

    private CodeAreaCommandHandler createCommandHandler(CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        return commandHandlerFactory.createCommandHandler(this);
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

    // TODO - collision with component
    @Override
    public boolean hasSelection() {
        if (this instanceof SelectionCapable) {
            return ((SelectionCapable) this).hasSelection();
        }

        return false;
    }

    @Override
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
        repaint();
    }

    @Override
    public long getDataSize() {
        return contentData.getDataSize();
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleComponent();
        }
        return accessibleContext;
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
            if (RCodeAreaCore.this.isEditable()) {
                states.add(AccessibleState.EDITABLE);
            }
            return states;
        }
    }

   private class SimulatedCaret implements Caret {

        private JTextComponent component;
        private final EventListenerList listenerList = new EventListenerList();
        private boolean visible = true;
        private boolean selectionVisible = false;
        private int rate;
        private int dot;
        private Point magicCaretPosition = new Point();

        @Override
        public void install(JTextComponent component) {
            this.component = component;
        }

        @Override
        public void deinstall(JTextComponent jtc) {
            this.component = null;
        }

        @Override
        public void paint(Graphics g) {
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            listenerList.add(ChangeListener.class, listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            listenerList.remove(ChangeListener.class, listener);
        }

        @Override
        public boolean isVisible() {
            return visible;
        }

        @Override
        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        @Override
        public boolean isSelectionVisible() {
            return selectionVisible;
        }

        @Override
        public void setSelectionVisible(boolean selectionVisible) {
            this.selectionVisible = selectionVisible;
        }

        @Override
        public void setMagicCaretPosition(Point magicCaretPosition) {
            this.magicCaretPosition = magicCaretPosition;
        }

        @Override
        public Point getMagicCaretPosition() {
            return magicCaretPosition;
        }

        @Override
        public void setBlinkRate(int rate) {
            this.rate = rate;
        }

        @Override
        public int getBlinkRate() {
            return rate;
        }

        @Override
        public int getDot() {
            return dot;
        }

        @Override
        public int getMark() {
            return 0;
        }

        @Override
        public void setDot(int dot) {
            this.dot = dot;
        }

        @Override
        public void moveDot(int dot) {
            this.dot = dot;
        }
    }
}
