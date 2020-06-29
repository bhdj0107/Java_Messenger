import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import java.net.*;
public class Main extends JFrame {
	Socket sock;
	boolean accepted = false;
	// jf1 상대의 메시지, jf2 나의 입력
	JTextField jf1 = new JTextField();
	JTextField jf2 = new JTextField();
	DataOutputStream osDataStream = null;
	Main() {
		setTitle("메신저 프로그램");
		setSize(450,540);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel pn = new JPanel();
		setContentPane(pn);
		pn.setLayout(null);

		// Mode 설정
		JButton server = new JButton("서버 열기");
		JLabel ip = new JLabel("IP주소 : ");
		JTextField ipAddress = new JTextField();
		JButton ipButton = new JButton("연결");
		JLabel Mode = new JLabel();
		Mode.setBounds(15,5,400,20);
		JPanel modeset = new JPanel();
		modeset.setBounds(0,5,430,20);
		modeset.setVisible(false);
		
		modeset.add(Mode);
		pn.add(modeset);
		
		server.setBounds(20,5,100,20);
		server.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// 프로그램 Title 변경 후, Server 메소드 실행
					InetAddress Address = InetAddress.getLocalHost();
					setTitle("메신저 프로그램 / 서버 Mode / " + Address.toString().split("/")[1]);
					server();
					server.setVisible(false);
					ip.setVisible(false);
					ipAddress.setVisible(false);
					ipButton.setVisible(false);
					Mode.setText("Server 모드로 설정되었습니다.");
					modeset.setVisible(true);
					
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
			}
		});
		pn.add(server);
		ip.setBounds(190,5, 100, 20);
		pn.add(ip);
		ipAddress.setBounds(240,5, 100, 20);
		pn.add(ipAddress);
		
		ipButton.setBounds(350,5,60,20);
		ipButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					InetAddress Address = InetAddress.getLocalHost();
					setTitle("메신저 프로그램 / 클라 Mode / " + Address.toString().split("/")[1]);
					client(ipAddress.getText());
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
			}
		});
		pn.add(ipButton);
		
		JLabel jl1 = new JLabel("상대방의 채팅");
		JLabel portlabel = new JLabel("PORT : 5095");
		JLabel jl2 = new JLabel("나의 채팅");
		jl1.setBounds(20,30, 100, 20);
		jl2.setBounds(20,255, 100,20);
		portlabel.setBounds(300,30, 100,20);
		
		jf1 = new JTextField("상대의 메세지가 작성될 칸 입니다.");
		jf2 = new JTextField();
		jf2.addActionListener(new TextActionHandler());
		jf1.setBounds(15,50, 400, 205);
		jf2.setBounds(15,275, 400,205);
		jf1.setEditable(false);
		jf1.setBackground(Color.WHITE);
		
		pn.add(portlabel);
		pn.add(jl1);
		pn.add(jl2);
		pn.add(jf1);
		pn.add(jf2);
		setVisible(true);
		
		
	}
	public void server() {
		new ServerSockThread(this).start();
		new ReceiveDataThread(this).start();
	}
	public void client(String serverName) {
		new ClientSockThread(this, serverName).start();
		new ReceiveDataThread(this).start();
	}
	class TextActionHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (osDataStream == null)
				return;
			try {
				osDataStream.writeUTF(jf2.getText());
				jf2.setText("");
			} catch (IOException x) {
				
			}
		} 
	}
	public void addRecvString(String str) {
		jf1.setText(str);
	}
	public void finalize() throws Throwable {
		try {
			if (osDataStream != null)
				osDataStream.close();
			if (sock != null)
				sock.close();
		} catch (IOException x) {
		}
		super.finalize();
	}
	public static void main(String args[]) throws UnknownHostException {
		new Main();
	}
}
class ServerSockThread extends Thread {
	private Main chat;
	private ServerSocket serverSock = null;
	private Socket sock = null;
	ServerSockThread(Main chat) {
		this.chat = chat;
	}
	public void run() {
		try {
			InetAddress serverAddr = InetAddress.getByName(null);
			serverSock = new ServerSocket(5095, 1);
			chat.sock = serverSock.accept();
			chat.jf1.setText("연결됨");
			chat.osDataStream = new DataOutputStream(chat.sock.getOutputStream());
			chat.accepted = true;
		} catch (IOException e) {
		} finally {
			if (serverSock != null) {
				try {
					serverSock.close();
				} catch (IOException x) {
				}
			}
		}
	}
}
class ClientSockThread extends Thread {
	private Main chat;
	private ServerSocket serverSock = null;
	private Socket sock = null;
	private String serverName;
	ClientSockThread(Main chat, String serverName) {
		this.chat = chat;
		this.serverName = serverName;
	}
	public void run() {
		try {
			if (serverName.equals("localhost")) 
				serverName = null;
			InetAddress serverAddr = InetAddress.getByName(serverName);
			chat.sock = new Socket(serverAddr.getHostName(), 5095);
			chat.jf1.setText("연결됨");
			chat.osDataStream = new DataOutputStream(chat.sock.getOutputStream());
			chat.accepted = true;
		} catch (IOException e) {
			
		}
	}
	public Socket sock() {
		return sock;
	}
}
class ReceiveDataThread extends Thread {
	private Main chat;
	private DataInputStream isDataStream;
	private boolean bWaitting = true;
	public ReceiveDataThread(Main chat) {
		this.chat = chat;
	}
	public synchronized void run() {
		String str;
		while(true) {
			if(chat.accepted) break;
			try {
				sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			isDataStream = new DataInputStream(chat.sock.getInputStream());
			while (bWaitting) {
				str = isDataStream.readUTF();
				chat.addRecvString(str);
			}
		} catch (IOException e) {
			
		} finally {
			try {
				if (isDataStream != null) {
					isDataStream.close();
				}
			} catch  (IOException e) {
			}
		}
	}
}
