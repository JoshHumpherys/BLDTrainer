import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
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
		int width = (int)(screenDimension.getWidth() * 2 / 3);
		int height = (int)(screenDimension.getHeight() * 2 / 3);
		setPreferredSize(new Dimension(width, height));
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
		
		JPanel panel = new JPanel();
		add(panel);
		
		Insets insets = getInsets();
		JLabel image = new JLabel(new ImageIcon(list.get(0).getImage(width - insets.right - insets.left, height - insets.top - insets.bottom)));
		panel.add(image);
		
		panel.repaint();
	}
	
	private class Data {
		private String pair, imageString;
		private Image image;
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
		public Image getImage(int width, int height) {
			if(image == null) {
				try {
					image = ImageIO.read(new File("img/" + pair + ".png"));
				}
				catch(IOException e) {
					JOptionPane.showMessageDialog(new JFrame(), "Error reading or opening image: img/" + pair + ".png");
					System.exit(1);
				}
				double imgWidth = image.getWidth(null);
				double imgHeight = image.getHeight(null);
				if(imgWidth / width > imgHeight / height) {
					// scale width to max
					image = image.getScaledInstance(width, (int)(width * imgHeight / imgWidth), Image.SCALE_SMOOTH);
				}
				else {
					// scale height to max
					image = image.getScaledInstance((int)(height * imgWidth / imgHeight), height, Image.SCALE_SMOOTH);
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