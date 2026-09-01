package tank;

import robocode.JuniorRobot;

public final class WallStrategy implements Strategy {

    private boolean movingForward = true;
    private boolean effectiveShot = false;
    private boolean wasHit = false;
    private int lastEnergy = 100;
    private boolean initialized = false;

    private int scanState = 0;

    // Margen de seguridad en píxeles para no tocar la pared
    private final int WALL_MARGIN = 30;

    @Override
    public void run(JuniorRobot robot) {
        if (!initialized) {
            goToClosestWall(robot);
            lastEnergy = robot.energy;
            initialized = true;
        }

        // Detección de "disparo efectivo" analizando la energía
        int currentEnergy = robot.energy;
        if (currentEnergy > lastEnergy) {
            effectiveShot = true;
        }
        lastEnergy = currentEnergy;

        // Saber hacia dónde nos estamos moviendo (0, 90, 180 o 270 grados)
        int currentHeading = movingForward ? robot.heading : (robot.heading + 180) % 360;

        // ESCANEO ROTATIVO: Alterna la visión para no tener puntos ciegos en el muro
        this.rotaryScan(robot, currentHeading);

        int distToCorner = getDistanceToWall(robot, currentHeading);

        // Moverse solo lo necesario sin pasarse del margen
        if (distToCorner > 0) {
            int moveAmount = Math.min(50, distToCorner);
            if (movingForward) {
                robot.ahead(moveAmount);
            } else {
                robot.back(moveAmount);
            }
        }

        // Si la distancia a la esquina llegó a 0 (estamos en el margen), ejecutamos la lógica de giro
        if (getDistanceToWall(robot, currentHeading) <= 0) {
            handleCornerLogic(robot);
        }
    }

    private void rotaryScan(JuniorRobot robot, int currentHeading) {
        switch (scanState) {
            case 0 -> robot.turnGunTo(currentHeading);                     // Frente de marcha
            case 1 -> robot.turnGunTo(robot.heading - 90);                 // Centro del mapa
            case 2 -> robot.turnGunTo((currentHeading + 180) % 360);       // Retaguardia
        }
        scanState = (scanState + 1) % 3;
    }

    private void goToClosestWall(JuniorRobot robot) {
        int distN = robot.fieldHeight - robot.robotY;
        int distS = robot.robotY;
        int distE = robot.fieldWidth - robot.robotX;
        int distW = robot.robotX;

        int minDist = Math.min(Math.min(distN, distS), Math.min(distE, distW));

        if (minDist == distN) {
            moveToWall(robot, 0, distN);
        } else if (minDist == distS) {
            moveToWall(robot, 180, distS);
        } else if (minDist == distE) {
            moveToWall(robot, 90, distE);
        } else {
            moveToWall(robot, 270, distW);
        }
    }

    private void moveToWall(JuniorRobot robot, int heading, int distanceToWall) {
        robot.turnTo(heading);
        robot.ahead(distanceToWall - WALL_MARGIN);
        robot.turnTo((heading + 270) % 360);
    }

    private int getDistanceToWall(JuniorRobot robot, int heading) {
        heading = (heading % 360 + 360) % 360;

        if (heading >= 315 || heading < 45) { // Mirando al Norte
            return robot.fieldHeight - robot.robotY - WALL_MARGIN;
        } else if (heading >= 45 && heading < 135) { // Mirando al Este
            return robot.fieldWidth - robot.robotX - WALL_MARGIN;
        } else if (heading >= 135 && heading < 225) { // Mirando al Sur
            return robot.robotY - WALL_MARGIN;
        } else { // Mirando al Oeste
            return robot.robotX - WALL_MARGIN;
        }
    }

    private void handleCornerLogic(JuniorRobot robot) {
        if (effectiveShot && !wasHit) {
            movingForward = !movingForward;
        } else {
            if (movingForward) {
                robot.turnLeft(90);
            } else {
                robot.turnRight(90);
                movingForward = true;
            }
        }
        effectiveShot = false;
        wasHit = false;
        lastEnergy = robot.energy;
    }

    @Override
    public void onScannedRobot(JuniorRobot robot) {
        robot.turnGunTo(robot.scannedAngle);

        int relativeBearing = (robot.scannedAngle - robot.heading + 360) % 360;

        boolean enemyInFront = relativeBearing < 20 || relativeBearing > 340;
        boolean enemyBehind = relativeBearing > 160 && relativeBearing < 200;

        // La lógica de exterminio se activará enseguida gracias al nuevo escaneo rotativo
        if ((movingForward && enemyInFront) || (!movingForward && enemyBehind)) {
            robot.fire(3);
            if (movingForward) {
                robot.ahead(robot.scannedDistance);
            } else {
                robot.back(robot.scannedDistance);
            }
        } else {
            robot.fire(2);
        }
    }

    @Override
    public void onHitByBullet(JuniorRobot robot) {
        wasHit = true;
        lastEnergy = robot.energy;
    }

    @Override
    public void onHitRobot(JuniorRobot robot) {
        robot.turnGunTo(robot.hitRobotAngle);
        robot.fire(3);
        effectiveShot = true;
        lastEnergy = robot.energy;
    }

    @Override
    public void onHitWall(JuniorRobot robot) {
        if (movingForward) {
            robot.back(20);
        } else {
            robot.ahead(20);
        }
    }
}