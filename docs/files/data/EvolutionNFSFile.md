# Evolution Data
* 1 to n Evolution Entries

## Evolution Entry
* 2 bytes - method
* 2 bytes - requirement
* 2 bytes - species

## Evolution Method
*  0 - None
*  1 - High Friendship
*  2 - High Friendship, Day
*  3 - High Friendship, Night
*  4 - Level Up
*  5 - Traded
*  6 - Traded (while holding an item)
*  7 - By Item
*  8 - Attack is higher than its Defense
*  9 - Attack and Defense stats are equal
* 10 - Defense is higher than its Attack
* 11 - upper half of its personality value, pw mod 10, less than or equal to 4 (wurmple->silcoon)
* 12 - upper half of its personality value, pw mod 10, greater than 4 (wurmple->cascoon)
* 13 - Level Up (nincada->ninjask)
* 14 - Level Up, if empty slot in party and an extra Poké Ball.  (nincada->shedinja)
* 15 - Level Up, with its Beautiful condition high enough

## Evolution Requirement
Friendship Required/Level Required/Item Required, etc.