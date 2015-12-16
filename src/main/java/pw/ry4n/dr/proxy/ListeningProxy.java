package pw.ry4n.dr.proxy;

import java.io.IOException;
import java.net.Socket;

public class ListeningProxy extends AbstractProxy {
	public ListeningProxy(Socket local, Socket remote) {
		super(local, remote);
	}

	/**
	 * The listening filter will write the buffer directly to the {@code to}
	 * OutputStream. It also will notify any listeners of content that flows
	 * through it.
	 */
	@Override
	protected void filter(byte[] buffer, int count) throws IOException {
		// write output direct to client
		to.write(buffer, 0, count);

		String bufferString = new String(buffer, 0, count, "iso-8859-1");
		System.out.println(bufferString);
		String[] lines = bufferString.split("\n");

		for (String line : lines) {
			if (!"".equals(line)) {
				// notify any listeners
				notifyAllListeners(line.trim());
			}
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
