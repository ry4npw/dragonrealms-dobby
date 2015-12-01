package pw.ry4n.dr.proxy;

import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import pw.ry4n.dr.engine.sf.model.Program;

public class InterceptingProxy extends AbstractProxy implements StreamListener {
	private Queue<String> sendQueue = new ArrayBlockingQueue<String>(50);
	private Object monitorObject = new Object();
	private boolean waitToSend = false;
	private String lastCommand = null;

	public InterceptingProxy(Socket local, Socket remote) {
		super(local, remote);
	}

	@Override
	public void run() {
		companion.subscribe(this);
		super.run();
	}

	@Override
	protected void filter(String line) throws IOException {
		System.out.println(line);

		if (line.trim().startsWith(";")) {
			// capture commands that start with a semicolon
			String input = line.substring(1);

			// parse command and do something with it
			System.out.println("Captured input: " + input);

			Thread script = new Thread(new Program(input, this, companion));
			script.start();
		} else {
			// all other input should be passed along to server
			send(line);

			// and to any listeners
			notifyAllListeners(line);
		}
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
			while (waitToSend) {
				try {
					monitorObject.wait();
					waitToSend = false;
				} catch (InterruptedException e) {
					// moving on
				}
			}
		}

		synchronized (sendQueue) {
			lastCommand = sendQueue.remove();
			send(lastCommand);
			waitToSend = true;
		}
	}

	@Override
	public void notify(String line) {
		synchronized (monitorObject) {
			if (line.contains("type ahead") || line.startsWith("...wait")) {
				System.out.println("OOPS! Too fast, need to resend: " + lastCommand);
				// TODO handle RT and reinsert line at front of queue
				// (only when the offending command was sent by enqueue and not by client)
			} else {
				waitToSend = false;
				monitorObject.notify();
			}
		}
	}
}
