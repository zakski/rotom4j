package com.szadowsz.rotom4j.file.nitro.ncer.cells;

public class CellPojo {
        int nAttribs;
        int cellAttr;

        short maxX;
        short maxY;
        short minX;
        short minY;

        int[][] attrs;

        public CellPojo(int cellCount, int cellAttr) {
                this.nAttribs = cellCount;
                this.cellAttr = cellAttr;
                this.attrs = new int[nAttribs][];
                for (int i = 0; i < this.nAttribs; i++) {
                        this.attrs[i] = new int[3];
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
                this.attrs = new int[nAttribs][];
                attrs[0] = new int[3];
                attrs[0][0] = 0x0200; // Disable rendering
        }

        public void setOamAttrs(int index, int attr0, int attr1, int attr2) {
                attrs[index][0] = attr0;
                attrs[index][1] = attr1;
                attrs[index][2] = attr2;
        }

        public int[] getOamAttrs(int index) {
                return attrs[index];
        }
}
