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

public class Server3 {

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
	private ReceiveFromClientThread receiveFromServer2;
	private SendToClientThread sendToServer2;
	private Thread receiveFromServer2Thread;
	private Thread sendToServer2Thread;
	private Socket server2Socket;
	@SuppressWarnings("unused")
	private ReceiveFromClientThread receiveFromServer4;
	private SendToClientThread sendToServer4;
	private Thread receiveFromServer4Thread;
	private Thread sendToServer4Thread;
	private Socket server4Socket;
	private String server2IPAddress;
	private int server2PortNumber;
	private int localIndex = 2;

	public Server3(int portNumber, String server2IPAddress, int server2PortNumber) {

		this.portNumber = portNumber;
		this.server2IPAddress = server2IPAddress;
		this.server2PortNumber = server2PortNumber;

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

			showMessage("\nAttemping to connect to Server (2)... \n");
			server2Socket = new Socket(InetAddress.getByName(server2IPAddress), server2PortNumber);
			initiateServer2Threads();

		} catch (Exception e) {

			showMessage("\nCould not connect to Server (2). \n");

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

	private void initiateServer2Threads() throws IOException {

		receiveFromServer2 = new ReceiveFromClientThread(server2Socket);
		receiveFromServer2Thread = new Thread(receiveFromServer2);
		receiveFromServer2Thread.setName("Server 2 receptionThread");
		receiveFromServer2Thread.start();

		sendToServer2 = new SendToClientThread(server2Socket);
		sendToServer2Thread = new Thread(sendToServer2);
		sendToServer2Thread.setName("Server 2 transmissionThread");
		sendToServer2Thread.start();

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

		receiveFromServer4Thread = receptionThreads.get(ID);
		sendToServer4Thread = transmissionThreads.get(ID);

		receiveFromServer4 = receptionSpecializedThreads.get(ID);
		sendToServer4 = transmissionSpecializedThreads.get(ID);

		server4Socket = sockets.get(ID);

		receiveFromServer4Thread.setName("Server 4 receptionThread");
		sendToServer4Thread.setName("Server 4 transmissionThread");

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

	private void sendMessageToServer2(String message) {

		if (server2Socket != null) {

			sendToServer2.printWriter.println(message);
			sendToServer2.printWriter.flush();

		}

	}

	private void sendMessageToServer4(String message) {

		if (server4Socket != null) {

			sendToServer4.printWriter.println(message);
			sendToServer4.printWriter.flush();

		}

	}

	private void broadcast(String message) {

		sendMessageToServer2(message);
		sendMessageToServer4(message);

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

		for (int i = 2; i < globalMemberList.size(); i += 4) {

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

									sendMessageToServer2("$001 " + clientID + " server identification");

									broadcastGlobalMemberList();

									showMessage("\nIntegrated with Server (2): "
											+ server2Socket.getInetAddress().getHostName() + "\n");

								}

								if (commandArray[0].equals("$001")) {

									clientID = Integer.parseInt(commandArray[1]);

									transformClientToServer(clientID);

									showMessage("\nIntegrated with Server (4): "
											+ server4Socket.getInetAddress().getHostName() + "\n");

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

									if (clientID % 4 == 3)
										sendMessageToServer2(message);
									else
										sendMessageToServer4(message);

								}

								if (commandArray[0].equals("$105")) {

									clientID = Integer.parseInt(commandArray[3]);
									String clientName = commandArray[4];

									globalMemberList.set(clientID, clientName);
									broadcastGlobalMemberListToClients();

									if (clientID % 4 == 3)
										sendMessageToServer2(message);
									else
										sendMessageToServer4(message);

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

														if (destinationID % 4 == 3)
															sendMessageToServer4(updatedMessage);
														else if (destinationID % 4 == 2)
															sendMessage(destinationID, updatedMessage);
														else
															sendMessageToServer2(updatedMessage);

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

									if (clientID % 4 == 3)
										sendMessageToServer2(message);
									else
										sendMessageToServer4(message);

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

					server2Socket = new Socket(InetAddress.getByName(server2IPAddress), server2PortNumber);
					initiateServer2Threads();
					break;

				} catch (Exception e) {

				}

			}

		}

	}

}
