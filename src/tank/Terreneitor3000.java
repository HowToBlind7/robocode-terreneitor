package tank;
import robocode.*;

import java.awt.*;


public class Terreneitor3000 extends JuniorRobot
{

	private final Strategist strategist = Strategist.MainStrategist.getInstance();
	private Strategy strategy;

	@Override
	public void run() {
		this.setColors(
				Color.BLACK.getRGB(),
				Color.RED.getRGB(),
				Color.RED.getRGB(),
				Color.RED.getRGB(),
				Color.RED.getRGB()
		);
		while (true) {
			strategy = strategist.decideStrategy(this);
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