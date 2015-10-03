package VoltzWagon;
import robocode.*;
import java.awt.Color;
import java.lang.Math;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * VoltzWagon - a robot by (your name here)
 */
public class VoltzWagon extends AdvancedRobot
{
	private class SeenRobot {
		private long tickSeen;
		private double bearing,
					   distance,
					   energy,
					   heading,
					   velocity;
			
		public SeenRobot(ScannedRobotEvent e) {
			this.update(e);
		}
		
		public void update(ScannedRobotEvent e) {
			this.tickSeen = getTime();
			this.bearing = e.getBearing();
			this.distance = e.getDistance();
			this.energy = e.getEnergy();
			this.heading = e.getHeading();
			this.velocity = e.getVelocity();
		}
		
		public long getTickSeen() {
			return this.tickSeen;
		}
		
		public double getBearing() {
			return this.bearing;
		}
		
		public double getDistance() {
			return this.distance;
		}
		
		public double getEnergy() {
			return this.energy;
		}
		
		public double getHeading() {
			return this.heading;
		}
		
		public double getVelocity() {
			return this.velocity;
		}
	}
	
	// Only doing 1v1 so we can hard define out target
	private SeenRobot enemy;
	
	// Ticks after which a seen robot isn't reliable anymore
	private final double TICK_TIMEOUT = 20;
	
	/**
	 * run: VoltzWagon's default behavior
	 */
	public void run() {
		// Initialization of the robot should be put here
		enemy = null;
		setScanColor(Color.yellow);
		// Separate our turns
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		setColors(Color.red,Color.white,Color.blue); // body,gun,radar
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);

		// Robot main loop
		while(true) {
			/*----- Update Scanner Position -----*/
			if (enemy == null) {
				// No enemy seen yet
				// Scan clockwise for now, 45 degrees/tick
				turnRadarRight(45);
			}
			else if (getTime() - enemy.getTickSeen() > TICK_TIMEOUT) {
				// Data is old and unreliable
			}
			else {
				// Take action based on data we have
				
				double targAngle = getHeading() + enemy.getBearing();
				if (targAngle < 0) targAngle += 360;
				double radarBearing = targAngle - getRadarHeading();
				System.out.println("Radar bearing:");
				System.out.println(radarBearing);
				if (radarBearing < 0) {
					if (Math.abs(radarBearing) > 45)
						turnRadarLeft(45);
					else
						turnRadarLeft(Math.abs(radarBearing));
				}
				else {
					if (radarBearing > 45)
						turnRadarRight(45);
					else
						turnRadarRight(radarBearing);
				}	
			}
			ahead(20);
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		if (enemy == null)
			enemy = new SeenRobot(e);
		else
			enemy.update(e);
		/*double toTurn = (getHeading()-getGunHeading())+e.getBearing();
		if (toTurn>360) {toTurn-=360;}
		if (toTurn>180) {
			turnGunLeft(toTurn-180);
		} else {
			turnGunRight(toTurn);
		}
		//turnRight(e.getBearing());
		//System.out.println(Double.toString(toTurn));
		fire(3);//*/
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		back(10);
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		back(20);
	}
	
	// Fires every tick
	public void onStatus(StatusEvent e) {
		RobotStatus status = e.getStatus();
		
	}
	
}
