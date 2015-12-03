package pw.ry4n.dr.proxy;

import java.io.IOException;
import java.util.Queue;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

public class CommandSender implements Runnable, StreamListener {
	private Queue<String> sendQueue = new ArrayBlockingQueue<String>(64);
	private boolean waitingForResponse = false;
	private String lastCommand = null;
	private long roundTimeOver = -1;

	private AbstractProxy sendProxy;
	private StreamMonitor outputStreamMonitor;

	CommandSender() {
		// package private constructor for unit tests
	}

	public CommandSender(AbstractProxy sendProxy, StreamMonitor outputStreamMonitor) {
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
				// TODO reinsert lastCommand at front of queue

				updateRoundtime(line);
			} else if (line.contains("type ahead")) {
				System.out.println("OOPS! Too fast, need to resend: " + lastCommand);
				// TODO reinsert lastCommand at front of queue
	
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

		// TODO RT handling/blocking

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

		}, 0, 50);
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
