package tank;

import robocode.*;
import java.awt.*;

public class Terreneitor3000 extends JuniorRobot {

	private final Strategist strategist = Strategist.MainStrategist.getInstance();

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
			strategist.decideStrategy(this).run(this);
		}
	}

	@Override
	public void onScannedRobot() {
		strategist.decideStrategy(this).onScannedRobot(this);
	}

	@Override
	public void onHitByBullet() {
		strategist.decideStrategy(this).onHitByBullet(this);
	}

	@Override
	public void onHitWall() {
		strategist.decideStrategy(this).onHitWall(this);
	}

	@Override
	public void onHitRobot() {
		strategist.decideStrategy(this).onHitRobot(this);
	}
}