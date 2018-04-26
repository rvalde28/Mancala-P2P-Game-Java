// import java.net.Socket;
// import java.util.*;
// import java.io.*;
// import java.util.concurrent.Semaphore;

// class AIuser extends Player {

  // public AIuser(Socket socket, int playerNum, Board mainBoard, Semaphore messageSem, Semaphore moveSem, Semaphore otherMoveSem) throws IOException, InterruptedException {
    // super(socket, playerNum, mainBoard, messageSem, moveSem, otherMoveSem);
    // name = "Player";
    // if(playerNum == 1) {
      // playing = playerType.PLAYER1;
    // }
    // else if(playerNum == 2){
      // playing = playerType.PLAYER2;
    // }
    // {
      // playing = playerType.NONE;
    // }
    // moveSem.acquire();
  // }
  
  // public String getMove() throws IOException, InterruptedException {
    // Random rand = new Random();
    // move = Integer.toString(rand.nextInt(boardCopy.getNumPits()));
    // make sure move is valid
    // while (boardCopy.getSeedsInPit(playing.ordinal(), Integer.parseInt(move)) == 0) {
      // move = Integer.toString(rand.nextInt(boardCopy.getNumPits()));
    // }
    // System.out.println("AI chose location " + move);
    // return move;
  // }

// }