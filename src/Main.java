import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
		
		List<Data> list = new ArrayList<Data>();
		try {
			FileReader fr = new FileReader(IMAGES_FILE_DIR);
			BufferedReader br = new BufferedReader(fr);
			String line = null;
			while((line = br.readLine()) != null) {
				int i = line.indexOf(" ");
				list.add(new Data(line.substring(0, i), line.substring(i + 1)));
			}
		}
		catch(FileNotFoundException e) {
			JOptionPane.showMessageDialog(new JFrame(), "Cannot find file: " + IMAGES_FILE_DIR);
			System.exit(1);
		}
		catch(IOException e) {
			JOptionPane.showMessageDialog(new JFrame(), "Error reading file: " + IMAGES_FILE_DIR);
			System.exit(1);
		}
		Collections.shuffle(list);
		System.out.println(list);
		
		JPanel panel = new JPanel();
		add(panel);
		
		JLabel image = new JLabel(new ImageIcon(list.get(0).getImage()));
		panel.add(image);
		
		panel.repaint();
	}
	
	private class Data {
		private String pair, imageString;
		private BufferedImage image;
		public Data(String pair, String imageString) {
			this.pair = pair;
			this.image = image;
		}
		public String getPair() {
			return pair;
		}
		public String getImageString() {
			return imageString;
		}
		public BufferedImage getImage() {
			if(image == null) {
				try {
					image = ImageIO.read(new File("img/" + pair + ".png"));
				}
				catch(IOException e) {
					JOptionPane.showMessageDialog(new JFrame(), "Error reading or opening image: img/" + pair + ".png");
					System.exit(1);
				}
				return image;
			}
			else return image;
		}
		@Override
		public String toString() {
			return pair + "=" + imageString;
		}
	}
}