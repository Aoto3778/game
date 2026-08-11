package jp.aoto.zerosum.core.engine

import jp.aoto.zerosum.core.content.GameCatalog
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.GameEvent
import jp.aoto.zerosum.core.model.GameEventKind
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.MapNode
import jp.aoto.zerosum.core.model.MapNodeType
import jp.aoto.zerosum.core.model.RunStatus
import jp.aoto.zerosum.core.model.Screen
import jp.aoto.zerosum.core.rng.SplitMix64

/** Deterministic branching map generation and node progression. */
public object RunMap {
    private val normalByAct = mapOf(
        1 to listOf("training_drone", "stray_coil", "copper_wasp", "relay_rat", "static_hound", "fuse_mite", "arc_monk", "battery_thief"),
        2 to listOf("rusted_guard", "pulse_turret", "cable_serpent", "grid_leech", "echo_unit", "breaker_adept"),
        3 to listOf("charge_mimic", "volt_vulture", "null_technician", "phase_stalker"),
    )
    private val eliteByAct = mapOf(
        1 to listOf("breaker_drone", "tesla_knight"),
        2 to listOf("audit_engine", "storm_colossus"),
        3 to listOf("blacksite_warden", "recursion_beast"),
    )

    /** Replaces the map with a deterministic 12-13 node act. */
    public fun startAct(state: GameState): GameState {
        val generated = generate(state.seed, state.act)
        return state.copy(
            screen = Screen.MAP,
            mapNodes = generated,
            availableNodeIds = generated.filter { it.row == 0 }.map(MapNode::id),
            currentNodeId = null,
        )
    }

    /** Enters a reachable node and delegates its rule resolution to core. */
    public fun select(state: GameState, nodeId: String): GameState {
        if (state.screen != Screen.MAP || nodeId !in state.availableNodeIds) return state.invalidMap(nodeId)
        val node = state.mapNodes.firstOrNull { it.id == nodeId } ?: return state.invalidMap(nodeId)
        val selected = state.copy(currentNodeId = node.id, availableNodeIds = emptyList())
        return when (node.type) {
            MapNodeType.NORMAL, MapNodeType.ELITE, MapNodeType.BOSS ->
                reduce(selected, Action.BeginCombat(node.contentId.orEmpty()))
            MapNodeType.EVENT, MapNodeType.MERCHANT ->
                reduce(selected, Action.BeginEvent(node.contentId.orEmpty()))
            MapNodeType.REST -> reduce(selected, Action.Rest)
        }
    }

    /** Unlocks the selected node's successors after its content is resolved. */
    public fun completeSelectedNode(state: GameState): GameState {
        val selectedId = state.currentNodeId ?: return state
        val selected = state.mapNodes.firstOrNull { it.id == selectedId } ?: return state
        if (state.runStatus != RunStatus.ACTIVE || state.screen != Screen.MAP) return state
        if (selected.type == MapNodeType.BOSS) {
            val nextBoss = selected.connections.singleOrNull()?.let { id -> state.mapNodes.firstOrNull { it.id == id } }
            return if (nextBoss?.type == MapNodeType.BOSS) {
                state.copy(availableNodeIds = listOf(nextBoss.id), currentNodeId = null)
            } else if (state.act >= 3) {
                state.copy(runStatus = RunStatus.WON, screen = Screen.RESULT, currentNodeId = null)
            } else {
                startAct(state.copy(act = state.act + 1, nodeIndex = 0))
            }
        }
        return state.copy(availableNodeIds = selected.connections, currentNodeId = null)
    }

    /** Generates an act without platform APIs or mutable random state. */
    public fun generate(seed: Long, act: Int): List<MapNode> {
        require(act in 1..3)
        val normals = shuffled(normalByAct.getValue(act), seed xor (act * 101L))
        val elites = shuffled(eliteByAct.getValue(act), seed xor (act * 211L))
        val events = shuffled(
            GameCatalog.allEvents().filter { act in it.actMin..it.actMax }.map { it.id },
            seed xor (act * 307L),
        )
        val rows = mutableListOf(
            listOf(MapNodeType.NORMAL, MapNodeType.NORMAL),
            listOf(MapNodeType.EVENT, MapNodeType.NORMAL, MapNodeType.REST),
            listOf(MapNodeType.NORMAL, MapNodeType.ELITE, MapNodeType.MERCHANT),
            listOf(MapNodeType.REST, MapNodeType.EVENT, MapNodeType.ELITE),
            listOf(MapNodeType.BOSS),
        )
        if (act == 3) rows += listOf(MapNodeType.BOSS)
        var normalIndex = 0
        var eliteIndex = 0
        var eventIndex = 0
        val raw = rows.flatMapIndexed { row, types ->
            types.mapIndexed { lane, type ->
                val id = "a${act}_r${row}_l$lane"
                val content = when (type) {
                    MapNodeType.NORMAL -> normals[normalIndex++ % normals.size]
                    MapNodeType.ELITE -> elites[eliteIndex++ % elites.size]
                    MapNodeType.EVENT -> events[eventIndex++ % events.size]
                    MapNodeType.MERCHANT -> if (act == 1) "memory_vendor" else "silent_auction"
                    MapNodeType.REST -> null
                    MapNodeType.BOSS -> when {
                        act == 1 -> "synchronizer"
                        act == 2 -> "accumulator"
                        row == rows.lastIndex -> "zero"
                        else -> "mirror"
                    }
                }
                MapNode(id, type, content, row, lane)
            }
        }
        return raw.map { node ->
            val next = raw.filter { it.row == node.row + 1 }
            val connections = when {
                next.isEmpty() -> emptyList()
                next.size == 1 -> listOf(next.first().id)
                else -> next.filter { kotlin.math.abs(it.lane - node.lane) <= 1 }.map(MapNode::id)
            }
            node.copy(connections = connections)
        }
    }

    private fun <T> shuffled(values: List<T>, seed: Long): List<T> = SplitMix64.shuffle(seed, values).value

    private fun GameState.invalidMap(source: String): GameState = copy(
        events = events + GameEvent(GameEventKind.INVALID_ACTION, source),
    )
}
