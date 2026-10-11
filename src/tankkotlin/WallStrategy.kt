package tankkotlin

import robocode.JuniorRobot
import java.lang.Math.min

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
class WallStrategy : Strategy {

    private var movingForward = true
    private var effectiveShot = false
    private var wasHit = false
    private var lastEnergy = 100
    private var initialized = false

    private var scanState = 0

    // Margen de seguridad en píxeles que se deja para evitar chocar contra la pared
    private val WALL_MARGIN = 30

    override fun run(robot: JuniorRobot) {
        // Se envia el robot al muro mas cercano
        if (!initialized) {
            goToClosestWall(robot)
            lastEnergy = robot.energy
            initialized = true
        }

        // Se detecta si se logró un "disparo efectivo" analizando si la energía aumentó respecto al turno anterior
        val currentEnergy = robot.energy
        if (currentEnergy > lastEnergy) {
            effectiveShot = true
        }
        lastEnergy = currentEnergy

        // Se determina hacia qué dirección física se está moviendo realmente (0, 90, 180 o 270 grados),
        // independientemente de hacia dónde apunte el chasis.
        val currentHeading = if (movingForward) robot.heading else (robot.heading + 180) % 360

        // ESCANEO ROTATIVO: Se alterna la visión del radar para asegurarse de no tener puntos ciegos durante el patrullaje
        rotaryScan(robot, currentHeading)

        // Se calcula a cuántos píxeles se está de la próxima esquina en la dirección actual
        val distToCorner = getDistanceToWall(robot, currentHeading)

        // Se mueve solo la distancia permitida para no chocar el muro
        if (distToCorner > 0) {
            val moveAmount = min(50, distToCorner)
            if (movingForward) {
                robot.ahead(moveAmount)
            } else {
                robot.back(moveAmount)
            }
        }

        // Si llega a una esquina, se decide si doblar o repetir trayecto
        if (getDistanceToWall(robot, currentHeading) <= 0) {
            handleCornerLogic(robot)
        }
    }

    private fun rotaryScan(robot: JuniorRobot, currentHeading: Int) {
        // Escaneo por turnos según el estado (scanState)
        when (scanState) {
            0 -> robot.turnGunTo(currentHeading)                     // Se vigila el frente hacia donde se dirige
            1 -> robot.turnGunTo(robot.heading - 90)                 // Se vigila el centro del mapa
            2 -> robot.turnGunTo((currentHeading + 180) % 360)       // Se vigila la retaguardia
        }
        // Se avanza al siguiente estado de escaneo
        scanState = (scanState + 1) % 3
    }

    private fun goToClosestWall(robot: JuniorRobot) {
        val distN = robot.fieldHeight - robot.robotY
        val distS = robot.robotY
        val distE = robot.fieldWidth - robot.robotX
        val distW = robot.robotX

        // Se establece la minima distancia a los bordes
        val minDist = min(min(distN, distS), min(distE, distW))

        // Se dirige al muro más cercano
        when (minDist) {
            distN -> moveToWall(robot, 0, distN)
            distS -> moveToWall(robot, 180, distS)
            distE -> moveToWall(robot, 90, distE)
            else  -> moveToWall(robot, 270, distW)
        }
    }

    private fun moveToWall(robot: JuniorRobot, heading: Int, distanceToWall: Int) {
        // Se apunta hacia la pared elegida y se gira para empezar el patrullaje
        robot.turnTo(heading)
        robot.ahead(distanceToWall - WALL_MARGIN)
        robot.turnTo((heading + 270) % 360)
    }

    private fun getDistanceToWall(robot: JuniorRobot, heading: Int): Int {
        return when {
            heading >= 315 || heading < 45  -> robot.fieldHeight - robot.robotY - WALL_MARGIN  // Norte
            heading >= 45  && heading < 135 -> robot.fieldWidth - robot.robotX - WALL_MARGIN   // Este
            heading >= 135 && heading < 225 -> robot.robotY - WALL_MARGIN                      // Sur
            else                            -> robot.robotX - WALL_MARGIN                      // Oeste
        }
    }

    private fun handleCornerLogic(robot: JuniorRobot) {
        // Si se concretó al menos un disparo y no se recibió daño, se invierte la marcha para acorralar al enemigo
        if (effectiveShot && !wasHit) {
            movingForward = !movingForward
        } else {
            // Se dobla en la esquina para evitar más daño o buscar más objetivos
            robot.turnLeft(90)
        }

        // Se resetean las banderas de estado para el próximo tramo del muro
        effectiveShot = false
        wasHit = false
        lastEnergy = robot.energy
    }

    override fun onScannedRobot(robot: JuniorRobot) {
        // Se apunta el cañón hacia el robot escaneado
        robot.turnGunTo(robot.scannedAngle)

        // Se calcula la posición relativa del enemigo respecto al chasis
        val relativeBearing = (robot.scannedAngle - robot.heading + 360) % 360

        // Se verifica si se tiene al enemigo alineado justo delante o justo detrás de la trayectoria
        val enemyInFront = relativeBearing < 20 || relativeBearing > 340
        val enemyBehind = relativeBearing > 160 && relativeBearing < 200

        // Esta lógica de exterminio se activa enseguida gracias al escaneo rotativo.
        // Si el enemigo está en la línea de avance o retroceso, se dispara con potencia máxima y se embiste.
        if ((movingForward && enemyInFront) || (!movingForward && enemyBehind)) {
            robot.fire(3.0)
            if (movingForward) {
                robot.ahead(robot.scannedDistance)
            } else {
                robot.back(robot.scannedDistance)
            }
        } else {
            // Si está a los costados, se prefiere un disparo de potencia media para conservar energía
            robot.fire(2.0)
        }
    }

    override fun onHitByBullet(robot: JuniorRobot) {
        wasHit = true
    }

    override fun onHitRobot(robot: JuniorRobot) {
        // Si se choca contra un enemigo, se le apunta a quemarropa y se descarga potencia máxima
        robot.turnGunTo(robot.hitRobotAngle)
        robot.fire(3.0)
        effectiveShot = true
        lastEnergy = robot.energy
    }

    override fun onHitWall(robot: JuniorRobot) {
        // No debería ocurrir este evento.
        // Por seguridad, si el cálculo de márgenes falla y se toca la pared, se rebota un poco para despegar
        if (movingForward) {
            robot.back(20)
        } else {
            robot.ahead(20)
        }
    }
}
