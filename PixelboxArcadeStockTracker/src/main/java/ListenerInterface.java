import java.util.List;

public interface ListenerInterface {

	public void doubleClick(String billID);
	
	public void doubleClick(int orderNumber);
	
	public void notifyDelete(String tabName);
	
	public void repaintItems();
	
	public void repaintCost(int orderNumber, double totalCost);
}
