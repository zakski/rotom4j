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
     * @param data a <code>byte[]</code> representation of a <code>Narc</code>
     */
    public NARC(File file, byte[] data) throws IOException {
        super(NFSFormat.NARC, (file!=null)?file.getName():"",data);
        MemBuf buf = MemBuf.create(rawData);
        MemBuf.MemBufReader reader = buf.reader();
        readGenericNtrHeader(NFSFormat.NARC,reader);
        readFile(reader);
    }

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
//                if (endOffset > startOffset) {
//                    files.add(new BinNFSFile(getFileNameWithoutExt(), numFiles,i, rawFiles.get(i)));
//                } else {
//                    files.add(new PlaceholderNFSFile(getFileNameWithoutExt(),numFiles,i));
//                }
            }
        }

        // parse the filenames
        reader.setPosition(fntbOffset + 8);
        filenames = Fnt.load(reader.readBytes((int) fntbSize));
    }

    /**
     * Load a NARC archive from a filesystem file
     * @param file a <code>String</code> containing the path to a NARC file on disk
     * @return a <code>Narc</code> object
     */
    public static NARC fromFile(String file) throws IOException {
        return fromFile(new File(file));
    }

    /**
     * Load a NARC archive from a file on disk
     * @param file a <code>File</code> object representing the path to a NARC file on disk
     * @return a <code>Narc</code> object
     */
    public static NARC fromFile(File file) throws IOException {
        return new NARC(file, Buffer.readFile(file.getAbsolutePath()));
    }

//    /**
//     * Load an unpacked NARC from a directory on disk
//     * @param dir a <code>String</code> object containing the path to an unpacked NARC directory on disk
//     * @param removeFilenames whether the NARC should have a Fnt (ignored if there are subfolders within)
//     * @param endiannessOfBeginning whether the NARC's beginning is encoded in Big Endian or Little Endian
//     *                              - can be <code>Endianness.EndiannessType.BIG</code> or <code>Endianness.EndiannessType.LITTLE</code>
//     * @return a <code>Narc</code> object
//     */
//    public static Narc fromUnpacked(String dir, boolean removeFilenames, Endianness.EndiannessType endiannessOfBeginning)
//    {
//        return fromUnpacked(new File(dir), removeFilenames, endiannessOfBeginning);
//    }
//
//
//    /**
//     * Load an unpacked NARC from a directory on disk
//     * @param dir a <code>File</code> object representing the path to an unpacked NARC directory on disk
//     * @param removeFilenames whether the NARC should have a Fnt (ignored if there are subfolders within)
//     * @return a <code>Narc</code> object
//     * @param endiannessOfBeginning whether the NARC's beginning is encoded in Big Endian or Little Endian
//     *                              - can be <code>Endianness.EndiannessType.BIG</code> or <code>Endianness.EndiannessType.LITTLE</code>
//     * @exception RuntimeException if the specified path on disk does not exist or is not a directory
//     */
//    public static Narc fromUnpacked(File dir, boolean removeFilenames, Endianness.EndiannessType endiannessOfBeginning)
//    {
//        Fnt.Folder root;
//        ArrayList<byte[]> files = new ArrayList<>();
//
//        int numFiles = Fnt.calculateNumFiles(dir);
//        for (int i = 0; i < numFiles; i++)
//        {
//            files.add(null);
//        }
//
//        root = Fnt.loadFromDisk(dir, files);
//
//        if (Objects.requireNonNull(dir.listFiles(File::isDirectory)).length == 0 && removeFilenames)
//        {
//            root = new Fnt.Folder("root");
//        }
//
//        return fromContentsAndNames(files, root, endiannessOfBeginning);
//    }
//
//
//    /**
//     * Create a NARC archive from a list of files and (optionally) a filename table.
//     * @param files an <code>ArrayList</code> of <code>byte[]</code>'s representing all the subfiles in the NARC
//     * @param filenames an (OPTIONAL) <code>Folder</code> representing the NARC's filesystem
//     * @param endiannessOfBeginning whether the NARC's beginning is encoded in Big Endian or Little Endian
//     *                              - can be <code>Endianness.EndiannessType.BIG</code> or <code>Endianness.EndiannessType.LITTLE</code>
//     * @return a <code>Narc</code> representation of the provided parameters
//     */
//    public static Narc fromContentsAndNames(ArrayList<byte[]> files, Fnt.Folder filenames, Endianness.EndiannessType endiannessOfBeginning)
//    {
//        Narc narc = new Narc();
//        if (endiannessOfBeginning != null)
//            narc.endiannessOfBeginning = endiannessOfBeginning;
//        else
//            narc.endiannessOfBeginning = Endianness.EndiannessType.LITTLE;
//
//        if (files != null)
//            narc.files = new ArrayList<>(files);
//        else
//            narc.files = new ArrayList<>();
//
//        if (filenames != null)
//            narc.filenames = filenames;
//        else
//            narc.filenames = new Fnt.Folder();
//        narc.filenames = filenames;
//        return narc;
//    }

    /**
     * Unpacks this <code>Narc</code> to disk at the specified path
     * @param dir a <code>String</code> containing the target directory to unpack the NARC to
     * @exception RuntimeException if this <code>Narc</code> has an internal filesystem, the specified path
     * already exists, a new directory at the specified path could not be created, or a failure to write the
     * subfiles occurred
     * @exception IOException if the parent directory of the output subfiles does not exist
     */
    public void unpack(String dir) throws IOException
    {
        if (dir != null) {
            unpack(new File(dir));
        }
    }

    public void unpackWithCompression(String dir) throws IOException
    {
        if (dir != null){
            unpackWithCompression(new File(dir));
        }
    }


    /**
     * Unpacks this <code>Narc</code> to disk at the specified path
     * @param dir a <code>File</code> containing the target directory to unpack the NARC to
     * @exception RuntimeException if this <code>Narc</code> has an internal filesystem, the specified path
     * already exists, a new directory at the specified path could not be created, or a failure to write the
     * subfiles occurred
     * @exception IOException if the parent directory of the output subfiles does not exist
     */
    public void unpack(File dir) throws IOException
    {
        if (filenames.getFiles().size() > 0)
            throw new RuntimeException("Unpacking of NARCs with internal filesystems not yet supported");

//        if (dir.exists())
//            throw new RuntimeException("There is already a directory at the specified location");
        if (!dir.exists() && !dir.mkdir())
            throw new RuntimeException("Failed to create output directory, check write permissions.");

        for (int i = 0; i < files.size(); i++)
        {
            System.out.println(StringFormatter.formatOutputString(i, files.size(), "", ""));
            BinaryWriter.writeFile(Paths.get(dir.getAbsolutePath(),files.get(i).getFileName()), files.get(i).getData());
        }
    }

    public void unpackWithCompression(File dir) throws IOException
    {
        if (filenames.getFiles().size() > 0)
            throw new RuntimeException("Unpacking of NARCs with internal filesystems not yet supported");

//        if (dir.exists())
//            throw new RuntimeException("There is already a directory at the specified location");
        if (!dir.exists() && !dir.mkdir())
            throw new RuntimeException("Failed to create output directory, check write permissions.");

        for (int i = 0; i < files.size(); i++)
        {
            System.out.println(StringFormatter.formatOutputString(i, files.size(), "", ""));
            BinaryWriter.writeFile(Paths.get(dir.getAbsolutePath(),files.get(i).getFileName())+".bin", files.get(i).getCompressedData());
        }
    }

//    /**
//     * Generate a <code>byte[]</code> representing this NARC.
//     * @return a <code>byte[]</code>
//     */
//    public byte[] save()
//    {
//        // Prepare the filedata and file allocation table block
//        MemBuf fimgBuf = MemBuf.create();
//        MemBuf.MemBufWriter fimgWriter = fimgBuf.writer();
//
//
//        MemBuf fatbBuf = MemBuf.create();
//        MemBuf.MemBufWriter fatbWriter = fatbBuf.writer();
//        fatbWriter.writeString("BTAF");
//        fatbWriter.writeInt(FATB_HEADER_SIZE + 8 * files.size());
//        fatbWriter.writeInt(files.size());
//
//        // Write data into the FIMG and FAT blocks
//        long startOffset;
//        long endOffset;
//        for(byte[] data : files) {
//            startOffset = fimgWriter.getPosition();
//            fimgWriter.write(data);
//            endOffset = fimgWriter.getPosition();
//            fatbWriter.writeUInt32(startOffset).writeUInt32(endOffset);
//            fimgWriter.align(4);
//        }
//
//        byte[] fimg = fimgBuf.reader().getBuffer();
//        fimgBuf = MemBuf.create();
//        fimgWriter = fimgBuf.writer();
//        fimgWriter.writeString("GMIF");
//        fimgWriter.writeUInt32(fimg.length + FIMG_HEADER_SIZE);
//        fimgWriter.write(fimg);
//
//        // Assemble the filename table block
//        MemBuf nameTable = Fnt.save(filenames);
//        nameTable.writer().align(4, (byte) 0xFF);
//        MemBuf fntbBuf = MemBuf.create();
//        fntbBuf.writer().writeString("BTNF");
//        fntbBuf.writer().writeUInt32(nameTable.reader().getBuffer().length + FNTB_HEADER_SIZE);
//        fntbBuf.writer().write(nameTable.reader().getBuffer());
//
//        // Put everything together and return.
//        MemBuf narcBuf = MemBuf.create();
//        MemBuf.MemBufWriter narcWriter = narcBuf.writer();
//
//        narcWriter.skip(NTR_HEADER_SIZE);
//        narcWriter.write(fatbBuf.reader().getBuffer());
//        narcWriter.write(fntbBuf.reader().getBuffer());
//        narcWriter.write(fimgBuf.reader().getBuffer());
//
//        int narcLength = narcWriter.getPosition();
//
//        narcWriter.setPosition(0);
//        writeGenericNtrHeader(narcWriter, narcLength, 3);
//
//        narcWriter.setPosition(narcLength);
//        return narcBuf.reader().getBuffer();
//    }


//    /**
//     * Generate a <code>byte[]</code> representing this NARC, and save it to a file on disk
//     * @param path a <code>String</code> containing the path to the file on disk to save as
//     * @throws IOException if the specified file's parent directory does not exist.
//     */
//    public void saveToFile(String path) throws IOException
//    {
//        saveToFile(new File(path));
//    }
//
//    /**
//     * Generate a <code>byte[]</code> representing this NARC, and save it to a file on disk
//     * @param file a <code>File</code> representing the path to the file on disk to save as
//     * @exception RuntimeException if the provided path leads to a directory
//     * @throws IOException if the specified file's parent directory does not exist.
//     */
//    public void saveToFile(File file) throws IOException
//    {
//        if (file.exists() && file.isDirectory())
//        {
//            throw new RuntimeException("\"" + file.getAbsolutePath() + "\" is a directory. Save failed.");
//        }
//        BinaryWriter.writeFile(file, save());
//    }

    public int getNumFiles()
{
    return rawFiles.size();
}
    public ArrayList<byte[]> getRawFiles()
    {
        return rawFiles;
    }

    public List<String> getFilenames() {
        return files.stream().map(BaseNFSFile::getFileName).toList();
    }

    public byte[] getFile(int index){
        return rawFiles.get(index);
    }

    /**
     * Return the contents of the file with the given filename (path).
     * @param filename a <code>String</code> containing the path to the requested NARC subfile
     * @return a <code>byte[]</code> containing the contents of the requested NARC subfile
     * @exception RuntimeException if file with given name is not found
     */
    public byte[] getFileByName(String filename)
    {
        int fid = filenames.getIdOf(filename);
        if (fid == -1)
        {
            throw new RuntimeException("Couldn't find file ID of \"" + filename + "\".");
        }
        return rawFiles.get(fid);
    }



    /**
     * Replace the contents of the NARC subfile with the given filename (path) with the given data.
     * @param filename a <code>String</code> containing the path to the specified NARC subfile
     * @param data a <code>byte[]</code> containing the contents to set for the specified NARC subfile
     * @exception RuntimeException if file with given name is not found
     */
    public void setFileByName(String filename, byte[] data)
    {
        int fid = filenames.getIdOf(filename);
        if (fid == -1)
        {
            throw new RuntimeException("Couldn't find file ID of \"" + filename + "\".");
        }
        rawFiles.set(fid, data);
    }

    public void setRawFiles(ArrayList<byte[]> rawFiles)
    {
        this.rawFiles = rawFiles;
    }


    public void setFile(int index, byte[] file)
    {
        rawFiles.set(index, file);
    }

    public void applyLst(String lstPath) throws IOException {
        if (lstPath != null) {
            LstFile lstFile = new LstFile(lstPath);
            List<String> lst = lstFile.getFileNames();
            if (lst.size() != files.size()) {
                throw new RuntimeException("LST DOES NOT MATCH NARC FILE LIST IN SIZE");
            }

            for (int i = 0; i < lst.size(); i++) {
                files.get(i).setFileName(lst.get(i));
            }
        }
    }

    public void applyDef(String defPath) throws IOException {
        if (defPath != null) {
            DefHeaderFile defFile = new DefHeaderFile(defPath);
            List<String> def = defFile.getFileNames();
            if (def.size() != files.size()) {
                throw new RuntimeException("DEF DOES NOT MATCH NARC FILE LIST IN SIZE");
            }

            for (int i = 0; i < def.size(); i++) {
                files.get(i).setFileName(def.get(i));
            }
        }
    }

    public void applyScr(String scrPath) throws IOException {
        if (scrPath != null) {
            ScrFile scrFile = new ScrFile(scrPath);
            List<String> def = scrFile.getFileNames();
            if (def.size() != files.size()) {
                throw new RuntimeException("SCR DOES NOT MATCH NARC FILE LIST IN SIZE");
            }

            for (int i = 0; i < def.size(); i++) {
                files.get(i).setFileName(def.get(i));
            }
        }
    }

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

    public LstFile createLst() {
        return new LstFile(getFilenames());
    }
    public void reindex(String reindexValue) {
        for (int i = 0; i < files.size(); i++) {
            String name = reindexValue + "_" + String.format("%0" + String.valueOf(files.size()).length() + "d", i) + "." + files.get(i).getExt();
            files.get(i).setFileName(name);
        }
    }
    public void addFile(byte[] file)
    {
        rawFiles.add(file);
    }

    public void removeFile(byte[] file)
    {
        rawFiles.remove(file);
    }

    public void removeFile(int index)
    {
        rawFiles.remove(index);
    }



    public String toString()
    {
        if (rawFiles != null)
            return String.format("(%s) NARC with %d files", endiannessOfBeginning.symbol, rawFiles.size());
        return String.format("(%s) NARC", endiannessOfBeginning.symbol);
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        NARC narc = (NARC) o;

        if (rawFiles.size() == narc.rawFiles.size())
        {
            for (int i = 0; i < rawFiles.size(); i++)
            {
                if (!Arrays.equals(rawFiles.get(i), narc.rawFiles.get(i)))
                {
                    return false;
                }
            }
        }
        else
        {
            return false;
        }


        return Objects.equals(filenames, narc.filenames) && endiannessOfBeginning == narc.endiannessOfBeginning;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(filenames, rawFiles, endiannessOfBeginning);
    }
}
