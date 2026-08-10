package jp.aoto.zerosum.core.content

import jp.aoto.zerosum.core.model.CardClass
import jp.aoto.zerosum.core.model.CardRarity
import jp.aoto.zerosum.core.model.CardTag
import jp.aoto.zerosum.core.model.Scaling
import jp.aoto.zerosum.core.model.Status

internal object EnemyCards {
    private val attack = setOf(CardTag.ATTACK, CardTag.ENEMY_ONLY)
    private val skill = setOf(CardTag.SKILL, CardTag.ENEMY_ONLY)

    internal val cards = listOf(
        // 通常敵の基準単発攻撃。
        card("enemy_jab", CardClass.ENEMY, CardRarity.SPECIAL, 0, listOf(damage(6)), listOf(damage(6)), attack),
        // 攻撃1回分を相殺する基準防御。
        card("enemy_guard", CardClass.ENEMY, CardRarity.SPECIAL, 0, listOf(block(6)), listOf(block(6)), skill),
        // 次の攻撃を25%増やす公開デバフ。
        card("enemy_overload", CardClass.ENEMY, CardRarity.SPECIAL, 0, listOf(status(Status.OVERLOAD, 1)), listOf(status(Status.OVERLOAD, 1)), skill),
        // 次ターン3ダメージ相当の遅延圧力。
        card("enemy_shock", CardClass.ENEMY, CardRarity.SPECIAL, 0, listOf(status(Status.SHOCK, 3)), listOf(status(Status.SHOCK, 3)), skill),
        // 事前公開される大型攻撃の基準12。
        card("enemy_heavy", CardClass.ENEMY, CardRarity.SPECIAL, 0, listOf(damage(12)), listOf(damage(12)), attack),
        // 4攻撃+3回復で持久戦を作る。
        card("enemy_drain", CardClass.ENEMY, CardRarity.SPECIAL, 0, listOf(damage(4), heal(3)), listOf(damage(4), heal(3)), attack),
        // エリート向けの12ブロック。
        card("enemy_fortify", CardClass.ENEMY, CardRarity.SPECIAL, 0, listOf(block(12)), listOf(block(12)), skill),
        // 出力に強く反応する2×3多段攻撃。
        card("enemy_multi", CardClass.ENEMY, CardRarity.SPECIAL, 0, listOf(damage(2, 3)), listOf(damage(2, 3)), attack),
        // プレイヤーの次効果を1回遮断する。
        card("enemy_disrupt", CardClass.ENEMY, CardRarity.SPECIAL, 0, listOf(status(Status.INTERRUPT, 1)), listOf(status(Status.INTERRUPT, 1)), skill),
        // 自己出力2で次の攻撃を予告強化。
        card("enemy_charge", CardClass.ENEMY, CardRarity.SPECIAL, 0, listOf(status(Status.OUTPUT, 2, true)), listOf(status(Status.OUTPUT, 2, true)), skill),
        // 瀕死を狙う18ダメージの明確な脅威。
        card("enemy_execute", CardClass.ENEMY, CardRarity.SPECIAL, 0, listOf(damage(18)), listOf(damage(18)), attack),
        // 3×2攻撃+2回復でブロックを削る。
        card("enemy_leech", CardClass.ENEMY, CardRarity.SPECIAL, 0, listOf(damage(3, 2), heal(2)), listOf(damage(3, 2), heal(2)), attack),
        // 感電2+過負荷1の複合デバフ。
        card("enemy_jam", CardClass.ENEMY, CardRarity.SPECIAL, 0, listOf(status(Status.SHOCK, 2), status(Status.OVERLOAD, 1)), listOf(status(Status.SHOCK, 2), status(Status.OVERLOAD, 1)), skill),
        // 現在ブロックを加算する防御反撃。
        card("enemy_armor_hit", CardClass.ENEMY, CardRarity.SPECIAL, 0, listOf(damage(2, scaling = Scaling.SELF_BLOCK)), listOf(damage(2, scaling = Scaling.SELF_BLOCK)), attack),
        // 自己出力1+8ブロックで次ターンを準備。
        card("enemy_surge", CardClass.ENEMY, CardRarity.SPECIAL, 0, listOf(status(Status.OUTPUT, 1, true), block(8)), listOf(status(Status.OUTPUT, 1, true), block(8)), skill),
        // 10攻撃+過負荷1のボス用圧力札。
        card("enemy_rupture", CardClass.ENEMY, CardRarity.SPECIAL, 0, listOf(damage(10), status(Status.OVERLOAD, 1)), listOf(damage(10), status(Status.OVERLOAD, 1)), attack),
    )
}
