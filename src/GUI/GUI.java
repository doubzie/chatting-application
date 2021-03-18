package GUI;

import java.util.ArrayList;

public class GUI {

	private Window1 signInWindow;
	private Window2 listWindow;
	private ArrayList<Window3> chatWindows;

	public GUI() {

		signInWindow = new Window1();
		listWindow = new Window2();
		chatWindows = new ArrayList<Window3>();

		for (int i = 0; i < 100; i++)
			chatWindows.add(null);

		signInWindow.setVisible(true);

	}

	public Window1 getSignInWindow() {
		return signInWindow;
	}

	public Window2 getListWindow() {
		return listWindow;
	}

	public ArrayList<Window3> getChatWindows() {
		return chatWindows;
	}

}
