//package com.szadowsz.nds4j.builder;
//
//import com.szadowsz.nds4j.file.nitro.ncer.NCER;
//
//import java.awt.*;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.List;
//
//public class CellBuilder {
//    private static final int CELLGEN_DIR_H    = 0;
//    private static final int CELLGEN_DIR_V    = 1;
//    private static final int CELLGEN_DIR_NONE = -1;
//
//    private static final int CELLGEN_MAX_DIV_H = 3;
//    private static final int CELLGEN_MAX_DIV_V = 3;
//    private static final int CELLGEN_MAX_DIV = (CELLGEN_MAX_DIV_H+CELLGEN_MAX_DIV_V);
//    private static final int CELLGEN_MAX_PACK    = 64;
//
//    private static final int SHIFT_FLAG_BOTTOM   =    1;
//    private static final int SHIFT_FLAG_RIGHT    =    2;
//    private static final int SHIFT_FLAG_NOREMOVE  =   4;
//
//    private int CELLGEN_ROUNDUP8(int x)  {
//        return (((x)+7)&~7);
//    }
//
//    private boolean CellgenObjContainsPixels(Color[] px, int[] ignores, int width, int height, OBJ_BOUNDS bounds) {
//        int[] xMin = new int[1], xMax = new int[1], yMin = new int[1], yMax = new int[1];
//        CellgenGetXYBounds(px, ignores, width, height, bounds.x, bounds.x + bounds.width, bounds.y, bounds.y + bounds.height,
//                xMin, xMax, yMin, yMax);
//
//        //if xMin == xMax or yMin == yMax, this bound is empty.
//        return (xMin[0] != xMax[0]) && (yMin[0] != yMax[0]);
//    }
//
//    private int CellgenGetLargestObjDimension(int n) {
//        if (n >= 64) return 64;
//        if (n >= 32) return 32;
//        if (n >= 16) return 16;
//        return 8;
//    }
//
//    private int CellgenAdjustCorrectObjWidth(int height, int width) {
//        if (height == 64 && width == 8) return 32;
//        if (height == 64 && width == 16) return 32;
//        if (height == 8 && width == 64) return 32;
//        if (height == 16 && width == 64) return 32;
//        return width;
//    }
//
//    private void CellgenAccountRegion(int[] accountBuf, int width, int height, OBJ_BOUNDS bounds) {
//        int objXMin = bounds.x, objXMax = bounds.x + bounds.width;
//        int objYMin = bounds.y, objYMax = bounds.y + bounds.height;
//
//        //clamp to image size
//        objXMin = Math.min(Math.max(objXMin, 0), width);
//        objXMax = Math.min(Math.max(objXMax, 0), width);
//        objYMin = Math.min(Math.max(objYMin, 0), height);
//        objYMax = Math.min(Math.max(objYMax, 0), height);
//
//        //account for all of this OBJ's pixels
//        for (int y = objYMin; y < objYMax; y++) {
//            for (int x = objXMin; x < objXMax; x++) {
//                accountBuf[y * width + x] = 1;
//            }
//        }
//    }
//
//    private int CellgenTryIterateSplit(Color[] px, int[] accountBuf, int width, int height, int agr, OBJ_BOUNDS[] obj, int nObj, int maxDepth) {
//        //memset(accountBuf, 0, width * height);
//
//        int nObjInit = nObj;
//        for (int i = 0; i < nObjInit; i++) {
//            //int nTrans = CellgenCountTransparent(px, accountBuf, obj->width, obj->height, bounds);
//            //if (nTrans < 64) continue;
//
//            //TODO: adjustable criteria here based on nTrans or nTrans/size?
//
//            int[] boundBufferSize = new int[]{1};
//            OBJ_BOUNDS[] boundBuffer = new OBJ_BOUNDS[CELLGEN_MAX_PACK]; //maximum number of split OBJ
//            boundBuffer[0] = new OBJ_BOUNDS(obj[i]);
//
//            //try subdividing to cull regions
//            int didCull = CellgenProcessSubdivision(px, accountBuf, width, height, agr, boundBuffer, boundBufferSize, 0, maxDepth);
//            if (didCull==0) {
//                continue;
//            }
//
//            //did cull, so process accordingly
//            int nObjWritten = 0;
//            for (int j = 0; j < CELLGEN_MAX_PACK; j++) {
//                if (boundBuffer[j].width == 0 && boundBuffer[j].height == 0) {
//                    continue; //deleted entry
//                }
//
//                //account this created entry
//                CellgenAccountRegion(accountBuf, width, height, boundBuffer[j]);
//
//                //if nObjWritten == 0, write to bounds (obj[i])
//                if (nObjWritten == 0) {
//                    obj[i] = boundBuffer[j];
//                } else {
//                    //else write to obj + nObj
//                    obj[nObj++] = boundBuffer[j];
//                }
//
//                nObjWritten++;
//            }
//        }
//
//        return nObj;
//    }
//
//    private boolean CellgenIsSizeAllowed(int w, int h) {
//        //up to 32x32 all are allowed
//        if (w <= 32 && h <= 32) return true;
//
//        //beyond that only these are allowed
//        if (w == 64 && h == 64) return true;
//        if (w == 64 && h == 32) return true;
//        if (w == 32 && h == 64) return true;
//        return false;
//    }
//
//    private int CellgenTryCoalesce(OBJ_BOUNDS[] obj, int nObj) {
//        //try coalesce
//        for (int i = 0; i < nObj; i++) {
//            OBJ_BOUNDS obj1 = obj[i];
//            int w1 = obj1.width;
//            int h1 = obj1.height;
//
//            for (int j = i + 1; j < nObj; j++) {
//                OBJ_BOUNDS obj2 = obj[j];
//                int w2 = obj2.width;
//                int h2 = obj2.height;
//
//                //must be same size
//                if (w1 != w2 || h1 != h2) continue;
//
//                //cannot be 64x64
//                if (w1 == 64 && h1 == 64) continue;
//
//                //X or Y must match
//                if (obj1.x != obj2.x && obj1.y != obj2.y) continue;
//
//                //test positions
//                int removeIndex = -1;
//                if (obj1.y == obj2.y) {
//                    //same row
//                    if ((obj1.x + w1) == obj2.x && CellgenIsSizeAllowed(w1 * 2, h1)) {
//                        obj1.width *= 2;
//                        removeIndex = j;
//                    }
//                    if ((obj2.x + w2) == obj1.x && CellgenIsSizeAllowed(w2 * 2, h2)) {
//                        obj2.width *= 2;
//                        removeIndex = i;
//                    }
//                } else if (obj1.x == obj2.x) {
//                    //same column
//                    if ((obj1.y + h1) == obj2.y && CellgenIsSizeAllowed(w1, h1 * 2)) {
//                        obj1.height *= 2;
//                        removeIndex = j;
//                    }
//                    if ((obj2.y + h2) == obj1.y && CellgenIsSizeAllowed(w2, h2 * 2)) {
//                        obj2.height *= 2;
//                        removeIndex = i;
//                    }
//                }
//
//                //remove
//                if (removeIndex != -1) {
//                    System.arraycopy(obj, removeIndex + 1, obj, removeIndex, nObj - removeIndex - 1);
//
//                    if (removeIndex == i) {
//                        i--; //so we can iterate the new entry there
//                    }
//                    nObj--;
//                    break;
//                }
//            }
//        }
//
//        return nObj;
//    }
//
//    private int CellgenTryRemoveOverlapping(Color[] px, int[] accountBuf, int width, int height, OBJ_BOUNDS[] obj, int nObj) {
//        //scan through list of OBJ and see if they are redundant
//        for (int i = 0; i < nObj; i++) {
//            OBJ_BOUNDS obj1 = obj[i];
//            Arrays.fill(accountBuf, (byte) 0);
//
//            for (int j = 0; j < nObj; j++) {
//                if (j == i) {
//                    continue;
//                }
//                OBJ_BOUNDS obj2 = obj[j];
//
//                CellgenAccountRegion(accountBuf, width, height, obj2);
//            }
//
//            //is OBJ useful?
//            int nTrans = CellgenCountTransparent(px, accountBuf, width, height, obj1);
//            if (nTrans == obj1.width * obj1.height) {
//                //not useful
//                System.arraycopy(obj, i + 1, obj, i, nObj - i - 1);
//                nObj--;
//                i--;
//            }
//        }
//
//        return nObj;
//    }
//
//    private boolean CellgenTryAggressiveMerge(Color[] px, int[] accountBuf, int width, int height, OBJ_BOUNDS obj1, OBJ_BOUNDS obj2) {
//        int w = obj1.width, h = obj1.height;
//        int x1 = obj1.x, y1 = obj1.y, x2 = obj2.x, y2 = obj2.y;
//        if (obj2.width != w || obj2.height != h){
//            return false; //cannot merge
//        }
//
//        //get bounding box of both objects
//        int[] xMin1 = new int[1], yMin1 = new int[1], xMax1 = new int[1], yMax1 = new int[1];
//        int[] xMin2 = new int[1], yMin2 = new int[1], xMax2 = new int[1], yMax2 = new int[1];
//        CellgenGetXYBounds(px, accountBuf, width, height, x1, x1 + w, y1, y1 + h, xMin1, xMax1, yMin1, yMax1);
//        CellgenGetXYBounds(px, accountBuf, width, height, x2, x2 + w, y2, y2 + h, xMin2, xMax2, yMin2, yMax2);
//
//        //get total bounding box
//        int xMin = Math.min(xMin1[0], xMin2[0]);
//        int xMax = Math.max(xMax1[0], xMax2[0]);
//        int yMin = Math.min(yMin1[0], yMin2[0]);
//        int yMax = Math.max(yMax1[0], yMax2[0]);
//
//        //bounding region within twice the region of the input OBJ?
//        int boundWidth = xMax - xMin;
//        int boundHeight = yMax - yMin;
//        if (boundWidth > w && boundHeight > h) {
//            return false; //cannot do in one merge (must be within one dimension)
//        }
//
//        //within an existing bound??
//        if (boundWidth <= w && boundHeight <= h) {
//            obj1.x = xMin;
//            obj1.y = yMin;
//            return true;
//        }
//
//        //within twice width?
//        if (boundWidth <= (2 * w) && boundHeight <= h) {
//            int newWidth = w * 2;
//            if (!CellgenIsSizeAllowed(newWidth, h)) return false;
//
//            obj1.x = xMin;
//            obj1.y = yMin;
//            obj1.width = newWidth;
//            return true;
//        }
//
//        //within twice height?
//        if (boundHeight <= (2 * h) && boundWidth <= w) {
//            int newHeight = h * 2;
//            if (!CellgenIsSizeAllowed(w, newHeight)) {
//                return false;
//            }
//
//            obj1.x = xMin;
//            obj1.y = yMin;
//            obj1.height *= 2;
//            return true;
//        }
//
//        return false;
//    }
//
//    private void CellgenSplitObj(OBJ_BOUNDS obj, int dir, OBJ_BOUNDS out1, OBJ_BOUNDS out2) {
//        if (dir == CELLGEN_DIR_NONE) {
//            return;
//        }
//        out1.set(obj);
//        out2.set(obj);
//
//        if (dir == CELLGEN_DIR_H) {
//            out1.width /= 2;
//            out2.width /= 2;
//            out2.x += out1.width;
//        } else if (dir == CELLGEN_DIR_V) {
//            out1.height /= 2;
//            out2.height /= 2;
//            out2.y += out1.height;
//        }
//    }
//
//    private int CellgenDecideSplitDirection(Color[] px, int[] accountBuf, int width, int height, OBJ_BOUNDS bounds) {
//        //can we split both ways?
//        boolean canHSplit = false, canVSplit = false;
//
//        if (bounds.width != 8) {
//            if (bounds.width != 64 && bounds.height != 64) canHSplit = true; //all dimensions without a 64
//            if (bounds.width == 64 && bounds.height == 64) canHSplit = true; //64x64
//            if (bounds.width == 64 && bounds.height == 32) canHSplit = true; //64x32
//            if (bounds.width <= 32 && bounds.height <= 32) canHSplit = true; //any 32 and under
//        }
//        if (bounds.height != 8) {
//            if (bounds.width != 64 && bounds.height != 64) canVSplit = true; //all dimensions without 64
//            if (bounds.width == 64 && bounds.height == 64) canVSplit = true; //64x64
//            if (bounds.width == 32 && bounds.height == 64) canVSplit = true; //32x64
//            if (bounds.width <= 32 && bounds.height <= 32) canVSplit = true; //any 32 and under
//        }
//
//        //if only one direction is splittable, return that
//        if (!canHSplit && !canVSplit) return CELLGEN_DIR_NONE;
//        if (canHSplit && !canVSplit) return CELLGEN_DIR_H;
//        if (!canHSplit && canVSplit) return CELLGEN_DIR_V;
//
//        //else, decide split direction.
//        OBJ_BOUNDS[] tempH = new OBJ_BOUNDS[2];
//        OBJ_BOUNDS[] tempV = new OBJ_BOUNDS[2];
//        CellgenSplitObj(bounds, CELLGEN_DIR_H, tempH[0], tempH[1]);
//        CellgenSplitObj(bounds, CELLGEN_DIR_V, tempV[0], tempV[1]);
//
//        int diffH = CellgenCountTransparent(px, accountBuf, width, height, tempH[0])
//                - CellgenCountTransparent(px, accountBuf, width, height, tempH[1]);
//        int diffV = CellgenCountTransparent(px, accountBuf, width, height, tempV[0])
//                - CellgenCountTransparent(px, accountBuf, width, height, tempV[1]);
//        if (diffH < 0) diffH = -diffH;
//        if (diffV < 0) diffV = -diffV;
//
//        //the larger difference is how we want to split
//        if (diffH > diffV) return CELLGEN_DIR_H;
//        if (diffV > diffH) return CELLGEN_DIR_V;
//
//        if (bounds.width > bounds.height) return CELLGEN_DIR_H;
//        return CELLGEN_DIR_V;
//    }
//
//    private int CellgenProcessSubdivision(Color[] px, int[] accountBuf, int width, int height, int aggressiveness,
//                                          OBJ_BOUNDS[] boundBuffer, int[] pCount, int index, int maxDepth) {
//        //should we give up here based on the aggressiveness parameter?
//        int area = boundBuffer[index].width * boundBuffer[index].height;
//        int nTrans = CellgenCountTransparent(px, accountBuf, width, height, boundBuffer[index]);
//
//        //if nTrans / size >= proportionRequired
//        if (nTrans * 100 / area < (100 - aggressiveness)) return 0; //did not split
//
//        //can split?
//        int splitDir = CellgenDecideSplitDirection(px, accountBuf, width, height, boundBuffer[index]);
//        if (splitDir == CELLGEN_DIR_NONE) {
//            return 0; //did not split
//        }
//
//        //can split.
//        int split1Index = index;   //same
//        int split2Index = pCount[0]; //end
//        OBJ_BOUNDS original = new OBJ_BOUNDS(boundBuffer[index]);
//
//        //do split
//        CellgenSplitObj(original, splitDir, boundBuffer[split1Index], boundBuffer[split2Index]);
//        pCount[0]++; //buffer grew by 1
//
//        //are either of the two OBJ fully transparent?
//        boolean split1HasPixels = CellgenObjContainsPixels(px, accountBuf, width, height, boundBuffer[split1Index]);
//        boolean split2HasPixels = CellgenObjContainsPixels(px, accountBuf, width, height, boundBuffer[split2Index]);
//
//        int didCull = 0;
//        if (!split1HasPixels) {
//            //split 1 can be culled
//            boundBuffer[split1Index].width = 0; //removing would be dangerous
//            boundBuffer[split1Index].height = 0; //so just set size to (0,0)
//            didCull = 1;
//        }
//        if (!split2HasPixels) {
//            //split 2 can be culled
//            boundBuffer[split2Index].width = 0;
//            boundBuffer[split2Index].height = 0;
//            didCull = 1;
//        }
//
//        //try split children
//        int split1DidCull = 0, split2DidCull = 0;
//        if (split1HasPixels && maxDepth > 1) {
//            split1DidCull = CellgenProcessSubdivision(px, accountBuf, width, height, aggressiveness, boundBuffer, pCount, split1Index, maxDepth - 1);
//        }
//        if (split2HasPixels && maxDepth > 1) {
//            split2DidCull = CellgenProcessSubdivision(px, accountBuf, width, height, aggressiveness, boundBuffer, pCount, split2Index, maxDepth - 1);
//        }
//
//        //if either child did cull, we cannot re-merge.
//        if (split1DidCull==1 || split2DidCull==1) {
//            didCull = 1;
//        }
//
//        if (didCull==0) {
//            //merge back (no culling happened)
//            boundBuffer[split1Index] = new OBJ_BOUNDS(original);
//            boundBuffer[split2Index].width = 0;  //split 2 effectively deleted
//            boundBuffer[split2Index].height = 0;
//        }
//
//        //return based on whether we culled
//        return didCull;
//    }
//
//    private int CellgenShiftObj(Color[] px, int[] accountBuf, int width, int height, OBJ_BOUNDS[] obj, int nObj, int flag) {
//        //edges to fit pixels to
//        boolean matchLeft = (flag & SHIFT_FLAG_RIGHT)==0;
//        boolean matchTop = (flag & SHIFT_FLAG_BOTTOM)==0;
//
//        for (int i = 0; i < nObj; i++) {
//            //get OBJ current bounds
//            OBJ_BOUNDS bounds = obj[i];
//            int objXMin = bounds.x, objXMax = bounds.x + bounds.width;
//            int objYMin = bounds.y, objYMax = bounds.y + bounds.height;
//
//            //get pixel bounds of this OBJ
//            int[] bxMin = new int[1], bxMax = new int[1], byMin = new int[1], byMax = new int[1];
//            CellgenGetXYBounds(px, accountBuf, width, height, objXMin, objXMax, objYMin, objYMax, bxMin, bxMax, byMin, byMax);
//
//            //if xMin == xMax or yMin == yMax, this OBJ has become useless in this step, so remove it.
//            if ((bxMin[0] == bxMax[0] || byMin[0] == byMax[0]) && (flag & SHIFT_FLAG_NOREMOVE)==0) {
//                System.arraycopy(obj, i + 1, obj, i, nObj - i - 1);
//                nObj--;
//                i--;
//                continue;
//            }
//
//            //if xMin > objXMin, slide right the difference
//            //if yMin > objYMin, slide down the difference
//            if (matchLeft) {
//                if (bxMin[0] > objXMin) {
//                    bounds.x += (bxMin[0] - objXMin);
//                }
//            } else {
//                if (bxMax[0] < objXMax) {
//                    bounds.x -= (objXMax - bxMax[0]);
//                }
//            }
//            if (matchTop) {
//                if (byMin[0] > objYMin) {
//                    bounds.y += (byMin[0] - objYMin);
//                }
//            } else {
//                if (byMax[0] < objYMax) {
//                    bounds.y -= (objYMax - byMax[0]);
//                }
//            }
//
//            //recompute bounds
//            objXMin = bounds.x;
//            objXMax = bounds.x + bounds.width;
//            objYMin = bounds.y;
//            objYMax = bounds.y + bounds.height;
//
//            //account for all of this OBJ's pixels
//            CellgenAccountRegion(accountBuf, width, height, bounds);
//        }
//        return nObj;
//    }
//
//    private int CellgenIterateAllShifts(Color[] px, int[] accountBuf, int width, int height, OBJ_BOUNDS[] obj, int nObj) {
//        //all combinations of flags
//        for (int i = 0; i < 4; i++) {
//            Arrays.fill(accountBuf,  0);
//            nObj = CellgenShiftObj(px, accountBuf, width, height, obj, nObj, i);
//        }
//        return nObj;
//    }
//
//    void CellgenGetXYBounds(Color[] px, int[] ignores, int width, int height, int xMin, int xMax, int yMin, int yMax, int[] pxMin, int[] pxMax, int[] pyMin, int[] pyMax) {
//        //clamp bounds
//        yMin = Math.min(Math.max(yMin, 0), height);
//        yMax = Math.min(Math.max(yMax, 0), height);
//        xMin = Math.min(Math.max(xMin, 0), width);
//        xMax = Math.min(Math.max(xMax, 0), width);
//
//        //init -1 to indicate none found (yet)
//        //must be same in case none ever found
//        int xOutMin = -1, xOutMax = -1;
//        int yOutMin = -1, yOutMax = -1;
//
//        //scan rectangle
//        for (int y = yMin; y < yMax; y++) {
//            for (int x = xMin; x < xMax; x++) {
//                Color c = px[y * width + x];
//                boolean a = ((c.getRGB() >> 24) & 0xFF) >= 128;
//
//                int ignore = 0;
//                if (ignores != null) {
//                    ignore = ignores[y * width + x];
//                }
//
//                //ignore marks pixel as effectively transparent
//                if (ignore > 0) {
//                    a = false;
//                }
//
//                if (a) {
//                    if (xOutMin == -1 || x < xOutMin) xOutMin = x;
//                    if (yOutMin == -1 || y < yOutMin) yOutMin = y;
//                    if (x + 1 > xOutMax) xOutMax = x + 1;
//                    if (y + 1 > yOutMax) yOutMax = y + 1;
//                }
//            }
//        }
//
//        pxMin[0] = xOutMin;
//        pxMax[0] = xOutMax;
//        pyMin[0] = yOutMin;
//        pyMax[0] = yOutMax;
//    }
//
//    static int CellgenCountTransparent(Color[] px, int[] accountBuf, int width, int height, OBJ_BOUNDS bounds) {
//        int xMin = bounds.x, xMax = bounds.x + bounds.width;
//        int yMin = bounds.y, yMax = bounds.y + bounds.height;
//        yMin = Math.min(Math.max(yMin, 0), height);
//        yMax = Math.min(Math.max(yMax, 0), height);
//        xMin = Math.min(Math.max(xMin, 0), width);
//        xMax = Math.min(Math.max(xMax, 0), width);
//
//        int nTransparent = 0;
//        for (int y = yMin; y < yMax; y++) {
//            for (int x = xMin; x < xMax; x++) {
//                Color c = px[y * width + x];
//                boolean a = (c.getRGB() >> 24) >= 128;
//
//                int skip = 0;
//                if (accountBuf != null) {
//                    skip = accountBuf[y * width + x];
//                }
//                if (skip==1){
//                    a = false;
//                }
//
//                if (!a) {
//                    nTransparent++;
//                }
//            }
//        }
//        return nTransparent;
//    }
//
//    private int CellgenCoalesceAggressively(Color[] px, int[] accountBuf, int width, int height, OBJ_BOUNDS[] obj, int nObj) {
//        Arrays.fill(accountBuf, 0);
//
//        //some cells may not be directly adjacent but still make for viable merges.
//        //the goal here is to coalesce those that would be candidates for this kind
//        //of merge.
//        for (int i = 0; i < nObj; i++) {
//            OBJ_BOUNDS obj1 = obj[i];
//            int w1 = obj1.width;
//            int h1 = obj1.height;
//
//            for (int j = i + 1; j < nObj; j++) {
//                OBJ_BOUNDS obj2 = obj[j];
//                int w2 = obj2.width;
//                int h2 = obj2.height;
//
//                //must be same size
//                if (w1 != w2 || h1 != h2) continue;
//
//                //cannot be 64x64
//                if (w1 == 64 && h1 == 64) continue;
//
//                //set account buffer to every OBJ excluding these two
//                Arrays.fill(accountBuf,  0);
//                for (int k = 0; k < nObj; k++) {
//                    if (k == i || k == j) continue;
//                    CellgenAccountRegion(accountBuf, width, height, obj[k]);
//                }
//
//                //can these merge?
//                boolean merged = CellgenTryAggressiveMerge(px, accountBuf, width, height, obj1, obj2);
//
//                //if merged...
//                if (merged) {
//                    //consume obj2
//                    System.arraycopy(obj, j + 1, obj, j, nObj - j - 1);
//
//                    nObj--;
//                    i--;
//                    break;
//                }
//            }
//        }
//        return nObj;
//    }
//
//    private int CellgenCondenseObj(Color[] px, int[] accountBuf, int width, int height, int cx, int cy, OBJ_BOUNDS[] obj, int nObj) {
//        Arrays.fill(accountBuf,  0);
//
//        //push towards center
//        for (int i = 0; i < nObj; i++) {
//            OBJ_BOUNDS o = obj[i];
//            int x = o.x + o.width / 2;
//            int y = o.y + o.height / 2;
//
//            //if x > cx, push left
//            //if y > cy, push up
//            int flag = 0;
//            if (x > cx) flag |= SHIFT_FLAG_RIGHT;  //right edge
//            if (y > cy) flag |= SHIFT_FLAG_BOTTOM; //bottom edge
//            CellgenShiftObj(px, accountBuf, width, height, new OBJ_BOUNDS[]{o}, 1, flag | SHIFT_FLAG_NOREMOVE);
//            CellgenAccountRegion(accountBuf, width, height, o);
//        }
//
//        return nObj;
//    }
//    /******************************************************************************\
//     *
//     * Processes a set of OBJ and divides those that have a greater width:height or
//     * height:width ratio (depending on which is larger) to ensure that no OBJ is
//     * too elongated. This can help to ensure that an affine cell can be rotated
//     * properly. The input array is discarded after calling this function and the
//     * returned array should be used instead.
//     *
//     * Parameters:
//     *   obj                     the input OBJ array
//     *   nObj                    the number of input OBJ
//     *   maxRatio                the largest acceptable size ratio
//     *   pnOutObj                the output number of OBJ
//     *
//     * Returns:
//     *	A list of OBJ satisfying the ratio criteria.
//     *
//     \******************************************************************************/
//    private OBJ_BOUNDS[] CellgenEnsureRatio(OBJ_BOUNDS[] obj, int nObj, int maxRatio, int[] pnOutObj) {
//        java.util.List<OBJ_BOUNDS> result = new ArrayList<>(List.of(obj));
//        //for each OBJ, split any whose aspect ratio exceeds maxRatio
//        for (int i = 0; i < nObj; i++) {
//            OBJ_BOUNDS bound = obj[i];
//            boolean wide = (bound.width > bound.height);
//
//            int ratio = 1;
//            if (wide) {
//                ratio = bound.width / bound.height;
//            } else {
//                ratio = bound.height / bound.width;
//            }
//
//            if (ratio <= maxRatio) {
//                continue;
//            }
//
//            //split
//            OBJ_BOUNDS temp[] = new OBJ_BOUNDS[]{new OBJ_BOUNDS(), new OBJ_BOUNDS()};
//            CellgenSplitObj(bound, wide ? CELLGEN_DIR_H : CELLGEN_DIR_V, temp[0], temp[1]);
//
//            // make space for new OBJ
//            nObj++;
//            // Replace the original object with the first split
//            result.set(i, temp[0]);
//            // Add the second split
//            result.add(i + 1, temp[1]);
//
//            i--; //allow obj to be recursively processed
//        }
//
//        pnOutObj[0] = result.size();
//        return result.toArray(new OBJ_BOUNDS[0]);
//    }
//
//    private int CellgenRemoveHalfRedundant(Color[] px, int[] accountBuf, int width, int height, OBJ_BOUNDS[] obj, int nObj) {
//        //for each, add all others to account buffer and remove half
//        for (int i = 0; i < nObj; i++) {
//            Arrays.fill(accountBuf, 0);
//
//            for (int j = 0; j < nObj; j++) {
//                if (j == i) continue;
//                CellgenAccountRegion(accountBuf, width, height, obj[j]);
//            }
//
//            //try make split
//            int n = CellgenTryIterateSplit(px, accountBuf, width, height, 100, new OBJ_BOUNDS[]{obj[i]}, 1, 1);
//            if (n == 0) {
//                //object was removed
//                System.arraycopy(obj, i + 1, obj, i, nObj - i - 1);
//                nObj--;
//                i--;
//            }
//        }
//
//        return nObj;
//    }
//
//    /******************************************************************************\
//     *
//     * Divide an image into a set of OBJ. This function outputs OBJ with valid
//     * hardware OBJ sizes. Only the opaque region of the image is guaranteed to be
//     * covered when aggressiveness > 0. With aggressiveness=0, the whole image is
//     * covered with OBJ. Only the bounding box of opaque pixels is covered wen the
//     * full parameter is zero, or the whole image area otherwise.
//     *
//     * Parameters:
//     *	px                      the image pixels
//     *   width                   the image width
//     *   height                  the image height
//     *	aggressiveness          the level of aggressiveness when dividing the OBJ
//     *                           (0-100)
//     *	full                    controls whether the whole or only opaque region is
//     *                           used.
//     *   affine                  makes a cell for affine use. If nonzero, the cell
//     *                           is generated using 8x8, 16x16, 32x32, and 64x64 OBJ
//     *                           only.
//     *   pnObj                   pointer to the output number of OBJ.
//     *
//     * Returns:
//     *	A list of OBJ covering the image with coordinates in the image space.
//     *
//     \******************************************************************************/
//    OBJ_BOUNDS[] CellgenMakeCell(Color[] px, int width, int height, int aggressiveness, int full, boolean affine, int[] pnObj) {
//        //get image bounds
//        int[] xMin = new int[1], xMax = new int[1], yMin = new int[1], yMax = new int[1];
//        CellgenGetXYBounds(px, null, width, height, 0, width, 0, height, xMin, xMax, yMin, yMax);
//
//        //if full image rectangle requested
//        if (full>0) {
//            xMin[0] = yMin[0] = 0;
//            xMax[0] = width;
//            yMax[0] = height;
//        }
//
//        int boundingWidth = xMax[0] - xMin[0];
//        int boundingHeight = yMax[0] - yMin[0];
//
//        //pump up bounds to a multiple of 8
//        boundingWidth = (boundingWidth + 7) & ~7;
//        boundingHeight = (boundingHeight + 7) & ~7;
//        xMax[0] = xMin[0] + boundingWidth;
//        yMax[0] = yMin[0] + boundingHeight;
//
//        //trivial case: (0, 0) size
//        if (boundingWidth == 0 && boundingHeight == 0) {
//            pnObj[0] = 0;
//            return null;
//        }
//
//        //trivial case: one 8x8 OBJ required
//        if (boundingWidth <= 8 && boundingHeight <= 8) {
//            OBJ_BOUNDS[] obj = new OBJ_BOUNDS[1];
//            obj[0] = new OBJ_BOUNDS();
//            obj[0].x = xMin[0];
//            obj[0].y = yMin[0];
//            obj[0].width = 8;
//            obj[0].height = 8;
//
//            pnObj[0] = 1;
//            return obj;
//        }
//
//        int nObj = 0;
//        OBJ_BOUNDS[] obj = new OBJ_BOUNDS[256 / 8 * 512 / 8]; // max possible
//
//
//        //greedily split into OBJ as large as we can fit
//        for (int y = yMin[0]; y < yMax[0];) {
//            //get OBJ height for this row
//            int yRemaining = CELLGEN_ROUNDUP8(yMax[0] - y);
//            int objHeight = CellgenGetLargestObjDimension(yRemaining);
//
//            //scan across
//            for (int x = xMin[0]; x < xMax[0];) {
//                int xRemaining = CELLGEN_ROUNDUP8(xMax[0] - x);
//                int objWidth = CellgenGetLargestObjDimension(xRemaining);
//
//                //for this OBJ height, some widths may be disallowed.
//                objWidth = CellgenAdjustCorrectObjWidth(objHeight, objWidth);
//
//                //slot in
//                obj[nObj].x = x;
//                obj[nObj].y = y;
//                obj[nObj].width = objWidth;
//                obj[nObj].height = objHeight;
//                nObj++;
//
//                //next column
//                x += objWidth;
//            }
//
//            //next row
//            y += objHeight;
//        }
//
//        //remove all OBJ not occupying any opaque pixels
//        if (aggressiveness > 0) {
//            for (int i = 0; i < nObj; i++) {
//                OBJ_BOUNDS bounds = obj[i];
//
//                //check bounding region
//                int[] bxMin = new int[1], bxMax = new int[1], byMin = new int[1], byMax = new int[1];
//                CellgenGetXYBounds(px, null, width, height, bounds.x, bounds.x + bounds.width, bounds.y, bounds.y + bounds.height,
//                        bxMin, bxMax, byMin, byMax);
//
//                if (bxMin[0] == bxMax[0] && byMin[0] == byMax[0]) {
//                    //remove
//                    System.arraycopy(obj, i + 1, obj, i, nObj - i - 1);
//                    nObj--;
//                    i--;
//                }
//            }
//        }
//
//        //we now have a set of OBJ that fully cover the image region with all OBJ
//        //covering some amount of pixels. Now, adjust the OBJ positions so that their
//        //pixels are towards the top and left edge.
//
//        //create a "pixels accounted" buffer, 1 byte per pixel of image. It will keep
//        //track of which opaque pixels of image have been accounted for in some OBJ.
//        //this will be useful for potentially overlapping OBJ, so we know which pixels
//        //overlap and don't need to worry about (so we may potentially move another OBJ
//        //over).
//        int[] accountBuf = new int[width * height];
//
//        //run one shift round
//        if (aggressiveness > 0) {
//            nObj = CellgenIterateAllShifts(px, accountBuf, width, height, obj, nObj);
//        }
//        //run 6 rounds (maximum possible times an OBJ can be divided)
//        if (aggressiveness > 0) {
//            for (int i = 0; i < CELLGEN_MAX_DIV; i++) {
//                //next, begin the subdivision step.
//                Arrays.fill(accountBuf,  0);
//                nObj = CellgenTryIterateSplit(px, accountBuf, width, height, aggressiveness, obj, nObj, CELLGEN_MAX_DIV);
//                nObj = CellgenTryCoalesce(obj, nObj);
//
//                //iterate OBJ shift again
//                if (aggressiveness > 0)
//                    nObj = CellgenIterateAllShifts(px, accountBuf, width, height, obj, nObj);
//
//                //remove any overlapped
//                nObj = CellgenTryRemoveOverlapping(px, accountBuf, width, height, obj, nObj);
//            }
//        }
//
//        //try to aid in coalescing: push objects towards the center and remove overlapping
//        if (aggressiveness > 0) {
//            CellgenCondenseObj(px, accountBuf, width, height, (xMin[0] + xMax[0]) / 2, (yMin[0] + yMax[0]) / 2, obj, nObj);
//            nObj = CellgenTryRemoveOverlapping(px, accountBuf, width, height, obj, nObj);
//
//            //reverse OBJ array and try splitting one last time
//            for (int i = 0; i < nObj / 2; i++) {
//                OBJ_BOUNDS aux = obj[i];
//                obj[i] = obj[nObj - i - 1];
//                obj[nObj - i - 1] = aux;
//            }
//
//            Arrays.fill(accountBuf,  0);
//            nObj = CellgenTryIterateSplit(px, accountBuf, width, height, aggressiveness, obj, nObj, CELLGEN_MAX_DIV);
//            nObj = CellgenTryRemoveOverlapping(px, accountBuf, width, height, obj, nObj);
//        }
//
//        //try aggressive coalesce
//        if (aggressiveness > 0) {
//            for (int i = 0; i < CELLGEN_MAX_DIV; i++) {
//                nObj = CellgenCoalesceAggressively(px, accountBuf, width, height, obj, nObj);
//            }
//        }
//
//        //if affine, split OBJ into squares.
//        if (affine) {
//            obj = CellgenEnsureRatio(obj, nObj, 1, new int[]{nObj});
//            nObj = obj.length;
//
//            //with new OBJ division, ensure none are redundant
//            if (aggressiveness > 0) {
//                nObj = CellgenIterateAllShifts(px, accountBuf, width, height, obj, nObj);
//            }
//
//            //shift in
//            nObj = CellgenCondenseObj(px, accountBuf, width, height, (xMin[0] + xMax[0]) / 2, (yMin[0] + yMax[0]) / 2, obj, nObj);
//        }
//
//        //order from big->small
//        Arrays.sort(obj, 0, nObj, (a, b) -> Integer.compare(b.width * b.height, a.width * a.height));
//
//        //remove objects that are over half overlapped by another, starting from big
//        if (aggressiveness > 0) {
//            nObj = CellgenTryRemoveOverlapping(px, accountBuf, width, height, obj, nObj);
//            if (!affine) {
//                nObj = CellgenRemoveHalfRedundant(px, accountBuf, width, height, obj, nObj);
//            }
//        }
//
//        //sort OBJ by position
//        Arrays.sort(obj, 0, nObj, Comparator.comparingInt((OBJ_BOUNDS o) -> o.y).thenComparingInt(o -> o.x));
//
//        //resize buffer and return
//        OBJ_BOUNDS[] result = Arrays.copyOf(obj, nObj);
//        pnObj[0] = nObj;
//        return result;
//    }
//
//    class OBJ_BOUNDS {
//
//        public int x;
//        public int y;
//        public int width;
//        public int height;
//
//        public OBJ_BOUNDS() {
//        }
//
//        public OBJ_BOUNDS(OBJ_BOUNDS other) {
//            this.x = other.x;
//            this.y = other.y;
//            this.width = other.width;
//            this.height = other.height;
//        }
//
//        public void set(OBJ_BOUNDS other){
//            this.x = other.x;
//            this.y = other.y;
//            this.width = other.width;
//            this.height = other.height;
//        }
//    }
//}
