package com.szadowsz.nds4j.app.nodes.nitro.narc;

import com.szadowsz.nds4j.file.nitro.narc.NARC;
import com.old.ui.input.ActivateByType;
import com.old.ui.node.impl.ButtonNode;
import com.old.ui.node.impl.FolderNode;
import com.old.ui.node.impl.TextNode;
import com.old.ui.window.WindowManager;

public class NarcReindexFolderNode extends FolderNode {
    private static final String NAME = "Name";
    private static final String REINDEX = "Reindex";

    private final NarcFolderNode narcFolder;
    private final NARC narc;

    NarcReindexFolderNode(String path, NarcFolderNode parent) {
        super(path, parent);
        narcFolder = parent;
        narc = narcFolder.narc;
        initNodes();
    }

    private void initNodes() {
        if (!children.isEmpty()) {
            return;
        }
        TextNode text = new TextNode(path + "/" + NAME, this,"");
        children.add(text);

        ButtonNode reindex = new ButtonNode(path + "/" + REINDEX, this);
        reindex.registerAction(ActivateByType.RELEASE, () -> {
            String reindexValue = text.getValueAsString();
            text.setStringValue("");
            narc.reindex(reindexValue);
            WindowManager.uncoverOrCreateWindow(text.parent);
//            filesRadio.setOptions(narc.getFilenames().toArray(new String[0]), narc.getFilenames().getFirst());
        });
        children.add(reindex);
    }
}
