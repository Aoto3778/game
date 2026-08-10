package jp.aoto.zerosum.core.engine

import jp.aoto.zerosum.core.model.EnemyTier
import jp.aoto.zerosum.core.model.HeroClass

/** Central tunable numbers consumed by both the engine and simulator. */
public object Balance {
    public const val STARTING_ENERGY: Int = 3
    public const val HAND_SIZE: Int = 5
    public const val STARTING_GOLD: Int = 80
    public const val DRAFT_SIZE: Int = 5
    public const val OVERLOAD_PERCENT_PER_STACK: Int = 25

    /** Starting HP by hero class. */
    public fun startingHp(heroClass: HeroClass): Int = when (heroClass) {
        HeroClass.CONDUCTOR -> 72
        HeroClass.BREAKER -> 84
        HeroClass.RESOLVER -> 66
    }

    /** Number of shared pool cards incorporated by an encounter. */
    public fun enemyPoolTake(tier: EnemyTier, ascension: Int): Int {
        val base = when (tier) {
            EnemyTier.NORMAL -> 2
            EnemyTier.ELITE -> 4
            EnemyTier.BOSS -> 6
        }
        val tierBonus = (if (ascension >= 10) 1 else 0) + (if (ascension >= 20) 1 else 0)
        return base + tierBonus
    }
}
