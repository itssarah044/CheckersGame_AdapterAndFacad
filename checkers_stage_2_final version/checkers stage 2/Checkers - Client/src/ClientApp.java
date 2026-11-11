import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import EnumConstants.Checkers;
import EnumConstants.TimerType;
import Handler.Controller;
import Handler.MyMouseListener;
import Model.Player;
import View.BoardPanel;
import View.TimerPanel;
import java.awt.BorderLayout;

import java.io.*;
import java.net.*;
import java.util.Random;

/**
 * Client Application -> ClientApp
 * 
 * @author Keerthikan
 * 
 *         ClientApp
 */
public class ClientApp extends JFrame {

	private static final long serialVersionUID = 1L;

	private String address;
	private int port;

	// Model
	private Player player;

	// View
	private BoardPanel boardPanel;
	private TimerPanel timerPanel;

	// Network properties
	private Socket connection;
	private DataInputStream fromServer;
	private DataOutputStream toServer;

	// Constructor
	public ClientApp() {

		try {
			PropertyManager pm = PropertyManager.getInstance();
			address = pm.getAddress();
			port = pm.getPort();

			String name = (String) JOptionPane.showInputDialog(null, "Enter your name to Connect", "Connect to Server",
					JOptionPane.OK_CANCEL_OPTION);

			if (name != null && name.length() > 0) {
				player = new Player(name);
				connect();
			} else {
				JOptionPane.showMessageDialog(null, "Please enter valid name", "Error", JOptionPane.ERROR_MESSAGE,
						null);
				System.exit(0);
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Please enter valid IPv4-Address", "Error", JOptionPane.ERROR_MESSAGE,
					null);
			System.exit(0);
		}

	}

	private void connect() {

		try {
			connection = new Socket(address, port);

			// Should error occurs, handle it in a seperate thread (under try)
			fromServer = new DataInputStream(connection.getInputStream());
			toServer = new DataOutputStream(connection.getOutputStream());

			// First player get 1 and second player get 2
			player.setPlayerID(fromServer.readInt());

			// The first player decides which timer the match is played with
			if (player.getPlayerID() == Checkers.PLAYER_ONE.getValue()) {
				toServer.writeInt(askTimerType().getValue());
			}

			Controller task = new Controller(player, fromServer, toServer);
			setup(task);

			new Thread(task).start();
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "Internal Server Error - Check your IPv4-Address", "Error",
					JOptionPane.ERROR_MESSAGE, null);
			System.exit(0);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Couldn't connect to Server. Please try again", "Error",
					JOptionPane.ERROR_MESSAGE, null);
			System.exit(0);
		}
	}

	private TimerType askTimerType() {
		TimerType[] types = TimerType.values();
		String[] options = new String[types.length];
		for (int i = 0; i < types.length; i++) {
			options[i] = types[i].getLabel();
		}

		int choice;
		do {
			choice = JOptionPane.showOptionDialog(null, "Choose the timer for this match", "Timer mode",
					JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		} while (choice < 0);	// dialog closed without choosing -> ask again

		return types[choice];
	}

	private void setup(Controller c) {
		MyMouseListener listener = new MyMouseListener();
		listener.setController(c);

		boardPanel = new BoardPanel(listener);
		c.setBoardPanel(boardPanel);
		add(boardPanel, BorderLayout.CENTER);

		timerPanel = new TimerPanel();
		c.setTimerPanel(timerPanel);
		add(timerPanel, BorderLayout.NORTH);
		
		JButton hintButton = new JButton("Hint");
		hintButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				c.requestHint();
			}
		});
		add(hintButton, BorderLayout.SOUTH);
	}
}
