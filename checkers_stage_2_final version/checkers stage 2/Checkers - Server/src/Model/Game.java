package Model;
import EnumConstants.*;

/**
 * Server Application -> Game
 * @author  Siyar
 * 
 * Game Model
 */
public class Game {
	private Square[][] squares;
	
	//Constructor
	
	public Game(Square[][] squares) {
    this.squares = squares;
}
	

	public Square[][] getSquares(){
		return this.squares;
	}
	
	public int getTotlaSquares(){
		return squares.length;
	}
	
	public void printSquareDetails(){
		for(int r=0;r<Checkers.NUM_ROWS.getValue();r++){
			for(int c=0;c<Checkers.NUM_COLS.getValue();c++){
				System.out.println(squares[r][c].getSquareID() + "--" + squares[r][c].getSquareRow() + "--" + squares[r][c].getSquareCol()
						+ squares[r][c].getPlayerID());
			}
		}
	}

	public Square getSquare(int from) {
		for(Square[] sRows:squares){
			for(Square s: sRows){
				if(s.getSquareID()==from){
					return s;
				}
					
			}
		}		
		return null;
	}
	
	public void printAvailableSquareDetails(){
		
		int playerOne = 0;
		int playerTwo = 0;
		for(int r=0;r<Checkers.NUM_ROWS.getValue();r++){
			for(int c=0;c<Checkers.NUM_COLS.getValue();c++){
				if(squares[r][c].getPlayerID()==1)
					playerOne++;
				
				if(squares[r][c].getPlayerID()==2)
					playerTwo++;
			}
		}
		
		System.out.println("Player 1 has " + playerOne);
		System.out.println("Player 2 has " + playerTwo);
	}
	
	public boolean isOver(){
		
		int playerOne = 0;
		int playerTwo = 0;
		for(int r=0;r<Checkers.NUM_ROWS.getValue();r++){
			for(int c=0;c<Checkers.NUM_COLS.getValue();c++){
				if(squares[r][c].getPlayerID()==1)
					playerOne++;
				
				if(squares[r][c].getPlayerID()==2)
					playerTwo++;
			}
		}
		
		if(playerOne==0 || playerTwo==0){
			return true;
		}
		
		return false;
	}
}
