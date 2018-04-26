import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import javax.annotation.processing.SupportedSourceVersion;
import java.util.*;
class BestMove{
    Board boardState;
    int index;
    BestMove(){
        boardState = new Board();
    }
}

public class TreeNode extends Thread{
    private Board state;

    //this is used for level deepening
    private int MAX_DEPTH = 3;

    private int AI;
    private int opponent;

    TreeNode(){

    }

    //constructor sets the board and the player number and opponent number
    TreeNode(Board board, int AI ,int opponent){
        state = new Board(board);
        this.AI = AI;
        this.opponent = opponent;
    }

    //minimax function that takes in the current board, whether its a max level or not, and the depth as input
    public int miniMax(Board currentBoard, boolean maxLevel, int depth){
        int score = checkWinner(currentBoard);
        Board boardCopy = new Board(currentBoard);

        //exits when the desired depth has been reached
        if(depth == 0){
            return score;
        }

        //ends if the AI has won
        if(score == 100){
            return score;
        }

        //ends if the opponent has won
        if (score == -100){
            return score;
        }

        //this is the AI's turns and is the max level
        int goAgainCount = 0;
        if(maxLevel){
            int bestMove = -1000;

            for(int i = state.getNumPits()-1; i >= 0;i--){
                //makes the move
                if(currentBoard.getSeedsInPit(AI, i) > 0){
                    //outputs a value of the player goes again
                    int goAgain = makeMoveOnBoard(currentBoard, AI,i);

                    //if it goes again we are at the same level
                    if(goAgain == 1) {
                        bestMove = Math.max(bestMove, miniMax(currentBoard, maxLevel,depth) + goAgain);
                    }
                    //otherwise continue to the next level
                    else{
                        bestMove = Math.max(bestMove, miniMax(currentBoard, !maxLevel,depth-1));
                    }
                    currentBoard = new Board(boardCopy);
                }
            }
        }
        //this is the opponents turns and is the min level
        else{
            int minMove = 1000;

            for(int i = state.getNumPits()-1; i >= 0;i--) {
                if(currentBoard.getSeedsInPit(opponent, i) > 0) {
                    //outputs a value of the player goes again
                    int goAgain = makeMoveOnBoard(currentBoard, opponent, i);

                    //if it goes again we are at the same level
                    if (goAgain == 1) {
                        minMove = Math.min(minMove, miniMax(currentBoard, maxLevel,depth) - goAgain);
                    }
                    //otherwise go the next level
                    else {
                        minMove = Math.min(minMove, miniMax(currentBoard, !maxLevel,depth-1));
                    }

                    //return back to the original state
                    currentBoard = new Board(boardCopy);
                }
            }
        }
        return score;
    }
    //this will get the best move to make
    public BestMove bestMove(Board state,int AI,int opponent){
        this.AI = AI;
        this.opponent = opponent;

        //this will return index of the best move
        BestMove move = new BestMove();

        //we will be doing a max node first
        int nodeValue = -1000;
        Board copyState = new Board(state);

        for(int i = state.getNumPits()-1; i > 0;i--){

            //if the pocket has seeds in it, make a move
            if(copyState.getSeedsInPit(AI,i) > 0) {
                //return 1, if you get another turn
                //return 0 if you dont go again
                int goAgain = makeMoveOnBoard(copyState,AI,i);

                int valueOfMove;
                //if the player goes again we remain at a max node
                if(goAgain == 1){
                    valueOfMove = miniMax(copyState,true,MAX_DEPTH) + goAgain;
                }
                //otherise we go to a min node
                else{
                    valueOfMove = miniMax(copyState,false,MAX_DEPTH) + goAgain;
                }

                //return back to the original board state
                copyState = new Board(state);

                //sets the current best move
                if(valueOfMove > nodeValue){
                    nodeValue = valueOfMove;
                    move.index = i;
                    move.boardState=copyState;

                }
            }
        }

        return move;
    }

    //makes the move,
    //returns 1 if the player will go again, return 0 otherwise
    public int makeMoveOnBoard(Board state, int currentlyPlaying, int move){

        //wipe the seeds from index specified by user (set num of seeds in pit to 0)
        int numSeeds = state.getSeedsInPit(currentlyPlaying,move);
        state.wipeSeeds(currentlyPlaying,move);

        //add one to every pit and store crossed, not including other player's store
        int index = move + 1;

        //will hold the ending pit by the end of the while loop
        int lastPit = 0;

        //holds the side of the board we are distributing on: 1 or 2
        int boardSide = currentlyPlaying;

        while (numSeeds > 0) {
            lastPit = index;
            //if player is at the end and at own store, add to store, switch to other side of the board and continue on
            if ((index == state.getNumPits()) && (boardSide == currentlyPlaying)) {

                //add to store
                state.addStore(currentlyPlaying);
                numSeeds--;

                //switch board sides
                boardSide = state.otherPlayerSide(boardSide);
                index = 0;

                //if player ends in their store, they do another turn
                if (numSeeds == 0) {
                    return 1;
                }
            }

            //if we're at the end and not at our own store then do not add to store, switch to other side and continue on
            else if (index == state.getNumPits()) {

                //switch board sides
                boardSide = state.otherPlayerSide(boardSide);
                index = 0;

            }
            //just add a seed and move on
            else {
                state.addSeed(boardSide,index);

                numSeeds--;
                index++;
            }
        }

        if (lastPit == state.getNumPits()) {
            return 0;
        }
        //if the user ends their turn in a pit on their own side in an empty pit and the pit directly
        //  across from it (on the other player's side) has seeds, all seeds in both pits
        //  go to the store of the user who just finished moving seeds
        int otherSide = state.otherPlayerSide(currentlyPlaying);
        if ((boardSide == currentlyPlaying) &&
                (state.getSeedsInPit(currentlyPlaying, lastPit) == 1) &&
                (state.getSeedsInPit(otherSide, state.getNumPits() - lastPit - 1) != 0)) {

            int pitSeeds1 = state.getSeedsInPit(currentlyPlaying, lastPit);
            int pitSeeds2 = state.getSeedsInPit(otherSide, state.getNumPits() - lastPit - 1);

            //wipe seeds from both pits
            state.wipeSeeds(currentlyPlaying, lastPit);
            state.wipeSeeds(otherSide, state.getNumPits() - lastPit - 1);

            //put seeds in p's store
            state.addStoreByValue(currentlyPlaying, pitSeeds1 + pitSeeds2);
        }
        return 0;
    }

    //given a board object it returns who won using an integer
    //100 if the AI won, -100 if the opponent won, 0 otherwise
    public int checkWinner(Board state){
        if(gameOver()) {
            //add up all of the remaining seeds on the stores
            for(int i = 0 ; i < state.getNumPits();i++){
                //adds seeds on AI side to the store
                int countSeeds = state.accessStore(AI);
                state.addStoreByValue(AI,countSeeds);

                //adds seeds on opponent side to their store
                countSeeds = state.accessStore(opponent);
                state.addStoreByValue(opponent,countSeeds);
            }
            //the AI wins
            if (state.accessStore(AI) > state.accessStore(opponent)) {
                return 100;
            }
            //the opponent wins
            else if (state.accessStore(AI) < state.accessStore(opponent)) {
                return -100;
            }
            //there is a tie in the game
            else {
                return 0;
            }
        }
        //setruns the difference in seeds as the score
        else{
            int score1 = 0;
            int score2 = 0;

            for(int i = 0; i < state.getNumPits();i++){
                score1 += state.getSeedsInPit(AI,i);
                score2 += state.getSeedsInPit(opponent,i);
            }

             int difference = (state.accessStore(AI)+score1) - (state.accessStore(opponent) + score2);
            return difference;
        }
    }

    /**
     * This checks if the game is over
     * Checks if there are any more moves available to make
     */
    public boolean gameOver(){
        int counterPlayer1 = 0;
        int counterPlayer2 = 0;

        for(int i = 0; i < state.getNumPits();i++){
            int player1PocketValue = state.getSeedsInPit(AI,i);
            int player2PocketValue = state.getSeedsInPit(opponent,i);

            if(player1PocketValue > 0) {
                counterPlayer1++;
            }
            if(player2PocketValue > 0) {
                counterPlayer2++;
            }
        }
        // if one of the players sides has no more seeds in it then the game has ended
        if(counterPlayer1 == 0 || counterPlayer2 == 0){
            return true;
        }

        return false;
    }

}
