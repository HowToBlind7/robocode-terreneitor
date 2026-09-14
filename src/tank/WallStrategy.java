package tank;

import robocode.JuniorRobot;

/**
 * Estrategia de patrullaje de muros (Wall Strategy).
 * El objetivo de esta estrategia es ubicar rápidamente el tanque en el muro más cercano
 * para luego recorrer los bordes del mapa de forma continua.
 * Se mantiene un margen de seguridad de 30 píxeles respecto a las paredes para evitar
 * perder energía por colisiones. Además, se implementa un escaneo rotativo continuo
 * que vigila el frente, el centro de la arena y la retaguardia para no tener puntos ciegos.
 * Si se logra un disparo efectivo durante un tramo sin haber recibido daño,
 * se invierte la marcha para acorralar al enemigo. Si se recibe daño o no se concreta un impacto,
 * el robot dobla la esquina manteniendo su tracción actual (avanzando en sentido antihorario o retrocediendo en sentido horario).
 */
public final class WallStrategy implements Strategy {

    private boolean movingForward = true;
    private boolean effectiveShot = false;
    private boolean wasHit = false;
    private int lastEnergy = 100;
    private boolean initialized = false;

    private int scanState = 0;

    // Margen de seguridad en píxeles que se deja para evitar chocar contra la pared
    private final int WALL_MARGIN = 30;

    public WallStrategy() {}

    @Override
    public void run(JuniorRobot robot) {
        // Se envia el robot al muro mas cercano
        if (!initialized) {
            goToClosestWall(robot);
            lastEnergy = robot.energy;
            initialized = true;
        }

        // Se detecta si se logró un "disparo efectivo" analizando si la energía aumentó respecto al turno anterior
        int currentEnergy = robot.energy;
        if (currentEnergy > lastEnergy) {
            effectiveShot = true;
        }
        lastEnergy = currentEnergy;

        // Se determina hacia qué dirección física se está moviendo realmente (0, 90, 180 o 270 grados),
        // independientemente de hacia dónde apunte el chasis.
        int currentHeading = movingForward ? robot.heading : (robot.heading + 180) % 360;

        // ESCANEO ROTATIVO: Se alterna la visión del radar para asegurarse de no tener puntos ciegos durante el patrullaje
        this.rotaryScan(robot, currentHeading);

        // Se calcula a cuántos píxeles se está de la próxima esquina en la dirección actual
        int distToCorner = getDistanceToWall(robot, currentHeading);

        // Se mueve solo la distancia permitida para no chocar el muro
        if (distToCorner > 0) {
            int moveAmount = Math.min(50, distToCorner);
            if (movingForward) {
                robot.ahead(moveAmount);
            } else {
                robot.back(moveAmount);
            }
        }

        // Si llega a una esquina, se decide si doblar o repetir trayecto
        if (getDistanceToWall(robot, currentHeading) <= 0) {
            handleCornerLogic(robot);
        }
    }

    private void rotaryScan(JuniorRobot robot, int currentHeading) {
        // Escaneo por turnos según el estado (scanState)
        switch (scanState) {
            case 0 -> robot.turnGunTo(currentHeading);                     // Se vigila el frente hacia donde se dirige
            case 1 -> robot.turnGunTo(robot.heading - 90);                 // Se vigila el centro del mapa
            case 2 -> robot.turnGunTo((currentHeading + 180) % 360);       // Se vigila la retaguardia
        }
        // Se avanza al siguiente estado de escaneo
        scanState = (scanState + 1) % 3;
    }

    private void goToClosestWall(JuniorRobot robot) {
        int distN = robot.fieldHeight - robot.robotY;
        int distS = robot.robotY;
        int distE = robot.fieldWidth - robot.robotX;
        int distW = robot.robotX;

        // Se establece la minima distancia a los bordes
        int minDist = Math.min(Math.min(distN, distS), Math.min(distE, distW));

        // Se dirige al muro más cercano
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
        // Se apunta hacia la pared elegida y se gira para empezar el patrullaje
        robot.turnTo(heading);
        robot.ahead(distanceToWall - WALL_MARGIN);
        robot.turnTo((heading + 270) % 360);
    }

    private int getDistanceToWall(JuniorRobot robot, int heading) {
        if (heading >= 315 || heading < 45) { // Si el desplazamiento es hacia el Norte
            return robot.fieldHeight - robot.robotY - WALL_MARGIN;
        } else if (heading >= 45 && heading < 135) { // Si el desplazamiento es hacia el Este
            return robot.fieldWidth - robot.robotX - WALL_MARGIN;
        } else if (heading >= 135 && heading < 225) { // Si el desplazamiento es hacia el Sur
            return robot.robotY - WALL_MARGIN;
        } else { // Si el desplazamiento es hacia el Oeste
            return robot.robotX - WALL_MARGIN;
        }
    }

    private void handleCornerLogic(JuniorRobot robot) {
        // Si se concretó al menos un disparo y no se recibió daño, se invierte la marcha para acorralar al enemigo
        if (effectiveShot && !wasHit) {
            movingForward = !movingForward;
        } else {
            // Se dobla en la esquina para evitar más daño o buscar más objetivos
            robot.turnLeft(90);
        }

        // Se resetean las banderas de estado para el próximo tramo del muro
        effectiveShot = false;
        wasHit = false;
        lastEnergy = robot.energy;
    }

    @Override
    public void onScannedRobot(JuniorRobot robot) {
        // Se apunta el cañón hacia el robot escaneado
        robot.turnGunTo(robot.scannedAngle);

        // Se calcula la posición relativa del enemigo respecto al chasis
        int relativeBearing = (robot.scannedAngle - robot.heading + 360) % 360;

        // Se verifica si se tiene al enemigo alineado justo delante o justo detrás de la trayectoria
        boolean enemyInFront = relativeBearing < 20 || relativeBearing > 340;
        boolean enemyBehind = relativeBearing > 160 && relativeBearing < 200;

        // Esta lógica de exterminio se activa enseguida gracias al escaneo rotativo.
        // Si el enemigo está en la línea de avance o retroceso, se dispara con potencia máxima y se embiste.
        if ((movingForward && enemyInFront) || (!movingForward && enemyBehind)) {
            robot.fire(3);
            if (movingForward) {
                robot.ahead(robot.scannedDistance);
            } else {
                robot.back(robot.scannedDistance);
            }
        } else {
            // Si está a los costados, se prefiere un disparo de potencia media para conservar energía
            robot.fire(2);
        }
    }

    @Override
    public void onHitByBullet(JuniorRobot robot) {
        wasHit = true;
    }

    @Override
    public void onHitRobot(JuniorRobot robot) {
        // Si se choca contra un enemigo, se le apunta a quemarropa y se descarga potencia máxima
        robot.turnGunTo(robot.hitRobotAngle);
        robot.fire(3);
        effectiveShot = true;
        lastEnergy = robot.energy;
    }

    @Override
    public void onHitWall(JuniorRobot robot) {
        // No debería ocurrir este evento.
        // Por seguridad, si el cálculo de márgenes falla y se toca la pared, se rebota un poco para despegar
        if (movingForward) {
            robot.back(20);
        } else {
            robot.ahead(20);
        }
    }
}