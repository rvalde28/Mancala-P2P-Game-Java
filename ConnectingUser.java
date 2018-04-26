import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class ConnectingUser {
  //adress information
  private String serverAddress;
  //readers and writers for the server.
  private PrintWriter out;
  private BufferedReader in;
  private Socket socket;
  
  //board info
  private Board board;
  private int playerNum;
  private int numSeeds;
  private int numPits;
  private int time;
  private boolean normalConfig = true;
  GUI guiBoard;
  Timing.Timer timerObj = new Timing.Timer((int)Double.POSITIVE_INFINITY);


  //default constructor for client
  public ConnectingUser() throws IOException {
    serverAddress = "127.0.0.1";
    numPits = 6;
    playerNum = -1;
    time = 0;
    
    //initialize pits and stores to 0; 6 is arbitrary
  }

  //read an integer in from the user
  public String getUserMove() {
      String moves = "";
      
      //if there's already a winner, or time is up, just return
      if (this.board.checkWinner() || timerObj.timeIsUp()) {
        return moves;
      }
    
      Board boardCopy = new Board(this.board);
      
      while(!guiBoard.moveMade && !timerObj.timeIsUp()){
        
        //pause for a moment; linux server is slow and can't handle busy waiting
        try {
          Thread.sleep(1000);
        } 
        catch(InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
        if (board.checkWinner()) {
          break;
        }
      }
      
      if (timerObj.timeIsUp()) {
        return moves;
      }
      
      int index = guiBoard.returnMove();
      
      int valid = -1;
      while ((valid != 0) && (board.checkWinner() == false) && !timerObj.timeIsUp()) {
        boardCopy = new Board(this.board);
        valid = boardCopy.makeMove(this.playerNum, index);
        
        if (valid != -1) {
          //make move on this user's board
          this.board = boardCopy;
          //add to list of moves we will return from this function
          moves = moves + Integer.toString(index) + " ";
          //print new board
          this.board.printBoardPlayer(playerNum);
        }
        else if (valid == -1) {
          System.out.println("Invalid move! Try again:");
          guiBoard.setMyMove();
          while(!guiBoard.moveMade && !timerObj.timeIsUp()){
            //pause for a moment; linux server is slow and can't handle busy waiting
            try {
                Thread.sleep(1000);
            } 
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            if (board.checkWinner()) {
              break;
            }
          }

          index = guiBoard.returnMove();
          
        }
        if ((valid == 1) && (!board.checkWinner())) {
          System.out.println("Go again:");
          guiBoard.setMyMove();
          this.board.printBoardPlayer(playerNum);
          //get the next move
          while(!guiBoard.moveMade && !timerObj.timeIsUp()){
            //pause for a moment; linux server is slow and can't handle busy waiting
            try {
                Thread.sleep(1000);
            } 
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            if (board.checkWinner()) {
              break;
            }
            
          }

          index = guiBoard.returnMove();
        }
        
        if (board.checkWinner()) {
          break;
        }
      }
      
      //if there is already a winner, just return some random move
      if (moves == "") {
        moves = "1 ";
      }
     
      return moves;
      
  }
  
private void sendUserMoves() throws IOException {
    
    //initial info exchange
    socket = new Socket(this.serverAddress, 56789);
    
    //recieve welcome from Mancala
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    String response = in.readLine();
    ArrayList<String> responseArr = splitOnSpaces(response);
    
    //Check that we recieved a welcome
    if (responseArr.get(0).charAt(0) != 'W') {
      System.out.println("Was expecting WELCOME, got " + responseArr.get(0));
      socket.close();
      System.exit(0);
    }
    else {
      System.out.println("WELOME");
    }
    
    //read in board configuration
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    response = in.readLine();
    responseArr = splitOnSpaces(response);
    
    //check that this is an INFO command
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

      //normal configuration is run if the condition is true
      if(normalConfig == true) {
        guiBoard = new GUI(playerNum, numPits, numSeeds);

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
        guiBoard = new GUI(board, playerNum,numPits);
      }
    
    Thread timeThread;
    
    try {
      
      //recieve a move first
      if (playerNum == 2) {
        System.out.println("Getting opponent move from Mancala...");
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        response = in.readLine();
        System.out.println("Opponent's move: " + response);
        
        //start the timer
        timerObj = new Timing.Timer(time);
        timeThread = new Thread(timerObj);
        timeThread.start();
        
        parseInput(response);
        
        //acknowledge we got the other player's move
        out = new PrintWriter(socket.getOutputStream(), true);
        out.println("OK");
        
        //make the moves on the board
        ArrayList<Integer> opponentMoves = new ArrayList<Integer>(parseForMoves(response));
        for (int i = 0; i < opponentMoves.size(); i++) {
          this.board.makeMove(otherPlayer(this.getPlayerNum()), opponentMoves.get(i));
          this.guiBoard.opponentMove(opponentMoves.get(i));
        }
        
        this.guiBoard.setMyMove();
        this.board.printBoardPlayer(playerNum);
      }
      
      int counter = 0;
      while (true) {
      
        //send a move
        //pause a moment to let server prepare to recieve move
        try {
            Thread.sleep(1000);
        } 
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Sending a move...");
        
        //In the first iteration, if this is player 1 then the timer has not been started yet
        if ((counter == 0) && (playerNum == 1)) {
          //start the timer
          timerObj = new Timing.Timer(time);
          timeThread = new Thread(timerObj);
          timeThread.start();
        }
        
        //pie rule
        String move = "-1";
        if ((counter == 0) && (playerNum == 2)) {
          int choice = JOptionPane.showConfirmDialog(null,"Would you like to make a pie move?", "Pie move",JOptionPane.YES_NO_OPTION);
          if (choice == JOptionPane.YES_OPTION) {
            //make the pie move
            move = "P";
            this.board.pieMove();
            this.guiBoard.board = new Board(board);
            this.guiBoard.updateGame();
            System.out.println("Board after pie move:");
            this.board.printBoardPlayer(playerNum);
          }
          else {
            //don't make the pie move
            move = this.getUserMove();
          }
        }
        else {
          //don't make the pie move
          move = this.getUserMove();
        }
        
        //stop the timer
        boolean overTime = timerObj.timeIsUp();
        timerObj.signalStopTimer();
        
        //send any move if we are over time
        if (overTime == true) {
          //ensure server timer runs out as well
          try {
            Thread.sleep(5000);
          } 
          catch(InterruptedException ex) {
              Thread.currentThread().interrupt();
          }
          move = "0"; 
        }
        
        out = new PrintWriter(socket.getOutputStream(), true);
        System.out.println("Sending move " + move + " to server");
        out.println(move);
        
        //verify our move from Mancala
        System.out.println("Player " + playerNum + " getting verification from server...");
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        response = in.readLine();
        System.out.println("Player " + playerNum + " verification from server recieved.");
        
        parseInput(response);
        
        System.out.println("Getting other player's move...");
        
        //get other player's move
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        response = in.readLine();
        
        //start the timer
        timerObj = new Timing.Timer(time);
        timeThread = new Thread(timerObj);
        timeThread.start();
        
        parseInput(response);
        
        //send acknowledgement
        out = new PrintWriter(socket.getOutputStream(), true);
        out.println("OK");
        
        //verify the moves
        //pie move
        if (response.startsWith("P") && (counter == 0) && (playerNum == 1)) {
          System.out.println("Making pie move...");
          this.board.pieMove();
          this.guiBoard.board = new Board(board);
          this.guiBoard.updateGame();
          this.board.printBoardPlayer(playerNum);
          System.out.println("Pie move complete");
        }
        System.out.println("Player " + playerNum + " making server's moves...");
        //make other player's moves on the board
        ArrayList<Integer> opponentMoves = new ArrayList<Integer>(parseForMoves(response));
        for (int i = 0; i < opponentMoves.size(); i++) {
          this.board.makeMove(otherPlayer(this.getPlayerNum()), opponentMoves.get(i));
          this.guiBoard.opponentMove(opponentMoves.get(i));
        }
        System.out.println("Server's moves complete");
        this.guiBoard.setMyMove();
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
      
      //get other player's move or winner/loser message
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      response = in.readLine();
      
      //make other player's moves on the board
      ArrayList<Integer> opponentMoves = new ArrayList<Integer>(parseForMoves(response));
      for (int i = 0; i < opponentMoves.size(); i++) {
        this.board.makeMove(otherPlayer(this.getPlayerNum()), opponentMoves.get(i));
        this.guiBoard.opponentMove(opponentMoves.get(i));

      }
      this.guiBoard.setMyMove();

      if (response.startsWith("WINNER")) {
        System.out.println("Other player sent invalid move. You win! Thanks for playing!");
      }
      else {
        System.out.println("You sent an invalid move. You lose! Thanks for playing!");
      }
      
      //Print the final board
      this.board.printBoardPlayer(playerNum);
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
        this.board.makeMove(otherPlayer(this.getPlayerNum()), opponentMoves.get(i));
        this.guiBoard.opponentMove(opponentMoves.get(i));
      }
      this.guiBoard.setMyMove();
      if (response.startsWith("WINNER")) {
        System.out.println("Other player went over time. You win! Thanks for playing!");
      }
      else {
        System.out.println("You went over time. You lose! Thanks for playing!");
      }

      this.board.printBoardPlayer(playerNum);
          socket.close();
          System.exit(0);
    }
    else if (response.startsWith("WINNER")) { //WINNER
      
      this.board.printBoardPlayer(playerNum);
      System.out.println("You win! Thanks for playing");
      
      //print final board
      this.board.winningBoard();
      this.board.printBoardPlayer(playerNum);
      this.guiBoard.updateGame();
      
      socket.close();
      System.exit(0);
    }
    else if (response.startsWith("LOSER")) { //LOSER
      
      this.board.printBoardPlayer(playerNum);
      System.out.println("You lose! Thanks for playing");
      
      //print final board
      this.board.winningBoard();
      this.board.printBoardPlayer(playerNum);
      this.guiBoard.updateGame();
      
      socket.close();
      System.exit(0);
    }
    else if (response.startsWith("TIE")) { //TIE

      this.board.printBoardPlayer(playerNum);
      System.out.println("Tie! Thanks for playing");

      //print final board
      this.board.winningBoard();
      this.board.printBoardPlayer(playerNum);
      this.guiBoard.updateGame();
      
      socket.close();
      System.exit(0);
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
  
  public int getPlayerNum() {
    return playerNum;
  }
  
  
  public static void main(String[] args) throws IOException {
    ConnectingUser u = new ConnectingUser();
    u.sendUserMoves();
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
  
}

