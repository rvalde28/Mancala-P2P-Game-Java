import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.Semaphore;
import java.lang.*;

class Manager {
  private Player player1;
  private Player player2; 
  
  //public final Semaphore moveSem;
  public final Semaphore messageSem1;
  public final Semaphore messageSem2;
  public final Semaphore p1MoveSem;
  public final Semaphore p2MoveSem;
  protected ServerSocket listener;
  protected PrintWriter out;
  protected Socket socket;
  protected BufferedReader in;
  
  //Don't allow a getter for board, as we want the manager to be the only one changing it
  private Board mainBoard;
  int time;
  int numSeeds;
  int numPits;
  String gameType;
  int numSeconds;
  
  //Holds AI process if it exists
  Process AIprocess;
  
  //by default player 1 is a user, player 2 is AI
  Manager(int seeds, int pits, String gameType, int seconds, boolean random) throws IOException, InterruptedException {
    //random distribution of seeds
    ArrayList<Integer> rand = new ArrayList<Integer>();
    if (random == true) {
      rand = randomDist(pits, seeds);
      mainBoard = new Board(rand, pits);
      mainBoard.printBoardPlayer(1);
    }
    else {
      mainBoard = new Board(seeds,pits);
    }
    
    numSeeds = seeds;
    numPits = pits;
    this.gameType = gameType;
    this.numSeconds = seconds;

    listener = new ServerSocket(56789);
    
    messageSem1 = new Semaphore(1);
    messageSem2 = new Semaphore(1);
    p1MoveSem = new Semaphore(1);
    p2MoveSem = new Semaphore(1);
    
    System.out.println("Initial board:");
    mainBoard.printBoardPlayer(1);

    this.time = seconds;
    System.out.println("Starting Mancala");

    //this sections determines what game type we want to run, PvP, PvAI, AIvAI
    if(gameType.equals("PvP")) {
        System.out.println("Waiting for connections...");
        player1 = new User(listener.accept(), 1, time, mainBoard, messageSem1, p1MoveSem, p2MoveSem);
        System.out.println("Player 1 connected");
        player2 = new User(listener.accept(), 2, time, mainBoard, messageSem2, p2MoveSem, p1MoveSem);
        System.out.println("Player 2 connected");
        player1.setOtherPlayer(player2);
        player2.setOtherPlayer(player1);
        
        //start sending messages
        player1.init(random, rand);
        player2.init(random, rand);
        player1.start();
        player2.start();
    }
    else if (gameType.equals("PvAI")){ 
        System.out.println("Waiting for connection...");
        player1 = new User(listener.accept(), 1, time, mainBoard, messageSem1, p1MoveSem, p2MoveSem);
        System.out.println("Player 1 connected");
        
        //launch the AI program
        AIThread a = new AIThread();
        a.start();
        
        player2 = new User(listener.accept(), 2, time, mainBoard, messageSem2, p2MoveSem, p1MoveSem);
        
        player1.setOtherPlayer(player2);
        player2.setOtherPlayer(player1);
        
        //start sending messages
        player1.init(random, rand);
        player2.init(random, rand);
        player1.start();
        player2.start();
    }
    
    else if (gameType.equals("AIvAI")){ 
        System.out.println("Waiting for other AI to connect...");
        player1 = new User(listener.accept(), 1, time, mainBoard, messageSem1, p1MoveSem, p2MoveSem);
        System.out.println("AI 1 connected");
        
        //launch the AI program
        AIThread a = new AIThread();
        a.start();
        
        player2 = new User(listener.accept(), 2, time, mainBoard, messageSem2, p2MoveSem, p1MoveSem);
        System.out.println("Both AIs connected");
        
        player1.setOtherPlayer(player2);
        player2.setOtherPlayer(player1);
        
        //start sending messages
        player1.init(random, rand);
        player2.init(random, rand);
        player1.start();
        player2.start();
    }
    
    else{
        System.out.println("Incorrect game type");
    }

  }
  

  private synchronized boolean isValidMove(Player currentlyPlaying, int move) throws IOException {
    //make sure index is valid
    if ((move < 0) || (move >= mainBoard.getNumPits())) {
      return false;
    }
    
    //make sure player does not choose empty pit
    else if (mainBoard.getSeedsInPit(currentlyPlaying.getPlayerType().ordinal() + 1, move) == 0) {
      return false;
    } 
    
    else {
      //send continue message
      currentlyPlaying.setMessage(Player.message.OK);
 
      return true;
    }
  }
  
  //returns whether there is a winner or not
  boolean checkWinner() {
    int counterPlayer1 = 0;
    int counterPlayer2 = 0;

    for(int i = 0; i < mainBoard.getNumPits(); i++){
        int player1PocketValue = mainBoard.getSeedsInPit(player1.getPlayerType().ordinal() + 1, i);
        int player2PocketValue = mainBoard.getSeedsInPit(player2.getPlayerType().ordinal() + 1, i);
        
        if(player1PocketValue > 0) {
            counterPlayer1++;
        }
        if(player2PocketValue > 0) {
            counterPlayer2++;
        }
    }

    if(counterPlayer1 == 0){
        return true;
    }
    else if(counterPlayer2 == 0){
        return true;
    }

    return false;
  }


  public Player.playerType returnWinner() throws IOException, InterruptedException {
      Player.playerType noWinner = Player.playerType.NONE;
      int pitValue;

      for(int i = 0; i < mainBoard.getNumPits(); i++){

          pitValue = mainBoard.getSeedsInPit(1, i);
          mainBoard.addStoreByValue(1, pitValue);
          pitValue = mainBoard.getSeedsInPit(2, i);
          mainBoard.addStoreByValue(2, pitValue);
      }
      
      if(mainBoard.accessStore(1) > mainBoard.accessStore(2)){

          return player1.getPlayerType();
      }
      else if(mainBoard.accessStore(1) < mainBoard.accessStore(2)){
          return player2.getPlayerType();
      }
      else{
        return noWinner;
      }

  }

  //If we have two players in the game, this fuction takes as input one Player and outputs the other Player
  private Player.playerType otherPlayerType(Player.playerType player) {
    if (player == player1.getPlayerType()) {
      return player2.getPlayerType();
    }
    else {
      return player1.getPlayerType();
    }

  }

    //TODO: send last move to loser before sending the loser message
  public void playGame() throws IOException, InterruptedException {
   
    int turn = 1;
    int counter = 0;
    String player2Move = "";
    String player1Move = "";
    Board prevBoard;
    while (!checkWinner()) {
        prevBoard = new Board(mainBoard);
        counter++;
        mainBoard.printBoardPlayer(player1.playerNum);
        if(turn == 1) {
          
          player1Move = player1.getMove();

          ArrayList<Integer> player1Moves = new ArrayList<Integer>(parseForMoves(player1Move));
          
          //make every legal move. If we encounter an illegal move, set message to illegal 
          for (int i = 0; i < player1Moves.size(); i++) {

            if (mainBoard.isValidMove(1, player1Moves.get(i))) {
              if ((player1.getMessage() == Player.message.OK) && !checkWinner()) {
                player1.setMessage(Player.message.OK);
              }
              else {
                break;
              }
              int makeReturn = mainBoard.makeMove(1, player1Moves.get(i));
              
              //if not supposed to go again but there is another move, this move is illegal
              if ((makeReturn == 0) && (i != player1Moves.size() - 1)) {
                player1.setMessage(Player.message.ILLEGAL);
                mainBoard = prevBoard;
                break;
              }
            }
            else {
              player1.setMessage(Player.message.ILLEGAL);
              mainBoard = prevBoard;
              break;
            }
          }
          turn = 2;
          player1.setBoard(mainBoard);
          player2.setBoard(mainBoard);
        }
        
        else if(turn == 2) {
            player2Move = player2.getMove();
            ArrayList<Integer> player2Moves = new ArrayList<Integer>(parseForMoves(player2Move));
            
            //pie move
            if (player2Move.startsWith("P") && counter == 2) {
              System.out.println("Pie move:");
              mainBoard.pieMove();
              mainBoard.printBoardPlayer(turn);
              player2.setMessage(Player.message.OK);
              player1.setBoard(mainBoard);
              player2.setBoard(mainBoard);
              turn = 1;
              continue;
            }
            
            //make every legal move. If we encounter an illegal move, set message to illegal and reset the board to what it was before
            for (int i = 0; i < player2Moves.size(); i++) {
              if (mainBoard.isValidMove(2, player2Moves.get(i))) {
                
                if ((player2.getMessage() == Player.message.OK) && !checkWinner()) {
                  player2.setMessage(Player.message.OK);
                }
                else {
                  break;
                }
                int makeReturn = mainBoard.makeMove(2, player2Moves.get(i));
                if ((makeReturn == 0) && (i != player2Moves.size() - 1)) {
                  player2.setMessage(Player.message.ILLEGAL);
                  mainBoard = prevBoard;
                  break;
                }
              }
              else {
                player2.setMessage(Player.message.ILLEGAL);
                mainBoard = prevBoard;
                break;
              }
            }
            turn = 1;
            player1.setBoard(mainBoard);
            player2.setBoard(mainBoard);
        }
        
        if (player1.getMessage() == Player.message.ILLEGAL) {
          player2.sendMove(Player.message.ILLEGAL.name());
          player1.sendMove(Player.message.ILLEGAL.name());
          
          //pause to let client prepare to recieve winner/loser
          try {
            Thread.sleep(1000);
          } 
          catch(InterruptedException ex) {
              Thread.currentThread().interrupt();
          }
          System.out.println("Final board:");
          mainBoard.printBoardPlayer(1);
          
          System.out.println("Player 2 wins! Player 1 sent an invalid move.");
          player1.sendMove(Player.message.LOSER.name());
          player2.sendMove(Player.message.WINNER.name());
          break;
        }
        else if (player2.getMessage() == Player.message.ILLEGAL) {
          player2.sendMove(Player.message.ILLEGAL.name());
          player1.sendMove(Player.message.ILLEGAL.name());
          
          //pause to let client prepare to recieve winner/loser
          try {
            Thread.sleep(1000);
          } 
          catch(InterruptedException ex) {
              Thread.currentThread().interrupt();
          }
          
          System.out.println("Final board:");
          mainBoard.printBoardPlayer(1);
       
          System.out.println("Player 1 wins! Player 2 sent an invalid move.");
          player2.sendMove(Player.message.LOSER.name());
          player1.sendMove(Player.message.WINNER.name());
          break;
        }
        else if (player1.getMessage() == Player.message.TIME || player2.getMessage() == Player.message.TIME) {
          break;
        }
        
        
    }

    if(checkWinner() && (player1.getMessage() != Player.message.TIME) && (player2.getMessage() != Player.message.TIME)){
      
        mainBoard.printBoardPlayer(1);
        System.out.println("Final board:");
        mainBoard.winningBoard();
        mainBoard.printBoardPlayer(1);
        
        //send the last move 
        //most recent player to go was player 2
        if (turn == 1) { 
          player1.sendMove(player2Move);
        }
        //most recent player to go was player 1
        else if (turn == 2) { 
          player2.sendMove(player1Move);
        }
        
        //pause to let client prepare to recieve Winner/loser message
        try {
          Thread.sleep(1000);
        } 
        catch(InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
      
        Player.playerType winningPlayer = returnWinner();
        if(winningPlayer == player1.getPlayerType()){
            System.out.println("Player 1 has won!");
            player1.setMessage(Player.message.WINNER);
            player2.setMessage(Player.message.LOSER);
            player1.sendMove(Player.message.WINNER.name());
            player2.sendMove(Player.message.LOSER.name());
        }

        else if (winningPlayer == player2.getPlayerType()){
            System.out.println("Player 2 has won!");
            player1.setMessage(Player.message.LOSER);
            player2.setMessage(Player.message.WINNER);
            player1.sendMove(Player.message.LOSER.name());
            player2.sendMove(Player.message.WINNER.name());
        }
        
        else {
            System.out.println("TIE");
            player1.setMessage(Player.message.TIE);
            player2.setMessage(Player.message.TIE);
            player1.sendMove(Player.message.TIE.name());
            player2.sendMove(Player.message.TIE.name());
        }
        
    }
    
    player2.setDone(true);
    player1.setDone(true);
    
    player1.join();
    player2.join();
    
    // mainBoard.printBoardPlayer(1);
    // System.out.println("Final board:");
    // mainBoard.winningBoard();
    // mainBoard.printBoardPlayer(1);
    
  }
  
  public Player otherPlayer(Player p) {
    if (p == player1) {
      return player2;
    }
    else if (p == player2) {
      return player1;
    }
    //error
    else {
      return null;
    }
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
  
  //get a random distribution of seeds for one side of the board
  public ArrayList<Integer> randomDist(int pits,int seeds) {
    ArrayList<Integer> distribution = new ArrayList<Integer>();
    Random rand = new Random();
    int totalSeeds = pits*seeds;
    
    int seedsLeft = totalSeeds;
    
    for (int i = 0; i < pits - 1; i++) {
      int thisPit = rand.nextInt(seedsLeft);
      seedsLeft = seedsLeft - thisPit;
      distribution.add(thisPit);
    }
    
    distribution.add(seedsLeft);
    
    return distribution;
    
  }



}
  
  
  
  
        
    