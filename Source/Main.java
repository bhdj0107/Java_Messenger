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

		// Mode 설정 버튼 배치
		JButton server = new JButton("서버 열기");
		JLabel ip = new JLabel("IP주소 : ");
		JTextField ipAddress = new JTextField();
		JButton ipButton = new JButton("연결");
		JLabel Mode = new JLabel();
		JLabel jl1 = new JLabel("상대방의 채팅");
		JLabel portlabel = new JLabel("PORT : 5095");
		JLabel jl2 = new JLabel("나의 채팅");
		jf1 = new JTextField("상대의 메세지가 작성될 칸 입니다.");
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
		
		// 서버 모드 버튼
		server.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// 프로그램 Title 변경 후, Server 메소드 실행
					InetAddress Address = InetAddress.getLocalHost();
					setTitle("메신저 프로그램 / 서버 Mode / " + Address.toString().split("/")[1]);
					server();
					
					// 모드 설정 버튼을 감추고, 모드 안내 문구 표시
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
		
		// 클라이언트 모드 버튼
		ipButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// 서버와 동일
					InetAddress Address = InetAddress.getLocalHost();
					setTitle("메신저 프로그램 / 클라 Mode / " + Address.toString().split("/")[1]);
					client(ipAddress.getText());
					server.setVisible(false);
					ip.setVisible(false);
					ipAddress.setVisible(false);
					ipButton.setVisible(false);
					Mode.setText("Client 모드로 설정되었습니다.");
					modeset.setVisible(true);
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		// 입력 창 이벤트
		jf2.addActionListener(new TextActionHandler());
		
		// JFrame 표시
		setVisible(true);
		
		
	}
	
	// 서버 메소드
	public void server() {
		new ServerSockThread(this).start();
		new ReceiveDataThread(this).start();
	}
	
	// 클라이언트 메소드
	public void client(String serverName) {
		new ClientSockThread(this, serverName).start();
		new ReceiveDataThread(this).start();
	}
	
	// 메세지를 보내는 jf2 필드의 이벤트를 듣는 이벤트리스너
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
	
	// 소켓으로 부터 입력을 받아 메세지를 jf1로 띄우는 메서드
	public void addRecvString(String str) {
		jf1.setText(str);
	}
	
	// 소멸자
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

/* 서버의 소켓을 듣는 쓰레드
 * 기존 13장에 있던 코드는 클라이언트와 연결이 성립되기 전까지 프로그램이 멈추는 경우가 발생하였음
 * 해당 버그를 해결하기 위해서 서버가 Listen 하는 메서드를 쓰레드로 분리하였음.
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
			// 5095 포트로 서버를 연다.
			serverSock = new ServerSocket(5095, 1);
			
			// 클라이언트와의 연결이 성립되면
			// 클라이언트의 전송값을 받아올 준비를 한다.
			chat.sock = serverSock.accept();
			chat.jf1.setText("연결됨");
			chat.osDataStream = new DataOutputStream(chat.sock.getOutputStream());
			
			// server 메서스에서 ReceiveDataThread 쓰레도와 동시 동작하므로,
			// 먼저 클라이언트와의 연결이 정상적으로 이루어졌는지를 체크하는 boolean 변수
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

// 위의 서버의 경우와 동일
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
			// 클라이언트 - 서버 연결이 성립될 때 까지 대기
			if(chat.accepted) break;
			try {
				// 딜레이를 주지 않은 경우
				// 쓰레드가 넘어오면서 Socket을 불러오는데 버그가 발생하는 것으로 보임
				// 딜레이를 주어 원활하게 자원에 접근할 수 있도록 변경
				sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			// 상대로 부터 넘겨받은 메시지가 있는 경우
			// 데이터스트림으로부터 String을 읽어, addRecvString의 인자로 전달
			// addRecvString은 화면에 메시지를 표시
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
