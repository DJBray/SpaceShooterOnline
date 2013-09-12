import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import spaceWar.Constants;
import spaceWar.Sector;
import spaceWar.SpaceCraft;
import spaceWar.SpaceGUIInterface;
import spaceWar.SpaceGameGUI;
import spaceWar.Torpedo;

/**
 * @author bachmaer
 *
 * Driver class for a simple networked space game. Opponents try to destroy each 
 * other by ramming. Head on collisions destroy both ships. Ships move and turn 
 * through GUI mouse clicks. All friendly and alien ships are displayed on a 2D 
 * interface.  
 */
public class SpaceGameClient implements SpaceGUIInterface
{
	// Keeps track of the game state
	Sector sector;
	
	// User interface
	SpaceGameGUI gui;
	
	// IP address and port to identify ownship and the 
	// DatagramSocket being used for game play messages.
	InetSocketAddress ownShipID;
	
	// Socket for sending and receiving
	// game play messages.
	DatagramSocket gamePlaySocket;

	// Socket used to register and to receive remove information
	// for ships
	Socket reliableSocket;
	
	// Set to false to stops all receiving loops
	boolean playing = true;
	
	DataOutputStream dos;
	DataInputStream dis;
	
	static final boolean DEBUG = false;
	
	/**
	 * Creates all components needed to start a space game. Creates Sector 
	 * canvas, GUI interface, a Sender object for sending update messages, a 
	 * Receiver object for receiving messages.
	 * @throws UnknownHostException 
	 */
	public SpaceGameClient()
	{
		// Create UDP Datagram Socket for sending and receiving
		// game play messages.
		try {
			gamePlaySocket = new DatagramSocket();
			
			
		} catch (SocketException e) {
			System.err.println("Error creating game play datagram socket.");
			System.exit(0);
		} 
		
		// Instantiate ownShipID using the DatagramSocket port
		// and the local IP address.
		try {
			
			ownShipID = new InetSocketAddress(
							InetAddress.getLocalHost(),
							gamePlaySocket.getLocalPort());
		} catch (UnknownHostException e) {
			// Auto-generated catch block
			System.err.println("Error creating ownship ID. Exiting.");
			System.exit(0);
		}
		
		// Create display, ownPort is used to uniquely identify the 
		// controlled entity.
		sector = new Sector( ownShipID );
		
		//	gui will call SpaceGame methods to handle user events
		gui = new SpaceGameGUI( this, sector ); 
		
		// Call a method that uses TCP/IP to register with the server 
		// and receive obstacles from the server. 
		register();
		
		
		// Infinite loop or separate thread to receive update and join
		// messages from the server and use the messages to 
		// update the sector display
		new UpdateJoinThread().start();
		
		// Infinite loop or separate thread to receive remove 
		// messages from the server and use the messages to 
		// update the sector display
		new RemoveThread().start();
		

	} // end SpaceGame constructor
	
	
	/**
	 * sendUpdatedShip
	 * @author Daniel J Bray
	 * 
	 * A helper method used for sending BestEffort messages sent over the
	 * game play socket. Useful so you don't have to remember message protocol
	 * when updating a ship's position. Use the parameter to specify the code
	 * sent to the server such as Constants.JOIN or Constants.UPDATE_SHIP
	 * 
	 * @param param
	 * 			The code being sent in the message.
	 */
	private void sendUpdatedShip(int param){
		int x = sector.ownShip.getXPosition();
		int y = sector.ownShip.getYPosition();
		int heading = sector.ownShip.getHeading();
		
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dp_dos = new DataOutputStream(baos);

			dp_dos.write(sector.ownShip.ID.getAddress().getAddress());
			dp_dos.writeInt(sector.ownShip.ID.getPort());
			dp_dos.writeInt(param);
			dp_dos.writeInt(x);
			dp_dos.writeInt(y);
			dp_dos.writeInt(heading);

			byte[] baosArray = baos.toByteArray();
			DatagramPacket dp = new DatagramPacket(baosArray, baosArray.length);
			dp_dos.close();

			dp.setSocketAddress(new InetSocketAddress(Constants.SERVER_IP, Constants.SERVER_PORT));
			gamePlaySocket.send(dp);
		}
		catch(IOException e){
			System.out.println("Error Updating Ship");
		}
	}
	
	/**
	 * Causes sector.ownShip to turn and sends an update message for the heading 
	 * change.
	 */
	public void turnRight()
	{
		if (sector.ownShip != null) {
			
			if ( DEBUG ) System.out.println( " Right Turn " );
			
			// Update the display			
			sector.ownShip.rightTurn();
			
			// Send update message to server with new heading.
			sendUpdatedShip(Constants.UPDATE_SHIP);
		} 
		
	} // end turnRight


	/**
	 * Causes sector.ownShip to turn and sends an update message for the heading 
	 * change.
	 */
	public void turnLeft()
	{
		// See if the player has a ship in play
		if (sector.ownShip != null) {		
			
			if ( DEBUG ) System.out.println( " Left Turn " );
			
			// Update the display
			sector.ownShip.leftTurn();
			
			// Send update message to other server with new heading.
			sendUpdatedShip(Constants.UPDATE_SHIP);
		}		
		
	} // end turnLeft
	
	/**
	 * Causes sector.ownShip to turn and sends an update message for the heading 
	 * change.
	 */
	public void fireTorpedo()
	{
		// See if the player has a ship in play
		if (sector.ownShip != null) {		
			
			if ( DEBUG ) System.out.println( "Informing server of new torpedo" );
			
			// Make a TCP connection to the server and send torpedo information
			try{
				Socket s = new Socket(Constants.SERVER_IP, Constants.SERVER_PORT);
				DataOutputStream ft = new DataOutputStream(s.getOutputStream());
				
				ft.writeInt(Constants.FIRED_TORPEDO);
				ft.writeInt(ownShipID.getPort());
				ft.writeInt(sector.ownShip.getXPosition());
				ft.writeInt(sector.ownShip.getYPosition());
				ft.writeInt(sector.ownShip.getHeading());
				
				ft.close();
				s.close();
			}
			catch(IOException e){
				System.out.println("Error firing torpedo");
			}
		}		
		
	} // end turnLeft

	
	/**
	 * Causes sector.ownShip to move forward and sends an update message for the 
	 * position change. If there is an obstacle in front of
	 * the ship it will not move forward and a message is not sent. 
	 */
	public void moveFoward()
	{
		// Check if the player has and unblocked ship in the game
		if ( sector.ownShip != null && sector.clearInfront() ) {
			
			if ( DEBUG ) System.out.println( " Move Forward" );
			
			//Update the displayed position of the ship
			sector.ownShip.moveForward();
			
			// Send a message with the updated position to server
			sendUpdatedShip(Constants.UPDATE_SHIP);
		}
								
	} // end moveFoward
	
	
	/**
	 * Causes sector.ownShip to move forward and sends an update message for the 
	 * position change. If there is an obstacle in front of
	 * the ship it will not move forward and a message is not sent. 
	 */
	public void moveBackward()
	{
		// Check if the player has and unblocked ship in the game
		if ( sector.ownShip != null && sector.clearBehind() ) {
			
			if ( DEBUG ) System.out.println( " Move Backward" );
			
			//Update the displayed position of the ship
			sector.ownShip.moveBackward();
			
			// Send a message with the updated position to server
			sendUpdatedShip(Constants.UPDATE_SHIP);
		}
								
	} // end moveFoward

	/**
	 * register
	 * @author Daniel J Bray
	 * 
	 * Establishes a TCP connection with the server and receives all the obstacles
	 * present in the game. Connection is left open to allow for remove messages to
	 * be sent using a reliable connection.
	 */
	public void register(){
		try{			
			reliableSocket = new Socket(Constants.SERVER_IP, Constants.SERVER_PORT);
			dos = new DataOutputStream(reliableSocket.getOutputStream());
			dis = new DataInputStream(reliableSocket.getInputStream());
			
			dos.writeInt(Constants.REGISTER);
			dos.writeInt(ownShipID.getPort());
			
			int x = dis.readInt();
			
			while(x != -1){
				sector.addObstacle(x, dis.readInt());
				x = dis.readInt();
			}
		}
		catch(IOException e){
			System.out.println("Error registering with server. Exiting");
			System.exit(0);
		}
	}
	
	/**
	 * Creates a new sector.ownShip if one does not exist. Sends a join message 
	 * for the new ship.
	 *
	 */
	public void join()
	{
		if (sector.ownShip == null ) {

			if ( DEBUG ) System.out.println( " Join " );
			
			// Add a new ownShip to the sector display
			sector.createOwnSpaceCraft();
			
			// Send message to server let them know you have joined the game using the 
			// send object			
			sendUpdatedShip(Constants.JOIN);
		}
		
	} // end join

	
	/**
	 *  Perform clean-up for application shut down
	 */
	public void stop()
	{
		if ( DEBUG ) System.out.println("stop");
		
		// Stop all thread and close all streams and sockets
		playing = false;

		// Inform the server that the client is leaving the game
		try{
			Socket s = new Socket(Constants.SERVER_IP, Constants.SERVER_PORT);
			DataOutputStream stopStream = new DataOutputStream(s.getOutputStream());
			
			stopStream.writeInt(Constants.EXIT);
			stopStream.writeInt(ownShipID.getPort());
			
			stopStream.close();
			s.close();
			
			dis.close();
			dos.close();
			reliableSocket.close();
			gamePlaySocket.close();
		}
		catch(IOException e){
			System.out.println("Error stopping");
		}
		
	} // end stop
	
	/**
	 * UpdateJoinThread
	 * @author Daniel J Bray
	 *
	 * UpdateJointhread is a thread used in conjunction with the space
	 * game client to receive update or join messages from the server
	 * originally sent by other players which are then used to update
	 * the display. Closes when client application is closed.
	 */
	class UpdateJoinThread extends Thread{
		public UpdateJoinThread(){
			super();
		}
		
		@Override
		public void run(){
			while(playing){
				try{
					//Reads in datagram packet
					DatagramPacket dp = new DatagramPacket(new byte[24], 24);
					gamePlaySocket.receive(dp);
					
					//Creates necessary streams
					ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
					DataInputStream dp_dis = new DataInputStream(bais);
					
					//Reads in message data
					byte[] ip = new byte[4];
					dp_dis.read(ip);
					int port = dp_dis.readInt();
					int code = dp_dis.readInt();
					int x = dp_dis.readInt();
					int y = dp_dis.readInt();
					int heading = dp_dis.readInt();
					
					InetSocketAddress addr = new InetSocketAddress(InetAddress.getByAddress(ip), port);
					
					//If the code was a ship update it updates that ship's position on the display
					if(code == Constants.UPDATE_SHIP || code == Constants.JOIN)
						sector.updateOrAddSpaceCraft(new SpaceCraft(addr, x, y, heading));
					//Likewise, if the code was a torpedo then it updates that torpedo's position
					else if(code == Constants.UPDATE_TORPEDO)
						sector.updateOrAddTorpedo(new Torpedo(addr, x, y, heading));
					
					//Close data streams
					bais.close();
					dp_dis.close();
				}
				catch(IOException e){}
			}
		}
	}
	
	/**
	 * RemoveThread
	 * @author Daniel J Bray
	 *
	 * RemoveThread is used to listen for remove messages sent by the
	 * server. If a remove message is received then that indicates either
	 * a player left, a player's ship was destroyed, or a torpedo needs to 
	 * be removed from the display. Closed when client application is closed.
	 */
	class RemoveThread extends Thread{
		public RemoveThread(){
			super();
		}
		
		@Override
		public void run(){
			while(playing){
				try{
					//Reads in message info
					byte[] ip = new byte[4];
					dis.read(ip);
					int port = dis.readInt();
					int code = dis.readInt();
					InetSocketAddress addr = new InetSocketAddress(InetAddress.getByAddress(ip), port);	
					
					//Removes spacecraft if it's a ship
					if(code == Constants.REMOVE_SHIP || code == Constants.EXIT){
						sector.removeSpaceCraft(new SpaceCraft(addr));
					}
					//Removes torpedo if it's a torpedo
					else if(code == Constants.REMOVE_TORPEDO){
						sector.removeTorpedo(new Torpedo(addr));
					}
				}
				catch(IOException e){}
			}
		}
	}
	
	/*
	 * Starts the space game. Driver for the application.
	 */
	public static void main(String[] args) 
	{	
		new SpaceGameClient();
				
	} // end main
	
	
} // end SpaceGame class
