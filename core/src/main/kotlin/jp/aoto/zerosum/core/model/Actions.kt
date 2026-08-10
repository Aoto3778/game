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

    /** Plays a card from the player's current hand. */
    public data class PlayCard(val instanceId: Long) : Action

    /** Ends the player turn and resolves the published enemy intent. */
    public data object EndTurn : Action

    /** Chooses one card from the current five-card draft. */
    public data class ChooseDraft(val instanceId: Long) : Action

    /** Opens a core-owned screen without changing the run. */
    public data class Navigate(val screen: Screen) : Action

    /** Abandons the current run intentionally. */
    public data object AbandonRun : Action
}
