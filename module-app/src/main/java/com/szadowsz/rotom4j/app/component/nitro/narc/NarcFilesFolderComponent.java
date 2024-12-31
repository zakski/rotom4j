package com.szadowsz.rotom4j.app.component.nitro.narc;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.BaseNFSFile;
import com.szadowsz.nds4j.file.nitro.nanr.NANR;
import com.szadowsz.nds4j.file.nitro.narc.NARC;
import com.szadowsz.nds4j.file.nitro.ncer.NCER;
import com.szadowsz.nds4j.file.nitro.ncgr.NCGR;
import com.szadowsz.nds4j.file.nitro.nclr.NCLR;
import com.szadowsz.nds4j.file.nitro.nscr.NSCR;
import com.szadowsz.rotom4j.app.RotomGuiImpl;
import com.szadowsz.rotom4j.app.component.nitro.nanr.NANRFolderComponent;
import com.szadowsz.rotom4j.app.component.nitro.ncer.NCERFolderComponent;
import com.szadowsz.rotom4j.app.component.nitro.ncgr.NCGRFolderComponent;
import com.szadowsz.rotom4j.app.component.nitro.nclr.NCLRFolderComponent;
import com.szadowsz.rotom4j.app.component.nitro.nscr.NSCRFolderComponent;

import java.util.List;

public class NarcFilesFolderComponent extends RFolder {
    private static final String NAME = "Name";
    private static final String REINDEX = "Reindex";

    private final NarcFolderComponent narcFolder;
    private final NARC narc;

    NarcFilesFolderComponent(RotomGui gui, String path, NarcFolderComponent parent) {
        super(gui, path, parent);
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
                        case NANR nanr -> new NANRFolderComponent(gui,path + "/" + nanr.getFileName(), this, nanr);
                        case NCER ncer -> new NCERFolderComponent(gui,path + "/" + ncer.getFileName(), this, ncer);
                        case NSCR nscr -> new NSCRFolderComponent(gui,path + "/" + nscr.getFileName(), this, nscr);
                        case NCGR ncgr -> new NCGRFolderComponent(gui,path + "/" + ncgr.getFileName(), this, ncgr);
                        case NCLR nclr -> new NCLRFolderComponent(gui,path + "/" + nclr.getFileName(), this, nclr);
                        default -> new RButton(gui,path + "/" + file.getFileName(), this);// new BinFolderNode(path + "/" + file.getFileName(), this, file);
                    }
            );
        }
    }
}
