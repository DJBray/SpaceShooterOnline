package spaceWar;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.ListIterator;
import java.util.Random;
import java.util.Vector;

/**
 *
 * Double buffered drawing canvas for displaying and storing space game state. Maintains
 * and renders all ships in the sector based on type. Maintains and renders all obstacles 
 * and all torpedoes. Provides method for updating all torpedos. Provides accessor and mutator
 * methods for adding and removing spacecraft and torpedoes. 
 * 
 *  @author bachmaer
 */
public class Sector extends Canvas implements Runnable {

	/** Reference to the users own ship. Typically used by client.
	 * 	Server does not have an OwnSpaceCraft
	 */
	public OwnSpaceCraft ownShip = null;
		
	private static final long serialVersionUID = 1L;
	
	// Frames per second for animation
	protected static final int DEFAULT_FPS = 20;
	
	// delay between frames in milliseconds
	protected int delay;

	// Holds non own ship SpaceCraft in the sector
	protected Vector<SpaceCraft> inSector = new Vector<SpaceCraft>();

	// Holds torpedoes currently in the sector
	protected Vector<Torpedo> torpedoes = new Vector<Torpedo>();	
	
	// Holds obstacles in the sector
	protected ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
	
	// IP address and port number that uniquely identifies 
	// a client to the server and other clients
	protected  InetSocketAddress ownShipID;

	// Back buffer for rendering
	protected Image offImage = null;
	protected Graphics offGraphics = null;
	protected Dimension offDimension = null;

	// Rendering thread
	protected Thread animatorThread;

	// Random number generator for making star positions
	protected static Random rand = new Random();

	// Array of 2D points to use for displaying stars
	protected Point[] starPositions = new Point[250];

	/**
	 * Initializes the drawing canvas. Sets the size, frame rate,
	 * and background color. Creates event handlers and starts the
	 * animation thread and torpedo update threads.
	 * 
	 * @param owner IP address and port number that identifies for "own ship"
	 */
	public Sector( InetSocketAddress owner ) {
		
		this();
		
		this.ownShipID 
			= new InetSocketAddress( owner.getAddress(),
									 owner.getPort());
	} // end Sector constructor
	
	/**
	 * Initializes the drawing canvas. Sets the size, frame rate,
	 * and background color. Creates event handlers and starts the
	 * animation thread and torpedo update thread. Display does 
	 * not include an icon for ownship. Frame rate is set to default.
	 */
	public Sector() {
		
		this.setSize(Constants.MAX_SECTOR_X, Constants.MAX_SECTOR_Y);

		this.addComponentListener(new sizeChangeListener());

		// Set the background color for the screen.
		this.setBackground(Color.black);
		
		// Create stars in random positions
		createStars();
		
		// Set the delay between frames
		delay = 1000 / DEFAULT_FPS;
		
		// Start the animation thread
		animatorThread = new Thread(this);
		animatorThread.start();

	} // end Sector constructor
	
	
	/**
	 * Accessor method for the obstacles contained in the sector.
	 * 
	 * @return list of the obstacles
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Obstacle> getObstacles( ) {
		
		return  (ArrayList<Obstacle>)obstacles.clone();
		
	} // end getObstacles
	
	/**
	 * Accessor method for the torpedoes contained in the sector.
	 * 
	 * @return list of the obstacles
	 */
	@SuppressWarnings("unchecked")
	public Vector<Torpedo> getTorpedoes( ) {
		
		return  (Vector<Torpedo>) torpedoes.clone();
		
	} // end getTorpedoes
	
	/**
	 * Checks for a collision between the mover spacecraft and 
	 * all other ships in the sector. It returns a list of ships 
	 * destroyed in a collision if one occurred. If the collision 
	 * between two ships was head-on both the mover and 
	 * the ship it hit are destroyed. Destroyed ships are 
	 * removed from the display. 
	 * 
	 * @param mover ship to check against all other ships
	 * 
	 * @return ships destroyed if a collision occurred. 
	 * null otherwise.
	 */
	public synchronized ArrayList<SpaceCraft> collisionCheck( SpaceCraft mover ) {
		
		// List of ships in collision
		ArrayList<SpaceCraft>  destroyedShips = null;
		
		// Go through all the ships in the sector and compare
		// each against mover
		for( SpaceCraft target: inSector ) {

			// Have mover and the target ship collided
			if ( !target.equals(mover) && inCollision( target, mover)) {

				// A collision has occurred. Create a list to hold the destroyed ships
				destroyedShips = new ArrayList<SpaceCraft>();
				
				// Check for head-on collision.
				if ( opposingHeading( mover , target) ) {

					// Remove move and add to destroyed ship list
					removeSpaceCraft( mover );
					destroyedShips.add( mover );
				}
				
				// Add the ship that was hit to the destroyed ship list
				removeSpaceCraft( target );
				destroyedShips.add( target );
				
				// Return the ship collided with 
				return destroyedShips;	
			}
			
		} // end for

		// no  collision occurred
		return null;

	} // end collisionCheck

	
	/**
	 * Updates the positions of all the torpedoes. If a torpedo
	 * hits an obstacle, it is destroyed. If a torpedo hits a
	 * spacecraft, both the ship and the torpedo are destroyed. 
	 * Destroyed ships and torpedoes are removed from the display. 
	 * 
	 * @return destroyed ships and torpedoes. 
	 * null otherwise.
	 */
	public synchronized ArrayList<SpaceCraft> updateTorpedoes(  ) {
		
		// List of ships and torpedoes in collision
		ArrayList<SpaceCraft>  destroyedObjects = null;
		
		try {
				
			// Update each torpedo and check for collision with an obstacle
			for ( Torpedo mover: this.torpedoes ){
				
				// Update torpedo position. Check for end of life
				boolean endOfLife = mover.update();

				// Remove torpedo if it hit an obstacle or its life is over
				if ( !obstacleClear( mover, true ) || false ==  endOfLife) {
					
					// Create destroyed list if it has not been created yet
					if ( destroyedObjects== null ) {
						destroyedObjects = new ArrayList<SpaceCraft>();
					}
	
					// Remove torpedo from display add to destroyed
					removeTorpedo( mover );		
					destroyedObjects.add( mover );
				}
			} // end for
			
			// Check each remaining torpedo against all ships in the sector.
			for ( Torpedo mover: this.torpedoes ){
			
				// Go through all the ships in the sector
				for( SpaceCraft target: inSector ) {
		
					// Topedoes cannot kill the shipd that fires them
					if( !mover.ID.equals(target.ID)) {
					
						// Have mover and the target ship collided
						if ( inCollision( target, mover) ) {
							
								if ( destroyedObjects== null ) {
									destroyedObjects = new ArrayList<SpaceCraft>();
								}
		
								// Remove torpedo and ship from display
								removeTorpedo( mover );
								removeSpaceCraft( target );
								
								// Add ship and torpedo  to destroyed
								destroyedObjects.add( mover );
								destroyedObjects.add( target );
							}
						}
				} // end for
			} // end for

		} 
		catch( ConcurrentModificationException e ){
			//System.out.println("ConcurrentModificationException my");
		}
		
		return destroyedObjects;

	} // end updateTorpedoes
	

	
	
	/**
	 * Adds a new torpedo to the sector display. If the
	 * torpedo is already in the display, it is updated
	 * 
	 * @param id Socket address of the torpedo
	 * @param x torpedo x position
	 * @param y torpedo y position
	 * @param heading torpedo heading
	 */
	public synchronized void updateOrAddTorpedo( InetSocketAddress id, int x, int y, int heading ) {
		
		updateOrAddTorpedo( new Torpedo( id, x, y, heading ) );
	 
	} // end updateOrAddTorpedo

	
	/**
	 * Adds a new torpedo to the sector display. If the
	 * torpedo is already in the display, it is updated
	 * 
	 * @param torp torpedo to be added or updated
	 */
	public synchronized void updateOrAddTorpedo(Torpedo torp) {
		
		Torpedo target;
		
		ListIterator<Torpedo> iter = torpedoes.listIterator();

		// See if the torpedo is already in the display
		while (iter.hasNext()) {

			target = (Torpedo) iter.next();

			if (torp.equals(target)) {

				// Update an existing torpedo
				target.setHeading(torp.heading);
				target.setX(torp.xPosition);
				target.setY(torp.yPosition);

				return;
			}
		}

		// Add the torpedo. It was not already in the sector
		torpedoes.add( torp );

		return;

	} // end updateOrAddTorpedo
	
	
	/**
	 * If the IDed SpaceCraft is already in the sector 
	 * its heading and/or position are updated. If the SpaceCraft 
	 * is not found in the sector, it is added.
	 * 
	 * @param id Socket address of the spacecraft
	 * @param x spacecraft x position
	 * @param y spacecraft y position
	 * @param heading spacecraft heading
	 */
	public synchronized void updateOrAddSpaceCraft( InetSocketAddress id, int x, int y, int heading ) {
	
		updateOrAddSpaceCraft( new SpaceCraft( id, x, y, heading ) );
	
	} // end updateOrAddSpaceCraft
	
	/**
	 * If the input parameter SpaceCraft is already in the sector 
	 * its heading and/or position are updated. If the SpaceCraft 
	 * is not found in the sector, it is added.
	 * 
	 * @param craft to be updated or added
	 */
	public synchronized void updateOrAddSpaceCraft(SpaceCraft craft) {
		SpaceCraft target;
	
		ListIterator<SpaceCraft> iter = inSector.listIterator();

		while (iter.hasNext()) {

			target = (SpaceCraft) iter.next();

			if (craft.equals(target)) {

				target.setHeading(craft.heading);
				target.setX(craft.xPosition);
				target.setY(craft.yPosition);

				return;
			}
		}

		// Add the ship. It was not already in the sector
		inSector.add(craft);

		return;

	} // end updateOrAddSpaceCraft

	
	/**
	 * Remove a SpaceCraft from the sector. The SpaceCraft to 
	 * be removed could be either alien or ownShip.
	 * 
	 * @param id Socket address of the spacecraft
	 * @param x spacecraft x position
	 * @param y spacecraft y position
	 * @param heading spacecraft heading
	 */
	public synchronized void removeSpaceCraft( InetSocketAddress id, int x, int y, int heading ) {
	
		removeSpaceCraft( new SpaceCraft( id, x, y, heading ) );
	
	} // end removeSpaceCraft

	
	/**
	 * Remove a SpaceCraft from the sector. The SpaceCraft to 
	 * be removed could be either alien or ownShip.
	 * 
	 * @param craft SpaceCraft to be removed.
	 */
	public synchronized void removeSpaceCraft(SpaceCraft craft) {
		
		SpaceCraft target;
		ListIterator<SpaceCraft> iter = inSector.listIterator();

		// Check if ownship is being removed
		if (ownShip != null && craft.equals(ownShip)) {

			ownShip = null;
		} else {

			// Check for shipd in list of ships
			while (iter.hasNext()) {

				target = (SpaceCraft) iter.next();

				if (craft.equals(target)) {
					
					// Remove the ship from the display
					inSector.remove(iter.previousIndex());

					return;
				}
			} // end while
		}

	} // end removeSpaceCraft

	
	/**
	 * Remove a torpedo from the sector.
	 * 
	 * @param id Socket address of the torpedo
	 * @param x torpedo x position
	 * @param y torpedo y position
	 * @param heading torpedo heading
	 */
	public synchronized void removeTorpedo( InetSocketAddress id, int x, int y, int heading ) {
	
		removeTorpedo( new Torpedo( id, x, y, heading ) );
	
	} // end removeTorpedo
	
	
	/**
	 * Remove a torpedo from the sector.
	 * 
	 * @param torp Torpedo to be removed.
	 */
	public synchronized void removeTorpedo(Torpedo torp) {
		
		Torpedo target;
		ListIterator<Torpedo> iter = torpedoes.listIterator();

		// Look for the torpedo
		while (iter.hasNext()) {

			target = (Torpedo) iter.next();

			if (torp.equals(target)) {

				torpedoes.remove(iter.previousIndex());

				return;
			}
		}

	} // end remove
	
	/**
	 * Creates a space craft for the user in a random position with a random 
	 * heading.
	 * 
	 * @return reference to the users space craft for updating
	 */
	public OwnSpaceCraft createOwnSpaceCraft() {
	
		if (ownShip == null) {
			ownShip = new OwnSpaceCraft( ownShipID );
		}

		return ownShip;
	} // end createOwnSpaceCraft
	
	
	/**
	 * Creates stars in random positions for the display.
	 *
	 */
	void createStars() {
		
		for (int i = 0; i < starPositions.length; i++) {

			starPositions[i] = new Point();

			starPositions[i].x = rand.nextInt(Constants.MAX_SECTOR_X);
			starPositions[i].y = rand.nextInt(Constants.MAX_SECTOR_Y);
		}

	} // end createStars

	/**
	 * Add a single obstacle to those in the sector. Assumes that is will only be
	 * called once per obstacle. Does not check for duplicates.
	 * 
	 * @param obs obstacle to be added to the scene
	 */
	public void addObstacle(Obstacle obs) {
		
		obstacles.add(obs);

	} // end addObstacle
	
	
	/**
	 * Add a single obstacle to those in the sector. Assumes that is will only be
	 * called once per obstacle. Does not check for duplicates.
	 * 
	 * @param x x position of the obstacle
	 * @param y y position of the obstacle
	 */
	public void addObstacle(int x, int y) {
		
		addObstacle( new Obstacle(x, y));

	} // end addObstacle
	
	
	/**
	 * Check to see if an obstacle is in front of ownShip.
	 * 
	 * @return true if the position is clear, false otherwise
	 */
	public boolean clearInfront() {
		
		return obstacleClear( ownShip, true );
		
	}
	
	/**
	 * Check to see if an obstacle is in front of ownShip.
	 * 
	 * @return true if the position is clear, false otherwise
	 */
	public boolean clearBehind() {
		
		return obstacleClear( ownShip, false );
		
	}
	
	/**
	 * Check to see if an obstacle is in front of an object of the SpaceCraft class.
	 * 
	 * @param craft SpaceCraft or sub-class to check against the obstacles
	 * @param forward true if checking for obstacles in front of craft. false if
	 * checking for obstacles behind.
	 * @return true if the position is clear, false otherwise
	 */
	public boolean obstacleClear(SpaceCraft craft, boolean forward) {
		
		// Temps to hold hypothetical position
		int xObs = craft.getXPosition();
		int yObs = craft.getYPosition();
		
		int direction = (forward)? OwnSpaceCraft.INCREMENT : -OwnSpaceCraft.INCREMENT;

		// Create hypothetical position
		switch (craft.getHeading()) {

		case Constants.NORTH:

			yObs -= direction;

			break;

		case Constants.NORTH_EAST:

			xObs += direction;
			yObs -= direction;

			break;			
			
		case Constants.EAST:

			xObs += direction;

			break;
	
		case Constants.SOUTH_EAST:

			xObs += direction;
			yObs += direction;
			break;			

		case Constants.SOUTH:

			yObs += direction;

			break;
		case Constants.SOUTH_WEST:

			xObs -= direction;
			yObs += direction;

			break;

		case Constants.WEST:

			xObs -= direction;

			break;

		case Constants.NORTH_WEST:

			xObs -= direction;
			yObs -= direction;
			
			break;			
			
		default:

			System.err.println("Case error in obstacleClear");

		}

		// Check hypothetical position against all obstacles
		ListIterator<Obstacle> iter = obstacles.listIterator();

		while (iter.hasNext()) {

			Obstacle obs = (Obstacle) iter.next();

			if (Math.abs(obs.getXPosition() - xObs) < 10
					&& Math.abs(obs.getYPosition() - yObs) < 10) {

				// "Obstacle in path";
				return false;
			}
		}

		// "Path clear of obstacles"
		return true;

	} // end obstacleClear

	
	/** 
	 * Animation thread. Sleeps in between frames according 
	 * to the frame rate.
	 */
	public void run() {

		//Just to be nice, lower this thread's priority
		//so it can't interfere with other processing going on.
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

		//Remember the starting time.
		long startTime = System.currentTimeMillis();

		//Remember which thread we are.
		Thread currentThread = Thread.currentThread();

		//This is the animation loop.
		while (currentThread == animatorThread) {

			// Indirectly call paint
			repaint();

			//Delay depending on how far we are behind.
			try {
				startTime += delay;
				Thread.sleep(Math
						.max(0, startTime - System.currentTimeMillis()));
			} catch (InterruptedException e) {
				break;
			}
		}
	} // end run

	/** 
	 * Override of the paint method for double buffering.
	 */
	public void paint(Graphics g) {
		update(g);

	} // end paint

	/**
	 * Renders all objects in the sector to the back buffer and 
	 * then swaps buffers.
	 */
	public void update(Graphics g) {
		
		// Necessary to run on a MAC since resize is not called on
		// startup
		if (offGraphics == null){

			offImage = createImage( Constants.MAX_SECTOR_X, Constants.MAX_SECTOR_Y);
			offGraphics = offImage.getGraphics();
			offGraphics.setColor(getBackground());
			offDimension = new Dimension( Constants.MAX_SECTOR_X, Constants.MAX_SECTOR_Y); 
		}
		
		if (this.getBackground() != null) {
			offGraphics.setColor(getBackground());
		}
		
		offGraphics.fillRect(0, 0, offDimension.width, offDimension.height);

		// Call the draw methods for all objects in the sector
		drawStars(offGraphics);
		drawObstacles(offGraphics);
		drawTorpedoes(offGraphics);
		drawAllSpaceCraft(offGraphics);

		// Swap buffers
		g.drawImage(offImage, 0, 0, this);

	} // end update
	
	
	/*
	 * Checks for collision between two SpaceCraft or sub-types.
	 * 
	 * @param first ship for collision check
	 * @param seconds ship for collision check
	 * @return true if the ships are in collision
	 */
	protected boolean inCollision(SpaceCraft sp1, SpaceCraft sp2)
	{
		double diffX = sp1.getXPosition() - sp2.getXPosition();
		double diffY = sp1.getYPosition() - sp2.getYPosition();
		
		if (Math.sqrt( diffX * diffX + diffY * diffY) <= Constants.OBJECT_WIDTH) {

			return true;
		}
		else {
			return false;
		}
		
	} // end inCollision
	
	
	/*
	 * Checks if two SpaceCraft or sub-types have opposing headings
	 * 
	 * @param first ship 
	 * @param seconds ship 
	 * @returns true if the ships face each other
	 */
	protected boolean opposingHeading( SpaceCraft sp1, SpaceCraft sp2)
	{	
		if (Math.abs(sp1.getHeading() - sp2.getHeading()) == 4) {


			return true;
		}
		else {
			return false;
		}
		
	} // end opposingHeading

	/**
	 * Draws all alien space craft in the sector. Draws own ship if it is present.
	 * 
	 * @param g Graphics context for rendering
	 */
	protected synchronized void drawAllSpaceCraft(Graphics g) {
	
		ListIterator<SpaceCraft> iter = inSector.listIterator();

		while (iter.hasNext()) {

			((SpaceCraft) (iter.next())).draw(offGraphics);

		}

		if (this.ownShip != null) {

			ownShip.draw(g);

		}

	} // end drawAllSpaceCraft

	
	/** 
	 * Renders all the stars in the sector.
	 * 
	 * @param g Graphics context for rendering
	 */
	protected void drawStars(Graphics g) {
	
		g.setColor(Color.WHITE);

		for (int i = 0; i < starPositions.length; i++) {

			g.fillOval(starPositions[i].x, starPositions[i].y, 2, 2);
		}
	} // end drawStars
	
	
	/**
	 * Draws all obstacles in the sector. 
	 * 
	 * @param g Graphics context for rendering
	 */
	protected void drawObstacles(Graphics g) {
	
		ListIterator<Obstacle> iter = obstacles.listIterator();

		while (iter.hasNext()) {

			((Obstacle) (iter.next())).draw(offGraphics);
		}

	} // end drawObstacles
	
	
	/**
	 * Draws all torpedoes in the sector. 
	 * 
	 * @param g Graphics context for rendering
	 */
	protected synchronized void drawTorpedoes(Graphics g) {
	
		ListIterator<Torpedo> iter = torpedoes.listIterator();

		while (iter.hasNext()) {

			((Torpedo) (iter.next())).draw(offGraphics);
		}

	} // end drawTorpedoes

	
	/**
	 * @author bachmaer
	 *
	 * Inner class for handling size change events
	 */
	class sizeChangeListener extends ComponentAdapter {
	
		public void componentResized(ComponentEvent e) {

			Dimension newSize = e.getComponent().getSize();

			newSize.width = (newSize.width > 0) ? newSize.width : 1;
			newSize.height = (newSize.height > 0) ? newSize.height : 1;

			if ((offGraphics == null) || (newSize.width != offDimension.width)
					|| (newSize.height != offDimension.height)) {

				offImage = createImage(newSize.width, newSize.height);

				offGraphics = offImage.getGraphics();
				offGraphics.setColor(getBackground());
				offDimension = newSize;
			}
		}
	} // end sizeChangeListener inner class

} // end Sector class

