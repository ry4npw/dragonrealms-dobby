package pw.ry4n.dr.proxy;

import java.io.IOException;
import java.net.Socket;

public class ListeningProxy extends AbstractProxy {
	public ListeningProxy(Socket local, Socket remote) {
		super(local, remote);
	}

	@Override
	protected void filter(String line) throws IOException {
		// pass line along
		super.send(line);

		if (line.trim().startsWith("GS")) {
			// TODO parse the simutronics protocol
			// https://github.com/sproctor/warlock-gtk/blob/master/docs/SIMU-PROTOCOL
		}

		// notify any listeners
		notifyAllListeners(line);
	}

	@Override
	public void send(String line) throws IOException {
		to.write("dobby [".getBytes());
		to.write(line.getBytes());
		to.write("]".getBytes());
		to.write(NEWLINE);
	}
}
