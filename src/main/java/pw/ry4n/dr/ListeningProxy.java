package pw.ry4n.dr;

import java.io.IOException;
import java.net.Socket;

public class ListeningProxy extends AbstractProxy {
	public ListeningProxy(Socket local, Socket remote) {
		super(local, remote);
	}

	@Override
	protected void filter(String line) throws IOException {
		// forward line upstream, do not wait on other logic.
		send(line);

		if (line.trim().startsWith("GS")) {
			// TODO figure out the simutronics protocol
			// https://github.com/sproctor/warlock-gtk/blob/master/docs/SIMU-PROTOCOL
		} else {
			// TODO This is where we can MATCH input.
			System.out.println(line);
		}
	}
}
