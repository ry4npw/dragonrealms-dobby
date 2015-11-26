package pw.ry4n.dr;

import java.io.BufferedReader;
//import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * A class useful for the proxy server. This class takes over control of newly
 * created connections and redirects the data streams.
 */
public class Redirector implements Runnable {
	private Redirector companion = null;
	private Socket localSocket, remoteSocket;
	//private InputStream from;
	private OutputStream to;
	//private byte[] buffer = new byte[4096];
	private BufferedReader in;
	private final static byte[] NEWLINE = "\n".getBytes();

	/**
	 * redirector gets the streams from sockets
	 */
	public Redirector(Socket local, Socket remote) {
		try {
			localSocket = local;
			remoteSocket = remote;
			//from = localSocket.getInputStream();
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
	public void couple(Redirector c) {
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
				if (line.trim().startsWith(";")) {
					// do something with line
					System.out.println("Captured command: " + line);
				} else {
					System.out.println(line);
					to.write(line.getBytes());
					to.write(NEWLINE);
				}
			}
		} catch (Exception e) {
			System.err.println("redirector: connection lost");
		}
		try {
			in.close();
			//from.close();
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
}
