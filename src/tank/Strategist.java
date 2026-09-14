package tank;

import robocode.JuniorRobot;

public interface Strategist {

    Strategy decideStrategy(JuniorRobot robot);

    class MainStrategist implements Strategist {

        private static final MainStrategist INSTANCE = new MainStrategist();
        private final Strategy wallStrategy = new WallStrategy();
        private final Strategy simpleRandomStrategy = new SimpleRandomStrategy();

        private MainStrategist() {}

        public static MainStrategist getInstance() {
            return INSTANCE;
        }

        @Override
        public Strategy decideStrategy(JuniorRobot robot) {
            if (robot.energy < 30) {
                return simpleRandomStrategy;
            } else {
                return wallStrategy;
            }
        }
    }
}