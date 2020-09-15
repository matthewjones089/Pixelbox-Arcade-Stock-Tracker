import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
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
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;


public class BOMDPanel extends JPanel {

	private DatabaseManager dbm;
	private JTable table;
	private NumberFormat formatter = new DecimalFormat("#0.000");

	
	public BOMDPanel(DatabaseManager dBM, String billID, int w, int h) {
		//Hold locally the database manager
		dbm = dBM;
		//Set the size of the panel and give it a border layout
		setPreferredSize(new Dimension(w, h));
		setLayout(new BorderLayout());
		
		//Create a table cell renderer in order to right allign some of the columns contents
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		
		//Create a table model using the custom model, passing in the list of bills
		TableItemModel tModel = new TableItemModel(dbm.getBOMDS(billID));
		//Create the table using the model
		table = new JTable(tModel);
		//Allow the table to use the standard sorter
		table.setAutoCreateRowSorter(true);
		//Customly render the 3rd column
		table.getColumnModel().getColumn(2).setCellRenderer(new MyTableRenderer());
		
		//Set all of the column widths
		setColumnWidths();
		
		//Add a mouse listener to the table
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//If a row is double clicked open an update dialog box
				if (e.getClickCount() > 1) {
					//Get the row selected
					int modelRow = table.getSelectedRow();
					//Convert the row incase the table has been sorted
			        int row = table.convertRowIndexToView(modelRow);
			        //Get the Bill of Material Detail at that row
					BOMD bomd = tModel.getBOMDAt(row);
					//Create a dialog
			        bomd = createDialogBox(bomd, billID);
			        //As long as the returned bomd is not nul
			        if (bomd != null)  {
			        	//Update the bomd in the database
			        	dbm.updateBOMD(bomd);
			        	//And update in the model
			        	tModel.updateRow(row, bomd);
			        }
				}
			}
		});
		
		//Create an action for deleting
		Action delete = new AbstractAction()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	//Gets the bomd to be deleted and deletes from database and model
		        int modelRow = Integer.valueOf( e.getActionCommand() );
		        BOMD bomd = tModel.getBOMDAt(modelRow);
		        dbm.deleteBOMD(bomd.getBillID(), bomd.getItemCode());
		        tModel.removeRow(modelRow);
		    }
		};
		
		//Create an action for opening a web page
		Action openPage = new AbstractAction() {
			public void actionPerformed(ActionEvent e)
		    {
				//Get the bomd to find the link from
		        int modelRow = Integer.valueOf( e.getActionCommand() );
		        BOMD bomd = tModel.getBOMDAt(modelRow);
		        //Go and get the link from the database
		        String link = dbm.getNotes(bomd.getItemCode());
		        
		        //Open the link
				openWebPage(link);
		    }
		};
		
		//Make two columns into button columns and add the previously created actions to them
		ButtonColumn webButtonColumn = new ButtonColumn(table, openPage, 5);
		ButtonColumn delButtonColumn = new ButtonColumn(table, delete, 6);
		
		//Make some columns right alligned
		//e.g. cost 
		table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
		table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
		table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
		
		//Create a scroll pane
		JScrollPane sPane = new JScrollPane();
		//Set the viewport to the table
		sPane.setViewportView(table);
		
		//Create a Menu Bar
		JMenuBar mBar = new JMenuBar();
		
		//Create an options menu
		JMenu menu = new JMenu("Options");
		
		//Create an "Add Item" menu option and set tooltip text
		JMenuItem menuItm = new JMenuItem("Add Item");
		menuItm.setToolTipText("Add Item to Bill of Material");
		//Add listener to this option
		menuItm.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//Create a dialog box 
				BOMD bomd = createDialogBox(null, billID);
				if (bomd != null) {
					//Add a bomd to the database
					dbm.addBOMD(bomd);
					//And a row in the model
					tModel.addRow(bomd);
					//Upate the model
					tModel.fireTableRowsInserted(tModel.getNumberItems() - 1, tModel.getNumberItems() - 1);
				}
			}
			
		});
		//Create a "View Cost" menu option and set tooltip text
		JMenuItem viewCost = new JMenuItem("View Total Cost");
		viewCost.setToolTipText("View Total Cost of Bill");
		//Add listener to this option
		viewCost.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//Show dialog box displaying total cost
				JOptionPane.showConfirmDialog(null, getTotalCost(tModel.getBOMDS()), 
						"Please Enter item information", JOptionPane.OK_CANCEL_OPTION);
			}
			
		});
		//Add options to the menu
		menu.add(menuItm);
		menu.add(viewCost);
		//Add the menu to the menu bar
		mBar.add(menu);
		
		//Add the menu bar and the scroll pane to the panel
		add(mBar, BorderLayout.NORTH);
		add(sPane, BorderLayout.CENTER);
	}
	
	/**
	 * Calculates the total cost and puts into a string
	 * @param bomds
	 * @return
	 */
	private String getTotalCost(List<BOMD> bomds) {
		double totCost = 0;
		
		//Loops through all bomds to calculate costs
		for (BOMD bomd : bomds) {
			totCost += bomd.getQuantity() * dbm.getCost(bomd.getItemCode());
		}
		
		return "Total Cost: " + totCost;
	}
	
	/**
	 * Opens a specified link
	 * @param link
	 */
	private void openWebPage(String link) {
		//Gets the current runtime
		Runtime rt = Runtime.getRuntime();
		try {
			//Executes the link through google chrome in the runtime
			rt.exec(new String[] {"C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe", link});
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Sets column widths
	 */
	private void setColumnWidths() {
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.getColumnModel().getColumn(0).setPreferredWidth(170);
		table.getColumnModel().getColumn(1).setPreferredWidth(400);
		table.getColumnModel().getColumn(2).setPreferredWidth(80);
		table.getColumnModel().getColumn(3).setPreferredWidth(80);
		table.getColumnModel().getColumn(4).setPreferredWidth(70);
		table.getColumnModel().getColumn(5).setPreferredWidth(100);
		table.getColumnModel().getColumn(6).setPreferredWidth(100);
	}
	
	/**
	 * Creates a dialog box
	 * @param bomd
	 * @param billID
	 * @return
	 */
	public BOMD createDialogBox(BOMD bomd, String billID) {
		//If a null bomd is passed in then a new bomd is initialized
		if (bomd == null) {
			bomd = new BOMD(billID, "", "", 0, 0);
		}

		//Creates textfields and comboboxes for the dialog box
		JTextField billIDField  = new JTextField(5);
		billIDField.setText(billID);
		billIDField.setMinimumSize(new Dimension(150, 20));
		billIDField.setEditable(false);

		JTextField descField = new JTextField(30);
		descField.setText(bomd.getDescription());
		descField.setEditable(false);
		
		//Create a combo box model with all of the Item Codes
		DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<Object>((Object[]) dbm.getAllICodes().toArray());
		//Sets the initially selected item to the passed in Item Code
		model.setSelectedItem(bomd.getItemCode());
		//Uses the model for the combo box
		JComboBox<Object> iCodeBox = new JComboBox<Object>(model);
		iCodeBox.setMinimumSize(new Dimension(150, 20));
		//Make the combo box not editable
		iCodeBox.setEditable(false);
		//Add an item listener
		iCodeBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				//If the selected item is changed, set the text of the description textfield
				//By getting the relevant description from the database
				if (e.getStateChange() == ItemEvent.SELECTED) {
					descField.setText(dbm.getItemDescription(model.getSelectedItem().toString()));
				}
			}
			
		});
		//Set selected item to the current bomds Item Code
		iCodeBox.setSelectedItem(bomd.getItemCode());
		
		JTextField quantField = new JTextField(3);
		quantField.setText(Double.toString(bomd.getQuantity()));
		quantField.setMinimumSize(new Dimension(50, 20));
		//Add a key listener
		quantField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				//Does not allow a value of more than 9 characters long
				if (quantField.getText().length() >= 9)
					e.consume();
				if (!e.isConsumed()) {
					String s = Character.toString(e.getKeyChar());
					//Does not allow characters other than "0-9" or "."
					if (!Pattern.matches("{0,1}[0-9.]", s))
						if (!s.equals(".")) 
							e.consume();
					//Ensures "." can only be put into sensible places
					if ((s.equals(".") && quantField.getText().contains(".")) || 
							(s.equals(".") && quantField.getText().length() == 0) || 
							(s.equals(".") && quantField.getText().length() == 8))
						e.consume();
				}
			}
		});
		//Sets focus to this
		quantField.addAncestorListener(new RequestFocusListener());

		//Creates a panel with a gridbag layout
		JPanel myPanel = new JPanel();
		myPanel.setPreferredSize(new Dimension(400, 200));
		myPanel.setLayout(new GridBagLayout());
		//Creates some gridbag constraints to handle layout on the panel
		GridBagConstraints c = new GridBagConstraints();

		//Handles the layout [ Label -> TextField/Combobox ]
		
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
		myPanel.add(billIDField, c);

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
		myPanel.add(new JLabel("Description:"), c);

		c.fill =  GridBagConstraints.HORIZONTAL;
		c.weightx = 0.9;
		c.gridx = 1;
		c.gridy = 2;
		myPanel.add(descField, c);

		c.fill =  GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.gridx = 0;
		c.gridy = 3;
		myPanel.add(new JLabel("Quantity:"), c);

		c.fill =  GridBagConstraints.NONE;
		c.weightx = 0.9;
		c.gridx = 1;
		c.gridy = 3;
		c.anchor = GridBagConstraints.WEST;
		myPanel.add(quantField, c);



		//Creates the dialog box using the previous panel
		int result = JOptionPane.showConfirmDialog(null, myPanel, 
				"Please Enter item information", JOptionPane.OK_CANCEL_OPTION);
		//If OK is selected a bomd with the input attributes is created and returned
		if (result == JOptionPane.OK_OPTION)
			return new BOMD(billID, model.getSelectedItem().toString(), descField.getText(), 
					Double.parseDouble(quantField.getText()), dbm.getAvailable(model.getSelectedItem().toString().toUpperCase()));
		else
			return null;
	}
	
	/**
	 * Custom Table Model
	 * @author Matt
	 *
	 */
	public class TableItemModel extends AbstractTableModel {

		//Array of column headers
		String[] columnNames = { "Item Code", "Description", "Required", "Available", "Total Cost", "", "" };

	    private List<BOMD> bomds;
	    
	    //Initializes list
	    public TableItemModel(List<BOMD> bomds) {

	        this.bomds = new ArrayList<BOMD>(bomds);

	    }

	    @Override
	    public int getRowCount() {
	        return bomds.size();
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
	    	if (col >= 5)
	    		return true;
	    	return false;
	    }
	    
	    @Override
	    public void setValueAt(Object value, int row, int col) {
	    	//Setup values in the model
			 BOMD bomd = bomds.get(row);
			 switch (col) {
		         case 0:
		        	 bomd.setItemCode((String)value);
		             break;
		         case 1:
		        	 bomd.setDescription((String)value);
		        	 break;
		         case 2:
		        	 bomd.setQuantity(Double.parseDouble((String)value));
		             break;
		         case 3:
		        	 bomd.setAvailable(Double.parseDouble((String)value));
		        	 break;
			 }		 
		    fireTableCellUpdated(row, col);
	  }
	    @Override
	    public Object getValueAt(int rowIndex, int columnIndex) {
	    	//Set value that will be shown on the table
	        Object value = "??";
	        BOMD bomd = bomds.get(rowIndex);
	        switch (columnIndex) {
	            case 0:
	            	value = bomd.getItemCode();
	                break;
	            case 1:
	            	value = bomd.getDescription();
	            	break;
	            case 2:
	                value = formatter.format(round(bomd.getQuantity(), 3));
	                break;
	            case 3:
	            	value = formatter.format(round(bomd.getAvailable(), 3));
	            	break;
	            case 4:
	            	value = formatter.format(round(bomd.getQuantity() * dbm.getCost(bomd.getItemCode()), 3));
	            	break;
	            case 5:
	            	value = "+";
	            	break;
	            case 6:
	            	value = "DEL";
	            	break;
	        }

	        return value;
	    }

	    /**
	     * Add a bomd
	     * @param bomd
	     */
	    public void addRow(BOMD bomd) {
	    	bomds.add(bomd);
	    }
	    
	    /**
	     * Remove a specified bomd
	     * @param row
	     */
	    public void removeRow(int row) {
	    	JPanel panel = new JPanel();
	    	//Asks the user to confirm they wish to delete the bomd
	    	panel.add(new JLabel("Are you sure you want to delete this bomd?"));
	    	int result = JOptionPane.showConfirmDialog(null, panel, 
		               "Confirmation", JOptionPane.YES_NO_OPTION);
		      if (result == JOptionPane.OK_OPTION) {
		    	  bomds.remove(row);
		    	  //Updates the model
		    	  this.fireTableRowsDeleted(row, row);
		      }
	    }
	    
	    /**
	     * Updates a specified bomd
	     * @param row
	     * @param bomd
	     */
	    public void updateRow(int row, BOMD bomd) {
	    	//Removes and re adds the updated bomd
	    	bomds.remove(row);
	    	bomds.add(row, bomd);
	    	//updates the model
	    	this.fireTableRowsUpdated(row, row);
	    }
	    /**
	     * This will return the bomd at the specified row...
	     * @param row
	     * @return 
	     */
	    public BOMD getBOMDAt(int row) {
	        return bomds.get(row);
	    }
	    
	    public int getNumberItems() {
	    	return bomds.size();
	    }
	    
	    /**
	     * Checks if there is enough stock to meet an order
	     * @param row
	     * @return
	     */
	    public boolean isEnoughStock(int row) {
	    	BOMD bomd = getBOMDAt(row);
	    	
	    	//Compares the quantity required to the available stock
	    	if (bomd.getQuantity() > bomd.getAvailable())
	    		return false;
	    	
	    	return true;
	    }
	    
	    public List<BOMD> getBOMDS() {
	    	return bomds;
	    }

	}
	
	/**
	 * Custom table cell renderer
	 * @author Matt
	 *
	 */
	public class MyTableRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			
			JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			//Gets the tabels model
			TableItemModel model = (TableItemModel) table.getModel();
			//If there is not enough stock, the column is made red
			if (!model.isEnoughStock(row))
				l.setBackground(Color.RED);
			//If the row is simpl selected and there is enough stock
			//Set the colour to the standard selection colour
			else if (isSelected)
				l.setBackground(Color.decode("#b8cfe5"));
			else
				l.setBackground(Color.WHITE);

			return l;
		}
	}
	
	/**
	 * A method to round quantites and costs
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
