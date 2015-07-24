package GUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Queue;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import usermanager.User;
import wetalk.WeTalkDownPart;

import java.awt.SystemColor;


public class ChatFrameOnline extends JFrame {
	protected static Logger log =
			LoggerFactory.getLogger(ChatFrameOnline.class);
	Queue<User> comeQueue;
	Queue<User> goQueue;
	
	private JPanel contentPane;
	private JTextField textField;
	private JLabel usernameLabel;

	/**
	 * Create the frame.
	 */
	public ChatFrameOnline(Queue<User> comequeue, Queue<User> goqueue) {
		setType(Type.UTILITY);
		this.comeQueue = comequeue;
		this.goQueue = goqueue;
		
		setTitle("WeTalk");
		setBackground(new Color(230, 230, 250));
		setIconImage(Toolkit.getDefaultToolkit().getImage("./icons/title.png"));	
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 398, 295);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JLayeredPane layeredPane = new JLayeredPane();
		contentPane.add(layeredPane, BorderLayout.CENTER);
		
		usernameLabel = new JLabel("用户名：");
		usernameLabel.setBounds(235, 62, 127, 47);
		layeredPane.add(usernameLabel);
		usernameLabel.setForeground(new Color(0, 0, 128));
		usernameLabel.setFont(new Font("楷体", Font.BOLD, 20));
		
		JButton btnNewButton = new JButton("登录");
		btnNewButton.setBounds(273, 198, 89, 41);
		layeredPane.add(btnNewButton);
		btnNewButton.setForeground(new Color(0, 0, 128));
		btnNewButton.setBackground(new Color(0, 204, 255));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				button_action(e);
			}
		});
		btnNewButton.setFont(new Font("楷体", Font.PLAIN, 26));
		
		textField = new JTextField();
		textField.setBackground(SystemColor.inactiveCaptionBorder);
		textField.setBounds(235, 120, 127, 42);
		layeredPane.add(textField);
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER){
					button_action(null);
				}
			}
		});
		textField.setForeground(new Color(0, 0, 128));
		textField.setFont(new Font("黑体", Font.PLAIN, 20));
		textField.setColumns(10);
		
		JLabel foregoundLabel = new JLabel("");
		foregoundLabel.setIcon(new ImageIcon("./icons/title.png"));
		foregoundLabel.setBounds(74, 34, 135, 126);
		layeredPane.add(foregoundLabel);
		
		JLabel backgroundLabel = new JLabel("");
		backgroundLabel.setBounds(0, 0, 372, 249);
		layeredPane.add(backgroundLabel);
		backgroundLabel.setIcon(new ImageIcon("./icons/online.jpg"));
	}

	// 按钮事件处理代码button_action
	void button_action(ActionEvent e) {
		String myname=textField.getText(); //获得用户名
		if(myname.compareTo("")!=0 ) {
			//创建自己的对象
			String myMAC = getHostMac();
			String myIP = getHostIp();
			if(myIP != null){
				User me = new User(myname,null,myMAC,myIP);
			
				//用户上线，显示主窗口
				this.setVisible(false);		
			
				WeTalkDownPart.Start(comeQueue, goQueue, me);
			
				log.info("show main frame.");
				ChatFrameMain.showFrameMain(comeQueue, goQueue,me);
			} else {
				usernameLabel.setText("请先联网!");
				usernameLabel.setForeground(Color.RED);
			}
			
		} else {
			usernameLabel.setText("填写用户名!");
			usernameLabel.setForeground(Color.RED);
		}

	}
	
	
	//显示上线窗口。静态方法
	public static void showFrameOnline(Queue<User> comequeue,Queue<User> goqueue) {
		try {
			ChatFrameOnline frameonline;
			frameonline = new ChatFrameOnline(comequeue,goqueue);
			frameonline.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			// 把窗口置于中心
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension frameSize = frameonline.getSize();
			if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
			}
			frameonline.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height -
			frameSize.height) / 2);
			
			frameonline.setVisible(true);
			log.info("online frame show up.");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}	

	
	//*************************
	//  内部函数
	//*************************

	//获得本机的IP地址
	protected String getHostIp(){
		String myip = null;
		
		try {
			InetAddress local = InetAddress.getLocalHost();
			while(local.isAnyLocalAddress()) local = InetAddress.getLocalHost();	//查找非回环地址
			myip = local.getHostAddress();
		} catch (UnknownHostException e) {
			log.error("Wrong when get self ip address.", e);
		}
		return myip;
	}

	//获得本机的MAC地址
	@SuppressWarnings("finally")
	protected String getHostMac(){
		String mymac = null;
		NetworkInterface netInterface = null;
		try {
			netInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
			byte[] macAddr = netInterface.getHardwareAddress();
			mymac = new String(macAddr);
		} catch (SocketException e) {
			log.error("Can not get MAC address", e);
			mymac =null;
		} catch (UnknownHostException e) {
			log.error("Can not get MAC address.", e);
			mymac =null;
		} finally {
			return mymac;	
		}
	}
}
