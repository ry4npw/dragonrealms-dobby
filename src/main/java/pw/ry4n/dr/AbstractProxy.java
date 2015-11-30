package pw.ry4n.dr;

import java.io.BufferedReader;
import java.io.IOException;
//import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * A class useful for the proxy server. This class takes over control of newly
 * created connections and redirects the data streams.
 */
public abstract class AbstractProxy implements Runnable {
	protected AbstractProxy companion = null;
	protected Socket localSocket, remoteSocket;
	protected OutputStream to;
	protected BufferedReader in;
	protected final static byte[] NEWLINE = "\n".getBytes();

	/**
	 * redirector gets the streams from sockets
	 */
	public AbstractProxy(Socket local, Socket remote) {
		try {
			localSocket = local;
			remoteSocket = remote;
			// from = localSocket.getInputStream();
			in = new BufferedReader(new InputStreamReader(localSocket.getInputStream()));
			to = remoteSocket.getOutputStream();
		} catch (Exception e) {
			System.err.println("redirector: cannot get streams");
		}
	}

	/**
	 * couple this redirector instance with another one (usually the other
	 * direction of the connection)
	 */
	public void couple(AbstractProxy c) {
		companion = c;
		Thread listen = new Thread(this);
		listen.start();
	}

	/**
	 * decouple us from our companion. This will let this redirector die after
	 * exiting from run()
	 */
	public void decouple() {
		companion = null;
	}

	/**
	 * read data from the input and write it to the destination stream until an
	 * error occurs or our companion is decoupled from us
	 */
	public void run() {
		String line;
		try {
			while (companion != null) {
				if ((line = in.readLine()) == null)
					break;
				filter(line);
			}
		} catch (Exception e) {
			System.err.println("redirector: connection lost");
		}
		try {
			in.close();
			// from.close();
			to.close();
			localSocket.close();
			remoteSocket.close();
			// is our companion dead? no, then decouple, because we die
			if (companion != null)
				companion.decouple();
		} catch (Exception io) {
			System.err.println("redirector: error closing streams and sockets");
			io.printStackTrace();
		}
	}

	/**
	 * <p>
	 * This method is to be overridden to create different types of filters. A
	 * few examples would include a read-only (or listening) filter, an
	 * intercepting filter, etc.
	 * </p>
	 * 
	 * <p>
	 * This filter needs to forward necessary input on, this is accomplished by:
	 * </p>
	 * 
	 * <pre>
	 * send(line);
	 * </pre>
	 * 
	 * @param line
	 * @throws IOException
	 */
	protected abstract void filter(String line) throws IOException;

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
	public void send(String line) throws IOException {
		// TODO turn this into a queue to mitigate errors and handle RT and
		// type ahead line problems
		to.write("dobby [".getBytes());
		to.write(line.getBytes());
		to.write("]".getBytes());
		to.write(NEWLINE);
	}
}
