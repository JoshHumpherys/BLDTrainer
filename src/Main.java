import java.awt.Dimension;
import java.awt.Font;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
	
	// read file
	// store mode, pair, string in arraylist
	// shuffle
	// start imageloader thread
	// imageloader thread loads the next 10 and previous 10 images of that mode in 2 linked lists
	// imageloader wait
	// when changing images, set boolean for direction and notify imageloader
	
	public ImageLoader il;
	public boolean dirForward;
	
	public int mode = 0;
	public int index = 0;
	public Map<Integer, String> modesMap;
	public List<Mode> modes;
	public boolean toShowImage = true;
	
	public JPanel panel;
	public JLabel label;
	public int panelWidth, panelHeight;
	public Font font;
	public Main() {
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int)(screenDimension.getWidth() * 2 / 3);
		int height = (int)(screenDimension.getHeight() * 2 / 3);
		setPreferredSize(new Dimension(width, height));
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		font = new Font("TimesRoman", Font.PLAIN, height / 10);
		
		try {
			FileReader fr = new FileReader(IMAGES_FILE_DIR);
			BufferedReader br = new BufferedReader(fr);
			String line = null;
			
			// Read mode names
			modesMap = new TreeMap<Integer, String>();
			modesMap.put(0, "All");
			while((line = br.readLine()) != null) {
				if(line.indexOf("#") == 0) {
					break;
				}
				try {
					int splitIndex = line.indexOf(" ");
					int modeNumber = Integer.parseInt(line.substring(0, splitIndex));
					modesMap.put(modeNumber, line.substring(splitIndex + 1));
				}
				catch(NumberFormatException e) {
					JOptionPane.showMessageDialog(new JFrame(), "Error parsing file: " + IMAGES_FILE_DIR);
					System.exit(1);
				}
			}
			
			modes = new ArrayList<Mode>();
			for(int i = 0; i <= modesMap.size(); i++) {
				modes.add(new Mode());
			}
			
			// Read data
			while((line = br.readLine()) != null) {
				String[] columns = line.split(" ", 3);
				if(columns.length < 3) {
					JOptionPane.showMessageDialog(new JFrame(), "Error parsing file: " + IMAGES_FILE_DIR);
					System.exit(1);
				}
				Data d = new Data(Integer.parseInt(columns[0]), columns[1], columns[2]);
				modes.get(0).add(d);
				modes.get(Integer.parseInt(columns[0])).add(d);
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
		catch(NumberFormatException e) {
			JOptionPane.showMessageDialog(new JFrame(), "Error parsing file: " + IMAGES_FILE_DIR);
			System.exit(1);
		}
		Insets insets = getInsets();
		panelWidth = width - insets.right - insets.left;
		panelHeight = height - insets.top - insets.bottom;
		for(Mode m : modes) {
			m.shuffle();
		}
		
		il = new ImageLoader(modes, panelWidth, panelHeight);
		il.start();
		
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		add(panel);
		
		label = current(panelWidth, panelHeight);
		label.setFont(font);
		panel.add(label);
		panel.repaint();
		
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_SHIFT:
					String prompt = "Modes:\n";
					Iterator mapIterator = modesMap.entrySet().iterator();
					while(mapIterator.hasNext()) {
						Map.Entry entry = (Map.Entry)mapIterator.next();
						prompt += entry.getKey() + ") " + entry.getValue() + (mapIterator.hasNext() ? "\n" : "");
//						mapIterator.remove();
					}
					String requestedMode = JOptionPane.showInputDialog(new JFrame(), prompt);
					try {
						mode = Integer.parseInt(requestedMode);
						index = 0;
						modes.get(mode).shuffle();
					}
					catch(NumberFormatException nfe) {
						JOptionPane.showMessageDialog(new JFrame(), "Error: Bad input");
					}
				case KeyEvent.VK_RIGHT:
				case KeyEvent.VK_D:
					panel.remove(label);
					label = next(panelWidth, panelHeight);
					if(!toShowImage) {
						toShowImage = true;
						label = flip(panelWidth, panelHeight);
					}
					panel.add(label);
					panel.revalidate();
					panel.repaint();
					break;
				case KeyEvent.VK_LEFT:
				case KeyEvent.VK_A:
					panel.remove(label);
					label = last(panelWidth, panelHeight);
					if(!toShowImage) {
						toShowImage = true;
						label = flip(panelWidth, panelHeight);
					}
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
				}
			}
		});
	}
	
	private JLabel next(int width, int height) {
		return next(width, height, 1);
	}
	
	private JLabel next(int width, int height, int n) {
		addToIndex(n);
		return new JLabel(new ImageIcon(new LoadedImage(modes.get(mode).get(index).getPair(), width, height).getImage()));
	}
	
//	private JLabel next(int width, int height, int n) {
//		addToIndex(n);
////		index += n;
//		int startIndex = index;
//		do {
////			index %= list.size();
//			Data current = modes.get(mode).get(index);
//			if(all || current.getMode() == mode) {
//				return new JLabel(new ImageIcon(new LoadedImage(current.getPair(), width, height).getImage()));
//			}
//			addToIndex(Integer.signum(n));
////			index += Integer.signum(n);
//		}
//		while(index != startIndex);
//		return null;
//	}
	
	private JLabel last(int width, int height) {
		return next(width, height, -1);
	}
	
	private JLabel current(int width, int height) {
		return next(width, height, 0);
	}
	
	private JLabel flip(int width, int height) {
		Data data = modes.get(mode).get(index);

		toShowImage = !toShowImage;
		
		if(!toShowImage) {
			JLabel returnLabel = new JLabel(data.getPair());
			returnLabel.setFont(font);
			return returnLabel;
		}
		else {
			return current(width, height);
		}
	}
	
	private void addToIndex(int n) {
		index += n;
		int size = modes.get(mode).getSize();
		index = ((index % size) + size) % size;
	}
	
	private class Data {
		private int mode;
		private String pair, imageString;
		public Data(int mode, String pair, String imageString) {
			this.mode = mode;
			this.pair = pair;
			this.imageString = imageString;
		}
		public int getMode() {
			return mode;
		}
		public String getPair() {
			return pair;
		}
		public String getImageString() {
			return imageString;
		}
		@Override
		public String toString() {
			return pair + "=" + imageString;
		}
	}
	
	private class Mode {
		private List<Data> list;
		public Mode() {
			list = new ArrayList<Data>();
		}
		public void add(Data d) {
			list.add(d);
		}
		public Data get(int i) {
			return list.get(i);
		}
		public void shuffle() {
			Collections.shuffle(list);
		}
		public int getSize() {
			return list.size();
		}
	}
	
	private class LoadedImage {
		private Image image;
		public LoadedImage(String pair, int width, int height) {
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
		}
		public Image getImage() {
			return image;
		}
	}
	
	private class ImageLoader extends Thread {
		private LoadedImage current;
		
		private List<Mode> modes;
		private int width, height;
		public ImageLoader(List<Mode> modes, int width, int height) {
			this.modes = new ArrayList<Mode>();
			for(Mode m : modes) {
				this.modes.add(m);
			}
			this.width = width;
			this.height = height;
		}
		
		@Override
		public void run() {
			current = new LoadedImage(modes.get(mode).get(index).getPair(), width, height);
		}
		
		public LoadedImage getCurrent() {
			return current;
		}
	}
}