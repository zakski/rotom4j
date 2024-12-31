package com.szadowsz.gui.input.clip;

import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;

public interface ClipboardData extends Transferable, ClipboardOwner {

        void dispose();
    }