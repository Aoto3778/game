package jp.aoto.zerosum.core.content

import jp.aoto.zerosum.core.model.RelicDefinition
import jp.aoto.zerosum.core.model.RelicEffect
import jp.aoto.zerosum.core.model.RelicRarity

internal object Relics {
    private fun relic(
        id: String,
        rarity: RelicRarity,
        effect: RelicEffect,
        amount: Int,
    ) = RelicDefinition(id, "relic_${id}_name", "relic_${id}_description", rarity, effect, amount)

    internal val all = listOf(
        // 戦闘開始防御は通常4、希少版7で初手事故を緩和。
        relic("copper_spool", RelicRarity.COMMON, RelicEffect.START_BLOCK, 4),
        relic("reinforced_spool", RelicRarity.RARE, RelicEffect.START_BLOCK, 7),
        // 初手エナジーは1ターン限定なので1/2の二段階。
        relic("reserve_cell", RelicRarity.UNCOMMON, RelicEffect.START_ENERGY, 1),
        relic("fusion_cell", RelicRarity.BOSS, RelicEffect.START_ENERGY, 2),
        // ドロー増加は全ターンに効くため最大2。
        relic("wide_bus", RelicRarity.RARE, RelicEffect.DRAW_BONUS, 1),
        relic("broad_channel", RelicRarity.BOSS, RelicEffect.DRAW_BONUS, 2),
        // 全攻撃加算は多段と相乗するため1/2に抑制。
        relic("sharpened_contact", RelicRarity.UNCOMMON, RelicEffect.DAMAGE_BONUS, 1),
        relic("tungsten_tip", RelicRarity.RARE, RelicEffect.DAMAGE_BONUS, 2),
        // 戦闘後回復は乏しい回復手段を4/7だけ補う。
        relic("field_kit", RelicRarity.COMMON, RelicEffect.HEAL_AFTER_COMBAT, 4),
        relic("med_patch", RelicRarity.RARE, RelicEffect.HEAL_AFTER_COMBAT, 7),
        // 最大HPは即時回復も兼ねるため6/10。
        relic("ceramic_core", RelicRarity.UNCOMMON, RelicEffect.MAX_HP, 6),
        relic("expanding_bus", RelicRarity.BOSS, RelicEffect.MAX_HP, 10),
        // ドラフト後の自動押収は1/2枚で敵成長を抑える。
        relic("evidence_bag", RelicRarity.RARE, RelicEffect.SEIZE_AFTER_DRAFT, 1),
        relic("archive_seal", RelicRarity.BOSS, RelicEffect.SEIZE_AFTER_DRAFT, 2),
        // 金獲得補正は15%/30%相当として整数化。
        relic("old_meter", RelicRarity.COMMON, RelicEffect.GOLD_GAIN, 15),
        relic("merchant_token", RelicRarity.RARE, RelicEffect.GOLD_GAIN, 30),
        // 初ターン出力は1/2で速攻を補助。
        relic("pilot_light", RelicRarity.UNCOMMON, RelicEffect.FIRST_TURN_OUTPUT, 1),
        relic("blue_flame", RelicRarity.RARE, RelicEffect.FIRST_TURN_OUTPUT, 2),
        // 開始遮断は1回、ボス版のみ2回。
        relic("breaker_key", RelicRarity.UNCOMMON, RelicEffect.START_INTERRUPT, 1),
        relic("insulated_key", RelicRarity.BOSS, RelicEffect.START_INTERRUPT, 2),
        // 感電付与量加算は1/2で指数化を抑える。
        relic("storm_glass", RelicRarity.UNCOMMON, RelicEffect.SHOCK_BONUS, 1),
        relic("ion_prism", RelicRarity.RARE, RelicEffect.SHOCK_BONUS, 2),
        // 低HP時のターン防御は8/12。
        relic("emergency_rail", RelicRarity.COMMON, RelicEffect.LOW_HP_BLOCK, 8),
        relic("panic_plate", RelicRarity.RARE, RelicEffect.LOW_HP_BLOCK, 12),
        // 継承カード1枚ごとの防御を3/5にする。
        relic("memory_clip", RelicRarity.COMMON, RelicEffect.RETAIN_BLOCK, 3),
        relic("binder_clip", RelicRarity.RARE, RelicEffect.RETAIN_BLOCK, 5),
        // コスト軽減は毎ターン最初の1/2枚のみを想定。
        relic("tariff_card", RelicRarity.RARE, RelicEffect.COST_REDUCTION, 1),
        relic("efficiency_chip", RelicRarity.BOSS, RelicEffect.COST_REDUCTION, 2),
        // 敵プール採用数を1/2減らす固有防御。
        relic("pool_filter", RelicRarity.RARE, RelicEffect.POOL_TAKE_REDUCTION, 1),
        relic("denial_warrant", RelicRarity.BOSS, RelicEffect.POOL_TAKE_REDUCTION, 2),
        // 復活HPは12/24で一度だけ消費する想定。
        relic("backup_fuse", RelicRarity.RARE, RelicEffect.REVIVE, 12),
        relic("second_heart", RelicRarity.BOSS, RelicEffect.REVIVE, 24),
        // 戦闘報酬の自動強化は1/2枚。
        relic("upgrade_stamp", RelicRarity.UNCOMMON, RelicEffect.UPGRADE_REWARD, 1),
        relic("master_stamp", RelicRarity.RARE, RelicEffect.UPGRADE_REWARD, 2),
        // イベント後回復は3/6で代償を一部相殺。
        relic("event_pass", RelicRarity.COMMON, RelicEffect.EVENT_HEAL, 3),
        relic("lucky_badge", RelicRarity.RARE, RelicEffect.EVENT_HEAL, 6),
        // ボス限定ダメージ加算は3/5。
        relic("boss_lens", RelicRarity.UNCOMMON, RelicEffect.BOSS_DAMAGE, 3),
        relic("execution_scope", RelicRarity.RARE, RelicEffect.BOSS_DAMAGE, 5),
        // 毎ターン回復は1/2で長期戦だけを支える。
        relic("trickle_charger", RelicRarity.RARE, RelicEffect.TURN_HEAL, 1),
        relic("regenerative_loop", RelicRarity.BOSS, RelicEffect.TURN_HEAL, 2),
    )
}
