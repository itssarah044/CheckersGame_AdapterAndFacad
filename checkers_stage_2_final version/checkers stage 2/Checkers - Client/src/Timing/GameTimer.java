package Timing;

/**
 * Timing -> GameTimer  (Product)
 *
 * Common interface for every timer variant. Players are identified by the
 * same IDs used everywhere else (Checkers.PLAYER_ONE = 1, PLAYER_TWO = 2).
 */
public interface GameTimer {

	/** The given player's turn begins now. */
	void startTurn(int playerId);

	/** The given player's turn is over (a move was made, or the game ended). */
	void endTurn(int playerId);

	/** Milliseconds the player may still use (never negative). */
	long getRemainingMillis(int playerId);

	/** ID of the player whose clock is running, or 0 if nobody's is. */
	int getActivePlayer();

	/** Short text shown to the players. */
	String getDescription();

	/** True when the player has no time left. */
	boolean isExpired(int playerId);
}