package VoltzWagon;
import robocode.*;
import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * VoltzWagon - a robot by (your name here)
 */
public class VoltzWagon extends AdvancedRobot
{
	/**
	 * run: VoltzWagon's default behavior
	 */
	public boolean dir = true;
	public void run() {
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		setColors(Color.red,Color.white,Color.blue); // body,gun,radar
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);

		// Robot main loop
		while(true) {
			// Replace the next 4 lines with any behavior you would like
			turnRadarRight(360);
			turnRadarRight(360);
			execute();
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// Replace the next line with any behavior you would like
		double toTurn = (getHeading()-getGunHeading())+e.getBearing();
		if (toTurn>360) {toTurn-=360;}
		if (toTurn<-360) {toTurn+=360;}
		if (toTurn>180) {
			turnGunLeft(toTurn-180);
		} else {
			turnGunRight(toTurn);
		}
		//turnRight(e.getBearing());
		//System.out.println(Double.toString(toTurn));
		fire(3);//
		
		if (e.getDistance()>250) {
			toTurn = e.getBearing()+45;
		} else if (e.getDistance()<200) {
			toTurn = e.getBearing()+135;
		} else {
			toTurn = e.getBearing()+90;
		}
		
		if (!dir) {
			System.out.println("DIR!");
			if (e.getDistance()>250) {
				toTurn = e.getBearing()-180+135;
			} else if (e.getDistance()<200) {
				toTurn = e.getBearing()-180+45;
			} else {
				toTurn = e.getBearing()-90;
			}
		}

		System.out.println(Double.toString(toTurn));
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
		turnRight(180);
		ahead(30);
		dir = !dir;
	}	
}
