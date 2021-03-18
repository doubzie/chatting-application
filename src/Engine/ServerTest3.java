package Engine;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerTest3 {

	static String server2IPAddress = "127.0.0.1";
	static int server2PortNumber = 6000;

	public static void main(String[] args) throws UnknownHostException {

		System.out.println("\n\nServer (3) IP Address: " + InetAddress.getLocalHost() + "\n");
		Server3 server = new Server3(7000, server2IPAddress, server2PortNumber);
		server.startRunning();

	}

}