package tank;

import robocode.JuniorRobot;

import static robocode.JuniorRobot.*;

public final class BasicStrategy implements Strategy {
    private boolean movingForward = true;

    @Override
    public void run(JuniorRobot robot) {
        // Escanea constantemente
        robot.turnGunRight(360);

        // Movimiento en arcos para simular bamboleo errático sin frenar
        if (movingForward) {
            robot.turnAheadRight(150, 45); // Avanza trazando una curva a la derecha
        } else {
            robot.turnBackLeft(150, 45);   // Retrocede en curva a la izquierda
        }
    }

    @Override
    public void onScannedRobot(JuniorRobot robot) {
        // 1. Apuntado real
        robot.bearGunTo(robot.scannedAngle);

        // 2. Lógica de ataque agresivo y transición a embestida
        if (robot.scannedDistance < 50) {
            robot.fire(3.0); // Fuego máximo

            // Cargar contra el enemigo para sumar puntos de impacto
            robot.turnTo(robot.scannedAngle);
            robot.ahead(robot.scannedDistance);
        } else {
            // Fuego de contención y continuar la evasión
            robot.fire(1.0);
        }
    }

    @Override
    public void onHitByBullet(JuniorRobot robot) {
        robot.turnBackLeft(1000, 45);
    }

    @Override
    public void onHitWall(JuniorRobot robot) {
        // 1. Si chocó yendo hacia adelante, retrocede y gira; si chocó yendo hacia atrás, avanza y gira
        if (movingForward) {
            robot.turnBackRight(100, 90);
        } else {
            robot.turnAheadLeft(100, 90);
        }

        // 2. Invierte el sentido para el próximo ciclo
        movingForward = !movingForward;
    }

    @Override
    public void onHitRobot(JuniorRobot robot) {
        doNoting();
    }

    private void doNoting() {
    }
}
