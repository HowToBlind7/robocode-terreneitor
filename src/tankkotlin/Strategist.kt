package tankkotlin

import robocode.JuniorRobot

/**
 * Interfaz que define el contrato para los estrategas que deciden qué estrategia usar.
 * Los singletons se modelan con object (eliminando el boilerplate del patrón
 * Singleton de Java: constructor privado + campo estático + getInstance()).
 */
interface Strategist {

    fun decideStrategy(robot: JuniorRobot): Strategy

    /**
     * Estratega principal: usa WallStrategy por defecto, y cambia a TurretStrategy
     * cuando la energía es muy baja (<= 5).
     */
    object MainStrategist : Strategist {

        private val wallStrategy = WallStrategy()

        override fun decideStrategy(robot: JuniorRobot) =
            if (robot.energy <= 5) TurretStrategy else wallStrategy
    }

    /**
     * Estratega de final de partida: usa SimpleRandomStrategy cuando quedan pocos
     * enemigos o la energía es baja, y WallStrategy en caso contrario.
     */
    object EndGameStrategist : Strategist {

        private val wallStrategy = WallStrategy()
        private val simpleRandomStrategy = SimpleRandomStrategy()

        override fun decideStrategy(robot: JuniorRobot) =
            if (robot.others <= 1 || robot.energy < 25) simpleRandomStrategy else wallStrategy
    }
}
