package Handler;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import EnumConstants.Checkers;
import Model.Player;
import Model.Square;
import View.BoardPanel;
import View.TimerPanel;
import Timing.GameTimer;
import Timing.TimerManager;
import Hint.HintProvider;
import Hint.AIHintAdapter;
import ExternalAI.AI;

/**
 * Client Application -> Controller
 * @author Keerthikan
 * 
 * ClientApp
 */
public class Controller implements Runnable {
	private boolean continueToPlay;
	private boolean waitingForAction;
	private boolean isOver;
	private volatile boolean timedOut;
	
	//Network
	private DataInputStream fromServer;
	private DataOutputStream toServer;
	
	private BoardPanel boardPanel;
	private Player player;
	private GameTimer timer;
	private TimerPanel timerPanel;
	private HintProvider hintProvider;
	private Square hintedFrom;
	private Square hintedTo;
	
	//Data
	private LinkedList<Square> selectedSquares;
	private LinkedList<Square> playableSquares;
	//private LinkedList<Square> crossableSquares;
	
	public Controller(Player player, DataInputStream input, DataOutputStream output){
		this.player = player;
		this.fromServer = input;
		this.toServer= output;
		
		selectedSquares = new LinkedList<Square>();
		playableSquares = new LinkedList<Square>();
		
		//Adapter: the external AI is used through the HintProvider interface
		AI externalAI = new AI(3);
		hintProvider = new AIHintAdapter(externalAI);
	}
	
	public void setBoardPanel(BoardPanel panel){
		this.boardPanel = panel;
	}
	
	public void setTimerPanel(TimerPanel panel){
		this.timerPanel = panel;
	}
	
	//Hint button: ask the HintProvider (Target) for a move and show it
	public void requestHint(){
		if(!isHisTurn()){
			JOptionPane.showMessageDialog(null, "Not your turn",
					"Error", JOptionPane.ERROR_MESSAGE, null);
			return;
		}
		
		int[] hint = hintProvider.getHint(boardPanel.getSquares(), player.getPlayerID());
		if(hint == null){
			JOptionPane.showMessageDialog(null, "No hint available",
					"Hint", JOptionPane.INFORMATION_MESSAGE, null);
			return;
		}
		
		Square from = boardPanel.getSquare(hint[0]);
		Square to = boardPanel.getSquare(hint[1]);
		
		squareDeselected();
		addToSelected(from);		//select the suggested piece
		from.setHinted(true);		//colour the piece's square
		to.setHinted(true);			//colour the suggested destination
		hintedFrom = from;
		hintedTo = to;
		boardPanel.repaintPanels();
	}
	
	@Override
	public void run() {
		continueToPlay = true;
		waitingForAction = true;
		isOver=false;
		
		try {
			
			//The server tells us which timer was chosen; TimerManager gets it
			TimerManager timerManager = new TimerManager();
			timer = timerManager.getGameTimer(fromServer.readInt());
			timerPanel.start(timer, player.getPlayerID());
			
			//Player One
			if(player.getPlayerID()==Checkers.PLAYER_ONE.getValue()){
				//wait for the notification to start
				fromServer.readInt();
				player.setMyTurn(true);
			}
					
			while(continueToPlay && !isOver){
				if(player.getPlayerID()==Checkers.PLAYER_ONE.getValue()){
					waitForPlayerAction();
					if(!isOver)
						receiveInfoFromServer();
				}else if(player.getPlayerID()==Checkers.PLAYER_TWO.getValue()){
					receiveInfoFromServer();
					if(!isOver)
						waitForPlayerAction();
				}
			}
			
			if(isOver){
				timerPanel.stop();
				String message = timedOut ? "Time is up - you lost the game" : "Game is over";
				JOptionPane.showMessageDialog(null, message,
						"Information", JOptionPane.INFORMATION_MESSAGE, null);
				System.exit(0);
			}
			
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Connection lost",
					"Error", JOptionPane.ERROR_MESSAGE, null);
			System.exit(0);
		} catch (InterruptedException e) {
			JOptionPane.showMessageDialog(null, "Connection interrupted",
					"Error", JOptionPane.ERROR_MESSAGE, null);
		}			
	}
	
	private int opponentId(){
		return (player.getPlayerID() == Checkers.PLAYER_ONE.getValue())
				? Checkers.PLAYER_TWO.getValue() : Checkers.PLAYER_ONE.getValue();
	}
	
	private void receiveInfoFromServer() throws IOException {
		player.setMyTurn(false);
		timer.startTurn(opponentId());
		int from = fromServer.readInt();
		timer.endTurn(opponentId());
		if(from==Checkers.YOU_LOSE.getValue()){
			from = fromServer.readInt();
			int to = fromServer.readInt();
			updateReceivedInfo(from, to);
			isOver=true;
		}else if(from==Checkers.YOU_WIN.getValue()){
			isOver=true;
			continueToPlay=false;
		}else{
			int to = fromServer.readInt();
			updateReceivedInfo(from, to);
		}
	}	

	private void sendMove(Square from, Square to) throws IOException {
		toServer.writeInt(from.getSquareID());
		toServer.writeInt(to.getSquareID());
	}

	private void waitForPlayerAction() throws InterruptedException, IOException {
		int myId = player.getPlayerID();
		player.setMyTurn(true);
		timer.startTurn(myId);
		
		while(waitingForAction && !timedOut){
			if(timer.isExpired(myId)){
				timedOut = true;		//my own clock reached zero
			}else if(fromServer.available() > 0 && fromServer.readInt() == Checkers.YOU_LOSE.getValue()){
				timedOut = true;		//the server declared my time over
			}else{
				Thread.sleep(100);
			}
		}
		
		timer.endTurn(myId);
		
		if(timedOut){
			player.setMyTurn(false);
			isOver = true;
			return;
		}
		waitingForAction = true;		
	}
	
	public void move(Square from, Square to){
		to.setPlayerID(from.getPlayerID());
		from.setPlayerID(Checkers.EMPTY_SQUARE.getValue());
		checkCrossJump(from, to);
		checkKing(from, to);
		squareDeselected();
		
		waitingForAction = false;
		try {
			sendMove(from, to);
		} catch (IOException e) {
			System.out.println("Sending failed");
		}		
	}
	
	//When a square is selected
	public void squareSelected(Square s) {
		
		if(selectedSquares.isEmpty()){
			addToSelected(s);
		}		
		//if one is already selected, check if it is possible move
		else if(selectedSquares.size()>=1){
			if(playableSquares.contains(s)){
				move(selectedSquares.getFirst(),s);
			}else{
				squareDeselected();
				addToSelected(s);
			}
		}
	}
	
	private void addToSelected(Square s){
		s.setSelected(true);
		selectedSquares.add(s);
		getPlayableSquares(s);
	}

	public void squareDeselected() {
		
		for(Square square:selectedSquares)
			square.setSelected(false);
		
		selectedSquares.clear();
		
		for(Square square:playableSquares){
			square.setPossibleToMove(false);
		}
		
		playableSquares.clear();
		
		if(hintedFrom != null){
			hintedFrom.setHinted(false);
			hintedTo.setHinted(false);
			hintedFrom = null;
			hintedTo = null;
		}
		boardPanel.repaintPanels();
	}
	
	
	private void getPlayableSquares(Square s){
		playableSquares.clear();		
		playableSquares = boardPanel.getPlayableSquares(s);
		
		for(Square square:playableSquares){
			System.out.println(square.getSquareID());
		}		
		boardPanel.repaintPanels();
	}
	
	public boolean isHisTurn(){
		return player.isMyTurn() && !timedOut;
	}
	
	private void checkCrossJump(Square from, Square to){		
		if(Math.abs(from.getSquareRow()-to.getSquareRow())==2){		
			int middleRow = (from.getSquareRow() + to.getSquareRow())/2;
			int middleCol = (from.getSquareCol() + to.getSquareCol())/2;
			
			Square middleSquare = boardPanel.getSquare((middleRow*8)+middleCol+1);
			middleSquare.setPlayerID(Checkers.EMPTY_SQUARE.getValue());
			middleSquare.removeKing();
		}
	}
	
	private void checkKing(Square from, Square movedSquare){		
		if(from.isKing()){
			movedSquare.setKing();
			from.removeKing();
		}else if(movedSquare.getSquareRow()==7 && movedSquare.getPlayerID()==Checkers.PLAYER_ONE.getValue()){
			movedSquare.setKing();
		}else if(movedSquare.getSquareRow()==0 && movedSquare.getPlayerID()==Checkers.PLAYER_TWO.getValue()){
			movedSquare.setKing();
		}
	}
	
	private void updateReceivedInfo(int from, int to){
		Square fromSquare = boardPanel.getSquare(from);
		Square toSquare = boardPanel.getSquare(to);
		toSquare.setPlayerID(fromSquare.getPlayerID());
		fromSquare.setPlayerID(Checkers.EMPTY_SQUARE.getValue());
		checkCrossJump(fromSquare, toSquare);
		checkKing(fromSquare, toSquare);
		boardPanel.repaintPanels();
	}
}