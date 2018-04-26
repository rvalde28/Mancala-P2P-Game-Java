import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.io.*;
import java.net.*;

//the program run by java Mancala acts as a client
///to run this program in a client/server style: type java ConnectingUser in one terminal, then java Mancala in another terminal
public class Mancala {

  boolean settingsInputed = false;
  boolean randomSettings = false;
  String gameType;
  String seeds;
  String pits;
  String numSeconds;

  public boolean sendSettings(){
    JComboBox numPits;
    JComboBox numSeeds;
    JComboBox kindOfGame;
    JButton submit;
    JButton random;
    JFrame start;
    JLabel choose;
    JPanel options;
    start = new JFrame("Start Mancala");
    start.setSize(500,300);
    start.setLayout(new BorderLayout());
    numPits = new JComboBox();
    numSeeds = new JComboBox();
    kindOfGame = new JComboBox();

    //this is the submit button for the game
    submit = new JButton("Create Game");
    
    random = new JButton("Random Seed Distribution");
    choose = new JLabel("Please enter the number of seconds, seeds, pits, and what kind of game to play: ");
    options = new JPanel();

    //this adds the text label to get the number of seconds
    String secondsLabel = "Seconds";
    JLabel seconds = new JLabel(secondsLabel, JLabel.TRAILING);
    options.add(seconds);
    JTextField textField = new JTextField(10);
    seconds.setLabelFor(textField);
    options.add(textField);

    //adds the options for the number of pits
    for(int i = 3; i < 10;i++){
      numPits.addItem(Integer.toString(i) + " pits");
    }

    //adds the options for the number of seeds
    for(int i = 1; i < 9;i++){
      numSeeds.addItem(Integer.toString(i) + " seeds");
    }

    //adds the options for the type of game
    kindOfGame.addItem("PvP");
    kindOfGame.addItem("PvAI");
    kindOfGame.addItem("AIvAI");

    //adds them to the panel
    options.add(numPits);
    options.add(numSeeds);
    options.add(kindOfGame);


    //adds them to the frame
    start.add(choose, BorderLayout.NORTH);
    start.add(options, BorderLayout.CENTER);
    start.add(submit, BorderLayout.SOUTH);
    start.add(random, BorderLayout.EAST);
    start.setVisible(true);


    //when the submit button is pressed, it sets all of the values from the frame
    submit.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        gameType = (String) kindOfGame.getSelectedItem();
        seeds = (String)numSeeds.getSelectedItem();
        pits = (String)numPits.getSelectedItem();
        settingsInputed = true;
        start.setVisible(false);
        start.dispose();
        String secondField = textField.getText();
        numSeconds = secondField;
      }
    });
	    random.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        
        randomSettings = true;
        
      }
    });
    //call the action button so that it sends a true
    return false;
  }

  public int getSeeds(){

    String[] split = seeds.split("\\s+");
    return Integer.parseInt(split[0]);
  }

  public int getPits(){
    String[] split = pits.split("\\s+");
    return Integer.parseInt(split[0]);
  }
  public String getGameType(){
    return gameType;
  }

  public int getNumSeconds(){
    String[] split = numSeconds.split("\\s+");
    return Integer.parseInt(split[0]);
  }



  public static void main(String args []) throws IOException, InterruptedException {

    //starts the to get the game configuration from the user
    Mancala frame = new Mancala();
    frame.sendSettings();
    //wait for the user to input the settings before continuing

    while(!frame.settingsInputed){
      
      //pause for a moment; linux server is slow and can't handle busy waiting
      try {
        Thread.sleep(1000);
      } 
      catch(InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }
 
    Manager a = new Manager(frame.getSeeds(), frame.getPits(), frame.getGameType(),frame.getNumSeconds(), frame.randomSettings);
		a.playGame();

  }
}
