import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import spaceWar.Constants;
import spaceWar.Obstacle;
import spaceWar.SpaceCraft;
import spaceWar.Torpedo;

/**
 * @author bachmaer
 * 
 * Multi-threaded TCP server to listen for new clients sending information reliably 
 * 
 * It takes care of the following events:
 * 1. Clients coming into the game
 * 2. Clients firing torpedoes
 * 3. Clients leaving the game
 */
class ReliableServer extends Thread {

	ServerSocket gameServerSocket = null;

	// Reference to the SpaceGameServer object that instantiated an object 
	// of this class. Methods of that class are called through this reference.
	SpaceGameServer spaceGameServer = null;
	
	int port = -1;
	int code = -1;
	
	// Setting to false cases all message forwarding
	// and game state updating to end.
	protected boolean playing = true;
	
	
	public ReliableServer( SpaceGameServer spaceGameServer ){
		
		this.spaceGameServer = spaceGameServer;
		
		try {

			// Uses the same port number as the UDP socket that is used
			// to best effort send game information
			gameServerSocket = new ServerSocket(Constants.SERVER_PORT);
			
		} catch (IOException e) {
			System.err.println("Error creating server socket used to listing for joining clients.");
			System.exit(0);
		}
		
		start();
		
	} // end JoinServer
	
	
	/**
	 * Causes all threads and timer tasks to cease execution and closes all
	 * sockets.
	 */
	public void close ()
	{		
		playing = false;

	} // end close 
	
	/**
	 * Listens for join and exiting clients using TCP. Joining clients are sent
	 * the x and y coordinates of all obstacles followed by a negative number.
	 */
	public void run(){
		
		while( playing ){ // loop till playing is set to false
			
			try {
				new HandleClientEvent( gameServerSocket.accept() );

				// Be nice to the other threads
				yield();
				
			} catch (IOException e) {

				System.err.print("Error comminicating with a client.");

			} 

		} // end while
		
		try {
			this.gameServerSocket.close();
		} catch (IOException e) {
			
			
		}
		
	} // end run
	
	
	// Inner class to handle client generated events. An object of this
	// class is instantiated each time a client generates an event that
	// must be reliable communicated to the other clients.
	class HandleClientEvent extends Thread
	{	
		// Reliable connection with the client
		Socket clientConnection = null;
		
		// Streams used to communicate with the client
		DataInputStream dis = null;
		DataOutputStream dos = null;
		
		// Identification information for the particular client
		InetSocketAddress clientID;
		
		HandleClientEvent(Socket clientConnection )
		{
			this.clientConnection = clientConnection;
			
			start();
			
		} // end handleEventClient constructor
		

		// Handles a client requests and then ends
		public void run(){
		
			try {
			
				// Make streams to communicate with the client
				dis = new DataInputStream( clientConnection.getInputStream() );
				dos = new DataOutputStream( clientConnection.getOutputStream() );
				
				// Determine if a client is registering, firing, or exiting
				code = dis.readInt();
				
				// Read Port number
				port =  dis.readInt();
				
				// Get the clients IP address
				clientID = new InetSocketAddress( clientConnection.getInetAddress(), port);
				
				// Handle the client's request
				if (code ==  Constants.REGISTER){
				
					handleNewClient();
				}
				else if ( code == Constants.FIRED_TORPEDO) {
	
					handleTorpedoLaunch();
				}
				else if ( code ==  Constants.EXIT ) {
					
					handleExitingClient();
				}

			} catch (IOException e) {
				
				System.err.println("Error communicating with client.");
				
			}

		} // end run
		
		
		/**
		 * Takes care of clients that are first coming into the game.
		 * It sends all the obstacles to the client. Saves the UDP 
		 * socket address for the clients and save a TCP socket 
		 * connection to the client.
		 * @throws IOException
		 */
		protected void handleNewClient() throws IOException
		{
			// Add the player to the database
			System.out.println("New Client; " + clientID );
			spaceGameServer.clientDatagramSocketAddresses.add( clientID  );
			
			// Retrieve a list of the obstacles in the sector
			ArrayList<Obstacle> obstacles =  spaceGameServer.sector.getObstacles();
			
			// Send to coordinates of the obstacles
			for ( Obstacle obs : obstacles) {
				
				dos.writeInt( obs.getXPosition() );
				dos.writeInt( obs.getYPosition() ); 
			}

			// Signal that their are no more obstacle coordinates to send
			dos.writeInt( -1);		

			// Leave the connection open and save it for sending removal messages
			spaceGameServer.playerTCPConncetions.add(new ClientSocketConnection(clientConnection, dos));
			
		} // end handleNewClient
		
		
		/**
		 * Receives information from a client about a torpedo that
		 * has been launched and sets up the torpedo for automatic 
		 * updating.
		 * @throws IOException
		 */
		protected void handleTorpedoLaunch() throws IOException
		{
			System.out.println("client is firing torpedo");
			// Get the torpedo position and heading
			int x = dis.readInt();
			int y = dis.readInt();
			int heading = dis.readInt();
			
			// The torpedo to the sector so that it can be automatically
			// updated by the timer task.
			Torpedo torpedo = new Torpedo( clientID, x, y, heading );
			spaceGameServer.sector.updateOrAddTorpedo(torpedo);
			
			// Close off the connection. The request has been completed
			dis.close();
			dos.close();
			clientConnection.close();	
				
		} // end handleTorpedoLaunch
		
		
		/**
		 * Takes care of a client that is leaving the game. It 
		 * removes the UDP socket address from the list of
		 * client socket address. Note: In order to simplify 
		 * things, it does not attempt to remove TCP 
		 * information about the client from the server 
		 * database. 
		 * @throws IOException
		 */
		protected void handleExitingClient() throws IOException
		{
			System.out.println("Departing Client; " + clientID );
			
			// Remove the player from the database
			spaceGameServer.clientDatagramSocketAddresses.remove( clientID );
			
			// Temp spacecraft object for updating the sector display
			SpaceCraft sc = new SpaceCraft(clientID);
			
			// Remove the client from the server sector display
			spaceGameServer.sector.removeSpaceCraft( sc );
			
			// Tell all the other clients to remove the ship
			spaceGameServer.sendRemove( sc ); 
			
			// Close off the connection. The request has been completed
			dis.close();
			dos.close();
			clientConnection.close();
			
		} // end handleExitingClient
		
	} // end HandleClientEvent class
	
		
} // end ReliableServer class