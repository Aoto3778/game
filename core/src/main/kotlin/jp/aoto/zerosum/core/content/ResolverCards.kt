package jp.aoto.zerosum.core.content

import jp.aoto.zerosum.core.model.CardClass
import jp.aoto.zerosum.core.model.CardRarity
import jp.aoto.zerosum.core.model.CardTag
import jp.aoto.zerosum.core.model.EffectCondition
import jp.aoto.zerosum.core.model.Scaling
import jp.aoto.zerosum.core.model.Status

internal object ResolverCards {
    private val attack = setOf(CardTag.ATTACK)
    private val skill = setOf(CardTag.SKILL)
    private val exhaust = setOf(CardTag.SKILL, CardTag.EXHAUST)
    private val retain = setOf(CardTag.SKILL, CardTag.RETAIN)

    internal val cards = listOf(
        // 無料で1エナジー+2ドローのため消尽させる。
        card("overclock", CardClass.RESOLVER, CardRarity.UNCOMMON, 0, listOf(energy(1), draw(2)), listOf(energy(2), draw(2)), exhaust),
        // 捨て札1枚回収は1コスト、強化で無料化。
        card("reclaim", CardClass.RESOLVER, CardRarity.COMMON, 1, listOf(returnDiscard(1)), listOf(returnDiscard(1)), skill, upgradedCost = 0),
        // 直前札コピーは強力なので消尽し、強化で無料化。
        card("echo", CardClass.RESOLVER, CardRarity.RARE, 1, listOf(copyLast(1)), listOf(copyLast(1)), exhaust, upgradedCost = 0),
        // 2ドロー1捨ての無料交換、強化で3ドロー。
        card("scramble", CardClass.RESOLVER, CardRarity.UNCOMMON, 0, listOf(draw(2), discard(1)), listOf(draw(3), discard(1)), skill),
        // 捨て札2枚回収は2コスト消尽で循環を制限。
        card("recursion", CardClass.RESOLVER, CardRarity.RARE, 2, listOf(returnDiscard(2)), listOf(returnDiscard(3)), exhaust),
        // 1ドロー札を継承可能にして将来ターンへ送る。
        card("archive", CardClass.RESOLVER, CardRarity.COMMON, 1, listOf(draw(1)), listOf(draw(2)), retain),
        // 手札枚数を防御へ変え、保持判断を報酬化。
        card("align", CardClass.RESOLVER, CardRarity.COMMON, 1, listOf(block(1, Scaling.HAND_SIZE)), listOf(block(4, Scaling.HAND_SIZE)), skill),
        // 4枚見て1枚捨て、低寄与だった選別札を実質+3枚へ強化。
        card("reorder", CardClass.RESOLVER, CardRarity.COMMON, 1, listOf(draw(4), discard(1)), listOf(draw(5), discard(1)), skill),
        // 回収1を継承できる代わりに2コスト。
        card("memory_loop", CardClass.RESOLVER, CardRarity.UNCOMMON, 2, listOf(returnDiscard(1)), listOf(returnDiscard(2)), retain),
        // 手札枚数を火力化し、基礎2を加える。
        card("stack_trace", CardClass.RESOLVER, CardRarity.UNCOMMON, 1, listOf(damage(2, scaling = Scaling.HAND_SIZE)), listOf(damage(5, scaling = Scaling.HAND_SIZE)), attack),
        // 5火力+2ドロー-1捨てを2コストの攻撃交換札にする。
        card("handoff", CardClass.RESOLVER, CardRarity.UNCOMMON, 2, listOf(damage(5), draw(2), discard(1)), listOf(damage(8), draw(2), discard(1)), attack),
        // 非消尽コピーは1コスト、強化で無料化して再生札を使える余地を残す。
        card("memoize", CardClass.RESOLVER, CardRarity.RARE, 1, listOf(copyLast(1)), listOf(copyLast(1), draw(1)), skill, upgradedCost = 0),
        // 手札5枚以上なら9→14火力となる標準フィニッシャー。
        card("resolve", CardClass.RESOLVER, CardRarity.UNCOMMON, 1, listOf(damage(9, condition = EffectCondition.IF_HAND_AT_LEAST, threshold = 5)), listOf(damage(14, condition = EffectCondition.IF_HAND_AT_LEAST, threshold = 5)), attack),
        // 手札5枚以上で10→15ブロックを得る防御対。
        card("checksum", CardClass.RESOLVER, CardRarity.UNCOMMON, 1, listOf(block(10, condition = EffectCondition.IF_HAND_AT_LEAST, threshold = 5)), listOf(block(15, condition = EffectCondition.IF_HAND_AT_LEAST, threshold = 5)), skill),
        // 小回復と回収を消尽にし、持久無限化を防ぐ。
        card("rollback", CardClass.RESOLVER, CardRarity.RARE, 1, listOf(heal(3), returnDiscard(1)), listOf(heal(5), returnDiscard(1)), exhaust),
        // 直前札2枚コピー後に1→2エナジーを戻し、生成札を使えるようにする。
        card("fork", CardClass.RESOLVER, CardRarity.RARE, 2, listOf(copyLast(2), energy(1)), listOf(copyLast(2), energy(2)), exhaust),
        // 3ヒットそれぞれに手札枚数が乗る高相乗札。
        card("converge", CardClass.RESOLVER, CardRarity.RARE, 3, listOf(damage(1, 3, Scaling.HAND_SIZE)), listOf(damage(2, 3, Scaling.HAND_SIZE)), attack),
        // コピー+ドローを1回限りにして0コスト連鎖を許容。
        card("deep_copy", CardClass.RESOLVER, CardRarity.RARE, 0, listOf(copyLast(1), draw(1)), listOf(copyLast(1), draw(2)), exhaust),
        // 2枚捨てて2エナジーを得る等価交換。
        card("garbage_collect", CardClass.RESOLVER, CardRarity.UNCOMMON, 0, listOf(discard(2), energy(2)), listOf(discard(1), energy(2)), skill),
        // 無料2ドローは消尽、強化で3ドロー。
        card("cache_hit", CardClass.RESOLVER, CardRarity.UNCOMMON, 0, listOf(draw(2)), listOf(draw(3)), exhaust),
        // 低採用・低寄与を直すため1コストで4→7+手札枚数ブロック。
        card("fixed_point", CardClass.RESOLVER, CardRarity.RARE, 1, listOf(block(4, Scaling.HAND_SIZE)), listOf(block(7, Scaling.HAND_SIZE)), retain),
        // 遮断の高寄与を踏まえ、防御を6→9へ抑える。
        card("invariant", CardClass.RESOLVER, CardRarity.UNCOMMON, 2, listOf(block(6), status(Status.INTERRUPT, 1, true)), listOf(block(9), status(Status.INTERRUPT, 1, true)), skill),
        // 5火力後に捨て札1枚を戻し、攻撃列を延長。
        card("tail_call", CardClass.RESOLVER, CardRarity.COMMON, 1, listOf(damage(5), returnDiscard(1)), listOf(damage(8), returnDiscard(1)), attack),
        // 手札枚数を直接火力化する2コストの単発札。
        card("singularity", CardClass.RESOLVER, CardRarity.RARE, 2, listOf(damage(3, scaling = Scaling.HAND_SIZE)), listOf(damage(8, scaling = Scaling.HAND_SIZE)), attack),
        // 1エナジー生成と手札火力を3コストで同時解決。
        card("perfect_fit", CardClass.RESOLVER, CardRarity.RARE, 3, listOf(energy(1), damage(5, scaling = Scaling.HAND_SIZE)), listOf(energy(2), damage(7, scaling = Scaling.HAND_SIZE)), attack),
    )
}
