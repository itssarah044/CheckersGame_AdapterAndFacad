
package Session;

import java.net.Socket;

/**
 * Runs a Checkers session on a separate thread.
 * Session operations are delegated to GameSessionFacade.
 */
public class HandleSession implements Runnable {

    private final GameSessionFacade sessionFacade;

    public HandleSession(Socket p1, Socket p2) {
        sessionFacade = new GameSessionFacade(p1, p2);
    }

    @Override
    public void run() {
        try {
            sessionFacade.startSession();

            while (sessionFacade.shouldContinue()) {
                if (!sessionFacade.playPlayerOneTurn()) {
                    break;
                }

                if (!sessionFacade.playPlayerTwoTurn()) {
                    break;
                }
            }

        } catch (Exception ex) {
            System.out.println("Connection is being closed");

        } finally {
            sessionFacade.closeConnections();
        }
    }
}
