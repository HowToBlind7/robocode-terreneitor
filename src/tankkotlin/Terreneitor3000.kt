package tankkotlin

import robocode.JuniorRobot
import java.awt.Color

/**
 * Robot principal Terreneitor3000 migrado a Kotlin.
 * Extiende JuniorRobot de Robocode y delega toda la lógica de comportamiento
 * al Strategist, que decide qué Strategy aplicar en cada momento.
 *
 * Se accede al singleton MainStrategist directamente (sin getInstance()),
 * y se usa la propiedad .rgb de Color en lugar de .getRGB().
 */
class Terreneitor3000 : JuniorRobot() {

    private val strategist: Strategist = Strategist.MainStrategist

    override fun run() {
        setColors(
            Color.BLACK.rgb,
            Color.RED.rgb,
            Color.RED.rgb,
            Color.RED.rgb,
            Color.RED.rgb
        )
        while (true) {
            strategist.decideStrategy(this).run(this)
        }
    }

    override fun onScannedRobot() {
        strategist.decideStrategy(this).onScannedRobot(this)
    }

    override fun onHitByBullet() {
        strategist.decideStrategy(this).onHitByBullet(this)
    }

    override fun onHitWall() {
        strategist.decideStrategy(this).onHitWall(this)
    }

    override fun onHitRobot() {
        strategist.decideStrategy(this).onHitRobot(this)
    }
}
