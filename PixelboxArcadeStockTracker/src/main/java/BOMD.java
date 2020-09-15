
public class BOMD {

	private String billID;
	private String description;
	private String itemCode;
	private double quantity;
	private double available;
	/**
	 * @param billID
	 * @param itemCode
	 * @param description
	 * @param quantity
	 * @param available
	 */
	public BOMD(String billID, String itemCode, String description, double quantity, double available) {
		super();
		this.billID = billID;
		this.itemCode = itemCode;
		this.description = description;
		this.quantity = quantity;
		this.available = available;
	}
	/**
	 * @return the billID
	 */
	public String getBillID() {
		return billID;
	}
	/**
	 * @param billID the billID to set
	 */
	public void setBillID(String billID) {
		this.billID = billID;
	}
	/**
	 * @return the itemCode
	 */
	public String getItemCode() {
		return itemCode;
	}
	/**
	 * @param itemCode the itemCode to set
	 */
	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the quantity
	 */
	public double getQuantity() {
		return quantity;
	}
	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}
	/**
	 * @return the available
	 */
	public double getAvailable() {
		return available;
	}
	/**
	 * @param available the available to set
	 */
	public void setAvailable(double available) {
		this.available = available;
	}
}
