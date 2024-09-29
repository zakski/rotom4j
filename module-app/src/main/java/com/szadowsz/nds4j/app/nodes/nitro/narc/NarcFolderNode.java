package com.szadowsz.nds4j.app.nodes.nitro.narc;

import com.szadowsz.nds4j.file.nitro.narc.NARC;
import com.old.ui.node.impl.FolderNode;

public class NarcFolderNode extends FolderNode {

    private static final String APPLY_FOLDER = "Apply";
    private static final String REINDEX_FOLDER = "Reindex";
    private static final String EXTRACT_FOLDER = "Extract";
    private static final String FILES_FOLDER = "Files";

    final NARC narc;

    public NarcFolderNode(String path, FolderNode parent, NARC narc) {
        super(path, parent);
        this.narc = narc;
        initNodes();
    }

    private void initNodes() {
        if (!children.isEmpty()) {
            return;
        }
        children.add(new NarcApplyFolderNode(path + "/" + APPLY_FOLDER, this));
        children.add(new NarcReindexFolderNode(path + "/" + REINDEX_FOLDER, this));
        children.add(new NarcExtractFolderNode(path + "/" + EXTRACT_FOLDER, this));
        children.add(new NarcFilesFolderNode(path + "/" + FILES_FOLDER, this));
    }
}
