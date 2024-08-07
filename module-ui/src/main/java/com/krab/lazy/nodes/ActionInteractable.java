package com.krab.lazy.nodes;

import com.krab.lazy.input.mouse.MouseAction;

public interface ActionInteractable extends MouseInteractable {

    void registerAction(ActivateByType type, MouseAction action);

    void executePressActions();

    void executeReleaseActions();
}
