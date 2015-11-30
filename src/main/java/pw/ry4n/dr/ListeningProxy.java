package pw.ry4n.dr;

import java.io.IOException;
import java.net.Socket;

public class ListeningProxy extends AbstractProxy {
	public ListeningProxy(Socket local, Socket remote) {
		super(local, remote);
	}

	@Override
	protected void filter(String line) throws IOException {
		// pass line along
		send(line);

		if (line.trim().startsWith("GS")) {
			// TODO parse the simutronics protocol
			// https://github.com/sproctor/warlock-gtk/blob/master/docs/SIMU-PROTOCOL
		} else {
			// TODO do something with this for downstream MATCHing
			System.out.println(line);
		}
	}
}
