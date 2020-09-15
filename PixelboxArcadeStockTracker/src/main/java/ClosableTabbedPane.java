import java.awt.Component;
import java.awt.Container;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JTabbedPane;

public class ClosableTabbedPane extends JTabbedPane {
		//-----------//
	    // insertTab //
	    //-----------//
	    /**
	     * {@inheritDoc}
	     * <p>
	     * It overrides the standard method so that any inserted tab uses a ButtonTabComponent
	     */
	    @Override
	    public void insertTab (String title,
	                           Icon icon,
	                           Component component,
	                           String tip,
	                           int index)
	    {
	        super.insertTab(title, icon, component, tip, index);

	        // Use a ButtonTabComponent
	        final int i = indexOfComponent(component);
	        if (i > 2) {
	        	setTabComponentAt(i, new ButtonTabComponent(this));
	        }
	    }

	    //---------------------//
	    // removeClosingButton //
	    //---------------------//
	    /**
	     * Remove the closing button for the provided tab index.
	     *
	     * @param tabIndex index of tab in tabbed pane
	     */
	    public void removeClosingButton (int tabIndex)
	    {
	        Component tab = getTabComponentAt(tabIndex);

	        if (tab instanceof ButtonTabComponent) {
	            for (Component c : ((Container) tab).getComponents()) {
	                if (c instanceof JButton) {
	                    ((Container) tab).remove(c);
	                    tab.invalidate();
	                    tab.repaint();

	                    return;
	                }
	            }
	        }
	    }

	    //-----------------//
	    // tabAboutToClose //
	    //-----------------//
	    /**
	     * Signal that the tab at provided index is about to close.
	     * This method can be overridden to add any specific processing at this point.
	     *
	     * @param tabIndex index of tab in tabbed pane
	     * @return true to continue closing, false to cancel
	     */
	    public boolean tabAboutToClose (int tabIndex)
	    {
	        return true; // By default, complete the closing
	    }
	}