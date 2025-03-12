package com.old.krab.lazy.input;

import com.old.krab.lazy.input.keys.LazyKeyEvent;
import com.old.krab.lazy.stores.HotkeyStore;

/**
 * Singleton class listening for global hotkeys that fall through all the potential visual controls under the mouse unconsumed.
 */
public class HotkeySubscriber implements UserInputSubscriber {

    static HotkeySubscriber singleton;

    private HotkeySubscriber() {}

    public static void initSingleton() {
        if(singleton == null){
            singleton = new HotkeySubscriber();
        }
        UserInputPublisher.subscribe(singleton);
    }

    @Override
    public void keyPressed(LazyKeyEvent keyEvent) {
        HotkeyStore.handleHotkeyInteraction(keyEvent);
    }

}
