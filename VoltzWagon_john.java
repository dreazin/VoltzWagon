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
	private final double TICK_TIMEOUT = 7;
	
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
				setTurnRadarRight(45);
			}
			else if (getTime() - enemy.getTickSeen() > TICK_TIMEOUT) {
				// Data is old and unreliable
				enemy = null;
			}
			else {
				// Take action based on data we have
				System.out.println("Robot heading is " + Double.toString(getHeading()));
				System.out.println("Enemy bearing is " + Double.toString(enemy.getBearing()));
				double targAngle = getHeading() + enemy.getBearing();
				if (targAngle < 0) targAngle += 360;
				if (targAngle > 360) targAngle -= 360;
				System.out.println("targ angle is " + Double.toString(targAngle));
				
				double radarBearing = targAngle - getRadarHeading();
				if (radarBearing > 180)
					targAngle = getRadarHeading() - targAngle;
				if (radarBearing < 0) {
					System.out.println("Turning radar left");
					if (Math.abs(radarBearing) > 45)
						setTurnRadarLeft(45);
					else
						setTurnRadarLeft(Math.abs(radarBearing));
				}
				else {
					System.out.println("Turning radar right");
					if (radarBearing > 45)
						setTurnRadarRight(45);
					else
						setTurnRadarRight(radarBearing);
				}
				System.out.println("Enemy is absolute " + Double.toString(targAngle));
				System.out.println("Radar is currently " + Double.toString(getRadarHeading()));
				System.out.println("Radar bearing is " + Double.toString(radarBearing));
			}
			setAhead(20);
			
			execute();
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
