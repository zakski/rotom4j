package com.szadowsz.ui.input;


import com.szadowsz.ui.input.mouse.MouseAction;

public interface ActionInteractable extends MouseInteractable {

    void registerAction(ActivateByType type, MouseAction action);

    void executePressActions();

    void executeReleaseActions();
}
