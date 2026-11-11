package Hint;

import Model.Square;

/**
 * Hint -> HintProvider  (Target)
 *
 * The interface the Controller (Client) expects from anything that gives
 * the player a hint.
 */
public interface HintProvider {

	/**
	 * Returns the suggested move as {fromSquareID, toSquareID}
	 * (the same square IDs 1..64 used by the rest of the game),
	 * or null if there is no move to suggest.
	 */
	public int[] getHint(Square[][] squares, int playerID);

}
