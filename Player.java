import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.*;
import java.lang.Thread.*;
import java.util.concurrent.Semaphore;

abstract public class Player extends Thread {
  protected String name;
  
  protected Semaphore moveSem;
  protected Semaphore messageSem;
  protected Semaphore otherMoveSem;
  protected boolean done;
  
  protected ServerSocket listener;
  protected PrintWriter out;
  protected Socket socket;
  protected BufferedReader in;
  protected int time;
  protected char startingConfig;
  protected char play;
  protected int playerNum;
  protected Player otherPlayer;
  
  protected String move;
  protected String otherPlayerMove;
  
  enum playerType{PLAYER1, PLAYER2, NONE};  
  enum message{OK, ILLEGAL, TIME, LOSER, WINNER, TIE};
  
  protected playerType playing; 
  protected message msg;
  protected Board boardCopy; 
  
  
  abstract String getMove() throws IOException, InterruptedException;
  
  
  public Player(Socket socket, int playerNum, int time, Board mainBoard, Semaphore messageSem, Semaphore moveSem, Semaphore otherMoveSem) {
    this.socket = socket;
    this.messageSem = messageSem;
    this.moveSem = moveSem;
    this.otherMoveSem = otherMoveSem;
    this.done = false;
    this.time = time;
    startingConfig = 'S';
    msg = message.OK;
    move = "-1";
    this.playerNum = playerNum;
    otherPlayerMove = "-1";
    boardCopy = new Board(mainBoard);
      
    if (playerNum == 1) {
      playing = playerType.PLAYER1;
      play = 'F';
    }
    else if (playerNum == 2) {
      playing = playerType.PLAYER2;
      play = 'S';
    }
    else {
      System.out.println("Incorrect player number " + playerNum + "Exiting");
      System.exit(0);
    }
    
  }
  
  public Player() throws IOException, InterruptedException {
    name = "Generic name";
    playing = playerType.NONE;
    move = "-1";
    msg = message.OK;
    moveSem = new Semaphore(1);
    messageSem = new Semaphore(1);
    time = 0;
    startingConfig = 'S';
    
  }
  
  void setOtherPlayer(Player otherPlayer) {
    this.otherPlayer = otherPlayer;
  }
  
  public void init(boolean random, ArrayList<Integer> dist) {
    try {
      
      //send welcome
      out = new PrintWriter(socket.getOutputStream(), true);
      out.println("WELCOME");
      
      //pause a moment to let client set up stuff to read in
      try {
          Thread.sleep(1000);
      } 
      catch(InterruptedException ex) {
          Thread.currentThread().interrupt();
      }
      
      //if random, put distribution of seeds into INFO statement
      String seedConfig = "";
      if (random == true) {
        startingConfig = 'R';
        for (int i = 0; i < dist.size(); i++) {
          seedConfig = seedConfig +  " " + Integer.toString(dist.get(i));
        }
        
      }
      else {
        startingConfig = 'S';
      }
      //send board configuration
      System.out.println("Sending info to player " + playerNum);
      out = new PrintWriter(socket.getOutputStream(), true);
      out.println("INFO " + boardCopy.getNumPits() + " " + boardCopy.getInitNumSeeds() + " " + time + " " + play + " " + startingConfig + seedConfig); 
      
      //recieve client's READY message
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      String response = in.readLine();
      
      //Check that the message says READY
      if (!response.startsWith("READY")) {
        System.out.println("Was expecting READY, got " + response);
        socket.close();
        
        //end the game
        System.exit(0);
      }
      else {
        System.out.println("READY");
      }
      
    } 
    catch (IOException e) {
      System.out.println("Player " + playerNum + " lost");
    }
  }
  
  public void run() {
   
    try {
      this.connection();
    }
    catch(IOException ex) {}
    catch(InterruptedException ex) {}
    finally {
      try {
        this.sendMove(msg.name());
      }
      catch (IOException e) {}
      
      //wait for manager to finish its job
      while (this.done == false) {
        
        try {
          Thread.sleep(1000);
        } 
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
      }
      
      //wait until the other thread is done sending messages 
      while (otherPlayer.getDone() == false) {
        try {
          Thread.sleep(1000);
        } 
        catch(InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
      }
      
      try {
        this.sendMove(msg.name());
        socket.close();
      }
      catch (IOException e) {}
      
      //only execute once
      if (playerNum == 1) {
        System.out.println("Thanks for playing. Goodbye");
        try {
          listener.close();
        }
        catch (IOException e) {}
        catch (NullPointerException e) {}
      }
    }
    
  }
  
  
  private void connection() throws IOException, InterruptedException {
    //open the connection
    Timing.Timer timerObj = new Timing.Timer((int)Double.POSITIVE_INFINITY);
    Thread timeThread;
    String serverResponse = "";
    try {
      String otherVerif = "";
      
      if (playerNum == 2) {
        //recieve verification of player1's move from player2 client
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        otherVerif = in.readLine();
        
        //start the timer
        timerObj = new Timing.Timer(time);
        timeThread = new Thread(timerObj);
        timeThread.start();
        
        messageSem.acquire();
        
        //if player 2 said player1's move is illegal, report illegal move
        serverResponse = msg.name();
        if (serverResponse.startsWith("ILLEGAL")) {
          serverResponse = "ILLEGAL";
        }
        
        //send manager's response to p1 client
        otherPlayer.sendMove(serverResponse);
      }
      
      //get moves from connection
      int counter = 0;
      while (true) {
          //if we're done sending messages, break out, send final messages and close socket
          if (this.done == true) {
            break;
          }
          
           //In the first iteration, if this is player 1 then the timer has not been started yet
          if ((counter == 0) && (playerNum == 1)) {
            //start the timer
            timerObj = new Timing.Timer(time);
            timeThread = new Thread(timerObj);
            timeThread.start();
          }
          
          //get a move from this player, the client
          in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          move = in.readLine();
          moveSem.release();
          
          //stop the timer
          boolean overTime = timerObj.timeIsUp();
          
          timerObj.signalStopTimer();
          
          //Send move to the other client regardless of whether it is ok or not, so long as the timer isn't up
          if (overTime == false) {
            otherPlayer.setOtherPlayerMove(move);
            otherPlayer.sendMove(move);
            
            //semaphore to wait for a response from the manager
            messageSem.acquire(); 
          }
          
          //If the time limit was exceeded, report time and winner/loser
          if ((overTime) && (time != 0)) {
            
            this.setMessage(Player.message.TIME);
            this.sendMove(Player.message.TIME.name());
            otherPlayer.setMessage(Player.message.TIME);
            otherPlayer.sendMove(Player.message.TIME.name());
            
            //let the client have some time to prepare to recieve winner/loser messages
            try {
              Thread.sleep(1000);
            } 
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            
            this.sendMove(Player.message.LOSER.name());
            otherPlayer.sendMove(Player.message.WINNER.name());
            break;
          }
          
          //if manager or the other player say move is not ok, send message to the client
          if (otherVerif.startsWith("ILLEGAL")) {
            msg = Player.message.ILLEGAL;
          }
          else if (otherVerif.startsWith("TIME")) {
            msg = Player.message.TIME;
          }
          if (msg != Player.message.OK ) {
            break;
          }
          
          //if the game is done, break out of the loop
          if (this.done == true) {
            break;
          }
          
          //recieve verification of other player's move from this player's client
          in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          otherVerif = in.readLine();
          
          //start the timer
          timerObj = new Timing.Timer(time);
          timeThread = new Thread(timerObj);
          timeThread.start();
          
          if (otherVerif == null) {
            break;
          }
          
          //send illegal to other client if response is not OK
          if (otherVerif.startsWith("ILLEGAL")) {
            otherPlayer.setMessage(Player.message.ILLEGAL);
            otherPlayer.sendMove(Player.message.ILLEGAL.name());
          }
          else if (otherVerif.startsWith("TIME")) {
            otherPlayer.setMessage(Player.message.TIME);
            otherPlayer.sendMove(Player.message.TIME.name());
          }
          //send ok to other client if response is OK
          else {
            otherPlayer.sendMove("OK");
          }
         
          //if we didn't get anything from the client, the game is over
          if (move == null) {
            break;
          }
         counter++; 
      }
    }
    finally {
      //wait until manager is done sending messages, then clean up
      timerObj.signalStopTimer();
      while (this.done == false) {
        try {
          Thread.sleep(1000);
        } 
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
      }
      
      //wait until the other thread is done sending messages to close the port
      while (otherPlayer.getDone() == false) {
        try {
          Thread.sleep(1000);
        } 
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
      }
      
      this.sendMove(msg.name());
     
    }
        
    
    
  }
  
  public void setMessage(message msg) throws IOException {
    this.msg = msg;
    messageSem.release();
  }
  
  public void setBoard(Board newBoard) {
    boardCopy = new Board(newBoard);
  }
  
  public playerType getPlayerType() {
    return playing;
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
  
  public void setOtherPlayerMove(String move) {
    otherPlayerMove = move;
  }
  public Player.message getMessage() {
    return msg;
  }
  
  public void sendMove(String move) throws IOException {
    out = new PrintWriter(socket.getOutputStream(), true);
    out.println(move); 
  }
  
  public void setDone(boolean d) {
    this.done = d;
  }
  
  public boolean getDone() {
    return done;
  }

}
