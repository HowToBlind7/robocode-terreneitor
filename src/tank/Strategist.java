package tank;

import robocode.JuniorRobot;

public interface Strategist {

    Strategy decideStrategy(JuniorRobot robot);

    class MainStrategist implements Strategist {

        private static final MainStrategist INSTANCE = new MainStrategist();

        private final Strategy wallStrategy = new WallStrategy();
        private final Strategy turretStrategy = new TurretStrategy();

        private MainStrategist() {
        }

        public static MainStrategist getInstance() {
            return INSTANCE;
        }

        @Override
        public Strategy decideStrategy(JuniorRobot robot) {
            if (robot.energy <= 5) {
                return turretStrategy;
            }

            return wallStrategy;
        }
    }


    class EndGameStrategist implements Strategist {

        private static final EndGameStrategist INSTANCE = new EndGameStrategist();
        private final Strategy wallStrategy = new WallStrategy();
        private final Strategy simpleRandomStrategy = new SimpleRandomStrategy();

        private EndGameStrategist() {}

        public static EndGameStrategist getInstance() {
            return INSTANCE;
        }

        @Override
        public Strategy decideStrategy(JuniorRobot robot) {
            if (robot.others <= 1 || robot.energy < 25) {
                return simpleRandomStrategy;
            } else {
                return wallStrategy;
            }
        }
    }
}