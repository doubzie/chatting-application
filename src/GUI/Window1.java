package GUI;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

public class Window1 extends JFrame {

	private static final long serialVersionUID = 1L;

	private JTextField textFieldUsername;
	private JSpinner spinnerServer;
	private JButton buttonSignIn;
	private JProgressBar progressBar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Window1 window = new Window1();
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
	public Window1() {

		setBounds(100, 100, 350, 500);
		setTitle("Sign in");
		getContentPane().setLayout(null);
		setIconImage(new ImageIcon(this.getClass().getResource("/Images/bubbleImage.png")).getImage());
		setLocation(715, 300);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JLabel bubbleImage = new JLabel(new ImageIcon(this.getClass().getResource("/Images/bubbleImage.png")));
		bubbleImage.setBounds(36, 25, 275, 206);
		getContentPane().add(bubbleImage);

		JLabel lblUsername = new JLabel("Username");
		lblUsername.setHorizontalAlignment(SwingConstants.CENTER);
		lblUsername.setBounds(124, 259, 94, 23);
		getContentPane().add(lblUsername);

		textFieldUsername = new JTextField();
		textFieldUsername.setBounds(113, 281, 114, 25);
		getContentPane().add(textFieldUsername);
		textFieldUsername.setColumns(10);

		spinnerServer = new JSpinner();
		spinnerServer.setModel(new SpinnerNumberModel(1, 1, 4, 1));
		spinnerServer.setBounds(184, 352, 45, 20);
		getContentPane().add(spinnerServer);

		JLabel lblServer = new JLabel("Server");
		lblServer.setBounds(113, 354, 53, 15);
		getContentPane().add(lblServer);

		buttonSignIn = new JButton("Sign in");
		buttonSignIn.setBounds(113, 405, 117, 25);
		buttonSignIn.setBackground(Color.LIGHT_GRAY);
		getContentPane().add(buttonSignIn);

		getRootPane().setDefaultButton(buttonSignIn);

		progressBar = new JProgressBar(0, 10000);
		progressBar.setBounds(71, 442, 208, 14);
		getContentPane().add(progressBar);

	}

	/*
	 * Label with background class
	 */
	class JLabelWithIcon extends JLabel {

		private static final long serialVersionUID = 1L;

		public JLabelWithIcon(String path) {
			super(new ImageIcon(path));
		}

	}

	public JTextField getTextFieldUsername() {
		return textFieldUsername;
	}

	public JSpinner getSpinnerServer() {
		return spinnerServer;
	}

	public JButton getButtonSignIn() {
		return buttonSignIn;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}
}
