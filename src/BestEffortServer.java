import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import spaceWar.Constants;
import spaceWar.SpaceCraft;

/**
 *  Class to receive and forward UDP packets containing
 *  updates from clients. In addition, it checks for 
 *  collisions caused by client movements and sends
 *  appropriate removal information
 *  
 * @author bachmaer
 */
class BestEffortServer extends Thread {
	
	// Socket through which all client UDP messages are received
	protected DatagramSocket gamePlaySocket = null;

	// DatagramPacket for receiving updates. All updates are 24 bytes.
	protected DatagramPacket recPack = new DatagramPacket(new byte[24], 24);
	
	// Data members for holding values contained in the fields of
	// received messages
	protected byte ipBytes[] = new byte[4];
	protected int port, code, x, y, heading;
	protected InetSocketAddress id;
	
	// Reference to the SpaceGameServer object that instantiated an object 
	// of this class. Methods of that class are called through this reference.
	SpaceGameServer spaceGameServer = null;

	// Setting to false cases all message forwarding
	// and game state updating to end.
	protected boolean playing = true;
	
	/**
	 * Creates DatagramSocket through which all client update messages
	 * will be received and forwarded.
	 */
	public BestEffortServer( SpaceGameServer spaceGameServer ){
		
		this.spaceGameServer = spaceGameServer;
		
		try {

			// Uses the same port number as the TCP socket that is used
			// to reliably send game information.
			gamePlaySocket = new DatagramSocket( Constants.SERVER_PORT );
			
		} catch (IOException e) {

			System.err.println("Error creating socket to receive and forward messages.");
			System.exit(0);
		}
		
		start();
		
	} // end gamePlayServer
	
	/**
	 * Causes all threads and timer tasks to cease execution and closes all
	 * sockets.
	 */
	public void close ()
	{		
		playing = false;

	} // end close 
	
	
	/**
	 * run method that continuously receives update messages, updates the display, 
	 * and then forwards update messages.
	 */
	public void run() {

		// Receive and forward messages
		while (playing) {
			
			try {
				
				receiveReadAndForwardMessage();
						
				updateDisplay();

				// Be nice to other threads
				yield();

			} catch (IOException e) {
				System.err.println("Error sending remove message.");
			}
		}
		
		gamePlaySocket.close();

	} // end run
	
	
	/**
	 * Receives an update message and extracts the value contained in 
	 * each field of the message. The message is then forwared to all other 
	 * clients.
	 * 
	 * @throws IOException
	 */
	protected void receiveReadAndForwardMessage() throws IOException
	{
		// Receive packet
		gamePlaySocket.receive(recPack);

		// Create streams
		ByteArrayInputStream bais = new ByteArrayInputStream(
				recPack.getData());
		DataInputStream dis = new DataInputStream(bais);

		// Read message fields
		dis.read(ipBytes);
		port = dis.readInt();
		code = dis.readInt();
		x = dis.readInt();
		y = dis.readInt();
		heading = dis.readInt();
		
		// Get id for the client that sent the message
		id = new InetSocketAddress( InetAddress.getByAddress(ipBytes), port);
		
		// Forward to all clients except the one that sent it.
		spaceGameServer.selectiveForward( recPack, id, gamePlaySocket);

	} // end receiveReadAndForwardMessage
	
	
	/**
	 * Updates the sector display and check for collisions. If it determines
	 * a collision has occurred. it sends remove messages to all clients.
	 */
	protected void updateDisplay()
	{
		if (code == Constants.JOIN || code == Constants.UPDATE_SHIP ) {
			
			// Create a temp spacecraft for adding to the sector display 
			// or for updating.
			SpaceCraft ship = new SpaceCraft( id, x, y, heading );
			
			// Update the sector display
			spaceGameServer.sector.updateOrAddSpaceCraft( ship );
			
			// Check to see if any collisions have occurred
			ArrayList<SpaceCraft> destroyed = spaceGameServer.sector.collisionCheck( ship );
			
			// Send remove information if something was destroyed in a 
			// collision.
			if (destroyed != null ) {
				
				for ( SpaceCraft sc: destroyed) {
					spaceGameServer.sendRemove( sc );
				}
			}
		}
		else {
			System.out.println("Unknown UDP message received. Code: " + code);
		}
		
	} // end updateDisplay
	
	
} // end gamePlayServer class
