import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class OrderPanel extends JPanel {

	TableItemModel tModel;
	JTable table;
	DatabaseManager dbm;
	private NumberFormat formatter = new DecimalFormat("#0.000");

	private List<ListenerInterface> listeners = new ArrayList<ListenerInterface>();
	
	OrderPanel(DatabaseManager dbManager, int w, int h) {
		//Locally hold the database manager
		dbm = dbManager;
		
		//Setup the panel with a border layout
		setPreferredSize(new Dimension(w, h));
		setLayout(new BorderLayout());
		
		//Create a cell renderer to handleright alligning certain columns
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		
		//Create custom table model with orders from the database
		tModel = new TableItemModel(dbm.getAllOrders());
		//Create the table using the model and apply a standard row sorter
		table = new JTable(tModel);
		table.setAutoCreateRowSorter(true);

		setColumnWidths();
		
		//Add listener for double clicks
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1)
					notifyDoubleClick();
				
			}
		});
		
		//Create delete action
		Action delete = new AbstractAction()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	//Get which item is to be deleted and remove from database and model
		        int row = Integer.valueOf( e.getActionCommand() );
		        int orderNum = tModel.getOrderAt(row).getOrderNumber();
		        dbm.deleteAllOrderDetail(orderNum);
		        dbm.deleteOrder(orderNum);
		        tModel.removeRow(row);
		        notifyDelete(Integer.toString(orderNum));
		    }
		};
		
		//Create update action
		Action update = new AbstractAction()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	//Get which item is to be updated 
		    	//Open dialog box and allow editing
		    	//and alter database and model
		        int modelRow = Integer.valueOf( e.getActionCommand() );
		        Order order = tModel.getOrderAt(modelRow);
		        order = createDialogBox(order);
		        if (order != null)  {
		        	dbm.updateOrder(order);
		        	tModel.updateRow(modelRow, order);
		        }
		    }
		};
		
		//Make two columns into button columns and add the previously created actions to them
		ButtonColumn updButtonColumn = new ButtonColumn(table, update, 11);
		ButtonColumn delButtonColumn = new ButtonColumn(table, delete, 12);
		
		//Right allign a specific column
		//e.g. Total cost
		table.getColumnModel().getColumn(10).setCellRenderer(rightRenderer);
		
		//Create scroll pane with table
		JScrollPane sPane = new JScrollPane();
		sPane.setViewportView(table);
		
		//Create menu bar
		JMenuBar mBar = new JMenuBar();
		//Create tab
		JMenu menu = new JMenu("Options");
		
		//Create menu item for creating an order
		JMenuItem menuItm = new JMenuItem("Add Order");
		menuItm.setToolTipText("Add Order Header");
		//Add listener to create dialog box and add order to database and model
		menuItm.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Order order = createDialogBox(null);
				if (order != null) {
					dbm.addOrder(order);
					tModel.addRow(order);
					tModel.fireTableRowsInserted(tModel.getNumberItems() - 1, tModel.getNumberItems() - 1);
				}
			}
			
		});
		//Add item to tab and tab to menu bar
		menu.add(menuItm);
		mBar.add(menu);
		
		//Add menu bar and scroll pane to panel
		add(mBar, BorderLayout.PAGE_START);
		add(sPane, BorderLayout.CENTER);
	}
	
	private void setColumnWidths() {
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.getColumnModel().getColumn(0).setMinWidth(100);
		table.getColumnModel().getColumn(1).setMinWidth(65);
		table.getColumnModel().getColumn(2).setMinWidth(65);
		table.getColumnModel().getColumn(3).setMinWidth(65);
		table.getColumnModel().getColumn(4).setMinWidth(65);
		table.getColumnModel().getColumn(5).setMinWidth(80);
		table.getColumnModel().getColumn(6).setMinWidth(80);
		table.getColumnModel().getColumn(7).setMinWidth(80);
		table.getColumnModel().getColumn(8).setMinWidth(80);
		table.getColumnModel().getColumn(9).setMinWidth(120);
		table.getColumnModel().getColumn(10).setMinWidth(70);
		table.getColumnModel().getColumn(11).setMinWidth(65);
		table.getColumnModel().getColumn(12).setMinWidth(65);
	}
	
	/**
	 * Add a listener to this class
	 * @param listener
	 */
	public void addListener(ListenerInterface listener) {
		listeners.add(listener);
	}
	
	/**
	 * Get the currently selected order number in the table
	 * @return
	 */
	public int getSelectedOrderNumber() {
		return tModel.getOrderAt(table.getSelectedRow()).getOrderNumber();
	}
	
	public DatabaseManager getDBM() {
		return dbm;
	}

	/**
	 * Notify the listeners of a double click
	 * This is to open the details page for a selected order
	 */
	public void notifyDoubleClick() {
		for (ListenerInterface li : listeners) {
			li.doubleClick(getSelectedOrderNumber());
		}
	}
	
	/**
	 * Notify the listeners of a delete
	 * This is to go and close any relevant pages to the deleted order
	 * @param tabName
	 */
	private void notifyDelete(String tabName) {
		for (ListenerInterface li : listeners) {
			li.notifyDelete(tabName);
		}
	}
	
	/**
	 * Handles the udpating of the total cost
	 * Signalled from the details page
	 * @param orderNumber
	 * @param totalCost
	 */
	public void updateCost(int orderNumber, double totalCost) {
		tModel.updateOrder(orderNumber, totalCost);
	}
	
	/**
	 * Create a dialog box
	 * @param order
	 * @return
	 */
	public Order createDialogBox(Order order) {
		//Create a blank order if needed
		if (order == null) {			
			Date date = Date.valueOf(LocalDate.now());
			order = new Order(-1, "", "", "", "", "", date, date, date, "", 0);
		}
		
		//Setup the textfields and combo boxes
		
		JTextField orderNumberField  = new JTextField(5);
		orderNumberField.setText(Integer.toString((order.getOrderNumber())));
		orderNumberField.setMinimumSize(new Dimension(60, 20));
		//Add a listener to restrict the order number to only numeric characters
		orderNumberField.addKeyListener(new KeyAdapter() {
			  @Override
			  public void keyTyped(KeyEvent e) {
				  String s = Character.toString(e.getKeyChar());
				  if (!Pattern.matches("{0,1}[0-9]", s))					 
					  e.consume();
			  }
		  });
		//Give this field initial focus
		orderNumberField.addAncestorListener(new RequestFocusListener());
		
	  JTextField address1Field = new JTextField(30);
	  address1Field.setText(order.getAddress1());
	  address1Field.setMinimumSize(new Dimension(150, 20));
	  //Add a listener to restrict to 30 characters
	  address1Field.addKeyListener(new KeyAdapter() {
		  @Override
		  public void keyTyped(KeyEvent e) {
			  if (address1Field.getText().length() >= 30)
				  e.consume();
		  }
	  });
	  
	  JTextField address2Field = new JTextField(30);
	  address2Field.setText(order.getAddress2());
	  address2Field.setMinimumSize(new Dimension(150, 20));
	  //Add a listener to restrict to 30 characters
	  address2Field.addKeyListener(new KeyAdapter() {
		  @Override
		  public void keyTyped(KeyEvent e) {
			  if (address2Field.getText().length() >= 30)
				  e.consume();
		  }
	  });
	  
	  JTextField address3Field = new JTextField(30);
	  address3Field.setText(order.getAddress3());
	  address3Field.setMinimumSize(new Dimension(150, 20));
	  //Add a listener to restrict to 30 characters
	  address3Field.addKeyListener(new KeyAdapter() {
		  @Override
		  public void keyTyped(KeyEvent e) {
			  if (address3Field.getText().length() >= 30)
				  e.consume();
		  }
	  });
	  
	  JTextField address4Field = new JTextField(30);
	  address4Field.setText(order.getAddress4());
	  address4Field.setMinimumSize(new Dimension(150, 20));
	  //Add a listener to restrict to 30 characters
	  address4Field.addKeyListener(new KeyAdapter() {
		  @Override
		  public void keyTyped(KeyEvent e) {
			  if (address4Field.getText().length() >= 30)
				  e.consume();
		  }
	  });
	  
	  JTextField postcodeField = new JTextField(10);
	  postcodeField.setText(order.getPostcode());
	  postcodeField.setMinimumSize(new Dimension(100, 20));
	  //Add a listener to restrict to 10 characters
	  postcodeField.addKeyListener(new KeyAdapter() {
		  @Override
		  public void keyTyped(KeyEvent e) {
			  if (postcodeField.getText().length() >= 10)
				  e.consume();
		  }
	  });
	  
	  JTextField orderDateField = new JTextField(30);
	  orderDateField.setText(order.getOrderDate().toString());
	  orderDateField.setMinimumSize(new Dimension(100, 20));
	  
	  JButton pickODate = new JButton("...");
	  pickODate.setMinimumSize(new Dimension(30, 20));
	  //Add a listener to open the date picker dialog
	  pickODate.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			DatePicker dPicker = new DatePicker(pickODate, orderDateField.getText());
			orderDateField.setText(dPicker.formatDate());
		}
		  
	  });
	  
	  JTextField promiseDateField = new JTextField(30);
	  promiseDateField.setText(order.getPromiseDate().toString());
	  promiseDateField.setMinimumSize(new Dimension(100, 20));
	  
	  JButton pickPDate = new JButton("...");
	  pickPDate.setMinimumSize(new Dimension(30, 20));
	  //Add a listener to open the date picker dialog
	  pickPDate.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			DatePicker dPicker = new DatePicker(pickPDate, promiseDateField.getText());
			promiseDateField.setText(dPicker.formatDate());
		}
		  
	  });
	  
	  JTextField deliveryDateField = new JTextField(30);
	  deliveryDateField.setText(order.getDeliveryDate().toString());
	  deliveryDateField.setMinimumSize(new Dimension(100, 20));
	  
	  JButton pickDDate = new JButton("...");
	  pickDDate.setMinimumSize(new Dimension(30, 20));
	  //Add a listener to open the date picker dialog
	  pickDDate.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			DatePicker dPicker = new DatePicker(pickDDate, deliveryDateField.getText());
			deliveryDateField.setText(dPicker.formatDate());
		}
		  
	  });
	  
	  JTextField notesField = new JTextField(200);
	  notesField.setText(order.getNotes());
	  //Add a listener to restrict to 200 characters
	  notesField.addKeyListener(new KeyAdapter() {
		  @Override
		  public void keyTyped(KeyEvent e) {
			  if (notesField.getText().length() >= 200)
				  e.consume();
		  }
	  });
	  
	  //Create panel with gridbag layout
	  JPanel myPanel = new JPanel();
	  myPanel.setPreferredSize(new Dimension(400, 200));
	  myPanel.setLayout(new GridBagLayout());
	  //Use constraints to handle layout
	  GridBagConstraints c = new GridBagConstraints();
	  
	  //Layout: [ Label -> textfield/combobox -> button ]
	 
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
	  myPanel.add(new JLabel("Customer Address 1:"), c);
	  
	  c.fill =  GridBagConstraints.NONE;
	  c.weightx = 0.9;
	  c.gridx = 1;
	  c.gridy = 1;
	  c.anchor = GridBagConstraints.WEST;
	  myPanel.add(address1Field, c);
	  
	  c.fill =  GridBagConstraints.HORIZONTAL;
	  c.weightx = 0.1;
	  c.gridx = 0;
	  c.gridy = 2;
	  myPanel.add(new JLabel("Customer Address 2:"), c);
	  
	  c.fill =  GridBagConstraints.NONE;
	  c.weightx = 0.9;
	  c.gridx = 1;
	  c.gridy = 2;
	  c.anchor = GridBagConstraints.WEST;
	  myPanel.add(address2Field, c);
	  
	  c.fill =  GridBagConstraints.HORIZONTAL;
	  c.weightx = 0.1;
	  c.gridx = 0;
	  c.gridy = 3;
	  myPanel.add(new JLabel("Customer Address 3:"), c);
	  
	  c.fill =  GridBagConstraints.NONE;
	  c.weightx = 0.9;
	  c.gridx = 1;
	  c.gridy = 3;
	  c.anchor = GridBagConstraints.WEST;
	  myPanel.add(address3Field, c);
	  
	  c.fill =  GridBagConstraints.HORIZONTAL;
	  c.weightx = 0.1;
	  c.gridx = 0;
	  c.gridy = 4;
	  myPanel.add(new JLabel("Customer Address 4:"), c);
	  
	  c.fill =  GridBagConstraints.NONE;
	  c.weightx = 0.9;
	  c.gridx = 1;
	  c.gridy = 4;
	  c.anchor = GridBagConstraints.WEST;
	  myPanel.add(address4Field, c);
	  
	  c.fill =  GridBagConstraints.HORIZONTAL;
	  c.weightx = 0.1;
	  c.gridx = 0;
	  c.gridy = 5;
	  myPanel.add(new JLabel("Postcode:"), c);
	  
	  c.fill =  GridBagConstraints.NONE;
	  c.weightx = 0.9;
	  c.gridx = 1;
	  c.gridy = 5;
	  c.anchor = GridBagConstraints.WEST;
	  myPanel.add(postcodeField, c);
	  
	  c.fill =  GridBagConstraints.HORIZONTAL;
	  c.weightx = 0.1;
	  c.gridx = 0;
	  c.gridy = 6;
	  myPanel.add(new JLabel("Order Date:"), c);
	  
	  c.fill =  GridBagConstraints.NONE;
	  c.weightx = 0.7;
	  c.gridx = 1;
	  c.gridy = 6;
	  c.anchor = GridBagConstraints.WEST;
	  myPanel.add(orderDateField, c);
	  
	  c.fill =  GridBagConstraints.NONE;
	  c.weightx = 0.2;
	  c.gridx = 2;
	  c.gridy = 6;
	  c.anchor = GridBagConstraints.WEST;
	  myPanel.add(pickODate, c);
	  
	  c.fill =  GridBagConstraints.HORIZONTAL;
	  c.weightx = 0.1;
	  c.gridx = 0;
	  c.gridy = 7;
	  myPanel.add(new JLabel("Promise Date:"), c);
	  
	  c.fill =  GridBagConstraints.NONE;
	  c.weightx = 0.7;
	  c.gridx = 1;
	  c.gridy = 7;
	  c.anchor = GridBagConstraints.WEST;
	  myPanel.add(promiseDateField, c);
	  
	  c.fill =  GridBagConstraints.NONE;
	  c.weightx = 0.2;
	  c.gridx = 2;
	  c.gridy = 7;
	  c.anchor = GridBagConstraints.WEST;
	  myPanel.add(pickPDate, c);
	  
	  c.fill =  GridBagConstraints.HORIZONTAL;
	  c.weightx = 0.1;
	  c.gridx = 0;
	  c.gridy = 8;
	  myPanel.add(new JLabel("Delivery Date:"), c);
	  
	  c.fill =  GridBagConstraints.NONE;
	  c.weightx = 0.7;
	  c.gridx = 1;
	  c.gridy = 8;
	  c.anchor = GridBagConstraints.WEST;
	  myPanel.add(deliveryDateField, c);
	  
	  c.fill =  GridBagConstraints.NONE;
	  c.weightx = 0.2;
	  c.gridx = 2;
	  c.gridy = 8;
	  c.anchor = GridBagConstraints.WEST;
	  myPanel.add(pickDDate, c);
	  
	  c.fill =  GridBagConstraints.HORIZONTAL;
	  c.weightx = 0.1;
	  c.gridx = 0;
	  c.gridy = 9;
	  myPanel.add(new JLabel("Notes:"), c);
	  
	  c.fill =  GridBagConstraints.HORIZONTAL;
	  c.weightx = 0.9;
	  c.gridx = 1;
	  c.gridy = 9;
	  myPanel.add(notesField, c);      
	
	  //Create dialog box using panel
	  int result = JOptionPane.showConfirmDialog(null, myPanel, 
	           "Please Enter item information", JOptionPane.OK_CANCEL_OPTION);
	  //If OK is selected create the order
	  if (result == JOptionPane.OK_OPTION)
		  return new Order(Integer.parseInt(orderNumberField.getText()), address1Field.getText(), address2Field.getText(), 
				  address3Field.getText(), address4Field.getText(), postcodeField.getText().toUpperCase(), Date.valueOf(orderDateField.getText()),
				  Date.valueOf(promiseDateField.getText()), Date.valueOf(deliveryDateField.getText()), notesField.getText(), 0);
	  else
		  return null;
	}
	
	
	/**
	 * Custom table model
	 * @author Matt
	 *
	 */
	public class TableItemModel extends AbstractTableModel {

		//Array of table headers
		String[] columnNames = { "Order Number", "Address 1", "Address 2", "Address 3",
				"Address 4", "Postcode", "Order Date", "Promise Date", "Delivery Date", "Notes", "Total Cost", "", ""};

	    private List<Order> orders;
	    
	    /**
	     * Initialize orders list
	     */
	    public TableItemModel(List<Order> orders) {

	        this.orders = new ArrayList<Order>(orders);

	    }

	    @Override
	    public int getRowCount() {
	        return orders.size();
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
	    	//Only button columns should be editable
	    	if (col == 11 || col == 12)
	    		return true;
	    	return false;
	    }
	    
	    @Override
	    public void setValueAt(Object value, int row, int col) {
	    	//Setup values in the model
			 Order order = orders.get(row);
			 switch (col) {
				 case 0:
					 order.setOrderNumber((int)value);
		             break;
		         case 1:
		        	 order.setAddress1((String)value);
		             break;
		         case 2:
		        	 order.setAddress2((String)value);
		             break;
		         case 3:
		        	 order.setAddress3((String)value);
		             break;
		         case 4:
		        	 order.setAddress4((String)value);
		             break;
		         case 5:
		        	 order.setPostcode((String)value);
		             break;
		         case 6:
		        	 order.setOrderDate((Date)value);
		             break;
		         case 7:
		        	 order.setPromiseDate((Date)value);
		             break;
		         case 8:
		        	 order.setDeliveryDate((Date)value);
		             break;
		         case 9:
		        	 order.setNotes((String)value);
		             break;
		         case 10:
		        	 order.setTotCost((double)value);
		        	 break;
			 }		 
		    fireTableCellUpdated(row, col);
	  }
	    @Override
	    public Object getValueAt(int rowIndex, int columnIndex) {
	    	//Set values to be shown on the table
	        Object value = "??";
	        Order order = orders.get(rowIndex);
	        switch (columnIndex) {
	            case 0:
	            	value = order.getOrderNumber();
	                break;
	            case 1:
	                value = order.getAddress1();
	                break;
	            case 2:
	                value = order.getAddress2();
	                break;
	            case 3:
	                value = order.getAddress3();
	                break;
	            case 4:
	                value = order.getAddress4();
	                break;
	            case 5:
	                value = order.getPostcode();
	                break;
	            case 6:
	                value = order.getOrderDate();
	                break;
	            case 7:
	                value = order.getPromiseDate();
	                break;
	            case 8:
	                value = order.getDeliveryDate();
	                break;
	            case 9:
	                value = order.getNotes();
	                break;
	            case 10:
	            	value = formatter.format(round(order.getTotCost(), 3));
	            	break;
	            case 11:
	            	value = "UPD";
	            	break;
	            case 12:
	            	value = "DEL";
	            	break;
	        }

	        return value;
	    }
	    
	    /**
	     * Apply an update to a specific order in the model
	     * @param orderNumber
	     * @param totalCost
	     */
	    public void updateOrder(int orderNumber, double totalCost) {
	    	for (Order order : orders) {
	    		if (order.getOrderNumber() == orderNumber) {
	    			order.setTotCost(totalCost);
	    			fireTableDataChanged();
	    		}
	    	}
	    }
	    
	    /**
	     * Add an order to the model
	     * @param order
	     */
	    public void addRow(Order order) {
	    	orders.add(order);
	    }
	    
	    /**
	     * Remove an order from the model
	     * @param row
	     */
	    public void removeRow(int row) {
	    	//Present a confirmation dialog
	    	JPanel panel = new JPanel();
	    	panel.add(new JLabel("Are you sure you want to delete this order?"));
	    	int result = JOptionPane.showConfirmDialog(null, panel, 
		               "Confirmation", JOptionPane.YES_NO_OPTION);
		      if (result == JOptionPane.OK_OPTION) {
		    	  orders.remove(row);
		    	  this.fireTableRowsDeleted(row, row);
		      }
	    }

	    /**
	     * Update a specific row in the model
	     * @param row
	     * @param order
	     */
	    public void updateRow(int row, Order order) {
	    	orders.remove(row);
	    	orders.add(row, order);
	    	this.fireTableRowsUpdated(row, row);
	    }
	    /**
	     * This will return the order at the specified row...
	     * @param row
	     * @return 
	     */
	    public Order getOrderAt(int row) {
	        return orders.get(row);
	    }
	    
	    public int getNumberItems() {
	    	return orders.size();
	    }

	}
	
	/**
	 * Handle rounding of costs and quantites
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
