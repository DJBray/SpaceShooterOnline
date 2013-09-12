package spaceWar;

import java.awt.Color;
import java.awt.Graphics;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;


/**
 * Maintains the state information for an Alien SpaceCraft.
 * 
 *  @author bachmaer
 */
public class AlienSpaceCraft extends SpaceCraft 
{
	
	/**
	 * Creates an AlienSpaceCraft with the values of the input parameters
	 * 
	 * @param ID IP address and port associated with the ship
	 * @param x position of the ship
	 * @param y position of the ship
	 * @param heading of the ship
	 */
	public AlienSpaceCraft( InetSocketAddress ID, int x, int y, int heading )
	{
		super( ID, x, y, heading);
		
	} // end AlienCraft constructor
	
	
	
	/**
	 * Renders the alien space craft as a red dot. A small line segment points in
	 * the direction of the heading.
	 * 
	 * @param g Graphical render context
	 */
	public void draw( Graphics g )
	{
		draw( g, Color.RED);
	
	} // end draw
	

	
	/**
	 * Test main for the AlienSpaceCraft class.
	 * 
	 * @param args
	 */
	/*
	public static void main(String[] args)
	{
		AlienSpaceCraft a;
		AlienSpaceCraft b;
		
		try {
			a = new AlienSpaceCraft(InetAddress.getLocalHost(), 10000, 1, 1, 1);
			b = new AlienSpaceCraft(InetAddress.getLocalHost(), 10001, 1, 1, 1);	
			
			if ( a.equals(b) ) System.out.println( "They are equal" );	
			
		} catch (UnknownHostException e) {

			e.printStackTrace();
		}
		
	} // end main
*/

} // end AlienSpaceCraft class
