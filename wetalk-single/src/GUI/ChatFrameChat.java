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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;

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

import usermanager.User;
import File.FileSend;

public class ChatFrameChat extends JFrame {
	User ME;
	User PEER;
	private Map<String, User> UserStore;
	int TalkPort;
	
	DatagramSocket socket = null;
	DatagramPacket pkt = null;

	private JPanel contentPane;
	private JTextField textField;
	private JTextArea textArea;
	private final Action action = new SwingAction();

	/**
	 * Create the frame.
	 */
	public ChatFrameChat(User me, User peer, Map<String, User> userstore) {
		ME = me;
		PEER = peer;
		UserStore = userstore;
		
		try{
			socket = new DatagramSocket();
		} catch(Exception e) {
			e.printStackTrace();			
		}
		
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
	
	//发送消息事件处理
	protected void sendButtenEvent(){
		String payload = textField.getText(); //获得用户名
		if(payload.compareTo("")!=0 ) {
			textArea.append(ME.getName()+":"+payload + '\n');
			textField.setText(null);
			
			//TODO 发送给对端
		}
		
	}
	
	//显示会话窗口。静态方法
	public static void showFrameChat(User me,User peer,Map<String, User> userstore) {
		try {
			ChatFrameChat framechat;
			framechat = new ChatFrameChat(me, peer,userstore);
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
		//	framemain.working();
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
