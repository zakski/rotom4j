package com.szadowsz.rotom4j.file;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum to Represent Different Types Of Files
 * <p>
 * Archive-related:
 *      NARC (Nitro ARChive)                            → Basically a Zip File
 * Data-related:
 *      bin/dat                                         → See BinFormat                 -> BinNFSFile class
 * 2D-related:
 *      NCLR (Nitro CoLoR)                              → Color Palette                 -> NCLR class
 *      NCGR/NCBR (Nitro Character Graphic Resource)    → Graphical Tiles               -> NCGR class
 *      NBGR (Nitro Basic Graphic Resource)             → Graphical Tiles               -> NCGR class
 *      NSCR (Nitro SCreen Resource)                    → Maps/Images                   -> NSCR class
 *      NCER (Nitro CEll Resource)                      → Tile Arrangement Information
 *      NANR (Nitro ANimation Resource)                 → Animation Data
 *      NFTR (Nitro Font Table Resource)                → Fonts
 * 3D-related:
 *      NSBMD (Nitro Sdk Binary Model Data)             → 3D Polygonal Model data
 *      NSBTX (Nitro Sdk Binary TeXture)                → Texture image and palette data
 *      NSBCA (Nitro Sdk Binary Character Animation)    → Skeletal animation data
 *      NSBTP (Nitro Sdk Binary Texture Pattern)        → Texture-swapping animations
 *      NSBTA (Nitro Sdk Binary Texture Animation)      → UV-change animations
 *      NSBMA (Nitro Sdk Binary Material Animation)     → Material-change animations
 *      NSBVA (Nitro Sdk Binary Visibility Animation)   → Visibility animations
 *      SPA (Unknown) → Particles
 */
public enum RotomFormat {
    // Archive-related
    NARC(new String[]{"NARC"},new String[]{"NARC"}),
    // Data-related
    BINARY(new String[]{"BIN"},new String[]{"bin","dat", "spa", "resdat"}),
    // 2D-related
    NCER(new String[]{"RECN"},new String[]{"NCER"}),
    NCLR(new String[]{"RLCN"}, new String[]{"NCLR"}),
    NSCR(new String[]{"RCSN"},new String[]{"NSCR"}),
    NCGR(new String[]{"RGCN"}, new String[]{"NCGR","NCBR"}),
    NANR(new String[]{"RNAN"}, new String[]{"NANR"}),
    NSBCA(new String[]{"BCA0"}, new String[]{"NSBCA"}),
    NSBMD(new String[]{"BMD0"}, new String[]{"NSBMD"});


    // Mapping of Format by label encoded into the Nitro File Header (Binary Files do not have a header)
    private static final Map<String, RotomFormat> BY_LABEL = new HashMap<>();
    // Mapping of Format by File name extension
    private static final Map<String, RotomFormat> BY_EXT = new HashMap<>();

    // Initialise static maps
    static {
        for (RotomFormat e: values()) {
            for (String magic: e.magic) {
                BY_LABEL.put(magic,e);
            }
            for (String ext: e.ext) {
                BY_EXT.put(ext,e);
            }
        }
    }

    private final String[] magic;
    private final String[] ext;

    /**
     * Nitro Format Enum Constructor
     *
     * @param magic list of valid Nitro File Header encoding labels
     * @param extension list of valid Nitro File Header extensions
     */
    RotomFormat(String[] magic, String[] extension) {
        this.magic = magic;
        this.ext = extension;
    }

    /**
     * get valid labels from Nitro File Header encoding
     *
     * @return a list of valid labels
     */
    public String[] getLabel() {
        return magic;
    }

    /**
     * get valid file extensions of Nitro File Format
     *
     * @return a list of valid file extensions
     */
    public String[] getExt() {
        return ext;
    }

    /**
     * Get the index of the file extension we are using
     *
     * @param extension the extension to check
     * @return the index of the extension
     */
    public int getExtIndex(String extension) {
        for (int i = 0; i < ext.length;i++){
            if (ext[i].equalsIgnoreCase(extension)){
                return i;
            }
        }
        return 0;
    }

    /**
     * Get the relevant Nitro Format from the Nitro File Header Label
     *
     * @param label the label we are using
     * @return corresponding Nitro Format
     */
    public static RotomFormat valueOfLabel(String label) {
        return BY_LABEL.get(label);
    }

    /**
     * Get the relevant Nitro Format from the Nitro File extension
     *
     * @param ext the file extension we are using
     * @return corresponding Nitro Format
     */
    public static RotomFormat valueOfExt(String ext) {
        RotomFormat format = BY_EXT.get(ext);
        if (format == null){
            format = BY_EXT.get(ext.toLowerCase());
        }
        if (format == null){
            format = BY_EXT.get(ext.toUpperCase());
        }
        return format;
    }
}