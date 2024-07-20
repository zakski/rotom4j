# NCER
Nintendo CELL Resource (NCER/RECN)

## Header
- [uses Generic Header](/docs/files/nitro/subsections/Generic%20Header%20Format.md)
- magic ID is #RECN (0x5245434E)

## Data
- contains 3 sub-sections

###  #1 Section - CELL Bank (KBEC)
| Offset | Length | Name             | Description                                                                                                                                                                                             |
|--------|--------|------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 0x0    | 0x4    | Magic ID         | #KBEC (0x4B424543)                                                                                                                                                                                      |
| 0x4    | 0x4    | Section Size     | Size of this section, including the header.                                                                                                                                                             |
| 0x8    | 0x4    | Number of Images |                                                                                                                                                                                                         | 
| 0xC    | 0x4    | Unknown          | Always (0x12)                                                                                                                                                                                           |
| 0x10   | 0x4    | Boundary Size    | Specifies the the area in which the image can be drawn multiplied by 64. ie a boundary size of 2 means that the area is 128x128 pixels.                                                                 |
| 0x14   | 0x12   | Padding          | Always (0x0)                                                                                                                                                                                            |
| DATA   |        |                  | Cell Table (8 bytes each)                                                                                                                                                                               |
| 0x0    | 0x2    | Number of Cells  | The number of cells that make up an image.                                                                                                                                                              |
| 0x2    | 0x2    | Unknown          |                                                                                                                                                                                                         | 
| 0x4    | 0x4    | Cell Data Offset | This offset is RELATIVE to the end of the Cell Table.                                                                                                                                                   |
| DATA   |        |                  | Cell Data (Starts at Number of Cells * 8 \| each cell is made up of 6 bytes)                                                                                                                            |
| 0x0    | 0x1    | Row Data         | RR = Row (Note that the row order is 2, 3, 0, 1)<br>Y1 = Y Offset from the top of the row. (Multiplied by 16)<br>Y2 = Y Offset from the top of the row. (Multiplied by 4)                               |
| 0x1    | 0x1    | Cell Width       | Format is (WW????FF)<br>WW = Width of the cell.<br>FF = Vertical Flag (Not sure if this is used or not).                                                                                                |
| 0x2    | 0x1    | Column Data      | Format is (CCX1X2??)<br>CC = Column (Note that the column order is 2, 3, 0, 1)<br>X1 = X Offset from the top of the row. (Multiplied by 16)<br>X2 = X Offset from the top of the row. (Multiplied by 4) |
| 0x3    | 0x1    | Cell Height      | Format is (HH????FF)<br>HH = Height of the cell.<br>FF = Horizontal flag.                                                                                                                               |

#### Rows/Columns:
Row/Column are numbered like that because for regular images, they shouldn't exceed the size of 0 & 1 (128x128 pixels), 
the first two rows/columns are meant for larger images up to 256x256 pixels.

The height and width of the cell is determined by referencing to 2D array. I am yet to find any cells that use nodes 
with ??.

| Height/<brWidth | 00   | 01    | 10    | 11    |
|-----------------|------|-------|-------|-------|
| 00              | 8x8  | 16x16 | 32x32 | 64x64 |
| 01              | 16x8 | 32x8  | 32x16 | 64x32 |
| 10              | 8x16 | 8x32  | 16x32 | 32x64 |
| 11              | ??   | ??    | ??    | ??    |

So if a cell has a WW of 0 and a HH of 3 it's size is 64x64.

#### X & Y Offsets:
I am not sure if the Y Offset is affected in the same way, but if the position of a cell is within the far right column 
and the FF (Flag) value is equal to 1. The offset is subtracted by 256.

| Offset | Length | Name        | Description                                           |
|--------|--------|-------------|-------------------------------------------------------|
| 0x4    | 0x2    | Tile Offset | The offset into the tile data where this cell starts. |

###  #2 Section - Animation Label (LABL)
| Offset | Length | Name         | Description                                 |
|--------|--------|--------------|---------------------------------------------|
| 0x0    | 0x4    | Magic ID     | #LBAL (0x4C42414C)                          |
| 0x4    | 0x4    | Section Size | Size of this section, including the header. |
| DATA   |        |              |                                             |
| 0x8    | 0x4    | Label Offset |                                             |
| DATA   |        |              |                                             |	  	  	 
|        |        | Label Name   | Terminated by (0x0)                         |

###  #3 Section - (TXEU)
| Offset | Length | Name         | Description                                 |
|--------|--------|--------------|---------------------------------------------|
| 0x0    | 0x4    | Magic ID     | #TXEU (0x54584555)                          |
| 0x4    | 0x4    | Section Size | Size of this section, including the header. |
| 0x8    | 0x4    | Unknown      | 0 or 1                                      |