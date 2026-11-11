package Timing;

import EnumConstants.TimerType;

/**
 * Timing -> PerTurnTimer  (Concrete Product)
 *
 * Every turn starts with a fresh 30 seconds; time is not carried over.
 */
public class PerTurnTimer implements GameTimer {

	public static final long TURN_MILLIS = 30 * 1000L;

	private int activePlayer = 0;
	private long turnStartedAt;

	@Override
	public synchronized void startTurn(int playerId) {
		activePlayer = playerId;
		turnStartedAt = now();
	}

	@Override
	public synchronized void endTurn(int playerId) {
		if (activePlayer == playerId) {
			activePlayer = 0;
		}
	}

	@Override
	public synchronized long getRemainingMillis(int playerId) {
		if (activePlayer != playerId) {
			return TURN_MILLIS;		// not running: the next turn starts fresh
		}
		return Math.max(0, TURN_MILLIS - (now() - turnStartedAt));
	}

	@Override
	public synchronized int getActivePlayer() {
		return activePlayer;
	}

		@Override
	public boolean isExpired(int playerId) {
		return getRemainingMillis(playerId) <= 0;
	}
	
	@Override
	public String getDescription() {
		return TimerType.PER_TURN.getLabel();
	}

	private static long now() {
		return System.nanoTime() / 1000000L;
	}
}
