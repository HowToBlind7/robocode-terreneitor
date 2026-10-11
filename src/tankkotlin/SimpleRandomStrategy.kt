package tankkotlin

import robocode.JuniorRobot
import java.util.Random

/**
 * Estrategia de movimiento aleatorio simple.
 * Se usa un valor por defecto para el parámetro rng en el constructor primario,
 * eliminando la necesidad de un constructor secundario.
 */
class SimpleRandomStrategy(private val rng: Random = Random()) : Strategy {

    override fun run(robot: JuniorRobot) {
        // Escaneo constante girando el cañón
        robot.turnGunRight(20)

        // Elegir una dirección aleatoria y avanzar
        val randomAngle = rng.nextInt(360)
        robot.turnTo(randomAngle)
        robot.ahead(100)
    }

    override fun onScannedRobot(robot: JuniorRobot) {
        // Apuntar al enemigo y disparar
        robot.turnGunTo(robot.scannedAngle)
        robot.fire(1.0)
    }

    override fun onHitByBullet(robot: JuniorRobot) {
        // Escapar girando aleatoriamente y retrocediendo
        val escapeAngle = rng.nextInt(360)
        robot.turnTo(escapeAngle)
        robot.back(200)
    }

    override fun onHitWall(robot: JuniorRobot) {
        // Girar aleatoriamente para alejarse de la pared
        val newHeading = (robot.heading + 180) % 360
        robot.turnTo(newHeading)
        robot.ahead(50)
    }

    override fun onHitRobot(robot: JuniorRobot) {
        // Disparar a quemarropa al robot con el que se chocó
        robot.turnGunTo(robot.hitRobotAngle)
        robot.fire(2.0)
    }
}
