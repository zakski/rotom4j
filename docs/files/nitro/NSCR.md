# NSCR
Nintendo SCreen Resource (NSCR/RCSN)

## Header
- [uses Generic Header](/docs/files/nitro/subsections/Generic%20Header%20Format.md)
- magic ID is #RCSN (0x5243534E)

## Data
- contains only 1 sub-section

###  #1 Section - Screen Data (NRCS)
| Offset | Length | Name             | Description                                                          |
|--------|--------|------------------|----------------------------------------------------------------------|
| 0x0    | 0x4    | Magic ID         | #NRCS (0x4E524353)                                                   |
| 0x4    | 0x1    | Header Size      | Should always be (0x14)                                              |
| 0x4    | 0x4    | Section Size     | Size of this section, including the header.                          |
| 0x8    | 0x2    | Screen Width     | Value is in pixels.                                                  |
| 0xA    | 0x2    | Screen Height    | Value is in pixels.                                                  |
| 0xC    | 0x4    | Padding          | Always (0x0)                                                         |
| 0x10   | 0x4    | Screen Data Size |                                                                      |
| DATA   |        |                  |  Screen Data stored as [NTFS](/docs/files/nitro/subsections/NTFS.md) |