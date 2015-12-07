package pw.ry4n.dr.proxy;

import java.io.IOException;
import java.util.Deque;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedDeque;

public class CommandQueue implements Runnable, StreamListener {
	private Deque<String> sendQueue = new ConcurrentLinkedDeque<String>();
	private boolean waitingForResponse = false;
	private boolean waitForRT = false;
	private String lastCommand = null;
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
			sendQueue.offer(line);
		}
	}

	@Override
	public void notify(String line) {
		if (line.startsWith("Roundtime: ")) {
			updateRoundtime(line);
		}

		if (justSentCommand()) {
			if (line.startsWith("...wait")) {
				System.out.println("In RT, resending: " + lastCommand);
				sendQueue.push(lastCommand);
				updateRoundtime(line);
				waitForRT = true;
			} else if (line.contains("type ahead")) {
				System.out.println("OOPS! Too fast, need to resend: " + lastCommand);
				sendQueue.push("lastCommand");
			}

			waitingForResponse = false;
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

		// RT handling/blocking
		if (waitForRT) {
			if (inRoundtime()) {
				return;
			} else {
				waitForRT = false;
			}
		}

		synchronized (sendQueue) {
			lastCommand = sendQueue.poll();
			if (lastCommand != null) {
				try {
					getSendProxy().send(lastCommand);
					System.out.println("[CommandSender] " + lastCommand);
					waitingForResponse = true;
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

		t.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				processSendQueue();
			}

		}, 0, 10);
	}

	private boolean inRoundtime() {
		return System.currentTimeMillis() < roundTimeOver;
	}

	private boolean justSentCommand() {
		return waitingForResponse;
	}

	public AbstractProxy getSendProxy() {
		return sendProxy;
	}
}
