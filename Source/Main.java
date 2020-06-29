import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import java.net.*;
public class Main extends JFrame {
	Socket sock;
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
		
		// UI 배치
		JButton server = new JButton("서버 열기");
		server.setBounds(20,5,100,20);
		server.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					InetAddress Address = InetAddress.getLocalHost();
					setTitle("메신저 프로그램 / 서버 Mode / " + Address.toString().split("/")[1]);
					server();
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
			}
		});
		pn.add(server);
		JLabel ip = new JLabel("IP주소 : ");
		ip.setBounds(190,5, 100, 20);
		pn.add(ip);
		
		JTextField ipAddress = new JTextField();
		ipAddress.setBounds(240,5, 100, 20);
		pn.add(ipAddress);
		
		JButton ipButton = new JButton("연결");
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
		ServerSocket serverSock = null;
		try {
			InetAddress serverAddr = InetAddress.getByName(null);
			serverSock = new ServerSocket(5095, 1);
			sock = serverSock.accept();
			jf1.setText("연결됨");
			osDataStream = new DataOutputStream(sock.getOutputStream());
			new ReceiveDataThread(this).start();
		} catch (IOException e) {		} finally {
			if (serverSock != null) {
				try {
					serverSock.close();
				} catch (IOException x) {
				}
			}
		}
	}
	public void client(String serverName) {
		try {
			if (serverName.equals("localhost")) 
				serverName = null;
			InetAddress serverAddr = InetAddress.getByName(serverName);
			sock = new Socket(serverAddr.getHostName(), 5095);
			jf1.setText("연결됨");
			osDataStream = new DataOutputStream(sock.getOutputStream());
			new ReceiveDataThread(this).start();
		} catch (IOException e) {
			
		}
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
class ReceiveDataThread extends Thread {
	private Main chat;
	private DataInputStream isDataStream;
	private boolean bWaitting = true;
	public ReceiveDataThread(Main chat) {
		this.chat = chat;
	}
	public synchronized void run() {
		String str;
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
