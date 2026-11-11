package EnumConstants;

/**
 * Timer modes a match can be played with.
 * The numeric value is what travels over the socket
 * (Player 1 -> Server, then Server -> both players).
 */
public enum TimerType {

	PER_GAME(1, "Per game: 5:00 total for each player"),
	PER_TURN(2, "Per turn: 30 seconds for each move");

	private final int value;
	private final String label;

	private TimerType(int value, String label) {
		this.value = value;
		this.label = label;
	}

	public int getValue() {
		return value;
	}

	public String getLabel() {
		return label;
	}

	public static TimerType fromValue(int value) {
		for (TimerType type : values()) {
			if (type.value == value) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown timer type: " + value);
	}
}
