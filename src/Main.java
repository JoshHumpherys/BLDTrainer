import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main extends JFrame {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Main();
			}
		});
	}
	
	public Main() {
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		setPreferredSize(new Dimension((int)(screenDimension.getWidth() * 2 / 3), (int)(screenDimension.getHeight() * 2 / 3)));
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}