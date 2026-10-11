package tankkotlin

import robocode.JuniorRobot

/**
 * Interfaz sellada que define el contrato para las estrategias del robot.
 * En Kotlin se modela como sealed interface, equivalente al sealed interface de Java 17+.
 */
sealed interface Strategy {
    fun run(robot: JuniorRobot)
    fun onScannedRobot(robot: JuniorRobot)
    fun onHitByBullet(robot: JuniorRobot)
    fun onHitWall(robot: JuniorRobot)
    fun onHitRobot(robot: JuniorRobot)
}
