package dtownsend_svivanco;
import robocode.*;
import robocode.util.Utils;
import java.awt.*;
import java.awt.geom.*;
//import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * Ragnorobot - a robot by (your name here)
 */
public class dtownsend_vivanco extends AdvancedRobot
{
	/* times decided to not change direction. */
	public int sameDirectionCounter = 0;
 
	/* continue to move in the current direction */
	public long moveTime = 1;
 
	/* direction moving in */
	public static int moveDirection = 1;
 
	/* speed of the last bullet that hit me, determines distance 
	* before changing directions
	*/
	public static double lastBulletSpeed = 15.0;
 
	public double wallStick = 120;
	
	public void run() {
	
		setBodyColor(Color.pink);
		setGunColor(Color.white);
		setRadarColor(Color.pink);
		setBulletColor(Color.pink);
		setScanColor(Color.pink);
 
		setAdjustGunForRobotTurn(true); 
		setAdjustRadarForGunTurn(true); //keep the radar still while we turn
 
	
		while (true) {
			if (getRadarTurnRemaining() == 0.0)
		            	setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
	        	execute();
		}
	}

	/**
	 * when we see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
	
		/**
		 * returns the bearing to the robot you scanned
		 * returns the heading of the robot, in degrees
		 * 
		 * (I found this definition from robocode.sourceforge.net)
		 */
		 
		double bearing = e.getBearingRadians() + getHeadingRadians();
		double distance = e.getDistance() + (Math.random()-0.5)*5.0;
		
 			//radar, subtract current radar heading to get turn
	    	double radarTurn = Utils.normalRelativeAngle(bearing
			- getRadarHeadingRadians() );
 
		double baseScanSpan = (18.0 + 36.0*Math.random());
		
		// distance we want to scan from middle of enemy to either side
		double extraTurn = Math.min(Math.atan(baseScanSpan / distance), Math.PI/4.0);
			setTurnRadarRightRadians(radarTurn + (radarTurn < 0 ? -extraTurn : extraTurn));
 
		if(--moveTime <= 0) {
			distance = Math.max(distance, 100 + Math.random()*50) * 1.25;
			moveTime = 50 + (long)(distance / lastBulletSpeed);
 
			++sameDirectionCounter;
 
			/* change direction(?) */
			if(Math.random() < 0.5 || sameDirectionCounter > 16) {
				moveDirection = -moveDirection;
				sameDirectionCounter = 0;
			}
		}
 
 
		/* perpendicular to enemy, based on movement direction */
		double goalDirection = bearing-Math.PI/2.0*moveDirection;
 
		/* added randomness */
		goalDirection += (Math.random()-0.5) * (Math.random()*2.0 + 1.0);
 
		/* smooth around the walls, if we smooth too much, reverse direction! */
		double x = getX();
		double y = getY();
		double smooth = 0;
 
		/* calculates smoothing we would do if actually smoothed walls. */
		Rectangle2D fieldRect = new Rectangle2D.Double(18, 18, getBattleFieldWidth()-36, getBattleFieldHeight()-36);
 
		while (!fieldRect.contains(x+Math.sin(goalDirection)*wallStick, y+ Math.cos(goalDirection)*wallStick)) {
			/* turn a little toward enemy and try again */
			goalDirection += moveDirection*0.1;
			smooth += 0.1;
		}
 
		/* if we would have smoothed, then reverse direction. */
		/* add check*/
		if(smooth > 0.5 + Math.random()*0.125) {
			moveDirection = -moveDirection;
			sameDirectionCounter = 0;
		}
 
		double turn = Utils.normalRelativeAngle(goalDirection - getHeadingRadians());
 
		/* adjust so we drive backwards if its less*/
		if (Math.abs(turn) > Math.PI/2) {
			turn = Utils.normalRelativeAngle(turn + Math.PI);
			setBack(100);
		} else {
			setAhead(100);
		}
 
		setTurnRightRadians(turn);
 
 
		/* fire */
 		double bulletPower = 1.0 + Math.random()*2.0;
		double bulletSpeed = 20 - 3 * bulletPower;
		
		double latVel=e.getVelocity() * Math.sin(e.getHeadingRadians() -bearing);//enemies later velocity
		double gunTurnAmt;//amount to turn our gun
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//lock on the radar
	
		double enemyLatVel = e.getVelocity()*Math.sin(e.getHeadingRadians() - bearing);
		double escapeAngle = Math.asin(8.0 / bulletSpeed);

/* Signum produces 0 if it is not moving, meaning we will fire directly head on at an unmoving target */
	double enemyDirection = Math.signum(enemyLatVel);
	double angleOffset = escapeAngle * enemyDirection * Math.random();
	setTurnGunRightRadians(Utils.normalRelativeAngle(bearing + angleOffset - getGunHeadingRadians()));



	if (getEnergy() >= 50){		
		fire(3);
	}else if(getEnergy() < 50 && getEnergy() >= 20){
		fire(2);
	}else{
		fire(1);
}
		if(Math.random()>.9){
			setMaxVelocity((12*Math.random())+12);//randomly change speed
		}
		if (e.getDistance() > 150) {//if distance is greater than 150
			gunTurnAmt = robocode.util.Utils.normalRelativeAngle(bearing- getGunHeadingRadians()+latVel/22);//amount to turn our gun, lead just a little bit
			setTurnGunRightRadians(gunTurnAmt); //turn our gun
			setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(bearing-getHeadingRadians()+latVel/getVelocity()));//drive towards the enemies predicted future location
			setAhead((e.getDistance() - 140)*moveDirection);//move forward
			setFire(3);//fire
		}
		else{//if we are close enough...
			gunTurnAmt = robocode.util.Utils.normalRelativeAngle(bearing- getGunHeadingRadians()+latVel/15);//amount to turn our gun, lead just a little bit
			setTurnGunRightRadians(gunTurnAmt);//turn our gun
			setTurnLeft(-90-e.getBearing()); //turn perpendicular to the enemy
			setAhead((e.getDistance() - 140)*moveDirection);//move forward
			setFire(3);//fire
		}
	
}
 
	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		turnRight(45);
		back(50);
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		back(200);
		turnRight(45);
		ahead(50);
		scan();
	}	
}
