package Timing;

/**
 * Timing -> TimerFactory  (Factory)
 *
 * Use getTimer method to get an object of type GameTimer.
 * This is the only class that creates the concrete timers.
 */
public class TimerFactory {

	public GameTimer getTimer(String timerType){
		if(timerType == null){
			return null;
		}

		if(timerType.equalsIgnoreCase("PER_GAME")){
			return new PerGameTimer();

		} else if(timerType.equalsIgnoreCase("PER_TURN")){
			return new PerTurnTimer();

		}
		return null;
	}
}