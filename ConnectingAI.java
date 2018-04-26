import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import javax.annotation.processing.SupportedSourceVersion;
import java.util.*;
import java.io.*;
import java.net.*;

public class ConnectingAI {
  
  //client connection fields
  private String serverAddress;
  private PrintWriter out;
  private BufferedReader in;
  private Socket socket;
  
  //INFO fields
  int numPits;
  int playerNum;
  Board board;
  int time;
  private boolean normalConfig = true;
  
  //AI
  AlphaBetaMiniMax AI;
  
  ConnectingAI() throws IOException {
    this.serverAddress = "127.0.0.1";
    numPits = -1;
    AI = new AlphaBetaMiniMax();
  }
  
  public void sendAIMoves() throws IOException {
    //initial info exchange
    socket = new Socket(this.serverAddress, 56789);
    
    //recieve welcome from Mancala
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    String response = in.readLine();
    ArrayList<String> responseArr = splitOnSpaces(response);
    
    //Check that we got WELCOME
    if (responseArr.get(0).charAt(0) != 'W') {
      System.out.println("Was expecting WELCOME, got " + responseArr.get(0));
      socket.close();
      System.exit(0);
    }
    else {
      System.out.println("WELCOME");
    }
    
    //read in board configuration
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    response = in.readLine();
    responseArr = splitOnSpaces(response);
    
    if (responseArr.get(0).charAt(0) != 'I') {
      System.out.println("Was expecting INFO, got " + response);
      socket.close();
      System.exit(0);
    }
    
    //load in number of pits
    numPits = Integer.parseInt(responseArr.get(1));
    
    //load in playerNum
    if (responseArr.get(4).charAt(0) == 'F') {
      playerNum = 1;
    }
    else if (responseArr.get(4).charAt(0) == 'S') {
      playerNum = 2;
    }
    else {
      System.out.println("Was expecting player number, got " + responseArr.get(4));
      System.exit(0);
    }
    
    //load in number of seeds per pit
    int numSeeds = Integer.parseInt(responseArr.get(2));
    
    //load in the time
    this.time = Integer.parseInt(responseArr.get(3));
    
    //this gets the distribution of seeds according to what was passed in the info statement
    //it then goes on the put then in the distribution array
    ArrayList<Integer> distribution = new ArrayList<>();
      if(responseArr.get(5).charAt(0) == 'R'){
          for(int i = 6; i < responseArr.size();i++) {
            distribution.add(Integer.parseInt(responseArr.get(i)));
          }
          //tells the client that this is no longer a normal configuration but a random one
          normalConfig = false;
      }
      System.out.println("Distribution: " + distribution);

      //normal configuration is run if the condition is true
      if(normalConfig == true) {
        //send ready
        out = new PrintWriter(socket.getOutputStream(), true);
        out.println("READY");

        board = new Board(numSeeds, numPits);
      }
      //otherwise a random configuration is set in place
      else{

        out = new PrintWriter(socket.getOutputStream(),true);
        out.println("READY");

        board = new Board(distribution,numPits);
      }
      
    System.out.println("AI beginning board:");
    this.board.printBoardPlayer(playerNum);
    try {
      
      //recieve a move first
      if (playerNum == 2) {
        System.out.println("Getting opponent move from Mancala");
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        response = in.readLine();
        System.out.println("Opponent move: " + response);
        parseInput(response);
        
        //acknowledge we got the move
        out = new PrintWriter(socket.getOutputStream(), true);
        out.println("OK");
        
        //make the moves on the board
        ArrayList<Integer> opponentMoves = new ArrayList<Integer>(parseForMoves(response));
        for (int i = 0; i < opponentMoves.size(); i++) {
          this.board.makeMove(otherPlayer(playerNum), opponentMoves.get(i));
        }
        
        //this.guiBoard.setMyMove();
        System.out.println("AI board after opponent moves:");
        this.board.printBoardPlayer(playerNum);
      }
      
      int counter = 0;
      while (true) {
      
        //send a move
        //pause a second to let server prepare to recieve AI's move
        try {
            Thread.sleep(1000);
        } 
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Getting a move...");
        out = new PrintWriter(socket.getOutputStream(), true);
        
        String move = this.getAIMove(new Board(board));
        System.out.println("Sending move " + move);
        out.println(move);
        
        //make the AI's moves on the board
        ArrayList<Integer> AIMoves = new ArrayList<Integer>(parseForMoves(move));
        for (int i = 0; i < AIMoves.size(); i++) {
          this.board.makeMove(playerNum, AIMoves.get(i));
        }
   
        System.out.println("AI board after making AI moves:");
        this.board.printBoardPlayer(playerNum);
        
        //verify our move from Mancala
        System.out.println("Player " + playerNum + " getting verification from server...");
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        response = in.readLine();
        System.out.println("Player " + playerNum + " verification from server recieved");
        
        parseInput(response);
        
        System.out.println("Getting other player's move...");
        
        //get other player's move
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        response = in.readLine();
        System.out.println("Other player's move: " + response);
        
        parseInput(response);
        
        //acknowledge we got the other player's move
        out = new PrintWriter(socket.getOutputStream(), true);
        out.println("OK");
        
        //pie move
        if (response.startsWith("P") && counter == 0 && playerNum == 1) {
          this.board.pieMove();
          this.board.printBoardPlayer(playerNum);
          System.out.println("Pie move completed");
          
        }
        
        //make other player's moves on the board
        ArrayList<Integer> opponentMoves = new ArrayList<Integer>(parseForMoves(response));
        for (int i = 0; i < opponentMoves.size(); i++) {
          this.board.makeMove(otherPlayer(playerNum), opponentMoves.get(i));
        }

        System.out.println("AI board after making opponent's moves:");
        this.board.printBoardPlayer(playerNum);
        counter++;
      }
    }
    finally {
      socket.close();
      System.out.println("Game has been ended. Exiting");
      System.exit(0);
    }
  }
  
  public void parseInput(String response) throws IOException {
    
    if (response.startsWith("OK")) {
          System.out.println("Valid move");
    }
    
    else if (response.startsWith("ILLEGAL")) { //ILLEGAL
      //get other player's move
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      response = in.readLine();
      
      //make other player's moves on the board
      ArrayList<Integer> opponentMoves = new ArrayList<Integer>(parseForMoves(response));
      for (int i = 0; i < opponentMoves.size(); i++) {
        this.board.makeMove(otherPlayer(playerNum), opponentMoves.get(i));
      }
   
      this.board.printBoardPlayer(playerNum);
      if (response.startsWith("WINNER")) {
        System.out.println("Other player sent invalid move. You win! Thanks for playing!");
      }
      else {
        System.out.println("You sent an invalid move. You lose! Thanks for playing!");
      }

      socket.close();
      System.exit(0);
    }
    
    else if (response.startsWith("TIME")) { //TIME
      
      //get other player's move
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      response = in.readLine();
      
      //make other player's moves on the board
      ArrayList<Integer> opponentMoves = new ArrayList<Integer>(parseForMoves(response));
      for (int i = 0; i < opponentMoves.size(); i++) {
        this.board.makeMove(otherPlayer(playerNum), opponentMoves.get(i));
      }
  
      this.board.printBoardPlayer(playerNum);
      if (response.startsWith("WINNER")) {
        System.out.println("Other player went over time. You win! Thanks for playing!");
      }
      else {
        System.out.println("You went over time. You lose! Thanks for playing!");
      }
      
      socket.close();
      System.exit(0);
    }
    
    else if (response.startsWith("WINNER")) { //WINNER
      
      this.board.printBoardPlayer(playerNum);
      System.out.println("You win! Thanks for playing");
      
      //print final board
      this.board.winningBoard();
      this.board.printBoardPlayer(playerNum);
      
      socket.close();
      System.exit(0);
    }
    
    else if (response.startsWith("LOSER")) { //LOSER
      
      this.board.printBoardPlayer(playerNum);
      System.out.println("You lose! Thanks for playing");
      
      //print final board
      this.board.winningBoard();
      this.board.printBoardPlayer(playerNum);
      
      socket.close();
      System.exit(0);
    }
    
    else if (response.startsWith("TIE")) { //TIE

      this.board.printBoardPlayer(playerNum);
      System.out.println("Tie! Thanks for playing");
      
      //print final board
      this.board.winningBoard();
      this.board.printBoardPlayer(playerNum);
      
      socket.close();
      System.exit(0);
    }
    
  }
  
  //TODO: adjust so that AI returns multiple moves when it can go again?
  String getAIMove(Board board) {
    int move = AI.alphaBetaPruning(board, playerNum, otherPlayer(playerNum));
    String moveString = Integer.toString(move);
    return moveString;
  }
  
  public int otherPlayer(int p) {
    if (p == 1) {
      return 2;
    }
    else if (p == 2) {
      return 1;
    }
    else {
      return -1;
    }
  }
  
  public ArrayList<String> splitOnSpaces(String str) {
    ArrayList<String> strArray = new ArrayList<String>();
    String currWord = "";
    
    for (int i = 0; i < str.length(); i++) {
      if ((str.charAt(i) == ' ') && (currWord != "")) {
        strArray.add(currWord);
        currWord = "";
      }
      else if (str.charAt(i) != ' ') {
        currWord = currWord + str.charAt(i);
      }
    }
    if (currWord != "") {
      strArray.add(currWord);
    }
    
    return strArray;
  }
  
  ArrayList<Integer> parseForMoves(String movesSent) {
    ArrayList<Integer> movesList = new ArrayList<Integer>();
    for (int i = 0; i < movesSent.length(); i++) {
      if (Character.isDigit(movesSent.charAt(i))) {
        movesList.add(Character.getNumericValue(movesSent.charAt(i)));
      }
    }
    return movesList;
  }
  
  public static void main(String[] args) throws IOException {
    ConnectingAI ai = new ConnectingAI();
    ai.sendAIMoves();
  }
}