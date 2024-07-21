# NANR
Nintendo Animation Resource (NANR/RNAN)

## Header
- [uses Generic Header](/docs/files/nitro/subsections/Generic%20Header%20Format.md)
- magic ID is #RNAN (0x524E414E)

## Data
- contains 2 sub-sections

## #1 Animation BaNK Section (ABNK)

| Offset | Length | Name                            | Variable | Description                                 |
|--------|--------|---------------------------------|----------|---------------------------------------------|
| 0x0    | 0x4    | Magic ID                        |          | #KNBA (0x4B4E4241)                          |
| 0x4    | 0x4    | Section Size                    |          | Size of this section, including the header. |
| 0x8    | 0x2    | Animations Count                |          | Each animation is 16 bytes.                 |
| 0xA    | 0x2    | Frame Count                     |          | Each animation is 8 bytes.                  |
| 0xC    | 0x4    | Animations Block Offset         |          | 0x18. Relative to start of Section + 0x8.   |
| 0x10   | 0x4    | Frame Header Block Offset       |          | Relative to start of Section + 0x8.         |
| 0x14   | 0x4    | Frame Data Block Offset         |          | Relative to start of Section + 0x8.         |
|        |        |                                 |          |                                             |
| 0x18   | 0x8    | Padding                         |          | 0x0.                                        |
|        |        |                                 |          |                                             |
| DATA   |        | Animation Block                 |          | x Animations Count                          |
| 0x0    | 0x4    | Frame Count                     |          |                                             |
| 0x4    | 0x2    | Data Type                       |          | 0 = 4 bytes<br>1 = 16 bytes<br>2 = 8 bytes  |
| 0x6    | 0x2    | UNKNOWN                         |          |                                             |
| 0x8    | 0x4    | UNKNOWN                         |          |                                             |
| 0xC    | 0x4    | First Frame Header Offset       |          | Relative to start of Header Block.          |
|        |        |                                 |          |                                             |
|        | 0x1    | Padding                         |          | 0x0.                                        |
|        |        |                                 |          |                                             |
| DATA   |        | Frame Header Block              |          | x Frame Count                               |
| 0x0    | 0x4    | Frame Data Offset               |          | Relative to start of Frame Data Block.      |
| 0x4    | 0x2    | Frame Duration                  |          | ~= (1/60)s?                                 |
| 0x6    | 0x2    | UNKNOWN Constant                |          | Always (0xBEEF)                             |
|        |        |                                 |          |                                             |
|        | 0x1    | Padding                         |          | 0x0.                                        |
|        |        |                                 |          |                                             |
| DATA   |        | Frame Data Block                |          | x Frame  Count                              |
| 0x0    | 0x2    | Cell Index                      |          | Relative to start of Frame Data Block.      |
|        |        | Type 0 - Normal                 |          |                                             |
| 0x2    | 0x2    | Garbage                         |          | Last frame will be 0xCCCC                   |
|        |        | Type 1 - Transform/Displacement |          |                                             |
| 0x2    | 0xA    | Transform                       |          |                                             |
| 0xC    | 0x2    | X Displacement                  |          |                                             |
| 0xE    | 0x2    | X Displacement                  |          |                                             |
|        |        | Type 2 - Displacement           |          |                                             |
| 0x2    | 0x2    | Constant                        |          | 0xBEEF                                      |
| 0x4    | 0x2    | X Displacement                  |          |                                             |
| 0x6    | 0x2    | X Displacement                  |          |                                             |

## Transform Details
The transform is usually of this format: {B, A, C, D, E}, with: E = 0

A,B,C,D as used in the formula in the bottom of this section in GBATEK

However C and E can be 0xFFFF sometimes (pokemon B/W file 505, pikachu), which seem to be some special marker. Not sure 
yet what they mean.

## LCD I/O BG Rotation/Scaling
* 4000028h - BG2X_L - BG2 Reference Point X-Coordinate, lower 16 bit (W)
* 400002Ah - BG2X_H - BG2 Reference Point X-Coordinate, upper 12 bit (W)
* 400002Ch - BG2Y_L - BG2 Reference Point Y-Coordinate, lower 16 bit (W)
* 400002Eh - BG2Y_H - BG2 Reference Point Y-Coordinate, upper 12 bit (W)

These registers are replacing the BG scrolling registers which are used for Text mode, ie. the X/Y coordinates specify 
the source position from inside of the BG Map/Bitmap of the pixel to be displayed at upper left of the GBA display. The 
normal BG scrolling registers are ignored in Rotation/Scaling and Bitmap modes.

| Bit   | Expl.                       |
|-------|-----------------------------|
| 0-7   | Fractional portion (8 bits) |
| 8-26  | Integer portion (19 bits)   |
| 27    | Sign (1 bit)                |
| 28-31 | Not used                    |

Because values are shifted left by eight, fractional portions may be specified in steps of 1/256 pixels (this would be 
relevant only if the screen is actually rotated or scaled). Normal signed 32bit values may be written to above
registers (the most significant bits will be ignored and the value will be cut-down to 28bits, but this is no actual 
problem because signed values have set all MSBs to the same value).

### Internal Reference Point Registers
The above reference points are automatically copied to internal registers during each vblank, specifying the origin for 
the first scanline. The internal registers are then incremented by dmx and dmy after each scanline. 

Caution: Writing to a reference point register by software outside of the Vblank period does immediately copy the new 
value to the corresponding internal register, that means: in the current frame, the new value specifies the origin of 
the <current> scanline (instead of the topmost scanline).

* 4000020h - BG2PA - BG2 Rotation/Scaling Parameter *A* (alias dx) (W)
* 4000022h - BG2PB - BG2 Rotation/Scaling Parameter *B* (alias dmx) (W)
* 4000024h - BG2PC - BG2 Rotation/Scaling Parameter *C* (alias dy) (W)
* 4000026h - BG2PD - BG2 Rotation/Scaling Parameter *D* (alias dmy) (W)

| Bit  | Expl.                       |
|------|-----------------------------|
| 0-7  | Fractional portion (8 bits) |
| 8-14 | Integer portion (7 bits)    |
| 15   | Sign (1 bit)                |

See below for details.

400003Xh - BG3X_L/H, BG3Y_L/H, BG3PA-D - BG3 Rotation/Scaling Parameters
Same as above BG2 Reference Point, and Rotation/Scaling Parameters, for BG3 respectively.

#### dx (PA) and dy (PC)
When transforming a horizontal line, dx and dy specify the resulting gradient and magnification for that line.

For example:

Horizontal line, length=100, dx=1, and dy=1. The resulting line would be drawn at 45 degrees, f(y)=1/1*x.

Note that this would involve that line is magnified, the new length is SQR(100^2+100^2)=141.42. Yup, exactly - that's 
the old a^2 + b^2 = c^2 formula.

#### dmx (PB) and dmy (PD)
These values define the resulting gradient and magnification for transformation of vertical lines. However, when 
rotating a square area (which is surrounded by horizontal and vertical lines), then the desired result should be usually 
a rotated <square> area (ie. not a parallelogram, for example).

Thus, dmx and dmy must be defined in direct relationship to dx and dy, taking the example above, we'd have to set 
dmx=-1, and dmy=1, f(x)=-1/1*y.

#### Area Overflow
In result of rotation/scaling it may often happen that areas outside of the actual BG area become moved into the LCD 
viewport. Depending of the Area Overflow bit (BG2CNT and BG3CNT, Bit 13) these areas may be either displayed (by 
wrapping the BG area), or may be displayed transparent.

This works only in BG modes 1 and 2. The area overflow is ignored in Bitmap modes (BG modes 3-5), the outside of the 
Bitmaps is always transparent.

## --- more details and confusing or helpful formulas ---
The following parameters are required for Rotation/Scaling

* Rotation Center X and Y Coordinates (x0,y0)
* Rotation Angle                      (alpha)
* Magnification X and Y Values        (xMag,yMag)

The display is rotated by 'alpha' degrees around the center.
The displayed picture is magnified by 'xMag' along x-Axis (Y=y0) and 'yMag' along y-Axis (X=x0).

### Calculating Rotation/Scaling Parameters A-D

* A = Cos (alpha) / xMag    ;distance moved in direction x, same line
* B = Sin (alpha) / xMag    ;distance moved in direction x, next line
* C = Sin (alpha) / yMag    ;distance moved in direction y, same line
* D = Cos (alpha) / yMag    ;distance moved in direction y, next line


Calculating the position of a rotated/scaled dot using the following expressions,

* x0,y0    Rotation Center
* x1,y1    Old Position of a pixel (before rotation/scaling)
* x2,y2    New position of above pixel (after rotation scaling)
* A,B,C,D  BG2PA-BG2PD Parameters (as calculated above)

the following formula can be used to calculate x2,y2:

* x2 = A(x1-x0) + B(y1-y0) + x0
* y2 = C(x1-x0) + D(y1-y0) + y0

## Other Sections
See LABL and UEXT in NCER