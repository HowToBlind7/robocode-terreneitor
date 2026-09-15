package tank;
import robocode.*;

public sealed interface Strategy permits SimpleRandomStrategy, TurretStrategy, WallStrategy {

    void run(JuniorRobot robot);
    void onScannedRobot(JuniorRobot robot);
    void onHitByBullet(JuniorRobot robot);
    void onHitWall(JuniorRobot robot);
    void onHitRobot(JuniorRobot robot);
}
