package com.szadowsz.nds4j.app.nodes.nitro.narc;

import com.szadowsz.nds4j.app.nodes.nitro.nanr.NANRFolderNode;
import com.szadowsz.nds4j.app.nodes.nitro.ncer.NCERFolderNode;
import com.szadowsz.nds4j.app.nodes.nitro.ncgr.NCGRFolderNode;
import com.szadowsz.nds4j.app.nodes.nitro.nclr.NCLRFolderNode;
import com.szadowsz.nds4j.app.nodes.nitro.nscr.NSCRFolderNode;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.BaseNFSFile;
import com.szadowsz.nds4j.file.nitro.nanr.NANR;
import com.szadowsz.nds4j.file.nitro.narc.NARC;
import com.szadowsz.nds4j.file.nitro.ncer.NCER;
import com.szadowsz.nds4j.file.nitro.ncgr.NCGR;
import com.szadowsz.nds4j.file.nitro.nclr.NCLR;
import com.szadowsz.nds4j.file.nitro.nscr.NSCR;
import com.szadowsz.ui.input.ActivateByType;
import com.szadowsz.ui.node.impl.ButtonNode;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.node.impl.TextNode;
import com.szadowsz.ui.window.WindowManager;

import java.util.List;

public class NarcFilesFolderNode extends FolderNode {
    private static final String NAME = "Name";
    private static final String REINDEX = "Reindex";

    private final NarcFolderNode narcFolder;
    private final NARC narc;

    NarcFilesFolderNode(String path, NarcFolderNode parent) {
        super(path, parent);
        narcFolder = parent;
        narc = narcFolder.narc;
        try {
            initNodes();
        } catch (NitroException ignored){

        }
    }

    private void initNodes() throws NitroException {
        if (!children.isEmpty()) {
            return;
        }
        List<BaseNFSFile> files = narc.getFiles();

        for (BaseNFSFile file : files){
            children.add(
                    switch (file) {
                        case NANR nanr -> new NANRFolderNode(path + "/" + nanr.getFileName(), this, nanr);
                        case NCER ncer -> new NCERFolderNode(path + "/" + ncer.getFileName(), this, ncer);
                        case NSCR nscr -> new NSCRFolderNode(path + "/" + nscr.getFileName(), this, nscr);
                        case NCGR ncgr -> new NCGRFolderNode(path + "/" + ncgr.getFileName(), this, ncgr);
                        case NCLR nclr -> new NCLRFolderNode(path + "/" + nclr.getFileName(), this, nclr);
                        default -> new ButtonNode(path + "/" + file.getFileName(), this);// new BinFolderNode(path + "/" + file.getFileName(), this, file);
                    }
            );
        }
    }
}
