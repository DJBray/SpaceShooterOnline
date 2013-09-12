import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import spaceWar.*;

/**
 * Helper class to keep track of client TCP Socket connections and
 * an associate output stream. Maintaining a collection of these
 * objects removes the requirement to have a continuous running
 * thread for each client. After a client has registered only
 * remove information will be set using this Socket by the 
 * server. The server will not receive anything.  
 *  
 * @author bachmaer
 *
 */
class ClientSocketConnection
{	
	/**
	 * Connection to a client that is listing for remove information
	 */
	public Socket clientTCPConnection;
	
	/**
	 * Output stream for a connected socket;
	 */
	public DataOutputStream dos;
	
	public ClientSocketConnection(Socket socket, DataOutputStream out)
	{
		// Save the connection and the output stream
		this.clientTCPConnection = socket;
		this.dos = out;
	}
	
} // end ClientSocketConnection


/**
 * @author bachmaer
 *
 * SpaceGameServer. 
 * 
 * Technically this class is not a server. 
 *  
 * Instantiates to several servers to support a multiuser game 
 * in which clients maneuver spacecraft through a shared virtual world and 
 * attempt to destroy each other by either firing torpedoes or ramming.
 * The objects that the server instantiates are the arbiter of all events 
 * resulting in the destruction of a torpedo or a spacecraft. They generate 
 * messages for removal. Removal information is sent reliably via TCP. 
 * Best effort UDP update messages that are received from clients are 
 * forwarded to all other clients.
 *   
 */
public class SpaceGameServer 
{
	// Random number generator for obstacle positions
	Random rand = new Random();

	// Contains all IP addresses and port numbers of the DatagramSockets
	// used by the clients. UDP segments are forwarded using the information
	// in this data member.
	protected ArrayList<InetSocketAddress> clientDatagramSocketAddresses 
 		= new  ArrayList<InetSocketAddress>();
	
	// Contains objects which holds a Socket connection and an associated
	// DataOutputStream for each client. Data pertaining to the removal
	// of torpedoes and ships are sent reliably to clients using this
	// ArrayList.
	protected ArrayList<ClientSocketConnection> playerTCPConncetions 
		= new ArrayList<ClientSocketConnection>();
	
	// Simple gui to display what the server is tracking
	protected ServerGUI display;
	
	// Sector containing all information about the game state
	protected Sector sector;
	
	// Server that reliably handles game information
	ReliableServer reliableServer = null;
	
	// Best effort server for handling game information
	BestEffortServer bestEffortServer = null;
	
	// Periodically updates torpedos within the sector and 
	// sends the information to all clients
	TorpdedoUpdater torpdedoUpdater = null;
		
	/**
	 * Server constructor. Create server objects that will
	 * track and update game information. Each of the server ojbects
	 * will constitutes a separate thread of execution. Create obstacles.
	 * Create and start GUI.
	 */
	public SpaceGameServer() 
	{
		// Create sector to hold all game information
		sector = new Sector();
		
		// Create the GUI that will display the sector
		display = new ServerGUI( sector );
		
		// Create and position the obstacles that will be shared by all
		// the clients in the game
		createObstacles();
	
		// Start the TCP and UDP servers
		reliableServer = new ReliableServer( this );
		bestEffortServer = new BestEffortServer( this );
		
		// Start the server that sends out updates on torpedoes in the sector
		torpdedoUpdater = new TorpdedoUpdater( this );
		
	} // end SpaceGameServer constructor
	
	/**
	 * Causes all threads and timer tasks to cease execution and closes all
	 * sockets.
	 */
	public void close ()
	{		
		// Stop each of the servers
		reliableServer.close();
		bestEffortServer.close();
		torpdedoUpdater.close();

	} // end close 
	
	
	/**
	 * Create a number of obstacles as determined by a value held in 
	 * Constants.NUMBER_OF_OBSTACLES. Obstacles are in random positions
	 * and are shared by all clients.
	 */
	protected void createObstacles() 
	{
		for(int i = 0 ; i < Constants.NUMBER_OF_OBSTACLES ; i++){
			
			sector.addObstacle( new Obstacle( rand.nextInt(Constants.MAX_SECTOR_X), 
											  rand.nextInt(Constants.MAX_SECTOR_Y) ) );	
		}

	} // end createObstacles

	
	/**
	 * Sends remove information for a particular SpaceCraft or Torpedo to all clients.
	 * Each client maintains a TCP Socket with which it only listens for this 
	 * remove information.
	 * 
	 * @param sc ship or torpedo to be removed
	 */
	synchronized protected void sendRemove( SpaceCraft sc ) {
		
		// Go through all the players in the game
		for(int i = 0; i < playerTCPConncetions.size(); i++ ) {

			// Get the Socket and DataOutputStream for a particular
			// player
			ClientSocketConnection isa = playerTCPConncetions.get(i);
			DataOutputStream dos = isa.dos;

			try {
				// Write out identifying information for the 
				// entity to be removed.
				dos.write( sc.ID.getAddress().getAddress());
				dos.writeInt( sc.ID.getPort());
				
				// Indicate whether the entity to be removed
				// is a torpedo or a spacecraft
				if( sc instanceof Torpedo) {
					
					dos.writeInt( Constants.REMOVE_TORPEDO );
				}
				else {
					
					dos.writeInt( Constants.REMOVE_SHIP );
				}
	
			} catch (IOException e) {
				
				// The Socket connection has been lost. Most likely the 
				// client has left the game. Remove the socket from the
				// list of connections. Yes, this is sloppy. :(
				playerTCPConncetions.remove(isa);
			}
		}

	} // end sendRemove

	
	/**
	 * Sends a datagram packet to all clients except one as 
	 * specified by an input argument.
	 * 
	 * @param fwdPack packet to send
	 * @param notSendTo address to skip
	 * @param dgSock socket to use to send the message
	 */
	synchronized public void selectiveForward(DatagramPacket fwdPack, InetSocketAddress notSendTo, DatagramSocket dgSock )
	{
		for(InetSocketAddress isa : clientDatagramSocketAddresses ) {
						
			if( !isa.equals(notSendTo)) {
				
				fwdPack.setSocketAddress( isa );
				try {
					dgSock.send( fwdPack );

				} catch (IOException e) {
					System.err.println("Error performing selective forward.");
				}
			}
		}
	} // end selectiveForward
	
	
	/**
	 * Sends a datagram packet to all clients.
	 * 
	 * @param fwdPack packet to send
	 * @param dgSock socket to use to send the message
	 */
	synchronized public void allForward(DatagramPacket fwdPack, DatagramSocket dgSock  )
	{
		for(InetSocketAddress isa : clientDatagramSocketAddresses ) {
				
			fwdPack.setSocketAddress( isa );
			
			try {
				dgSock.send( fwdPack );
			} catch (IOException e) {
				System.err.println("Error forward message to all clients.");
			}
		}
		
	} // end allForward
	


	/**
	 * Driver for starting the server.
	 * 
	 * @param args
	 */
	public static void main(String[] args) 
	{
		new SpaceGameServer();
		
	} // end main
	
	
} // end SpaceGameServer class

