package spaceWar;

import java.net.InetAddress;
import java.net.UnknownHostException;
/**
 * Class to hold constants for the space game.
 * 
 * @author bachmaer
 *
 */
public class Constants 
{
	/**
	 * IP Address of the server
	 */
	public static InetAddress SERVER_IP; 

	
	// Static code block to initialize a static 
	// variable.
	static {
		try {
			SERVER_IP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {

			e.printStackTrace();		
			System.err.println(" Could not determine IP address of the server!");
			System.exit(0);
		} 
	}
	
	
	/**
	 * Port number used by the SERVER for both TCP and UDP
	 * communication.
	 */
	public static final int SERVER_PORT = 32100;
	
	/**
	 *  Default width of the objects
	 */
	public static final int OBJECT_WIDTH = 10;
	
	/**
	 * Move increment for the ships
	 */
	public static final int INCREMENT = OBJECT_WIDTH / 2;
	
	/**
	 *  Available headings for the space ships in the game
	 */
	public static final int NORTH = 1; 
	public static final int NORTH_EAST = 2;
	public static final int EAST = 3; 
	public static final int SOUTH_EAST = 4;	
	public static final int SOUTH = 5; 
	public static final int SOUTH_WEST = 6;
	public static final int WEST = 7;
	public static final int NORTH_WEST = 8;

	/**
	 * Maximum horizontal dimension to be used in the game. X value of
	 * 0 is at the left side of the sector display. MAX_SECTOR_X is the
	 * far right.
	 */
	public static final int MAX_SECTOR_X = 50 * OBJECT_WIDTH; 
	
	/**
	 * Maximum vertical dimension to be used in the game. Y value of
	 * 0 is at the top of the sector display. MAX_SECTOR_Y is the
	 * bottom.
	 */
	public static final int MAX_SECTOR_Y = 50 * OBJECT_WIDTH;
	
	/**
	 *  Total number of obstacles to be displayed in a sector.
	 */
	public static final int NUMBER_OF_OBSTACLES = 15;
	
	// Codes used for game play
	
	/**
	 * Code sent to server when a client first contacts the
	 * server in order to register and receive obstacles.
	 */
	public static final int REGISTER = -1;
		
	/**
	 * Code sent to server when by a client is entering a
	 * space ship into the game.
	 */
	public static final int JOIN = 0;
	
	/**
	 * Code indicating and position or heading of a 
	 * spacecraft is be updated.
	 */
	public static final int UPDATE_SHIP = 1;
	
	/**
	 * Code indicating a spacecraft is be removed from play.
	 */
	public static final int REMOVE_SHIP = 2;
	
	/**
	 *  Code sent to server when by a client to indicate
	 *  they have fired a torpedo.
	 */
	public static final int FIRED_TORPEDO = 3;
	
	/**
	 * Code indicating and position of a 
	 * torpedo is be updated.
	 */
	public static final int UPDATE_TORPEDO = 4;
	
	/**
	 * Code indicating a torpedo is be removed from play.
	 */
	public static final int REMOVE_TORPEDO = 5;
	
	/**
	 * Code sent to server when a client is leaving the game
	 * and will no longer be participating.
	 */
	public static final int EXIT = 6;
	
} // end Constants Class
