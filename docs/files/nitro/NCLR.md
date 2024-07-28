# NCLR
Nintendo Color Resource (NCLR/RLCN)

## Header
- [uses Generic Header](/docs/files/nitro/subsections/Generic%20Header%20Format.md)
- magic ID is #RLCN (0x524C434E)

## Data
- contains 2 sub-sections

###  #1 Section - Palette Data (TTLP)
| Offset | Length | Name               | Variable                   | Description                                                                                                           |
|--------|--------|--------------------|----------------------------|-----------------------------------------------------------------------------------------------------------------------|
| 0x0    | 0x4    | Magic ID           | paletteMagic (local)       | #TTLP (0x54544C50)                                                                                                    |
| 0x4    | 0x4    | Section Size       | paletteSectionSize (local) | Size of this section, including the header.                                                                           |
| 0x8    | 0x2    | Palette Bit Depth  | bitDepth                   | 3 = 4 Bits, 4 = 8 Bits                                                                                                |
| 0xA    | 0x1    | Padding?           | compNum (local)            |                                                                                                                       |
| 0xB    | 0x1    | Padding?           | skipped (local)            |                                                                                                                       |
| 0xC    | 0x4    | Padding?           | paletteUnknown1 (local)    | Always ( 0x000000)                                                                                                    |
| 0x10   | 0x4    | Palette Data Size  | paletteLength (local)      | Size of Palette Data in bytes. if (0x200-size > 0) then size = 0x200-size also if the bit depth is 8 palette is 0x200 |
| 0x14   | 0x4    | Colors Per Palette | colorStartOffset (local)   |                                                                                                                       |
| DATA   |        |                    | colors                     | Palette Data stored as [NTFP](/docs/files/nitro/subsections/NTFP.md)                                                  |

###  #2 Section - Palette Count Map? (PMCP)
| Offset | Length | Name          | Variable | Description                                            |
|--------|--------|---------------|----------|--------------------------------------------------------|
| 0x0    | 0x4    | Magic ID      |          | #PMCP (0x504D4350)                                     |
| 0x4    | 0x4    | Section Size  |          | Should always be (0x12).                               |
| 0x8    | 0x2    | Palette Count |          | Number of palettes in file.                            |
| 0xA    | 0x6    | Unknown       |          | Always (0xEFBE080000)                                  |
| DATA   | 0x2    | Palette ID    |          | Simple ID number for each palette (starting from 0x0). |