package tank;

import robocode.JuniorRobot;

public final class CompetitiveStrategy implements Strategy {

    // Variable to track movement direction for evasive maneuvers
    private boolean movingForward = true;

    @Override
    public void run(JuniorRobot robot) {
        // Continuous scanning by turning the gun 360 degrees
        robot.turnGunRight(360);
    }

    @Override
    public void onScannedRobot(JuniorRobot robot) {
        // Aim directly at the scanned enemy's angle
        robot.bearGunTo(robot.scannedAngle);

        // Dynamic firepower based on distance to conserve energy
        if (robot.scannedDistance < 150) {
            robot.fire(3.0); // Maximum power for close range
        } else if (robot.scannedDistance < 400) {
            robot.fire(2.0); // Medium power
        } else {
            robot.fire(1.0); // Minimum power for long distance shots
        }

        // Perpendicular movement (Strafing)
        robot.turnTo(robot.scannedAngle + 90);

        // Move forward or backward based on the current state
        if (movingForward) {
            robot.ahead(100);
        } else {
            robot.back(100);
        }
    }

    @Override
    public void onHitByBullet(JuniorRobot robot) {
        // Reverse direction to break the enemy's targeting prediction
        movingForward = !movingForward;
    }

    @Override
    public void onHitWall(JuniorRobot robot) {
        // Reverse direction and maneuver away from the wall
        movingForward = !movingForward;
        if (movingForward) {
            robot.turnAheadRight(100, 90);
        } else {
            robot.turnBackRight(100, 90);
        }
    }

    @Override
    public void onHitRobot(JuniorRobot robot) {
        doNoting();
    }

    private void doNoting() {
    }
}