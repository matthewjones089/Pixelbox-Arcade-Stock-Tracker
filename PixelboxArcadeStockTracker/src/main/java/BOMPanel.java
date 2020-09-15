import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

public class BOMPanel extends JPanel {

	TableItemModel tModel;
	JTable table;
	DatabaseManager dbm;
	private List<ListenerInterface> listeners = new ArrayList<ListenerInterface>();
	
	BOMPanel(DatabaseManager dbManager, int w, int h) {
		//Holds a local instance of database manager
		dbm = dbManager;
		//Setup panel with a border layout
		setPreferredSize(new Dimension(w, h));
		setLayout(new BorderLayout());
		
		//Create instance of custom table model using the Bill of Materials from the database
		tModel = new TableItemModel(dbm.getBOMS());
		//Crate table using model with standard row sorter
		table = new JTable(tModel);
		table.setAutoCreateRowSorter(true);

		//Set widths
		setColumnWidths();

		//Add mouse listener
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//Listens for double click
				if (e.getClickCount() > 1)
					notifyDoubleClick();
				
			}
		});
		
		//Create an action for deleting
		Action delete = new AbstractAction()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	//Gets the bom to be deleted and deletes from database and model
		        int modelRow = Integer.valueOf( e.getActionCommand() );
		        BOM bom = tModel.getBOMAt(modelRow);
		        //Deletes all of the details for the selected Bill as well
		        dbm.deleteAllBOMD(bom.getBillID());
		        dbm.deleteBOM(bom.getBillID());
		        tModel.removeRow(modelRow);
		        //Notifies of the delete in order to delete the detail page too
		        notifyDelete(bom.getBillID());
		    }
		};
		
		//Create an action for updating
		Action update = new AbstractAction()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	//Gets the bom to be updated and opens a dialog box
		        int modelRow = Integer.valueOf( e.getActionCommand() );
		        BOM bom = tModel.getBOMAt(modelRow);
		        bom = createDialogBox(bom);
		        if (bom != null)  {
		        	//Updates the bom from the database
		        	dbm.updateBOM(bom);
		        	//Updates the model
		        	tModel.updateRow(modelRow, bom);
		        }
		    }
		};
		
		//Make two columns into button columns and add the previously created actions to them
		ButtonColumn updButtonColumn = new ButtonColumn(table, update, 2);
		ButtonColumn delButtonColumn = new ButtonColumn(table, delete, 3);
		
		//Creates a scroll pane with the table as view port
		JScrollPane sPane = new JScrollPane();
		sPane.setViewportView(table);
		
		//Creates a menu bar
		JMenuBar mBar = new JMenuBar();
		//Creates a menu options tab
		JMenu menu = new JMenu("Options");
		
		//Creates an "Add Item" option with tooltip text
		JMenuItem menuItm = new JMenuItem("Add BOM");
		menuItm.setToolTipText("Add Bill of Material");
		menuItm.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//Creates a dialog box
				BOM bom = createDialogBox(null);
				if (bom != null) {
					//Adds the bom to the database
					dbm.addBOM(bom);
					//Updates the model
					tModel.addRow(bom);
					tModel.fireTableRowsInserted(tModel.getNumberItems() - 1, tModel.getNumberItems() - 1);
				}
			}
			
		});
		//Adds to menu tab
		menu.add(menuItm);
		//Adds tab to menu bar
		mBar.add(menu);
		
		//Add menu bar and scroll pane to the panel
		add(mBar, BorderLayout.PAGE_START);
		add(sPane, BorderLayout.CENTER);
	}
	
	/**
	 * Sets column widths
	 */
	private void setColumnWidths() {
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.getColumnModel().getColumn(0).setPreferredWidth(150);
		table.getColumnModel().getColumn(1).setPreferredWidth(650);
		table.getColumnModel().getColumn(2).setPreferredWidth(100);
		table.getColumnModel().getColumn(3).setPreferredWidth(100);
	}
	
	/**
	 * Handles adding of listeners to this class
	 * @param listener
	 */
	public void addListener(ListenerInterface listener) {
		listeners.add(listener);
	}
	
	/**
	 * Gets the currently selected bill id
	 * @return
	 */
	public String getSelectedBillID() {
		return tModel.getBOMAt(table.getSelectedRow()).getBillID();
	}
	
	/**
	 * Get the database manager
	 * @return
	 */
	public DatabaseManager getDBM() {
		return dbm;
	}

	/**
	 * Notify the classes listeners of a double click
	 */
	public void notifyDoubleClick() {
		for (ListenerInterface li : listeners) {
			li.doubleClick(getSelectedBillID());
		}
	}
	
	/**
	 * Notify the classes listeners of a deletion
	 */
	private void notifyDelete(String tabName) {
		for (ListenerInterface li : listeners) {
			li.notifyDelete(tabName);
		}
	}
	
	/**
	 * Create dialog box
	 * @param bom
	 * @return
	 */
	public BOM createDialogBox(BOM bom) {
		//If a null bom is passed in then a new bom is initialized
		if (bom == null) {
			bom = new BOM("", "");
		}
		
		//Creates textfields and comboboxes for the dialog box
		
		JTextField billIDField  = new JTextField(5);
	      billIDField.setText(bom.getBillID());
	      billIDField.setMinimumSize(new Dimension(60, 20));
	      //Add a key listener to stop the user inputting more than 15 characters
	      billIDField.addKeyListener(new KeyAdapter() {
	    	  @Override
				public void keyTyped(KeyEvent e) {
					String s = Character.toString(e.getKeyChar());
					if (billIDField.getText().length() >= 15)					 
						e.consume();
				}
		  });
	      //Set focus to this
	      billIDField.addAncestorListener(new RequestFocusListener());
		
	  JTextField descField = new JTextField(30);
	  descField.setText(bom.getDescription());
	  //Add listener to stop the user entering more than 30 characters
	  descField.addKeyListener(new KeyAdapter() {
		  @Override
		  public void keyTyped(KeyEvent e) {
			  if (descField.getText().length() >= 30)
				  e.consume();
		  }
	  });
	
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
	  myPanel.add(new JLabel("Description:"), c);
	  
	  c.fill =  GridBagConstraints.HORIZONTAL;
	  c.weightx = 0.9;
	  c.gridx = 1;
	  c.gridy = 1;
	  myPanel.add(descField, c);      
	
		//Creates the dialog box using the previous panel
	  int result = JOptionPane.showConfirmDialog(null, myPanel, 
	           "Please Enter item information", JOptionPane.OK_CANCEL_OPTION);
		//If OK is selected a bom with the input attributes is created and returned
	  if (result == JOptionPane.OK_OPTION)
		  return new BOM(billIDField.getText().toUpperCase(), descField.getText());
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
		String[] columnNames = { "Bill ID", "Description", "", "" };

	    private List<BOM> boms;
	    
	    //Initializes list
	    public TableItemModel(List<BOM> boms) {

	        this.boms = new ArrayList<BOM>(boms);

	    }

	    @Override
	    public int getRowCount() {
	        return boms.size();
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
	    	if (col == 2 || col == 3)
	    		return true;
	    	return false;
	    }
	    
	    @Override
	    public void setValueAt(Object value, int row, int col) {
	    	//Setup values in the model
			 BOM bom = boms.get(row);
			 switch (col) {
				 case 0:
					 bom.setBillID((String)value);
		             break;
		         case 1:
		        	 bom.setDescription((String)value);
		             break;
			 }		 
		    fireTableCellUpdated(row, col);
	  }
	    @Override
	    public Object getValueAt(int rowIndex, int columnIndex) {
	    	//Set value that will be shown on the table
	        Object value = "??";
	        BOM bom = boms.get(rowIndex);
	        switch (columnIndex) {
	            case 0:
	            	value = bom.getBillID();
	                break;
	            case 1:
	                value = bom.getDescription();
	                break;
	            case 2:
	            	value = "UPD";
	            	break;
	            case 3:
	            	value = "DEL";
	            	break;
	        }

	        return value;
	    }

	    public void addRow(BOM bom) {
	    	boms.add(bom);
	    }
	    
	    public void removeRow(int row) {
	    	JPanel panel = new JPanel();
	    	//Asks the user to confirm they wish to delete the bom
	    	panel.add(new JLabel("Are you sure you want to delete this bom?"));
	    	int result = JOptionPane.showConfirmDialog(null, panel, 
		               "Confirmation", JOptionPane.YES_NO_OPTION);
		      if (result == JOptionPane.OK_OPTION) {
		    	  boms.remove(row);
		    	  this.fireTableRowsDeleted(row, row);
		      }
	    }

	    /**
	     * Updates a specified bom
	     * @param row
	     * @param bom
	     */
	    public void updateRow(int row, BOM bom) {
	    	//Removes and re adds the updated bom
	    	boms.remove(row);
	    	boms.add(row, bom);
	    	//Updates the model
	    	this.fireTableRowsUpdated(row, row);
	    }
	    /**
	     * This will return the bom at the specified row...
	     * @param row
	     * @return 
	     */
	    public BOM getBOMAt(int row) {
	        return boms.get(row);
	    }
	    
	    public int getNumberItems() {
	    	return boms.size();
	    }

	}
}
