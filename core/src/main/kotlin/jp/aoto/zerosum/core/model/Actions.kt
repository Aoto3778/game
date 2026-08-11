package jp.aoto.zerosum.core.model

/** Every permitted input to the pure reducer. */
public sealed interface Action {
    /** Starts a deterministic run. */
    public data class StartRun(
        val heroClass: HeroClass,
        val seed: Long,
        val ascension: Int = 0,
    ) : Action

    /** Enters combat with a catalog enemy. */
    public data class BeginCombat(val enemyId: String) : Action

    /** Selects one currently reachable node from the act map. */
    public data class SelectMapNode(val nodeId: String) : Action

    /** Plays a card from the player's current hand. */
    public data class PlayCard(val instanceId: Long) : Action

    /** Ends the player turn and resolves the published enemy intent. */
    public data object EndTurn : Action

    /** Chooses one card from the current five-card draft. */
    public data class ChooseDraft(val instanceId: Long) : Action

    /** Opens a catalog event without resolving a branch. */
    public data class BeginEvent(val eventId: String) : Action

    /** Resolves one legal branch of the current event. */
    public data class ChooseEvent(val choiceId: String) : Action

    /** Recovers twenty percent maximum HP at a rest node. */
    public data object Rest : Action

    /** Advances to the next act or marks the third act complete. */
    public data object CompleteAct : Action

    /** Opens a core-owned screen without changing the run. */
    public data class Navigate(val screen: Screen) : Action

    /** Abandons the current run intentionally. */
    public data object AbandonRun : Action
}
