
package Session;

import EnumConstants.Checkers;
import Model.Game;
import Model.GameBuilder;
import Model.Player;
import Model.Square;
import Model.StandardGameBuilder;
import Timing.GameTimer;
import Timing.TimerManager;
import java.net.Socket;

/**
 * Facade for the server-side Checkers session.
 * Provides a simple interface to the game-session subsystem.
 */
public class GameSessionFacade {

    private final Game checkers;
    private final Player player1;
    private final Player player2;
    private GameTimer timer;
    private boolean continueToPlay = true;

    public GameSessionFacade(Socket p1, Socket p2) {
        player1 = new Player(Checkers.PLAYER_ONE.getValue(), p1);
        player2 = new Player(Checkers.PLAYER_TWO.getValue(), p2);

        GameBuilder builder = new StandardGameBuilder();
        builder.createBoard();
        builder.initializeSquares();
        builder.placePlayerPieces();

        checkers = builder.build();
    }

    public void startSession() throws Exception {
        configureTimer();
        player1.sendData(1);
    }

    public boolean playPlayerOneTurn() throws Exception {
        return playTurn(player1, player2);
    }

    public boolean playPlayerTwoTurn() throws Exception {
        return playTurn(player2, player1);
    }

    public boolean shouldContinue() {
        return continueToPlay;
    }

    public void closeConnections() {
        if (player1.isOnline()) {
            player1.closeConnection();
        }

        if (player2.isOnline()) {
            player2.closeConnection();
        }
    }

    private void configureTimer() throws Exception {
        int choice = player1.receiveData();
        checkStatus(choice, choice);

        TimerManager timerManager = new TimerManager();
        timer = timerManager.getGameTimer(choice);

        int status1 = player1.sendData(choice);
        int status2 = player2.sendData(choice);

        checkStatus(status1, status2);
    }

    private boolean playTurn(
            Player current, Player opponent) throws Exception {

        int[] move = receiveMove(current);

        if (move == null) {
            current.sendData(Checkers.YOU_LOSE.getValue());
            opponent.sendData(Checkers.YOU_WIN.getValue());

            continueToPlay = false;
            return false;
        }

        int from = move[0];
        int to = move[1];

        checkStatus(from, to);
        updateGameModel(from, to);

        if (checkers.isOver()) {
            opponent.sendData(Checkers.YOU_LOSE.getValue());
        }

        int fromStatus = opponent.sendData(from);
        int toStatus = opponent.sendData(to);

        checkStatus(fromStatus, toStatus);

        if (checkers.isOver()) {
            current.sendData(Checkers.YOU_WIN.getValue());

            continueToPlay = false;
            return false;
        }

        return true;
    }

    private int[] receiveMove(Player player) {
        int id = player.getPlayerID();
        timer.startTurn(id);

        try {
            int from = receiveWithinTime(player);

            int to = (from == Checkers.TIME_UP.getValue())
                    ? from
                    : receiveWithinTime(player);

            if (from == Checkers.TIME_UP.getValue()
                    || to == Checkers.TIME_UP.getValue()) {
                return null;
            }

            return new int[] { from, to };

        } finally {
            timer.endTurn(id);
        }
    }

    private int receiveWithinTime(Player player) {
        long remaining =
                timer.getRemainingMillis(player.getPlayerID());

        if (remaining <= 0) {
            return Checkers.TIME_UP.getValue();
        }

        player.setTimeout(
                (int) Math.min(remaining, Integer.MAX_VALUE));

        return player.receiveData();
    }

    private void checkStatus(int status, int status2)
            throws Exception {

        if (status == 99 || status2 == 99) {
            throw new Exception("Connection is lost");
        }
    }

    private void updateGameModel(int from, int to) {
        Square fromSquare = checkers.getSquare(from);
        Square toSquare = checkers.getSquare(to);

        toSquare.setPlayerID(fromSquare.getPlayerID());
        fromSquare.setPlayerID(
                Checkers.EMPTY_SQUARE.getValue());

        checkCrossJump(fromSquare, toSquare);
    }

    private void checkCrossJump(Square from, Square to) {
        if (Math.abs(
                from.getSquareRow() - to.getSquareRow()) == 2) {

            int middleRow =
                    (from.getSquareRow() + to.getSquareRow()) / 2;

            int middleCol =
                    (from.getSquareCol() + to.getSquareCol()) / 2;

            Square middleSquare = checkers.getSquare(
                    (middleRow * 8) + middleCol + 1);

            middleSquare.setPlayerID(
                    Checkers.EMPTY_SQUARE.getValue());
        }
    }
}
