package Listeners;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import Engine.ClientTest;
import GUI.GUI;

public class Main {

	public Main() {

		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {

		}

		GUI gui = new GUI();
		ClientTest clientTest = new ClientTest(gui);
		new Controller(clientTest, gui);

	}

	public static void main(String[] args) {

		new Main();

	}

}