import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import spaceWar.Constants;
import spaceWar.SpaceCraft;
import spaceWar.Torpedo;

	/**
	 * Task which periodically updates the torpedoes that are in 
	 * the sector and determines if they have hit anything.
	 * 
	 * @author bachmaer
	 */
	class TorpdedoUpdater extends TimerTask
	{
		/**
		 * Socket through which all torpedo update messages will be sent
		 */
		DatagramSocket dgsock;
		
		// Reference for timer object used to automatically update torpedoes
		protected Timer torpedoTimer;
		
		// Reference to the SpaceGameServer object that instantiated an object 
		// of this class. Methods of that class are called through this reference.
		SpaceGameServer spaceGameServer = null;
		
		// Setting to false cases all torpedo updating to end.
		protected boolean playing = true;
		
		
		/**
		 * Creates a DatagramSocket that is used to send update mesages.
		 */
		public TorpdedoUpdater(SpaceGameServer spaceGameServer ){
			
			this.spaceGameServer = spaceGameServer;
			
			try {
				dgsock = new DatagramSocket();
			} catch (SocketException e) {
				System.err.println("Could not create Datagram Socket for torpedo updater.");
				System.exit(0);
			}
			
			
			// Start the task to update the torpedoes
			torpedoTimer = new Timer();
			
			torpedoTimer.scheduleAtFixedRate( this, 0, 50);
			
			
		} // end TorpdedoUpdater constructor
		
		/**
		 * Causes all threads and timer tasks to cease execution and closes all
		 * sockets.
		 */
		public void close ()
		{		
			playing = false;

		} // end close 
		
		
		/**
		 * run method that will be called periodically by a Timer (sub-class of thread).
		 * It updates all torpedoes in the sector and then sends updates message to
		 * all clients to provide them with the new positions of the torpedoes. Additionally
		 * it sends remove messages for torpedoes and ships. Torpedoes are removed when 
		 * they reach the end of their life or hit a ship. Ships are removed if they
		 * are hit by torpedoes. 
		 */
		public void run() {
				
			// Move all torpedoes and determine if they hit anything 
			ArrayList<SpaceCraft> destroyed = spaceGameServer.sector.updateTorpedoes();
			
			// Send remove messages for any ships of torpedoes 
			// that are no longer in the game.
			if (destroyed != null ) {
				
				for ( SpaceCraft sc: destroyed) {
					
					spaceGameServer.sendRemove( sc );
				}
			}
			
			// Access the torpedoes that are still in the sector
			Vector<Torpedo> remainingTorpedoes = spaceGameServer.sector.getTorpedoes();
			
			// Send update messages for torpedoes that are still
			// in the game
			for ( Torpedo t: remainingTorpedoes) {
				
				sendTorpedoUpdate( t, dgsock );
			}
			
			// Check to see if the game has ended
			if (playing == false ){
				
				// Stop the timer
				this.cancel();
				
				// Close the UDP socket
				dgsock.close();
			}

		} // end run
		
		
		/**
		 * Creates a update message for a torpedo and sends it to all
		 * clients.
		 * 
		 * @param sc torpedo being updated
		 * @param dgSock socket to use to send the message
		 */
		synchronized public void sendTorpedoUpdate( Torpedo sc, DatagramSocket dgSock  ) {
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream( baos );

			try {
				// Write fields of the message
				dos.write( sc.ID.getAddress().getAddress());
				dos.writeInt( sc.ID.getPort());
				dos.writeInt( Constants.UPDATE_TORPEDO );
				dos.writeInt( sc.getXPosition() );
				dos.writeInt( sc.getYPosition() );
				dos.writeInt( sc.getHeading() );
				
			} catch (IOException e) {
				System.err.println("Error sending torpedo update.");
			}

			// Create the packet
			DatagramPacket dpack 
				= new DatagramPacket(baos.toByteArray(), baos.size() );
				
			// Send the packet to every client
			spaceGameServer.allForward( dpack, dgSock );
				
			try {
				dos.close();
			} catch (IOException e) {
				System.err.println("Error closing stream.");
			}

		} // end sendTorpedoUpdate
		
	} // end TorpdedoUpdater class