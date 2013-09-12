package spaceWar;
import java.awt.Color;
import java.awt.Graphics;
import java.net.InetSocketAddress;


/**

 * Maintains state of the users own SpaceCraft.
 * 
 * @author bachmaer
 */
public class OwnSpaceCraft extends SpaceCraft 
{

	/**
	 * Creates OwnSpaceCraft with random position and heading.
	 *
	 * @param owner IP address and port number to be used to identify  
	 * the ship.
	 */
	public OwnSpaceCraft( InetSocketAddress owner )
	{
		super( owner );

	} // end OwnSpaceCraft constructor

	
	
	/**
	 * Draws the SpaceCraft in white as a arrow point oriented in the
	 * heading direction.
	 */
	public void draw( Graphics g )
	{
		int halfWidth = Constants.OBJECT_WIDTH / 2;
		
		// Draw the spacecraft with the appropriate heading	
		g.setColor( Color.WHITE );
		
		int cosineObjectWidth = (int)(Math.cos(Math.PI/4) * Constants.OBJECT_WIDTH);
		int cosineHalfWidth = (int)(Math.cos(Math.PI/4) * Constants.OBJECT_WIDTH / 2.0);

		switch ( heading ) {
			
			case Constants.NORTH:
			
				g.drawLine(xPosition, yPosition - halfWidth, xPosition - halfWidth, 
						yPosition + halfWidth);
				g.drawLine(xPosition, yPosition - halfWidth, xPosition + halfWidth, 
						yPosition + halfWidth);
			
				break;
				
			case Constants.NORTH_EAST:
				
				g.drawLine(xPosition + cosineObjectWidth, yPosition - cosineObjectWidth, 
						   xPosition - cosineHalfWidth , yPosition - cosineHalfWidth);
				
				g.drawLine(xPosition + cosineObjectWidth, yPosition - cosineObjectWidth, 
						   xPosition + cosineHalfWidth , yPosition + cosineHalfWidth);				
				break;
				
			case Constants.EAST:
			
				g.drawLine(xPosition + halfWidth, yPosition, xPosition - halfWidth, 
						yPosition + halfWidth);
				g.drawLine(xPosition + halfWidth, yPosition, xPosition - halfWidth, 
						yPosition - halfWidth);
			
				break;
				
			case Constants.SOUTH_EAST:
				
				g.drawLine(xPosition + cosineObjectWidth, yPosition + cosineObjectWidth, 
						   xPosition + cosineHalfWidth , yPosition - cosineHalfWidth);
				
				g.drawLine(xPosition + cosineObjectWidth, yPosition + cosineObjectWidth, 
						   xPosition - cosineHalfWidth , yPosition + cosineHalfWidth);	
				
				break;
				
			case Constants.SOUTH:
			
				g.drawLine(xPosition, yPosition + halfWidth, xPosition - halfWidth, 
						yPosition - halfWidth);
				g.drawLine(xPosition, yPosition + halfWidth, xPosition + halfWidth, 
						yPosition - halfWidth);
			
				break;

			case Constants.SOUTH_WEST:
				
				g.drawLine(xPosition - cosineObjectWidth, yPosition + cosineObjectWidth, 
						   xPosition - cosineHalfWidth , yPosition - cosineHalfWidth);
				
				g.drawLine(xPosition - cosineObjectWidth, yPosition + cosineObjectWidth, 
						   xPosition + cosineHalfWidth , yPosition + cosineHalfWidth);	
				
				break;
				
			case Constants.WEST:
			
				g.drawLine(xPosition - halfWidth, yPosition, xPosition + halfWidth, 
						yPosition + halfWidth);
				g.drawLine(xPosition - halfWidth, yPosition, xPosition + halfWidth, 
						yPosition - halfWidth);			
				break;
				
			case Constants.NORTH_WEST:
				
				g.drawLine(xPosition - cosineObjectWidth, yPosition - cosineObjectWidth, 
						   xPosition + cosineHalfWidth , yPosition - cosineHalfWidth);
				
				g.drawLine(xPosition - cosineObjectWidth, yPosition - cosineObjectWidth, 
						   xPosition - cosineHalfWidth , yPosition + cosineHalfWidth);	
				
				break;				
				
			default:
					
					System.err.println("illegal heading");
			break;
		}
	} // end draw


} // end OwnSpaceCraft class
