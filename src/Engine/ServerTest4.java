package Engine;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerTest4 {

	static String server3IPAddress = "127.0.0.1";
	static int server3PortNumber = 7000;

	public static void main(String[] args) throws UnknownHostException {

		System.out.println("\n\nServer (4) IP Address: " + InetAddress.getLocalHost() + "\n");
		Server4 server = new Server4(8000, server3IPAddress, server3PortNumber);
		server.startRunning();

	}

}