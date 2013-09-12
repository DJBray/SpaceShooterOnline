package spaceWar;
import java.awt.Color;
import java.awt.Graphics;
import java.net.InetSocketAddress;
import java.util.Random;

/**
 * Super class for all moving objects in the space game. Uniquely identifies each 
 * Spacecraft using a IP address and a port number. Maintains position and 
 * heading state for SpaceCraft objects.
 * 
 * @author bachmaer
 * @author opyrchal
 */
public class SpaceCraft 
{
	/**
	 * IP address and port used to uniquely identify the SpaceCraft
	 */
	public InetSocketAddress ID = null;
	
	// Two dimensional position of the SpaceCraft
	int xPosition, yPosition;
	
	// Current heading of the SpaceCraft
	int heading;

	// Position change increment 
	static final int INCREMENT = Constants.INCREMENT; 
		
	// Random number generator for setting random position with a random heading
	static Random rand	= new Random();

	/**
	 * Creates SpaceCraft with random position and heading.
	 *
	 */
	public SpaceCraft( InetSocketAddress owner )
	{
		this.ID = new InetSocketAddress( 
					owner.getAddress(),
					owner.getPort());
		
		this.heading = rand.nextInt(8) + 1;
		
		this.xPosition = rand.nextInt( Constants.MAX_SECTOR_X );
		
		this.yPosition = rand.nextInt( Constants.MAX_SECTOR_Y );
		
	} // end SpaceCraft constructor
	
	/**
	 * Creates an AlienSpaceCraft with the values of the input parameters.
	 * 
	 * @param ID IP address and port associated with the ship
	 * @param x position of the ship
	 * @param y position of the ship
	 * @param heading of the ship
	 */
	public SpaceCraft( InetSocketAddress ID, int x, int y, int heading )
	{
		this.ID 
		= new InetSocketAddress( ID.getAddress(), 
								 ID.getPort() );
		
		this.setHeading( heading );
	
		this.setX( x );
	
		this.setY( y );
		
	} // end AlienCraft constructor
	
	/**
	 * Accessor method for the heading. Headings are integer
	 * values of 1, 2, 3, 4, 5, 6, 7, or 8. 1 corresponds to
	 * "North" and 8 corresponds to "Northwest."
	 * 
	 * @return current heading
	 */
	public int getHeading()
	{
		return heading;
			 
	} // end getHeading


	/**
	 * Checks to see if this SpaceCraft has the same ID and port number
	 * as the input parameter.
	 * 
	 * @param subject SpaceCraft being checked for equality
	 * 
	 * @return true if the SpaceCraft have same ID and port, false otherwise
	 */
	public boolean equals( SpaceCraft subject)
	{

		return ( this.ID.equals(subject.ID) );
		
	} // end equals
	

	/**
	 * Accessor method for the xPosition.
	 * 
	 * @return current x position
	 */
	public int getXPosition()
	{
		return xPosition;

	} // end getXPosition
	
	
	/**
	 * Accessor method for the yPosition.
	 * 
	 * @return current y position
	 */
	public int getYPosition()
	{
		return yPosition;

	} // end getYPosition
	
	
	/**
	 * Increments the heading 45 degrees to the right.
	 */
	public void rightTurn()
	{
		heading++;
		if (heading > Constants.NORTH_WEST) heading = Constants.NORTH;
		
	} // end rightTurn
	 

	/**
	 * Increments the heading 45 degrees to the left.
	 */	 
	public void leftTurn()
	{
		heading--;
		
		if (heading < Constants.NORTH) heading = Constants.NORTH_WEST;
		
	} // end leftTurn
	
	/**
	 * Move OwnsSpaceCraft ahead one increment in the direction in which it is 
	 * headed. Spacecraft is stopped at the edge of the world.
	 *
	 */
	public boolean moveForward()
	{
		
		return moveAhead(INCREMENT);
		
	} // end move
	
	/**
	 * Move OwnsSpaceCraft ahead one increment in the direction in which it is 
	 * headed. Spacecraft is stopped at the edge of the world.
	 *
	 */
	public boolean  moveBackward()
	{
		
		return moveBack(INCREMENT);
		
	} // end move
	
	

	/**
	 * Move SpaceCraft ahead one inc in the direction in which it is 
	 * headed. Spacecraft is stopped at the edge of the world.
	 *
	 *@param inc number of pixels to move ahead
	 */
	public boolean moveAhead(int inc)
	{		
		int possibleX = xPosition;
		int possibleY = yPosition;
		
		switch (heading) {
		
		case Constants.NORTH:
		
			possibleY = ( possibleY - inc > 0 ) ? possibleY - inc : 0; 
		
		break;
		
		case Constants.NORTH_EAST:
			
			possibleX = ( possibleX + inc < Constants.MAX_SECTOR_X ) 
						  ? possibleX + inc :  Constants.MAX_SECTOR_X;
			possibleY = ( possibleY - inc > 0 ) ? possibleY - inc : 0; 
		
		break;
		
		case Constants.EAST:
		
			possibleX = ( possibleX + inc < Constants.MAX_SECTOR_X ) 
				? possibleX + inc :  Constants.MAX_SECTOR_X;
		
		break;	
		
		case Constants.SOUTH_EAST:
			
			possibleX = ( possibleX + inc < Constants.MAX_SECTOR_X ) 
				? possibleX + inc :  Constants.MAX_SECTOR_X;
			possibleY = ( possibleY + inc < Constants.MAX_SECTOR_Y  ) 
				? possibleY + inc : Constants.MAX_SECTOR_Y; 
		
		break;		
		
		case Constants.SOUTH:
		
			possibleY = ( possibleY + inc < Constants.MAX_SECTOR_Y  ) 
				? possibleY + inc : Constants.MAX_SECTOR_Y; 
		
		break;	
		
		case Constants.SOUTH_WEST:
			
			possibleX = ( possibleX - inc > 0) ? possibleX - inc : 0;
			possibleY = ( possibleY + inc < Constants.MAX_SECTOR_Y  ) 
				? possibleY + inc : Constants.MAX_SECTOR_Y; 
		
		break;	
		
		case Constants.WEST:
		
			possibleX = ( possibleX - inc > 0) ? possibleX - inc : 0;
		
		break;
		
		case Constants.NORTH_WEST:
			
			possibleX = ( possibleX - inc > 0) ? possibleX - inc : 0;
			possibleY = ( possibleY - inc > 0 ) ? possibleY - inc : 0;
		break;
		
		default:
		
			System.err.println( "Illegal Heading");
		break;	
		}
		
		if (possibleX == xPosition && possibleY == yPosition ){
			return false;
		}
		else {
			xPosition = possibleX;
			yPosition = possibleY;
			return true;
		}
			
	} // end moveAhead
	
	/**
	 * Move SpaceCraft ahead one inc in the opposite direction from
	 * the current heading. Spacecraft is stopped at the edge of the world.
	 *
	 *@param inc number of pixels to move back
	 */
	public boolean moveBack(int inc)
	{		
		int possibleX = xPosition;
		int possibleY = yPosition;
		
		switch (heading) {
		
		case Constants.NORTH:
		
			possibleY = ( possibleY + inc < Constants.MAX_SECTOR_Y  ) 
				? possibleY + inc : Constants.MAX_SECTOR_Y; 
		
		break;
		
		case Constants.NORTH_EAST:
			
			possibleX = ( possibleX - inc > 0) ? possibleX - inc : 0;
			possibleY = ( possibleY + inc < Constants.MAX_SECTOR_Y  ) 
				? possibleY + inc : Constants.MAX_SECTOR_Y; 
		
		break;
		
		case Constants.EAST:
		
			possibleX = ( possibleX - inc > 0) ? possibleX - inc : 0;
		
		break;	
		
		case Constants.SOUTH_EAST:
			
			possibleX = ( possibleX - inc > 0) ? possibleX - inc : 0;
			possibleY = ( possibleY - inc > 0 ) ? possibleY - inc : 0;
		
		break;		
		
		case Constants.SOUTH:
		
			possibleY = ( possibleY - inc > 0 ) ? possibleY - inc : 0; 
		
		break;	
		
		case Constants.SOUTH_WEST:
			
			possibleX = ( possibleX + inc < Constants.MAX_SECTOR_X ) 
			  ? possibleX + inc :  Constants.MAX_SECTOR_X;
			possibleY = ( possibleY - inc > 0 ) ? possibleY - inc : 0; 
		
		break;	
		
		case Constants.WEST:
		
			possibleX = ( possibleX + inc < Constants.MAX_SECTOR_X ) 
				? possibleX + inc :  Constants.MAX_SECTOR_X;
			
		break;
		
		case Constants.NORTH_WEST:
			
			possibleX = ( possibleX + inc < Constants.MAX_SECTOR_X ) 
				? possibleX + inc :  Constants.MAX_SECTOR_X;
			possibleY = ( possibleY + inc < Constants.MAX_SECTOR_Y  ) 
				? possibleY + inc : Constants.MAX_SECTOR_Y; 
			
		break;
		
		default:
		
			System.err.println( "Illegal Heading");
		break;	
		}
		
		if (possibleX == xPosition && possibleY == yPosition ){
			return false;
		}
		else {
			xPosition = possibleX;
			yPosition = possibleY;
			return true;
		}
			
	} // end moveBack
	
	
	/**
	 * Mutator method for the heading. If input parameter contains an illegal 
	 * heading it leaves the heading unchanged.
	 * 
	 * @param heading parameter set to 1, 2, 3, 4, 5, 6, 7, or 8
	 */
	public void setHeading( int heading )
	{
		this.heading = 
			(heading > 0 && heading <= Constants.NORTH_WEST )? heading: this.heading;
		
	} // end setHeading
	
	
	/**
	 * Mutator method for the x positon. If input parameter contains an illegal 
	 * position it leaves the position unchanged.
	 * 
	 * @param x new x position
	 */
	public void setX(int x)
	{
		this.xPosition = 
			( x > 0 && x < Constants.MAX_SECTOR_X ) ? x : this.xPosition;
		
	} // end setX
	
	
	/**
	 * Mutator method for the y position. If input parameter contains an illegal 
	 * position it leaves the position unchanged.
	 * 
	 * @param y new y position
	 */
	public void setY(int y)
	{
		this.yPosition = 
			( y > 0 && y < Constants.MAX_SECTOR_Y ) ? y : this.yPosition;
		
	} // end setY	
	
	
	/**
	 * Default rendering method. Renders the SpaceCraft as a green circle.
	 * 
	 * param g Graphical render context
	 */
	public void draw( Graphics g )
	{
		draw( g, Color.GREEN);
	
	} // end draw
	
	
	/**
	 * Renders the space craft using a specified color. A small line segment points in
	 * the direction of the heading.
	 * 
	 * @param g Graphical render context
	 * @param craftColor color in which the spacecraft is to be rendered
	 */
	protected void draw( Graphics g, Color craftColor )
	{
		int halfWidth = Constants.OBJECT_WIDTH / 2;
		
		// Draw the spacecraft with the appropriate heading	
		g.setColor( craftColor );
		
		g.fillOval(xPosition - halfWidth, yPosition - halfWidth, 
				Constants.OBJECT_WIDTH, Constants.OBJECT_WIDTH );
		
		int cosineObjectWidth = (int)(Math.cos(Math.PI/4) * Constants.OBJECT_WIDTH);

		switch ( heading ) {
			
			case Constants.NORTH:
			
				g.drawLine(xPosition, yPosition - halfWidth, xPosition, 
						   yPosition - Constants.OBJECT_WIDTH);				
				break;
			case Constants.NORTH_EAST:
				
				
				
				g.drawLine(xPosition + halfWidth, yPosition - halfWidth, 
						   xPosition + cosineObjectWidth, 
						   yPosition - cosineObjectWidth);				
				break;				
			case Constants.EAST:

				g.drawLine(xPosition + halfWidth, yPosition, 
						xPosition + Constants.OBJECT_WIDTH, yPosition);			
				break;
			case Constants.SOUTH_EAST:
				
				g.drawLine(xPosition + halfWidth, yPosition + halfWidth, 
						   xPosition + cosineObjectWidth, 
						   yPosition + cosineObjectWidth);			
				break;				
			case Constants.SOUTH:
			
				g.drawLine(xPosition, yPosition + halfWidth, xPosition, 
						yPosition + Constants.OBJECT_WIDTH);			
				break;
			case Constants.SOUTH_WEST:
				
				g.drawLine(xPosition - halfWidth, yPosition + halfWidth, 
						   xPosition - cosineObjectWidth, 
						   yPosition + cosineObjectWidth);					
				break;				
			case Constants.WEST:
			
				g.drawLine(xPosition - halfWidth, yPosition, 
						xPosition - Constants.OBJECT_WIDTH, yPosition);
				break;
			case Constants.NORTH_WEST:
				
				g.drawLine(xPosition - halfWidth, yPosition - halfWidth, 
						   xPosition - cosineObjectWidth, 
						   yPosition - cosineObjectWidth);					
				break;				
		}	
	
	} // end draw
	
	


	
	/**
	 * Constructs a string representation of the the craft.
	 * 
	 * @return string representation of the SpaceCraft
	 */
	public String toString(){
	
		return "Address: " + ID + " heading: " 
				+ heading + " x: " + xPosition + " y: " + yPosition;

	} // end toString


} // end SpaceCraft
