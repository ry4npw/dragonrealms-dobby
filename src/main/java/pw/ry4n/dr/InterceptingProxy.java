package pw.ry4n.dr;

import java.io.IOException;
import java.net.Socket;

import pw.ry4n.dr.engine.sf.model.Program;
import pw.ry4n.dr.engine.sf.parser.FileParser;

public class InterceptingProxy extends AbstractProxy {
	public InterceptingProxy(Socket local, Socket remote) {
		super(local, remote);
	}

	@Override
	protected void filter(String line) throws IOException {
		if (line.trim().startsWith(";")) {
			// capture commands that start with a semicolon
			String command = line.substring(1);

			// TODO parse command and do something with it
			System.out.println("Captured command: " + command);
			FileParser fileParser = new FileParser(command);
			Program program = fileParser.parse();
			program.run();
		} else {
			System.out.println(line);

			// all other input should be sent upstream
			send(line);
		}
	}
}
