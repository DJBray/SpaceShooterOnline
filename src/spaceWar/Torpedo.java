package spaceWar;

import java.awt.Color;
import java.awt.Graphics;
import java.net.InetSocketAddress;

/**
 * Torpedo class. Represents a moving torpedo 
 * 
 * @author bachmaer
 */
public class Torpedo extends SpaceCraft {


	// Maximum Number of times a Torpedo can be updated
	static int MAX_RANGE = Constants.INCREMENT * 25;
	
	// Tracks number of times Torpedo is updated.
	int lifeCounter = 0;
	
	
	/**
	 * Creates an Torpedo with the values of the input parameters
	 * 
	 * @param ID IP address and port associated with the ship that fired the torpedo
	 * @param x position of the torpedo
	 * @param y position of the torpedo
	 * @param heading of the torpedo
	 */
	public Torpedo( InetSocketAddress ID, int x, int y, int heading )
	{
		super( ID, x, y, heading);
		
	} // end AlienCraft constructor
	
	
	/**
	 * Creates Torpedo with random position and heading.
	 *
	 */
	public Torpedo( InetSocketAddress owner )
	{
		super( owner );
		
	} // end OwnSpaceCraft constructor

	
	/**
	 * Updates a torpedo by moving it forward.
	 * 
	 * @return false if the torpedo has reached maximum range or the 
	 * edge of the sector. true otherwise. 
	 */
	public boolean update(  )
	{
		lifeCounter += ( Constants.INCREMENT);
		
		if ( lifeCounter < MAX_RANGE) {
			
			return moveAhead( Constants.INCREMENT );

		}
		else 
		{
			return false;
		}
	} // end update 
	
	
	
	/**
	 * Draws the Torpedo as a blue box.
	 */
	public void draw( Graphics g )
	{
		int quarterWidth = Constants.OBJECT_WIDTH / 2;
		
		// Draw the spacecraft with the appropriate heading	
		g.setColor( Color.BLUE );
		
		g.fillRect(xPosition - Constants.OBJECT_WIDTH / 4, 
			     yPosition - Constants.OBJECT_WIDTH / 4, 
				  Constants.OBJECT_WIDTH / 2, Constants.OBJECT_WIDTH  / 2);

	} // end draw


} // end Torpedo class

