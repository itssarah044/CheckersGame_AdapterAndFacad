package Timing;

import EnumConstants.TimerType;

/**
 * Timing -> PerGameTimer  (Concrete Product)
 *
 * Each player owns a bank of 5 minutes for the whole game. The bank only
 * runs down while it is that player's turn.
 */
public class PerGameTimer implements GameTimer {

	public static final long TOTAL_MILLIS = 5 * 60 * 1000L;

	private final long[] bank = new long[3];	// index = player ID (1 or 2)
	private int activePlayer = 0;
	private long turnStartedAt;

	public PerGameTimer() {
		bank[1] = TOTAL_MILLIS;
		bank[2] = TOTAL_MILLIS;
	}

	@Override
	public synchronized void startTurn(int playerId) {
		activePlayer = playerId;
		turnStartedAt = now();
	}

	@Override
	public synchronized void endTurn(int playerId) {
		if (activePlayer == playerId) {
			bank[playerId] = Math.max(0, bank[playerId] - (now() - turnStartedAt));
			activePlayer = 0;
		}
	}

	@Override
	public synchronized long getRemainingMillis(int playerId) {
		long remaining = bank[playerId];
		if (activePlayer == playerId) {
			remaining -= now() - turnStartedAt;
		}
		return Math.max(0, remaining);
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
		return TimerType.PER_GAME.getLabel();
	}

	private static long now() {
		return System.nanoTime() / 1000000L;
	}
}
