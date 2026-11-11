package Model;

import EnumConstants.Checkers;

public class StandardGameBuilder implements GameBuilder {

    private Square[][] squares;

    @Override
    public void createBoard() {
        squares = new Square[8][8];
    }

    @Override
    public void initializeSquares() {
        int count = 0;

        for (int r = 0; r < Checkers.NUM_ROWS.getValue(); r++) {
            boolean rowInitialFilled = (r % 2 == 1);

            for (int c = 0; c < Checkers.NUM_COLS.getValue(); c++) {
                boolean isFilled =
                    (rowInitialFilled && c % 2 == 0) ||
                    (!rowInitialFilled && c % 2 == 1);

                count++;
                squares[r][c] = new Square(count, r, c, isFilled);
            }
        }
    }

    @Override
    public void placePlayerPieces() {

        // Player One
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < Checkers.NUM_COLS.getValue(); c++) {
                if (squares[r][c].getIsFilled()) {
                    squares[r][c].setPlayerID(
                        Checkers.PLAYER_ONE.getValue()
                    );
                }
            }
        }

        // Player Two
        for (int r = 5; r < 8; r++) {
            for (int c = 0; c < Checkers.NUM_COLS.getValue(); c++) {
                if (squares[r][c].getIsFilled()) {
                    squares[r][c].setPlayerID(
                        Checkers.PLAYER_TWO.getValue()
                    );
                }
            }
        }
    }

    @Override
    public Game build() {
        return new Game(squares);
    }
}