# NCER
Nintendo CEll Resource (NCER/RECN)

## Header
- [uses Generic Header](/docs/files/nitro/subsections/Generic%20Header%20Format.md)
- magic ID is #RECN (0x5245434E)

## Data
- contains 3 sub-sections

###  #1 Section - CELL Bank (KBEC)
| Offset | Length | Name                  | Variable                | Description                                                                                                                             |
|--------|--------|-----------------------|-------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| 0x0    | 0x4    | Magic ID              | cebkId                  | #KBEC (0x4B424543)                                                                                                                      |
| 0x4    | 0x4    | Section Size          | cebkSectionSize         | Size of this section, including the header.                                                                                             |
| 0x8    | 0x4    | No of Banks           | cebkNumCells            |                                                                                                                                         | 
| 0xA    | 0x2    | Extended Flag         | cebkBankType            | If 1, data is extended.                                                                                                                 |
| 0xC    | 0x4    | Data Offset           | cebkDataOffset          | Relative to start of Section + 8.                                                                                                       |
| 0x10   | 0x4    | Flags                 | cebkMappingType         | Bit 0-1 << 1 Tile Index Offset<br>Bit 2 If 1, use sub-images                                                                            |
| 0x10   | 0x4    | Boundary Size         |                         | Specifies the the area in which the image can be drawn multiplied by 64. ie a boundary size of 2 means that the area is 128x128 pixels. |
| 0x14   | 0x4    | Partition Data Offset | cebkPartitionDataOffset | Only present if not zero.                                                                                                               |
| 0x18   | 0x8    | Padding               | cebkUnused              |                                                                                                                                         |
| DATA   |        | Per Cell              | cells/cellPojo          | Cell Table (8/16 bytes each)                                                                                                            |
| 0x0    | 0x2    | Number of OAMs        | nOAMEntries (local)     | The number of cells that make up an image.                                                                                              |
| 0x2    | 0x2    | Unknown               | cellAttr (local)        |                                                                                                                                         |
| 0x4    | 0x4    | Cell Data Offset      | attrOffset  (local)     | This offset is RELATIVE to the end of the Cell Table.                                                                                   |
| 0x6    | 0x2    | X Max                 | maxX                    | Only present in Extended Mode                                                                                                           |
| 0x8    | 0x2    | Y Max                 | maxY                    | Only present in Extended Mode                                                                                                           |
| 0xA    | 0x2    | X Min                 | minX                    | Only present in Extended Mode                                                                                                           |
| 0xC    | 0x2    | Y Min                 | minY                    | Only present in Extended Mode                                                                                                           |
| DATA   |        |                       |                         | OAM Data (Starts at Number of Cells * 8/16)                                                                                             |
| 0x0    | 0x2    | OBJ Attribute 0       | attrs\[index\]\[0\]     | see below                                                                                                                               |
| 0x2    | 0x2    | OBJ Attribute 1       | attrs\[index\]\[1\]     | see below                                                                                                                               |
| 0x4    | 0x2    | OBJ Attribute 2       | attrs\[index\]\[2\]     | see below                                                                                                                               |

#### OAM attributes

*Attribute 0*

| Offset | Length (bits) | Name                  | Variable      | Description                                                   |
|--------|---------------|-----------------------|---------------|---------------------------------------------------------------|
| 0x0    | 7             | Y Position (Signed)   | yCoord        | Range (-128, 127)                                             |
| 0x8    | 1             | Rotation/Scaling Flag | rotation      | R/S Flag                                                      |
| 0x9    | 1             | Double-Size Flag      | doubleSize    | If R/S Flag=1                                                 |
| 0x9    | 1             | OBJ Disable Flag      | objDisable    | If R/S Flag=0                                                 |
| 0xA    | 2             | OBJ Mode              | mode          | 0 = normal<br>1 = semi-trans<br>2 = window<br>3 = invalid     |
| 0xC    | 1             | Mosaic Flag           | mosaic        | Should not be set in NCER?                                    |
| 0xD    | 1             | Mosaic Flag           | characterBits | 0 = 4bit<br>1 = 8bit                                          |
| 0xE    | 2             | OBJ Shape             | shape         | 0 = square<br> 1 = horizontal<br> 2 = vertial<br> 3 = invalid |

*Attribute 1*

| Offset | Length (bits) | Name                    | Variable        | Description                                    |
|--------|---------------|-------------------------|-----------------|------------------------------------------------|
| 0x0    | 8             | X Position (Unsigned)   | xCoord          | Range (-256, 255), subtract 0x200 if >= 0x100? |
| 0x9    | 5             | R/S Parameter Selection | rotationScaling | If R/S Flag=1                                  |
| 0x9    | 3             | Unused                  | unused          | If R/S Flag=0                                  |
| 0xC    | 1             | Horizontal Flip         | flipX           | If R/S Flag=0                                  |
| 0xD    | 1             | Vertical Flip           | flipY           | If R/S Flag=0                                  |
| 0xE    | 2             | OBJ Size                | size            |                                                |

*Attribute 2*

| Offset | Length (bits) | Name            | Variable   | Description                              |
|--------|---------------|-----------------|------------|------------------------------------------|
| 0x0    | 9             | Tile Index      | tileOffset |                                          |
| 0xA    | 2             | Priority        | priority   | relative to BG, normally not set in NCER |
| 0xC    | 3             | Palette Index   | palette    |                                          |


#### OAM Sizing:
The height and width of the cell is determined by referencing to 2D array.

| Size/<br>Shape | 00   | 01    | 10    | 11    |
|----------------|------|-------|-------|-------|
| 00             | 8x8  | 16x16 | 32x32 | 64x64 |
| 01             | 16x8 | 32x8  | 32x16 | 64x32 |
| 10             | 8x16 | 8x32  | 16x32 | 32x64 |
| 11             | ??   | ??    | ??    | ??    |

So if a cell has a Shape of 0 (square) and a Size of 3 it's size is 64x64.

###  #2 Section - Animation Label (LABL)
| Offset | Length | Name         | Variable        | Description                                 |
|--------|--------|--------------|-----------------|---------------------------------------------|
| 0x0    | 0x4    | Magic ID     | lablID          | #LBAL (0x4C42414C)                          |
| 0x4    | 0x4    | Section Size | lablSectionSize | Size of this section, including the header. |
| DATA   |        |              | labels          |                                             |
| 0x8    | 0x4    | Label Offset |                 | Name Offsets (!= always equal # objects.)   |
| DATA   |        |              |                 |                                             |	  	  	 
|        |        | Label Name   |                 | Terminated by (0x0)                         |


###  #3 Section - (TXEU)
| Offset | Length | Name         | Variable        | Description                                 |
|--------|--------|--------------|-----------------|---------------------------------------------|
| 0x0    | 0x4    | Magic ID     | uextID          | #TXEU (0x54584555)                          |
| 0x4    | 0x4    | Section Size | uextSectionSize | Size of this section, including the header. |
| 0x8    | ???    | Unknown      | uextData        |                                             |