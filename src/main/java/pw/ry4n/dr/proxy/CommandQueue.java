package pw.ry4n.dr.proxy;

import java.io.IOException;
import java.util.Deque;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Implementation of a command queue where commands are limited to be sent only
 * after some server response is received from the last command. Specifically,
 * commands are not released from the queue until a server timestamp ("GSq"
 * response) is received.
 * 
 * Commands are stored and sent from a FIFO command queue.
 * 
 * @author Ryan Powell
 */
public class CommandQueue implements Runnable, StreamListener {
	protected QueueState state = QueueState.CLEAR;
	protected Deque<String> sendQueue = new ConcurrentLinkedDeque<String>();
	protected String lastCommand = null;
	protected long roundTimeOver = -1;

	protected AbstractProxy sendProxy;
	protected StreamMonitor outputStreamMonitor;

	CommandQueue() {
		// package private constructor for unit testing only
	}

	public CommandQueue(AbstractProxy sendProxy, StreamMonitor outputStreamMonitor) {
		this.sendProxy = sendProxy;
		this.outputStreamMonitor = outputStreamMonitor;
	}

	public void enqueue(String line) throws IOException {
		synchronized (sendQueue) {
			sendQueue.offerLast(line);
		}
	}

	@Override
	public void notify(String line) {
		if (line == null) {
			return;
		}

		// update RT
		if (line.contains("Roundtime") || line.startsWith("[Praying for ")) {
			updateRoundtime(line);
			return;
		}

		if (QueueState.BLOCKING.equals(state)) {
			// stop blocking on server response
			if (line.startsWith("GSq") || line.startsWith("GSQ")) {
				state = QueueState.CLEAR;
			} else if (line.startsWith("...wait")) {
				// resend the last command on RT
				System.out.println("OOPS! In RT, resending: " + lastCommand);
				sendQueue.offerFirst(lastCommand);
				updateRoundtime(line);
			} else if (line.contains("type ahead")) {
				// resend the last command on type ahead
				System.out.println("OOPS! Exceeded type ahead limit, resending:" + lastCommand);
				sendQueue.offerFirst(lastCommand);
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

	protected void processSendQueue() {
		if (QueueState.BLOCKING.equals(state) || sendQueue.isEmpty() || inRoundtime()) {
			return;
		}

		lastCommand = sendQueue.pollFirst();
		if (lastCommand != null) {
			try {
				state = QueueState.BLOCKING;
				getSendProxy().send(lastCommand);
				System.out.println(">CommandQueue[" + lastCommand + "]");
			} catch (IOException e) {
				e.printStackTrace();
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

	public enum QueueState {
		CLEAR, BLOCKING;
	}
}
