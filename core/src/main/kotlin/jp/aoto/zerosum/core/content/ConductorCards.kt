package jp.aoto.zerosum.core.content

import jp.aoto.zerosum.core.model.CardClass
import jp.aoto.zerosum.core.model.CardRarity
import jp.aoto.zerosum.core.model.CardTag
import jp.aoto.zerosum.core.model.EffectCondition
import jp.aoto.zerosum.core.model.Scaling
import jp.aoto.zerosum.core.model.Status

internal object ConductorCards {
    private val attack = setOf(CardTag.ATTACK)
    private val skill = setOf(CardTag.SKILL)
    private val exhaust = setOf(CardTag.SKILL, CardTag.EXHAUST)

    internal val cards = listOf(
        // 感電導入札なので火力を基準より2低くする。
        card("spark", CardClass.CONDUCTOR, CardRarity.COMMON, 1, listOf(damage(4), status(Status.SHOCK, 2)), listOf(damage(5), status(Status.SHOCK, 3)), attack),
        // 永続的な出力ではないが2スタックを1コスト投資とする。
        card("surge", CardClass.CONDUCTOR, CardRarity.UNCOMMON, 1, listOf(status(Status.OUTPUT, 2, true)), listOf(status(Status.OUTPUT, 3, true)), setOf(CardTag.POWER)),
        // 出力が各ヒットに乗るため素の火力は2×3。
        card("pulse", CardClass.CONDUCTOR, CardRarity.UNCOMMON, 2, listOf(damage(2, 3, Scaling.OPPONENT_SHOCK)), listOf(damage(2, 4, Scaling.OPPONENT_SHOCK)), attack),
        // 5火力+感電1を標準の複合攻撃とする。
        card("arc_lash", CardClass.CONDUCTOR, CardRarity.COMMON, 1, listOf(damage(5), status(Status.SHOCK, 1)), listOf(damage(7), status(Status.SHOCK, 2)), attack),
        // 多段による出力シナジーを2×3に抑える。
        card("chain_lightning", CardClass.CONDUCTOR, CardRarity.UNCOMMON, 2, listOf(damage(2, 3), status(Status.SHOCK, 1)), listOf(damage(3, 3), status(Status.SHOCK, 2)), attack),
        // 条件不成立でも4火力を保証し、感電時は合計12→17。
        card("voltage_spike", CardClass.CONDUCTOR, CardRarity.UNCOMMON, 1, listOf(damage(4), damage(8, condition = EffectCondition.IF_SHOCKED)), listOf(damage(5), damage(12, condition = EffectCondition.IF_SHOCKED)), attack),
        // ダメージ無しの感電4は次ターン5相当の遅延火力。
        card("corona", CardClass.CONDUCTOR, CardRarity.COMMON, 1, listOf(status(Status.SHOCK, 4)), listOf(status(Status.SHOCK, 6)), skill),
        // 感電量がそのまま防御に加わる転換札。
        card("induction", CardClass.CONDUCTOR, CardRarity.UNCOMMON, 1, listOf(block(3, Scaling.OPPONENT_SHOCK)), listOf(block(6, Scaling.OPPONENT_SHOCK)), skill),
        // 出力1に即時防御5を付けてテンポ損を緩和。
        card("live_wire", CardClass.CONDUCTOR, CardRarity.UNCOMMON, 2, listOf(status(Status.OUTPUT, 1, true), block(5)), listOf(status(Status.OUTPUT, 2, true), block(5)), setOf(CardTag.POWER)),
        // 1×5は出力1ごとに総火力が5増える設計。
        card("flashover", CardClass.CONDUCTOR, CardRarity.RARE, 2, listOf(damage(1, 5)), listOf(damage(1, 7)), attack),
        // 継承できる出力源は効果を1に抑制。
        card("storm_cell", CardClass.CONDUCTOR, CardRarity.RARE, 1, listOf(status(Status.OUTPUT, 1, true)), listOf(status(Status.OUTPUT, 2, true)), setOf(CardTag.POWER, CardTag.RETAIN)),
        // 感電3と過負荷1で次の攻撃を準備する。
        card("ionize", CardClass.CONDUCTOR, CardRarity.UNCOMMON, 1, listOf(status(Status.SHOCK, 3), status(Status.OVERLOAD, 1)), listOf(status(Status.SHOCK, 5), status(Status.OVERLOAD, 1)), skill),
        // 先に6ブロックを得て同量を火力へ変換する。
        card("feedback", CardClass.CONDUCTOR, CardRarity.RARE, 2, listOf(block(6), damage(0, scaling = Scaling.SELF_BLOCK)), listOf(block(9), damage(0, scaling = Scaling.SELF_BLOCK)), attack),
        // 感電を参照する2コスト決着札。
        card("discharge", CardClass.CONDUCTOR, CardRarity.UNCOMMON, 2, listOf(damage(5, scaling = Scaling.OPPONENT_SHOCK)), listOf(damage(9, scaling = Scaling.OPPONENT_SHOCK)), attack),
        // 過負荷2は後続への倍率が大きいため素火力7。
        card("overload_ray", CardClass.CONDUCTOR, CardRarity.RARE, 2, listOf(damage(7), status(Status.OVERLOAD, 2)), listOf(damage(11), status(Status.OVERLOAD, 2)), attack),
        // 出力2+感電2の長期価値を3コスト化。
        card("tesla_field", CardClass.CONDUCTOR, CardRarity.RARE, 3, listOf(status(Status.OUTPUT, 2, true), status(Status.SHOCK, 2)), listOf(status(Status.OUTPUT, 3, true), status(Status.SHOCK, 3)), setOf(CardTag.POWER)),
        // 無料の出力とエナジーは消尽で一度だけ。
        card("hot_start", CardClass.CONDUCTOR, CardRarity.UNCOMMON, 0, listOf(energy(1), status(Status.OUTPUT, 1, true)), listOf(energy(1), status(Status.OUTPUT, 2, true)), exhaust),
        // 2ドロー+1エナジーから1枚捨て、実質+1枚。
        card("phase_shift", CardClass.CONDUCTOR, CardRarity.UNCOMMON, 1, listOf(draw(2), energy(1), discard(1)), listOf(draw(3), energy(1), discard(1)), skill),
        // 攻防4+4を2回攻撃の出力シナジーと交換。
        card("current_divider", CardClass.CONDUCTOR, CardRarity.COMMON, 2, listOf(damage(4, 2), block(4)), listOf(damage(5, 2), block(6)), attack),
        // 多段の高寄与を抑え、1×4と感電1にする。
        card("cascade", CardClass.CONDUCTOR, CardRarity.UNCOMMON, 1, listOf(damage(1, 4), status(Status.SHOCK, 1)), listOf(damage(2, 4), status(Status.SHOCK, 1)), attack),
        // 3コストの外れ値を12火力+感電2へ抑える。
        card("thunderhead", CardClass.CONDUCTOR, CardRarity.RARE, 3, listOf(damage(12), status(Status.SHOCK, 2)), listOf(damage(17), status(Status.SHOCK, 3)), attack),
        // 出力4の代償として自分に過負荷2を課す。
        card("redline", CardClass.CONDUCTOR, CardRarity.RARE, 1, listOf(status(Status.OUTPUT, 4, true), status(Status.OVERLOAD, 2, true)), listOf(status(Status.OUTPUT, 5, true), status(Status.OVERLOAD, 1, true)), exhaust),
        // 出力3+エナジー1は3コスト消尽の構築到達点。
        card("superconductor", CardClass.CONDUCTOR, CardRarity.RARE, 3, listOf(status(Status.OUTPUT, 3, true), energy(1)), listOf(status(Status.OUTPUT, 4, true), energy(1)), exhaust),
        // 押収と感電を兼ねる代わりに消尽する。
        card("short_circuit", CardClass.CONDUCTOR, CardRarity.RARE, 1, listOf(damage(6), status(Status.SHOCK, 1), seize(1)), listOf(damage(9), status(Status.SHOCK, 2), seize(1)), setOf(CardTag.ATTACK, CardTag.SEIZE, CardTag.EXHAUST)),
        // 敵プールを火力化しつつ出力1を残す最高位札。
        card("grid_master", CardClass.CONDUCTOR, CardRarity.RARE, 3, listOf(damage(3, scaling = Scaling.ENEMY_POOL), status(Status.OUTPUT, 1, true)), listOf(damage(7, scaling = Scaling.ENEMY_POOL), status(Status.OUTPUT, 2, true)), attack),
    )
}
