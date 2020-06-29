import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import java.net.*;
public class Main extends JFrame {
	Socket sock;
	boolean accepted = false;
	// jf1 ����� �޽���, jf2 ���� �Է�
	JTextField jf1 = new JTextField();
	JTextField jf2 = new JTextField();
	DataOutputStream osDataStream = null;
	Main() {
		setTitle("�޽��� ���α׷�");
		setSize(450,540);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel pn = new JPanel();
		setContentPane(pn);
		pn.setLayout(null);

		// Mode ���� ��ư ��ġ
		JButton server = new JButton("���� ����");
		JLabel ip = new JLabel("IP�ּ� : ");
		JTextField ipAddress = new JTextField();
		JButton ipButton = new JButton("����");
		JLabel Mode = new JLabel();
		JLabel jl1 = new JLabel("������ ä��");
		JLabel portlabel = new JLabel("PORT : 5095");
		JLabel jl2 = new JLabel("���� ä��");
		jf1 = new JTextField("����� �޼����� �ۼ��� ĭ �Դϴ�.");
		jf2 = new JTextField();
		
		server.setBounds(20,5,100,20);
		ip.setBounds(190,5, 100, 20);
		ipAddress.setBounds(240,5, 100, 20);
		ipButton.setBounds(350,5,60,20);
		jl1.setBounds(20,30, 100, 20);
		jl2.setBounds(20,255, 100,20);
		portlabel.setBounds(300,30, 100,20);
		jf1.setBounds(15,50, 400, 205);
		jf2.setBounds(15,275, 400,205);

		jf1.setEditable(false);
		jf1.setBackground(Color.WHITE);
		
		Mode.setBounds(15,5,400,20);
		JPanel modeset = new JPanel();
		modeset.setBounds(0,5,430,20);
		modeset.setVisible(false);
		modeset.add(Mode);
		
		pn.add(server);
		pn.add(ip);
		pn.add(ipAddress);
		pn.add(modeset);
		pn.add(ipButton);
		pn.add(portlabel);
		pn.add(jl1);
		pn.add(jl2);
		pn.add(jf1);
		pn.add(jf2);
		
		// ���� ��� ��ư
		server.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// ���α׷� Title ���� ��, Server �޼ҵ� ����
					InetAddress Address = InetAddress.getLocalHost();
					setTitle("�޽��� ���α׷� / ���� Mode / " + Address.toString().split("/")[1]);
					server();
					
					// ��� ���� ��ư�� ���߰�, ��� �ȳ� ���� ǥ��
					server.setVisible(false);
					ip.setVisible(false);
					ipAddress.setVisible(false);
					ipButton.setVisible(false);
					Mode.setText("Server ���� �����Ǿ����ϴ�.");
					modeset.setVisible(true);
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		// Ŭ���̾�Ʈ ��� ��ư
		ipButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// ������ ����
					InetAddress Address = InetAddress.getLocalHost();
					setTitle("�޽��� ���α׷� / Ŭ�� Mode / " + Address.toString().split("/")[1]);
					client(ipAddress.getText());
					server.setVisible(false);
					ip.setVisible(false);
					ipAddress.setVisible(false);
					ipButton.setVisible(false);
					Mode.setText("Client ���� �����Ǿ����ϴ�.");
					modeset.setVisible(true);
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		// �Է� â �̺�Ʈ
		jf2.addActionListener(new TextActionHandler());
		
		// JFrame ǥ��
		setVisible(true);
		
		
	}
	
	// ���� �޼ҵ�
	public void server() {
		new ServerSockThread(this).start();
		new ReceiveDataThread(this).start();
	}
	
	// Ŭ���̾�Ʈ �޼ҵ�
	public void client(String serverName) {
		new ClientSockThread(this, serverName).start();
		new ReceiveDataThread(this).start();
	}
	
	// �޼����� ������ jf2 �ʵ��� �̺�Ʈ�� ��� �̺�Ʈ������
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
	
	// �������� ���� �Է��� �޾� �޼����� jf1�� ���� �޼���
	public void addRecvString(String str) {
		jf1.setText(str);
	}
	
	// �Ҹ���
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
	
	// main
	public static void main(String args[]) throws UnknownHostException {
		new Main();
	}
}

/* ������ ������ ��� ������
 * ���� 13�忡 �ִ� �ڵ�� Ŭ���̾�Ʈ�� ������ �����Ǳ� ������ ���α׷��� ���ߴ� ��찡 �߻��Ͽ���
 * �ش� ���׸� �ذ��ϱ� ���ؼ� ������ Listen �ϴ� �޼��带 ������� �и��Ͽ���.
 */
class ServerSockThread extends Thread {
	private Main chat;
	private ServerSocket serverSock = null;
	private Socket sock = null;
	ServerSockThread(Main chat) {
		this.chat = chat;
	}
	public void run() {
		try {
			// 5095 ��Ʈ�� ������ ����.
			serverSock = new ServerSocket(5095, 1);
			
			// Ŭ���̾�Ʈ���� ������ �����Ǹ�
			// Ŭ���̾�Ʈ�� ���۰��� �޾ƿ� �غ� �Ѵ�.
			chat.sock = serverSock.accept();
			chat.jf1.setText("�����");
			chat.osDataStream = new DataOutputStream(chat.sock.getOutputStream());
			
			// server �޼������� ReceiveDataThread �������� ���� �����ϹǷ�,
			// ���� Ŭ���̾�Ʈ���� ������ ���������� �̷���������� üũ�ϴ� boolean ����
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

// ���� ������ ���� ����
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
			chat.jf1.setText("�����");
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
			// Ŭ���̾�Ʈ - ���� ������ ������ �� ���� ���
			if(chat.accepted) break;
			try {
				// �����̸� ���� ���� ���
				// �����尡 �Ѿ���鼭 Socket�� �ҷ����µ� ���װ� �߻��ϴ� ������ ����
				// �����̸� �־� ��Ȱ�ϰ� �ڿ��� ������ �� �ֵ��� ����
				sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			// ���� ���� �Ѱܹ��� �޽����� �ִ� ���
			// �����ͽ�Ʈ�����κ��� String�� �о�, addRecvString�� ���ڷ� ����
			// addRecvString�� ȭ�鿡 �޽����� ǥ��
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
