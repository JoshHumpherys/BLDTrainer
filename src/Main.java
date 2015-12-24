import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
	
	public boolean image = true;
	public int index = -1;
	public List<Data> list;
	JLabel label;
	public Main() {
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int)(screenDimension.getWidth() * 2 / 3);
		int height = (int)(screenDimension.getHeight() * 2 / 3);
		setPreferredSize(new Dimension(width, height));
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		
		list = new ArrayList<Data>();
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
		panel.setLayout(new GridBagLayout());
		add(panel);
		
		Insets insets = getInsets();
		int panelWidth = width - insets.right - insets.left;
		int panelHeight = height - insets.top - insets.bottom;
		
		label = next(panelWidth, panelHeight);
		panel.add(label);
		panel.repaint();
		
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_RIGHT:
				case KeyEvent.VK_D:
					panel.remove(label);
					label = next(panelWidth, panelHeight);
					panel.add(label);
					panel.revalidate();
					panel.repaint();
					break;
				case KeyEvent.VK_UP:
				case KeyEvent.VK_W:
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_S:
					panel.remove(label);
					label = flip(panelWidth, panelHeight);
					panel.add(label);
					panel.revalidate();
					panel.repaint();
					break;
				case KeyEvent.VK_LEFT:
				case KeyEvent.VK_A:
					panel.remove(label);
					label = last(panelWidth, panelHeight);
					panel.add(label);
					panel.revalidate();
					panel.repaint();
					break;
				}
			}
		});
	}
	
	private JLabel next(int width, int height) {
		if(index < list.size() - 1) {
			index++;
			System.out.println(list.get(index));
			return new JLabel(new ImageIcon(list.get(index).getImage(width, height)));
		}
		else {
			index = -1;
			if(list.size() != 0) {
				return next(width, height);
			}
			else {
				JOptionPane.showMessageDialog(new JFrame(), "Error: images.txt does not contain image letter pairs");
				System.exit(1);
				return null;
			}
		}
	}
	
	private JLabel last(int width, int height) {
		index -= 2;
		while(index < -1) {
			index += list.size();
		}
		return next(width, height);
	}
	
	private JLabel flip(int width, int height) {
		Data data = list.get(index);
		
		if(image) {
		
		image = !image;
		return new JLabel(data.getPair() + ", " + data.getImageString());
		}
		else {
			image = !image;
			index++;
			return last(width, height);
		}
	}
	
	private class Data {
		private String pair, imageString;
		private Image image;
		public Data(String pair, String imageString) {
			this.pair = pair;
			this.imageString = imageString;
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