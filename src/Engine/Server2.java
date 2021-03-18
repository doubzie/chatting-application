package Engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server2 {

	private int portNumber;
	private ServerSocket listenerSocket;
	private ArrayList<Socket> sockets;
	private ArrayList<String> globalMemberList;
	private ArrayList<Thread> receptionThreads;
	private ArrayList<Thread> transmissionThreads;
	private ArrayList<ReceiveFromClientThread> receptionSpecializedThreads;
	private ArrayList<SendToClientThread> transmissionSpecializedThreads;
	private WaitForConnectionThread waitForConnection;
	private TryConnectionThread tryConnection;
	private ReceiveFromClientThread receiveFromServer1;
	private SendToClientThread sendToServer1;
	private Thread receiveFromServer1Thread;
	private Thread sendToServer1Thread;
	private Socket server1Socket;
	@SuppressWarnings("unused")
	private ReceiveFromClientThread receiveFromServer3;
	private SendToClientThread sendToServer3;
	private Thread receiveFromServer3Thread;
	private Thread sendToServer3Thread;
	private Socket server3Socket;
	private String server1IPAddress;
	private int server1PortNumber;
	private int localIndex = 1;

	public Server2(int portNumber, String server1IPAddress, int server1PortNumber) {

		this.portNumber = portNumber;
		this.server1IPAddress = server1IPAddress;
		this.server1PortNumber = server1PortNumber;

	}

	public void startRunning() {

		sockets = new ArrayList<Socket>();
		globalMemberList = new ArrayList<String>();
		receptionThreads = new ArrayList<Thread>();
		transmissionThreads = new ArrayList<Thread>();
		receptionSpecializedThreads = new ArrayList<ReceiveFromClientThread>();
		transmissionSpecializedThreads = new ArrayList<SendToClientThread>();

		for (int i = 0; i < 100; i++) {

			sockets.add(null);
			globalMemberList.add(null);
			receptionThreads.add(null);
			transmissionThreads.add(null);
			receptionSpecializedThreads.add(null);
			transmissionSpecializedThreads.add(null);

		}

		try {

			showMessage("\nAttemping to connect to Server (1)... \n");
			server1Socket = new Socket(InetAddress.getByName(server1IPAddress), server1PortNumber);
			initiateServer1Threads();

		} catch (Exception e) {

			showMessage("\nCould not connect to Server (1). \n");

			tryConnection = new TryConnectionThread();
			Thread tryConnectionThread = new Thread(tryConnection);
			tryConnectionThread.setName("tryConnectionThread");
			tryConnectionThread.start();

		}

		try {

			listenerSocket = new ServerSocket(portNumber);

		} catch (IOException e) {
			e.printStackTrace();
		}

		waitForConnection = new WaitForConnectionThread();
		Thread waitForConnectionThread = new Thread(waitForConnection);
		waitForConnectionThread.setName("waitForConnectionThread");
		waitForConnectionThread.start();

	}

	private void waitForConnection() throws IOException {

		while (true) {

			Socket socket = listenerSocket.accept();
			sockets.set(localIndex, socket);
			localIndex = localIndex + 4;
			initiateClientThreads(socket, sockets.indexOf(socket));

		}

	}

	private void initiateServer1Threads() throws IOException {

		receiveFromServer1 = new ReceiveFromClientThread(server1Socket);
		receiveFromServer1Thread = new Thread(receiveFromServer1);
		receiveFromServer1Thread.setName("Server 1 receptionThread");
		receiveFromServer1Thread.start();

		sendToServer1 = new SendToClientThread(server1Socket);
		sendToServer1Thread = new Thread(sendToServer1);
		sendToServer1Thread.setName("Server 1 transmissionThread");
		sendToServer1Thread.start();

	}

	private void initiateClientThreads(Socket socket, int threadID) throws IOException {

		ReceiveFromClientThread receiveFromClient = new ReceiveFromClientThread(socket);
		Thread receptionThread = new Thread(receiveFromClient);
		receptionThread.setName("Client " + threadID + " receptionThread");
		receptionThreads.set(threadID, receptionThread);
		receptionThread.start();

		SendToClientThread sendToClient = new SendToClientThread(socket);
		Thread transmissionThread = new Thread(sendToClient);
		transmissionThread.setName("Client " + threadID + " transmissionThread");
		transmissionThreads.set(threadID, transmissionThread);
		transmissionThread.start();

		receptionSpecializedThreads.set(threadID, receiveFromClient);
		transmissionSpecializedThreads.set(threadID, sendToClient);

		sendMessage(sockets.indexOf(socket), "$000 identify self " + threadID);

	}

	private void transformClientToServer(int ID) {

		receiveFromServer3Thread = receptionThreads.get(ID);
		sendToServer3Thread = transmissionThreads.get(ID);

		receiveFromServer3 = receptionSpecializedThreads.get(ID);
		sendToServer3 = transmissionSpecializedThreads.get(ID);

		server3Socket = sockets.get(ID);

		receiveFromServer3Thread.setName("Server 3 receptionThread");
		sendToServer3Thread.setName("Server 3 transmissionThread");

		globalMemberList.remove(ID);

		receptionThreads.remove(ID);
		transmissionThreads.remove(ID);

		receptionSpecializedThreads.remove(ID);
		transmissionSpecializedThreads.remove(ID);

		sockets.remove(ID);

		localIndex = localIndex - 4;

		broadcastGlobalMemberList();

	}

	private void closeConnection(int ID) {

		broadcast("$401 " + ID + " " + globalMemberList.get(ID) + " quit");

		try {

			globalMemberList.set(ID, "ClientLoggedOff");

			receptionThreads.get(ID).interrupt();
			transmissionThreads.get(ID).interrupt();

			receptionThreads.set(ID, null);
			transmissionThreads.set(ID, null);

			receptionSpecializedThreads.set(ID, null);
			transmissionSpecializedThreads.set(ID, null);

			sockets.set(ID, null);

		} catch (Exception e) {

		}

		broadcastGlobalMemberListToClients();

		showMessage("\nClient (" + ID + ") left. \n");

	}

	private void sendMessage(int destinationID, String message) {

		transmissionSpecializedThreads.get(destinationID).printWriter.println(message);
		transmissionSpecializedThreads.get(destinationID).printWriter.flush();

	}

	private void sendMessageToServer1(String message) {

		if (server1Socket != null) {

			sendToServer1.printWriter.println(message);
			sendToServer1.printWriter.flush();

		}

	}

	private void sendMessageToServer3(String message) {

		if (server3Socket != null) {

			sendToServer3.printWriter.println(message);
			sendToServer3.printWriter.flush();

		}

	}

	private void broadcast(String message) {

		sendMessageToServer1(message);
		sendMessageToServer3(message);

	}

	private void showMessage(String text) {

		System.out.println(text);

	}

	private String getGlobalMemberList() {

		String globalMemberListString = "";

		for (int i = 0; i < globalMemberList.size(); i++) {

			if (globalMemberList.get(i) != null && !globalMemberList.get(i).equals("ClientConnectedNotJoined")
					&& !globalMemberList.get(i).equals("ClientLoggedOff"))
				globalMemberListString += globalMemberList.get(i) + " (" + i + ") ";

		}

		return globalMemberListString;

	}

	private void broadcastGlobalMemberList() {

		for (int i = 0; i < globalMemberList.size(); i++) {

			if (globalMemberList.get(i) != null) {

				if (globalMemberList.get(i).equals("ClientConnectedNotJoined"))
					broadcast("$104 " + i + " add connectedClient");

				else if (globalMemberList.get(i).equals("ClientLoggedOff"))
					broadcast("$401 " + i + " " + globalMemberList.get(i) + " quit");

				else
					broadcast("$105 add joinedClient " + i + " " + globalMemberList.get(i));

			}

		}

	}

	private void broadcastToJoinedClients(String message) {

		for (int i = 1; i < globalMemberList.size(); i += 4) {

			if (globalMemberList.get(i) != null && !globalMemberList.get(i).equals("ClientConnectedNotJoined")
					&& !globalMemberList.get(i).equals("ClientLoggedOff")) {

				sendMessage(i, message);

			}

		}

	}

	private void broadcastGlobalMemberListToClients() {

		for (int i = 0; i < globalMemberList.size(); i++) {

			if (globalMemberList.get(i) != null) {

				if (globalMemberList.get(i).equals("ClientConnectedNotJoined"))
					broadcastToJoinedClients("$104 " + i + " add connectedClient");

				else if (globalMemberList.get(i).equals("ClientLoggedOff"))
					broadcastToJoinedClients("$401 " + i + " " + globalMemberList.get(i) + " quit");
				else
					broadcastToJoinedClients("$105 add joinedClient " + i + " " + globalMemberList.get(i));

			}

		}

	}

	// Class 1

	class ReceiveFromClientThread implements Runnable {

		Socket clientSocket;
		BufferedReader bufferedReader;

		public ReceiveFromClientThread(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		@Override
		public void run() {

			try {

				bufferedReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
				String message;

				while (true) {

					while (((message = bufferedReader.readLine()) != null)) {

						if (!message.equals("")) {

							if (message.charAt(0) == '$') {

								String[] commandArray = message.split(" ");
								int clientID;

								if (commandArray[0].equals("$000")) {

									clientID = Integer.parseInt(commandArray[3]);

									sendMessageToServer1("$001 " + clientID + " server identification");

									broadcastGlobalMemberList();

									showMessage("\nIntegrated with Server (1): "
											+ server1Socket.getInetAddress().getHostName() + "\n");

								}

								if (commandArray[0].equals("$001")) {

									clientID = Integer.parseInt(commandArray[1]);

									transformClientToServer(clientID);

									showMessage("\nIntegrated with Server (3): "
											+ server3Socket.getInetAddress().getHostName() + "\n");

								}

								if (commandArray[0].equals("$002")) {

									clientID = Integer.parseInt(commandArray[1]);

									globalMemberList.set(clientID, "ClientConnectedNotJoined");
									broadcast("$104 " + clientID + " add connectedClient");

									broadcastGlobalMemberListToClients();

									showMessage("\nConnected with Client (" + clientID + "): "
											+ sockets.get(clientID).getInetAddress().getHostName() + "\n");

								}

								if (commandArray[0].equals("$100")) {

									clientID = Integer.parseInt(commandArray[1]);
									String clientName = commandArray[4];

									if (!globalMemberList.get(clientID).equals("ClientConnectedNotJoined"))
										sendMessage(clientID, "$103 repeated join " + clientID + " " + clientName);

									else {

										if (globalMemberList.contains(clientName))
											sendMessage(clientID, "$102 join denied " + clientID + " " + clientName);

										else {

											globalMemberList.set(clientID, clientName);
											broadcast("$105 add joinedClient " + clientID + " " + clientName);
											broadcastGlobalMemberListToClients();
											sendMessage(clientID, "$101 join granted " + clientID + " " + clientName);

										}

									}

								}

								if (commandArray[0].equals("$104")) {

									clientID = Integer.parseInt(commandArray[1]);

									globalMemberList.set(clientID, "ClientConnectedNotJoined");

									if (clientID % 4 == 0)
										sendMessageToServer3(message);
									else
										sendMessageToServer1(message);
								}

								if (commandArray[0].equals("$105")) {

									clientID = Integer.parseInt(commandArray[3]);
									String clientName = commandArray[4];

									globalMemberList.set(clientID, clientName);
									broadcastGlobalMemberListToClients();

									if (clientID % 4 == 0)
										sendMessageToServer3(message);
									else
										sendMessageToServer1(message);

								}

								if (commandArray[0].equals("$200")) {

									clientID = Integer.parseInt(commandArray[1]);

									if (!globalMemberList.contains(commandArray[2]))
										sendMessage(clientID, "$202 " + clientID + " permission denied");

									else
										sendMessage(clientID, "$201 " + clientID + " send member list " + "%"
												+ getGlobalMemberList());

								}

								if (commandArray[0].equals("$300")) {

									clientID = Integer.parseInt(commandArray[1]);

									if (!globalMemberList.contains(commandArray[2]))
										sendMessage(clientID, "$302 " + clientID + " permission denied");

									else {

										int sourceID = Integer.parseInt(commandArray[1]);
										int destinationID = -1;

										try {

											destinationID = Integer.parseInt(commandArray[4]);

										} catch (NumberFormatException e) {
											sendMessage(clientID, "$306 " + clientID + " non-existent destination");
										}

										int ttl = Integer.parseInt(commandArray[5]);

										if (sourceID == destinationID)
											sendMessage(clientID, "$304 " + clientID + " message to self");

										else {

											if (destinationID < 0 || destinationID > globalMemberList.size()
													|| globalMemberList.get(destinationID) == null || globalMemberList
															.get(destinationID).equals("ClientConnectedNotJoined"))
												sendMessage(clientID, "$306 " + clientID + " non-existent destination");

											else {

												if (globalMemberList.get(destinationID).equals("ClientLoggedOff"))
													sendMessage(clientID,
															"$305 " + clientID + " destination logged off");

												else {

													if (--ttl < 0)
														sendMessage(clientID, "$303 " + clientID + " ttl timeout");

													else {

														String[] commandArray2 = message.split("%", 2);

														String updatedMessage = "$300 ";

														updatedMessage += commandArray[1] + " ";
														updatedMessage += commandArray[2] + " ";
														updatedMessage += "chat ";
														updatedMessage += commandArray[4] + " ";
														updatedMessage += ttl + " ";
														updatedMessage += "%";
														updatedMessage += commandArray2[1];

														if (destinationID % 4 == 0)
															sendMessageToServer1(updatedMessage);
														else if (destinationID % 4 == 1)
															sendMessage(destinationID, updatedMessage);
														else
															sendMessageToServer3(updatedMessage);

													}

												}

											}

										}

									}

								}

								if (commandArray[0].equals("$400")) {

									clientID = Integer.parseInt(commandArray[1]);
									closeConnection(clientID);

								}

								if (commandArray[0].equals("$401")) {

									clientID = Integer.parseInt(commandArray[1]);

									globalMemberList.set(clientID, "ClientLoggedOff");

									broadcastGlobalMemberListToClients();

									if (clientID % 4 == 0)
										sendMessageToServer3(message);
									else
										sendMessageToServer1(message);

									showMessage("\nClient (" + clientID + ") left. \n");

								}

							}

						}

					}

				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	// Class 2

	class SendToClientThread implements Runnable {

		int threadID;
		PrintWriter printWriter;
		Socket clientSocket;

		public SendToClientThread(Socket clientSocket) {

			this.clientSocket = clientSocket;

			try {

				printWriter = new PrintWriter(new OutputStreamWriter(this.clientSocket.getOutputStream()));

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void run() {

			try {

				while (true) {

					String message;
					BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

					message = input.readLine();
					printWriter.println(message);
					printWriter.flush();

				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	// Class 3

	class WaitForConnectionThread implements Runnable {

		public WaitForConnectionThread() {

		}

		@Override
		public void run() {

			try {

				waitForConnection();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	// Class 4

	class TryConnectionThread implements Runnable {

		public TryConnectionThread() {

		}

		@Override
		public void run() {

			while (true) {

				try {

					server1Socket = new Socket(InetAddress.getByName(server1IPAddress), server1PortNumber);
					initiateServer1Threads();
					break;

				} catch (Exception e) {

				}

			}

		}

	}

}
