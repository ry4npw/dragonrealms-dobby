package pw.ry4n.dr.proxy;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import pw.ry4n.dr.engine.core.Program;
import pw.ry4n.dr.engine.core.State;

public class TimedThread implements Program {
	private InterceptingProxy proxy = null;
	private CommandQueue sendQueue = null;
	private String command = null;
	private long duration = -1L;
	private TimeUnit timeUnit = TimeUnit.SECONDS;
	private State state = State.RUNNING;
	private Thread thread = null;
	private Timer t = null;

	public TimedThread(InterceptingProxy proxy, CommandQueue sendQueue, long duration, TimeUnit timeUnit,
			String command) {
		this.proxy = proxy;
		this.sendQueue = sendQueue;
		this.duration = duration;
		this.timeUnit = timeUnit;
		this.command = command;
	}

	@Override
	public void run() {
		t = new Timer();

		long periodInMilliseconds = TimeUnit.MILLISECONDS.convert(duration, timeUnit);
		// attempt to send the next command at a scheduled interval
		t.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					if (State.PAUSED.equals(state) || State.STOPPED.equals(state)) {
						return;
					}

					if (command.startsWith(";")) {
						proxy.runScript(command.substring(1));
					} else {
						proxy.sendUserMessage(getName());
						sendQueue.enqueue(command);
					}
				} catch (Exception e) {
					System.err.println("Timer '" + command + "' halted due to exception.");
					t.cancel();
				}
			}
		}, 0, periodInMilliseconds);
	}

	@Override
	public String getName() {
		return "Every " + duration + " " + timeUnit.name().toLowerCase() + ": " + command;
	}

	@Override
	public void pause() {
		if (State.STOPPED.equals(state)) {
			return;
		}

		state = State.PAUSED;
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public void resume() {
		if (State.STOPPED.equals(state)) {
			return;
		}

		state = State.RUNNING;
	}

	@Override
	public void stop() {
		state = State.STOPPED;

		if (t != null) {
			t.cancel();
		}

		try {
			proxy.sendUserMessage(getName() + " " + state.name());
		} catch (IOException e) {
			// ignore send error
		}
	}

	@Override
	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}
}
