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
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

/**
 * This is the class Server that runs a multi-threading server to communicate with multiple clients via TCP socket
 * @author GuoYu Cao
 * @version 1.0
 * @since 1.0
 */
public class Server {

	private JFrame frame;
	private JTextArea contentArea;
	private JButton btn_start;
	private JPanel northPanel;
	private JPanel southPanel;
	private JScrollPane rightPanel;
	private JScrollPane leftPanel;
	private JSplitPane centerSplit;
	private ServerSocket serverSocket;
	private ServerThread serverThread;
	private ArrayList<ClientThread> clients;
	private boolean isStart = false;

	/**
	 * This is the main entrance of the server class
	 * 
	 * @param args The default parameter of the main method, currently it is empty
	 */
	public static void main(String[] args) {
		new Server();
	}

	/**
	 * This is the constructor of Server class, it create the UI frame and
	 * initialize the TCP server
	 */
	public Server() {
		frame = new JFrame("Chat Server");
		contentArea = new JTextArea();
		contentArea.setEditable(false);
		contentArea.setForeground(Color.blue);
		btn_start = new JButton("Start Server");

		southPanel = new JPanel(new BorderLayout());
		southPanel.setBorder(new TitledBorder(""));
		rightPanel = new JScrollPane(contentArea);
		rightPanel.setBorder(new TitledBorder("Messages:"));

		centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
		centerSplit.setDividerLocation(100);
		northPanel = new JPanel();
		northPanel.setLayout(new GridLayout(1, 6));
		northPanel.add(btn_start);

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
		 * This the eventListener that listening to the frame. If the frame is closed,
		 * then this method will be triggered to close the server and exit
		 */
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (isStart) {
					closeServer();// close the server thread
				}
				System.exit(0);// exit the application
			}
		});

		/**
		 * This the eventListener that listening to the btn_start button. When the
		 * btn_start button get clicked, it this method calls StartUpServer method to
		 * starts a new thread to hold the server
		 */
		btn_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isStart) {
					JOptionPane.showMessageDialog(frame, "Server has already started!", "Error",
							JOptionPane.ERROR_MESSAGE);

				} else {
					int max = 10;
					int port = 5000;
					try {
						StartUpServer(max, port);
						contentArea.append("Server started! Max people can join： " + max + ", Port：" + port + "\r\n");
						JOptionPane.showMessageDialog(frame, "Server Started!");
						btn_start.setEnabled(false);

					} catch (Exception exc) {
						JOptionPane.showMessageDialog(frame, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
	}

	/**
	 * This is the method that starts a new thread of the server to keep accepting
	 * the clients. It needs to be in a thread so that the socket.accept() will not
	 * block the entire program.
	 * 
	 * @param max  The max number of clients that could join this server
	 * @param port The port number of the server socket.
	 * @throws java.net.BindException If the binding of server socket and port
	 *                                failed, then this exception will be thrown.
	 */
	public void StartUpServer(int max, int port) throws java.net.BindException {
		try {
			clients = new ArrayList<ClientThread>(); // Initialize the clients array
			serverSocket = new ServerSocket(port); // create new socket
			serverThread = new ServerThread(serverSocket, max); // use a thread to accept clients
			serverThread.start(); // start the thread
			isStart = true; // shows server is started
		} catch (BindException e) {
			isStart = false;
			throw new BindException("Port is occupied!Please change the Port Number!");
		} catch (Exception e1) {
			e1.printStackTrace();
			isStart = false;
			throw new BindException("Server start failed!");
		}
	}

	/**
	 * This is the method that close the server. It stops the thread and close the
	 * socket.
	 */
	@SuppressWarnings("deprecation")
	public void closeServer() {
		try {
			if (serverThread != null) {
				serverThread.stop();// stop the thread
			}

			if (serverSocket != null) {
				serverSocket.close();// close the socket
			}
			isStart = false;
		} catch (IOException e) {
			e.printStackTrace();
			isStart = true;
		}
	}

	/**
	 * This the thread class of ServerThread. It is a thread that keep accepting the
	 * clients' connections.
	 * 
	 * @author GuoYu Cao
	 *
	 */
	class ServerThread extends Thread {
		private ServerSocket serverSocket;
		private int max;// max clients number

		/**
		 * This is the constructor
		 * @param serverSocket the socket of the server
		 * @param max the max number of client can join
		 */
		public ServerThread(ServerSocket serverSocket, int max) {
			this.serverSocket = serverSocket;
			this.max = max;
		}

		/**
		 * This is the run method of the thread. It create a new client thread to handle the receiving and sending messages.
		 */
		public void run() {
			while (true) {// keep accepting
				try {
					if (clients.size() <= max) {
						Socket socket = serverSocket.accept();
						ClientThread client = new ClientThread(socket); // create a new client thread to handle the
																		// receiving and sending
						client.start();// start the client thread
						clients.add(client); // add this newly created client to the clients arrayList
						contentArea.append(
								client.getUser().getName() + " / "+ client.getUser().getIp() + " joined the chat room!\r\n"); // update
																														// the																								// chat
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * This is the thread class of Client thread. It keeps receiving messages from
	 * the clients and broadcast the received message to all the people in the chat
	 * room.
	 * 
	 * @author GuoYu Cao
	 *
	 */
	class ClientThread extends Thread {
		private Socket socket;
		private BufferedReader reader;
		private PrintWriter writer;
		private User user;

		/**
		 * This is the method to get the BufferedReader of the client
		 * 
		 * @return reader It returns the BufferedReader reader.
		 */
		public BufferedReader getReader() {
			return reader;
		}

		/**
		 * This is the method to get the PrintWriter of the client
		 * 
		 * @return writer It returns the PrintWriter writer
		 */
		public PrintWriter getWriter() {
			return writer;
		}

		/**
		 * This is the method to get the User of the client
		 * 
		 * @return user It returns the User user
		 */
		public User getUser() {
			return user;
		}

		/**
		 * This is the constructor of client thread
		 * 
		 * @param socket
		 */
		public ClientThread(Socket socket) {
			try {
				this.socket = socket; // set the socket to the accepted socket in the startUp method
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream())); // setup the reader to
																								// receive message
				writer = new PrintWriter(socket.getOutputStream(), true); // setup the write to send message. Use "true"
																			// to setup auto flush

				String str = reader.readLine(); // receive message

				StringTokenizer st = new StringTokenizer(str, "@"); // StringTokenizer is kind like String split, it
																	// seperates the string by the delimiter
				user = new User(st.nextToken(), st.nextToken());
				// response the the client
				writer.println(user.getName() + user.getIp() + "Successful Connected!");
				writer.flush();

				// display the current users online
				if (clients.size() > 0) {
					String temp = "";
					for (int i = clients.size() - 1; i >= 0; i--) {
						temp += (clients.get(i).getUser().getName() + "/" + clients.get(i).getUser().getIp()) + "@";
					}
					writer.println("Online People Number: " + clients.size() + " People: " + temp);
					writer.flush();
				}

				// notice all the user that the new user joined the chat room
				for (int i = clients.size() - 1; i >= 0; i--) {
					clients.get(i).getWriter().println("New person joined: " + user.getName() + user.getIp());
					clients.get(i).getWriter().flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * This the run method the client thread/ It keep receicing the messages from
		 * the clients
		 */
		@SuppressWarnings("deprecation")
		public void run() {// keep receiving messages
			String message = "";
			while (true) {
				try {
					message = reader.readLine();// receive message

					if (message.equals("CLOSE"))// if the client closed, then the server will receive CLOSE, then close
												// the socket and thread
					{
						contentArea
								.append(this.getUser().getName() + this.getUser().getIp() + " left the chat room!\r\n");

						// close the resources
						reader.close();
						writer.close();
						socket.close();

						// notice all the users that the user has left the chat room
						for (int i = clients.size() - 1; i >= 0; i--) {
							clients.get(i).getWriter().println("Person left the chat room: " + user.getName());
							clients.get(i).getWriter().flush();
						}

						// stop the thread for the leaving user
						for (int i = clients.size() - 1; i >= 0; i--) {
							if (clients.get(i).getUser() == user) {
								ClientThread temp = clients.get(i); // get the current thread
								clients.remove(i);// remove from the clients list
								temp.stop();// stop the current thread
								return;
							}
						}
					} else {
						broadcastMessage(message);// Broadcast the received message to all the online users
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * Broadcast the received message to all the online users
		 * 
		 * @param message The message that the client thread received from the user
		 */
		public void broadcastMessage(String message) {
			StringTokenizer token = new StringTokenizer(message, "@");
			String name = token.nextToken();
			String mesg = token.nextToken();
			message = name + " ： " + mesg;
			contentArea.append(message + "\r\n"); // shows on the server frame

			for (int i = clients.size() - 1; i >= 0; i--) {
				clients.get(i).getWriter().println(message); // send message to other users
				clients.get(i).getWriter().flush();
			}

		}
	}
}