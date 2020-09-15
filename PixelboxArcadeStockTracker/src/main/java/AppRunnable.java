import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class AppRunnable implements Runnable, ListenerInterface{

	private int WIDTH;
	private int HEIGHT;
	private int reducedHeight;
	private final JFrame frame = new JFrame("Stock Tracker");
	private ClosableTabbedPane tabbedPane;
	private DatabaseManager dbm;
	private ItemPanel itemPanel;
	private OrderPanel orderPanel;	
	
	AppRunnable(int w, int h) {
		WIDTH = w;
		HEIGHT = h;
	}
	
	@Override
	public void run() {
		
		//Setup the frame properties
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		//Create a panel the same size as the frame with a borer layout
		final JPanel gui = new JPanel(new BorderLayout());
		gui.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		//Initialize the database manager
		dbm = null;
		try {
			dbm = new DatabaseManager();
		} catch (ClassNotFoundException | IOException | SQLException e1) {
			e1.printStackTrace();
		}
		
		//Create a closable tabbed pane
		tabbedPane = new ClosableTabbedPane();
				
		//Create a panel for the item table and insert as a tab to the pane
		itemPanel = new ItemPanel(dbm, WIDTH, HEIGHT);
		tabbedPane.insertTab("Item Table", null, itemPanel, "Item Table", tabbedPane.getTabCount());
		
		//Create a panel for the Bill of Material table and insert as a tab to the pane
		BOMPanel bomPanel = new BOMPanel(dbm, WIDTH, HEIGHT);
		//Add a listener to the panels
		bomPanel.addListener(this);
		tabbedPane.insertTab("Bill of Material Table", null, bomPanel, "Bill of Material Table", tabbedPane.getTabCount());
		
		//Create a panel for the order table and insert as a tab to the pane
		orderPanel = new OrderPanel(dbm, WIDTH, HEIGHT);
		//Add a listener to the panel
		orderPanel.addListener(this);
		tabbedPane.insertTab("Order Table", null, orderPanel, "Order Table", tabbedPane.getTabCount());
		
		//Add the tabbed pane to the main panel
		gui.add(tabbedPane, BorderLayout.NORTH);
		
		//Add the main panel to the frame, pack and set the frame to visible
		frame.setContentPane(gui);
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * Handle double clicking on the BOM panel
	 */
	@Override
	public void doubleClick(String billID) {
		
		//Get the number of tabs
		int count = tabbedPane.getTabCount();
		boolean add = true;
		//Loop through the number of tabs
		for (int i = 0; i < count; i++) {
			//Check to see if a tab exists containing the bill id required
			//If so, set add to false
			if (tabbedPane.getTitleAt(i).contains(billID))
				add = false;
		}
		
		//If add is still true
		if (add) {
			//Create a panel for Bill of Material Detail using the required id
			//And insert it on the tab pane
			BOMDPanel bomdPanel = new BOMDPanel(dbm, billID, WIDTH, HEIGHT);
			tabbedPane.insertTab(billID + " Details Table", null, bomdPanel, billID + " Details Table", tabbedPane.getTabCount());
		}
	}
	
	/**
	 * Handle double clicking on the order panel
	 */
	@Override
	public void doubleClick(int orderNumber) {
		
		//Get the number of tabs
		int count = tabbedPane.getTabCount();
		boolean add = true;
		//Loop through the number of tabs
		for (int i = 0; i < count; i++) {
			//Check to see if a tab exists containing the order number required
			//If so, set add to false
			if (tabbedPane.getTitleAt(i).contains(Integer.toString(orderNumber)))
				add = false;
		}
		
		//If add is still true
		if (add) {
			//Create a panel for Order Detail using the required number
			//And insert it on the tab pane
			OrderDetailPanel orderDetailPanel = new OrderDetailPanel(dbm, WIDTH, HEIGHT, orderNumber);
			orderDetailPanel.addListener(this);
			tabbedPane.insertTab(orderNumber + " Details Table", null, orderDetailPanel, orderNumber + " Details Table", 
					tabbedPane.getTabCount());	
		}

	}
	
	/**
	 * Signals there needs to be an update of the stock level
	 */
	@Override
	public void repaintItems() {
		itemPanel.updateStockLevel();
	}
	
	/**
	 * Signals there needs to be an update to  the cost on the order panel
	 */
	@Override
	public void repaintCost(int orderNumber, double totalCost) {
		orderPanel.updateCost(orderNumber, totalCost);
		frame.repaint();
	}

	/**
	 * Signals to remove a specific tab from the pane
	 */
	@Override
	public void notifyDelete(String tabName) {
		int count = tabbedPane.getTabCount();
		
		for (int i = 0; i < count; i++) {
			if (tabbedPane.getTitleAt(i).contains(tabName))
				tabbedPane.remove(i);
		}
		
	}
}
