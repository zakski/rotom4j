/*
 * Copyright (c) 2023 Turtleisaac.
 *
 * This file is part of Nds4j.
 *
 * Nds4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nds4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nds4j. If not, see <https://www.gnu.org/licenses/>.
 */

package com.szadowsz.nds4j.file.nitro.narc;

import com.szadowsz.nds4j.NFSFactory;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.NFSFormat;
import com.szadowsz.nds4j.file.nitro.narc.data.Fnt;
import com.szadowsz.nds4j.file.BaseNFSFile;
import com.szadowsz.nds4j.file.index.DefHeaderFile;
import com.szadowsz.nds4j.file.index.LstFile;
import com.szadowsz.nds4j.file.index.NaixFile;
import com.szadowsz.nds4j.file.index.ScrFile;
import com.szadowsz.nds4j.file.nitro.GenericNFSFile;
import com.szadowsz.nds4j.reader.Buffer;
import com.szadowsz.nds4j.reader.MemBuf;
import com.szadowsz.nds4j.utils.StringFormatter;
import com.szadowsz.nds4j.writer.BinaryWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * An object representation of a NARC file (Nitro-Archive)
 */
public class NARC extends GenericNFSFile {
    static Logger LOGGER = LoggerFactory.getLogger(NARC.class);

    public static final int FATB_HEADER_SIZE = 0x0C;
    public static final int FIMG_HEADER_SIZE = 8;
    public static final int FNTB_HEADER_SIZE = 8;
    protected Fnt.Folder filenames; // represents the root folder of the filesystem
    protected ArrayList<byte[]> rawFiles;
    protected ArrayList<BaseNFSFile> files;

    /**
     * Read NARC data, and create a filename table and a list of files.
     *
     * @param file file obj information
     * @param data byte[] representation of a Narc
     * @throws IOException if there are issues reading the data/file
     */
    public NARC(File file, byte[] data) throws IOException {
        super(NFSFormat.NARC, (file!=null)?file.getName():"",data);
        MemBuf buf = MemBuf.create(rawData);
        MemBuf.MemBufReader reader = buf.reader();
        readGenericNtrHeader(NFSFormat.NARC,reader);
        readFile(reader);
    }

    /**
     * Parse the data of the file body
     *
     * @param reader the byte reader to access the file body data
     */
    @Override
    protected void readFile(MemBuf.MemBufReader reader){
        filenames = new Fnt.Folder();
        if (version != 1) {
            throw new RuntimeException("Unsupported NARC version: " + version);
        }

        // Read the file allocation block (current position is now 0x10)
        String fatbMagic = reader.readString(4);
        long fatbSize = reader.readUInt32();
        long numFiles = reader.readUInt32();

        int fatbStart = reader.getPosition();

        if (!fatbMagic.equals("BTAF")) {
            throw new RuntimeException("Incorrect NARC FATB magic: " + fatbMagic);
        }

        // read the file name block
        long fntbOffset = NTR_HEADER_SIZE + fatbSize;
        reader.setPosition(fntbOffset);
        String fntbMagic = reader.readString(4);
        long fntbSize = reader.readUInt32();

        if (!fntbMagic.equals("BTNF")) {
            throw new RuntimeException("Incorrect NARC FNTB magic: " + fntbMagic);
        }

        // get the data from the file data block before continuing
        long fimgOffset = fntbOffset + fntbSize;
        reader.setPosition(fimgOffset);
        String fimgMagic = reader.readString(4);
        long fimgSize = reader.readUInt32();

        if (!fimgMagic.equals("GMIF")) {
            throw new RuntimeException("Incorrect NARC FIMG magic: " + fimgMagic);
        }

        int rawDataOffset = reader.getPosition(); // fimgOffset + 8
        rawFiles = new ArrayList<>();
        files = new ArrayList<>();

        // read the files' contents
        long startOffset;
        long endOffset;
        LOGGER.info("Parsing File Contents for " + getFileName());
        for (int i = 0; i < numFiles; i++) {
            LOGGER.info("Reading File index " + i + " of " + numFiles);
            reader.setPosition(fatbStart + 8*i);
            startOffset = reader.readUInt32();
            endOffset = reader.readUInt32();
            reader.setPosition(rawDataOffset + startOffset);
            rawFiles.add(reader.readTo(rawDataOffset + endOffset));
            try {
               files.add(NFSFactory.fromNarc(getFileNameWithoutExt(),i,numFiles,rawFiles.get(i)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // parse the filenames
        reader.setPosition(fntbOffset + 8);
        filenames = Fnt.load(reader.readBytes((int) fntbSize));
    }

    /**
     * Load a NARC archive from a filesystem file
     * @param file a String containing the path to a NARC file on disk
     * @return a Narc object
     */
    public static NARC fromFile(String file) throws IOException {
        return fromFile(new File(file));
    }

    /**
     * Load a NARC archive from a file on disk
     * @param file a File object representing the path to a NARC file on disk
     * @return a Narc object
     */
    public static NARC fromFile(File file) throws IOException {
        return new NARC(file, Buffer.readFile(file.getAbsolutePath()));
    }

    /**
     * Unpacks this Narc to disk at the specified path
     * @param dir a String containing the target directory to unpack the NARC to
     * @exception RuntimeException if this Narc has an internal filesystem, the specified path
     * already exists, a new directory at the specified path could not be created, or a failure to write the
     * subfiles occurred
     * @exception IOException if the parent directory of the output subfiles does not exist
     */
    public void unpack(String dir) throws IOException {
        if (dir != null) {
            unpack(new File(dir));
        }
    }

    /**
     * Unpacks this Narc to disk at the specified path
     * @param dir a String containing the target directory to unpack the NARC to
     * @exception RuntimeException if this Narc has an internal filesystem, the specified path
     * already exists, a new directory at the specified path could not be created, or a failure to write the
     * subfiles occurred
     * @exception IOException if the parent directory of the output subfiles does not exist
     */
    public void unpackWithCompression(String dir) throws IOException {
        if (dir != null){
            unpackWithCompression(new File(dir));
        }
    }


    /**
     * Unpacks this Narc to disk at the specified path
     * @param dir a File containing the target directory to unpack the NARC to
     * @exception RuntimeException if this Narc has an internal filesystem, the specified path
     * already exists, a new directory at the specified path could not be created, or a failure to write the
     * subfiles occurred
     * @exception IOException if the parent directory of the output subfiles does not exist
     */
    public void unpack(File dir) throws IOException {
        if (!filenames.getFiles().isEmpty()) {
            throw new RuntimeException("Unpacking of NARCs with internal filesystems not yet supported");
        }

        if (!dir.exists() && !dir.mkdir()) {
            throw new RuntimeException("Failed to create output directory, check write permissions.");
        }

        for (int i = 0; i < files.size(); i++) {
            System.out.println(StringFormatter.formatOutputString(i, files.size(), "", ""));
            BinaryWriter.writeFile(Paths.get(dir.getAbsolutePath(),files.get(i).getFileName()), files.get(i).getData());
        }
    }

    /**
     * Unpacks this Narc to disk at the specified path
     * @param dir a File containing the target directory to unpack the NARC to
     * @exception RuntimeException if this Narc has an internal filesystem, the specified path
     * already exists, a new directory at the specified path could not be created, or a failure to write the
     * subfiles occurred
     * @exception IOException if the parent directory of the output subfiles does not exist
     */
    public void unpackWithCompression(File dir) throws IOException {
        if (!filenames.getFiles().isEmpty()) {
            throw new RuntimeException("Unpacking of NARCs with internal filesystems not yet supported");
        }

        if (!dir.exists() && !dir.mkdir()) {
            throw new RuntimeException("Failed to create output directory, check write permissions.");
        }

        for (int i = 0; i < files.size(); i++) {
            System.out.println(StringFormatter.formatOutputString(i, files.size(), "", ""));
            BinaryWriter.writeFile(Paths.get(dir.getAbsolutePath(),files.get(i).getFileName())+".bin", files.get(i).getCompressedData());
        }
    }

    /**
     * Get the current list of files contained within the Narc
     *
     * @return String List of the filenames
     */
    public List<String> getFilenames() {
        return files.stream().map(BaseNFSFile::getFileName).toList();
    }

    /**
     * Get the current list of files contained within the Narc
     *
     * @return String List of the files
     */
    public List<BaseNFSFile> getFiles() {
        return files;
    }

    /**
     * Return the contents of the file with the given filename (path).
     * @param filename a String containing the path to the requested NARC subfile
     * @return a byte[] containing the contents of the requested NARC subfile
     * @exception RuntimeException if file with given name is not found
     */
    public byte[] getRawFileByName(String filename) {
        int fid = filenames.getIdOf(filename);
        if (fid == -1) {
            throw new RuntimeException("Couldn't find file ID of \"" + filename + "\".");
        }
        return rawFiles.get(fid);
    }

    /**
     * Replace the contents of the NARC subfile with the given filename (path) with the given data.
     * @param filename a String containing the path to the specified NARC subfile
     * @param data a byte[] containing the contents to set for the specified NARC subfile
     * @exception RuntimeException if file with given name is not found
     */
    public void setRawFileByName(String filename, byte[] data) {
        int fid = filenames.getIdOf(filename);
        if (fid == -1) {
            throw new RuntimeException("Couldn't find file ID of \"" + filename + "\".");
        }
        rawFiles.set(fid, data);
    }

    /**
     * Rename all files in the Narc according to an .lst file
     *
     * @param lstPath the path to the .lst file
     * @throws IOException failed to parse the .lst file
     */
    public void applyLst(String lstPath) throws IOException {
        if (lstPath != null) {
            LstFile lstFile = new LstFile(lstPath);
            List<String> lst = lstFile.getFileNames();
            if (lst.size() != files.size()) {
                throw new NitroException("LST DOES NOT MATCH NARC FILE LIST IN SIZE");
            }

            for (int i = 0; i < lst.size(); i++) {
                files.get(i).setFileName(lst.get(i));
            }
        }
    }

    /**
     * Rename all files in the Narc according to an .h file
     *
     * @param defPath the path to the .h file
     * @throws IOException failed to parse the .h file
     */
    public void applyDef(String defPath) throws IOException {
        if (defPath != null) {
            DefHeaderFile defFile = new DefHeaderFile(defPath);
            List<String> def = defFile.getFileNames();
            if (def.size() != files.size()) {
                throw new NitroException("DEF DOES NOT MATCH NARC FILE LIST IN SIZE");
            }

            for (int i = 0; i < def.size(); i++) {
                files.get(i).setFileName(def.get(i));
            }
        }
    }

    /**
     * Rename all files in the Narc according to an .scr file
     *
     * @param scrPath the path to the .scr file
     * @throws IOException failed to parse the .scr file
     */
    public void applyScr(String scrPath) throws IOException {
        if (scrPath != null) {
            ScrFile scrFile = new ScrFile(scrPath);
            List<String> def = scrFile.getFileNames();
            if (def.size() != files.size()) {
                throw new NitroException("SCR DOES NOT MATCH NARC FILE LIST IN SIZE");
            }

            for (int i = 0; i < def.size(); i++) {
                files.get(i).setFileName(def.get(i));
            }
        }
    }

    /**
     * Rename all files in the Narc according to an .naix file
     *
     * @param naixPath the path to the .naix file
     * @throws IOException failed to parse the .naix file
     */
    public void applyNaix(String naixPath) throws IOException {
        if (naixPath != null) {
            NaixFile scrFile = new NaixFile(fileName,naixPath);
            List<String> def = scrFile.getFileNames();
            if (def.size() != files.size()) {
                throw new RuntimeException("NAIX DOES NOT MATCH NARC FILE LIST IN SIZE");
            }

            for (int i = 0; i < def.size(); i++) {
                files.get(i).setFileName(def.get(i));
            }
        }
    }

    /**
     * Create a .lst file obj based ont eh current filenames
     *
     * @return .lst file
     */
    public LstFile createLst() {
        return new LstFile(getFilenames());
    }

    /**
     * Rename all files in the Narc using a specified prefix value
     *
     * @param reindexValue prefix value
     */
    public void reindex(String reindexValue) {
        for (int i = 0; i < files.size(); i++) {
            String name = reindexValue + "_" + String.format("%0" + String.valueOf(files.size()).length() + "d", i) + "." + files.get(i).getExt();
            files.get(i).setFileName(name);
        }
    }

    @Override
    public String toString() {
        if (rawFiles != null)
            return String.format("(%s) NARC with %d files", endiannessOfBeginning.symbol, rawFiles.size());
        return String.format("(%s) NARC", endiannessOfBeginning.symbol);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        NARC narc = (NARC) o;

        if (rawFiles.size() == narc.rawFiles.size()) {
            for (int i = 0; i < rawFiles.size(); i++) {
                if (!Arrays.equals(rawFiles.get(i), narc.rawFiles.get(i))) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return Objects.equals(filenames, narc.filenames) && endiannessOfBeginning == narc.endiannessOfBeginning;
    }

    @Override
    public int hashCode() {
        return Objects.hash(filenames, rawFiles, endiannessOfBeginning);
    }
}
