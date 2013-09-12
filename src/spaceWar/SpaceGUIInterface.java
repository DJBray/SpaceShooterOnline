package spaceWar;


/**
 * Interface to implement in order respond to events from the 
 * SpaceGameGUI.
 * 
 * @author bachmaer
 */
public interface SpaceGUIInterface {

	/**
	 * Handles callbacks for sector.ownship turning right.
	 */
	public void turnRight();

	/**
	 * Handles callbacks for sector.ownship turning left.
	 */
	public void turnLeft();
	
	/**
	 * Handles callbacks for sector.ownship moving forward on square. 
	 */
	public void moveFoward();
	
	/**
	 * Handles callbacks for sector.ownship moving backward one square. 
	 */
	public void moveBackward();

	
	/**
	 * Creates a new torpedo. 
	 *
	 */
	public void fireTorpedo();

	/**
	 * Creates a new sector.ownShip if one does not exist. Sends a join message 
	 * for the new ship.
	 *
	 */
	public void join();

	/**
	 * Performs clean up when the GUI is closed.
	 */
	public void stop();
	
}
