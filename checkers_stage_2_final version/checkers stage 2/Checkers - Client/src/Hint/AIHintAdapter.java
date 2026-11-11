package Hint;

import EnumConstants.Checkers;
import ExternalAI.AI;
import ExternalAI.Board;
import ExternalAI.Game;
import Model.Square;

/**
 * Hint -> AIHintAdapter  (Adapter)
 *
 * Lets the external Minimax AI (Adaptee, ExternalAI.AI) be used as a
 * HintProvider (Target). The Controller only calls getHint(); this class
 * converts our board to the AI's board, calls theAI.move(), and converts
 * the AI's answer back to {fromSquareID, toSquareID}.
 *
 * Differences this adapter hides from the Controller:
 *  - Our board is Square[][] (playerID + isKing); the AI's board is int[][]
 *    with 1 = dark, 2 = light, 3 = dark king, 4 = light king.
 *  - theAI.move() returns the board AFTER the move, not the move itself,
 *    so the move is found by comparing the board before and after.
 *  - The AI always plays the dark side (our Player 1). For Player 2 the
 *    board is turned 180 degrees and the colours are swapped.
 *  - The AI needs "new Game()" once, otherwise its square numbering is null.
 */
public class AIHintAdapter implements HintProvider {

	// values used by the external AI's board
	private static final int DARK = 1, LIGHT = 2, DARK_KING = 3, LIGHT_KING = 4;

	AI theAI;

	public AIHintAdapter(AI newAI) {
		theAI = newAI;
		new Game();		// fills the AI's square-numbering tables
	}

	@Override
	public int[] getHint(Square[][] squares, int playerID) {
		boolean flip = (playerID == Checkers.PLAYER_TWO.getValue());

		Board before = toAIBoard(squares, playerID, flip);
		Board after = new Board(before);

		try {
			theAI.move(after);			// adaptee -> specific request
		} catch (Exception e) {
			// In some end games the AI's search fails to pick a move;
			// then use the AI's own randomAI() instead.
			try {
				after = new Board(before);
				theAI.randomAI(after);
			} catch (Exception noMove) {
				return null;			// the player has no legal move
			}
		}

		return findMove(before.getBoard(), after.getBoard(), flip);
	}

	/** Our Square[][] -> the AI's Board, with the hint player as DARK. */
	private Board toAIBoard(Square[][] squares, int playerID, boolean flip) {
		int[][] grid = new int[8][8];
		int dark = 0, light = 0, darkKings = 0, lightKings = 0;

		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				Square s = squares[r][c];
				int id = s.getPlayerID();
				if (id == Checkers.EMPTY_SQUARE.getValue())
					continue;

				boolean mine = (id == playerID);
				int value = mine ? (s.isKing() ? DARK_KING : DARK) : (s.isKing() ? LIGHT_KING : LIGHT);

				if (mine) { dark++; if (s.isKing()) darkKings++; }
				else { light++; if (s.isKing()) lightKings++; }

				if (flip)
					grid[7 - r][7 - c] = value;
				else
					grid[r][c] = value;
			}
		}

		Board board = new Board();
		board.setBoard(grid);
		board.setCountDarkPieces(dark);
		board.setCountLightPieces(light);
		board.setCountDarkKings(darkKings);
		board.setCountLightKings(lightKings);
		board.setPlayerColor(DARK);
		return board;
	}

	/** Compares the AI's board before and after its move -> {from, to}. */
	private int[] findMove(int[][] before, int[][] after, boolean flip) {
		int srcRow = -1, srcCol = -1, destRow = -1, destCol = -1;

		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				boolean wasMine = before[r][c] == DARK || before[r][c] == DARK_KING;
				boolean isMine = after[r][c] == DARK || after[r][c] == DARK_KING;

				if (wasMine && after[r][c] == 0) { srcRow = r; srcCol = c; }
				if (before[r][c] == 0 && isMine) { destRow = r; destCol = c; }
			}
		}

		if (srcRow == -1 || destRow == -1)
			return null;

		// Our game plays one jump per move: for a multi-jump, give the first jump
		boolean step = Math.abs(destRow - srcRow) == 1;
		boolean singleJump = Math.abs(destRow - srcRow) == 2 && Math.abs(destCol - srcCol) == 2
				&& captured(before, after, srcRow, srcCol, destRow, destCol);
		if (!step && !singleJump) {
			int[] first = firstJump(before, after, srcRow, srcCol);
			if (first == null)
				return null;
			destRow = first[0];
			destCol = first[1];
		}

		return new int[] { toSquareID(srcRow, srcCol, flip), toSquareID(destRow, destCol, flip) };
	}

	/** True if the piece between (r1,c1) and (r2,c2) was taken by the move. */
	private boolean captured(int[][] before, int[][] after, int r1, int c1, int r2, int c2) {
		int mr = (r1 + r2) / 2, mc = (c1 + c2) / 2;
		boolean wasOpponent = before[mr][mc] == LIGHT || before[mr][mc] == LIGHT_KING;
		return wasOpponent && after[mr][mc] == 0;
	}

	/** First landing square of a multi-jump: a jump from the source over a captured piece. */
	private int[] firstJump(int[][] before, int[][] after, int srcRow, int srcCol) {
		for (int dr = -2; dr <= 2; dr += 4) {
			for (int dc = -2; dc <= 2; dc += 4) {
				int r = srcRow + dr, c = srcCol + dc;
				if (r < 0 || r > 7 || c < 0 || c > 7 || before[r][c] != 0)
					continue;
				if (captured(before, after, srcRow, srcCol, r, c))
					return new int[] { r, c };
			}
		}
		return null;
	}

	/** AI (row, col) -> our square ID 1..64 (undoing the flip for Player 2). */
	private int toSquareID(int row, int col, boolean flip) {
		if (flip) {
			row = 7 - row;
			col = 7 - col;
		}
		return row * 8 + col + 1;
	}
}
