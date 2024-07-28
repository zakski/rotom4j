# NCGR
Nintendo Character Graphic Resource (NCGR/RGCN)

## Header
- [uses Generic Header](/docs/files/nitro/subsections/Generic%20Header%20Format.md)
- magic ID is #RGCN (0x5247434E)

## Data
- contains 2 sub-sections

###  #1 Section - Character Data (RAHC)
| Offset | Length | Name             | Variable         | Description                                                       |
|--------|--------|------------------|------------------|-------------------------------------------------------------------|
| 0x0    | 0x4    | Magic ID         | charMagic        | #RAHC (0x52414843)                                                |
| 0x4    | 0x4    | Section Size     | charSectionSize  | Size of this section, including the header.                       |
| 0x8    | 0x2    | Tile Height      | charTilesHeight  | Height unless 0xFFFF.                                             |
| 0xA    | 0x2    | Tile Width       | charTilesWidth   | Width unless 0xFFFF.                                              |
| 0xC    | 0x4    | Tile Bit Depth   | charBitDepth     | 3 = 4 Bits, 4 = 8 Bits                                            |
| 0x10   | 0x2    | Unknown Height   | charUnknown1     | Set when Height is 0xFFFF.                                        |
| 0x12   | 0x2    | Unknown Width    | charMappingType  | Set when Width is 0xFFFF.                                         |
| 0x14   | 0x4    | Tiled Flag       | charTiledFlag    | If not set, image is tiled.                                       |
| 0x14   | 0x1    | Tiled Flag       | charTiledFlag    | If not set, image is tiled.                                       |
| 0x15   | 0x1    | Partitioned Flag |                  | If set, image is partitioned.                                     |
| 0x16   | 0x2    | Unknown          |                  | only seen 0x0 here.                                               |
| 0x18   | 0x4    | Tile Data Size   | charTiledataSize | Divided by 1024 should equal Tile Count.                          |
| 0x1C   | 0x4    | Data Offset      |                  |                                                                   |
| DATA   |        |                  |                  | Tile Data stored as [NTFT](/docs/files/nitro/subsections/NTFT.md) |

###  #2 Section - (SOPC)
| Offset | Length | Name         | Variable        | Description                                      |
|--------|--------|--------------|-----------------|--------------------------------------------------|
| 0x0    | 0x4    | Magic ID     | sopcMagic       | #SOPC (0x534F5043)                               |
| 0x4    | 0x4    | Section Size | sopcSectionSize | Should always be (0x10)                          |
| 0x8    | 0x4    | Padding      | sopcUnknown1    | Always (0x0)                                     |
| 0xC    | 0x2    | Tile Size?   | sopcCharSize    | Always (0x20)                                    |
| 0xE    | 0x2    | Tile Count   | sopcNChars      | Is always identical to Tile Count in #1 Section. |