package com.szadowsz.rotom4j.component.nitro.narc;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.RGroupDrawable;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.component.nitro.nanr.NANRFolder;
import com.szadowsz.rotom4j.component.nitro.ncer.NCERFolder;
import com.szadowsz.rotom4j.component.nitro.ncgr.NCGRFolder;
import com.szadowsz.rotom4j.component.nitro.nclr.NCLRFolder;
import com.szadowsz.rotom4j.component.nitro.nscr.NSCRFolder;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.RotomFile;
import com.szadowsz.rotom4j.file.nitro.nanr.NANR;
import com.szadowsz.rotom4j.file.nitro.narc.NARC;
import com.szadowsz.rotom4j.file.nitro.ncer.NCER;
import com.szadowsz.rotom4j.file.nitro.ncgr.NCGR;
import com.szadowsz.rotom4j.file.nitro.nclr.NCLR;
import com.szadowsz.rotom4j.file.nitro.nscr.NSCR;

import java.util.List;

public class NARCFilesGroup extends R4JComponent<NARC> {

    protected final NARC narc;

    public NARCFilesGroup(RotomGui gui, String path, RGroup parent, NARC data) {
        super(gui, path, parent);
        narc = data;
        try {
            initNodes();
        } catch (NitroException ignored) {

        }
    }

    private void initNodes() throws NitroException {
        if (!children.isEmpty()) {
            return;
        }
        List<RotomFile> files = narc.getFiles();

        for (RotomFile file : files) {
            children.add(
                    switch (file) {
                        case NANR nanr -> new NANRFolder(gui, path + "/" + nanr.getFileName(), this, nanr);
                        case NCER ncer -> new NCERFolder(gui, path + "/" + ncer.getFileName(), this, ncer);
                        case NSCR nscr -> new NSCRFolder(gui, path + "/" + nscr.getFileName(), this, nscr);
                        case NCGR ncgr -> new NCGRFolder(gui, path + "/" + ncgr.getFileName(), this, ncgr);
                        case NCLR nclr -> new NCLRFolder(gui, path + "/" + nclr.getFileName(), this, nclr);
                        default ->
                                new RButton(gui, path + "/" + file.getFileName(), this);// new BinFolderNode(path + "/" + file.getFileName(), this, file);
                    }
            );
        }
    }

    @Override
    public void setLayout(RLayoutBase layout) {

    }

    @Override
    public void recolorImage() throws NitroException {
    }
}
