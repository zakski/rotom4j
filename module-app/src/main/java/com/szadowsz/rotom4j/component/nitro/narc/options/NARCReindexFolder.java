package com.szadowsz.rotom4j.component.nitro.narc.options;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.component.text.RTextField;
import com.szadowsz.gui.input.mouse.RActivateByType;
import com.szadowsz.rotom4j.file.nitro.narc.NARC;

public class NARCReindexFolder extends RFolder {
    private static final String NAME = "Name";
    private static final String REINDEX = "Reindex";

    private final NARCOptions narcOptions;
    private final NARC narc;

    public NARCReindexFolder(RotomGui gui, String path, NARCOptions parent) {
        super(gui,path, parent);
        narcOptions = parent;
        narc = narcOptions.getData();
        initNodes();
    }

    private void initNodes() {
        if (!children.isEmpty()) {
            return;
        }
        RTextField text = new RTextField(gui,path + "/" + NAME, this);
        children.add(text);

        RButton reindex = new RButton(gui,path + "/" + REINDEX, this);
        reindex.registerAction(RActivateByType.RELEASE, () -> {
            String reindexValue = text.getValueAsString();
            text.setText("");
            narc.reindex(reindexValue);
            gui.getWinManager().uncoverOrCreateWindow(text.getParentFolder());
//            filesRadio.setOptions(narc.getFilenames().toArray(new String[0]), narc.getFilenames().getFirst());
        });
        children.add(reindex);
    }
}
