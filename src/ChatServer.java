import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;

//import com.sun.media.sound.WaveExtensibleFileReader;

import java.awt.BorderLayout;

public class ChatServer {

	private JFrame frmChatServer;

	/**
	 * Launch the application.
	 */
	static private JList<String> listClients;
	static private HashMap<String, Socket> nameClientSocket = new HashMap<>();
	static private HashMap<String, String> chatConnections =new HashMap<>();
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			Scanner sc = new Scanner(System.in);
		
			public void run() {
				try {
					ServerSocket server = new ServerSocket(5555);
					while (true) {
						Socket socket = server.accept();
						System.out.println("A connection established.");
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								try {
									// cmd:[online;my name]
									BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
									String cmd = reader.readLine();
									String clientName=executeCommand(cmd,null,socket);					
									

									DefaultListModel<String> listModel = new DefaultListModel();
									listModel.addAll(nameClientSocket.keySet());
									listClients.setModel(listModel);
									
									// cmd:[onlines;[name1;name2;name3]]
									BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
									writer.write("cmd:[onlines;[");
									for (String key: nameClientSocket.keySet()) {
										writer.write(key+";");
									}
									writer.write("]]");
									writer.newLine();
									
									
									while(true) {
										// cmd:[connect;name to connect]
										// cmd:[disconnect;name to disconnect]
										// cmd:[offline]
										String message = reader.readLine();
										
										String result = executeCommand(message,clientName,socket);
										if (result.equals("NOTCMD")) {
											String friend = chatConnections.get(clientName);
											Socket friendSocket = nameClientSocket.get(friend);
											BufferedWriter friendWriter= new BufferedWriter(new OutputStreamWriter(friendSocket.getOutputStream()));
											friendWriter.write(message);
											friendWriter.newLine();
										} else if (result.equals("OFFLINE")) {
											socket.close();
										}
										
									}
 
								} catch (Exception e) {
									System.out.println("Client connection error");
									e.printStackTrace();
								}
								finally {
									socket.close();
								}
							}

							
						});
					}
				}
				catch (Exception e) {
					System.out.println("Socket server error:");
					e.printStackTrace();
				}
			}
		});
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatServer window = new ChatServer();
					window.frmChatServer.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ChatServer() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmChatServer = new JFrame();
		frmChatServer.setTitle("Chat Server");
		frmChatServer.setBounds(100, 100, 450, 300);
		frmChatServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		listClients = new JList<String>();
		frmChatServer.getContentPane().add(listClients, BorderLayout.CENTER);
		
	}
	
	static private String executeCommand(String cmd, String clientName, Socket clientSocket) {
		// parse the command
		if (cmd.length()<4) return "NOTCMD";
		String part = cmd.substring(0,4);
		if (!part.equals("cmd:")) return "NOTCOMD";
		part = cmd.substring(4);
		if (part.charAt(0)!='[' || part.charAt(part.length()-1)!=']')
			return "NOTCMD";
		part = part.substring(1,part.length()-1);
		if (part.equals("offline")) {
			String friend = part.substring(part.indexOf(";"));
			chatConnections.put(clientName, friend);
			chatConnections.put(friend, clientName);
			nameClientSocket.remove(clientName);
			return "OFFLINE";
		}
		
		String cmdName=part.substring(0,part.indexOf(";"));
		if (cmdName.equals("online")) {
			String name = part.substring(part.indexOf(";")+1);
			nameClientSocket.put(name, clientSocket);
			return name;
			
		} else if (cmdName.equals("connect")) {
			String friend = part.substring(part.indexOf(";"));
			chatConnections.put(clientName, friend);
			chatConnections.put(friend, clientName);
			return "SUCCESS";
		} else if (cmdName.equals("disconnect")) {
			String friend = chatConnections.get(clientName);
			chatConnections.remove(clientName);
			chatConnections.remove(friend);
			return "SUCCESS";
		}
		
		return "FAIL";
	}

}
