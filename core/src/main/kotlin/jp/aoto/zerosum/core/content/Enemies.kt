package jp.aoto.zerosum.core.content

import jp.aoto.zerosum.core.model.BossRule
import jp.aoto.zerosum.core.model.EnemyDefinition
import jp.aoto.zerosum.core.model.EnemyTier

internal object Enemies {
    private fun deck(vararg ids: String): List<String> = ids.toList().also {
        require(it.size == 8) { "Enemy base deck must contain exactly eight cards" }
    }

    private fun enemy(
        id: String,
        tier: EnemyTier,
        hp: Int,
        act: Int,
        cards: List<String>,
        bossRule: BossRule = BossRule.NONE,
        profile: String = "balanced",
    ) = EnemyDefinition(id, "enemy_${id}_name", tier, hp, cards, act, bossRule, profile)

    internal val all = listOf(
        enemy("training_drone", EnemyTier.NORMAL, 38, 1, deck("enemy_jab", "enemy_guard", "enemy_jab", "enemy_overload", "enemy_jab", "enemy_guard", "enemy_shock", "enemy_heavy")),
        enemy("stray_coil", EnemyTier.NORMAL, 34, 1, deck("enemy_shock", "enemy_jab", "enemy_multi", "enemy_guard", "enemy_shock", "enemy_jab", "enemy_multi", "enemy_heavy")),
        enemy("copper_wasp", EnemyTier.NORMAL, 31, 1, deck("enemy_multi", "enemy_jab", "enemy_charge", "enemy_multi", "enemy_guard", "enemy_jab", "enemy_multi", "enemy_execute"), profile = "aggressive"),
        enemy("relay_rat", EnemyTier.NORMAL, 42, 1, deck("enemy_drain", "enemy_guard", "enemy_jab", "enemy_drain", "enemy_overload", "enemy_jab", "enemy_guard", "enemy_heavy")),
        enemy("static_hound", EnemyTier.NORMAL, 40, 1, deck("enemy_jab", "enemy_charge", "enemy_heavy", "enemy_guard", "enemy_jab", "enemy_shock", "enemy_heavy", "enemy_multi"), profile = "aggressive"),
        enemy("fuse_mite", EnemyTier.NORMAL, 29, 1, deck("enemy_guard", "enemy_charge", "enemy_armor_hit", "enemy_jab", "enemy_guard", "enemy_armor_hit", "enemy_shock", "enemy_heavy")),
        enemy("arc_monk", EnemyTier.NORMAL, 46, 1, deck("enemy_shock", "enemy_guard", "enemy_jab", "enemy_disrupt", "enemy_multi", "enemy_guard", "enemy_jam", "enemy_heavy")),
        enemy("battery_thief", EnemyTier.NORMAL, 37, 1, deck("enemy_disrupt", "enemy_jab", "enemy_drain", "enemy_guard", "enemy_overload", "enemy_jab", "enemy_drain", "enemy_execute")),
        enemy("rusted_guard", EnemyTier.NORMAL, 58, 2, deck("enemy_fortify", "enemy_armor_hit", "enemy_guard", "enemy_heavy", "enemy_fortify", "enemy_armor_hit", "enemy_overload", "enemy_execute")),
        enemy("pulse_turret", EnemyTier.NORMAL, 49, 2, deck("enemy_charge", "enemy_multi", "enemy_multi", "enemy_guard", "enemy_charge", "enemy_heavy", "enemy_multi", "enemy_execute"), profile = "aggressive"),
        enemy("cable_serpent", EnemyTier.NORMAL, 54, 2, deck("enemy_jam", "enemy_jab", "enemy_leech", "enemy_shock", "enemy_heavy", "enemy_leech", "enemy_guard", "enemy_execute")),
        enemy("grid_leech", EnemyTier.NORMAL, 62, 2, deck("enemy_drain", "enemy_leech", "enemy_guard", "enemy_drain", "enemy_overload", "enemy_leech", "enemy_fortify", "enemy_heavy")),
        enemy("echo_unit", EnemyTier.NORMAL, 51, 2, deck("enemy_jab", "enemy_jab", "enemy_guard", "enemy_guard", "enemy_shock", "enemy_shock", "enemy_heavy", "enemy_heavy")),
        enemy("breaker_adept", EnemyTier.NORMAL, 57, 2, deck("enemy_disrupt", "enemy_guard", "enemy_rupture", "enemy_fortify", "enemy_overload", "enemy_heavy", "enemy_disrupt", "enemy_execute")),
        enemy("charge_mimic", EnemyTier.NORMAL, 71, 3, deck("enemy_charge", "enemy_surge", "enemy_heavy", "enemy_guard", "enemy_charge", "enemy_rupture", "enemy_fortify", "enemy_execute")),
        enemy("volt_vulture", EnemyTier.NORMAL, 65, 3, deck("enemy_multi", "enemy_jam", "enemy_leech", "enemy_charge", "enemy_multi", "enemy_heavy", "enemy_leech", "enemy_execute"), profile = "aggressive"),
        enemy("null_technician", EnemyTier.NORMAL, 73, 3, deck("enemy_disrupt", "enemy_fortify", "enemy_jam", "enemy_heavy", "enemy_disrupt", "enemy_surge", "enemy_rupture", "enemy_execute")),
        enemy("phase_stalker", EnemyTier.NORMAL, 68, 3, deck("enemy_jab", "enemy_disrupt", "enemy_heavy", "enemy_multi", "enemy_guard", "enemy_rupture", "enemy_charge", "enemy_execute"), profile = "aggressive"),
        enemy("breaker_drone", EnemyTier.ELITE, 74, 1, deck("enemy_heavy", "enemy_guard", "enemy_overload", "enemy_jab", "enemy_shock", "enemy_fortify", "enemy_heavy", "enemy_execute")),
        enemy("tesla_knight", EnemyTier.ELITE, 92, 1, deck("enemy_surge", "enemy_heavy", "enemy_jam", "enemy_multi", "enemy_fortify", "enemy_rupture", "enemy_charge", "enemy_execute")),
        enemy("audit_engine", EnemyTier.ELITE, 108, 2, deck("enemy_disrupt", "enemy_fortify", "enemy_armor_hit", "enemy_jam", "enemy_heavy", "enemy_surge", "enemy_rupture", "enemy_execute")),
        enemy("storm_colossus", EnemyTier.ELITE, 126, 2, deck("enemy_shock", "enemy_charge", "enemy_multi", "enemy_fortify", "enemy_jam", "enemy_heavy", "enemy_rupture", "enemy_execute"), profile = "aggressive"),
        enemy("blacksite_warden", EnemyTier.ELITE, 142, 3, deck("enemy_fortify", "enemy_disrupt", "enemy_armor_hit", "enemy_rupture", "enemy_surge", "enemy_heavy", "enemy_jam", "enemy_execute")),
        enemy("recursion_beast", EnemyTier.ELITE, 132, 3, deck("enemy_leech", "enemy_multi", "enemy_charge", "enemy_leech", "enemy_heavy", "enemy_surge", "enemy_rupture", "enemy_execute"), profile = "aggressive"),
        // 毎ターン、公開意図とプレイヤー手札を1枚交換する。
        enemy("synchronizer", EnemyTier.BOSS, 168, 1, deck("enemy_jab", "enemy_guard", "enemy_shock", "enemy_heavy", "enemy_disrupt", "enemy_surge", "enemy_rupture", "enemy_execute"), BossRule.SYNCHRONIZER),
        // 敵プール4枚ごとに全攻撃へ+1する。
        enemy("accumulator", EnemyTier.BOSS, 224, 2, deck("enemy_guard", "enemy_charge", "enemy_heavy", "enemy_fortify", "enemy_armor_hit", "enemy_rupture", "enemy_surge", "enemy_execute"), BossRule.ACCUMULATOR),
        // 基礎デッキではなくプレイヤーデッキのコピーを使う。
        enemy("mirror", EnemyTier.BOSS, 246, 3, deck("enemy_jab", "enemy_guard", "enemy_jab", "enemy_guard", "enemy_heavy", "enemy_shock", "enemy_rupture", "enemy_execute"), BossRule.MIRROR),
        // 2ターン目以降、プレイヤーの回復エナジーを1ずつ減らす最終ボス。
        enemy("zero", EnemyTier.BOSS, 288, 3, deck("enemy_jam", "enemy_heavy", "enemy_disrupt", "enemy_rupture", "enemy_fortify", "enemy_execute", "enemy_surge", "enemy_execute"), BossRule.ZERO, "aggressive"),
    )
}
