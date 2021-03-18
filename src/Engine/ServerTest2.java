package Engine;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerTest2 {

	static String server1IPAddress = "127.0.0.1";
	static int server1PortNumber = 5000;

	public static void main(String[] args) throws UnknownHostException {

		System.out.println("\n\nServer (2) IP Address: " + InetAddress.getLocalHost() + "\n");
		Server2 server = new Server2(6000, server1IPAddress, server1PortNumber);
		server.startRunning();

	}

}