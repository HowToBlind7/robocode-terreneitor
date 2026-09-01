package tank;
import robocode.*;


public class Terreneitor3000 extends JuniorRobot
{

	private final Strategy strategy = new WallStrategy();

	@Override
	public void run() {
		while (true) {
			strategy.run(this);
		}
	}

	@Override
	public void onScannedRobot() {
		strategy.onScannedRobot(this);
	}

	@Override
	public void onHitByBullet() {
		strategy.onHitByBullet(this);
	}

	@Override
	public void onHitWall() {
		strategy.onHitWall(this);
	}

	@Override
	public void onHitRobot() {
		strategy.onHitRobot(this);
	}
}