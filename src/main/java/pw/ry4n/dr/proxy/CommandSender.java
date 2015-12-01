package pw.ry4n.dr.proxy;

import java.io.IOException;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

public class CommandSender implements Runnable, StreamListener {
	private Queue<String> sendQueue = new ArrayBlockingQueue<String>(64);
	private Object monitorObject = new Object();
	private boolean waitingForResponse = false;
	private String lastCommand = null;
	private long lastCommandSent = -1;
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

		synchronized (monitorObject) {
			while (waitingForResponse || inRoundtime()) {
				try {
					monitorObject.wait();
				} catch (InterruptedException e) {
					// moving on
				}
			}
		}

		synchronized (sendQueue) {
			lastCommand = sendQueue.remove();
			getSendProxy().send(lastCommand);
			lastCommandSent = System.currentTimeMillis();
			waitingForResponse = true;
		}
	}

	@Override
	public void notify(String line) {
		synchronized (monitorObject) {
			if (line.startsWith("Roundtime: ")) {
				updateRoundtime(line);
			}

			if (line.startsWith("...wait") && justSentCommand()) {
				System.out.println("In RT, resending: " + lastCommand);
				// TODO reinsert line at front of queue

				updateRoundtime(line);
			} else if (line.contains("type ahead") && justSentCommand()) {
				System.out.println("OOPS! Too fast, need to resend: " + lastCommand);
				// TODO reinsert line at front of queue

				if (!inRoundtime()) {
					// pause for 0.2 seconds
					roundTimeOver = System.currentTimeMillis() + 200;
				}
			}

			waitingForResponse = false;
			monitorObject.notify();
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

	@Override
	public void run() {
		outputStreamMonitor.subscribe(this);
	}

	private boolean inRoundtime() {
		return System.currentTimeMillis() < roundTimeOver;
	}

	private boolean justSentCommand() {
		return System.currentTimeMillis() - lastCommandSent < 100;
	}

	public AbstractProxy getSendProxy() {
		return sendProxy;
	}
}
