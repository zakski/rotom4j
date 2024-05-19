# Generic Header Format

| Offset | Length | Name               | Description                                     |
|--------|--------|--------------------|-------------------------------------------------|
| 0x0    | 0x4    | Magic ID           | Identifies the file format.                     |
| 0x4    | 0x2    | Bom                | Always (0xFFFE)                                 |
| 0x6    | 0x2    | Version            | Always (0x0001)                                 |
| 0x8    | 0x4    | File Size          | Size of this section, including the header.     |
| 0xC    | 0x2    | Header Size        | Size of this header. (Should always equal 0x10) |
| 0xE    | 0x2    | Number of Sections | The number of sub-sections in this file.        |

