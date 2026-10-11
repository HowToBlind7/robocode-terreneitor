package tankkotlin

import robocode.JuniorRobot

/**
 * Interfaz que define el contrato para los estrategas que deciden qué estrategia usar.
 * En Kotlin los Singletons internos se modelan con companion object + object declarations,
 * reemplazando el patrón getInstance() de Java.
 */
interface Strategist {

    fun decideStrategy(robot: JuniorRobot): Strategy

    /**
     * Estratega principal: usa WallStrategy por defecto, y cambia a TurretStrategy
     * cuando la energía es muy baja (<= 5).
     * Se implementa como object (singleton de Kotlin) eliminando el boilerplate del
     * patrón Singleton de Java (constructor privado + campo estático + getInstance()).
     */
    object MainStrategist : Strategist {

        private val wallStrategy: Strategy = WallStrategy()
        private val turretStrategy: Strategy = TurretStrategy()

        override fun decideStrategy(robot: JuniorRobot): Strategy {
            return if (robot.energy <= 5) {
                turretStrategy
            } else {
                wallStrategy
            }
        }
    }

    /**
     * Estratega de final de partida: usa SimpleRandomStrategy cuando quedan pocos
     * enemigos o la energía es baja, y WallStrategy en caso contrario.
     * También modelado como object singleton de Kotlin.
     */
    object EndGameStrategist : Strategist {

        private val wallStrategy: Strategy = WallStrategy()
        private val simpleRandomStrategy: Strategy = SimpleRandomStrategy()

        override fun decideStrategy(robot: JuniorRobot): Strategy {
            return if (robot.others <= 1 || robot.energy < 25) {
                simpleRandomStrategy
            } else {
                wallStrategy
            }
        }
    }
}
