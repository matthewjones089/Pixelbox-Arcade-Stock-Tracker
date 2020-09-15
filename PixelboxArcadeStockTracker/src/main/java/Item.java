
public class Item {

	private String itemCode;
	private String description;
	private String unitMeasure;
	private double quantity;
	private String stockLocation;
	private String notes;
	private double cost;

	
	/**
	 * @param itemCode
	 * @param description
	 * @param unitMeasure
	 * @param quantity
	 * @param stockLocation
	 * @param notes
	 * @param cost
	 */
	public Item(String itemCode, String description, String unitMeasure, double quantity, String stockLocation,
			String notes, double cost) {
		super();
		this.itemCode = itemCode;
		this.description = description;
		this.unitMeasure = unitMeasure;
		this.quantity = quantity;
		this.stockLocation = stockLocation;
		this.notes = notes;
		this.cost = cost;
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
	 * @return the unitMeasure
	 */
	public String getUnitMeasure() {
		return unitMeasure;
	}
	/**
	 * @param unitMeasure the unitMeasure to set
	 */
	public void setUnitMeasure(String unitMeasure) {
		this.unitMeasure = unitMeasure;
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
	 * @return the stockLocation
	 */
	public String getStockLocation() {
		return stockLocation;
	}
	/**
	 * @param stockLocation the stockLocation to set
	 */
	public void setStockLocation(String stockLocation) {
		this.stockLocation = stockLocation;
	}
	/**
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}
	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}
	/**
	 * @return the cost
	 */
	public double getCost() {
		return cost;
	}
	/**
	 * @param cost the cost to set
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}
}
