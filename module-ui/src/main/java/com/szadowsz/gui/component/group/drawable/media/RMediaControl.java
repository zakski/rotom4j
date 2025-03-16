package com.szadowsz.gui.component.group.drawable.media;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.RGroupDrawable;
import com.szadowsz.gui.component.input.toggle.ROptionToggle;
import com.szadowsz.gui.layout.RDirection;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLinearLayout;

public class RMediaControl extends RGroupDrawable {

    private static final String LOOP = "loop";
    private static final String PLAY = "play-pause";
    private static final String PLAYBACK_SPEED = "playback_speed";
    private static final String SKIP_BACK = "skip_back";
    private static final String SKIP_FORWARD = "skip_forward";

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    public RMediaControl(RotomGui gui, String path, RGroup parent) {
        super(gui, path, parent);
        layout = new RLinearLayout(this, RDirection.HORIZONTAL); // Horizontal Layout
        initComponents();
    }

    private void initComponents() {
        children.add(new RSkip(gui, path + "/" + SKIP_BACK, this, false));
        children.add(new RPlayPause(gui, path + "/" + PLAY, this, false));
        children.add(new ROptionToggle<Float>(gui, path + "/" + PLAYBACK_SPEED, this, new Float[]{0.25f,0.5f,1.0f,2.0f,4.0f},2));
        children.add(new RSkip(gui, path + "/" + SKIP_FORWARD, this, true));
        children.add(new RLoop(gui, path + "/" + LOOP, this, false));
    }

    @Override
    public void setLayout(RLayoutBase layout) {
        // NOOP
    }
}
