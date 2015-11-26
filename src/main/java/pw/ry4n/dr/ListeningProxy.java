package pw.ry4n.dr;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ListeningProxy extends AbstractProxy {
	public ListeningProxy(Socket local, Socket remote) {
		super(local, remote);
	}

	@Override
	protected void filter(String line, OutputStream send) throws IOException {
		if (line.trim().startsWith("GS")) {
			// TODO figure out the simutronics protocol
			//
			// https://github.com/sproctor/warlock-gtk/blob/master/docs/SIMU-PROTOCOL
		} else {
			// TODO do something with line, but DO NOT modify it
			System.out.println(line);
		}

		send.write(line.getBytes());
		send.write(NEWLINE);
	}
}
