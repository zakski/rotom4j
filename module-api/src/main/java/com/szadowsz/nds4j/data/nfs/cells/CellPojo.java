package com.szadowsz.nds4j.data.nfs.cells;

public class CellPojo {
        int nAttribs;
        int cellAttr;

        short maxX;
        short maxY;
        short minX;
        short minY;

        short[][] attrs;

        public CellPojo(int cellCount, int cellAttr) {
                this.nAttribs = cellCount;
                this.cellAttr = cellAttr;
                this.attrs = new short[nAttribs][];
                for (int i = 0; i < this.nAttribs; i++) {
                        this.attrs[i] = new short[3];
                }
        }

        public void setBounds(short maxX, short maxY, short minX, short minY) {
                this.maxX = maxX;
                this.maxY = maxY;
                this.minX = minX;
                this.minY = minY;
        }

        public void setEmptyAttributes() {
                nAttribs = 1;
                this.attrs = new short[nAttribs][];
                attrs[0] = new short[3];
                attrs[0][0] = 0x0200; // Disable rendering
        }

        public void setOamAttrs(int index, short attr0, short attr1, short attr2) {
                attrs[index][0] = attr0;
                attrs[index][1] = attr1;
                attrs[index][2] = attr2;
        }

        public short[] getOamAttrs(int index) {
                return attrs[index];
        }
}
