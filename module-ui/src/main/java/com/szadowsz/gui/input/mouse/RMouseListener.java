package com.szadowsz.gui.input.mouse;

public interface RMouseListener {

    void mousePressed(RMouseEvent e);

    void mouseReleased(RMouseEvent e);

    void mouseDragged(RMouseEvent e);

    void mouseMoved(RMouseEvent e);

    void mouseClicked(RMouseEvent e);

    void mouseEntered(RMouseEvent e);

    void mouseExited(RMouseEvent e);

    void mouseWheel(RMouseEvent e);
}
