package com.szadowsz.gui.component.bined;

import com.szadowsz.gui.component.bined.settings.CodeAreaSection;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;

import java.awt.*;

public class RBinColorAssessor {

    protected CodeAreaSection activeSection = null;
    protected int codeLastCharPos;
    protected Color selectionColor;
    protected Color selectionMirrorColor;
    protected Color selectionBackground;
    protected Color selectionMirrorBackground;

    public Color getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean inSelection) {
        if (inSelection && (section == CodeAreaSection.CODE_MATRIX)) {
            if (charOnRow == codeLastCharPos) {
                inSelection = false;
            }
        }

        if (inSelection) {
            return section == activeSection ? selectionBackground : selectionMirrorBackground;
        }

        return null;
    }

    public Color getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean inSelection) {
        if (inSelection) {
            return section == activeSection ? selectionColor : selectionMirrorColor;
        }

        return null;
    }

    public void update(RBinEditor editor) {
        activeSection = editor.getActiveSection();
        codeLastCharPos = editor.getCodeLastCharPos();
        //CodeAreaColorsProfile colorsProfile = codeAreaPainterState.getColorsProfile();
        selectionColor = RThemeStore.getColor(RColorType.FOCUS_FOREGROUND); //colorsProfile.getColor(CodeAreaBasicColors.SELECTION_COLOR);
        selectionMirrorColor = RThemeStore.getColor(RColorType.FOCUS_FOREGROUND); //colorsProfile.getColor(CodeAreaBasicColors.SELECTION_MIRROR_COLOR);
        selectionBackground = RThemeStore.getColor(RColorType.FOCUS_BACKGROUND);//colorsProfile.getColor(CodeAreaBasicColors.SELECTION_BACKGROUND);
        selectionMirrorBackground = RThemeStore.getColor(RColorType.FOCUS_BACKGROUND); //colorsProfile.getColor(CodeAreaBasicColors.SELECTION_MIRROR_BACKGROUND);
    }
}