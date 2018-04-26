//Temporary graphics class to create board
import java.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D.Double;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class GUI extends JFrame{
	
	
	public GUI(){
		//setLayout(new FlowLayout());// so apparently this was overriding setBounds...
		setLayout(null);
		JButton button_1 = new JButton(" ");
		button_1.setBounds(250,50, 100, 100);
		add(button_1);
		JButton button_2 = new JButton(" ");
		button_2.setBounds(375,50, 100, 100);
		add(button_2);
		JButton button_3 = new JButton(" ");
		button_3.setBounds(500,50, 100, 100);
		add(button_3);
		JButton button_4 = new JButton(" ");
		button_4.setBounds(625,50, 100, 100);
		add(button_4);
		JButton button_5 = new JButton(" ");
		button_5.setBounds(750,50, 100, 100);
		add(button_5);
		JButton button_6 = new JButton(" ");
		button_6.setBounds(875,50, 100, 100);
		add(button_6);
		JButton button_7 = new JButton(" ");
		button_7.setBounds(250,500, 100, 100);
		add(button_7);
		JButton button_8 = new JButton(" ");
		button_8.setBounds(375,500, 100, 100);
		add(button_8);
		JButton button_9 = new JButton(" ");
		button_9.setBounds(500,500, 100, 100);
		add(button_9);
		JButton button_10 = new JButton(" ");
		button_10.setBounds(625,500, 100, 100);
		add(button_10);
		JButton button_11 = new JButton(" ");
		button_11.setBounds(750,500, 100, 100);
		add(button_11);
		JButton button_12 = new JButton(" ");
		button_12.setBounds(875,500, 100, 100);
		add(button_12);
		
		JButton button_player1 = new JButton("Player 1");
		button_player1.setBounds(1025, 150, 150, 350);
		add(button_player1);
		JButton button_player2 = new JButton("Player 2");
		button_player2.setBounds(50, 150, 150, 350);
		add(button_player2);
	
	
	
	
	}
	
	public static void main(String args[]){
		GUI gui = new GUI();
		gui.setTitle("Mancala");
		gui.setVisible(true);
		gui.setPreferredSize(new Dimension(1250, 650));
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.pack();
		
	}
}