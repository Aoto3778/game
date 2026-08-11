package jp.aoto.zerosum.core.content

import jp.aoto.zerosum.core.model.CardClass
import jp.aoto.zerosum.core.model.CardRarity
import jp.aoto.zerosum.core.model.CardTag
import jp.aoto.zerosum.core.model.EffectCondition
import jp.aoto.zerosum.core.model.Scaling
import jp.aoto.zerosum.core.model.Status

internal object BreakerCards {
    private val attack = setOf(CardTag.ATTACK)
    private val skill = setOf(CardTag.SKILL)
    private val exhaust = setOf(CardTag.SKILL, CardTag.EXHAUST)
    private val seizure = setOf(CardTag.SKILL, CardTag.SEIZE, CardTag.EXHAUST)

    internal val cards = listOf(
        // 遮断1に7ブロックを添え、2効果の基準札とする。
        card("grounding", CardClass.BREAKER, CardRarity.UNCOMMON, 1, listOf(block(7), status(Status.INTERRUPT, 1, true)), listOf(block(10), status(Status.INTERRUPT, 1, true)), skill),
        // 押収が主価値なので3火力、強化後も6に留める。
        card("seizure", CardClass.BREAKER, CardRarity.RARE, 1, listOf(damage(3), seize(1)), listOf(damage(6), seize(1)), setOf(CardTag.ATTACK, CardTag.SEIZE, CardTag.EXHAUST)),
        // 最大HPはラン全体に効くため3→5かつ消尽。
        card("reinforce", CardClass.BREAKER, CardRarity.RARE, 1, listOf(maxHp(3)), listOf(maxHp(5)), exhaust),
        // 継承できる防御は7→10に抑える。
        card("firewall", CardClass.BREAKER, CardRarity.COMMON, 1, listOf(block(7)), listOf(block(10)), setOf(CardTag.SKILL, CardTag.RETAIN)),
        // 押収1に即時4ブロックを付けた防御型の基準。
        card("confiscate", CardClass.BREAKER, CardRarity.RARE, 1, listOf(seize(1), block(4)), listOf(seize(1), block(8)), seizure),
        // 相手を過負荷にしつつ次効果を遮断する準備札。
        card("lockout", CardClass.BREAKER, CardRarity.UNCOMMON, 1, listOf(status(Status.OVERLOAD, 1), status(Status.INTERRUPT, 1, true)), listOf(status(Status.OVERLOAD, 2), status(Status.INTERRUPT, 1, true)), skill),
        // 遮断の寄与が高いため火力を4→7へ抑える。
        card("circuit_breaker", CardClass.BREAKER, CardRarity.UNCOMMON, 2, listOf(damage(4), status(Status.INTERRUPT, 1, true)), listOf(damage(7), status(Status.INTERRUPT, 1, true)), attack),
        // 現在ブロックを火力へ転換し、先積みを報酬化。
        card("hard_trip", CardClass.BREAKER, CardRarity.RARE, 2, listOf(damage(0, scaling = Scaling.SELF_BLOCK)), listOf(damage(4, scaling = Scaling.SELF_BLOCK)), attack),
        // 8ブロック+押収は消尽で反復を防止。
        card("quarantine", CardClass.BREAKER, CardRarity.RARE, 2, listOf(block(8), seize(1)), listOf(block(13), seize(1)), seizure),
        // 低火力と過負荷で後続の価値を上げる。
        card("suppress", CardClass.BREAKER, CardRarity.COMMON, 1, listOf(damage(4), status(Status.OVERLOAD, 1)), listOf(damage(7), status(Status.OVERLOAD, 1)), attack),
        // 無料遮断は消尽にし、強化で4ブロックのみ追加。
        card("dead_zone", CardClass.BREAKER, CardRarity.UNCOMMON, 0, listOf(status(Status.INTERRUPT, 1, true)), listOf(status(Status.INTERRUPT, 1, true), block(4)), exhaust),
        // 攻防5+5を2コストの安定札に設定。
        card("countercharge", CardClass.BREAKER, CardRarity.COMMON, 2, listOf(block(5), damage(5)), listOf(block(8), damage(8)), attack),
        // 小防御と出力1で反撃軸への橋渡し。
        card("fuse", CardClass.BREAKER, CardRarity.UNCOMMON, 1, listOf(block(3), status(Status.OUTPUT, 1, true)), listOf(block(6), status(Status.OUTPUT, 1, true)), skill),
        // 12ブロックの代償にランダム1枚を捨てる。
        card("isolation", CardClass.BREAKER, CardRarity.COMMON, 1, listOf(block(12), discard(1)), listOf(block(16), discard(1)), skill),
        // 押収+ドローは0コスト消尽でテンポ札にする。
        card("audit", CardClass.BREAKER, CardRarity.RARE, 0, listOf(seize(1), draw(1)), listOf(seize(1), draw(2)), seizure),
        // 2枚押収は敵成長を大きく抑えるため2コスト消尽。
        card("impound", CardClass.BREAKER, CardRarity.RARE, 2, listOf(seize(2)), listOf(seize(2), block(8)), seizure),
        // 小回復2+防御8を2コストの持久札にする。
        card("safe_mode", CardClass.BREAKER, CardRarity.UNCOMMON, 2, listOf(block(8), heal(2)), listOf(block(12), heal(3)), skill),
        // 16ブロックの純防御は2コスト、強化で22。
        card("shield_wall", CardClass.BREAKER, CardRarity.UNCOMMON, 2, listOf(block(16)), listOf(block(22)), skill),
        // 8火力+押収を2コスト消尽の攻撃型にする。
        card("purge_order", CardClass.BREAKER, CardRarity.RARE, 2, listOf(damage(8), seize(1)), listOf(damage(13), seize(1)), setOf(CardTag.ATTACK, CardTag.SEIZE, CardTag.EXHAUST)),
        // 6ブロック+感電2で送電士との共通導線を残す。
        card("tripwire", CardClass.BREAKER, CardRarity.COMMON, 1, listOf(block(6), status(Status.SHOCK, 2)), listOf(block(9), status(Status.SHOCK, 3)), skill),
        // 敵プール5枚以上でのみ押収と高防御が起動。
        card("embargo", CardClass.BREAKER, CardRarity.UNCOMMON, 1, listOf(block(10, condition = EffectCondition.IF_ENEMY_POOL_AT_LEAST, threshold = 5), seize(1)), listOf(block(15, condition = EffectCondition.IF_ENEMY_POOL_AT_LEAST, threshold = 5), seize(1)), seizure),
        // 遮断2+6ブロックは2コストの希少防御。
        card("containment", CardClass.BREAKER, CardRarity.RARE, 2, listOf(status(Status.INTERRUPT, 2, true), block(6)), listOf(status(Status.INTERRUPT, 2, true), block(11)), skill),
        // 押収2と最大HP2を3コスト消尽の長期投資にする。
        card("eminent_domain", CardClass.BREAKER, CardRarity.RARE, 3, listOf(seize(2), maxHp(2)), listOf(seize(3), maxHp(2)), seizure),
        // 敵プール10枚以上の高寄与を10→15火力へ抑える。
        card("final_notice", CardClass.BREAKER, CardRarity.RARE, 1, listOf(damage(10, condition = EffectCondition.IF_ENEMY_POOL_AT_LEAST, threshold = 10)), listOf(damage(15, condition = EffectCondition.IF_ENEMY_POOL_AT_LEAST, threshold = 10)), attack),
        // 20ブロック+遮断1を3コストの防御到達点とする。
        card("iron_law", CardClass.BREAKER, CardRarity.RARE, 3, listOf(block(20), status(Status.INTERRUPT, 1, true)), listOf(block(28), status(Status.INTERRUPT, 1, true)), skill),
    )
}
