package spaceWar;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * Implements GUI for the server.
 * 
 * @author bachmaer
 */

public class ServerGUI extends Frame {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = 1L;

	public ServerGUI(Sector sector) {

		super("Space War Server View");
		
		this.setTitle("Space War Server View");
		
		sector.setPreferredSize(new Dimension(Constants.MAX_SECTOR_X, Constants.MAX_SECTOR_Y));
		
		add( sector, BorderLayout.CENTER);
	
		addWindowListener(new WindowAdapter() {
	
		   public void windowClosing(WindowEvent e) { System.exit(0); }
	
		});

		this.pack();
		this.setResizable( false );		
		setVisible( true );

} // end default constructor
}
