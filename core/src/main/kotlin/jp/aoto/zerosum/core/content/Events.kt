package jp.aoto.zerosum.core.content

import jp.aoto.zerosum.core.model.EventChoice
import jp.aoto.zerosum.core.model.EventDefinition
import jp.aoto.zerosum.core.model.EventOutcome
import jp.aoto.zerosum.core.model.EventOutcomeKind
import jp.aoto.zerosum.core.model.EventRequirement

internal object Events {
    private fun outcome(kind: EventOutcomeKind, amount: Int = 0, id: String? = null) = EventOutcome(kind, amount, id)
    private fun hp(amount: Int) = outcome(EventOutcomeKind.HP_DELTA, amount)
    private fun maxHp(amount: Int) = outcome(EventOutcomeKind.MAX_HP_DELTA, amount)
    private fun gold(amount: Int) = outcome(EventOutcomeKind.GOLD_DELTA, amount)
    private fun upgrade(amount: Int = 1) = outcome(EventOutcomeKind.UPGRADE_RANDOM_CARD, amount)
    private fun remove(amount: Int = 1) = outcome(EventOutcomeKind.REMOVE_RANDOM_CARD, amount)
    private fun addCard(id: String) = outcome(EventOutcomeKind.ADD_CARD, id = id)
    private fun burn(amount: Int) = outcome(EventOutcomeKind.BURN_POOL, amount)
    private fun relic(id: String) = outcome(EventOutcomeKind.ADD_RELIC, id = id)
    private fun nothing() = outcome(EventOutcomeKind.NOTHING)

    private fun choice(
        id: String,
        vararg outcomes: EventOutcome,
        requirement: EventRequirement = EventRequirement.NONE,
        requirementAmount: Int = 0,
    ) = EventChoice(id, "event_choice_${id}", outcomes.toList(), requirement, requirementAmount)

    private fun event(
        id: String,
        vararg choices: EventChoice,
        actMin: Int = 1,
        actMax: Int = 3,
    ) = EventDefinition(id, "event_${id}_title", "event_${id}_body", choices.toList(), actMin, actMax)

    internal val all = listOf(
        event("abandoned_substation",
            choice("substation_overload", hp(-8), addCard("surge"), requirement = EventRequirement.MIN_HP, requirementAmount = 9),
            choice("substation_salvage", gold(35), burn(1))),
        event("burned_archive",
            choice("archive_restore", gold(-30), upgrade(2), requirement = EventRequirement.MIN_GOLD, requirementAmount = 30),
            choice("archive_erase", remove(1), hp(-4), requirement = EventRequirement.MIN_HP, requirementAmount = 5)),
        event("silent_auction",
            choice("auction_bid", gold(-55), relic("wide_bus"), requirement = EventRequirement.MIN_GOLD, requirementAmount = 55),
            choice("auction_spoil", burn(2), hp(-7), requirement = EventRequirement.MIN_HP, requirementAmount = 8)),
        event("forked_cable",
            choice("cable_left", addCard("crosswire"), hp(-5), requirement = EventRequirement.MIN_HP, requirementAmount = 6),
            choice("cable_right", addCard("hold_current"), gold(-15), requirement = EventRequirement.MIN_GOLD, requirementAmount = 15)),
        event("inspection_gate",
            choice("gate_submit", remove(1), burn(2)),
            choice("gate_bribe", gold(-40), relic("old_meter"), requirement = EventRequirement.MIN_GOLD, requirementAmount = 40)),
        event("live_transformer",
            choice("transformer_touch", maxHp(5), hp(-10), requirement = EventRequirement.MIN_HP, requirementAmount = 11),
            choice("transformer_ground", addCard("insulate"), burn(1))),
        event("memory_vendor",
            choice("memory_buy", gold(-45), addCard("reclaim"), requirement = EventRequirement.MIN_GOLD, requirementAmount = 45),
            choice("memory_trade", remove(1), addCard("echo"))),
        event("pool_fire",
            choice("fire_feed", gold(-25), burn(4), requirement = EventRequirement.MIN_GOLD, requirementAmount = 25),
            choice("fire_warm", hp(8), outcome(EventOutcomeKind.ADD_CARD, id = "pressure"))),
        event("broken_medic",
            choice("medic_treatment", gold(-35), hp(14), requirement = EventRequirement.MIN_GOLD, requirementAmount = 35),
            choice("medic_parts", relic("field_kit"), hp(-6), requirement = EventRequirement.MIN_HP, requirementAmount = 7)),
        event("recursive_room",
            choice("room_repeat", addCard("recursion"), outcome(EventOutcomeKind.ADD_CARD, id = "scramble")),
            choice("room_exit", gold(28), hp(-3), requirement = EventRequirement.MIN_HP, requirementAmount = 4)),
        event("breaker_court",
            choice("court_confess", burn(3), gold(-20), requirement = EventRequirement.MIN_GOLD, requirementAmount = 20),
            choice("court_appeal", addCard("final_notice"), hp(-9), requirement = EventRequirement.MIN_HP, requirementAmount = 10)),
        event("storm_bottle",
            choice("storm_open", addCard("corona"), maxHp(-3)),
            choice("storm_sell", gold(50), addCard("enemy_shock"))),
        event("null_shrine",
            choice("shrine_kneel", relic("breaker_key"), maxHp(-4)),
            choice("shrine_defy", addCard("blackout"), hp(-8), requirement = EventRequirement.MIN_HP, requirementAmount = 9)),
        event("golden_relay",
            choice("relay_connect", gold(75), addCard("enemy_overload")),
            choice("relay_strip", gold(35), burn(1))),
        event("sealed_locker",
            choice("locker_force", hp(-6), relic("memory_clip"), requirement = EventRequirement.MIN_HP, requirementAmount = 7),
            choice("locker_leave", nothing())),
        event("voltage_oracle",
            choice("oracle_future", upgrade(1), maxHp(-2)),
            choice("oracle_refuse", hp(5), gold(-10), requirement = EventRequirement.MIN_GOLD, requirementAmount = 10)),
        event("debt_collector",
            choice("debt_pay", gold(-60), burn(3), requirement = EventRequirement.MIN_GOLD, requirementAmount = 60),
            choice("debt_fight", hp(-12), addCard("hard_trip"), requirement = EventRequirement.MIN_HP, requirementAmount = 13)),
        event("mirror_well",
            choice("well_copy", addCard("deep_copy"), hp(-7), requirement = EventRequirement.MIN_HP, requirementAmount = 8),
            choice("well_shatter", burn(2), maxHp(-2))),
        event("emergency_bunker",
            choice("bunker_rest", hp(11), gold(-25), requirement = EventRequirement.MIN_GOLD, requirementAmount = 25),
            choice("bunker_arm", addCard("shield_wall"), maxHp(-3))),
        event("data_ghost",
            choice("ghost_listen", addCard("invariant"), gold(-20), requirement = EventRequirement.MIN_GOLD, requirementAmount = 20),
            choice("ghost_delete", burn(1), upgrade(1))),
        event("rust_market",
            choice("market_relic", gold(-70), relic("sharpened_contact"), requirement = EventRequirement.MIN_GOLD, requirementAmount = 70),
            choice("market_card", gold(-25), addCard("relay"), requirement = EventRequirement.MIN_GOLD, requirementAmount = 25)),
        event("accumulator_vault",
            choice("vault_drain", burn(5), hp(-15), requirement = EventRequirement.MIN_HP, requirementAmount = 16),
            choice("vault_charge", gold(90), outcome(EventOutcomeKind.ADD_CARD, id = "enemy_charge")), actMin = 2),
        event("last_conductor",
            choice("conductor_learn", addCard("superconductor"), maxHp(-5)),
            choice("conductor_aid", hp(16), burn(1)), actMin = 2),
        event("zero_contract",
            choice("contract_sign", relic("tariff_card"), hp(-14), requirement = EventRequirement.MIN_HP, requirementAmount = 15),
            choice("contract_burn", burn(4), gold(-40), requirement = EventRequirement.MIN_GOLD, requirementAmount = 40), actMin = 3),
        event("final_switch",
            choice("switch_on", upgrade(3), hp(-18), requirement = EventRequirement.MIN_HP, requirementAmount = 19),
            choice("switch_off", remove(2), maxHp(-4)), actMin = 3),
    )
}
