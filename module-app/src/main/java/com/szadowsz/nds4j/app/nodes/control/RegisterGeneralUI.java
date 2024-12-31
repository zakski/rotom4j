package com.szadowsz.nds4j.app.nodes.control;

import com.szadowsz.nds4j.app.NDSGuiImpl;
import com.szadowsz.nds4j.app.nodes.nitro.NitroImgFolderNode;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.utils.Configuration;
import com.old.ui.NDSGui;
import com.old.ui.input.ActivateByType;
import com.old.ui.node.NodeTree;
import com.old.ui.node.impl.ButtonNode;
import com.old.ui.node.impl.FolderNode;
import com.old.ui.node.impl.ToggleNode;
import com.old.ui.window.WindowManager;

import static com.szadowsz.nds4j.app.nodes.control.ControlConstants.*;
import static com.old.ui.store.LayoutStore.cell;

public class RegisterGeneralUI {


    private RegisterGeneralUI(){}

    private static void recolorImages() {
        var nodes = NodeTree.getAllNodesAsList(NitroImgFolderNode.class);
        nodes.forEach(n -> {
            try {
                n.recolorImage();
            } catch (NitroException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void registerNarcButton(NDSGuiImpl gui) {
        ButtonNode selectNarc = gui.button(selectNarcFile);
        selectNarc.registerAction(ActivateByType.RELEASE,() -> LoadedFileUI.createNarcUI(gui));
    }

    private static void register2DFileButtons(NDSGuiImpl gui) {
        // Tier 1a open
        gui.pushDropdown("Open 2D File");

        ButtonNode selectNanr = gui.button(selectNANRFile);
        selectNanr.registerAction(ActivateByType.RELEASE,() -> LoadedFileUI.createNanrUI(gui));

        ButtonNode selectNcer = gui.button(selectNCERFile);
        selectNcer.registerAction(ActivateByType.RELEASE,() -> LoadedFileUI.createNcerUI(gui));

        ButtonNode selectNscr = gui.button(selectNSCRFile);
        selectNscr.registerAction(ActivateByType.RELEASE,() -> LoadedFileUI.createNscrUI(gui));

        ButtonNode selectNcgr = gui.button(selectNCGRFile);
        selectNcgr.registerAction(ActivateByType.RELEASE,() -> LoadedFileUI.createNcgrUI(gui));

        ButtonNode selectNcLr = gui.button(selectNCLRFile);
        selectNcLr.registerAction(ActivateByType.RELEASE,() -> LoadedFileUI.createNclrUI(gui));

        // Tier 1a close
        gui.popFolder();
    }

    private static void registerDataButtons(NDSGuiImpl gui) {
        // Tier 1b open
        gui.pushDropdown("Open Data File");
        ButtonNode selectEvo = gui.button(selectEvoFile);
        selectEvo.registerAction(ActivateByType.RELEASE, () -> LoadedFileUI.createEvoUI(gui));
        ButtonNode selectStats = gui.button(selectStatsFile);
        selectStats.registerAction(ActivateByType.RELEASE, () -> LoadedFileUI.createStatsUI(gui));
        ButtonNode selectLearn = gui.button(selectLearnFile);
        selectLearn.registerAction(ActivateByType.RELEASE, () -> LoadedFileUI.createLearnUI(gui));
        ButtonNode selectGrow = gui.button(selectGrowthFile);
        selectGrow.registerAction(ActivateByType.RELEASE, () -> LoadedFileUI.createGrowthUI(gui));
        // Tier 1b close
        gui.popFolder();
    }

    private static void registerOpenFilesView(NDSGui gui) {
        // Tier 1bc open
        FolderNode loaded = gui.pushFolder("Loaded Files");
        WindowManager.uncoverOrCreateWindow(loaded,false,cell,cell*2,null);
        // Tier 1c close
        gui.popFolder();
    }

    public static void buildFileDropdown(NDSGuiImpl gui) {
        // Tier 0a open
        gui.pushDropdown("File");

        registerNarcButton(gui);
        register2DFileButtons(gui);
        registerDataButtons(gui);

        // Tier 0a close
        gui.popFolder();
    }

    public static void buildViewDropdown(NDSGui gui) {
        // Tier 0b open
        gui.pushDropdown("View");

        registerOpenFilesView(gui);

        // Tier 0b close
        gui.popFolder();
    }

    public static void buildOptionsDropdown(NDSGui gui) {
        // Tier 0c open
        gui.pushDropdown("Options");

        ToggleNode cellBounds =gui.toggle(showCellBounds, Configuration.isShowCellBounds());
        cellBounds.registerAction((b) -> {
            Configuration.setShowCellBounds(b);
            recolorImages();
        });

        ToggleNode guidelines = gui.toggle(showGuidelines, Configuration.isShowGuidelines());
        guidelines.registerAction((b) -> {
            Configuration.setShowGuidelines(b);
            recolorImages();
        });

        ToggleNode renderTransparent = gui.toggle(showTransparent, Configuration.isRenderTransparent());
        renderTransparent.registerAction((b) -> {
            Configuration.setRenderTransparent(b);
            recolorImages();
        });

        ToggleNode renderBackground = gui.toggle(showBackground, Configuration.isBackground());
        renderBackground.registerAction((b) -> {
            Configuration.setRenderBackground(b);
            recolorImages();
        });
        // Tier 0c close
        gui.popFolder();
    }

}
