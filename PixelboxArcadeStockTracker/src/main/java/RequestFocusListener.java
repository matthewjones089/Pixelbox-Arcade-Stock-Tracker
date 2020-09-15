import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class RequestFocusListener implements AncestorListener {

	private boolean removeListener;
	
	RequestFocusListener() {
		this(true);
	}
	
	RequestFocusListener(boolean remove) {
		removeListener = remove;
	}
	
	@Override
	public void ancestorAdded(AncestorEvent event) {
		
		JComponent component = event.getComponent();
		component.requestFocusInWindow();
		
		if (removeListener)
			component.removeAncestorListener(this);
		
	}

	@Override
	public void ancestorRemoved(AncestorEvent event) {}

	@Override
	public void ancestorMoved(AncestorEvent event) {}
}
