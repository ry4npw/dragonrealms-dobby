package pw.ry4n.dr.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ListeningProxy extends AbstractProxy {
	public ListeningProxy(Socket local, Socket remote) {
		super(local, remote);
	}

	private InputStream from;
	private byte[] buffer = new byte[4096];

	@Override
	public void run() {
		try {
			from = localSocket.getInputStream();
		} catch (Exception e) {
			System.err.println("ListeningProxy: cannot get streams");
		}

		int count;
		try {
			// using a BufferedReader breaks things in Avalon (missing
			// newlines?), need straight throughput on the listener
			while (companion != null) {
				if ((count = from.read(buffer)) < 0)
					break;
				to.write(buffer, 0, count);
				filter(new String(buffer, 0, count));
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

	@Override
	protected void filter(String buffer) throws IOException {
		System.out.println(buffer);
		String[] lines = buffer.split("\n");

		for (String line : lines) {
			if (line.startsWith("GS")) {
				// TODO parse the simutronics protocol to expose data
				// https://github.com/sproctor/warlock-gtk/blob/master/docs/SIMU-PROTOCOL
			}

			// notify any listeners
			notifyAllListeners(line);
		}
	}

	@Override
	public void send(String line) throws IOException {
		to.write("dobby [".getBytes());
		to.write(line.getBytes());
		to.write("]".getBytes());
		to.write(NEWLINE);
	}
}
