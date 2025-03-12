package com.old.krab.lazy.nodes;

import com.old.krab.lazy.input.mouse.MouseAction;

public interface ActionInteractable extends MouseInteractable {

    void registerAction(ActivateByType type, MouseAction action);

    void executePressActions();

    void executeReleaseActions();
}
