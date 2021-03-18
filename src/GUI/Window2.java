package GUI;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;

public class Window2 extends JFrame {

	private static final long serialVersionUID = 1L;

	private JList<String> listOnlineMembers;
	private DefaultListModel<String> listOnlineMembersModel;
	private JButton buttonChat;
	private JButton buttonRefreshMemberList;
	private JButton buttonQuit;
	private JLabel labelCurrentTime;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Window2 window = new Window2();
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
	public Window2() {

		setBounds(100, 100, 350, 500);

		setTitle("Online");
		getContentPane().setLayout(null);
		setIconImage(new ImageIcon(this.getClass().getResource("/Images/bubbleImage.png")).getImage());
		setLocation(715, 300);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		listOnlineMembersModel = new DefaultListModel<>();
		listOnlineMembers = new JList<>(listOnlineMembersModel);
		listOnlineMembers.setBounds(32, 50, 281, 306);
		getContentPane().add(listOnlineMembers);

		buttonChat = new JButton("Chat");
		buttonChat.setBounds(52, 393, 123, 55);
		buttonChat.setBackground(Color.LIGHT_GRAY);
		getContentPane().add(buttonChat);

		buttonRefreshMemberList = new JButton("Refresh");
		buttonRefreshMemberList.setBounds(209, 377, 104, 25);
		buttonRefreshMemberList.setBackground(Color.LIGHT_GRAY);
		getContentPane().add(buttonRefreshMemberList);

		buttonQuit = new JButton("Quit");
		buttonQuit.setBounds(209, 435, 104, 25);
		buttonQuit.setBackground(Color.LIGHT_GRAY);
		getContentPane().add(buttonQuit);

		labelCurrentTime = new JLabel("Time");
		labelCurrentTime.setFont(new Font("Century Schoolbook L", Font.BOLD, 15));
		labelCurrentTime.setHorizontalAlignment(SwingConstants.CENTER);
		labelCurrentTime.setBounds(90, 23, 160, 15);
		getContentPane().add(labelCurrentTime);

		getRootPane().setDefaultButton(buttonChat);

		TimerThread timer = new TimerThread();
		Thread timerThread = new Thread(timer);
		timerThread.setName("timerThread");
		timerThread.start();

	}

	// Class 2

	class TimerThread implements Runnable {

		Calendar cal;
		SimpleDateFormat sdf;

		public TimerThread() {

		}

		@Override
		public void run() {

			while (true) {

				cal = Calendar.getInstance();
				sdf = new SimpleDateFormat("hh:mm:ss a");

				labelCurrentTime.setText(sdf.format(cal.getTime()));

			}

		}

	}

	public JList<String> getListOnlineMembers() {
		return listOnlineMembers;
	}

	public DefaultListModel<String> getListOnlineMembersModel() {
		return listOnlineMembersModel;
	}

	public JButton getButtonChat() {
		return buttonChat;
	}

	public JButton getButtonRefreshMemberList() {
		return buttonRefreshMemberList;
	}

	public JButton getButtonQuit() {
		return buttonQuit;
	}

	public JLabel getLabelCurrentTime() {
		return labelCurrentTime;
	}

}
