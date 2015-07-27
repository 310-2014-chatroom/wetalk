package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Queue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import message.GroupChatMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import usermanager.User;

public class ChatFrameGroupChat extends JFrame {
	protected static Logger log = 
			LoggerFactory.getLogger(ChatFrameGroupChat.class);
	boolean isRunning;
	private JPanel contentPane;
	private JTextField textField;
	private volatile JTextArea textArea;
	int GroupListenPort=5555;
	String groupName;
	InetAddress groupIp;
	User ME;
	private Queue<GroupChatMessage> chatcontent;
	
	MulticastSocket ms = null;

	/**
	 * Create the frame.
	 */
	public ChatFrameGroupChat(String groupName,InetAddress groupIp,User Me,Queue<GroupChatMessage> chatcontent,
							MulticastSocket ms) {
		
		this.isRunning = true;
		
        this.groupIp=groupIp;
        this.groupName=groupName;
        this.ME=Me;
        this.chatcontent=chatcontent;
		this.ms = ms;
        
        setType(Type.UTILITY);
		setIconImage(Toolkit.getDefaultToolkit().getImage("./icons/title.png"));	
        setTitle(this.groupName);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				chatExit(e);
			}
		});
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 502, 677);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.setForeground(SystemColor.inactiveCaption);
		layeredPane.setBackground(SystemColor.inactiveCaption);
		contentPane.add(layeredPane, BorderLayout.CENTER);
		
		JLabel backgroundLabel = new JLabel("");
		backgroundLabel.setBackground(SystemColor.inactiveCaption);
		backgroundLabel.setFont(new Font("宋体", Font.PLAIN, 14));
		backgroundLabel.setForeground(SystemColor.inactiveCaption);
		backgroundLabel.setBounds(0, 0, 483, 631);
		layeredPane.add(backgroundLabel);
		
		JButton sendButton = new JButton("发送");
		sendButton.setForeground(new Color(0, 0, 128));
		sendButton.setBackground(SystemColor.inactiveCaption);
		sendButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				sendButtenEvent();
			}
		});
		sendButton.setFont(new Font("宋体", Font.BOLD, 16));
		sendButton.setBounds(390, 593, 83, 38);
		layeredPane.add(sendButton);
		
		textField = new JTextField();
		textField.setFont(new Font("宋体", Font.PLAIN, 18));
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER){
					sendButtenEvent();
				} else if(e.getKeyCode()==KeyEvent.VK_ENTER){
					
				}
			}
		});
		textField.setForeground(new Color(153, 0, 0));
		textField.setBackground(SystemColor.inactiveCaption);
		textField.setBounds(0, 562, 380, 69);
		layeredPane.add(textField);
		textField.setColumns(10);
		textField.setHorizontalAlignment(JTextField.LEFT);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 380, 552);
		layeredPane.add(scrollPane);
		
		textArea = new JTextArea();
		textArea.setBackground(SystemColor.inactiveCaption);
		textArea.setFont(new Font("宋体", Font.PLAIN, 20));
		textArea.setForeground(new Color(0, 0, 102));
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);		
	}
	
	//显示收到的消息
	private class MessageReceive extends Thread{
		public void run(){
			while(isRunning){
				while(!chatcontent.isEmpty()){
					GroupChatMessage recvmsg = chatcontent.poll();
					if(recvmsg.getGroupIp().equals(groupIp)){
						switch(recvmsg.getType()){
						case GroupChatMessage.BUILD_GROUP_MESSAGE:		
							break;
						case GroupChatMessage.JION_GROUP_MESSAGE:
							break;
						case GroupChatMessage.LEAVE_GROUP_MESSAGE:
							break;
							//消息生产者
						case GroupChatMessage.CHATING_GROUP_MESSAGE:
							textArea.append(recvmsg.getContent()+ '\n');	//显示消息
							break;
							//前端界面线程调用队列中消息并显示
						default:
							break;
						}
		
					}
				}
			}
			log.info("finish group talk's receive.");
		}
	}
	
	//发送消息（事件驱动）
	protected void sendButtenEvent(){
		String payload = textField.getText(); //获得用户名
		//判断发送消息是否为空
		if(payload.compareTo("")!=0 ) {
			
			textArea.append(this.ME.getName()+':' + payload + '\n');	//显示消息
			
			textField.setText(null);//发送框清空
			//构造消息数据报
			GroupChatMessage content=new GroupChatMessage( GroupChatMessage.CHATING_GROUP_MESSAGE ,this.GroupListenPort ,this.groupIp, this.groupName,this.ME.getName()+':'+payload);
			try {				
			    byte send[]=content.srialize();
			    DatagramPacket pkt=new DatagramPacket(send,send.length,groupIp,GroupListenPort);
			    ms.send(pkt);
			} catch (IOException e) {
				log.error("Wrong when seng multicast message.");
			}
				
		}
		
	}

	private void working(){
		MessageReceive receiver = new MessageReceive();
		receiver.start();
	}	
	
	//关闭窗口退出操作
	private void chatExit(WindowEvent e){
		isRunning = false;
		if(ms !=null) ms.close();
		log.info("finish group talk.");
	}
	
	
	//显示会话窗口。静态方法
	public static void showFrameChat(String groupName,InetAddress groupIp,User Me,Queue<GroupChatMessage> chatcontent,
						MulticastSocket ms) {
		try {
			ChatFrameGroupChat framechat;
			framechat = new ChatFrameGroupChat(groupName,groupIp,Me,chatcontent,ms);
			framechat.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			// 把窗口置于中心
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension frameSize = framechat.getSize();
			if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
			}
			framechat.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height -
			frameSize.height) / 2);
			
			framechat.setVisible(true);
			framechat.working();		 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
