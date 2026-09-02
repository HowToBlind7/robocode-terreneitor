package tank;

import robocode.JuniorRobot;

import java.util.Random;

public record SimpleRandomStrategy(Random rng) implements Strategy {

    public SimpleRandomStrategy() {
        this(new Random());
    }

    @Override
    public void run(JuniorRobot robot) {
        // Escaneo constante girando el cañón
        robot.turnGunRight(20);

        // Elegir una dirección aleatoria y avanzar
        int randomAngle = rng.nextInt(360);
        robot.turnTo(randomAngle);
        robot.ahead(100);
    }

    @Override
    public void onScannedRobot(JuniorRobot robot) {
        // Apuntar al enemigo y disparar
        robot.turnGunTo(robot.scannedAngle);
        robot.fire(1.0);
    }

    @Override
    public void onHitByBullet(JuniorRobot robot) {
        // Escapar girando aleatoriamente y retrocediendo
        int escapeAngle = rng.nextInt(360);
        robot.turnTo(escapeAngle);
        robot.back(200);
    }

    @Override
    public void onHitWall(JuniorRobot robot) {
        // Girar aleatoriamente para alejarse de la pared
        int newHeading = (robot.heading + 180) % 360;
        robot.turnTo(newHeading);
        robot.ahead(50);
    }

    @Override
    public void onHitRobot(JuniorRobot robot) {
        // Disparar a quemarropa al robot con el que se chocó
        robot.turnGunTo(robot.hitRobotAngle);
        robot.fire(2.0);
    }
}
