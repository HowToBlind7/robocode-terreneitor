package tankkotlin

import robocode.JuniorRobot

/**
 * Estrategia de torreta estática.
 * El robot no se mueve, solo gira el cañón 360° buscando enemigos y dispara
 * con potencia máxima.
 * Al no tener estado, se modela como object (singleton de Kotlin).
 */
object TurretStrategy : Strategy {

    override fun run(robot: JuniorRobot) {
        robot.turnGunRight(360)
    }

    override fun onScannedRobot(robot: JuniorRobot) {
        robot.turnGunTo(robot.scannedAngle)
        robot.fire(3.0)
    }

    override fun onHitByBullet(robot: JuniorRobot) {
        // Sin reacción: la torreta permanece estática
    }

    override fun onHitWall(robot: JuniorRobot) {
        // No debería ocurrir porque no se mueve
    }

    override fun onHitRobot(robot: JuniorRobot) {
        robot.turnGunTo(robot.hitRobotAngle)
        robot.fire(3.0)
    }
}
