package server;

import java.util.Random;

import local.GlobalConstants;
import client.Sprite;

/**
 * 
 * 
 * 
 * @author Andreas
 */
public class Monster extends Sprite implements Runnable, GlobalConstants{
	
	Thread thread = new Thread(this);
	
	//private static final int idleSleep = 2000;
	private int activeSleep = 50;
	//private static final int range = 5;

	public static final int DEFAULT_XPOS = 300;
	public static final int DEFAULT_YPOS = 320;
	
	private int id;
	private int index;
	
	//Stats
	private int monsterId;
	private String name;
	private int damage;
	private int health;
	private int level;
	private int aggro;
	private String spawnLocation;
	private boolean boss;
	
	//Active
	private int state;
	//private static final int DEAD = -1;
	//private static final int WAITING = 0;
	//private static final int ATTACKING = 1;
	private static final int MOVING = 2;

	private static final int MOVING_RIGHT = 2;
	private static final int MOVING_LEFT = 3;
	
	private double dx;
	private double dy;
	
	private boolean alive;
	
	public Monster() {
		id = 0;
		xpos = DEFAULT_XPOS;
		ypos = DEFAULT_YPOS;
		
		speed = 3;
	}
	
	private void randomDirection() {
		//Randomize movement direction
		Random randomize = new Random();
		int angle = randomize.nextInt(4);
		
		System.out.println("RANDOM ANGLE: "+angle);
		
		setDirection(angle);
	}
	
	private void setDirection(double angle) {
		//if not valid angle
		if(angle<0 || angle>360) return;
		
		double speed = (double)(this.speed);
		
		if(angle == 0) {
			dx = 1;
			dy = 0;
		} else if(angle == 1) {
			dx = -1;
			dy = 0;
		} else if(angle == 2) {
			dx = 0;
			dy = 1;
		} else {
			dx = 0;
			dy = -1;
		}
		
		
		
		/*if(angle<=90) {
			
			if(((angle/90)) >= 0.5) {
				dy = (angle/90);
				dx = (1-(angle/90));
			}
			else {
				dy = (1-(angle/90));
				dx = (angle/90);
			}
		}
		else if(angle>90 && angle<=180) {
			angle -= 90;
			
			if(((angle/90)) <= 0.5) {
				dy = (1-(angle/90));
				dx = -(angle/90);
			}
			else {
				dy = ((1-angle)/90);
				dx = -(angle/90);
			}
		}
		else if(angle>180 && angle<=270) {
			angle -= 180;
			
			if(((angle/90)) <= 0.5) {
				dy = -(angle/90);
				dx = -(1-(angle/90));
			}
			else {
				dy = -(angle/90);
				dx = -((1-angle)/90);
			}
		}
		else {
			angle -= 270;
			
			if(((angle/90)) <= 0.5) {
				dy = -((1-angle)/90);
				dx = (angle/90);
			}
			else {
				dy = -(1-(angle/90));
				dx = (angle/90);
			}
		}*/
		
		dx *= speed;
		dy *= speed;
		
		System.out.println("DX: "+dx);
		System.out.println("DY: "+dy);
		
		state = MOVING;
	}
	
	public synchronized void printStats() {
		System.out.println("--MONSTER STATS--");
		System.out.println("Id: "+id); //From Sprite
		System.out.println("Name: "+name);
		System.out.println("Health: "+health);
		System.out.println("Damage: "+damage);
		System.out.println("Level: "+level);
		System.out.println("Aggro: "+aggro);
		System.out.println("Speed: "+speed); //From Sprite
		System.out.println("Spawn: "+spawnLocation);
		System.out.println("Boss: "+boss);
		System.out.println("--------------");
	}
	
	//Override Sprite
	public String getCoords() {
		coords = (String.valueOf(xpos)+":"
				+ String.valueOf(ypos)+":"
				+ String.valueOf(turned)+":"
				+ String.valueOf(monsterId)+":"
				+ String.valueOf(id)+":"
				+ String.valueOf(health));
		
		return coords;
	}

	public void run() {
		
		alive = true;
		
		randomDirection();
		
		while(alive) {
			
			activeSleep = 100;
			//Update state
			update();
			
			move();
			if(collisionCheck()) randomDirection();
			
			
			
			/*
			if(state==WAITING) {
				//do nothing
				activeSleep = idleSleep;
			}
			else if(state==ATTACKING) {
				checkRange();
				
				move();
				
				attack();
				
				activeSleep = 20;
			}
			else if(state==DEAD) {
				alive = false;
				activeSleep = 0;
			}
			*/
			//Sleep a bit
			try {
				Thread.sleep(activeSleep);
			} catch (InterruptedException e) {}
			
		}
		
		MonsterService.dyingMonster(this);
		
	}
	
	private boolean collisionCheck() {
		/*int tileY = (ypos + (TILE_SIZE/2))/TILE_SIZE;
		
		if(dx < 0) {
			int tile = (xpos/TILE_SIZE)+1;
			if(xpos<tile && LoadMaps.getTile(tile, tileY)!=' '){
				xpos = tile;
			}
		}
		else if(dx > 0) {
			int tile = (xpos/TILE_SIZE);
			if(xpos>tile && LoadMaps.getTile(tile, tileY)!=' '){
				xpos = tile-1;
			}
		}*/
				
		int tx1 = (xpos-(TILE_SIZE/2))/TILE_SIZE;
		int tx2 = ((xpos-(TILE_SIZE/2))+TILE_SIZE)/TILE_SIZE;
		int ty1 = ypos/TILE_SIZE;
		int ty2 = (ypos+TILE_SIZE)/TILE_SIZE;
		
		if(tx1<0) tx1 = 0;
		if(tx2<0) tx2 = 0;
		if(ty1<0) ty1 = 0;
		if(ty2<0) ty2 = 0;
			
		if(dx<0) {
			if((LoadMaps.getTile(tx1, ty1)!=' ') || (LoadMaps.getTile(tx1, ty2)!=' ')) {
				int asdf = (tx1*TILE_SIZE);
				xpos = (asdf+2+(TILE_SIZE/2)+TILE_SIZE);

				return true;
			}
		}else if(dx>0) {
			if((LoadMaps.getTile(tx2, ty1)!=' ') || (LoadMaps.getTile(tx2, ty2)!=' ')) {
				int asdf = (tx2*TILE_SIZE);
				xpos = (asdf-2-(TILE_SIZE/2));

				return true;
			}
		}else if(dy<0) {
			if((LoadMaps.getTile(tx1, ty1)!=' ') || (LoadMaps.getTile(tx2, ty1)!=' ')) {
				int asdf = (ty1*TILE_SIZE)+TILE_SIZE;
				ypos = (asdf+2);

				return true;
			}
		}else if(dy>0) {
			if((LoadMaps.getTile(tx1, ty2)!=' ') || (LoadMaps.getTile(tx2, ty2)!=' ')) {
				int asdf = (ty2*TILE_SIZE)-TILE_SIZE;
				ypos = (asdf-2);

				return true;
			}
		}
		
		
		return false;
	}
	
//	private void checkRange() {
//		// TODO Auto-generated method stub
//		
//	}

	private void move() {		

		Random randomize = new Random();
		int asdf = randomize.nextInt(20);
		
		if(asdf==4) {
			randomDirection();
		}
		
		xpos = (int) (xpos + dx);
		ypos = (int) (ypos + dy);
		
		//update coords
		getCoords();
		
		//Send changes
		index = MapSrv.getMonsterIndex(id);
		MapSrv.monsterPositions.set(index, coords);
		MapSrv.sendToAll(coords);
	}

//	private void attack() {
//		// TODO Auto-generated method stub
//		
//	}
//
	private void update() {
		if(dx<0) turned = NOT_TURNED;
		else turned = TURNED;
		
	}
	
	
	//a Shitload of getters/setters
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getMonsterId() {
		return monsterId;
	}
	
	public void setMonsterId(int id) {
		this.monsterId = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getAggro() {
		return aggro;
	}

	public void setAggro(int aggro) {
		this.aggro = aggro;
	}

	public String getSpawnLocation() {
		return spawnLocation;
	}

	public void setSpawnLocation(String spawnLocation) {
		this.spawnLocation = spawnLocation;
	}

	public boolean isBoss() {
		return boss;
	}

	public void setBoss(boolean boss) {
		this.boss = boss;
	}
	
	public void setAlive(boolean alive) {
		this.alive = alive;
	}
	
	public boolean getAlive() {
		return alive;
	}
	
}