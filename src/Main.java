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
	
	public boolean all = true;
	public int mode = -1;
	public Map<Integer, String> modes;
	public List<Data> list;
	public boolean toShowImage = true;
	public int index = -1;
	
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
		
		list = new ArrayList<Data>();
		try {
			FileReader fr = new FileReader(IMAGES_FILE_DIR);
			BufferedReader br = new BufferedReader(fr);
			String line = null;
			
			// Read mode names
			modes = new TreeMap<Integer, String>();
			while((line = br.readLine()) != null) {
				if(line.indexOf("#") == 0) {
					break;
				}
				try {
					int splitIndex = line.indexOf(" ");
					int modeNumber = Integer.parseInt(line.substring(0, splitIndex));
					if(mode == -1) {
						mode = modeNumber;
					}
					modes.put(modeNumber, line.substring(splitIndex + 1));
				}
				catch(NumberFormatException e) {
					JOptionPane.showMessageDialog(new JFrame(), "Error parsing file: " + IMAGES_FILE_DIR);
					System.exit(1);
				}
			}
			
			// Read data
			while((line = br.readLine()) != null) {
				if(line.indexOf(" ", line.indexOf(" ") + 1) != -1) {
					String[] columns = line.split(" ");
					list.add(new Data(Integer.parseInt(columns[0]), columns[1], columns[2]));
				}
				else {
					JOptionPane.showMessageDialog(new JFrame(), "Error parsing file: " + IMAGES_FILE_DIR);
					System.exit(1);
				}
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
		Collections.shuffle(list);
		
		il = new ImageLoader(list, panelWidth, panelHeight);
		il.run();
		
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		add(panel);
		
		label = next(panelWidth, panelHeight, true);
		label.setFont(font);
		panel.add(label);
		panel.repaint();
		
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_SHIFT:
					String prompt = "Modes:\n0) All\n";
					Iterator mapIterator = modes.entrySet().iterator();
					while(mapIterator.hasNext()) {
						Map.Entry entry = (Map.Entry)mapIterator.next();
						prompt += entry.getKey() + ") " + entry.getValue() + (mapIterator.hasNext() ? "\n" : "");
//						mapIterator.remove();
					}
					String requestedMode = JOptionPane.showInputDialog(new JFrame(), prompt);
					try {
						if(requestedMode.equals("0")) {
							mode = 0;
							all = true;
						}
						else {
							mode = Integer.parseInt(requestedMode);
							all = false;
						}
					}
					catch(NumberFormatException nfe) {
						JOptionPane.showMessageDialog(new JFrame(), "Error: Bad input");
					}
					// only break if all or currently on same mode
					if(all || list.get(index).getMode() == mode) {
						break;
					}
				case KeyEvent.VK_RIGHT:
				case KeyEvent.VK_D:
					panel.remove(label);
					label = next(panelWidth, panelHeight, true);
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
	
	// TODO fix StackOverlowError when there are no images of a certain mode
	private JLabel next(int width, int height, boolean fromNext) {
		if(index < list.size() - 1) {
			index++;
			if(all || list.get(index).getMode() == mode) {
//				return new JLabel(new ImageIcon(list.get(index).getImage(width, height)));
				return new JLabel(new ImageIcon(new LoadedImage(list.get(index).getPair(), width, height).getImage()));
			}
			else {
				if(fromNext) {
					return next(width, height, true);
				}
				else {
					return last(width, height);
				}
			}
		}
		else {
			index = -1;
			if(list.size() != 0) {
				return next(width, height, true);
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
		if(index < -1) {
			index += list.size();
		}
		return next(width, height, false);
	}
	
	private JLabel flip(int width, int height) {
		Data data = list.get(index);

		toShowImage = !toShowImage;
		
		if(!toShowImage) {
			JLabel returnLabel = new JLabel(data.getPair());
			returnLabel.setFont(font);
			return returnLabel;
		}
		else {
			index++;
			return last(width, height);
		}
	}
	
//	// TODO use synchronized but then first image won't load until all are loaded
//	private JPanel loadingPanel = new JPanel();
////	private synchronized void loadImages() {
//	private synchronized void loadImages() {
////		for(Data d : list) {
////			// load image
////			// for some reason, adding to a random panel makes it load almost instantly on real panel
////			// EDIT: oh probably garbage collection
////			// EDIT: with too many images I get an OutOfMemoryError
//////			if(d.getMode() == mode) {
////				loadingPanel.add(new JLabel(new ImageIcon(d.getImage(panelWidth, panelHeight))));
//////			}
////		}
//		if(loadedImages == null) {
//			loadedImages = new ArrayList<LoadedImage>();
//			for(int i = -5; i <= 5; i++) {
//				loadedImages.add(new LoadedImage(list.get(getIndex(index + i)).getPair(), panelWidth, panelHeight));
//			}
//			System.out.println(loadedImages);
//		}
//	}
//	
//	private int getIndex(int i) {
//		while(i < 0) {
//			i += list.size();
//		}
//		while(i > list.size() - 1) {
//			i -= list.size();
//		}
//		return i;
//	}
//	
//	private void updateLoadedImages(boolean forward) {
//		if(forward) {
//			loadedImages.remove(0);
//			loadedImages.add(new LoadedImage(list.get(getIndex(index + 5)).getPair(), panelWidth, panelHeight));
//		}
//		else {
//			loadedImages.remove(loadedImages.size() - 1);
//			loadedImages.add(new LoadedImage(list.get(getIndex(index - 5)).getPair(), panelWidth, panelHeight));
//		}
//	}
	
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
	
//	private class ImageLoader implements Runnable {
//		public ImageLoader() {}
//		
//		@Override
//		public void run() {
//			loadImages();
//		}
//		public void update(boolean forward) {
//			updateLoadedImages(forward);
//		}
//		
//		public void start() {
//			Thread t = new Thread(this, "ImageLoader");
//			t.start();
//		}
//	}
	
	private class ImageLoader implements Runnable {
		private List<Data> list;
		private int panelWidth, panelHeight;
		public ImageLoader(List<Data> list, int panelWidth, int panelHeight) {
			this.list = new ArrayList<Data>();
			for(Data d : list) {
				this.list.add(d);
			}
		}
		
		@Override
		public void run() {
			System.out.println(list);
		}
	}
}