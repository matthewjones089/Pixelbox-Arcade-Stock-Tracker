import java.util.List;

public class BOM {
	
	private String billID;
	private String description;
	private List<BOMD> bomds;
	/**
	 * @param billID
	 * @param descrip
	 */
	public BOM(String billID, String description) {
		super();
		this.billID = billID;
		this.description = description;
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
	
	public void addBOMD(BOMD bomd) {
		bomds.add(bomd);
	}
	

}
