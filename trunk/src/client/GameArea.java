package client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.*;

import local.Database;
import local.GlobalConstants;

public class GameArea extends JPanel implements ActionListener, KeyListener, Runnable, GlobalConstants{
	
	private static final long serialVersionUID = -5572295459928673608L;
	private static int myId;
	
	private Socket socket;		//socket connecting to server
	private DataOutputStream dos;
	private DataInputStream dis;
	private int port = 49051;	//mapserver-port
	private Timer tim = new Timer(20,this);
	boolean[] arrowDown = new boolean[4];
	boolean readMap = false;
	private final static int monsterIdStart = 1000;
	
	private static boolean LOCK_LEFT = false;
	private static boolean LOCK_RIGHT = false;
	private static boolean LOCK_UP = false;
	private static boolean LOCK_DOWN = false;
	
	private javazoom.jl.player.Player mp3Player;
	private static final String soundDir = "../sound/";
	private static final String mp3File = "../sound/title.mp3"; 
	
	private ArrayList<String> tiles = new ArrayList<String>();
	private ImageIcon[] groundTile = {new ImageIcon("../images/tiles/ground.png"),
										new ImageIcon("../images/tiles/topStop.png"),
										new ImageIcon("../images/tiles/leftStop.png"),
										new ImageIcon("../images/tiles/rightStop.png"),
										new ImageIcon("../images/tiles/bottomStop.png"),
										new ImageIcon("../images/tiles/leftTopCorner.png"),
										new ImageIcon("../images/tiles/rightTopCorner.png"),
										new ImageIcon("../images/tiles/leftBottomCorner.png"),
										new ImageIcon("../images/tiles/rightBottomCorner.png"),
										new ImageIcon("../images/tiles/exit.png"),
										new ImageIcon("../images/tiles/highGround.png"),
										new ImageIcon("../images/tiles/invLeftTopCorner.png"),
										new ImageIcon("../images/tiles/invRightTopCorner.png"),
										new ImageIcon("../images/tiles/invLeftBottomCorner.png"),
										new ImageIcon("../images/tiles/invRightBottomCorner.png")};
	
	private ImageIcon[] playerImg = {new ImageIcon("../images/players/player.png"),
									new ImageIcon("../images/players/otherPlayer.png")};
	
	protected static LinkedList<Player> player = new LinkedList<Player>();
	protected static LinkedList<MonsterEcho> monster = new LinkedList<MonsterEcho>();
	
	Connection conn = null;
	private static final long sleep = 5;
	
	public GameArea(int id) {
		myId = id;
		
      	this.addKeyListener(this);
      	this.setBackground(Color.WHITE);
      	this.setDoubleBuffered(true);
      	
      	tim.addActionListener(this);
		tim.start();
		connect(true, "80:50:1:1");	//try to connect, "true" because its the first time
		
		createBgSound();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int imgInt;
		for (int i=0;i<tiles.size();i++) {
			for (int j=0;j < (tiles.get(i).length());j++) {
				switch(tiles.get(i).charAt(j)) {
				case ' ':
					imgInt = 0;
					g.drawImage(groundTile[imgInt].getImage(), TILE_SIZE*j, TILE_SIZE*i, TILE_SIZE, TILE_SIZE, null);
					break;
				case 'T':
					imgInt = 1;
					g.drawImage(groundTile[imgInt].getImage(), TILE_SIZE*j, TILE_SIZE*i, TILE_SIZE, TILE_SIZE, null);
					break;
				case 'L':
					imgInt = 2;
					g.drawImage(groundTile[imgInt].getImage(), TILE_SIZE*j, TILE_SIZE*i, TILE_SIZE, TILE_SIZE, null);
					break;
				case 'R':
					imgInt = 3;
					g.drawImage(groundTile[imgInt].getImage(), TILE_SIZE*j, TILE_SIZE*i, TILE_SIZE, TILE_SIZE, null);
					break;
				case 'Q':
					imgInt = 4;
					g.drawImage(groundTile[imgInt].getImage(), TILE_SIZE*j, TILE_SIZE*i, TILE_SIZE, TILE_SIZE, null);
					break;
				case 'C':
					imgInt = 5;
					g.drawImage(groundTile[imgInt].getImage(), TILE_SIZE*j, TILE_SIZE*i, TILE_SIZE, TILE_SIZE, null);
					break;
				case 'V':
					imgInt = 6;
					g.drawImage(groundTile[imgInt].getImage(), TILE_SIZE*j, TILE_SIZE*i, TILE_SIZE, TILE_SIZE, null);
					break;
				case 'B':
					imgInt = 7;
					g.drawImage(groundTile[imgInt].getImage(), TILE_SIZE*j, TILE_SIZE*i, TILE_SIZE, TILE_SIZE, null);
					break;
				case 'N':
					imgInt = 8;
					g.drawImage(groundTile[imgInt].getImage(), TILE_SIZE*j, TILE_SIZE*i, TILE_SIZE, TILE_SIZE, null);
					break;
				case 'E':
					imgInt = 9;
					g.drawImage(groundTile[imgInt].getImage(), TILE_SIZE*j, TILE_SIZE*i, TILE_SIZE, TILE_SIZE, null);
					break;
				case 'H':
					imgInt = 10;
					g.drawImage(groundTile[imgInt].getImage(), TILE_SIZE*j, TILE_SIZE*i, TILE_SIZE, TILE_SIZE, null);
					break;
				case 'I':
					imgInt = 11;
					g.drawImage(groundTile[imgInt].getImage(), TILE_SIZE*j, TILE_SIZE*i, TILE_SIZE, TILE_SIZE, null);
					break;
				case 'O':
					imgInt = 12;
					g.drawImage(groundTile[imgInt].getImage(), TILE_SIZE*j, TILE_SIZE*i, TILE_SIZE, TILE_SIZE, null);
					break;
				case 'P':
					imgInt = 13;
					g.drawImage(groundTile[imgInt].getImage(), TILE_SIZE*j, TILE_SIZE*i, TILE_SIZE, TILE_SIZE, null);
					break;
				case 'U':
					imgInt = 14;
					g.drawImage(groundTile[imgInt].getImage(), TILE_SIZE*j, TILE_SIZE*i, TILE_SIZE, TILE_SIZE, null);
					break;
				default:
					System.out.println("map failure");
				}
			}	
		}
		
		imgInt = 0;
		for (int i = 0; i < player.size(); i++) {
			if(player.get(i).getId() != 0.0){
				Player p = player.get(i);
				imgInt = (p.getId()==myId) ? 0 : 1; 
				g.drawImage(playerImg[imgInt].getImage(),
						(p.getXpos()-p.getTurned()*(playerImg[imgInt].getIconWidth()/2)),
						p.getYpos(),
						(p.getTurned()*playerImg[imgInt].getIconWidth()),
						(playerImg[imgInt].getIconHeight()*p.getDead()),
						null);
			}
		}
		
		for (int i=0;i<monster.size();i++) {
			MonsterEcho m = monster.get(i);
			ImageIcon img = ImageBank.getImage(m.getMonsterId());
			
			g.drawImage(img.getImage(), 
					m.getXpos()-m.getTurned()*(img.getIconWidth()/2),
					m.getYpos(), 
					img.getIconWidth()*monster.get(i).getTurned(),
					img.getIconHeight(),
					null);
		}
		
		if(Zincgull.isMouseActive()){
			this.requestFocus();
		}
	}

	//keep receiving messages from the server
	public void run() {
		
		
			if(!readMap) {
				new client.LoadMap();
				
				for(int i=0;i<LoadMap.getMapSegment().size();i++) {
					tiles.add(LoadMap.getMapSegment().get(i));
				}
				
				repaint();
				readMap = true;
			}
			
			try {
				while (true) {
					
					//Keep database connection alive
					if(conn == null) conn = Database.connect();
					
					String coords = dis.readUTF();
					if (!specialCommand(coords)) {
						
						String[] temp;
						temp = coords.split(":");
						
						if(Integer.valueOf(temp[4]) < monsterIdStart) { //if player
							Player ps = player.get(getId(Integer.valueOf(temp[4])));
							if( !temp[4].equals( Integer.toString(myId) ) ){		//only paint new coordinates if they didnt come from this client
								ps.setXpos(Integer.parseInt(temp[0]));
								ps.setYpos(Integer.parseInt(temp[1]));
								ps.setTurned(Integer.parseInt(temp[2]));
								ps.setSpeed(Integer.parseInt(temp[3]));
								repaint();
							}	
						}
						else if((getMonster(temp[4]))==-1) { //Since not player, if monster not already added
							System.out.println("add monster");
							int x = Integer.parseInt(temp[0]);	//X-pos
							int y = Integer.parseInt(temp[1]);	//Y-pos
							int t = Integer.parseInt(temp[2]);	//turned (1/0)
							int mi = Integer.parseInt(temp[3]);	//MonsterType-id, NOT UNIQUE
							int i = Integer.parseInt(temp[4]);	//id, UNIQUE
							int h = Integer.parseInt(temp[5]);	//health, the current health of the monster
							
							monster.add(new MonsterEcho(x,y,t,i,mi,h,conn));
							
							repaint();
						}
						else { //not player and already added, update the MonsterEcho-object.
														
							MonsterEcho m = monster.get(getMonster(temp[4]));
							
							m.setXpos(Integer.parseInt(temp[0]));
							m.setYpos(Integer.parseInt(temp[1]));
							m.setTurned(Integer.parseInt(temp[2]));
							//m.setMonsterId(Integer.parseInt(temp[3])); 	No need, it will never change
							//m.setId(Integer.parseInt(temp[4])); 			No need, it will never change
							m.setHealth(Integer.parseInt(temp[5]));
							
							repaint();
						}
						
					}
					
				}
			} catch( IOException ie ) {
				
				try {
					conn.close();
				} catch (SQLException e) {}
				
				Player p = player.get(getId(myId));
				Chat.chatOutput.append(Zincgull.getTime()+": MAP: Connection reset, reconnecting\n");
				int x = p.getXpos();
				int y = p.getYpos();
				int t = p.getTurned();
				int s = p.getSpeed();
				Zincgull.connected = false;
				player.clear();
				repaint();
				connect(false, x+":"+y+":"+t+":"+s);
				return;
			}
		
	}

	private int getMonster(String sid) {
		int id = Integer.valueOf(sid);
		for (int i = 0; i < monster.size(); i++) {
			if(monster.get(i).getId() == id) {	//needs to be unique
				return i;
			}
		}
		return -1;
	}

	private void sendData() {
		Player p = player.get(getId(myId));
		try {
			dos.writeUTF( p.getXpos() +":"+ p.getYpos() +":"+ p.getTurned() +":"+ p.getSpeed() +":"+ myId);
		} catch( IOException ie ) { 
			//Chat.chatOutput.append( Zincgull.getTime()+": MAP: Can't send coordinates\n" );
		}
	}
	
	public void connect(boolean first, String position) {
		while (true) {
			try {
				socket = new Socket(Zincgull.host, port);
				//create streams for communication
				dis = new DataInputStream( socket.getInputStream() );
				dos = new DataOutputStream( socket.getOutputStream() );
				dos.writeUTF("/HELLO "+position+":"+myId);
				// Start a background thread for receiving coordinates
				new Thread( this ).start();		//starts run()-method
				
				if(!first) Chat.chatOutput.append(Zincgull.getTime()+": MAP: Connected to mapserver\n");
				return;
			} catch( IOException e ) { 
				//System.out.println(e);
				if(first){
					Chat.chatOutput.append(Zincgull.getTime()+": MAP: Can't connect to server, trying again\n");
					first = false;
				}
				//Sleep a bit
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {}
			}
		}
	}
	
	public static int getId(int d){
		for (int i = 0; i < player.size(); i++) {
			if( player.get(i).getId() == d ){	//needs to be unique
				return i;
			}
		}
		return 0;
	}
	
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()>=37 && e.getKeyCode()<=40){
			arrowDown[40-e.getKeyCode()]=true;
		}
	}
	
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode()>=37 && e.getKeyCode()<=40)
			arrowDown[40-e.getKeyCode()]=false;
	}

	public void keyTyped(KeyEvent e) {}

	public void actionPerformed(ActionEvent arg0) {
		if ( Zincgull.connected ) {
			if (Zincgull.isMouseActive()&&(arrowDown[0]||arrowDown[1]||arrowDown[2]||arrowDown[3])) {
				calculateMove();
				sendData();
				repaint();	
			}else{
				for (int i = 0; i < arrowDown.length; i++) {
					arrowDown[i] = false;
				}
			}
		}
	}

	private void calculateMove() {
		Player p = player.get(getId(myId));
		if(p.getDead()==-1) return;
		int j = 0;
		for (int i = 0; i < arrowDown.length; i++) {
			if(arrowDown[i]) j++;
			if(j>2) return;
		}
		
		if(arrowDown[3]&&arrowDown[1]) return;
		else if(arrowDown[0]&&arrowDown[2]) return;
		
		checkCollision(p);
		
		if(!LOCK_LEFT && arrowDown[3]) {
			p.setXpos(p.getXpos()-p.getSpeed());
			p.setTurned(Player.TURNED);
		}
		
		if(!LOCK_RIGHT && arrowDown[1]) {
			p.setXpos(p.getXpos()+p.getSpeed());
			p.setTurned(Player.NOT_TURNED);
		}
		
		if(!LOCK_UP && arrowDown[2]) {
			p.setYpos(p.getYpos()-p.getSpeed());
		}
		
		if(!LOCK_DOWN && arrowDown[0]) {
			p.setYpos(p.getYpos()+p.getSpeed());
		}	
		
		if(p.getDead() != -1) {
			for(int i=0;i<monster.size();i++) {
				checkMonsterCollision(p, monster.get(i));
			}
		}
	}
	
	private void checkMonsterCollision(Player p, MonsterEcho e) {
		int px1 = p.getXpos() - (TILE_SIZE/2);
		int px2 = p.getXpos() + (TILE_SIZE/2);
		int py1 = p.getYpos();
		int py2 = p.getYpos() + TILE_SIZE;
		
		int ex1 = e.getXpos() - (TILE_SIZE/2);
		int ex2 = e.getXpos() + (TILE_SIZE/2);
		int ey1 = e.getYpos();
		int ey2 = e.getYpos() + TILE_SIZE;
		
		if(px1 > ex1 && px1 < ex2  &&
				((py1 > ey1 && py1 < ey2) || (py2 > ey1 && py2 < ey2))) {
			p.setDead(-1);
		}
		if(px2 < ex2 && px2 > ex1 &&
				((py1 > ey1 && py1 < ey2) || (py2 > ey1 && py2 < ey2))) {
			p.setDead(-1);
		}

		if(p.getDead() == -1) playSound("playerDead");
	}

	private void checkCollision(Player p) {
		int tx1 = (p.getXpos()-(TILE_SIZE/2))/TILE_SIZE;
		int tx2 = ((p.getXpos()-(TILE_SIZE/2))+TILE_SIZE)/TILE_SIZE;
		int ty1 = p.getYpos()/TILE_SIZE;
		int ty2 = (p.getYpos()+TILE_SIZE)/TILE_SIZE;
		
		char tile1;
		char tile2;
		
		if(arrowDown[3]) {
			int stx1 = (p.getXpos()-(TILE_SIZE/2)-p.getSpeed())/TILE_SIZE; //With Simulated Move
			tile1 = LoadMap.getTile(stx1, ty1);
			tile2 = LoadMap.getTile(stx1, ty2);
			if((tile1!=' ' && tile1!='E') || (tile2!=' ' && tile2!='E')) {
				LOCK_LEFT = true;
			}
			else {
				LOCK_LEFT = false;
			}
		}
		
		if(arrowDown[1]) {
			int stx2 = ((p.getXpos()-(TILE_SIZE/2))+TILE_SIZE+p.getSpeed())/TILE_SIZE; //With Simulated Move
			tile1 = LoadMap.getTile(stx2, ty1);
			tile2 = LoadMap.getTile(stx2, ty2);
			if((tile1!=' ' && tile1!='E') || (tile2!=' ' && tile2!='E')) {
				LOCK_RIGHT = true;
			}
			else {
				LOCK_RIGHT = false;
			}
		}
		
		if(arrowDown[2]) {
			int sty1 = (p.getYpos()-p.getSpeed())/TILE_SIZE; //With Simulated Move
			tile1 = LoadMap.getTile(tx1, sty1);
			tile2 = LoadMap.getTile(tx2, sty1);
			if((tile1!=' ' && tile1!='E') || (tile2!=' ' && tile2!='E')) {
				LOCK_UP = true;
			}
			else {
				LOCK_UP = false;
			}
		}
		
		if(arrowDown[0]) {
			int sty2 = (p.getYpos()+TILE_SIZE+p.getSpeed())/TILE_SIZE; //With Simulated Move
			tile1 = LoadMap.getTile(tx1, sty2);
			tile2 = LoadMap.getTile(tx2, sty2);
			if((tile1!=' ' && tile1!='E') || (tile2!=' ' && tile2!='E')) {
				LOCK_DOWN = true;
			}
			else {
				LOCK_DOWN = false;
			}
		}	
	}
	
	//possible commands the server can send
	public boolean specialCommand( String msg ){
		String[] temp;
		temp = msg.substring(5).split(":");
		if( msg.substring(0, 4).equals("/ADD") ){
			int x = Integer.parseInt(temp[0]);
			int y = Integer.parseInt(temp[1]);
			int s = Integer.parseInt(temp[2]);
			int t = Integer.parseInt(temp[3]);
			int i = Integer.parseInt(temp[4]);	
			
			player.add(new Player(x,y,t,s,i));
			Zincgull.connected = true;
			repaint();
			return true;
			
		}else if( msg.substring(0, 4).equals("/SUB") ){
			//player.remove(getId( Double.parseDouble(temp[4]) ));
			player.set(getId(Integer.parseInt(msg.substring(5))), new Player(0,0,0,0,0));
			repaint();
			return true;
		}else if( msg.substring(0, 6).equals("/HELLO") ){
			Chat.chatOutput.append(Zincgull.getTime()+": "+msg.substring(7)+"\n");
			return true;
		}
		return false;
	}
	
	/**
	 * Plays specified mp3-file.
	 * <br> 
	 * More or Less Copy/Paste from http://www.cs.princeton.edu/introcs/faq/mp3/MP3.java.html but without any means of stopping the sound.
	 * Added awesome looping capabilities. =D
	 */
	private void createBgSound() {		
		try {
            FileInputStream fis     = new FileInputStream(mp3File);
            BufferedInputStream bis = new BufferedInputStream(fis);
            mp3Player = new javazoom.jl.player.Player(bis);
        }
        catch (Exception e) {
            System.out.println("Problem playing file " + mp3File);
            System.out.println(e);
        }

        // run in new thread to play in background
        new Thread() {
            public void run() {
                try { 
                	mp3Player.play();
                }
                catch (Exception e) { 
                	System.out.println(e); 
                	try {Thread.sleep(5000);} catch (InterruptedException e1) {}
                }
                finally {
                	createBgSound(); //Rekursion ftw
                }
                
            }
        }.start();
	}
	
	private void playSound(String sound) {
		playSound(sound, false);
	}
	
	private void playSound(String sound, boolean loop) {
		//Remove file-extension before comparison if any 
		if(sound.endsWith(".mp3")) sound.substring(0, (sound.length()-5));
		
		//check sound-name against template, else try to load with the provided name
		if(sound.equalsIgnoreCase("playerDeath") || sound.equalsIgnoreCase("playerDead")) sound = "playerDeath";
		
		//Add default file-extension
		sound = sound.concat(".mp3");
		
		try {
			//Add dir-path and try to open file
            FileInputStream fis = new FileInputStream(soundDir+sound);
            BufferedInputStream bis = new BufferedInputStream(fis);
            mp3Player = new javazoom.jl.player.Player(bis);
            
            //run in new thread to play in background
            new Thread() {
                public void run() {
                    try { 
                    	mp3Player.play();
                    }
                    catch (Exception e) { 
                    	System.out.println(e);
                    }
                    
                }
            }.start();
        }
        catch (Exception e) {
            System.out.println("Problem playing file " + sound);
            System.out.println(e);
        }
	}
}
