package Model;

public interface GameBuilder {

    void createBoard();

    void initializeSquares();

    void placePlayerPieces();

    Game build();
}
