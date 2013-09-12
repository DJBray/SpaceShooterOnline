package spaceWar;
import java.awt.*;
import java.awt.event.*;

import javax.swing.KeyStroke;


/*
 * Created on Nov 17, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * Captures user input events and displays GUI containing a view 
 * of sector.
 * 
 * @author bachmaer
 */
public class SpaceGameGUI extends Frame {

	private static final long serialVersionUID = 1L;

	// The components of the the GUI. 
	private Button rtTurn = new Button("Turn Right");
	private Button lfTurn = new Button("Turn Left");
	private Button fire= new Button("Fire");
	private Button foward = new Button("Foward");
	private Button backward = new Button("Backward");
	private Button join = new Button("Join");

	SpaceGUIInterface game = null;

	public SpaceGameGUI(SpaceGUIInterface g, Sector sector) {

		super("Space War Client");

		this.game = g;

		// Create panels for holding the fields. To make it look nice,
		// create an extra panel for holding all the child panels.
		Panel controlPanel = new Panel(new GridLayout(0, 1));
		
		foward.setPreferredSize(new Dimension( 50, 50));
		controlPanel.add(foward);
		backward.setPreferredSize(new Dimension( 50, 50));
		controlPanel.add(backward);
		lfTurn.setPreferredSize(new Dimension( 50, 50));
		controlPanel.add(lfTurn);
		rtTurn.setPreferredSize(new Dimension( 50, 50));
		controlPanel.add(rtTurn);
		fire.setPreferredSize(new Dimension( 50, 50));
		controlPanel.add(fire);
		join.setPreferredSize(new Dimension( 50, 50));
		controlPanel.add(join);

		// Create and add listeners for each of the buttons
		foward.addActionListener(new fowardListener());
		backward.addActionListener(new backwardListener());
		lfTurn.addActionListener(new lfTurnListener());
		rtTurn.addActionListener(new rtTurnListener());
		fire.addActionListener(new fireListener());
		join.addActionListener(new joinListener());

		// Add a listener for x in menu bar
		addWindowListener(new QuitListener());
		
		// Set up key board response
		sector.setFocusable(true);
		sector.addKeyListener(new keyboardHandler());

		// Add the game display to the window
		this.add(sector, BorderLayout.CENTER);
		sector.setPreferredSize(new Dimension(Constants.MAX_SECTOR_X, Constants.MAX_SECTOR_Y));

		// Add the buttons to the window
		this.add("East", controlPanel);

		this.pack();
		this.setResizable(false);

		this.setVisible(true);

	} // end default constructor

	
	// Inner class Handler for the right turn event
	class rtTurnListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			game.turnRight();

		} // end actionPerformed

	} // end rtTurnListener class

	
	// Inner class Handler for the right turn event
	class lfTurnListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			game.turnLeft();

		} // end actionPerformed

	} // end rtTurnListener class	

	
	// Inner class Handler for the right turn event
	class fowardListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			game.moveFoward();

		} // end actionPerformed

	} // end moveListener class	
	
	// Inner class Handler for the right turn event
	class backwardListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			game.moveBackward();

		} // end actionPerformed

	} // end moveListener class	

	
	// Inner class Handler for the fire event
	class fireListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			//game.join();
			
			game.fireTorpedo();
		} // end actionPerformed

	} // end fireListener class
	
	// Inner class Handler for the join event
	class joinListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			game.join();

		} // end actionPerformed

	} // end joinListener class	

	
	// Inner class to handle Quit button events
	class QuitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			
			game.stop();

			System.exit(0);
		} // end actionPerformed

	} // end QuitListener class
	
	/**
	 * @author bachmaer
	 *
	 * Inner class for handling keyboard input.
	 */
	class keyboardHandler implements KeyListener  {
	
		// Boolean to make sure "fire" key is released
		// before another response is allowed.
		boolean fireKeyUp = true;
		
		public void keyTyped(KeyEvent e) {	}

	    /** Handle the key-pressed event from the text field. */
	    public void keyReleased(KeyEvent e) {
	    	
	    	if ( e.getKeyCode() == KeyEvent.VK_SPACE) {
	    		
	    		fireKeyUp = true;
	    	}

	    }

		public void keyPressed(KeyEvent e) {
			 
			switch (e.getKeyCode()) {

			case KeyEvent.VK_RIGHT:

				game.turnRight();
				break;

			case KeyEvent.VK_LEFT:

				game.turnLeft();
				break;
				
			case KeyEvent.VK_UP:

				game.moveFoward();
				break;
				
			case KeyEvent.VK_DOWN:

				game.moveBackward();
				break;
				
			case KeyEvent.VK_SPACE:

				if (this.fireKeyUp == true ) {
					
					fireKeyUp = false;
					game.fireTorpedo();			
				}
				break;				

			default:

				System.out.println("pressed key = " + e.getKeyChar());
				break;
			}

		} // end keyPressed

	} // end keyboardHandler inner class

	
} // end SpaceGUI class
