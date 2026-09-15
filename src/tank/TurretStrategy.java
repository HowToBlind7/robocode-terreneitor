package tank;

import robocode.JuniorRobot;

public record TurretStrategy() implements Strategy {

    @Override
    public void run(JuniorRobot robot) {
        robot.turnGunRight(360);
    }

    @Override
    public void onScannedRobot(JuniorRobot robot) {
        robot.turnGunTo(robot.scannedAngle);
        robot.fire(3);
    }

    @Override
    public void onHitByBullet(JuniorRobot robot) {
    }

    @Override
    public void onHitWall(JuniorRobot robot) {
        // No debería ocurrir porque no se mueve, pero se implementa vacío por contrato.
    }

    @Override
    public void onHitRobot(JuniorRobot robot) {
        robot.turnGunTo(robot.hitRobotAngle);
        robot.fire(3);
    }
}