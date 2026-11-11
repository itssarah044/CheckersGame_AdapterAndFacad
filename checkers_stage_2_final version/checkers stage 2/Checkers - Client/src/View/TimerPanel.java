package View;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import Timing.GameTimer;

/**
 * Client Application -> TimerPanel
 *
 * Shows the timer mode and the remaining time of both players. It only
 * displays what the GameTimer reports; it does not know which concrete
 * timer (per game / per turn) it is looking at.
 */
public class TimerPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final JLabel modeLabel = new JLabel("Waiting for the second player...", SwingConstants.CENTER);
	private final JLabel youLabel = new JLabel("You: --:--", SwingConstants.CENTER);
	private final JLabel opponentLabel = new JLabel("Opponent: --:--", SwingConstants.CENTER);
	private final Font normal = new Font("Arial", Font.PLAIN, 16);
	private final Font bold = new Font("Arial", Font.BOLD, 16);

	private GameTimer timer;
	private int myId;
	private Timer ticker;

	public TimerPanel() {
		setLayout(new GridLayout(2, 2));
		setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		modeLabel.setFont(normal);
		youLabel.setFont(normal);
		opponentLabel.setFont(normal);

		add(modeLabel);
		add(new JLabel());
		add(youLabel);
		add(opponentLabel);
	}

	/** Called by the Controller (game thread) once the timer exists. */
	public void start(GameTimer gameTimer, int playerId) {
		this.timer = gameTimer;
		this.myId = playerId;

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				modeLabel.setText(timer.getDescription());
				ticker = new Timer(200, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						refresh();
					}
				});
				ticker.start();
				refresh();
			}
		});
	}

	public void stop() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (ticker != null) {
					ticker.stop();
				}
			}
		});
	}

	private void refresh() {
		int opponentId = (myId == 1) ? 2 : 1;
		int active = timer.getActivePlayer();

		show(youLabel, "You", timer.getRemainingMillis(myId), active == myId);
		show(opponentLabel, "Opponent", timer.getRemainingMillis(opponentId), active == opponentId);
	}

	private void show(JLabel label, String who, long millis, boolean running) {
		long seconds = (millis + 999) / 1000;		// round up: 0:00 only when really over
		label.setText(String.format("%s: %d:%02d", who, seconds / 60, seconds % 60));
		label.setFont(running ? bold : normal);
		label.setForeground(running ? (seconds <= 10 ? Color.RED : Color.BLACK) : Color.GRAY);
	}
}
