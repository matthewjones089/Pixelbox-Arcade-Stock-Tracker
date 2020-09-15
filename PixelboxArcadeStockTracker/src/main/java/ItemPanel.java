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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class ItemPanel extends JPanel{ 

	JTable table;
	TableItemModel tModel;
	DatabaseManager dbm;
	private NumberFormat formatter = new DecimalFormat("#0.000");
	ItemPanel(DatabaseManager dbManager, int w, int h) {
		//Locally hold instance of database manager
		dbm = dbManager;
		
		//Setup panel with a border layout
		setPreferredSize(new Dimension(w, h));
		setLayout(new BorderLayout());
		
		//Create a cell renderer to right allign some columns
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		
		//Create a custom table model with all items currently in database
		tModel = new TableItemModel(dbm.getItems());
		//Create the table using the model and apply a standard sorter to it
		table = new JTable(tModel);
		table.setAutoCreateRowSorter(true);

		//Set the column widths
		setColumnWidths();
		
		//Add a mouse listener to the table
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//Check for a double click
				//If detected open an update dialog
				if (e.getClickCount() > 1) {
					//Get the row where the double click occured
					int row = table.getSelectedRow();
					//Convert idx to model idx (Incase of sorting)
			        int mRow = table.convertRowIndexToView(row);
			        Item itm = tModel.getItemAt(mRow);
			        //Open a dialog using the selected item
			        itm = createDialogBox(itm);
			        if (itm != null)  {
			        	//Go and update the item in the database and model
			        	dbm.updateItem(itm);
			        	tModel.updateRow(mRow, itm);
			        }
				}
			}
		});
		
		//Create an action for deleting
		Action delete = new AbstractAction()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	//Find the row of the action
		        int modelRow = Integer.valueOf( e.getActionCommand() );
		        //Deleted the selected item from the database and model
		        dbm.deleteItem(tModel.getItemAt(modelRow).getItemCode());
		        tModel.removeRow(modelRow);
		    }
		};
		
		//Create an action for adding stock
		Action addStock = new AbstractAction()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	//Find row of the action
		        int modelRow = Integer.valueOf( e.getActionCommand() );
		        //Get the item and open a new dialog using it
		        Item itm = tModel.getItemAt(modelRow);
		        itm = createAddStockDialog(itm);
		        //Update the item in the database and model
	        	dbm.updateItem(itm);
	        	tModel.updateRow(modelRow, itm);
		    }
		};
		
		//Make two of the columns into buttons with the corresponding actions
		ButtonColumn addStockButtonColumn = new ButtonColumn(table, addStock, 7);
		ButtonColumn delButtonColumn = new ButtonColumn(table, delete, 8);
		
		//Right allign these columns (for costs and quantities)
		table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
		table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
		
		//Create a scroll pane to hold the table
		JScrollPane sPane = new JScrollPane();
		sPane.setViewportView(table);
		
		//Create a menu bar
		JMenuBar mBar = new JMenuBar();
		
		//Create an options tab
		JMenu menuOpts = new JMenu("Options");
		
		//Create an "Add Item" option and a listener to it
		JMenuItem menuItm = new JMenuItem("Add Item");
		menuItm.setToolTipText("Add Item");
		menuItm.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//Create an add item dialog box with a blank item
				Item itm = createDialogBox(null);
				if (itm != null) {
					//Add the item to the database and model
					dbm.addItem(itm);
					tModel.addRow(itm);
					tModel.fireTableRowsInserted(tModel.getNumberItems() - 1, tModel.getNumberItems() - 1);
				}
			}
			
		});
		//Add the option to the tab
		menuOpts.add(menuItm);
		//Add the tab to the menu bar
		mBar.add(menuOpts);
		
		//Add the menu bar and the scroll pane to the panel
		add(mBar, BorderLayout.PAGE_START);
		add(sPane, BorderLayout.CENTER);

	}
	
	/**
	 * Set column widths
	 */
	private void setColumnWidths() {
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.getColumnModel().getColumn(0).setMinWidth(130);
		table.getColumnModel().getColumn(1).setMinWidth(255);
		table.getColumnModel().getColumn(2).setMinWidth(35);
		table.getColumnModel().getColumn(3).setMinWidth(80);
		table.getColumnModel().getColumn(4).setMinWidth(95);
		table.getColumnModel().getColumn(5).setMinWidth(90);
		table.getColumnModel().getColumn(6).setMinWidth(150);
		table.getColumnModel().getColumn(7).setMinWidth(100);
		table.getColumnModel().getColumn(8).setMinWidth(65);
	}
	
	/**
	 * Signal that some of the data may have changed
	 */
	public void updateStockLevel() {
		tModel.items = dbm.getItems();
		tModel.fireTableDataChanged();
	}
	
	/**
	 * Create a dialog box
	 * @param itm
	 * @return
	 */
	public Item createDialogBox(Item itm) {
		//Create a blank item
		if (itm == null) {
			itm = new Item("", "", "", 0, "", "", 0);
		}
		
		//Setup textfields and comboboxes
		
		  JTextField iCodeField  = new JTextField(15);
		  iCodeField.setMinimumSize(new Dimension(150, 20));
		  iCodeField.setText(itm.getItemCode());
		  //If the item is not blank do not allow editing of the item code
		  if (!itm.getItemCode().equals(""))
			  iCodeField.setEditable(false);
		  //Add a key listener to restirict the characters to 15
		  iCodeField.addKeyListener(new KeyAdapter() {
			  @Override
			  public void keyTyped(KeyEvent e) {
				  if (iCodeField.getText().length() >= 15)
					  e.consume();
			  }
		  });
		  iCodeField.addAncestorListener(new RequestFocusListener());
		  
	      JTextField descField = new JTextField(30);
	      descField.setText(itm.getDescription());
		  //Add a key listener to restirict the characters to 30
	      descField.addKeyListener(new KeyAdapter() {
			  @Override
			  public void keyTyped(KeyEvent e) {
				  if (descField.getText().length() >= 30)
					  e.consume();
			  }
		  });
	      JTextField unitField  = new JTextField(2);
	      unitField.setText(itm.getUnitMeasure());
	      unitField.setMinimumSize(new Dimension(30, 20));
		  //Add a key listener to restirict the characters to 2
	      unitField.addKeyListener(new KeyAdapter() {
			  @Override
			  public void keyTyped(KeyEvent e) {
				  if (unitField.getText().length() >= 2)
					  e.consume();
			  }
		  });
	      
	      JTextField sLocField  = new JTextField(5);
	      sLocField.setText(itm.getStockLocation());
	      sLocField.setMinimumSize(new Dimension(60, 20));
		  //Add a key listener to restirict the characters to 5
	      sLocField.addKeyListener(new KeyAdapter() {
			  @Override
			  public void keyTyped(KeyEvent e) {
				  if (sLocField.getText().length() >= 5)
					  e.consume();
			  }
		  });
	      JTextField notesField = new JTextField(100);
	      notesField.setText(itm.getNotes());
		  //Add a key listener to restirict the characters to 100
	      notesField.addKeyListener(new KeyAdapter() {
			  @Override
			  public void keyTyped(KeyEvent e) {
				  if (notesField.getText().length() >= 100)
					  e.consume();
			  }
		  });
	
	      //Create a panel with a gridbag layout to hold the fields and boxes
	      JPanel myPanel = new JPanel();
	      myPanel.setPreferredSize(new Dimension(400, 200));
	      myPanel.setLayout(new GridBagLayout());
	      //Create a constraints object to handle the layout of the fields
	      GridBagConstraints c = new GridBagConstraints();
	      
	      //Layout follows: [ Label -> textfield/combobox ]
	     
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
	      myPanel.add(iCodeField, c);
	      
	      c.fill =  GridBagConstraints.HORIZONTAL;
	      c.weightx = 0.1;
	      c.gridx = 0;
	      c.gridy = 1;
	      myPanel.add(new JLabel("Description:"), c);
	      
	      c.fill =  GridBagConstraints.HORIZONTAL;
	      c.weightx = 0.9;
	      c.gridx = 1;
	      c.gridy = 1;
	      myPanel.add(descField, c);
	      
	      c.fill =  GridBagConstraints.HORIZONTAL;
	      c.weightx = 0.1;
	      c.gridx = 0;
	      c.gridy = 2;
	      myPanel.add(new JLabel("Unit Measure:"), c);
	      
	      c.fill =  GridBagConstraints.NONE;
	      c.weightx = 0.9;
	      c.gridx = 1;
	      c.gridy = 2;
	      c.anchor = GridBagConstraints.WEST;
	      myPanel.add(unitField, c);
	      
	      c.fill =  GridBagConstraints.HORIZONTAL;
	      c.weightx = 0.1;
	      c.gridx = 0;
	      c.gridy = 3;
	      myPanel.add(new JLabel("Stock Location:"), c);
	      
	      c.fill =  GridBagConstraints.NONE;
	      c.weightx = 0.9;
	      c.gridx = 1;
	      c.gridy = 3;
	      c.anchor = GridBagConstraints.WEST;
	      myPanel.add(sLocField, c);
	      
	      c.fill =  GridBagConstraints.HORIZONTAL;
	      c.weightx = 0.1;
	      c.gridx = 0;
	      c.gridy = 4;
	      myPanel.add(new JLabel("Notes:"), c);
	      
	      c.fill =  GridBagConstraints.HORIZONTAL;
	      c.weightx = 0.9;
	      c.gridx = 1;
	      c.gridy = 4;
	      myPanel.add(notesField, c);
	
	      //Create a dialogbox using the previously created panel
	      int result = JOptionPane.showConfirmDialog(null, myPanel, 
	               "Please Enter item information", JOptionPane.OK_CANCEL_OPTION);
	      //If the user selects okay, go and create the item
	      if (result == JOptionPane.OK_OPTION)
	    	  return new Item(iCodeField.getText().toUpperCase(), descField.getText(), unitField.getText().toUpperCase(), itm.getQuantity(),
	    			  sLocField.getText().toUpperCase(), notesField.getText(), itm.getCost());
	      else
	    	  return null;
	}
	
	/**
	 * Creates an add stock dialog box
	 * @param itm
	 * @return
	 */
	public Item createAddStockDialog(Item itm) {
		
		//Create all of the textfields and comboboxes and only allow the quantity and cost to be editable
		
		JTextField iCodeField  = new JTextField(15);
		  iCodeField.setMinimumSize(new Dimension(150, 20));
		  iCodeField.setText(itm.getItemCode());
		  iCodeField.setEditable(false);
		  
	      JTextField descField = new JTextField(30);
	      descField.setText(itm.getDescription());
	      descField.setEditable(false);
		
		JTextField quantField = new JTextField(3);
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
					  if (!Pattern.matches("{0,1}[0-9.-]", s))
						  if (!s.equals(".")) 
							  e.consume();
					  if ((s.equals(".") && quantField.getText().contains(".")) || 
							  (s.equals(".") && quantField.getText().length() == 0) || 
							  		(s.equals(".") && quantField.getText().length() == 8))
						  e.consume();
				  }
			  }
		  });
	      
	      JTextField costField = new JTextField(5);
	      costField.setText(Double.toString(itm.getCost()));
	      costField.setMinimumSize(new Dimension(50, 20));
	      //Add a listener to ensure that the cost is formatted correctly
		  //And "." can not be placed in illogical places
	      costField.addKeyListener(new KeyAdapter() {
			  @Override
			  public void keyTyped(KeyEvent e) {
				  if (costField.getText().length() >= 9)
					  e.consume();
				  if (!e.isConsumed()) {
					  String s = Character.toString(e.getKeyChar());
					  if (!Pattern.matches("{0,1}[0-9.]", s))
						  if (!s.equals(".")) 
							  e.consume();
					  String txt = costField.getText();
					  int len = txt.length();
					  if ((s.equals(".") && txt.contains(".")) || 
							  (s.equals(".") && len == 0) || 
							  		(s.equals(".") && len == 8) ||
							  			(len > 4 && txt.charAt(len - 4) == '.'))
						  e.consume();
				  }
			  }
		  });
	      
	      //Create a panel with gridbag layout to hold the textfields
	      JPanel myPanel = new JPanel();
	      myPanel.setPreferredSize(new Dimension(400, 200));
	      myPanel.setLayout(new GridBagLayout());
	      //Use constraints to handle layout on the panel
	      GridBagConstraints c = new GridBagConstraints();
	      
	      //Layout: [ Label -> textfield/combobox ]
	     
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
	      myPanel.add(iCodeField, c);
	      
	      c.fill =  GridBagConstraints.HORIZONTAL;
	      c.weightx = 0.1;
	      c.gridx = 0;
	      c.gridy = 1;
	      myPanel.add(new JLabel("Description:"), c);
	      
	      c.fill =  GridBagConstraints.HORIZONTAL;
	      c.weightx = 0.9;
	      c.gridx = 1;
	      c.gridy = 1;
	      myPanel.add(descField, c);
	      
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
	      
	      c.fill =  GridBagConstraints.HORIZONTAL;
	      c.weightx = 0.1;
	      c.gridx = 0;
	      c.gridy = 3;
	      myPanel.add(new JLabel("Cost:"), c);
	     
	      c.fill =  GridBagConstraints.NONE;
	      c.weightx = 0.9;
	      c.gridx = 1;
	      c.gridy = 3;
	      c.anchor = GridBagConstraints.WEST;
	      myPanel.add(costField, c); 
	      
	      //Create a dialog using the panel
	      int result = JOptionPane.showConfirmDialog(null, myPanel, 
	               "Please Enter item information", JOptionPane.OK_CANCEL_OPTION);
	      //If the user selects OK, go and update the item as required
	      //Calculating the correct new cost
	      if (result == JOptionPane.OK_OPTION) {
	    	  itm.setCost(getNewCost(itm.getCost(), itm.getQuantity(), Double.parseDouble(costField.getText()),
	    			  Double.parseDouble(quantField.getText())));
	      	  itm.setQuantity(itm.getQuantity() + Double.parseDouble(quantField.getText()));
	      }
		
	      return itm;
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
	 * Custom table model class
	 * @author Matt
	 *
	 */
	public class TableItemModel extends AbstractTableModel {

		//Array for the column names
		String[] columnNames = { "Item Code", "Description", "Unit", "Current Stock", "Cost", "Stock Location", "Notes", "", "" };

	    private List<Item> items;
	    
	    /**
	     * Initializes the list of items
	     * @param items
	     */
	    public TableItemModel(List<Item> items) {

	        this.items = new ArrayList<Item>(items);

	    }

	    @Override
	    public int getRowCount() {
	        return items.size();
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
	    	if (col >= 7)
	    		return true;
	    	return false;
	    }
	    
	    @Override
	    public void setValueAt(Object value, int row, int col) {
	    	//Setup values in the model
			 Item item = items.get(row);
			 switch (col) {
				 case 0:
					 item.setItemCode((String)value);
		             break;
		         case 1:
		        	 item.setDescription((String)value);
		             break;
		         case 2:
		        	 item.setUnitMeasure((String)value);
		             break;
		         case 3:
		        	 item.setQuantity(Double.parseDouble((String)value));
		             break;
		         case 4:
		        	 item.setCost(Double.parseDouble((String)value));
		        	 break;
		         case 5:
		        	 item.setStockLocation((String)value);
		             break;
		         case 6:
		        	 item.setNotes((String)value);
		             break;
			 }		 
		    fireTableCellUpdated(row, col);
	  }
	    @Override
	    public Object getValueAt(int rowIndex, int columnIndex) {
	    	//Set value that will be shown in the table
	        Object value = "??";
	        Item item = items.get(rowIndex);
	        switch (columnIndex) {
	            case 0:
	                value = item.getItemCode();
	                break;
	            case 1:
	                value = item.getDescription();
	                break;
	            case 2:
	                value = item.getUnitMeasure();
	                break;
	            case 3:
	                value = formatter.format(round(item.getQuantity(), 3));
	                break;
	            case 4:
	            	value = formatter.format(round(item.getCost(), 3));
	            	break;
	            case 5:
	                value = item.getStockLocation();
	                break;
	            case 6:
	                value = item.getNotes();
	                break;
	            case 7:
	            	value = "Add Stock";
	            	break;
	            case 8:
	            	value = "DEL";
	            	break;
	        }

	        return value;
	    }

	    /**
	     * Add an item to the model
	     * @param itm
	     */
	    public void addRow(Item itm) {
	    	items.add(itm);
	    }
	    
	    /**
	     * Remove an item from the model
	     * @param row
	     */
	    public void removeRow(int row) {
	    	//Create a confirmation dialog
	    	JPanel panel = new JPanel();
	    	panel.add(new JLabel("Are you sure you want to delete this item?"));
	    	int result = JOptionPane.showConfirmDialog(null, panel, 
		               "Confirmation", JOptionPane.YES_NO_OPTION);
		      if (result == JOptionPane.OK_OPTION) {
		    	  items.remove(row);
		    	  this.fireTableRowsDeleted(row, row);
		      }
	    }

	    /**
	     * Update a row in the model
	     * @param row
	     * @param itm
	     */
	    public void updateRow(int row, Item itm) {
	    	items.remove(row);
	    	items.add(row, itm);
	    	this.fireTableRowsUpdated(row, row);
	    }
	    /**
	     * This will return the item at the specified row...
	     * @param row
	     * @return 
	     */
	    public Item getItemAt(int row) {
	        return items.get(row);
	    }
	    
	    public int getNumberItems() {
	    	return items.size();
	    }

	}
	
	/**
	 * Handle rounding for any quantity or costs
	 * @param val
	 * @param places
	 * @return
	 */
	private double round(double val, int places) {
		BigDecimal bd = BigDecimal.valueOf(val);
		//Rounds up
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
}
