package Listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import Engine.ClientTest;
import GUI.GUI;
import GUI.Window1;
import GUI.Window2;
import GUI.Window3;

public class Controller implements ActionListener, WindowListener {

	ClientTest clientTest;
	GUI gui;

	public Controller(ClientTest clientTest, GUI gui) {

		this.clientTest = clientTest;
		this.gui = gui;
		addActionListeners();

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals("Sign in")) {

			try {

				clientTest.startClient((int) (gui.getSignInWindow().getSpinnerServer().getValue()), gui);

				int initialTimeMillis = (int) System.currentTimeMillis();
				boolean timeElapsed = false;
				int difference;

				while (!timeElapsed) {

					difference = (int) System.currentTimeMillis() - initialTimeMillis;

					gui.getSignInWindow().getProgressBar().setValue(difference);
					gui.getSignInWindow().getProgressBar().update(gui.getSignInWindow().getProgressBar().getGraphics());

					if (difference > 5000)
						timeElapsed = true;

				}

				String proposedName = gui.getSignInWindow().getTextFieldUsername().getText();

				if (proposedName.equals("") || proposedName.equals(" ")) {

					JOptionPane.showMessageDialog(null, "You need to provide a name to sign in.");
					clientTest.getClient().quit();
					System.exit(0);

				} else
					clientTest.getClient().join(proposedName);

				int initialTimeMillis2 = (int) System.currentTimeMillis();
				boolean timeElapsed2 = false;
				int difference2;

				while (!timeElapsed2) {

					difference2 = (int) System.currentTimeMillis() - initialTimeMillis2;

					gui.getSignInWindow().getProgressBar().setValue(5000 + difference2);
					gui.getSignInWindow().getProgressBar().update(gui.getSignInWindow().getProgressBar().getGraphics());

					if (difference2 > 5000)
						timeElapsed2 = true;

				}

				gui.getSignInWindow().setVisible(false);

				refreshMemberList();
				gui.getListWindow().setTitle(gui.getSignInWindow().getTextFieldUsername().getText());

				gui.getListWindow().setVisible(true);

			} catch (Exception c) {
				System.exit(0);
			}

		}

		if (e.getActionCommand().equals("Refresh")) {
			refreshMemberList();
		}

		if (e.getActionCommand().equals("Chat")) {

			String selection = gui.getListWindow().getListOnlineMembers().getSelectedValue();

			if (selection == null)
				JOptionPane.showMessageDialog(null, "Choose a member to chat with.");
			else
				addChatWindow(selection);

		}

		if (e.getActionCommand().equals("Quit")) {

			clientTest.getClient().quit();
			System.exit(0);

		}

		if (e.getActionCommand().equals("Send")) {

			JButton b = (JButton) e.getSource();
			int i = Integer.parseInt(b.getToolTipText());

			clientTest.getClient().chat(gui.getChatWindows().get(i).getDestinationName(),
					gui.getChatWindows().get(i).getTextAreaToSend().getText());

			gui.getChatWindows().get(i).getTextAreaChat()
					.append("You: " + gui.getChatWindows().get(i).getTextAreaToSend().getText() + "\n\n");
			gui.getChatWindows().get(i).getTextAreaToSend().setText("");

		}

	}

	public void addActionListeners() {

		gui.getSignInWindow().getButtonSignIn().addActionListener(this);
		gui.getListWindow().getButtonRefreshMemberList().addActionListener(this);
		gui.getListWindow().getButtonChat().addActionListener(this);
		gui.getListWindow().getButtonQuit().addActionListener(this);

		gui.getSignInWindow().addWindowListener(this);
		gui.getListWindow().addWindowListener(this);

	}

	private void refreshMemberList() {

		gui.getListWindow().getListOnlineMembersModel().clear();

		for (int i = 0; i < clientTest.getClient().getGlobalMemberList().size(); i++)
			if (clientTest.getClient().getGlobalMemberList().get(i) != null
					&& !clientTest.getClient().getGlobalMemberList().get(i).equals("ClientConnectedNotJoined")
					&& !clientTest.getClient().getGlobalMemberList().get(i).equals("ClientLoggedOff"))
				gui.getListWindow().getListOnlineMembersModel()
						.addElement(clientTest.getClient().getGlobalMemberList().get(i));

	}

	public void addChatWindow(String destinationName) {

		int index = clientTest.getClient().getGlobalMemberList().indexOf(destinationName);

		Window3 chatWindow = new Window3(destinationName, index);
		gui.getChatWindows().set(index, chatWindow);

		gui.getChatWindows().get(index).getButtonSend().addActionListener(this);

		chatWindow.setVisible(true);

	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowClosing(WindowEvent arg0) {

		if (arg0.getSource() instanceof Window1)
			System.exit(0);

		if (arg0.getSource() instanceof Window2)
			clientTest.getClient().quit();

	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

}
