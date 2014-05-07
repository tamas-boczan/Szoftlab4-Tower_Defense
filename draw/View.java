package draw;

import game.Construct;
import game.Geometry;
import game.Tile;
import game.Updater;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class View extends JPanel { //az osztály maga a játékállást megjelenítő nézet
	private Map<Object, Drawable> drawables = new HashMap<Object, Drawable>();
	private Updater updater;
	private Geometry geometry;
	private JPanel menu;
	private JFrame frame;
	private String manaValue= "0";
	private JComboBox comboBoxTypes;
	private BufferedImage image;
	private Tile highlitedTile;
	private int selectedX = -1;
	private int selectedY = -1;
	
	public View(Updater updater) {
		//attribútumok beállítása
		this.updater = updater;
		geometry = updater.getGeometry();
		selectedX = selectedY = -1; //a kiválasztott 
		initTileList(); //a csempéket és nézeteiket hozzáadjuk a drawables Map-hez
		image = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB); //létrehozzuk a képet, amibe a játékteret rajzolni fogjuk
		
		//létrehozzuk a fő ablakot
		frame = new JFrame("Game");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setResizable(false);
		frame.setLayout(new FlowLayout());
		
		//beállítjuk és hozzáadjuk az ablakhoz a játékteret tartalmazó nézetet
		this.setPreferredSize(new Dimension(600, 600));
		this.addMouseListener(new GameMouseListener()); //az egeret figyelő listener beállítása
		frame.add(this);
		
		
		//a gombokat tartalmazó panel
		menu = new JPanel();
		menu.setPreferredSize(new Dimension(200, 600));
		buildMenu(); //a panel tartalmát hozza létre
		frame.add(menu);
		
		frame.pack();
		frame.setVisible(true);
	}
	
	public int[] getTilePosition(Tile tile) { //visszaadja a paraméterként kapott csempe helyét a képernyőn pixelben.
		return null;
	}
	
	public void drawAll() {
		Graphics g = image.getGraphics(); //a kirajzolandó képhez tartozó Graphics
		
		//csempék rajzolása
		int num = 0;
		for (Object obj : drawables.keySet()) { //végigiterálunk a drawables Map-en
			//ha csempét találunk a listában, akkor azonosítjuk, hogy milyen típusú és kirajzoljuk
			if (obj.getClass().toString().equals("class game.FieldTile")) {
				//((FieldTileView) obj).draw(g);
				System.out.println(num+". :"+obj.getClass().toString());
				num++;
			} else if (obj.getClass().toString().equals("class game.EndTile")) {
				//((EndTileView) obj).draw(g);
				System.out.println(num+". :"+obj.getClass().toString());
				num++;
			} else if (obj.getClass().toString().equals("class game.PathTile")) {
				//((PathTileView) obj).draw(g);
				System.out.println(num+". :"+obj.getClass().toString());
				num++;
			}
		}
		
		//épületek rajzolása
		ArrayList<Construct> constructs = updater.getConstructs();
	}
	
	public void addView(Drawable drawable) {
		drawables.put(null, drawable);
	}
	
	/*public void removeView (Drawable drawable) {
		
	}*/
	
	private void initTileList() { //létrehozza a csempékhez a hozzájuk tartozó nézeteket, majd páronként beteszi őket a drawables Map-be.
		Tile[][] tiles = geometry.getTiles();
			
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[0].length; y++) {
				if (tiles[x][y].getType().equals("FieldTile")) {
					FieldTileView view = null;
					drawables.put(tiles[x][y], view);
				} else if (tiles[x][y].getType().equals("PathTile")) {
					PathTileView view = null;
					drawables.put(tiles[x][y], view);
				} else if (tiles[x][y].getType().equals("EndTile")) {
					EndTileView view = null;
					drawables.put(tiles[x][y], view);
				}
			}
		}
	}
	
	private void buildMenu() {	// a menüsor elemeit helyezi el
		menu.setSize(200, 600);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[]{50, 50, 50, 30};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		menu.setLayout(gridBagLayout);
		
		JButton btnBuildTower = new JButton("Build Tower");
		btnBuildTower.addActionListener(new buildTowerActionListener());
		GridBagConstraints gbc_btnBuildTower = new GridBagConstraints();
		gbc_btnBuildTower.gridx = 0;
		gbc_btnBuildTower.gridy = 0;
		menu.add(btnBuildTower, gbc_btnBuildTower);
		
		JButton btnBuildBarricade = new JButton("Build Barricade");
		btnBuildBarricade.addActionListener(new buildBarricadeActionListener());
		GridBagConstraints gbc_btnBuildBarricade = new GridBagConstraints();
		gbc_btnBuildBarricade.gridx = 0;
		gbc_btnBuildBarricade.gridy = 1;
		menu.add(btnBuildBarricade, gbc_btnBuildBarricade);
		
		JButton btnUpgrade = new JButton("Upgrade");
		btnUpgrade.addActionListener(new upgradeActionListener());
		GridBagConstraints gbc_btnUpgrade = new GridBagConstraints();
		gbc_btnUpgrade.gridx = 0;
		gbc_btnUpgrade.gridy = 2;
		menu.add(btnUpgrade, gbc_btnUpgrade);
		
		String[] types = { "Human", "Elf", "Hobbit", "Dwarf", "Range", "Slow" , "Fire Rate", "Range"};
		comboBoxTypes = new JComboBox(types);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 3;
		menu.add(comboBoxTypes, gbc_comboBox);
		
		JLabel manaLabel = new JLabel("Varázserő: "+manaValue);
		GridBagConstraints gbc_manaLabel = new GridBagConstraints();
		gbc_manaLabel.gridx = 0;
		gbc_manaLabel.gridy = 16;
		menu.add(manaLabel, gbc_manaLabel);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.setBackground(Color.GREEN);
		g.drawImage(image, 0, 0, null);
		if (selectedX >= 0 && selectedY >= 0) { //amennyiben van érvényes kijelölt csempe, jelöljük azt
			//egy sága négyzetet rajzolunk a kijelölt csempe köré
			g.setColor(Color.YELLOW);
			g.drawRect(selectedX * 60, selectedY * 60, 60, 60);
		}
	}
	
	private class buildTowerActionListener implements ActionListener { //a torony építő gomhoz tartozó listener

		@Override
		public void actionPerformed(ActionEvent arg0) {
			//ha van kijelölt és érnéyes csempe, meghívjuk a Controller megfelelő függvényét
			if (highlitedTile != null && highlitedTile.getType().equals("FieldTile"))
				Controller.buildTower(highlitedTile);
		}
	}
	
	private class buildBarricadeActionListener implements ActionListener { //az akadály építő gomhoz tartozó listener

		@Override
		public void actionPerformed(ActionEvent arg0) {
			//ha van kijelölt és érnéyes csempe, meghívjuk a Controller megfelelő függvényét
			if (highlitedTile != null && highlitedTile.getType().equals("PathTile"))
				Controller.buildBarricade(highlitedTile);
		}
	}
	
	private class upgradeActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (highlitedTile != null && highlitedTile.getConstruct()!= null)
				Controller.upgrade(highlitedTile, comboBoxTypes.getSelectedItem().toString());
		}
	}
	
	private class GameMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent arg0) {
			//ha kattintunk az egérrel, akkor megnézzük, hogy épp milyen cellán van és ez lesz a kijelölés értéke
			
			//a kurzor X és Y koordinátája
			int mouseX = arg0.getX();
			int mouseY = arg0.getY();
			
			
			if (arg0.getButton() == MouseEvent.BUTTON1) { //ha a bal gombbal kattintottunk
				
				//megnézzük a pálya méretét
				int lenY = (geometry.getTiles())[0].length;
				int lenX = geometry.getTiles().length;
				
				//a pálya mérete, a kurzor helye és a rajzolt nézet mérete alapján kiszámoljuk, hogy hányadik csempére kattintottunk
				selectedX = mouseX / (600 / lenX);
				selectedY = mouseY / (600 / lenY);
				
				//a kiválasztott csempe
				highlitedTile = (geometry.getTiles())[selectedX][selectedY];
				repaint(); //a kijelölés miatt újrarajzoljuk a képet
			}
			
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
