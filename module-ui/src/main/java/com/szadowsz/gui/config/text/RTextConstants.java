package com.szadowsz.gui.config.text;

import java.awt.*;
import java.awt.font.TextAttribute;

public class RTextConstants {

    private RTextConstants() {}

    // used to filter in GTextField
    public final static int INTEGER 		= 0;
    public final static int DECIMAL 		= 1;
    public final static int EXPONENT 		= 2;

    // Font style
    public final static int PLAIN 	= Font.PLAIN;
    public final static int BOLD 	= Font.BOLD;
    public final static int ITALIC	= Font.ITALIC;

    // Attribute:- font weight   Value Type:- Float in range (0.5 to 2.75)
    public final static TextAttribute WEIGHT = TextAttribute.WEIGHT;
    // Predefined constants for font weight
    public final static Float WEIGHT_EXTRA_LIGHT 	= 0.5f;
    public final static  Float WEIGHT_LIGHT 			= 0.75f;
    public final static Float WEIGHT_DEMILIGHT 		= 0.875f;
    public final static Float WEIGHT_REGULAR 		= 1.0f;
    public final static Float WEIGHT_SEMIBOLD 		= 1.25f;
    public final static Float WEIGHT_MEDIUM 		= 1.5f;
    public final static Float WEIGHT_DEMIBOLD 		= 1.75f;
    public final static Float WEIGHT_BOLD 			= 2.0f;
    public final static Float WEIGHT_HEAVY 			= 2.25f;
    public final static Float WEIGHT_EXTRABOLD 		= 2.5f;
    public final static Float WEIGHT_ULTRABOLD 		= 2.75f;

    // Attribute:- font width   Value Type:- Float in range (0.75 to 1.5)
    public final static TextAttribute WIDTH 		= TextAttribute.WIDTH;
    // Predefined constants for font width
    public final static Float WIDTH_CONDENSED 		= 0.75f;
    public final static Float WIDTH_SEMI_CONDENSED 	= 0.875f;
    public final static Float WIDTH_REGULAR 		= 1.0f;
    public final static Float WIDTH_SEMI_EXTENDED 	= 1.25f;
    public final static Float WIDTH_EXTENDED 		= 1.5f;

    // Attribute:- font posture   Value Type:- Float in range (0.0 to 0.20)
    public final static TextAttribute POSTURE 		= TextAttribute.POSTURE;
    // Predefined constants for font posture (plain or italic)
    public final static Float POSTURE_REGULAR 		= 0.0f;
    public final static Float POSTURE_OBLIQUE 		= 0.20f;

    // Attribute:- font superscript   Value Type:- Integer (1 : super or -1 subscript)
    public final static TextAttribute SUPERSCRIPT 	= TextAttribute.SUPERSCRIPT;
    // Predefined constants for font super/subscript
    public final static Integer SUPERSCRIPT_SUPER 	= 1;
    public final static Integer SUPERSCRIPT_SUB 	= -1;
    public final static Integer SUPERSCRIPT_OFF 	= 0;

    // Attribute:- font foreground and background colour   Value Type:- Color
    public final static TextAttribute FOREGROUND 	= TextAttribute.FOREGROUND;
    public final static TextAttribute BACKGROUND 	= TextAttribute.BACKGROUND;

    // Attribute:- font strike through   Value:- Boolean
    public final static TextAttribute STRIKETHROUGH = TextAttribute.STRIKETHROUGH;
    // Predefined constants for font strike through on/off
    public final static Boolean STRIKETHROUGH_ON 	= true;
    public final static Boolean STRIKETHROUGH_OFF 	= false;

    // Constants for merging attribute runs
    public final static int I_NONE 		= 0;
    public final static int I_TL 		= 1;
    public final static int I_TR		= 2;
    public final static int I_CL		= 4;
    public final static int I_CR		= 8;
    public final static int I_INSIDE	= 16;
    public final static int I_COVERED	= 32;
    public final static int I_MODES		= 63;

    // Merger action
    public final static int MERGE_RUNS		= 256;
    public final static int CLIP_RUN		= 512;
    public final static int COMBI_MODES		= 768;

    // merger decision grid
    public final static int[][] grid = new int[][] {
            { I_NONE,	I_TL,		I_CL,		I_COVERED,	I_COVERED },
            { I_NONE,	I_NONE, 	I_INSIDE, 	I_INSIDE, 	I_COVERED },
            { I_NONE,	I_NONE, 	I_INSIDE, 	I_INSIDE, 	I_CR },
            { I_NONE,	I_NONE, 	I_NONE, 	I_NONE, 	I_TR },
            { I_NONE,	I_NONE, 	I_NONE, 	I_NONE, 	I_NONE }
    };
}
