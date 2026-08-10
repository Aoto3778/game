package jp.aoto.zerosum.core.content

import jp.aoto.zerosum.core.model.CardClass
import jp.aoto.zerosum.core.model.CardRarity
import jp.aoto.zerosum.core.model.CardTag
import jp.aoto.zerosum.core.model.EffectCondition
import jp.aoto.zerosum.core.model.Scaling
import jp.aoto.zerosum.core.model.Status

internal object CommonCards {
    private val attack = setOf(CardTag.ATTACK)
    private val skill = setOf(CardTag.SKILL)
    private val exhaust = setOf(CardTag.SKILL, CardTag.EXHAUST)

    internal val cards = listOf(
        // 6→9 は1エナジーの基準火力。
        card("strike", CardClass.COMMON, CardRarity.STARTER, 1, listOf(damage(6)), listOf(damage(9)), attack),
        // 5→8 は基準攻撃をほぼ相殺する防御量。
        card("guard", CardClass.COMMON, CardRarity.STARTER, 1, listOf(block(5)), listOf(block(8)), skill),
        // 初期デッキの循環を壊さない2枚ドロー。
        card("cycle", CardClass.COMMON, CardRarity.STARTER, 1, listOf(draw(2)), listOf(draw(3)), skill),
        // 希少回復は消尽と引き換えに5→8。
        card("triage", CardClass.COMMON, CardRarity.RARE, 1, listOf(heal(5)), listOf(heal(8)), exhaust),
        // 継承の柔軟性を3ブロック相当に評価。
        card("hold_current", CardClass.COMMON, CardRarity.COMMON, 1, listOf(block(3)), listOf(block(6)), setOf(CardTag.SKILL, CardTag.RETAIN)),
        // 0コストは手札枠を使うため3→5に抑制。
        card("quick_cut", CardClass.COMMON, CardRarity.COMMON, 0, listOf(damage(3)), listOf(damage(5)), attack),
        // 純防御専用なので基準ガードより2高い。
        card("brace", CardClass.COMMON, CardRarity.COMMON, 1, listOf(block(7)), listOf(block(10)), skill),
        // 無料ドローの連鎖を消尽で制限。
        card("scan", CardClass.COMMON, CardRarity.UNCOMMON, 0, listOf(draw(1)), listOf(draw(2)), exhaust),
        // エナジー純増を防ぐため1コストで防御を添える。
        card("battery", CardClass.COMMON, CardRarity.UNCOMMON, 1, listOf(energy(1), block(3)), listOf(energy(1), block(6)), skill),
        // 押収の価値が主役なので火力は4→7。
        card("purge", CardClass.COMMON, CardRarity.RARE, 1, listOf(damage(4), seize(1)), listOf(damage(7), seize(1)), setOf(CardTag.ATTACK, CardTag.SEIZE, CardTag.EXHAUST)),
        // 出力1と1ドローはテンポ投資として2コスト。
        card("calibrate", CardClass.COMMON, CardRarity.UNCOMMON, 2, listOf(status(Status.OUTPUT, 1, true), draw(1)), listOf(status(Status.OUTPUT, 2, true), draw(1)), skill),
        // 5火力に1ドローを付け、単純な上位互換を避ける。
        card("feint", CardClass.COMMON, CardRarity.UNCOMMON, 1, listOf(damage(5), draw(1)), listOf(damage(7), draw(1)), attack),
        // 遮断は次の効果だけなので基準防御も残す。
        card("insulate", CardClass.COMMON, CardRarity.UNCOMMON, 2, listOf(block(7), status(Status.INTERRUPT, 1, true)), listOf(block(11), status(Status.INTERRUPT, 1, true)), skill),
        // 捨て札回収はコンボ性が高いため防御を小さく設定。
        card("salvage", CardClass.COMMON, CardRarity.UNCOMMON, 1, listOf(returnDiscard(1), block(2)), listOf(returnDiscard(1), block(5)), skill),
        // 2エナジー生成は消尽で無限化を防ぐ。
        card("reserve", CardClass.COMMON, CardRarity.RARE, 0, listOf(energy(2)), listOf(energy(2), draw(1)), exhaust),
        // 敵プール肥大を直接火力へ変えるが2コスト。
        card("pressure", CardClass.COMMON, CardRarity.UNCOMMON, 2, listOf(damage(2, scaling = Scaling.ENEMY_POOL)), listOf(damage(5, scaling = Scaling.ENEMY_POOL)), attack),
        // 過負荷と防御の複合は即時火力を持たない。
        card("static_net", CardClass.COMMON, CardRarity.UNCOMMON, 1, listOf(status(Status.OVERLOAD, 1), block(4)), listOf(status(Status.OVERLOAD, 2), block(4)), skill),
        // 小回復とドローを消尽にせず2コストで提供。
        card("equalize", CardClass.COMMON, CardRarity.UNCOMMON, 2, listOf(heal(4), draw(1)), listOf(heal(6), draw(1)), skill),
        // 攻防合計6→10を2コストに収める。
        card("siphon", CardClass.COMMON, CardRarity.COMMON, 2, listOf(damage(4), heal(2)), listOf(damage(6), heal(4)), attack),
        // 手札交換は総枚数+1だけに制限。
        card("relay", CardClass.COMMON, CardRarity.COMMON, 1, listOf(draw(2), discard(1)), listOf(draw(3), discard(1)), skill),
        // 遮断2は強いので消尽かつ2コスト。
        card("nullify", CardClass.COMMON, CardRarity.RARE, 2, listOf(status(Status.INTERRUPT, 2, true)), listOf(status(Status.INTERRUPT, 2, true), block(6)), exhaust),
        // HP半分以下という条件で12→18ブロックを許容。
        card("last_stand", CardClass.COMMON, CardRarity.RARE, 1, listOf(block(12, condition = EffectCondition.IF_LOW_HP, threshold = 50)), listOf(block(18, condition = EffectCondition.IF_LOW_HP, threshold = 50)), skill),
        // 感電1を広く配るため素の火力は3→5。
        card("crosswire", CardClass.COMMON, CardRarity.COMMON, 1, listOf(damage(3), status(Status.SHOCK, 1)), listOf(damage(5), status(Status.SHOCK, 2)), attack),
        // 2コスト基準の8火力と過負荷1。
        card("blackout", CardClass.COMMON, CardRarity.UNCOMMON, 2, listOf(damage(8), status(Status.OVERLOAD, 1)), listOf(damage(12), status(Status.OVERLOAD, 1)), attack),
        // 手札枚数を防御へ変え、整合士以外でも保持判断を生む。
        card("shared_ground", CardClass.COMMON, CardRarity.RARE, 2, listOf(block(2, Scaling.HAND_SIZE)), listOf(block(5, Scaling.HAND_SIZE)), skill),
    )
}
