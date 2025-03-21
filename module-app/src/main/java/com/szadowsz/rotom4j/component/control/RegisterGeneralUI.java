package com.szadowsz.rotom4j.component.control;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.input.toggle.RToggle;
import com.szadowsz.gui.input.mouse.RActivateByType;
import com.szadowsz.rotom4j.component.R4JFolder;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.utils.Configuration;
import com.szadowsz.rotom4j.app.RotomGuiImpl;

public class RegisterGeneralUI extends ControlConstants {


    private RegisterGeneralUI(){}

    private static void recolorImages(RotomGui gui) {
        var nodes = gui.getComponentTree().getComponents(R4JFolder.class);
        nodes.forEach(n -> {
            try {
                n.recolorImage();
            } catch (NitroException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void registerNarcButton(RotomGuiImpl gui) {
        RButton selectNarc = gui.button(selectNarcFile);
        selectNarc.registerAction(RActivateByType.RELEASE,() -> LoadedFileUI.createNarcUI(gui));
    }

    private static void register2DFileButtons(RotomGuiImpl gui) {
        // Tier 1a open
        gui.pushDropdown("Open 2D File");

        RButton selectNanr = gui.button(selectNANRFile);
        selectNanr.registerAction(RActivateByType.RELEASE,() -> LoadedFileUI.createNanrUI(gui));

        RButton selectNcer = gui.button(selectNCERFile);
        selectNcer.registerAction(RActivateByType.RELEASE,() -> LoadedFileUI.createNcerUI(gui));

        RButton selectNscr = gui.button(selectNSCRFile);
        selectNscr.registerAction(RActivateByType.RELEASE,() -> LoadedFileUI.createNscrUI(gui));

        RButton selectNcgr = gui.button(selectNCGRFile);
        selectNcgr.registerAction(RActivateByType.RELEASE,() -> LoadedFileUI.createNcgrUI(gui));

        RButton selectNcLr = gui.button(selectNCLRFile);
        selectNcLr.registerAction(RActivateByType.RELEASE,() -> LoadedFileUI.createNclrUI(gui));

        // Tier 1a close
        gui.popWindow();
    }

    private static void registerDataButtons(RotomGuiImpl gui) {
        // Tier 1b open
        gui.pushDropdown("Open Data File");
        RButton selectEvo = gui.button(selectEvoFile);
        selectEvo.registerAction(RActivateByType.RELEASE, () -> LoadedFileUI.createEvoUI(gui));
        RButton selectStats = gui.button(selectStatsFile);
        selectStats.registerAction(RActivateByType.RELEASE, () -> LoadedFileUI.createStatsUI(gui));
        RButton selectLearn = gui.button(selectLearnFile);
        selectLearn.registerAction(RActivateByType.RELEASE, () -> LoadedFileUI.createLearnUI(gui));
        RButton selectGrow = gui.button(selectGrowthFile);
        selectGrow.registerAction(RActivateByType.RELEASE, () -> LoadedFileUI.createGrowthUI(gui));
        // Tier 1b close
        gui.popWindow();
    }

//    private static void registerOpenFilesView(RotomGuiImpl gui) {
//        // Tier 1bc open
//        RFolder loaded = gui.pushPanel("Loaded Files", RBorderLayout.RLocation.LEFT);
//       // gui.getWinManager().uncoverOrCreateWindow(loaded,false, RLayoutStore.getCell(),RLayoutStore.getCell()*2,null);
//        // Tier 1c close
//        gui.popWindow();
//    }

    public static void buildFileDropdown(RotomGuiImpl gui) {
        // Tier 0a open
        gui.pushDropdown("File");

        registerNarcButton(gui);
        register2DFileButtons(gui);
        registerDataButtons(gui);

        // Tier 0a close
        gui.popWindow();
    }

//    public static void buildViewDropdown(RotomGuiImpl gui) {
//        // Tier 0b open
//        gui.pushDropdown("View");
//
//        registerOpenFilesView(gui);
//
//        // Tier 0b close
//        gui.popWindow();
//    }

    public static void buildOptionsDropdown(RotomGuiImpl gui) {
        // Tier 0c open
        gui.pushDropdown("Options");

        RToggle cellBounds =gui.toggle(showCellBounds, Configuration.isShowCellBounds());
        cellBounds.registerAction((b) -> {
            Configuration.setShowCellBounds(b);
            recolorImages(gui);
        });

        RToggle guidelines = gui.toggle(showGuidelines, Configuration.isShowGuidelines());
        guidelines.registerAction((b) -> {
            Configuration.setShowGuidelines(b);
            recolorImages(gui);
        });

        RToggle renderTransparent = gui.toggle(showTransparent, Configuration.isRenderTransparent());
        renderTransparent.registerAction((b) -> {
            Configuration.setRenderTransparent(b);
            recolorImages(gui);
        });

        RToggle renderBackground = gui.toggle(showBackground, Configuration.isBackground());
        renderBackground.registerAction((b) -> {
            Configuration.setRenderBackground(b);
            recolorImages(gui);
        });
        // Tier 0c close
        gui.popWindow();
    }

}
