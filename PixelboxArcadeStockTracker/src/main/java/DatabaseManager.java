import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

	Connection conn;
	
	/**
	 * Construct the manager
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws SQLException
	 */
	DatabaseManager() throws ClassNotFoundException, IOException, SQLException {
		//Use the stored properties to create a connection to the database
		InputStream stream = DatabaseManager.class.getResourceAsStream("/database.properties");
		ConnectionManager.init(stream);
		conn = ConnectionManager.getConnection();
	}
	
	/**
	 * Handle the retrieval of all data from any specified table
	 * @param table
	 * @return results
	 */
	public List<Item> getItems() {
		List<Item> items = new ArrayList<>();
		try {
			//Create a statement
			Statement st = conn.createStatement();
			//Execute the required query and retrieve the results
			ResultSet rs = st.executeQuery("SELECT * FROM item");
			
			//Process the results row by row
			while (rs.next()) {
				items.add(new Item(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getString(5), rs.getString(6), 
						rs.getDouble(7)));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return items;
	}
	
	public ArrayList<String> getAllICodes() {
		ArrayList<String> iCodes = new ArrayList<String>();
		try {
			//Create a statement
			Statement st = conn.createStatement();
			//Execute the required query and retrieve the results
			ResultSet rs = st.executeQuery("SELECT ItemCode FROM item");
			
			//Process the results row by row
			while (rs.next()) {
				iCodes.add(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return iCodes;
	}
	
	public String getItemDescription(String iCode) {
		String desc = "";
		
		try {
			//Create a statement
			Statement st = conn.createStatement();
			//Execute the required query and retrieve the results
			ResultSet rs = st.executeQuery(String.format("SELECT Descrip FROM item WHERE ItemCode = '%s' LIMIT 1", iCode));
			
			//Process the results row by row
			while (rs.next()) {
				desc = rs.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return desc;
	}
	
	public double getCost(String iCode) {
		double cost = 0;
		
		try {
			//Create a statement
			Statement st = conn.createStatement();
			//Execute the required query and retrieve the results
			ResultSet rs = st.executeQuery(String.format("SELECT Cost FROM item WHERE ItemCode = '%s' LIMIT 1", iCode));
			
			//Process the results row by row
			while (rs.next()) {
				cost = rs.getDouble(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cost;
	}
	
	public double getAvailable(String iCode) {
		double av = 0;
		
		try {
			//Create a statement
			Statement st = conn.createStatement();
			//Execute the required query and retrieve the results
			ResultSet rs = st.executeQuery(String.format("SELECT Quantity FROM item WHERE ItemCode = '%s' LIMIT 1", iCode));
			
			//Process the results row by row
			while (rs.next()) {
				av = rs.getDouble(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return av;
	}
	
	public String getNotes(String iCode) {
		String note = "";
		
		try {
			//Create a statement
			Statement st = conn.createStatement();
			//Execute the required query and retrieve the results
			ResultSet rs = st.executeQuery(String.format("SELECT Notes FROM item WHERE ItemCode = '%s' LIMIT 1", iCode));
			
			//Process the results row by row
			while (rs.next()) {
				note = rs.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return note;
	}
	/**
	 * Add Item to database
	 * @param itm
	 */
	public void addItem(Item itm) {
		try {
			String command = "INSERT INTO item VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(command);
			
			//Input parameters into statement
			ps.setString(1, itm.getItemCode());
			ps.setString(2, itm.getDescription());
			ps.setString(3, itm.getUnitMeasure());
			ps.setDouble(4, itm.getQuantity());
			ps.setString(5, itm.getStockLocation());
			ps.setString(6, itm.getNotes());
			ps.setDouble(7, itm.getCost());
			
			//Execute the command
			ps.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateItem(Item itm) {
		try {
			//Create command string
			String command = String.format("UPDATE item SET Descrip = '%s', UnitMeasure = '%s', Quantity = '%.2f', "
					+ "StockLocation = '%s', Notes = '%s', Cost = %.2f WHERE ItemCode = '%s';", itm.getDescription(), 
					itm.getUnitMeasure(),itm.getQuantity(), itm.getStockLocation(), itm.getNotes(), itm.getCost(), itm.getItemCode());
			Statement st = conn.createStatement();
			//Execute the command
			st.execute(command);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteItem(String itmCode) {
		try {
			//Create command string
			String command = String.format("DELETE FROM item WHERE ItemCode = '%s';", itmCode);
			Statement st = conn.createStatement();
			//Execute the command
			st.execute(command);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addStock(String iCode, double quant, double cost) {
		try {
			//Create command string
			String command = String.format("UPDATE item SET Quantity = %.2f, Cost = %.2f WHERE ItemCode = '%s';", 
					quant, cost, iCode);
			Statement st = conn.createStatement();
			//Execute the command
			st.execute(command);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Handle the retrieval of all data from any specified table
	 * @param table
	 * @return results
	 */
	public List<BOM> getBOMS() {
		List<BOM> boms = new ArrayList<>();
		try {
			//Create a statement
			Statement st = conn.createStatement();
			//Execute the required query and retrieve the results
			ResultSet rs = st.executeQuery("SELECT * FROM billofmaterial");
			
			//Process the results row by row
			while (rs.next()) {
				boms.add(new BOM(rs.getString(1), rs.getString(2)));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return boms;
	}
	
	public List<String> getAllBillIDs() {
		List<String> ids = new ArrayList<>();
		try {
			//Create a statement
			Statement st = conn.createStatement();
			//Execute the required query and retrieve the results
			ResultSet rs = st.executeQuery("SELECT BillID FROM billofmaterial");
			
			//Process the results row by row
			while (rs.next()) {
				ids.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ids;
	}
			
	
	/**
	 * Handle the retrieval of all data from any specified table
	 * @param table
	 * @return results
	 */
	public void addBOM(BOM bom) {
		try {
			String command = "INSERT INTO billofmaterial VALUES ( ?, ? )";
			PreparedStatement ps = conn.prepareStatement(command);
			
			//Input parameters into statement
			ps.setString(1, bom.getBillID());
			ps.setString(2, bom.getDescription());
			//Execute the command
			ps.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateBOM(BOM bom) {
		try {
			//Create command string
			String command = String.format("UPDATE billofmaterial SET Descrip = '%s' WHERE BillID = %d", bom.getDescription(), 
					bom.getBillID());
			Statement st = conn.createStatement();
			//Execute the command
			st.execute(command);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteBOM(String billID) {
		try {
			//Create command string
			String command = String.format("DELETE FROM billofmaterial WHERE BillID = '%s';", billID);
			Statement st = conn.createStatement();
			//Execute the command
			st.execute(command);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Handle the retrieval of all data from any specified table
	 * @param table
	 * @return results
	 */
	public List<BOMD> getBOMDS(String billID) {
		List<BOMD> bomds = new ArrayList<>();
		try {
			//Create a statement
			Statement st = conn.createStatement();
			//Execute the required query and retrieve the results
			ResultSet rs = st.executeQuery(String.format("SELECT bomd.BillID, bomd.ItemCode, i.Descrip, "
					+ "bomd.Quantity, i.Quantity FROM billofmaterialdetail bomd, item i WHERE bomd.BillID = '%s' "
					+ "AND i.ItemCode = bomd.ItemCode;",
					billID));
			
			//Process the results row by row
			while (rs.next()) {
				bomds.add(new BOMD(rs.getString(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getDouble(5)));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bomds;
	}
	
	/**
	 * Handle the retrieval of all data from any specified table
	 * @param table
	 * @return results
	 */
	public void addBOMD(BOMD bomd) {
		try {
			String command = "INSERT INTO billofmaterialdetail VALUES ( ?, ?, ? )";
			PreparedStatement ps = conn.prepareStatement(command);
			
			//Input parameters into statement
			ps.setString(1, bomd.getBillID());
			ps.setString(2, bomd.getItemCode());
			ps.setDouble(3, bomd.getQuantity());
			//Execute the command
			ps.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateBOMD(BOMD bomd) {
		try {
			//Create command string
			String command = String.format("UPDATE billofmaterialdetail SET Quantity = '%.2f' WHERE BillID = '%s' AND ItemCode = '%s'",
					bomd.getQuantity(), bomd.getBillID(), bomd.getItemCode());
			Statement st = conn.createStatement();
			//Execute the command
			st.execute(command);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteBOMD(String billID, String iCode) {
		try {
			//Create command string
			String command = String.format("DELETE FROM billofmaterialdetail WHERE BillID = '%s' AND ItemCode = '%s';", billID, iCode);
			Statement st = conn.createStatement();
			//Execute the command
			st.execute(command);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteAllBOMD(String billID) {
		try {
			//Create command string
			String command = String.format("DELETE FROM billofmaterialdetail WHERE BillID = '%s';", billID);
			Statement st = conn.createStatement();
			//Execute the command
			st.execute(command);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<Order> getAllOrders() {
		List<Order> orders = new ArrayList<>();
		try {
			//Create a statement
			Statement st = conn.createStatement();
			Statement st2 = conn.createStatement();
			//Execute the required query and retrieve the results
			ResultSet rs = st.executeQuery("SELECT * FROM OrderHeader");
			
			//Process the results row by row
			while (rs.next()) {
				ResultSet rs2 = st2.executeQuery(String.format("SELECT SUM(i.Cost) FROM OrderDetail od, Item i "
						+ "WHERE od.OrderNumber = %d AND "
						+ "od.ItemCode = i.ItemCode", rs.getInt(1)));
				if (rs2.next())
					orders.add(new Order(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), 
							rs.getString(5), rs.getString(6), rs.getDate(7), rs.getDate(8), rs.getDate(9), 
							rs.getString(10), rs2.getDouble(1)));
				else
					orders.add(new Order(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), 
							rs.getString(5), rs.getString(6), rs.getDate(7), rs.getDate(8), rs.getDate(9), 
							rs.getString(10), 0));

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return orders;
	}
	
	public void addOrder(Order order) {
		try {
			String command = "INSERT INTO OrderHeader VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
			PreparedStatement ps = conn.prepareStatement(command);
			
			//Input parameters into statement
			ps.setInt(1, order.getOrderNumber());
			ps.setString(2, order.getAddress1());
			ps.setString(3, order.getAddress2());
			ps.setString(4, order.getAddress3());
			ps.setString(5, order.getAddress4());
			ps.setString(6,  order.getPostcode());
			ps.setDate(7, order.getOrderDate());
			ps.setDate(8, order.getPromiseDate());
			ps.setDate(9, order.getDeliveryDate());
			ps.setString(10, order.getNotes());
			//Execute the command
			ps.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateOrder(Order order) {
		try {
			//Create command string
			String command = String.format("UPDATE OrderHeader SET CustomerAddress1 = '%s', CustomerAddress2 = '%s',"
					+ " CustomerAddress3 = '%s', CustomerAddress4 = '%s', CustomerPostcode = '%s', OrderDate = '%s'"
					+ " PromiseDate = '%s', DeliveryDate = '%s', Notes = '%s' WHERE OrderNumber = %d;",
					order.getAddress1(), order.getAddress2(), order.getAddress3(), order.getAddress4(), order.getPostcode(),
					order.getOrderDate().toString(), order.getPromiseDate().toString(), order.getDeliveryDate().toString(),
					order.getOrderNumber());
			Statement st = conn.createStatement();
			//Execute the command
			st.execute(command);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteOrder(int orderNumber) {
		try {
			//Create command string
			String command = String.format("DELETE FROM OrderHeader WHERE OrderNumber = %d;", orderNumber);
			Statement st = conn.createStatement();
			//Execute the command
			st.execute(command);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<OrderDetail> getAllOrderDetails(int orderNumber) {
		List<OrderDetail> details = new ArrayList<>();
		try {
			//Create a statement
			Statement st = conn.createStatement();
			//Execute the required query and retrieve the results
			ResultSet rs = st.executeQuery(String.format("SELECT od.*, i.Descrip, i.Cost FROM orderdetail od, "
					+ "Item i WHERE od.OrderNumber = %d AND od.ItemCode = i.ItemCode;", orderNumber));
			
			//Process the results row by row
			while (rs.next()) {
				details.add(new OrderDetail(rs.getInt(1), rs.getString(2),  rs.getDouble(3), rs.getString(4), rs.getDouble(5)));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return details;
	}
	
	public void addOrderDetail(OrderDetail details) {
		try {
			String command = "INSERT INTO OrderDetail VALUES ( ?, ?, ? )";
			PreparedStatement ps = conn.prepareStatement(command);
			
			//Input parameters into statement
			ps.setInt(1, details.getOrderNumber());
			ps.setString(2, details.getItemCode());
			ps.setDouble(3, details.getQuantity());
			//Execute the command
			ps.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateOrderDetail(OrderDetail details) {
		try {
			//Create command string
			String command = String.format("UPDATE OrderDetail SET Quantity = %.2f" +
					"WHERE OrderNumber = %d AND ItemCode = '%s';",
					 details.getQuantity(), details.getOrderNumber(), details.getItemCode());
			Statement st = conn.createStatement();
			//Execute the command
			st.execute(command);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteOrderDetail(int orderNumber, String iCode) {
		try {
			//Create command string
			String command = String.format("DELETE FROM OrderDetail WHERE OrderNumber = %d AND ItemCode = '%s';", orderNumber, iCode);
			Statement st = conn.createStatement();
			//Execute the command
			st.execute(command);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteAllOrderDetail(int orderNumber) {
		try {
			//Create command string
			String command = String.format("DELETE FROM OrderDetail WHERE OrderNumber = %d;", orderNumber);
			Statement st = conn.createStatement();
			//Execute the command
			st.execute(command);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void reduceStock(String itemCode, double reduction) {
		try {
			//Create command string
			String command = String.format("UPDATE Item SET Quantity = Quantity - %.2f WHERE ItemCode = '%s' "
					+ "AND Quantity - %.2f >= 0;",
					 reduction, itemCode, reduction);
			Statement st = conn.createStatement();
			//Execute the command
			st.execute(command);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
