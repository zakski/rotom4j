package com.szadowsz.rotom4j.app.utils;

import com.szadowsz.nds4j.file.index.LstFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;

import static com.szadowsz.nds4j.utils.FileUtils.*;
import static processing.core.PConstants.MACOS;

public class FileChooser {

    static Logger LOGGER = LoggerFactory.getLogger(FileChooser.class);

    /**
     * Find the Frame associated with this object.
     *
     * @param owner the object that is responsible for this message
     * @return the frame (if any) that owns this object else return a new invisible one
     */
    private static Frame getFrame(PApplet owner) {
        Frame frame = null;
        try {
            frame = ((processing.awt.PSurfaceAWT.SmoothCanvas) owner.getSurface().getNative()).getFrame();
        } catch (Exception e) {
            frame = new JFrame("File chooser in frame");
            frame.pack();

            ((JFrame) frame).setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setAlwaysOnTop(true);
        }
        return frame;
    }

    /**
     *
     * @param frame Frame to attach the dialog to
     * @param prompt File Dialog Prompt Title
     * @param nameFilter filter files based on extensions
     * @param lastPath last path looked at by file selection dialog
     * @return File that was selected
     */
    private static File getFileOnMac(Frame frame, String prompt, ExtensionNameFilter nameFilter, String lastPath) {
        FileDialog fileDialog = new FileDialog(frame, prompt, FileDialog.LOAD);
        fileDialog.setFilenameFilter(nameFilter);
        fileDialog.setDirectory(lastPath);
        System.setProperty("apple.awt.fileDialogForDirectories", "true");
        // Make visible and wait for user input
        fileDialog.setVisible(true);
        // Reset this property for later dialogs
        System.setProperty("apple.awt.fileDialogForDirectories", "false");
        String filename = fileDialog.getFile();
        if (filename != null) {
            return new File(fileDialog.getDirectory(), fileDialog.getFile());
        }
        return null;
    }


    /**
     *
     *
     * @param frame Frame to attach the dialog to
     * @param prompt File Dialog Prompt Title
     * @param filter filter files based on extensions
     * @param lastPath last path looked at by file selection dialog
     * @return File that was selected
     */
    private static File getFileOnOther(Frame frame, String prompt, ExtensionFilter filter, String lastPath) {
        JFileChooser fileChooser = new JFileChooser(lastPath);
        fileChooser.setDialogTitle(prompt);
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setFileFilter(filter);
        fileChooser.grabFocus();
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    /**
     * Select a Folder from the local file system.
     *
     * @param sketchWindow parent processing window
     * @param lastPath last path looked at by file selection dialog
     * @param prompt File Dialog Prompt Title
     * @return the absolute path name for the selected folder, or null if action cancelled.
     */
    public static String selectFolder(PApplet sketchWindow, String lastPath, String prompt) {
        String selectedFolder = null;
        Frame frame = getFrame(sketchWindow);
        // Frame frame = (sketchWindow == null) ? null : sketchWindow.frame;
        if (PApplet.platform == MACOS) { // && PApplet.useNativeSelect != false) {
            FileDialog fileDialog = new FileDialog(frame, prompt, FileDialog.LOAD);
            // Make visible and wait for user input
            fileDialog.setDirectory(lastPath);
            fileDialog.setVisible(true);
            // Reset this property for later dialogs
            System.setProperty("apple.awt.fileDialogForDirectories", "false");
            String filename = fileDialog.getFile();
            if (filename != null) {
                try {
                    selectedFolder = (new File(fileDialog.getDirectory(), fileDialog.getFile())).getCanonicalPath();
                } catch (IOException ignored) {
                }
            }
        } else {
            JFileChooser fileChooser = new JFileChooser(lastPath);
            fileChooser.setDialogTitle(prompt);
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    selectedFolder = fileChooser.getSelectedFile().getCanonicalPath();
                } catch (IOException ignored) {
                }
            }
        }
        return selectedFolder;
    }

    /**
     * Select a Narc file from the local file system.
     *
     * @param sketchWindow parent processing window
     * @param lastPath last path looked at by file selection dialog
     * @param prompt File Dialog Prompt Title
     * @return the absolute path name for the selected Narc, or null if action cancelled.
     */
    public static String selectNarcFile(PApplet sketchWindow, String lastPath, String prompt) {
        LOGGER.info("Selecting Narc File");
        File selectedFile = null;
        Frame frame = getFrame(sketchWindow);
        // Frame frame = (sketchWindow == null) ? null : sketchWindow.frame;
        if (PApplet.platform == MACOS) { // && PApplet.useNativeSelect != false) {
            selectedFile = getFileOnMac(frame, prompt, narcNameFilter, lastPath);
        } else {
            selectedFile = getFileOnOther(frame, prompt, narcFilter, lastPath);
        }
        if (selectedFile != null) {
            LOGGER.info("Selected Narc File: " + selectedFile.getAbsolutePath());
            return selectedFile.getAbsolutePath();
        } else {
            LOGGER.info("No Narc File Selected");
            return null;
        }
    }

    /**
     * Select a NANR file from the local file system.
     *
     * @param sketchWindow parent processing window
     * @param lastPath last path looked at by file selection dialog
     * @param prompt File Dialog Prompt Title
     * @return the absolute path name for the selected NANR, or null if action cancelled.
     */
    public static String selectNanrFile(PApplet sketchWindow, String lastPath, String prompt) {
        LOGGER.info("Selecting NANR File");
        File selectedFile = null;
        Frame frame = getFrame(sketchWindow);
        // Frame frame = (sketchWindow == null) ? null : sketchWindow.frame;
        if (PApplet.platform == MACOS) { // && PApplet.useNativeSelect != false) {
            selectedFile = getFileOnMac(frame, prompt, nanrNameFilter, lastPath);
        } else {
            selectedFile = getFileOnOther(frame, prompt, nanrFilter, lastPath);
        }
        if (selectedFile != null) {
            LOGGER.info("Selected NANR File: " + selectedFile.getAbsolutePath());
            return selectedFile.getAbsolutePath();
        } else {
            LOGGER.info("No NANR File Selected");
            return null;
        }
    }

    /**
     * Select a NSCR file from the local file system.
     *
     * @param sketchWindow parent processing window
     * @param lastPath last path looked at by file selection dialog
     * @param prompt File Dialog Prompt Title
     * @return the absolute path name for the selected NSCR, or null if action cancelled.
     */
    public static String selectNcerFile(PApplet sketchWindow, String lastPath, String prompt) {
        LOGGER.info("Selecting NCER File");
        File selectedFile = null;
        Frame frame = getFrame(sketchWindow);
        // Frame frame = (sketchWindow == null) ? null : sketchWindow.frame;
        if (PApplet.platform == MACOS) { // && PApplet.useNativeSelect != false) {
            selectedFile = getFileOnMac(frame, prompt, ncerNameFilter, lastPath);
        } else {
            selectedFile = getFileOnOther(frame, prompt, ncerFilter, lastPath);
        }
        if (selectedFile != null) {
            LOGGER.info("Selected NCER File: " + selectedFile.getAbsolutePath());
            return selectedFile.getAbsolutePath();
        } else {
            LOGGER.info("No NCER File Selected");
            return null;
        }
    }

    /**
     * Select a NSCR file from the local file system.
     *
     * @param sketchWindow parent processing window
     * @param lastPath last path looked at by file selection dialog
     * @param prompt File Dialog Prompt Title
     * @return the absolute path name for the selected NSCR, or null if action cancelled.
     */
    public static String selectNscrFile(PApplet sketchWindow, String lastPath, String prompt) {
        LOGGER.info("Selecting NSCR File");
        File selectedFile = null;
        Frame frame = getFrame(sketchWindow);
        // Frame frame = (sketchWindow == null) ? null : sketchWindow.frame;
        if (PApplet.platform == MACOS) { // && PApplet.useNativeSelect != false) {
            selectedFile = getFileOnMac(frame, prompt, nscrNameFilter, lastPath);
        } else {
            selectedFile = getFileOnOther(frame, prompt, nscrFilter, lastPath);
        }
        if (selectedFile != null) {
            LOGGER.info("Selected NSCR File: " + selectedFile.getAbsolutePath());
            return selectedFile.getAbsolutePath();
        } else {
            LOGGER.info("No NSCR File Selected");
            return null;
        }
    }

    /**
     * Select a NCGR file from the local file system.
     *
     * @param sketchWindow parent processing window
     * @param lastPath last path looked at by file selection dialog
     * @param prompt File Dialog Prompt Title
     * @return the absolute path name for the selected NCGR, or null if action cancelled.
     */
    public static String selectNcgrFile(PApplet sketchWindow, String lastPath, String prompt) {
        LOGGER.info("Selecting NCGR File");
        File selectedFile = null;
        Frame frame = getFrame(sketchWindow);
        // Frame frame = (sketchWindow == null) ? null : sketchWindow.frame;
        if (PApplet.platform == MACOS) { // && PApplet.useNativeSelect != false) {
            selectedFile = getFileOnMac(frame, prompt, ncgrNameFilter, lastPath);
        } else {
            selectedFile = getFileOnOther(frame, prompt, ncgrFilter, lastPath);
        }
        if (selectedFile != null) {
            LOGGER.info("Selected NCGR File: " + selectedFile.getAbsolutePath());
            return selectedFile.getAbsolutePath();
        } else {
            LOGGER.info("No NCGR File Selected");
            return null;
        }
    }

    /**
     * Select a NCLR file from the local file system.
     *
     * @param sketchWindow parent processing window
     * @param lastPath last path looked at by file selection dialog
     * @param prompt File Dialog Prompt Title
     * @return the absolute path name for the selected NCLR, or null if action cancelled.
     */
    public static String selectNclrFile(PApplet sketchWindow, String lastPath, String prompt) {
        LOGGER.info("Selecting NCLR File");
        File selectedFile = null;
        Frame frame = getFrame(sketchWindow);
        // Frame frame = (sketchWindow == null) ? null : sketchWindow.frame;
        if (PApplet.platform == MACOS) { // && PApplet.useNativeSelect != false) {
            selectedFile = getFileOnMac(frame, prompt, nclrNameFilter, lastPath);
        } else {
            selectedFile = getFileOnOther(frame, prompt, nclrFilter, lastPath);
        }
        if (selectedFile != null) {
            LOGGER.info("Selected NCLR File: " + selectedFile.getAbsolutePath());
            return selectedFile.getAbsolutePath();
        } else {
            LOGGER.info("No NCLR File Selected");
            return null;
        }
    }

    /**
     * Select a .lst file from the local file system.
     *
     * @param prompt the frame text for the chooser
     * @return the absolute path name for the selected .lst, or null if action cancelled.
     */
    public static String selectLstFile(PApplet sketchWindow, String lastPath, String prompt) {
        LOGGER.info("Selecting .lst File");
        File selectedFile = null;
        Frame frame = getFrame(sketchWindow);
        // Frame frame = (sketchWindow == null) ? null : sketchWindow.frame;
        if (PApplet.platform == MACOS) { // && PApplet.useNativeSelect != false) {
            selectedFile = getFileOnMac(frame, prompt, lstNameFilter, lastPath);
        } else {
            selectedFile = getFileOnOther(frame, prompt, lstFilter, lastPath);
        }
        if (selectedFile != null) {
            LOGGER.info("Selected Lst File: " + selectedFile.getAbsolutePath());
            return selectedFile.getAbsolutePath();
        } else {
            LOGGER.info("No Lst File Selected");
            return null;
        }
    }


    /**
     * Save a .lst file to the local file system.
     *
     * @param sketchWindow parent processing window
     * @param lastPath last path looked at by file selection dialog
     * @param prompt File Dialog Prompt Title
     * @param lstFile the lst file to save
     * @return saved file's path
     */
    public static String saveLstFile(PApplet sketchWindow, String lastPath, String prompt, LstFile lstFile) {
        LOGGER.info("Creating .lst File");
        File savedFile = null;
        Frame frame = getFrame(sketchWindow);
        // Frame frame = (sketchWindow == null) ? null : sketchWindow.frame;
        if (PApplet.platform == MACOS) { // && PApplet.useNativeSelect != false) {
            savedFile = getFileOnMac(frame, prompt, lstNameFilter, lastPath);
        } else {
            JFileChooser fileChooser = new JFileChooser(lastPath);
            fileChooser.setDialogTitle(prompt);
            fileChooser.setAcceptAllFileFilterUsed(true);
            fileChooser.setFileFilter(lstFilter);
            int result = fileChooser.showSaveDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                savedFile = fileChooser.getSelectedFile();
            }
        }
        if (savedFile != null) {
            LOGGER.info("Saved Lst File: " + savedFile.getAbsolutePath());
            try (FileOutputStream out = new FileOutputStream(savedFile);
                 OutputStreamWriter writer = new OutputStreamWriter(out, "SHIFT_JIS");
                 BufferedWriter buf = new BufferedWriter(writer)) {
                List<String> lines = lstFile.getOutputList();
                for(int i = 0; i < lines.size();i++){
                    buf.write(lines.get(i));
                    if (i < lines.size()-1){
                        buf.newLine();
                    }

                }
                buf.flush();
             } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return savedFile.getAbsolutePath();
        } else {
            LOGGER.info("No Lst File Selected");
            return null;
        }
    }

   /**
     * Select a .h file from the local file system.
     *
     * @param sketchWindow parent processing window
     * @param lastPath last path looked at by file selection dialog
     * @param prompt File Dialog Prompt Title
     * @return the absolute path name for the selected .h, or null if action cancelled.
     */
    public static String selectDefFile(PApplet sketchWindow, String lastPath, String prompt) {
        LOGGER.info("Selecting .h File");
        File selectedFile = null;
        Frame frame = getFrame(sketchWindow);
        // Frame frame = (sketchWindow == null) ? null : sketchWindow.frame;
        if (PApplet.platform == MACOS) { // && PApplet.useNativeSelect != false) {
            selectedFile = getFileOnMac(frame, prompt, defNameFilter, lastPath);
        } else {
            selectedFile = getFileOnOther(frame, prompt, defFilter, lastPath);
        }
        if (selectedFile != null) {
            LOGGER.info("Selected .h File: " + selectedFile.getAbsolutePath());
            return selectedFile.getAbsolutePath();
        } else {
            LOGGER.info("No .h File Selected");
            return null;
        }
    }

    /**
     * Select a .scr file from the local file system.
     *
     * @param sketchWindow parent processing window
     * @param lastPath last path looked at by file selection dialog
     * @param prompt File Dialog Prompt Title
     * @return the absolute path name for the selected .scr, or null if action cancelled.
     */
    public static String selectScrFile(PApplet sketchWindow, String lastPath, String prompt) {
        LOGGER.info("Selecting .scr File");
        File selectedFile = null;
        Frame frame = getFrame(sketchWindow);
        // Frame frame = (sketchWindow == null) ? null : sketchWindow.frame;
        if (PApplet.platform == MACOS) { // && PApplet.useNativeSelect != false) {
            selectedFile = getFileOnMac(frame, prompt, scrNameFilter, lastPath);
        } else {
            selectedFile = getFileOnOther(frame, prompt, scrFilter, lastPath);
        }
        if (selectedFile != null) {
            LOGGER.info("Selected .scr File: " + selectedFile.getAbsolutePath());
            return selectedFile.getAbsolutePath();
        } else {
            LOGGER.info("No .scr File Selected");
            return null;
        }
    }

    /**
     * Select a .naix file from the local file system.
     *
     * @param sketchWindow parent processing window
     * @param lastPath last path looked at by file selection dialog
     * @param prompt File Dialog Prompt Title
     * @return the absolute path name for the selected .naix, or null if action cancelled.
     */
    public static String selectNaixFile(PApplet sketchWindow, String lastPath, String prompt) {
        LOGGER.info("Selecting .naix File");
        File selectedFile = null;
        Frame frame = getFrame(sketchWindow);
        // Frame frame = (sketchWindow == null) ? null : sketchWindow.frame;
        if (PApplet.platform == MACOS) { // && PApplet.useNativeSelect != false) {
            selectedFile = getFileOnMac(frame, prompt, naixNameFilter, lastPath);
        } else {
            selectedFile = getFileOnOther(frame, prompt, naixFilter, lastPath);
        }
        if (selectedFile != null) {
            LOGGER.info("Selected .naix File: " + selectedFile.getAbsolutePath());
            return selectedFile.getAbsolutePath();
        } else {
            LOGGER.info("No .naix File Selected");
            return null;
        }
    }

    /**
     * Select a .bin file from the local file system.
     *
     * @param sketchWindow parent processing window
     * @param lastPath last path looked at by file selection dialog
     * @param prompt File Dialog Prompt Title
     * @return the absolute path name for the selected .naix, or null if action cancelled.
     */
    public static String selectBinFile(PApplet sketchWindow, String lastPath, String prompt) {
        LOGGER.info("Selecting .bin File");
        File selectedFile = null;
        Frame frame = getFrame(sketchWindow);
        // Frame frame = (sketchWindow == null) ? null : sketchWindow.frame;
        if (PApplet.platform == MACOS) { // && PApplet.useNativeSelect != false) {
            selectedFile = getFileOnMac(frame, prompt, binNameFilter, lastPath);
        } else {
            selectedFile = getFileOnOther(frame, prompt, binFilter, lastPath);
        }
        if (selectedFile != null) {
            LOGGER.info("Selected .bin File: " + selectedFile.getAbsolutePath());
            return selectedFile.getAbsolutePath();
        } else {
            LOGGER.info("No .bin File Selected");
            return null;
        }
    }
}
