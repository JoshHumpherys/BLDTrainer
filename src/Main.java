import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main extends JFrame {
	public static final String IMAGES_FILE_DIR = "images.txt";
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
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		
		List<Pair> list = new ArrayList<Pair>();
		try {
			FileReader fr = new FileReader(IMAGES_FILE_DIR);
			BufferedReader br = new BufferedReader(fr);
			String line = null;
			while((line = br.readLine()) != null) {
				int i = line.indexOf(" ");
				list.add(new Pair(line.substring(0, i), line.substring(i + 1)));
			}
		}
		catch(FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, "Cannot find file: " + IMAGES_FILE_DIR);
			System.exit(1);
		}
		catch(IOException e) {
			JOptionPane.showMessageDialog(this, "Error reading file: " + IMAGES_FILE_DIR);
			System.exit(1);
		}
		Collections.shuffle(list);
		System.out.println(list);
	}
	
	private class Pair {
		private String pair, image;
		public Pair(String pair, String image) {
			this.pair = pair;
			this.image = image;
		}
		public String getPair() {
			return pair;
		}
		public String getImage() {
			return image;
		}
		@Override
		public String toString() {
			return pair + "=" + image;
		}
	}
}