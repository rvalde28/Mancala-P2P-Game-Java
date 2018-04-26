import java.util.*;
import static java.util.Arrays.*;

public class Board {
    private static final int NUM_PLAYERS = 2;
    private int initNumSeeds;
    private int initNumPits;
    
    private ArrayList<ArrayList<Integer> > pits;
    private ArrayList<Integer> stores;
    
    //constructor initializes the values of the board
    public Board(){
        
        initNumSeeds = 4;
        initNumPits = 6;
        
        //create each player's pits and stores
        stores = new ArrayList<Integer>();
        pits = new ArrayList<ArrayList<Integer> >();
        
        //fill each pit with seeds and initialize stores to 0
        ArrayList<Integer> oneSide = new ArrayList<Integer>();
        for (int i = 0; i < NUM_PLAYERS; i++) {
          
          //fill up one side of the board
          for (int j = 0; j < initNumPits; j++) {
            oneSide.add(new Integer(initNumSeeds));
          }
          
          pits.add(oneSide);
          oneSide = new ArrayList<Integer>();
          
          //initialize player's store to 0
          stores.add(0);
        }
        
    }
    
    //constructor initializes the values of the board
    public Board(int initNumSeeds, int initNumPits){
        
        this.initNumSeeds = initNumSeeds;
        this.initNumPits = initNumPits;
        
        //create each player's pits and stores
        stores = new ArrayList<Integer>();
        pits = new ArrayList<ArrayList<Integer> >();
        
        //fill each pit with seeds and initialize stores to 0
        ArrayList<Integer> oneSide = new ArrayList<Integer>();
        for (int i = 0; i < NUM_PLAYERS; i++) {
          
          //fill up one side of the board
          for (int j = 0; j < initNumPits; j++) {
            oneSide.add(new Integer(initNumSeeds));
          }
          
          pits.add(oneSide);
          oneSide = new ArrayList<Integer>();
          
          //initialize player's store to 0
          stores.add(i,0);
        }
    }
    
    //for initializing the board with the random integers sent from the server
    public Board(ArrayList<Integer> randSeeds, int initNumPits) {
      //get the initial number of seeds
      int totalSeeds = 0;
      for (int i = 0; i < randSeeds.size(); i++) {
        totalSeeds = totalSeeds + randSeeds.get(i);
      }
      this.initNumSeeds = totalSeeds/initNumPits;
      
      this.initNumPits = initNumPits;
      
      if (randSeeds.size() != initNumPits) {
        System.out.println("Size of randseeds array does not match number of pits! Exiting");
        System.exit(0);
      }
      
      //create each player's pits and stores
      stores = new ArrayList<Integer>();
      pits = new ArrayList<ArrayList<Integer> >();
      
      //fill each pit with seeds and initialize stores to 0
      ArrayList<Integer> oneSide = new ArrayList<Integer>();
      for (int i = 0; i < NUM_PLAYERS; i++) {
        
        //fill up one side of the board
        for (int j = 0; j < initNumPits; j++) {
          oneSide.add(randSeeds.get(j));
        }
        
        pits.add(oneSide);
        oneSide = new ArrayList<Integer>();
        
        //initialize player's store to 0
        stores.add(i,0);
      }
    }
    
    
    //copy constructor
    public Board(Board bSource) {
      initNumPits = bSource.getNumPits();
      
      initNumSeeds = bSource.getInitNumSeeds();
      
      //assumes dimensions of this board and source board are the same
      ArrayList<Integer> oneSide = new ArrayList<Integer>(bSource.pits.get(0));
      ArrayList<Integer> otherSide = new ArrayList<Integer>(bSource.pits.get(1));
      pits = new ArrayList<ArrayList<Integer> >();
      pits.add(oneSide);
      pits.add(otherSide);
      stores = new ArrayList<Integer>(bSource.stores);
      
    }
    
    //prints the board to the terminal from player 1's perspective
    //assumes there are exactly 2 players
    public void printBoardPlayer(int currentlyPlaying){
        System.out.print("\t");
        switch (currentlyPlaying){
            case 1:
                for (int i = pits.get(1).size() - 1; i >= 0; i--) {
                    System.out.print(pits.get(1).get(i) + " ");
                }

                System.out.println( "\n" + stores.get(1) + "\t\t\t" + stores.get(0));

                System.out.print("\t");

                //print player 1's pits
                for (int i = 0; i < pits.get(0).size(); i++) {
                    System.out.print(pits.get(0).get(i) + " ");
                }
                System.out.println("");
                break;
            case 2:
                for (int i = pits.get(0).size() - 1; i >= 0; i--) {
                    System.out.print(pits.get(0).get(i) + " ");
                }

                System.out.println( "\n" + stores.get(0) + "\t\t\t" + stores.get(1));

                System.out.print("\t");

                //print player 2's pits
                for (int i = 0; i < pits.get(1).size(); i++) {
                    System.out.print(pits.get(1).get(i) + " ");
                }
                System.out.println("");
                break;
        }

    }
    
    //add one seed to player's pit at the specified index
    //return -1 for error, 0 for success
    int addSeed(int playerNum, int index){
      if ((playerNum != 1) && (playerNum != 2)) {
        return -1;
      }
      playerNum--;
      //make sure we have a valid index
      if ((index < 0) || (index > this.getNumPits())) {
          return -1;
      }
      
      //add one seed to player's side at index
      pits.get(playerNum).set(index, pits.get(playerNum).get(index) + 1);
      
      return 0;
    }

    //return -1 for error
    public int addStore(int playerNum) {
      if ((playerNum != 1) && (playerNum != 2)) {
        System.out.println("Error with addStore!");
        return -1;
      }
      int playerIndex = playerNum - 1;


      //add 1 to the store of the specified player
      stores.set(playerIndex, stores.get(playerIndex) + 1);
      return 0;
    }

    public int addStoreByValue(int currentlyPlaying, int value) {
        //make sure playerNum is valid
        if ((currentlyPlaying != 1) && (currentlyPlaying != 2)) {
            return -1;
        }
        
        //make sure value is positive
        if (value < 0) {
            return -1;
        }

        int playerIndex = currentlyPlaying - 1;
        this.stores.set(playerIndex, stores.get(playerIndex) + value);
        return 0;
    }
    
    //remove all seeds from pit specified by index for player 1
    //returns -1 for error
    int wipeSeeds(int currentlyPlaying, int index) {
        
        //check playerNum is valid
        if ((currentlyPlaying != 1) && (currentlyPlaying != 2)) {
            return -1;
        }

        int playerNum = currentlyPlaying - 1;
        
        //check for valid index
        if ((index < 0) || (index >= pits.get(playerNum).size())) {
            return -1;
        }

        //take all seeds out of this player's pit
        pits.get(playerNum).set(index, 0);

        return 0;
    }
    
    int getSeedsInPit(int playerNum, int index){
       
        if ((playerNum != 1) && (playerNum != 2)) {
            System.out.println("Wrong player number for getSeedsInPit: " + playerNum);
            return -1;
        }
        else if ((index < 0) || (index > this.getNumPits())) {
          System.out.println("Wrong index for getSeedsInPit " + index);
          return -1;
        }
        
        else {
          playerNum--;
          return pits.get(playerNum).get(index);
        }
    }
    
    //returns the board in format: player1 store, player1 pits, player2 store, player2 pits
    public ArrayList<String> sendingFormat() {
      ArrayList<String> send = new ArrayList<String>();
      
      //add this player's store
      send.add(Integer.toString(stores.get(0)));
      
      //add this player's pits
      for (int i = 0; i < this.getNumPits(); i++) {
        send.add(Integer.toString(pits.get(0).get(i)));
      }
      
      //add other player's store
      send.add(Integer.toString(stores.get(1)));
      
      //add other player's pits
      for (int i = 0; i < this.getNumPits(); i++) {
        send.add(Integer.toString(pits.get(1).get(i)));
      }
      
      return send;
    }
    
    int otherPlayerSide(int player) {
      if (player == 1) {
        return 2;
      }
      else if (player == 2){
        return 1;
      }
      else {
        return -1;
      }
    }
    
    synchronized boolean isValidMove(int playerNum, int move) {
      //make sure player number is valid
      if ((playerNum != 1) && (playerNum != 2)) {
        System.out.println("Incorrect player number " + playerNum);
        return false;
      }
      
      //make sure index is valid 
      if ((move < 0) || (move >= this.getNumPits())) {
        return false;
      }
      
      //make sure player does not choose empty pit
      else if (this.getSeedsInPit(playerNum, move) == 0) {
        return false;
      } 
      
      else {
        return true;
      }
    }
    
    //return -1 for error, 0 for everything is fine, 1 for go again
  public int makeMove(int playerSide, int move) {
    
    if (!isValidMove(playerSide, move)) {
      return -1;
    }
    
    //wipe the seeds from index specified by user (set num of seeds in pit to 0)
    int numSeeds = this.getSeedsInPit(playerSide, move); 
    this.wipeSeeds(playerSide, move);
    
    //add one to every pit and store crossed, not including other player's store
    int index = move + 1;
    
    //will hold the ending pit by the end of the while loop
    int lastPit = 0; 
    
    //holds the side of the board we are distributing on: 1 or 2
    int boardSide = playerSide;
    
    while (numSeeds > 0) {
      lastPit = index;
      //if player is at the end and at own store, add to store, switch to other side of the board and continue on
      if ((index == this.getNumPits()) && (boardSide == playerSide)) {
        
        //add to store
        this.addStore(playerSide);
        numSeeds--;
        
        //switch board sides
        boardSide = otherPlayerSide(boardSide);
        index = 0;
        
        //if player ends in their store, they do another turn
        if (numSeeds == 0) {
          return 1;
        }
      }
      
      //if we're at the end and not at our own store then do not add to store, switch to other side and continue on
      else if (index == this.getNumPits()) {
        
        //switch board sides
        boardSide = otherPlayerSide(boardSide);
        index = 0;
        
      }
      //just add a seed and move on
      else {
        this.addSeed(boardSide, index);
        numSeeds--;
        index++;
      }
    }
    
    if (lastPit == this.getNumPits()) {
      return 0;
    }
    //if the user ends their turn in a pit on their own side in an empty pit and the pit directly 
    //  across from it (on the other player's side) has seeds, all seeds in both pits 
    //  go to the store of the user who just finished moving seeds
    
    int otherSide = otherPlayerSide(playerSide);
    if ((boardSide == playerSide) && 
      (this.getSeedsInPit(playerSide, lastPit) == 1) && 
      (this.getSeedsInPit(otherSide, this.getNumPits() - lastPit - 1) != 0)) {
      
      int pitSeeds1 = this.getSeedsInPit(playerSide, lastPit);
      int pitSeeds2 = this.getSeedsInPit(otherSide, this.getNumPits() - lastPit - 1);
      
      //wipe seeds from both pits
      this.wipeSeeds(playerSide, lastPit);
      this.wipeSeeds(otherSide, this.getNumPits() - lastPit - 1);
      
      //put seeds in p's store
      addStoreByValue(playerSide, pitSeeds1 + pitSeeds2);
    }
    return 0;
  }
  
  boolean checkWinner() {
    int counterPlayer1 = 0;
    int counterPlayer2 = 0;


    for(int i = 0; i < this.getNumPits(); i++){
        int player1PocketValue = this.getSeedsInPit(1, i);
        int player2PocketValue = this.getSeedsInPit(2, i);
        
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

    public int accessStore(int player){
      int index = player - 1;
      if (index < 0 || index > 1) {
        return -1;
      }
      return stores.get(index);
    }

    public int[] returnStores(){
        int[] array = new int[2];
        for(int i = 0; i < stores.size();i++){
            array[i] = stores.get(i);
        }
        return array;
    }

    public int[] returnPits(int playerNum){
        int[] array = new int[initNumPits];

        switch (playerNum){
            case 1:
                for(int i = 0; i <initNumPits;i++){
                    array[i] = pits.get(0).get(i);
                }
                break;

            case 2:
                for(int i = 0; i <initNumPits;i++){
                    array[i] = pits.get(1).get(i);
                }
                break;
        }
        return array;
    }

    public void pieMove(){
        int[][] tempArray = new int[NUM_PLAYERS][initNumPits];
        for(int i = 0; i < NUM_PLAYERS; i++) {
            for (int j = 0; j < initNumPits; j++) {
                tempArray[i][j] = pits.get(i).get(j);
            }
        }

        for(int i = NUM_PLAYERS-1; i >= 0; i--){
            for(int j = 0; j < initNumPits; j++){
                pits.get(i).set(j, tempArray[(NUM_PLAYERS-1)-i][j]);
            }
        }


        int myStore = stores.get(1);
        int opponentStore = stores.get(0);

        stores.set(0,myStore);
        stores.set(1,opponentStore);
    }
    
    public int sumOfPits(int playerNum) {
      
      if ((playerNum != 1) && (playerNum != 2)) {
        return -1;
      }
      ArrayList<Integer> playerPits = new ArrayList<Integer>(pits.get(playerNum - 1));
      
      int sum = 0;
      for (int i = 0; i < playerPits.size(); i++) {
        sum = sum + playerPits.get(i);
      }
      return sum;
      
    }
    
    //if the game is over, put all the seeds on a player's side into their store
    public int winningBoard() {
      
      if (!checkWinner()) {
        return -1;
      }
      
      //add all of p1's seeds to store
      int p1Seeds = sumOfPits(1);
      this.addStoreByValue(1, p1Seeds);
      
      //wipe seeds from p1's side
      for (int i = 0; i < pits.get(0).size(); i++) {
        this.wipeSeeds(1, i);
      }
      
      //add all of p2's seeds to store
      int p2Seeds = sumOfPits(2);
      this.addStoreByValue(2, p2Seeds);
      
      //wipe seeds from p2's side
      for (int i = 0; i < pits.get(1).size(); i++) {
        this.wipeSeeds(2, i);
      }
      
      return 0;
      
    }

    public int getInitNumSeeds() {
      return initNumSeeds;
    }
    
    public int getNumPits(){
        return initNumPits;
    }

}