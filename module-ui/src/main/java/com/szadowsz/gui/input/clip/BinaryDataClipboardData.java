package com.szadowsz.gui.input.clip;


import com.szadowsz.rotom4j.binary.BinaryData;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import static com.szadowsz.gui.config.text.RFontStore.DEFAULT_ENCODING;

public class BinaryDataClipboardData implements ClipboardData {

        private final BinaryData data;
        private final DataFlavor binedDataFlavor;
        private final DataFlavor binaryDataFlavor;
        private final Charset charset;

        public BinaryDataClipboardData(BinaryData data, DataFlavor binedDataFlavor, Charset charset) {
            this.data = data;
            this.binedDataFlavor = binedDataFlavor;
            this.binaryDataFlavor = null;
            this.charset = charset;
        }

        public BinaryDataClipboardData(BinaryData data, DataFlavor binedDataFlavor, DataFlavor binaryDataFlavor, Charset charset) {
            this.data = data;
            this.binedDataFlavor = binedDataFlavor;
            this.binaryDataFlavor = binaryDataFlavor;
            this.charset = charset;
        }

        public BinaryDataClipboardData(BinaryData data, DataFlavor binedDataFlavor) {
            this(data, binedDataFlavor, null);
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return binaryDataFlavor != null ? new DataFlavor[]{binedDataFlavor, binaryDataFlavor, DataFlavor.stringFlavor} : new DataFlavor[]{binedDataFlavor, DataFlavor.stringFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(binedDataFlavor) || flavor.equals(binaryDataFlavor) || flavor.equals(DataFlavor.stringFlavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.equals(binedDataFlavor)) {
                return data;
            } else if (flavor.equals(binaryDataFlavor)) {
                return data.getDataInputStream();
            } else if (flavor.equals(DataFlavor.stringFlavor)) {
                Object result;
                try (ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream()) {
                    data.saveToStream(byteArrayStream);
                    result = charset == null ? byteArrayStream.toString(DEFAULT_ENCODING) : byteArrayStream.toString(charset.name());
                }
                return result;
            }

            throw new UnsupportedFlavorException(flavor);
        }

        @Override
        public void lostOwnership(Clipboard clipboard, Transferable contents) {
            // do nothing
        }

        @Override
        public void dispose() {
            data.dispose();
        }
    }