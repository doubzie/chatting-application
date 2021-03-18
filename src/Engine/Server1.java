package Engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server1 {

	private int portNumber;
	private ServerSocket listenerSocket;
	private ArrayList<Socket> sockets;
	private ArrayList<String> globalMemberList;
	private ArrayList<Thread> receptionThreads;
	private ArrayList<Thread> transmissionThreads;
	private ArrayList<ReceiveFromClientThread> receptionSpecializedThreads;
	private ArrayList<SendToClientThread> transmissionSpecializedThreads;
	private WaitForConnectionThread waitForConnection;
	@SuppressWarnings("unused")
	private ReceiveFromClientThread receiveFromServer2;
	private SendToClientThread sendToServer2;
	private Thread receiveFromServer2Thread;
	private Thread sendToServer2Thread;
	private Socket server2Socket;
	private int localIndex = 0;

	public Server1(int portNumber) {

		this.portNumber = portNumber;

	}

	public void startRunning() {

		try {

			listenerSocket = new ServerSocket(portNumber);

		} catch (IOException e) {
			e.printStackTrace();
		}

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
			initiateThreads(socket, sockets.indexOf(socket));

		}

	}

	private void initiateThreads(Socket socket, int threadID) throws IOException {

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

		receiveFromServer2Thread = receptionThreads.get(ID);
		sendToServer2Thread = transmissionThreads.get(ID);

		receiveFromServer2 = receptionSpecializedThreads.get(ID);
		sendToServer2 = transmissionSpecializedThreads.get(ID);

		server2Socket = sockets.get(ID);

		receiveFromServer2Thread.setName("Server 2 receptionThread");
		sendToServer2Thread.setName("Server 2 transmissionThread");

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

	private void broadcast(String message) {

		sendMessageToServer2(message);

	}

	private void broadcastToJoinedClients(String message) {

		for (int i = 0; i < globalMemberList.size(); i += 4) {

			if (globalMemberList.get(i) != null && !globalMemberList.get(i).equals("ClientConnectedNotJoined")
					&& !globalMemberList.get(i).equals("ClientLoggedOff")) {

				sendMessage(i, message);

			}

		}

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

								if (commandArray[0].equals("$001")) {

									clientID = Integer.parseInt(commandArray[1]);

									transformClientToServer(clientID);

									showMessage("\nIntegrated with Server (2): "
											+ server2Socket.getInetAddress().getHostName() + "\n");

								}

								if (commandArray[0].equals("$002")) {

									clientID = Integer.parseInt(commandArray[1]);

									globalMemberList.set(clientID, "ClientConnectedNotJoined");

									if (server2Socket != null)
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

								}

								if (commandArray[0].equals("$105")) {

									clientID = Integer.parseInt(commandArray[3]);
									String clientName = commandArray[4];

									globalMemberList.set(clientID, clientName);
									broadcastGlobalMemberListToClients();
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

}
