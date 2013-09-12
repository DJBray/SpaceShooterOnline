package spaceWar;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

/**
 *
 * Super class for all static objects in the space game. Maintains position 
 * of Obstacle objects.
 * 
 * @author bachmaer
 */
public class Obstacle 
{
	
	// Two dimensional position of the Obstacle
	int xPosition, yPosition;
	
	// Random number generator for generating positions
	static Random rand = new Random();


	/**
	 * Default constructor. Creates and obstacle with a randomly generated 
	 * position. 
	 *
	 */
	public Obstacle(  )
	{
		this(rand.nextInt(Constants.MAX_SECTOR_X), 
			  rand.nextInt(Constants.MAX_SECTOR_Y) );

	} // end Obstacle
	
	
	/**
	 * Creates and obstacle with the specified location.
	 * 
	 * @param x horizontal position of the obstacle
	 * @param y vertical position of the obstacle
	 */
	public Obstacle( int x, int y )
	{
		this.setX( x );
		this.setY( y );
		
	} // end Obstacle
	
	
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
	 * Mutator method for the y positon. If input parameter contains an illegal 
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
	 * Default rendering method. Renders the Obstacle as a yellow square.
	 * 
	 */
	public void draw( Graphics g )
	{
		// Draw the spacecraft with the appropriate heading	
		g.setColor( Color.yellow );
		g.fillRect(xPosition - Constants.OBJECT_WIDTH / 2, 
				     yPosition - Constants.OBJECT_WIDTH / 2, 
					  Constants.OBJECT_WIDTH, Constants.OBJECT_WIDTH );

	} // end draw
	
	
	/**
	 * Constructs a string representation of the the obstacle.
	 * 
	 * @return string representation of the SpaceCraft
	 */
	public String toString(){
		
		return " x: " + xPosition + " y: " + yPosition;

	} // end toString
	
	
} // end Obstacle
