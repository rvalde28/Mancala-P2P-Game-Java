import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.concurrent.Semaphore;

class User extends Player {
  
  public User() throws IOException, InterruptedException {
    super();
    name = "Player";
    playing = playerType.PLAYER1;
  }
  
  public User(Socket socket, int playerNum, int time, Board mainBoard, Semaphore messageSem, Semaphore moveSem, Semaphore otherMoveSem) throws IOException, InterruptedException {
    super(socket, playerNum, time, mainBoard, messageSem, moveSem, otherMoveSem);
    name = "Player";
    if(playerNum == 1) {
      playing = playerType.PLAYER1;
    }
    else if(playerNum == 2){
      playing = playerType.PLAYER2;
    }
    else{
      playing = playerType.NONE;
    }
    
    //user must input a move before we allow the getMove function to return
    moveSem.acquire();
  }
  
  public String getMove() throws IOException, InterruptedException {
    moveSem.acquire();
    return move;
  }
    

}