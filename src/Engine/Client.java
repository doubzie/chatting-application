package Engine;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JOptionPane;

import GUI.GUI;

public class Client {

	private int clientID;
	private String clientName;
	private Socket socket;
	private String serverIP;
	private int portNumber;
	private Thread transmissionThread;
	private Thread receptionThread;
	private SendToServerThread sendThread;
	private ReceiveFromServerThread receiveThread;
	private ArrayList<String> globalMemberList;
	private GUI gui;

	static Scanner sc = new Scanner(System.in);
	static String server1IPAddress = "127.0.0.1";
	static String server2IPAddress = "127.0.0.1";
	static String server3IPAddress = "127.0.0.1";
	static String server4IPAddress = "127.0.0.1";
	static int server1PortNumber = 5000;
	static int server2PortNumber = 6000;
	static int server3PortNumber = 7000;
	static int server4PortNumber = 8000;

	public Client(int serverID, GUI gui) {

		if (serverID == 1) {
			this.serverIP = server1IPAddress;
			this.portNumber = server1PortNumber;
		}

		else if (serverID == 2) {
			this.serverIP = server2IPAddress;
			this.portNumber = server2PortNumber;
		}

		else if (serverID == 3) {
			this.serverIP = server3IPAddress;
			this.portNumber = server3PortNumber;
		}

		else if (serverID == 4) {
			this.serverIP = server4IPAddress;
			this.portNumber = server4PortNumber;
		}

		this.gui = gui;

		startRunning();

	}

	public void startRunning() {

		globalMemberList = new ArrayList<String>();

		for (int i = 0; i < 100; i++)
			globalMemberList.add(null);

		try {

			connectToServer();
			initiateThreads();

		} catch (EOFException e1) {
			showMessage("Connection terminated!");
		} catch (ConnectException e2) {
			showMessage("Cannot reach server!");
			System.exit(0);
		} catch (SocketException e3) {
			e3.printStackTrace();
		} catch (IOException e4) {
			e4.printStackTrace();
		}

	}

	private void connectToServer() throws IOException {

		// showMessage("\n\nAttemping to connect...");
		socket = new Socket(InetAddress.getByName(serverIP), portNumber);
		// showMessage("Connected to: " +
		// socket.getInetAddress().getHostName());

	}

	private void initiateThreads() {

		sendThread = new SendToServerThread(socket);
		transmissionThread = new Thread(sendThread);
		transmissionThread.start();

		receiveThread = new ReceiveFromServerThread(socket);
		receptionThread = new Thread(receiveThread);
		receptionThread.start();

	}

	public void join(String requestedName) {

		sendMessageToServer("$100 " + clientID + " join request " + requestedName);

	}

	public void chat(String destinationName, String message) {

		sendMessageToServer("$300 " + globalMemberList.indexOf(clientName) + " " + clientName + " chat "
				+ globalMemberList.indexOf(destinationName) + " 4 " + "%" + message);

	}

	public void quit() {

		sendMessageToServer("$400 " + clientID + " " + clientName + " quit");

	}

	private void closeConnection() {

		// showMessage("\nClosing connections...");

		try {

			transmissionThread.interrupt();
			receptionThread.interrupt();
			socket.close();

		} catch (IOException e) {
			e.getStackTrace();
		}

		// showMessage("Connections closed.");
		System.exit(0);

	}

	private void showMessage(String text) {

		JOptionPane.showMessageDialog(null, text);

	}

	private void sendMessageToServer(String message) {

		sendThread.printWriter.println(message);
		sendThread.printWriter.flush();

	}

	// Class 1

	class ReceiveFromServerThread implements Runnable {

		Socket serverSocket;
		BufferedReader bufferedReader;

		public ReceiveFromServerThread(Socket serverSocket) {
			this.serverSocket = serverSocket;
		}

		@Override
		public void run() {

			try {

				bufferedReader = new BufferedReader(new InputStreamReader(this.serverSocket.getInputStream()));

				String message;

				while ((message = bufferedReader.readLine()) != null) {

					String[] commandArray = message.split(" ");

					if (commandArray[0].equals("$000")) {

						sendMessageToServer("$002 " + commandArray[3] + " client identification");

						clientID = Integer.parseInt(commandArray[3]);
						// showMessage("You are registered by the server as
						// Client (" + clientID + ").");

					}

					if (commandArray[0].equals("$101")) {

						clientName = commandArray[4];
						// showMessage("\nYou successfully joined with name: " +
						// commandArray[4]);

					}

					if (commandArray[0].equals("$102")) {
						showMessage("Try with another name.");
						System.exit(0);
					}

					if (commandArray[0].equals("$103"))
						showMessage("You cannot join twice.");

					if (commandArray[0].equals("$104")) {

						clientID = Integer.parseInt(commandArray[1]);

						globalMemberList.set(clientID, "ClientConnectedNotJoined");

					}

					if (commandArray[0].equals("$105")) {

						clientID = Integer.parseInt(commandArray[3]);
						String clientName = commandArray[4];

						globalMemberList.set(clientID, clientName);

					}

					if (commandArray[0].equals("$201")) {

						String[] commandArray2 = message.split("%", 2);
						showMessage("\n");

						String[] names = commandArray2[1].split(" ");

						for (int i = 0; i < names.length; i = i + 2)
							showMessage(names[i] + " " + names[i + 1] + "\n");

					}

					if (commandArray[0].equals("$202"))
						showMessage("You have to join first.");

					if (commandArray[0].equals("$300")) {

						String[] commandArray2 = message.split("%", 2);
						showMessage(commandArray[2] + " (" + commandArray[1] + "): " + commandArray2[1] + "\n");

						int sourceID = Integer.parseInt(commandArray[1]);

						try {

							gui.getChatWindows().get(sourceID).getTextAreaChat()
									.append(commandArray[2] + ": " + commandArray2[1] + "\n\n");

						} catch (Exception e) {

						}

					}

					if (commandArray[0].equals("$302"))
						showMessage("You have to join first.");

					if (commandArray[0].equals("$304"))
						showMessage("You cannot send a message to yourself.");

					if (commandArray[0].equals("$305"))
						showMessage("Your intended recepient has logged off.");

					if (commandArray[0].equals("$306"))
						showMessage("There is no such recepient.");

					if (commandArray[0].equals("$401")) {

						clientID = Integer.parseInt(commandArray[1]);

						globalMemberList.set(clientID, "ClientLoggedOff");

						// showMessage("\nClient " + clientID + " left. \n");

					}

					// if (commandArray[0].charAt(0) != '$')
					// showMessage(message);

				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	// Class 2

	class SendToServerThread implements Runnable {

		Socket serverSocket;
		BufferedReader bufferedReader;
		PrintWriter printWriter;

		public SendToServerThread(Socket serverSocket) {
			this.serverSocket = serverSocket;
		}

		@Override
		public void run() {

			try {

				if (serverSocket.isConnected()) {

					printWriter = new PrintWriter(serverSocket.getOutputStream(), true);

					while (true) {

						bufferedReader = new BufferedReader(new InputStreamReader(System.in));
						String message;
						message = bufferedReader.readLine();
						sendMessage(message);

						if (message.equalsIgnoreCase("quit") || message.equalsIgnoreCase("bye"))
							break;

					}

					closeConnection();

				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private void sendMessage(String message) {

			String[] messageArray = message.split(" ");

			if (messageArray[0].equalsIgnoreCase("join")) {

				try {
					message = "$100 " + clientID + " join request " + messageArray[1];
				}

				catch (ArrayIndexOutOfBoundsException e) {
					showMessage("Incomplete command. ");
				}

			}

			if (message.equalsIgnoreCase("get member list"))
				message = "$200 " + clientID + " " + clientName + " get member list";

			if (messageArray[0].equalsIgnoreCase("chat")) {

				String messageText = "";

				try {

					for (int i = 2; i < messageArray.length; i++)
						messageText += messageArray[i] + " ";

					message = "$300 " + clientID + " " + clientName + " chat " + messageArray[1] + " 4 " + "%"
							+ messageText;

				}

				catch (ArrayIndexOutOfBoundsException e) {
					showMessage("Incomplete command.");
				}

			}

			if (message.equalsIgnoreCase("quit") || message.equalsIgnoreCase("bye"))
				message = "$400 " + clientID + " " + clientName + " " + message;

			printWriter.println(message);
			printWriter.flush();

		}

	}

	public ArrayList<String> getGlobalMemberList() {
		return globalMemberList;
	}

}