import java.sql.Date;

public class Order {

	private int orderNumber;
	private String address1;
	private String address2;
	private String address3;
	private String address4;
	private String postcode;
	private Date orderDate;
	private Date promiseDate;
	private Date deliveryDate;
	private String notes;
	private double totCost;
	/**
	 * @param orderNumber
	 * @param address1
	 * @param address2
	 * @param address3
	 * @param address4
	 * @param postcode
	 * @param orderDate
	 * @param promiseDate
	 * @param deliveryDate
	 * @param notes
	 * @param totCost
	 */
	public Order(int orderNumber, String address1, String address2, String address3, String address4, String postcode,
			Date orderDate, Date promiseDate, Date deliveryDate, String notes, double totCost) {
		super();
		this.orderNumber = orderNumber;
		this.address1 = address1;
		this.address2 = address2;
		this.address3 = address3;
		this.address4 = address4;
		this.postcode = postcode;
		this.orderDate = orderDate;
		this.promiseDate = promiseDate;
		this.deliveryDate = deliveryDate;
		this.notes = notes;
		this.totCost = totCost;
	}
	/**
	 * @return the orderNumber
	 */
	public int getOrderNumber() {
		return orderNumber;
	}
	/**
	 * @param orderNumber the orderNumber to set
	 */
	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}
	/**
	 * @return the address1
	 */
	public String getAddress1() {
		return address1;
	}
	/**
	 * @param address1 the address1 to set
	 */
	public void setAddress1(String address1) {
		this.address1 = address1;
	}
	/**
	 * @return the address2
	 */
	public String getAddress2() {
		return address2;
	}
	/**
	 * @param address2 the address2 to set
	 */
	public void setAddress2(String address2) {
		this.address2 = address2;
	}
	/**
	 * @return the address3
	 */
	public String getAddress3() {
		return address3;
	}
	/**
	 * @param address3 the address3 to set
	 */
	public void setAddress3(String address3) {
		this.address3 = address3;
	}
	/**
	 * @return the address4
	 */
	public String getAddress4() {
		return address4;
	}
	/**
	 * @param address4 the address4 to set
	 */
	public void setAddress4(String address4) {
		this.address4 = address4;
	}
	/**
	 * @return the postcode
	 */
	public String getPostcode() {
		return postcode;
	}
	/**
	 * @param postcode the postcode to set
	 */
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	/**
	 * @return the orderDate
	 */
	public Date getOrderDate() {
		return orderDate;
	}
	/**
	 * @param orderDate the orderDate to set
	 */
	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}
	/**
	 * @return the promiseDate
	 */
	public Date getPromiseDate() {
		return promiseDate;
	}
	/**
	 * @param promiseDate the promiseDate to set
	 */
	public void setPromiseDate(Date promiseDate) {
		this.promiseDate = promiseDate;
	}
	/**
	 * @return the deliveryDate
	 */
	public Date getDeliveryDate() {
		return deliveryDate;
	}
	/**
	 * @param deliveryDate the deliveryDate to set
	 */
	public void setDeliveryDate(Date deliveryDate) {
		this.deliveryDate = deliveryDate;
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
	 * @return the totCost
	 */
	public double getTotCost() {
		return totCost;
	}
	/**
	 * @param totCost the totCost to set
	 */
	public void setTotCost(double totCost) {
		this.totCost = totCost;
	}
	
	
	
	
}
