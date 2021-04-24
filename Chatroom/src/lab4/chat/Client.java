/**********************************************
Lab 4
Course:<BTP 400> - Semester 4
Last Name:<Cao>
First Name:<GuoYu>
ID:<061341145>
Section:<NAA>
This assignment represents my own work in accordance with Seneca Academic Policy.
Signature GuoYu Cao
Date:<2021-April-09>
**********************************************/
package lab4.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

/**
 * This is the class Client that runs a multi-threading client to communicate
 * with the server via TCP socket
 * 
 * @author GuoYu Cao
 * @version 1.0
 * @since 1.0
 */
public class Client {

	private JFrame frame;
	private JTextArea textArea;
	private JTextField textField;
	private JTextField txt_name;
	private JButton btn_start;
	private JButton btn_send;
	private JPanel northPanel;
	private JPanel southPanel;
	private JScrollPane rightScroll;
	private JScrollPane leftScroll;
	private JSplitPane centerSplit;
	private boolean isConnected = false;

	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	private ReceivingThread recvMessage;// receive message

	/**
	 * This is the main entrance of the Client class
	 * 
	 * @param args The default parameter of the main method, currently it is empty
	 */
	public static void main(String[] args) {
		new Client();
	}

	/**
	 * This is the method that get the text from the textField then send the text to
	 * the server and reset the textFeild to empty
	 */
	public void send() {
		if (!isConnected) {
			JOptionPane.showMessageDialog(frame, "Not connected to server!", "Error", JOptionPane.ERROR_MESSAGE);
		} else {
			String message = textField.getText().trim();
			if (message == null || message.equals("")) {
				JOptionPane.showMessageDialog(frame, "Message Cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
			} else {
				sendMessage(frame.getTitle() + "@" + message); // send the message in name@content style, so that the
																// server can know who is speaking
				textField.setText(null);

			}
		}
	}

	/**
	 * This is the constructor of Client class.
	 */
	public Client() {
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setForeground(Color.blue);
		textField = new JTextField();
		txt_name = new JTextField("");
		btn_start = new JButton("Connect");
		btn_send = new JButton("Send");
		northPanel = new JPanel();
		northPanel.setLayout(new GridLayout(1, 7));
		northPanel.add(new JLabel("Name: "));
		northPanel.add(txt_name);
		northPanel.add(btn_start);
		rightScroll = new JScrollPane(textArea);
		rightScroll.setBorder(new TitledBorder("Chat"));
		southPanel = new JPanel(new BorderLayout());
		southPanel.add(textField, "Center");
		southPanel.add(btn_send, "East");
		southPanel.setBorder(new TitledBorder("Write Message:"));
		centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
		centerSplit.setDividerLocation(100);

		frame = new JFrame("Chat Room Client");

		frame.setLayout(new BorderLayout());
		frame.add(northPanel, "North");
		frame.add(centerSplit, "Center");
		frame.add(southPanel, "South");
		frame.setSize(700, 400);
		int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
		frame.setLocation((screen_width - frame.getWidth()) / 2, (screen_height - frame.getHeight()) / 2);
		frame.setVisible(true);

		/**
		 * This is the eventListener that listening to the send button btn_send. When
		 * the button is clicked, the method will be triggered to send the message.
		 */
		btn_send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});

		/**
		 * This is the eventListener that listening to the start button btn_start. When
		 * the button is clicked, this method will be triggered to start the connection
		 * to the server.
		 */
		btn_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int port = 5000;
				String hostIp = "localhost";
				if (!isConnected) {
					try {
						String name = txt_name.getText().trim();
						if (name.equals("") || hostIp.equals("")) {
							throw new Exception("Name cannot be empty!");
						}
						boolean flag = connectServer(port, hostIp, name);
						if (flag == false) {
							throw new Exception("Failed to connect server!");
						}
						frame.setTitle(name);
						JOptionPane.showMessageDialog(frame, "Connection Succeed!");
						btn_start.setEnabled(false);
					} catch (Exception exc) {
						JOptionPane.showMessageDialog(frame, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}

				}
			}
		});

		/**
		 * This the eventListener that listening to the frame. If the frame is closed,
		 * then this method will be triggered to close the client and notice the server
		 * and other users that this user left the chat
		 */
		frame.addWindowListener(new WindowAdapter() {
			@SuppressWarnings("deprecation")
			public void windowClosing(WindowEvent e) {
				if (isConnected) {
					try {
						sendMessage("CLOSE");// send CLOSE message to the server, so that the server will stop this
												// thread in the server side
						recvMessage.stop();// stop the receive message thread of the client
						// close resource
						if (reader != null) {
							reader.close();
						}
						if (writer != null) {
							writer.close();
						}
						if (socket != null) {
							socket.close();
						}
						isConnected = false;

					} catch (IOException e1) {
						e1.printStackTrace();
						isConnected = true;

					}
				}
				System.exit(0);// exit the application
			}
		});
	}

	/**
	 * This is the method to connect to the server
	 * 
	 * @param port   the port number of the server
	 * @param hostIp the IP of the server
	 * @param name   the name of the user
	 * @return boolean It returns true when the connection is successful and returns false when the connection failed
	 */
	public boolean connectServer(int port, String hostIp, String name) {
		try {
			socket = new Socket(hostIp, port);// setup the socket connection by the IP and port
			writer = new PrintWriter(socket.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// send the user name and IP information
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			String info = " address " + socket.getLocalAddress().toString() + " port: " + socket.getPort()
					+ ", localport: " + socket.getLocalPort() + " , connected time: " + dtf.format(now);

			sendMessage(name + "@" + info);

			// create new thread to keep receiving from the server
			recvMessage = new ReceivingThread(reader, textArea);
			recvMessage.start();
			isConnected = true;// shows the connection is setup
			return true;
		} catch (Exception e) {
			textArea.append("To the Port：" + port + "    IP：" + hostIp + "  connection failed!" + "\r\n");
			isConnected = false;
			return false;
		}
	}

	/**
	 * This is the method to send message to the server
	 * 
	 * @param message The message that user entered
	 */
	public void sendMessage(String message) {
		writer.println(message);
		writer.flush();
	}

	/**
	 * This is the thread that keep receiving message from the server.
	 * 
	 * @author GuoYu Cao
	 *
	 */
	class ReceivingThread extends Thread {
		private BufferedReader reader;
		private JTextArea textArea;

		/**
		 * This is the constructor
		 * 
		 * @param reader   this is the BufferedReader that receives message
		 * @param textArea this is the JTextArea that displays the message on the screen
		 */
		public ReceivingThread(BufferedReader reader, JTextArea textArea) {
			this.reader = reader;
			this.textArea = textArea;
		}

		/**
		 * This is the run method of the thread
		 */
		public void run() {
			String message = "";
			while (true) {
				try {
					message = reader.readLine();
					textArea.append(message + "\r\n");
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}