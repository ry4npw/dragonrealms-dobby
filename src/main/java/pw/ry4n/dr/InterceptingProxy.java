package pw.ry4n.dr;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class InterceptingProxy extends AbstractProxy {
	public InterceptingProxy(Socket local, Socket remote) {
		super(local, remote);
	}

	@Override
	protected void filter(String line, OutputStream send) throws IOException {
		if (line.trim().startsWith(";")) {
			System.out.println("Captured command: " + line.substring(1));
			// do something with line
		} else {
			System.out.println(line);
			send.write(line.getBytes());
			send.write(NEWLINE);
		}
	}
}
