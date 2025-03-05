package com.szadowsz.rotom4j.component.nitro.narc;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.rotom4j.component.nitro.ncer.NCERFolder;
import com.szadowsz.rotom4j.component.nitro.ncgr.NCGRFolder;
import com.szadowsz.rotom4j.component.nitro.nclr.NCLRFolder;
import com.szadowsz.rotom4j.component.nitro.nscr.NSCRFolder;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.RotomFile;
import com.szadowsz.rotom4j.file.nitro.BaseNFSFile;
import com.szadowsz.rotom4j.file.nitro.nanr.NANR;
import com.szadowsz.rotom4j.file.nitro.narc.NARC;
import com.szadowsz.rotom4j.file.nitro.ncer.NCER;
import com.szadowsz.rotom4j.file.nitro.ncgr.NCGR;
import com.szadowsz.rotom4j.file.nitro.nclr.NCLR;
import com.szadowsz.rotom4j.file.nitro.nscr.NSCR;
import com.szadowsz.rotom4j.component.nitro.nanr.NANRFolderComponent;

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
        List<RotomFile> files = narc.getFiles();

        for (RotomFile file : files){
            children.add(
                    switch (file) {
                        case NANR nanr -> new NANRFolderComponent(gui,path + "/" + nanr.getFileName(), this, nanr);
                        case NCER ncer -> new NCERFolder(gui,path + "/" + ncer.getFileName(), this, ncer);
                        case NSCR nscr -> new NSCRFolder(gui,path + "/" + nscr.getFileName(), this, nscr);
                        case NCGR ncgr -> new NCGRFolder(gui,path + "/" + ncgr.getFileName(), this, ncgr);
                        case NCLR nclr -> new NCLRFolder(gui,path + "/" + nclr.getFileName(), this, nclr);
                        default -> new RButton(gui,path + "/" + file.getFileName(), this);// new BinFolderNode(path + "/" + file.getFileName(), this, file);
                    }
            );
        }
    }
}
