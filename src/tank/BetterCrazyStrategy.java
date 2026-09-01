package tank;

import robocode.JuniorRobot;

public final class BetterCrazyStrategy implements Strategy {

    private boolean movingForward = true;

    @Override
    public void run(JuniorRobot robot) {
        // Escaneo constante independiente del chasis
        robot.turnGunRight(360);

        // Movimiento en arcos para simular bamboleo errático sin frenar
        if (movingForward) {
            robot.turnAheadRight(500, 45); // Avanza trazando una curva a la derecha
        } else {
            robot.turnBackLeft(500, 45);   // Retrocede en curva a la izquierda
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
        // Romper la predicción del rival invirtiendo el arco
        movingForward = !movingForward;
    }

    @Override
    public void onHitWall(JuniorRobot robot) {
        // Rebote impredecible para escapar de los límites
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