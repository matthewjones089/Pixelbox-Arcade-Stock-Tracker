import javax.swing.SwingUtilities;

public class Main {

	public static void main(String[] args){
		
		final int WIDTH = 1000;
		final int HEIGHT = 600;
		//Creates and invokes a runnable using the specified width and height
		Runnable r = new AppRunnable(WIDTH, HEIGHT);
		
		SwingUtilities.invokeLater(r);
		
	}	
}
