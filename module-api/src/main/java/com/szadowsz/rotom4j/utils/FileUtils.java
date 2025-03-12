package com.szadowsz.rotom4j.utils;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.FilenameFilter;

/**
 * Utility Class to aid with processing of Files
 */
public class FileUtils {

    /**
     * An implementation of <code>FileFilter</code> which filters out files which do not have the specified file extensions
     */
    public static class ExtensionFilter extends FileFilter {
        private final String[] extensions;
        private final String description;

        /**
         * Creates a new <code>ExtensionFilter</code> which only shows files with the specified extensions
         *
         * @param description a <code>String</code> containing the description to show for the allowed file types
         * @param extensions  a <code>String[]</code> containing the allowed file types (including the "dot")
         */
        public ExtensionFilter(String description, String... extensions) {
            this.extensions = extensions;
            this.description = description;
        }


        @Override
        public boolean accept(File f) {
            for (String str : extensions) {
                if (f.getName().endsWith(str))
                    return true;
                else if (f.getName().endsWith(str))
                    return true;
                else if (f.isDirectory())
                    return true;
            }
            return false;
        }

        @Override
        public String getDescription() {
            StringBuilder extensions = new StringBuilder(" (");
            String extension;

            for (int i = 0; i < this.extensions.length; i++) {
                extension = this.extensions[i];
                extensions.append("*").append(extension);
                if (i != this.extensions.length - 1)
                    extensions.append(", ");
            }
            extensions.append(")");

            return description + extensions.toString();
        }
    }
    /**
     * An implementation of <code>FilenameFilter</code> which filters out files which do not have the specified file extensions
     */
    public static class ExtensionNameFilter implements FilenameFilter {
        private final String[] extensions;
        private final String description;

        /**
         * Creates a new <code>ExtensionNameFilter</code> which only shows files with the specified extensions
         *
         * @param description a <code>String</code> containing the description to show for the allowed file types
         * @param extensions  a <code>String[]</code> containing the allowed file types (including the "dot")
         */
        public ExtensionNameFilter(String description, String... extensions) {
            this.extensions = extensions;
            this.description = description;
        }


        @Override
        public boolean accept(File dir, String name) {
            File f = new File(dir, name);
            for (String str : extensions) {
                if (f.getName().endsWith(str))
                    return true;
                else if (f.getName().endsWith(str))
                    return true;
                else if (f.isDirectory())
                    return true;
            }
            return false;
        }
    }

    /**
     * The accepted file extensions for Nintendo DS Narcs
     */
    public static final String[] narcExtensions = {".narc", ".arc"};
    public static final String[] lstExtensions = {".lst"};
    public static final String[] defExtensions = {".h"};
    public static final String[] scrExtensions = {".scr"};
    public static final String[] naixExtensions = {".naix"};

    public static final String[] nanrExtensions = {".NANR"};
    public static final String[] ncerExtensions = {".NCER"};
    public static final String[] nscrExtensions = {".NSCR"};
    public static final String[] ncgrExtensions = {".NCGR", ".NCBR"};
    public static final String[] nclrExtensions = {".NCLR"};

    public static final String[] binExtensions = {".bin"};

    /**
     * A <code>ExtensionFilter</code> which displays only files with Nintendo DS ROM file extensions
     */
    public static final ExtensionFilter narcFilter = new ExtensionFilter("Nintendo DS Narc", narcExtensions);
    public static final ExtensionFilter lstFilter = new ExtensionFilter("Nintendo lst File", lstExtensions);
    public static final ExtensionFilter defFilter = new ExtensionFilter("Nintendo .h File", defExtensions);
    public static final ExtensionFilter scrFilter = new ExtensionFilter("Nintendo .scr File", scrExtensions);
    public static final ExtensionFilter naixFilter = new ExtensionFilter("Nintendo .naix File", naixExtensions);

    public static final ExtensionFilter nanrFilter = new ExtensionFilter("Nintendo .NANR File", nanrExtensions);
    public static final ExtensionFilter ncerFilter = new ExtensionFilter("Nintendo .NCER File", ncerExtensions);
    public static final ExtensionFilter nscrFilter = new ExtensionFilter("Nintendo DS NSCR", nscrExtensions);
    public static final ExtensionFilter ncgrFilter = new ExtensionFilter("Nintendo DS NCGR", ncgrExtensions);
    public static final ExtensionFilter nclrFilter = new ExtensionFilter("Nintendo DS NCLR", nclrExtensions);
    public static final ExtensionFilter binFilter = new ExtensionFilter("Bin File", binExtensions);
    /**
     * A <code>ExtensionNameFilter</code> which displays only files with Nintendo DS ROM file extensions
     */
    public static final ExtensionNameFilter narcNameFilter = new ExtensionNameFilter("Nintendo DS Narc", narcExtensions);
    public static final ExtensionNameFilter lstNameFilter = new ExtensionNameFilter("Nintendo lst File", lstExtensions);
    public static final ExtensionNameFilter defNameFilter = new ExtensionNameFilter("Nintendo .h File", defExtensions);
    public static final ExtensionNameFilter scrNameFilter = new ExtensionNameFilter("Nintendo .scr File", scrExtensions);
    public static final ExtensionNameFilter naixNameFilter = new ExtensionNameFilter("Nintendo .naix File", naixExtensions);

    public static final ExtensionNameFilter nanrNameFilter = new ExtensionNameFilter("Nintendo .NANR File", nanrExtensions);
    public static final ExtensionNameFilter ncerNameFilter = new ExtensionNameFilter("Nintendo .NCER File", ncerExtensions);
    public static final ExtensionNameFilter nscrNameFilter = new ExtensionNameFilter("Nintendo .NSCR File", nscrExtensions);
    public static final ExtensionNameFilter ncgrNameFilter = new ExtensionNameFilter("Nintendo .NCGR File", ncgrExtensions);
    public static final ExtensionNameFilter nclrNameFilter = new ExtensionNameFilter("Nintendo .NCLR File", nclrExtensions);
    public static final ExtensionNameFilter binNameFilter = new ExtensionNameFilter("Bin File", binExtensions);
}
