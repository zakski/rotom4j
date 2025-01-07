package com.szadowsz.gui.component.group.drawable.tab;

import com.szadowsz.gui.component.RComponent;

public interface RTabFunction<C extends RComponent> {

    C createComponent(RTab parent);
}
