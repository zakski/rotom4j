package com.old.ui.input;


import com.old.ui.input.mouse.MouseAction;

public interface ActionInteractable extends MouseInteractable {

    void registerAction(ActivateByType type, MouseAction action);

    void executePressActions();

    void executeReleaseActions();
}
