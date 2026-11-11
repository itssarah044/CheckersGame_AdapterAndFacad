package Timing;

import EnumConstants.TimerType;

/**
 * Timing -> TimerManager
 *
 * Connects HandleSession (server) and Controller (client) with the factory.
 * Both classes call this class, and this class calls the factory method
 * getTimer() of TimerFactory.
 */
public class TimerManager {

	private TimerFactory timerFactory;

	public TimerManager(){
		timerFactory = new TimerFactory();
	}

	public GameTimer getGameTimer(int choice){
		TimerType type = TimerType.fromValue(choice);
		GameTimer timer = timerFactory.getTimer(type.name());
		System.out.println("Timer created -> " + timer.getDescription());
		return timer;
	}
}