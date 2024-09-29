/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna).
 *
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010-2020 Martin Berglund
 */
package com.old.googlecode.lanterna.terminal.swing;

import com.old.googlecode.lanterna.TerminalSize;
import com.old.googlecode.lanterna.TextCharacter;
import com.old.googlecode.lanterna.input.KeyStroke;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.util.Collections;

/**
 * AWT implementation of {@link GraphicalTerminalImplementation} that contains all the overrides for AWT
 * Created by martin on 08/02/16.
 */
class AWTTerminalImplementation extends GraphicalTerminalImplementation {
    private final Component component;
    private final AWTTerminalFontConfiguration fontConfiguration;

    /**
     * Creates a new {@code AWTTerminalImplementation}
     * @param component Component that is the AWT terminal surface
     * @param fontConfiguration Font configuration to use
     * @param initialTerminalSize Initial size of the terminal
     * @param deviceConfiguration Device configuration
     * @param colorConfiguration Color configuration
     * @param scrollController Controller to be used when inspecting scroll status
     */
    AWTTerminalImplementation(
            Component component,
            AWTTerminalFontConfiguration fontConfiguration,
            TerminalSize initialTerminalSize,
            TerminalEmulatorDeviceConfiguration deviceConfiguration,
            TerminalEmulatorColorConfiguration colorConfiguration,
            TerminalScrollController scrollController) {

        super(initialTerminalSize, deviceConfiguration, colorConfiguration, scrollController);
        this.component = component;
        this.fontConfiguration = fontConfiguration;

        //Prevent us from shrinking beyond one character
        component.setMinimumSize(new Dimension(fontConfiguration.getFontWidth(), fontConfiguration.getFontHeight()));

        component.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.<AWTKeyStroke>emptySet());
        component.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.<AWTKeyStroke>emptySet());

        component.addKeyListener(new TerminalInputListener());
        component.addMouseListener(new TerminalMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                AWTTerminalImplementation.this.component.requestFocusInWindow();
            }
        });

        component.addHierarchyListener(e -> {
            if(e.getChangeFlags() == HierarchyEvent.DISPLAYABILITY_CHANGED) {
                if(e.getChanged().isDisplayable()) {
                    onCreated();
                }
                else {
                    onDestroyed();
                }
            }
        });
    }

    public AWTTerminalFontConfiguration getFontConfiguration() {
        return fontConfiguration;
    }

    @Override
    protected int getFontHeight() {
        return fontConfiguration.getFontHeight();
    }

    @Override
    protected int getFontWidth() {
        return fontConfiguration.getFontWidth();
    }

    @Override
    protected int getHeight() {
        return component.getHeight();
    }

    @Override
    protected int getWidth() {
        return component.getWidth();
    }

    @Override
    protected Font getFontForCharacter(TextCharacter character) {
        return fontConfiguration.getFontForCharacter(character);
    }

    @Override
    protected boolean isTextAntiAliased() {
        return fontConfiguration.isAntiAliased();
    }

    @Override
    protected void repaint() {
        if(EventQueue.isDispatchThread()) {
            component.repaint();
        }
        else {
            EventQueue.invokeLater(component::repaint);
        }
    }

    @Override
    public KeyStroke readInput() {
        if(EventQueue.isDispatchThread()) {
            throw new UnsupportedOperationException("Cannot call SwingTerminal.readInput() on the AWT thread");
        }
        return super.readInput();
    }
}
