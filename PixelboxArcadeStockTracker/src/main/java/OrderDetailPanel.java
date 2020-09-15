import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class OrderDetailPanel extends JPanel{

	TableItemModel tModel;
	JTable table;
	DatabaseManager dbm;
	private NumberFormat formatter = new DecimalFormat("#0.000");

	private List<ListenerInterface> listeners = new ArrayList<ListenerInterface>();

	OrderDetailPanel(DatabaseManager dbManager, int w, int h, int orderNumber) {
		//Locally holds the database manager
		dbm = dbManager;
		
		//Setup the panel with a border layout
		setPreferredSize(new Dimension(w, h));
		setLayout(new BorderLayout());

		//Create a cell renderer to handle right alligning in some cells
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		
		//Create a custom table model with the order details from the database
		tModel = new TableItemModel(dbm.getAllOrderDetails(orderNumber));
		notifyUpdateRequired(orderNumber);
		//Create the table using the model and apply a standard row sorter
		table = new JTable(tModel);
		table.setAutoCreateRowSorter(true);

		//Set column widths
		setColumnWidths();

		//Add a mouse listener to the table to open an update dialog
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//Check there is a double click
				if (e.getClickCount() > 1) {
					//Get the row of the event
					int row = table.getSelectedRow();
					//Convert to model row (incase of sorting)
			        int mRow = table.convertRowIndexToView(row);
					OrderDetail detail = tModel.getOrderDetailAt(mRow);
					//Create dialog box using the required details
					detail = createDialogBox(detail, orderNumber);
					if (detail != null)  {
						//Go and update the details in the database and the model
						dbm.updateOrderDetail(detail);
						tModel.updateRow(mRow, detail);
						notifyUpdateRequired(orderNumber);
					}
				}
			}
		});
		
		//Create a delete action
		Action delete = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				//Delete chosen row from database and model
				int modelRow = Integer.valueOf( e.getActionCommand() );
				OrderDetail od = tModel.getOrderDetailAt(modelRow);
				dbm.deleteOrderDetail(od.getOrderNumber(), od.getItemCode());
				tModel.removeRow(modelRow);
				notifyUpdateRequired(orderNumber);
			}
		};

		//Apply the action to a certain column with buttons
		ButtonColumn delButtonColumn = new ButtonColumn(table, delete, 4);
		
		//Right allign two columns (costs)
		table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
		table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

		//Create scroll pane with table
		JScrollPane sPane = new JScrollPane();
		sPane.setViewportView(table);

		//Create menu bar
		JMenuBar mBar = new JMenuBar();

		//Create an options tab
		JMenu menu = new JMenu("Options");

		//Create the items to add onto the tab
		
		JMenuItem menuItmAdd = new JMenuItem("Add Item");
		menuItmAdd.setToolTipText("Add Item to Order");
		//Add a listener to handle opening a dialog box
		//And adding to the database and model
		menuItmAdd.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				OrderDetail detail = createDialogBox(null, orderNumber);
				if (detail != null) {
					dbm.addOrderDetail(detail);
					tModel.addRow(detail);
					tModel.fireTableRowsInserted(tModel.getNumberItems() - 1, tModel.getNumberItems() - 1);
					notifyUpdateRequired(orderNumber);
				}
			}

		});

		JMenuItem menuItmImport = new JMenuItem("Import Items");
		menuItmImport.setToolTipText("Add Items from Bill of Material");
		//Add a listener to import a specified bill of materials items onto the order
		menuItmImport.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				List<OrderDetail> details = createImportDialog(dbm.getAllBillIDs(), orderNumber);
				if (details != null) {
					for (OrderDetail detail : details) {
						dbm.addOrderDetail(detail);
						tModel.addRow(detail);
						tModel.fireTableRowsInserted(tModel.getNumberItems() - 1, tModel.getNumberItems() - 1);
						notifyUpdateRequired(orderNumber);
					}
				}
			}

		});

		JMenuItem menuItmSubmit = new JMenuItem("Submit Order");
		menuItmSubmit.setToolTipText("Submit current order");
		//Add a listener to go and update the stock of all of the items on the order
		menuItmSubmit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to submit the order?", 
						"Submit Order", JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					List<OrderDetail> details = tModel.getAllDetails();
					for (OrderDetail detail : details) {
						double quantRequired = detail.getQuantity();
						dbm.reduceStock(detail.getItemCode(), quantRequired);
					}
					notifyRepaintRequired();
				}
			}

		});
		
		JMenuItem menuItmBuild = new JMenuItem("Build Item");
		menuItmBuild.setToolTipText("Build Current Order Into Item");
		//Add a listener to go and turn the items on this order into a single item
		//Updating the stock of the item on the item table
		//While also decreasing the stock of the items on the order
		menuItmBuild.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String iCode = createBuildItemDialog();
				int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to build the order?", 
						"Build Order", JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					applyStockReduction();
					applyStockIncrease(iCode);
				}
			}

		});

		//Add the items to the tab and the tab to the menu bar
		menu.add(menuItmAdd);
		menu.add(menuItmImport);
		menu.add(menuItmSubmit);
		menu.add(menuItmBuild);
		mBar.add(menu);

		//Add the menu bar and scroll pane to the panlel
		add(mBar, BorderLayout.PAGE_START);
		add(sPane, BorderLayout.CENTER);
	}

	private void setColumnWidths() {
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.getColumnModel().getColumn(0).setPreferredWidth(170);
		table.getColumnModel().getColumn(1).setPreferredWidth(400);
		table.getColumnModel().getColumn(2).setPreferredWidth(80);
		table.getColumnModel().getColumn(3).setPreferredWidth(80);
		table.getColumnModel().getColumn(4).setPreferredWidth(100);
	}
	
	/**
	 * Gets the currently selected rows order number
	 * @return
	 */
	public int getSelectedOrderNumber() {
		return tModel.getOrderDetailAt(table.getSelectedRow()).getOrderNumber();
	}

	public DatabaseManager getDBM() {
		return dbm;
	}

	/**
	 * Add a listener to this class
	 * @param listener
	 */
	public void addListener(ListenerInterface listener) {
		listeners.add(listener);
	}

	/**
	 * Notify the listeners that an update needs to occur on the items table
	 * For updating of stock
	 */
	public void notifyRepaintRequired() {
		for (ListenerInterface li : listeners) {
			li.repaintItems();
		}
	}

	/**
	 * Notify the listeners that an update needs to occur on the order table
	 * For updating the total cost
	 * @param orderNumber
	 */
	public void notifyUpdateRequired(int orderNumber) {
		for (ListenerInterface li : listeners) {
			li.repaintCost(orderNumber, getTotalCost());
		}
	}

	/**
	 * Calculates the total cost of the order
	 * @return
	 */
	private double getTotalCost() {
		double total = 0;
		for (OrderDetail detail : tModel.getAllDetails()) {
			total += detail.getCost() * detail.getQuantity();
		}
		return total;
	}
	
	/**
	 * Applies all of the stock reduction required for each item in the order
	 * This does so to the database first then goes and repaints the items table
	 */
	private void applyStockReduction() {
		List<OrderDetail> details = tModel.getAllDetails();
		for (OrderDetail detail : details) {
			double quantRequired = detail.getQuantity();
			dbm.reduceStock(detail.getItemCode(), quantRequired);
		}
		notifyRepaintRequired();
	}
	
	/**
	 * Applies the stock increase required for the item the order is to be built into
	 * This does so to the database first then goes and repaints the items table
	 */
	private void applyStockIncrease(String iCode) {
		double origStock = dbm.getAvailable(iCode);
		double newCost = getNewCost(dbm.getCost(iCode), origStock, getTotalCost(), 1);
		
		dbm.addStock(iCode, origStock + 1, newCost);
		notifyRepaintRequired();
	}

	/**
	 * Calculates an updated cost (a cost per unit)
	 * @param currC
	 * @param currS
	 * @param newC
	 * @param newS
	 * @return
	 */
	private double getNewCost(double currC, double currS, double newC, double newS) {
		if (newS + currS == 0)
			return 0;
		return (((currC * currS) + (newC * newS)) / (currS + newS));
	}
	
	/**
	 * Create build item dialog box
	 * @return
	 */
	private String createBuildItemDialog() {
		
		//Create combobox holding all of the item codes
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(convertToArray(dbm.getAllICodes()));
		JComboBox<String> iCodeBox = new JComboBox<String>(model);
		iCodeBox.setMinimumSize(new Dimension(150, 20));
		iCodeBox.setEditable(false);
		
		//Create panel with gridbag layout
		JPanel myPanel = new JPanel();
		myPanel.setPreferredSize(new Dimension(400, 200));
		myPanel.setLayout(new GridBagLayout());
		//Use constraints to handle layout
		GridBagConstraints c = new GridBagConstraints();
		
		//Layout: [ label -> combobox/textfield ]

		c.fill =  GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.gridx = 0;
		c.gridy = 0;
		myPanel.add(new JLabel("Item Code:"), c);

		c.fill =  GridBagConstraints.NONE;
		c.weightx = 0.9;
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		myPanel.add(iCodeBox, c);
		
		//Create dialog box using the panel
		int result = JOptionPane.showConfirmDialog(null, myPanel, 
				"Select Item to build into", JOptionPane.OK_CANCEL_OPTION);
		//If OK is selected return the selected item code
		if (result == JOptionPane.OK_OPTION)
			return model.getSelectedItem().toString();
		else
			return null;
	}
	
	/**
	 * Create standard dialog box
	 * @param detail
	 * @param orderNumber
	 * @return
	 */
	public OrderDetail createDialogBox(OrderDetail detail, int orderNumber) {
		//If needed create a blank details object
		if (detail == null) {			
			detail = new OrderDetail(orderNumber, "", 0, "", 0);
		}

		//Setup textfields and comboboxes
		
		JTextField orderNumberField  = new JTextField(5);
		orderNumberField.setText(Integer.toString((detail.getOrderNumber())));
		orderNumberField.setMinimumSize(new Dimension(60, 20));
		orderNumberField.setText(Integer.toString(orderNumber));
		orderNumberField.setEditable(false);

		JTextField descField = new JTextField(30);
		descField.setText(detail.getDescription());
		descField.setEditable(false);

		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(convertToArray(dbm.getAllICodes()));
		model.setSelectedItem(detail.getItemCode());
		JComboBox<String> iCodeBox = new JComboBox<String>(model);
		iCodeBox.setMinimumSize(new Dimension(150, 20));
		iCodeBox.setEditable(false);
		//Add listener to update the description field when the item code is changed
		iCodeBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					descField.setText(dbm.getItemDescription(model.getSelectedItem().toString()));
				}
			}

		});
		iCodeBox.setSelectedItem(detail.getItemCode());

		JTextField quantField = new JTextField(3);
		quantField.setText(Double.toString(detail.getQuantity()));
		quantField.setMinimumSize(new Dimension(50, 20));
		//Add a listener to ensure that the quantity is formatted correctly
		//And "." can not be placed in illogical places
		quantField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (quantField.getText().length() >= 9)
					e.consume();
				if (!e.isConsumed()) {
					String s = Character.toString(e.getKeyChar());
					if (!Pattern.matches("{0,1}[0-9.]", s))
						if (!s.equals(".")) 
							e.consume();
					if ((s.equals(".") && quantField.getText().contains(".")) || 
							(s.equals(".") && quantField.getText().length() == 0) || 
							(s.equals(".") && quantField.getText().length() == 8))
						e.consume();
				}
			}
		});
		//Put the focus onto this field
		quantField.addAncestorListener(new RequestFocusListener());

		//Create panel with gridbag layout
		JPanel myPanel = new JPanel();
		myPanel.setPreferredSize(new Dimension(400, 200));
		myPanel.setLayout(new GridBagLayout());
		//Use constraints to handle layout
		GridBagConstraints c = new GridBagConstraints();
		
		//Layout: [ Label -> textfield/combobox ]

		c.fill =  GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.gridx = 0;
		c.gridy = 0;
		myPanel.add(new JLabel("Order Number:"), c);

		c.fill =  GridBagConstraints.NONE;
		c.weightx = 0.9;
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		myPanel.add(orderNumberField, c);

		c.fill =  GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.gridx = 0;
		c.gridy = 1;
		myPanel.add(new JLabel("Item Code:"), c);

		c.fill =  GridBagConstraints.NONE;
		c.weightx = 0.9;
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		myPanel.add(iCodeBox, c);

		c.fill =  GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.gridx = 0;
		c.gridy = 2;
		myPanel.add(new JLabel("Quantity:"), c);

		c.fill =  GridBagConstraints.NONE;
		c.weightx = 0.9;
		c.gridx = 1;
		c.gridy = 2;
		c.anchor = GridBagConstraints.WEST;
		myPanel.add(quantField, c);

		//Create dialog box using the panel
		int result = JOptionPane.showConfirmDialog(null, myPanel, 
				"Please Enter detail information", JOptionPane.OK_CANCEL_OPTION);
		//If OK is selected create the order detail
		if (result == JOptionPane.OK_OPTION)
			return new OrderDetail(Integer.parseInt(orderNumberField.getText()), model.getSelectedItem().toString(), 
					Double.parseDouble(quantField.getText()), descField.getText(), dbm.getCost(model.getSelectedItem().toString()));
		else
			return null;
	}

	/**
	 * Create import items dialog box
	 * @param billIDs
	 * @param orderNumber
	 * @return
	 */
	public List<OrderDetail> createImportDialog(List<String> billIDs, int orderNumber) {
		//Create combo box with bill ids
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(convertToArray(billIDs));
		JComboBox<String> billIDsBox = new JComboBox<String>(model);
		billIDsBox.setMinimumSize(new Dimension(150, 20));
		billIDsBox.setEditable(false);

		//Create panel with gridbag layout
		JPanel myPanel = new JPanel();
		myPanel.setPreferredSize(new Dimension(100, 100));
		myPanel.setLayout(new GridBagLayout());
		//Use constraints the handle layout
		GridBagConstraints c = new GridBagConstraints();
		
		//Layout: [ Label -> combobox/textfield ]

		c.fill =  GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.gridx = 0;
		c.gridy = 0;
		myPanel.add(new JLabel("Bill ID:"), c);

		c.fill =  GridBagConstraints.NONE;
		c.weightx = 0.9;
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		myPanel.add(billIDsBox, c);

		//Create dialog using the panel
		int result = JOptionPane.showConfirmDialog(null, myPanel, 
				"Please Enter detail information", JOptionPane.OK_CANCEL_OPTION);
		//If OK is selected go and get all of the items from the bill
		//and add them to these order details
		if (result == JOptionPane.OK_OPTION) {
			List<BOMD> bomds = dbm.getBOMDS(model.getSelectedItem().toString());
			List<OrderDetail> details = new ArrayList<OrderDetail>();
			for (BOMD bomd : bomds) {
				details.add(new OrderDetail(orderNumber, bomd.getItemCode(), bomd.getQuantity(), 
						dbm.getItemDescription(bomd.getDescription()),dbm.getCost(bomd.getItemCode())));
			}
			return details;
		} else
			return null;

	}

	/**
	 * Convert a list of strings into an array for the model
	 * @param strs
	 * @return
	 */
	public String[] convertToArray(List<String> strs) {
		String[] arr = new String[strs.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = strs.get(i);
		}
		return arr;
	}
	
	/**
	 * Custom table model
	 * @author Matt
	 *
	 */
	public class TableItemModel extends AbstractTableModel {

		//Array for column headers
		String[] columnNames = { "Item Code", "Description", "Quantity", "Cost", "" };

		private List<OrderDetail> details;

		/**
		 * Initialize details list
		 * @param details
		 */
		public TableItemModel(List<OrderDetail> details) {

			this.details = new ArrayList<OrderDetail>(details);

		}

		@Override
		public int getRowCount() {
			return details.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int index)  {
			return columnNames[index]; 
		}

		@Override
		public boolean isCellEditable(int row, int col){
			//Only make button columns editable
			if (col == 4)
				return true;
			return false;
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			//Setup values in model
			OrderDetail detail = details.get(row);
			switch (col) {
			case 0:
				detail.setItemCode((String)value);
				break;
			case 1:
				detail.setDescription((String)value);
				break;
			case 2:
				detail.setQuantity((double)value);
				break;
			case 3:
				detail.setCost((double)value);
				break;
			}		 
			fireTableCellUpdated(row, col);
		}
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			//Set value that will be shown in the table
			Object value = "??";
			OrderDetail detail = details.get(rowIndex);
			switch (columnIndex) {
			case 0:
				value = detail.getItemCode();
				break;
			case 1:
				value = detail.getDescription();
				break;
			case 2:
				value = formatter.format(round(detail.getQuantity(), 3));
				break;
			case 3:
				value = formatter.format(round(detail.getCost(), 3));
				break;
			case 4:
				value = "DEL";
				break;
			}

			return value;
		}

		/**
		 * Add detail to model
		 * @param itm
		 */
		public void addRow(OrderDetail itm) {
			details.add(itm);
		}

		/**
		 * Remove detail from model
		 * @param row
		 */
		public void removeRow(int row) {
			//Create confirmation dialog box
			JPanel panel = new JPanel();
			panel.add(new JLabel("Are you sure you want to delete this detail?"));
			int result = JOptionPane.showConfirmDialog(null, panel, 
					"Confirmation", JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				details.remove(row);
				this.fireTableRowsDeleted(row, row);
			}
		}

		/**
		 * Update detail in model
		 * @param row
		 * @param itm
		 */
		public void updateRow(int row, OrderDetail itm) {
			details.remove(row);
			details.add(row, itm);
			this.fireTableRowsUpdated(row, row);
		}
		/**
		 * This will return the detail at the specified row...
		 * @param row
		 * @return 
		 */
		public OrderDetail getOrderDetailAt(int row) {
			return details.get(row);
		}

		public int getNumberItems() {
			return details.size();
		}

		public List<OrderDetail> getAllDetails() {
			return details;
		}

	}
	
	/**
	 * Handle rounding of costs and quantities
	 * @param val
	 * @param places
	 * @return
	 */
	private double round(double val, int places) {
		BigDecimal bd = BigDecimal.valueOf(val);
		//Round up
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
}
