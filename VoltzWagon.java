package VoltzWagon;
import robocode.*;
import robocode.util.Utils;
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
	
	public class Coordinate {
		public double x;
		public double y;
		
		public Coordinate(double x,double y) {
			this.x=x;
			this.y=y;
		}
		public Coordinate() {
			this.x=0;
			this.y=0;
		}
		public void set (double x, double y) {
			this.x=x;
			this.y=y;
		}
	}
	

	public class Intercept {
	 public Coordinate impactPoint = new Coordinate(0,0);
	 public double bulletHeading_deg;
	
	 protected Coordinate bulletStartingPoint = new Coordinate();
	 protected Coordinate targetStartingPoint = new Coordinate();
	 public double targetHeading;
	 public double targetVelocity;
	 public double bulletPower;
	 public double angleThreshold;
	 public double distance;
	
	 protected double impactTime;
	 protected double angularVelocity_rad_per_sec;
	
	 public void calculate (
	
	 // Initial bullet position x coordinate 
	 double xb, 
	 // Initial bullet position y coordinate
	 double yb, 
	 // Initial target position x coordinate
	 double xt, 
	 // Initial target position y coordinate
	 double yt, 
	 // Target heading
	 double tHeading, 
	 // Target velocity
	 double vt, 
	 // Power of the bullet that we will be firing
	 double bPower, 
	 // Angular velocity of the target
	 double angularVelocity_deg_per_sec 
	)
	{
	angularVelocity_rad_per_sec = 
	 Math.toRadians(angularVelocity_deg_per_sec);
	
	bulletStartingPoint.set(xb, yb);
	targetStartingPoint.set(xt, yt);
	
	targetHeading = tHeading;
	targetVelocity = vt;
	bulletPower = bPower;
	double vb = 20-3*bulletPower;
	
	double dX,dY;
	
	// Start with initial guesses at 10 and 20 ticks
	impactTime = getImpactTime(10, 20, 0.01); 
	impactPoint = getEstimatedPosition(impactTime);
	
	dX = (impactPoint.x - bulletStartingPoint.x);
	dY = (impactPoint.y - bulletStartingPoint.y);
	
	distance = Math.sqrt(dX*dX+dY*dY);
	
	bulletHeading_deg = Math.toDegrees(Math.atan2(dX,dY));
	angleThreshold = Math.toDegrees
	 (Math.atan(1/distance));
	}
	
	protected Coordinate getEstimatedPosition(double time) {
	
	double x = targetStartingPoint.x + 
	   targetVelocity * time * Math.sin(Math.toRadians(targetHeading));
	double y = targetStartingPoint.y + 
	   targetVelocity * time * Math.cos(Math.toRadians(targetHeading));
	return new Coordinate(x,y);
	}
	
	private double f(double time) {
	
	double vb = 20-3*bulletPower;
	
	Coordinate targetPosition = getEstimatedPosition(time);
	double dX = (targetPosition.x - bulletStartingPoint.x);
	double dY = (targetPosition.y - bulletStartingPoint.y);
	
	return Math.sqrt(dX*dX + dY*dY) - vb * time;
	}
	
	private double getImpactTime(double t0, 
	  double t1, double accuracy) {
	
	double X = t1;
	double lastX = t0;
	int iterationCount = 0;
	double lastfX = f(lastX);
	
	while ((Math.abs(X - lastX) >= accuracy) && 
	  (iterationCount < 15)) {
	
	iterationCount++;
	double fX = f(X);
	
	if ((fX-lastfX) == 0.0) break;
	
	double nextX = X - fX*(X-lastX)/(fX-lastfX);
	lastX = X;
	X = nextX;
	lastfX = fX;
	}
	
	return X;
	}
	}

	public class CircularIntercept extends Intercept {
	 protected Coordinate getEstimatedPosition(double time) {
	  if (Math.abs(angularVelocity_rad_per_sec) 
	   <= Math.toRadians(0.1)) {
	  return super.getEstimatedPosition(time);
	 }
	
	    double initialTargetHeading = Math.toRadians(targetHeading);
	    double finalTargetHeading   = initialTargetHeading +  
	     angularVelocity_rad_per_sec * time;
	    double x = targetStartingPoint.x - targetVelocity /
	     angularVelocity_rad_per_sec *(Math.cos(finalTargetHeading) - 
	     Math.cos(initialTargetHeading));
	    double y = targetStartingPoint.y - targetVelocity / 
	     angularVelocity_rad_per_sec *
	     (Math.sin(initialTargetHeading) - 
	     Math.sin(finalTargetHeading));
	    return new Coordinate(x,y);
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
	public int ROBOT_RADIUS = 1;
	public void run() {
		// Initialization of the robot should be put here
		
		System.out.println("In Main!");

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
		
		double absAngle = getHeading() + e.getBearing();		

		//System.out.println("absAngle: " + absAngle);		

		if (absAngle>360) {absAngle-=360;}
		if (absAngle<-360) {absAngle+=360;}
		
		double toTurn2 = smartFire(getX(), getY(), getX()+(e.getDistance()*Math.sin(Math.toRadians(absAngle))), getY()+(e.getDistance()*Math.cos(Math.toRadians(absAngle))),e.getHeading(),e.getVelocity(), fireFlag);
		double toTurn = (toTurn2-getGunHeading());
		if (toTurn>=360) {toTurn-=360;}
		if (toTurn<=-360) {toTurn+=360;}
		


		if (toTurn>180) {
			setTurnGunRight(toTurn-360);
			System.out.println("Turn 1: "+Double.toString(toTurn-360));
			if (toTurn-360>20 || toTurn-360<-20) {fireFlag=0;}
		} else {
			if (toTurn<-180) {
				setTurnGunRight(toTurn+360);
				System.out.println("Turn 2: "+Double.toString(toTurn+360));
				if (toTurn+360>20 || toTurn+360<-20) {fireFlag=0;}
			} else {
				setTurnGunRight(toTurn);
				System.out.println("Turn 3: "+Double.toString(toTurn));
				if (toTurn>20 || toTurn<-20) {fireFlag=0;}
			}
		}
		//turnRight(e.getBearing());
		//setTurnGunRight(Utils.normalRelativeAngle(toTurn));
		//System.out.println(Double.toString(toTurn));
		//System.out.println(Double.toString(toTurn+360));
		
		if (e.getDistance()>targetDist) {
			toTurn = e.getBearing()+45;
		} else if (e.getDistance()<targetDist-50) {
			toTurn = e.getBearing()+135;
		} else {
			toTurn = e.getBearing()+90;
		}
		
		if (!dir) {
			//System.out.println("DIR!");
			if (e.getDistance()>targetDist) {
				toTurn = e.getBearing()-180+135;
			} else if (e.getDistance()<targetDist-50) {
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
	 * onHitWall: What to do when you hit a wall	 */
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
		//System.out.println("Missed :(");
		if (missedCount == 2 && targetDist>=20) {
			targetDist-=20;
			//System.out.println("New Target Distance: "+Integer.toString(targetDist));
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
	
	public double smartFire(double ourRobotPositionX, double ourRobotPositionY,double currentTargetPositionX,double currentTargetPositionY,double curentTargetHeading_deg,double currentTargetVelocity,int bulletPower) {
		
		//System.out.println("Our X: "+Double.toString(ourRobotPositionX));
		//System.out.println("Our Y: "+Double.toString(ourRobotPositionY));
		//System.out.println("Their heading: "+Double.toString(curentTargetHeading_deg));
		//System.out.println("Their velocit: "+Double.toString(currentTargetVelocity));
		Intercept intercept = new Intercept();
		intercept.calculate
		(
		  ourRobotPositionX,
		  ourRobotPositionY,
		  currentTargetPositionX,
		  currentTargetPositionY,
		  curentTargetHeading_deg,
		  currentTargetVelocity,
		  bulletPower,
		  0 // Angular velocity
		);
		
		// Helper function that converts any angle into  
		// an angle between +180 and -180 degrees.
		double turnAngle = Utils.normalRelativeAngle(intercept.bulletHeading_deg - getGunHeading());
		
		//System.out.println("angle to shoot: "+Double.toString(intercept.bulletHeading_deg));
		
		return intercept.bulletHeading_deg;

	}
}