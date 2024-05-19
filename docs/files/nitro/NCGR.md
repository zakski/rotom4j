# NCGR
Nintendo Character Graphic Resource (NCGR/RGCN)

## Header
- [uses Generic Header](/docs/files/nitro/subsections/Generic%20Header%20Format.md)
- magic ID is #RGCN (0x5247434E)

## Data
- contains 2 sub-sections

###  #1 Section - Character Data (RAHC)
| Offset | Length | Name           | Description                                                       |
|--------|--------|----------------|-------------------------------------------------------------------|
| 0x0    | 0x4    | Magic ID       | #RAHC (0x52414843)                                                |
| 0x4    | 0x1    | Header Size    | Should always be (0x20)                                           |
| 0x4    | 0x4    | Section Size   | Size of this section, including the header.                       |
| 0x8    | 0x2    | Tile Count     | Multiplied by 1024 gets the total number of pixels in the file.   |
| 0xA    | 0x2    | Tile Size      | Always (0x20)                                                     |
| 0xC    | 0x4    | Tile Bit Depth | 3 = 4 Bits, 4 = 8 Bits                                            |
| 0x10   | 0x8    | Padding?       | Always (0x0)                                                      |
| 0x18   | 0x4    | Tile Data Size | Divided by 1024 should equal Tile Count.                          |
| 0x1C   | 0x4    | Unknown        | Always (0x24)                                                     |
| DATA   |        |                | Tile Data stored as [NTFT](/docs/files/nitro/subsections/NTFT.md) |

###  #2 Section - (SOPC)
| Offset | Length | Name         | Description                                      |
|--------|--------|--------------|--------------------------------------------------|
| 0x0    | 0x4    | Magic ID     | #SOPC (0x534F5043)                               |
| 0x4    | 0x4    | Section Size | Should always be (0x10)                          |
| 0x8    | 0x4    | Padding      | Always (0x0)                                     |
| 0xC    | 0x2    | Tile Size?   | Always (0x20)                                    |
| 0xE    | 0x2    | Tile Count   | Is always identical to Tile Count in #1 Section. |