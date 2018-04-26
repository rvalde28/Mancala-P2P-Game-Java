import java.util.*;
import java.lang.Thread;

//move class to store the current best move
class Move{
    int index = -1;
    Board state;
    Queue<Integer> sucessors;
    int score;
    Move(){}

}

//alpha betaminimax class
public class AlphaBetaMiniMax {
    private int AI;
    private int opponent;
    private Board state;
    private Move currentBestMove = new Move();
    final int MAXLEVEL = 5;
    private int timerForMove;
    private Timing.Timer timer;
    private Thread time;

    AlphaBetaMiniMax(){}

    //constructor sets the number of seconds
    AlphaBetaMiniMax(int seconds){
        timerForMove = seconds ;
        timer = new Timing.Timer(timerForMove);
        time = new Thread(timer);
    }

    //function for alpha beta pruning
    public int alphaBetaPruning(Board board, int AI, int opponent){
        
        Board boardCopy = new Board(board);
        state = boardCopy;
        this.AI = AI;
        this.opponent = opponent;
        int bestMove = -1000;

        //this sets the max depth desided, by default it is at infinity
        int MAXDEPTH = (int)Double.NEGATIVE_INFINITY;

        //sets the alpha and beta values for the root of the tree
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;

        //returns the best move
        for(int i = board.getNumPits()-1; i > 0;i--) {
            if (board.getSeedsInPit(AI, i) > 0) {
                
                //boardCopy.printBoardPlayer(2);
                int goAgain = makeMoveOnBoard(boardCopy, AI, i);
                
                int valueOfMove;
                //this is for when the user goes again we remain in the max node
                if(goAgain == 1) {
                    valueOfMove = max(boardCopy, this.AI, beta, alpha, MAXDEPTH) + goAgain;
                }
                //otherwise we continue at a min node
                else{
                    valueOfMove = min(boardCopy, this.opponent, beta ,alpha ,MAXDEPTH) + goAgain;
                }
                boardCopy = new Board(board);
                //sets the value of the current max value and index
                if(valueOfMove > bestMove){
                    bestMove = valueOfMove;
                    currentBestMove.index = i;
                    currentBestMove.score = valueOfMove;
                }
            }
        }

        //returns the best move
        if(bestMove == currentBestMove.score){
            return currentBestMove.index;
        }
        return currentBestMove.index;
    }

    //this is a function that returns the max at a max a level
    public int max(Board currentBoard, int currentPlayer, double beta, double alpha, int level){
        Board boardCopy = new Board(currentBoard);

        int score = checkWinner(currentBoard);
        //if the time is up return the current best move
        // if(timer.timeIsUp()){
            // return currentBestMove.score;
        // }
        //return the score if we tie
        if(level == 0){
            return score;
        }

        //if the Ai or the opponent have won, then return the score
        if(score == 100 || score == -100){
            return score;
        }


        int bestMove = -1;
        for (int i = boardCopy.getNumPits()-1; i >= 0; i--) {
            //checks if the move has seeds in it
            if (boardCopy.getSeedsInPit(AI, i) > 0) {
                int goAgain = makeMoveOnBoard(boardCopy, currentPlayer, i);

                //if the user goes again we remain at a max level
                if (goAgain == 1) {
                    bestMove = Math.max(bestMove, max(boardCopy, AI,beta,alpha,level) + goAgain + score);

                }
                //continue at a min level
                else {
                    bestMove = Math.max(bestMove, min(boardCopy, opponent,beta,alpha,level -1)+score);
                }

                //updates the alpha values
                if(bestMove > alpha){
                    alpha = bestMove;
                }

                //beta pruning
                if(beta <= alpha){
                    break;
                }
                //resets the board to its original state
                boardCopy = new Board(currentBoard);

            }
        }
        //returns the alpha value
        return (int)alpha;
    }

    //gets the min at the current min level
    public int min(Board currentBoard, int currentPlayer, double beta, double alpha, int level){
        Board boardCopy = new Board(currentBoard);

        //gets the current score of the board
        int score = checkWinner(boardCopy);

        //if the time is up return the current best score
        // if(timer.timeIsUp()){
            // return currentBestMove.score;
        // }

        //return score if a tie
        if(level == 0){
            return score;
        }

        //return the score if a winner or loser is detected
        if(score == 100 || score == -100){
            return score;
        }

        int minMove = 1;
        for (int i = boardCopy.getNumPits()-1; i >= 0; i--) {
            //if the pit has seeds in it then it is available
            if(boardCopy.getSeedsInPit(currentPlayer,i) > 0){
                int goAgain = makeMoveOnBoard(boardCopy,currentPlayer,i);

                //if the user goes again then we are still at a min node
                if(goAgain == 1){
                    minMove = Math.min(minMove, min(boardCopy, opponent,beta, alpha, level) - goAgain + score);
                }
                //otherwise go to a max level
                else{
                    minMove = Math.min(minMove, max(boardCopy,AI,beta,alpha,level-1) + score);
                }

                //new beta is found, update beta
                if(minMove < beta){
                    beta = minMove;
                }
                //pruning
                if(beta <= alpha){
                    break;
                }

                //return board to original state
                boardCopy = new Board(currentBoard);
            }
        }

        return (int)beta;
    }

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
        //nothing happens
        else{
            int score1 = 0;
            int score2 = 0;

            for(int i = 0; i < state.getNumPits();i++){
                score1 += state.getSeedsInPit(AI, i);
                score2 += state.getSeedsInPit(opponent, i);
            }

            int difference = (state.accessStore(AI)+score1) - (state.accessStore(opponent) + score2);
            //System.out.println("DIFF: " +difference);
            return difference;
        }
    }


    public boolean gameOver(){
        int counterPlayer1 = 0;
        int counterPlayer2 = 0;

        for(int i = 0; i < state.getNumPits();i++){
            int player1PocketValue = state.getSeedsInPit(AI,i);
            int player2PocketValue = state.getSeedsInPit(opponent, i);

            if(player1PocketValue > 0) {
                counterPlayer1++;
            }
            if(player2PocketValue > 0) {
                counterPlayer2++;
            }
        }

        if(counterPlayer1 == 0 || counterPlayer2 == 0){
            return true;
        }

        return false;
    }

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



}
