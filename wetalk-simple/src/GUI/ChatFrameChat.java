package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import message.MsgInternal;
import usermanager.User;
import File.FileSend;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ChatFrameChat extends JFrame {
	protected static Logger log = 
			LoggerFactory.getLogger(ChatFrameChat.class);
	static final int TalkPort = 4567;
	
	User ME;
	User PEER;
	Queue<MsgInternal> MsgInternalQueue;	//所有消息队列
	Map<String, User> chattingUserStore;	//会话的用户
	
	DatagramSocket socket = null;

	private JPanel contentPane;
	private JTextField textField;
	private volatile JTextArea textArea;
	private final Action action;
	
	private volatile boolean isRunning;		//表示接收线程状态

	/**
	 * Create the frame.
	 */
	public ChatFrameChat(User me, User peer, 
						Queue<MsgInternal> MsgInternalQueue,
						Map<String, User> chattingUserStore) {

		this.ME = me;
		this.PEER = peer;
		this.MsgInternalQueue = MsgInternalQueue;
		this.chattingUserStore = chattingUserStore;
		
		this.action = new SwingAction();
		this.isRunning = true;
		
		try{
			socket = new DatagramSocket();
		} catch(Exception e) {
			e.printStackTrace();			
		}
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				chatExit(e);
			}
		});
		setIconImage(Toolkit.getDefaultToolkit().getImage("./wetalk-single/icons/title.png"));	
		setTitle(peer.getRemark()==null? peer.getName() : peer.getRemark());

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 509, 479);
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
		backgroundLabel.setBounds(0, 0, 483, 431);
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
		sendButton.setBounds(390, 383, 83, 38);
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
		textField.setBounds(0, 352, 380, 69);
		layeredPane.add(textField);
		textField.setColumns(10);
		textField.setHorizontalAlignment(JTextField.LEFT);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 380, 342);
		layeredPane.add(scrollPane);
		
		textArea = new JTextArea();
		textArea.setBackground(SystemColor.inactiveCaption);
		textArea.setFont(new Font("宋体", Font.PLAIN, 20));
		textArea.setForeground(new Color(0, 0, 102));
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		
		JButton btnNewButton = new JButton("发送文件");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				sendFilebutton_event(e);
			}
		});
		btnNewButton.setAction(action);
		btnNewButton.setBounds(390, 10, 93, 38);
		layeredPane.add(btnNewButton);
	}
	
	//发送消息（事件驱动）
	protected void sendButtenEvent(){
		String payload = textField.getText(); //获得用户名
		if(payload.compareTo("")!=0 ) {
			textArea.append(ME.getName()+":"+payload + '\n');
			textField.setText(null);
			
			//发送给对端		
			try {				
				DatagramPacket pkt = new DatagramPacket(payload.getBytes(),payload.getBytes().length,
														InetAddress.getByName(this.PEER.getIPAddress()),
														TalkPort);
				socket.send(pkt);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	//接收消息（新线程轮询）
	private void working(){
		Receiver receiver = new Receiver();
		receiver.start();
	}
	private class Receiver extends Thread{
		public void run(){
			while(isRunning){
				if(!MsgInternalQueue.isEmpty()){
					Iterator<MsgInternal> iterator = MsgInternalQueue.iterator();
					while(iterator.hasNext()){
						MsgInternal msg = (MsgInternal)iterator.next();
						//找出发给自己的消息
						if(msg.getIP().equals(PEER.getIPAddress())){
							textArea.append(PEER.getName()+":" + msg.getMsg() + '\n');
							iterator.remove();
						}
					}
				}
			}
			log.info("finifsh chatting receiver.");
		}
	}
	
	//关闭窗口退出操作
	private void chatExit(WindowEvent e){
		chattingUserStore.remove(PEER.getIPAddress());
		//关闭接收线程
		this.isRunning = false;
		log.info("finish chatting with " + PEER.getName());
	}
		
	//显示会话窗口。静态方法
	public static void showFrameChat(User me,User peer,
								Queue<MsgInternal> MsgInternalQueue,
								Map<String, User> chattingUserStore) {
		try {
			ChatFrameChat framechat;
			framechat = new ChatFrameChat(me, peer, MsgInternalQueue, chattingUserStore);
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
	
	//select sendfile
	void sendFilebutton_event(MouseEvent e){
		File sendfile = null;
		JFileChooser chooser = new JFileChooser();//初始化文件选择框
		chooser.setDialogTitle("请选择文件");//设置文件选择框的标题 
		int result =chooser.showOpenDialog(null);//弹出选择框
		if(JFileChooser.APPROVE_OPTION == result){
			sendfile = chooser.getSelectedFile();
			FileSend filesender = new FileSend(this.PEER.getIPAddress(), sendfile);
			filesender.start();
		}
	}
	
	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "发送文件");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
		}
	}
}
