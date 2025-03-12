package com.szadowsz.rotom4j.file.data.evo.data;

/**
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
 */
public enum EvoMethod {
    EVO_NONE,
    EVO_FRIENDSHIP,         // Pokémon levels up with friendship ≥ 220
    EVO_FRIENDSHIP_DAY,     // Pokémon levels up during the day with friendship ≥ 220
    EVO_FRIENDSHIP_NIGHT,   // Pokémon levels up at night with friendship ≥ 220
    EVO_LEVEL,              // Pokémon reaches the specified level
    EVO_TRADE,              // Pokémon is traded
    EVO_TRADE_ITEM,         // Pokémon is traded while it's holding the specified item
    EVO_ITEM,               // specified item is used on Pokémon
    EVO_LEVEL_ATK_GT_DEF,   // Pokémon reaches the specified level with attack > defense
    EVO_LEVEL_ATK_EQ_DEF,   // Pokémon reaches the specified level with attack = defense
    EVO_LEVEL_ATK_LT_DEF,   // Pokémon reaches the specified level with attack < defense
    EVO_LEVEL_SILCOON,      // Pokémon reaches the specified level with a Silcoon personality value
    EVO_LEVEL_CASCOON,      // Pokémon reaches the specified level with a Cascoon personality value
    EVO_LEVEL_NINJASK,      // Pokémon reaches the specified level (special value for Ninjask)
    EVO_LEVEL_SHEDINJA,     // Pokémon reaches the specified level (special value for Shedinja)
    EVO_BEAUTY; // Pokémon levels up with beauty ≥ specified value
}
