package pw.ry4n.dr.proxy;

import java.io.IOException;
import java.util.Deque;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedDeque;

public class CommandQueue implements Runnable, StreamListener {
	private Deque<String> sendQueue = new ConcurrentLinkedDeque<String>();
	private String lastCommand = null;
	private int commandsWaitingForResponse = 0;
	private long roundTimeOver = -1;

	private AbstractProxy sendProxy;
	private StreamMonitor outputStreamMonitor;

	CommandQueue() {
		// package private constructor for unit tests
	}

	public CommandQueue(AbstractProxy sendProxy, StreamMonitor outputStreamMonitor) {
		this.sendProxy = sendProxy;
		this.outputStreamMonitor = outputStreamMonitor;
	}

	/**
	 * This method will send a line upstream to the server. This is designed to
	 * be the injection point and coordinator for all running scripts. Commands
	 * will be sent in a FIFO queue, and it will retry failed commands based on
	 * RT. Eventually some commands may be prioritized and skip the queue that
	 * do not cause RT.
	 * 
	 * @param line
	 * @throws IOException
	 */
	public void enqueue(String line) throws IOException {
		synchronized (sendQueue) {
			sendQueue.offerLast(line);
		}
	}

	@Override
	public void notify(String line) {
		if (line.startsWith("Roundtime") || line.startsWith("[Roundtime") || line.startsWith("[Praying for ")) {
			updateRoundtime(line);
			return;
		}

		if (commandsWaitingForResponse > 0) {
			if (line.startsWith("GSq")) {
				// stop waiting on timestamp
				commandsWaitingForResponse--;
				return;
			} else if (line.startsWith("...wait")) {
				System.out.println("OOPS! In RT, resending: " + lastCommand);
				sendQueue.offerFirst(lastCommand);
				updateRoundtime(line);
			} else if (line.contains("type ahead")) {
				System.out.println("OOPS! Exceeded type ahead limit, resending:" + lastCommand);
				sendQueue.offerFirst("lastCommand");
			}
		}
	}

	void updateRoundtime(String line) {
		roundTimeOver = System.currentTimeMillis() + parseRoundtime(line) * 1000;
	}

	int parseRoundtime(String line) {
		Scanner s = new Scanner(line);
		try {
			while (s.hasNext() && !s.hasNextInt()) {
				s.next();
			}

			if (s.hasNextInt()) {
				return s.nextInt();
			}

			return 0;
		} finally {
			s.close();
		}
	}

	private void processSendQueue() {
		if (sendQueue.isEmpty()) {
			return;
		}

		// do not send commands while in RT
		if (inRoundtime()) {
			// TODO do not block commands that work while in (i.e. EXPERIENCE, LOOK, etc)
			return;
		}

		synchronized (sendQueue) {
			lastCommand = sendQueue.pollFirst();
			if (lastCommand != null) {
				try {
					getSendProxy().send(lastCommand);
					System.out.println(">CommandQueue[" + lastCommand + "]");
					commandsWaitingForResponse++;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void run() {
		outputStreamMonitor.subscribe(this);

		Timer t = new Timer();

		// attempt to send the next command at a scheduled interval
		t.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				processSendQueue();
			}

		}, 0, 50);
	}

	public boolean inRoundtime() {
		return System.currentTimeMillis() < roundTimeOver;
	}

	public long getRoundTimeOver() {
		return roundTimeOver;
	}

	public AbstractProxy getSendProxy() {
		return sendProxy;
	}

	public int size() {
		return sendQueue.size();
	}
}
