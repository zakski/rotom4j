# Personal Stats Data   
* 1 byte - hp
* 1 byte - attack
* 1 byte - defense
* 1 byte - speed
* 1 byte - special attack
* 1 byte - special defence
* 1 byte - pokemon type 1
* 1 byte - pokemon type 2
* 1 byte - catch rate
* 1 byte - base exp yield
* 2 bytes - yield stats value
* 2 bytes - uncommon item number
* 2 bytes - rare item number
* 1 byte - gender Ratio
* 1 byte - egg Cycles
* 1 byte - base Happiness
* 1 byte - exp Rate
* 1 byte - egg Group 1
* 1 byte - egg Group 2
* 1 byte - pokemon ability 1
* 1 byte - pokemon ability 2
* 1 byte - run Chance
* 1 byte - color & flip byte
* 2 bytes - padding
* 16 bytes - HM/TM Compatibility bits

## Yield Stats
* y & 0x03              - hp effort value
* (y >> 2) & 0x03       - attack effort value
* (y >> 4) & 0x03       - defense effort value
* (y >> 6) & 0x03       - speed effort value
* (y >> 8) & 0x03       - special attack effort value
* (y >> 10) & 0x03      - special defence effort value
  
## Color & Flip byte
* dexColor - colorFlip & 0x7F;
* flip - ((colorFlip & 0x80) >> 7) == 1;
