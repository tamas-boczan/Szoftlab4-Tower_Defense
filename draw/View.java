package draw;

import game.Barricade;
import game.Construct;
import game.Dwarf;
import game.Elf;
import game.EndTile;
import game.Enemy;
import game.FieldTile;
import game.Geometry;
import game.Hobbit;
import game.Human;
import game.PathTile;
import game.Tile;
import game.Tower;
import game.Updater;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class View extends JPanel { //az osztály maga a játékállást megjelenítő nézet
	private Map<Object, Drawable> drawables = new HashMap<Object, Drawable>();
	private Updater updater;
	private Geometry geometry;
	private JPanel menu;
	private JFrame frame;
	//private String manaValue= "0";
	private JLabel manaLabel;
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
		initMenu(); //a panel tartalmát hozza létre
		frame.add(menu);
		
		frame.pack();
		frame.setVisible(true);
		
		//nézet frissítésére szolgáló időzítő
		Timer timer = new Timer(40, new RefreshListener());
		timer.start();
	}
	
	public int[] getTilePosition(Tile tile) { //visszaadja a paraméterként kapott csempe helyét a képernyőn pixelben.
		Tile[][] tiles = geometry.getTiles();
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[0].length; y++) {
				if (tiles[x][y] == tile)
					return new int[] {x * 30, y * 30};
			}
		}
		return null;
	}
	
	public void drawAll() {
		Graphics g = image.getGraphics(); //a kirajzolandó képhez tartozó Graphics
		
		//csempék rajzolása
		for (Object obj : drawables.keySet()) { //végigiterálunk a drawables Map-en
			//ha csempét találunk a listában, akkor kirajzoljuk
			if (obj.getClass().toString().equals("class game.FieldTile") || obj.getClass().toString().equals("class game.EndTile")
					|| obj.getClass().toString().equals("class game.PathTile")) {
				drawables.get(obj).draw(g);
			}
		}
		
		//épületek kirajzolása
		ArrayList<Construct> constructs = updater.getConstructs(); //lekérjük a pályán lévő épületek listáját
		for (int i = 0; i < constructs.size(); i++) { //végigjárjuk az épületek listáját
			/* ha az épület még nem szerepel a drawables listában, akkor létrehozzuk hozzá a hozzá tartozó
			 * nézetet és hozzáadjuk őket a drawables-hez.*/
			if (!drawables.containsKey(constructs.get(i))) {
				if (constructs.get(i).getType().equals("Tower")) {
					TowerView view = new TowerView(this, (Tower) constructs.get(i));
					drawables.put(constructs.get(i), view);
				} else if (constructs.get(i).getType().equals("Barricade")) {
					BarricadeView view = new BarricadeView(this, (Barricade) constructs.get(i));
					drawables.put(constructs.get(i), view);
				}
			}
			drawables.get(constructs.get(i)).draw(g); 	//lekérjük az épülethez tartozó nézetet és kirajzoljuk
		}
		
		//ellenségek rajzolása
		ArrayList<Enemy> enemies = updater.getEnemies();	//lekérjük a pályán lévő ellenségek listáját
		for (int i = 0; i < enemies.size(); i++) {	//végigjárjuk az ellenségek listáját
			/* ha az ellenség még nem szerepel a drawables listában, akkor létrehozzuk hozzá a hozzá tartozó
			 * nézetet és hozzáadjuk őket a drawables-hez.*/
			if (!drawables.containsKey(enemies.get(i))) {
				if (enemies.get(i).getType().equals("hobbit")) {
					HobbitView view = new HobbitView(this, (Hobbit) enemies.get(i));
					drawables.put(enemies.get(i), view);
				} else if (enemies.get(i).getType().equals("dwarf")) {
					DwarfView view = new DwarfView(this, (Dwarf) enemies.get(i));
					drawables.put(enemies.get(i), view);
				} else if (enemies.get(i).getType().equals("elf")) {
					ElfView view = new ElfView(this, (Elf) enemies.get(i));
					drawables.put(enemies.get(i), view);
				} else if (enemies.get(i).getType().equals("human")) {
					HumanView view = new HumanView(this, (Human) enemies.get(i));
					drawables.put(enemies.get(i), view);
				}
			}
			//lekérjük az épülethez tartozó nézetet és kirajzoljuk
			drawables.get(enemies.get(i)).draw(g);
		}
		
		
		if (updater.getFogStatus()) {
			try {
				Image fog;
				fog = ImageIO.read(new File ("textures/fog.png"));
				g.drawImage(fog, 0, 0, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			
		//ha véget ér a játék, akkor kirajzoljuk a megfelelő képet
		if (updater.getGameState().equals("win")) {
			try {
				Image end;
				end = ImageIO.read(new File ("textures/victory.png"));
				g.drawImage(end, (this.getWidth() - end.getWidth(null)) / 2, (this.getHeight() - end.getHeight(null)) / 2, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (updater.getGameState().equals("lose")) {
			try {
				Image end;
				end = ImageIO.read(new File ("textures/defeat.png"));
				g.drawImage(end, (this.getWidth() - end.getWidth(null)) / 2, (this.getHeight() - end.getHeight(null)) / 2, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void initTileList() { //létrehozza a csempékhez a hozzájuk tartozó nézeteket, majd páronként beteszi őket a drawables Map-be.
		Tile[][] tiles = geometry.getTiles();
			
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[0].length; y++) {
				if (tiles[x][y].getType().equals("FieldTile")) {
					FieldTileView view = new FieldTileView(this, (FieldTile) tiles[x][y]);
					drawables.put(tiles[x][y], view);
				} else if (tiles[x][y].getType().equals("PathTile")) {
					PathTileView view = new PathTileView(this, (PathTile) tiles[x][y]);
					drawables.put(tiles[x][y], view);
				} else if (tiles[x][y].getType().equals("EndTile")) {
					EndTileView view = new EndTileView(this, (EndTile) tiles[x][y]);
					drawables.put(tiles[x][y], view);
				}
			}
		}
	}
	
	private void initMenu() {	// a menüsor elemeit helyezi el
		menu.setSize(200, 600);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[]{50, 50, 50, 30};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		menu.setLayout(gridBagLayout);
		//toronyépítés gombja
		JButton btnBuildTower = new JButton("Build Tower");
		btnBuildTower.addActionListener(new buildTowerActionListener());
		GridBagConstraints gbc_btnBuildTower = new GridBagConstraints();
		gbc_btnBuildTower.gridx = 0;
		gbc_btnBuildTower.gridy = 0;
		menu.add(btnBuildTower, gbc_btnBuildTower);
		//akadályépítés gombja
		JButton btnBuildBarricade = new JButton("Build Barricade");
		btnBuildBarricade.addActionListener(new buildBarricadeActionListener());
		GridBagConstraints gbc_btnBuildBarricade = new GridBagConstraints();
		gbc_btnBuildBarricade.gridx = 0;
		gbc_btnBuildBarricade.gridy = 1;
		menu.add(btnBuildBarricade, gbc_btnBuildBarricade);
		//fejlesztés gombja
		JButton btnUpgrade = new JButton("Upgrade");
		btnUpgrade.addActionListener(new upgradeActionListener());
		GridBagConstraints gbc_btnUpgrade = new GridBagConstraints();
		gbc_btnUpgrade.gridx = 0;
		gbc_btnUpgrade.gridy = 2;
		menu.add(btnUpgrade, gbc_btnUpgrade);
		//fejlesztés típusának legördülő menüje
		String[] types = { "Human", "Elf", "Hobbit", "Dwarf", "Range", "Slow" , "Fire Rate"};
		comboBoxTypes = new JComboBox(types);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 3;
		menu.add(comboBoxTypes, gbc_comboBox);
		
		manaLabel = new JLabel("Varázserő: "+updater.getMana());
		GridBagConstraints gbc_manaLabel = new GridBagConstraints();
		gbc_manaLabel.gridx = 0;
		gbc_manaLabel.gridy = 16;
		menu.add(manaLabel, gbc_manaLabel);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.setBackground(Color.GRAY);
		
		drawAll(); //játékelemek nézeteinek frissítése
		manaLabel.setText("Varázserő: "+updater.getMana()); //varázserő mennyiség frissítése
		
		g.drawImage(image, 0, 0, null);
		
		//ha a játék már véget ért, akkor érvénytelenítjük a kijelölést
		if (updater.getGameState().equals("win") || updater.getGameState().equals("lose")) {
			selectedX = selectedY = -1;
		}
		
		if (selectedX >= 0 && selectedY >= 0) { //amennyiben van érvényes kijelölt csempe, jelöljük azt
			//egy fehér négyzetet rajzolunk a kijelölt csempe köré
			g.setColor(Color.WHITE);
			g.drawRect(selectedX * 30, selectedY * 30, 30, 30);
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
	
	private class RefreshListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			frame.repaint();
			
		}
		
	}
	
	private class GameMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent arg0) {
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
		}

		@Override
		public void mouseExited(MouseEvent arg0) {			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			//ha felengedjük az egér bal gombját, akkor megnézzük, hogy épp milyen cellán van és ez lesz a kijelölés értéke
			
			//a kurzor X és Y koordinátája
			int mouseX = arg0.getX();
			int mouseY = arg0.getY();
			
			
			if (arg0.getButton() == MouseEvent.BUTTON1) { //ha a bal gombbal kattintottunk
				
				//megnézzük a pálya méretét
				//int lenY = (geometry.getTiles())[0].length;
				//int lenX = geometry.getTiles().length;
				int lenY = 20;
				int lenX = 20;
				
				//a pálya mérete, a kurzor helye és a rajzolt nézet mérete alapján kiszámoljuk, hogy hányadik csempére kattintottunk
				selectedX = mouseX / (600 / lenX);
				selectedY = mouseY / (600 / lenY);
				
				//a kiválasztott csempe
				highlitedTile = (geometry.getTiles())[selectedX][selectedY];
			}
			
		}
		
	}
}
