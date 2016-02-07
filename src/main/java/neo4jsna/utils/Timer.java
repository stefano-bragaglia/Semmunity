package neo4jsna.utils;

import java.time.Clock;

public class Timer {
	protected static Timer instance;
	protected final Clock clock;
	public long ts, te;

	protected Timer() {
		clock = Clock.systemDefaultZone();
	}

	public static synchronized Timer timer() {
		if (instance == null)
			instance = new Timer();
		return instance;
	}

	public static Timer newTimer() {
		return new Timer();
	}

	public void start() {
		ts = clock.millis();
		te = 0;
	}

	public void stop() {
		te = clock.millis();
	}

	public String totalTime() {
		String res = "Total time: " + (te - ts) / 1000.0 + " s";
		te = 0;
		ts = 0;
		return res;
	}
}
