package pw.ry4n.dr;

import java.net.*;
import java.io.*;
import java.util.*;

import pw.ry4n.dr.proxy.AbstractProxy;
import pw.ry4n.dr.proxy.InterceptingProxy;
import pw.ry4n.dr.proxy.ListeningProxy;

public class Dobby implements Runnable {
	String remoteHost;
	int localPort;
	int remotePort;
	Thread listener;
	Thread connection;

	ServerSocket server;

	/**
	 * Create a server socket and start listening on the local port.
	 * 
	 * @param lport
	 *            local port
	 * @param raddr
	 *            address of the destination
	 * @param rport
	 *            port on the destination host
	 */
	public Dobby(int lport, String raddr, int rport) {
		localPort = lport;
		remoteHost = raddr;
		remotePort = rport;

		log("destination host is " + remoteHost + " at port " + remotePort);
		try {
			server = new ServerSocket(localPort);
		} catch (Exception e) {
			System.err.println("proxy: error: cannot create server socket");
			e.printStackTrace();
		}
		log("listening on port " + localPort);

		listener = new Thread(this);
		listener.setPriority(Thread.MIN_PRIORITY);
		listener.start();
	}

	/**
	 * This method is called when the application is run on the commandline. It
	 * takes two or three arguments: usage: java proxy local-port
	 * destination-host destination-port
	 * 
	 * @param args
	 *            The command line arguments
	 */
	public static void main(String args[]) {
		String remoteHost = null;
		int localPort = 4901, remotePort = 4901;

		System.out.println("proxy: arguments = " + args.length);

		if (args.length < 2) {
			try {
				InetAddress address = InetAddress.getByName("dr.simutronics.net");
				remoteHost = address.getHostAddress();

				if ("127.0.0.1".equals(remoteHost)) {
					System.err.println("proxy: You need to remove the dr.simutronics.net entry from /etc/hosts");
					System.exit(1);
				}
			} catch (UnknownHostException e) {
				System.err.println("proxy: unable to resovle IP for dr.simutronics.net");
			}
		} else {
			remoteHost = args[1];

			try {
				localPort = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.err.println("proxy: parameter <port>: number expected");
				System.exit(1);
			}

			if (args.length > 2) {
				try {
					remotePort = Integer.parseInt(args[2]);
				} catch (Exception e) {
					System.err.println("proxy: parameter <destination port>: " + "number expected");
					System.exit(1);
				}
			}
		}

		new Dobby(localPort, remoteHost, (remotePort == 0 ? 23 : remotePort));
	}

	/**
	 * Cycle around until an error occurs and wait for incoming connections. An
	 * incoming connection will create two redirectors. One for local-host -
	 * destination-host and one for destination-host - local-host.
	 */
	public void run() {
		while (true) {
			Socket localSocket = null;
			try {
				localSocket = server.accept();
			} catch (Exception e) {
				System.err.println("proxy: error: accept connection failed");
				continue;
			}
			log("accepted connection from " + localSocket.getInetAddress().getHostName());
			try {
				Socket destinationSocket = new Socket(remoteHost, remotePort);
				log("connecting " + localSocket.getInetAddress().getHostName() + " <-> "
						+ destinationSocket.getInetAddress().getHostName());
				AbstractProxy r1 = new InterceptingProxy(localSocket, destinationSocket);
				AbstractProxy r2 = new ListeningProxy(destinationSocket, localSocket);
				r1.couple(r2);
				r2.couple(r1);
			} catch (Exception e) {
				System.err.println("proxy: error: cannot create sockets");
				try {
					DataOutputStream os = new DataOutputStream(localSocket.getOutputStream());
					os.writeChars("Remote host refused connection.\n");
					localSocket.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				continue;
			}
		}
	}

	private void log(String msg) {
		System.out.println("proxy: [" + new Date() + "] " + msg);
	}
}
