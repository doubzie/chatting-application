package GUI;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

public class Window3 extends JFrame {

	private static final long serialVersionUID = 1L;

	private JTextArea textAreaChat;
	private JTextArea textAreaToSend;
	private JButton buttonSend;
	private String destinationName;
	private int index;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Window3 window = new Window3("", 0);
					window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Window3(String destinationName, int index) {

		this.destinationName = destinationName;
		this.index = index;

		setBounds(100, 100, 700, 500);
		setTitle("Chat with " + destinationName);
		getContentPane().setLayout(null);
		setIconImage(new ImageIcon(this.getClass().getResource("/Images/bubbleImage.png")).getImage());
		setLocation(1100, 300);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		textAreaChat = new JTextArea();
		textAreaChat.setBounds(12, 36, 676, 344);
		textAreaChat.setWrapStyleWord(true);
		textAreaChat.setLineWrap(true);
		textAreaChat.setEditable(false);
		getContentPane().add(textAreaChat);

		textAreaToSend = new JTextArea();
		textAreaToSend.setBounds(12, 406, 558, 56);
		textAreaToSend.setWrapStyleWord(true);
		textAreaToSend.setLineWrap(true);
		getContentPane().add(textAreaToSend);

		buttonSend = new JButton("Send");
		buttonSend.setBounds(582, 404, 106, 58);
		buttonSend.setToolTipText("" + index);
		buttonSend.setBackground(Color.LIGHT_GRAY);
		getContentPane().add(buttonSend);

		JSeparator separator = new JSeparator();
		separator.setBounds(12, 392, 676, 2);
		getContentPane().add(separator);

		getRootPane().setDefaultButton(buttonSend);

	}

	public JTextArea getTextAreaChat() {
		return textAreaChat;
	}

	public JTextArea getTextAreaToSend() {
		return textAreaToSend;
	}

	public JButton getButtonSend() {
		return buttonSend;
	}

	public String getDestinationName() {
		return destinationName;
	}

}
