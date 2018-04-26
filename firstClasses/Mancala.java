import java.util.*;

class Board{
    //private static final int DEFAULT_BOARD_SIZE = 14;
    private static final int INIT_NUM_SEEDS = 4;
    private static final int INIT_NUM_PITS = 6;
    //private LinkedList<Integer> userLocation = new LinkedList<Integer>();
    //private LinkedList<Integer> rivalLocation = new LinkedList<Integer>();
    private int [] pitsPlay1;
    private int [] pitsPlay2;
    private int storePlay1;
    private int storePlay2;
    //private int ownStore;
    //private int rivalStore;

    //constructor initializes the values of the board;
    Board(){
        System.out.println("Constructor Created");
        
        //create each player's pits
        pitsPlay1 = new int[INIT_NUM_PITS];
        pitsPlay2 = new int[INIT_NUM_PITS];
        
        //fill each pit with seeds
        Array.fill(storePlay1, INIT_NUM_SEEDS);
        Array.fill(storePlay2, INIT_NUM_SEEDS);
        
        //ownStore = 0;
        //rivalStore = 0;
        
        //initialize each player's store to 0
        storePlay1 = 0;
        storePlay2 = 0;
        
        // for(int i = 0; i < (DEFAULT_BOARD_SIZE/2) - 1; i++){
            // userLocation.add(4);
            // rivalLocation.add(4);
        // }
    }
    //prints the board on the terminal
    public void printBoard(){
      
        //output from player 1's perspective
        //player 2 up top, in reverse order
        
        // for(int i = 0; i < rivalLocation.size(); i++){
            // System.out.print(rivalLocation.get(i));
        // }
        System.out.print("\t");
        for (int i = 0; i < pitsPlay2.length; i++) {
          System.out.print(pitsPlay2[i] + " ");
        }
        
        // System.out.println(rivalStore + "      " + ownStore);
        System.out.println(storePlay2 + "      " + storePlay1);
        
        System.out.print("\n\t");
        //System.out.println(" ");
        for (int i = 0; i < pitsPlay1.length; i++) {
          System.out.print(pitsPlay1[i] + " ");
        }
        System.out.println("");
        // for(int i = 0; i < userLocation.size();i++){
            // System.out.print(userLocation.get(i));
        // }
    }
    
    public static void main(String [] args) {
      Board b1 = new Board();
      b1.printBoard();
    }
    // getter function for the user locations
    // public Integer userPocketValue(int index){
        // return userLocation.get(index);
    // }

    // setter function for the user locations
    // public void setUserLocation(int index, int value){
        // userLocation.set(index, value);
    // }

    // getter function for the rival location
    // public Integer rivalPocketValue(int index){
        // return rivalLocation.get(index);
    // }

    // setter function for the rival locations
    // public void setRivalLocation(int index, int value){
        // rivalLocation.set(index, value);
    // }

    // increments the rival store
    // public void addRivalStore(){
        // rivalStore++;
    // }

    // decrements the user store
    // public void addOwnStore(){
        // ownStore++;
    // }
    // public int getOwnStoreValue(){
        // return ownStore;
    // }
    // public int getRivalStoreValue(){
        // return rivalStore;
    // }


}

// public class Mancala extends Board{
    //10 pockets exist on the board
    // private Board b = new Board();

    // Mancala(){

    // }

    // public void printBoard(){
        // b.printBoard();
    // }

    // public void userMove(){
        // System.out.println("Enter a pocket number to make a move(1-6)");
        // String input = System.console().readLine();
        // int pocketLocation = Integer.parseInt(input) -1 ;
        // int pocketValue = b.userPocketValue(pocketLocation);
        // b.setUserLocation(pocketLocation,0);

        // while(pocketValue != 0){
            // if(pocketLocation >= 6){

                // int rivalPocketValue;
            // }
            // pocketValue--;
        // }
    // }

    // public void AIMove(){}

// }
