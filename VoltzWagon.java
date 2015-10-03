package VoltzWagon;
import robocode.*;
import java.awt.Color;
import java.lang.Math;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * VoltzWagon - a robot by Dave and John
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
	public boolean dir = true;
	public double oldEng = 100.0;
	public int shotCount = 0;
	public int hitCount = 5;
	public int missedCount = 0;
	public int targetDist = 250;
	public int fireFlag = 0;
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
			updateScanner();
            
			if (enemy != null) {
            	if (oldEng-enemy.getEnergy()>=1 && oldEng-enemy.getEnergy()<=3) {shotCount++;}

            	oldEng=enemy.getEnergy();
			}
			
			executePer();
			
		}
	}

	public void updateScanner() {
		if (enemy == null) {
			// No enemy seen yet
			// Scan clockwise for now, 45 degrees/tick
			setTurnRadarRight(45);
			System.out.println("Scanning...");
		}
		else if (getTime() - enemy.getTickSeen() > TICK_TIMEOUT) {
			// Data is old and unreliable
			enemy = null;
			System.out.println("Throwing out old data...");
		}
		else {
			// Take action based on data we have
			// Calculate target's position
			double targAngle = getHeading() + enemy.getBearing();
			if (targAngle < 0) targAngle += 360;
			if (targAngle > 360) targAngle -= 360;
			
			// Calculate the amount the radar must move (account for rollover)
			double radarBearing = targAngle - getRadarHeading();
			if (radarBearing < -180)
				radarBearing += 360;
			else if (radarBearing > 180)
				radarBearing -= 360;
			
			// Move as far as we can in one tick,
			// overshoot in case they move
			if (radarBearing < 0) {
				if (Math.abs(radarBearing) > 45)
					setTurnRadarLeft(45);
				else
					setTurnRadarLeft(Math.min(Math.abs(radarBearing) + 45/2, 45));
			}
			else {
				if (radarBearing > 45)
					setTurnRadarRight(45);
				else
					setTurnRadarRight(Math.min(radarBearing + 45/2, 45));
			}
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
        
		// Replace the next line with any behavior you would like
		
		fireFlag = (int) hitCount/5;
		
		double toTurn = (getHeading()-getGunHeading())+e.getBearing();
		if (toTurn>360) {toTurn-=360;}
		if (toTurn<-360) {toTurn+=360;}
        
        double enemyVAngle = e.getHeading() + getHeading() + e.getBearing();
        if (enemyVAngle > 360) enemyVAngle -= 360;
        if (enemyVAngle < 0) enemyVAngle += 360;
        
        double bulletVelocity = 20 - 3 * Math.min(fireFlag, 3);
        double flightTime = e.getDistance() / (bulletVelocity - e.getVelocity() * Math.cos(Math.toRadians(enemyVAngle)));
        double leadAngle = Math.toDegrees(Math.asin((flightTime * e.getVelocity() * Math.sin(Math.toRadians(enemyVAngle))) /
                                                    (e.getDistance() + flightTime * e.getVelocity() * Math.cos(Math.toRadians(enemyVAngle)))));
                                                    
        System.out.println("-----");
        System.out.println("Bullet velocity is " + Double.toString(bulletVelocity));
        System.out.println("enermyVAngle is " + Double.toString(enemyVAngle));
        System.out.println("Lead angle is " + Double.toString(leadAngle));
        
        toTurn += leadAngle;
		if (toTurn>360) {toTurn-=360;}
		if (toTurn<-360) {toTurn+=360;}
        
        
        
        
        
		if (toTurn>180) {
			setTurnGunLeft(toTurn-180);
		} else {
			setTurnGunRight(toTurn);
		}
		//turnRight(e.getBearing());
		//System.out.println(Double.toString(toTurn));
		
		if (e.getDistance()>250) {
			toTurn = e.getBearing()+45;
		} else if (e.getDistance()<200) {
			toTurn = e.getBearing()+135;
		} else {
			toTurn = e.getBearing()+90;
		}
		
		if (!dir) {
			//System.out.println("DIR!");
			if (e.getDistance()>250) {
				toTurn = e.getBearing()-180+135;
			} else if (e.getDistance()<200) {
				toTurn = e.getBearing()-180+45;
			} else {
				toTurn = e.getBearing()-90;
			}
		}

		//System.out.println(Double.toString(toTurn));
		setTurnRight(toTurn);
		setAhead(30);
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		//back(10);
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		setTurnRight(180);
		setAhead(30);
		dir = !dir;
		updateScanner();
		executePer();
	}	
	
	// Fires every tick
	public void onStatus(StatusEvent e) {
		RobotStatus status = e.getStatus();
		
	}
	
  	// Fires every tick
	public void onBulletHit(BulletHitEvent e) {
		hitCount++;		
	}
  
    public void onBulletMissed(BulletMissedEvent e) {
		missedCount++;
		System.out.println("Missed :(");
		if (missedCount == 2 && targetDist>0) {
			targetDist-=20;
			System.out.println("New Target Distance: "+Integer.toString(targetDist));
			missedCount=0;
		}
		
	}
	
	public void executePer() {
		execute();	
		if (fireFlag!=0) {
			setFire(fireFlag);
			fireFlag=0;
		}
	}
	
}
