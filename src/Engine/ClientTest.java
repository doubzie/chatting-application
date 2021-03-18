package Engine;

import GUI.GUI;

public class ClientTest {

	Client client;
	GUI gui;

	public ClientTest(GUI gui) {

		this.gui = gui;

	}

	public void startClient(int serverID, GUI gui) {

		client = new Client(serverID, gui);

	}

	public Client getClient() {
		return client;
	}

}