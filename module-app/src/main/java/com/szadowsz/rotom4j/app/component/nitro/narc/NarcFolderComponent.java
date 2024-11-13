package com.szadowsz.rotom4j.app.component.nitro.narc;

import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.nds4j.file.nitro.narc.NARC;
import com.szadowsz.rotom4j.app.RotomGuiImpl;

public class NarcFolderComponent extends RFolder {

    private static final String APPLY_FOLDER = "Apply";
    private static final String REINDEX_FOLDER = "Reindex";
    private static final String EXTRACT_FOLDER = "Extract";
    private static final String FILES_FOLDER = "Files";

    final NARC narc;

    public NarcFolderComponent(RotomGuiImpl gui, String path, RFolder parent, NARC narc) {
        super(gui, path, parent);
        this.narc = narc;
        initNodes();
    }

    private void initNodes() {
        if (!children.isEmpty()) {
            return;
        }
        children.add(new NarcApplyFolderComponent(gui,path + "/" + APPLY_FOLDER, this));
        children.add(new NarcReindexFolderComponent(gui,path + "/" + REINDEX_FOLDER, this));
        children.add(new NarcExtractFolderComponent(gui,path + "/" + EXTRACT_FOLDER, this));
        children.add(new NarcFilesFolderComponent(gui,path + "/" + FILES_FOLDER, this));
    }
}
