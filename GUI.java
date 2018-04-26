//Temporary graphics class to create board
import java.*;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;

//class for holding the values of a button
class MyButton extends  JButton{
	String value;
	int index;

	MyButton(){}
	MyButton(int i){
		index = i;
	}

	void setValue(String s){
		value = s;
	}

	void setIndex(int i){
		index = i;
	}
	int getIndex(){
		return index;
	}
	int getValue(){
		return Integer.parseInt(value);
	}
}

//gui classs
public class GUI{
	private JFrame frame = new JFrame("Mancala");
	private JLabel messageLabel = new JLabel("Message Label");

	Board board = new Board();
	private int pits;
	private int seeds;

	//button holder for the player, opponent, and stores
	private Vector<MyButton> playerButton;
	private Vector<MyButton> opponentButton;
	private Vector<MyButton> stores;

	//timer thread
	Thread thread;
	Timing.Timer timer;
	boolean timerStarted = false;

	//move being made
	private String move = "";
	//number of the player
	private int playing;

	private long startTime;

	//sets if a move has been make and if it is your turn
	public boolean moveMade = false;
	private boolean myMove;

	//GUi constructor using a board to initialize the board
	GUI(Board b, int playerNum, int numPits){
		Board boardCopy = new Board(b);
		int[] myArray;
		int[] opponentArray;
		pits = numPits;
		playing = playerNum;

		//initializes the message label on the frame
		messageLabel.setBackground(Color.lightGray);
		frame.getContentPane().add(messageLabel,"South");

		//gets the values of the player side and opponent side to initialize on buttons
		if(playerNum == 1){
			myArray = boardCopy.returnPits(1);
			opponentArray = boardCopy.returnPits(2);
		}
		else if(playerNum == 2){
			myArray = boardCopy.returnPits(2);
			opponentArray = boardCopy.returnPits(1);
		}
		else{
			myArray = new int[0];
			opponentArray = new int[0];
		}

		//sets the turn depending on the player number
		if(playing == 1){
			messageLabel.setText("Your Move");
			myMove = true;
		}
		else if(playing == 2){
			myMove = false;
		}
		else{
		}


		//settings for the board and layout
		board = boardCopy;
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(120*(pits+5), 650);
		frame.setVisible(true);
		frame.setLayout(null);



		int size = 100;
		int start = 150;
		int increase = 125;

		//initialized the buttons on the board
		opponentButton = new Vector<>(pits);
		playerButton = new Vector<>(pits);
		stores = new Vector<>(2);

		//sets the button on the containers
		for(int i = 0; i < pits; i++){
			MyButton button = new MyButton();
			MyButton button2 = new MyButton();

			playerButton.add(i,button);
			opponentButton.add(i,button2);
		}

		//adds the buttons on the board and initializes them with its value
		for(int i = 0; i < pits;i++){
			//sets opponents buttons on board and sets the button value
			opponentButton.get(i).setBounds(start +(increase*(i+1)),50,size,size);
			opponentButton.get(i).setText(Integer.toString(opponentArray[pits-i-1]));
			opponentButton.get(i).setValue(Integer.toString(opponentArray[pits-i-1]));

			//sets the playe rbuttons on board and sets the button value
			playerButton.get(i).setBounds(start + (increase*(i+1)),500,size,size);
			playerButton.get(i).setText(Integer.toString(myArray[i]));
			playerButton.get(i).setValue(Integer.toString(myArray[i]));
			playerButton.get(i).setIndex(i);


			final int j = i;
			//action listener for when the button is pressed
			playerButton.elementAt(i).addActionListener(new ActionListener() {
				@Override
				//this sends the move to the client with the move command, and also makes the move on its board
				public void actionPerformed(ActionEvent e) {
					changebutton(Integer.toString(j));
					//makes a move only when the its the player turn, this also stops the timer
					if(!moveMade && myMove) {
						if (timerStarted) {
							timerStarted = false;
						}
						moveMade = true;
						int index = playerButton.elementAt(j).getIndex();
						board.makeMove(playing, index);
						messageLabel.setText("Opponent's Move");
						//updates the board one the move has been made
						updateGame();
					}
					//if it is not your turn then it is not your move
					else{
						System.out.println("NOT YOUR MOVE");
						messageLabel.setText("Not your turn");
					}
				}
			});

			frame.add(playerButton.elementAt(i));
			frame.add(opponentButton.elementAt(i));
		}


		//sets the store buttons on the baord
		MyButton player1Store = new MyButton();
		player1Store.setBounds(50,150,150,350);
		stores.add(0,player1Store);
		stores.get(0).setText("0");
		frame.add(stores.elementAt(0));

		MyButton player2Store = new MyButton();
		player2Store.setBounds(start +(increase*(pits+1)) + 30,150,150,350);
		stores.add(1,player2Store);
		stores.get(1).setText("0");
		frame.add(stores.elementAt(1));

	}

	//gui constructor used for standard game options
	GUI(int playerNum, int numPits, int numSeeds){
		pits = numPits;
		seeds = numSeeds;
		playing = playerNum;

		//initializes the message label on the frame
		messageLabel.setBackground(Color.lightGray);
		frame.getContentPane().add(messageLabel,"South");

		//sets the turn value based on the player number
		if(playing == 1){
			myMove = true;
			messageLabel.setText("Your Move");
		}
		else if(playing == 2){
			myMove = false;
			messageLabel.setText("Opponent's move");
		}
		else{

		}

		//settings for the board layout
		board = new Board(seeds,pits);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(120*(pits+5), 650);
		//frame.setSize(1250,650);
		frame.setVisible(true);
		frame.setLayout(null);
		int size = 100;
		int start = 150;
		int increase = 125;

		//initializes the vector of buttons
		opponentButton = new Vector<>(pits);
		playerButton = new Vector<>(pits);
		stores = new Vector<>(2);

		//adds buttons to the arrays
		for(int i = 0; i < pits; i++){
			MyButton button = new MyButton();
			MyButton button2 = new MyButton();

			playerButton.add(i,button);
			opponentButton.add(i,button2);
		}

		//initializes buttons on board
		for(int i = 0; i < pits;i++){
			//sets the values for the opponents buttons and sets them on the board
			opponentButton.get(i).setBounds(start +(increase*(i+1)),50,size,size);
			opponentButton.get(i).setText(Integer.toString(seeds));
			opponentButton.get(i).setValue(Integer.toString(seeds));

			//sets the values for the players buttons and sets tem on the board
			playerButton.get(i).setBounds(start + (increase*(i+1)),500,size,size);
			playerButton.get(i).setText(Integer.toString(seeds));
			playerButton.get(i).setValue(Integer.toString(seeds));
			playerButton.get(i).setIndex(i);


			final int j = i;
			//sets the actions to be performed when the button is pressed
			playerButton.elementAt(i).addActionListener(new ActionListener() {
				@Override
				//this sends the move to the client with the move command, and also makes the move on its board
				public void actionPerformed(ActionEvent e) {
					changebutton(Integer.toString(j));
					//makes the move an stops the timer when it is your turn to go
					if(!moveMade && myMove) {
						if (timerStarted) {
							timer.signalStopTimer();
							timerStarted = false;
						}
						moveMade = true;
						int index = playerButton.elementAt(j).getIndex();
						board.makeMove(playing, index);

						//updates the board once the move has been made
						updateGame();
					}
					else{
						System.out.println("NOT YOUR MOVE");
						messageLabel.setText("Not your turn");
					}
				}
			});

			frame.add(playerButton.elementAt(i));
			frame.add(opponentButton.elementAt(i));
		}


		//Set the buttons for the store
		MyButton player1Store = new MyButton();
		player1Store.setBounds(50,150,150,350);
		stores.add(0,player1Store);
		stores.get(0).setText("0");
		frame.add(stores.elementAt(0));

		MyButton player2Store = new MyButton();
		player2Store.setBounds(start +(increase*(pits+1)) + 30,150,150,350);
		stores.add(1,player2Store);
		stores.get(1).setText("0");
		frame.add(stores.elementAt(1));
	}

	//sets the value of the move
	public void changebutton(String s){
		move = s;
	}

	//updates the board
	public void updateGame(){
		//values needed
		int[] playerArray = new int[pits];
		int[] opponentArray = new int[pits];
		int[] storeValues = board.returnStores();

		//sets the board according to the players perspective
		switch (playing){
			case 1:
				//gets the values of the board
				playerArray = board.returnPits(1);
				opponentArray = board.returnPits(2);

				//sets the new values of the store
				stores.get(1).setText(Integer.toString(storeValues[0]));
				stores.get(0).setText(Integer.toString(storeValues[1]));

				//sets the values of the players buttons
				for(int i = 0; i < pits;i++){
					String value = Integer.toString(playerArray[i]);
					playerButton.get(i).setText(value);
					playerButton.get(i).setValue(value);
				}

				//sets the values of the opponents buttons
				for(int i = 0; i < pits; i++){
					//reverses the array to change board easier
					int[] reversedArray = new int[pits];
					for(int j = 0; j < opponentArray.length; j++){
						reversedArray[j] = opponentArray[(opponentArray.length-1)-j];
					}

					String value = Integer.toString(reversedArray[i]);
					opponentButton.get(i).setText(value);
					opponentButton.get(i).setValue(value);
				}

				break;
			//sets the board for player 2's perspective
			case 2:
				//gets the values of the sides of the opponent and player
				playerArray = board.returnPits(2);
				opponentArray = board.returnPits(1);

				//sets the store values
				stores.get(1).setText(Integer.toString(storeValues[1]));
				stores.get(0).setText(Integer.toString(storeValues[0]));

				//updates the players pockets
				for(int i = 0; i < pits;i++){
					String value = Integer.toString(playerArray[i]);
					playerButton.get(i).setText(value);
					playerButton.get(i).setValue(value);
				}

				//updates the opponents pockets
				for(int i = 0; i < pits; i++){
					int[] reversedArray = new int[pits];
					for(int j = 0; j < opponentArray.length; j++){
						reversedArray[j] = opponentArray[(opponentArray.length-1)-j];
					}

					String value = Integer.toString(reversedArray[i]);
					opponentButton.get(i).setText(value);
					opponentButton.get(i).setValue(value);
				}
				break;
		}
	}

	//returns the move when the button is pressed
	public int returnMove(){
		//sets message that it is the opponets turn
		messageLabel.setText("Opponent's Move");
		int index = Integer.parseInt(move);
		moveMade = false;
		return index;
	}

	//makes the move on the opponents and updates the board
	public void opponentMove(int move){
		switch (playing){
			case 1:
				board.makeMove(2,move);
				updateGame();
				break;
			case 2:
				board.makeMove(1,move);
				updateGame();
				break;
		}
	}

	//sets the turn back to the user
	void setMyMove(){
		//sets message that it is users turn
		messageLabel.setText("Your Move");
		myMove = true;
	}

	public static void main(String args[]){
		GUI gui = new GUI(1,4,2);

		while(true){
			System.out.println(gui.returnMove());
			gui.updateGame();
      
      //pause a moment; linux server is slow and cannot handle busywaiting
      try  {
          Thread.sleep(1000);
      } 
      catch(InterruptedException ex) {
          Thread.currentThread().interrupt();
      }
		}

		
	}
}