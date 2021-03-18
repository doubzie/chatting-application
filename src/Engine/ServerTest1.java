package Engine;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerTest1 {

	public static void main(String[] args) throws UnknownHostException {

		System.out.println("\n\nServer (1) IP Address: " + InetAddress.getLocalHost() + "\n");
		Server1 server = new Server1(5000);
		server.startRunning();

	}

}