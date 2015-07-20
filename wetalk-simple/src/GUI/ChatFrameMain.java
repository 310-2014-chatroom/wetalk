package GUI;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import usermanager.User;
import wetalk.WeTalkDownPart;


public class ChatFrameMain extends JFrame {
	protected static Logger log = 
			LoggerFactory.getLogger(ChatFrameOnline.class);
	
	Queue<User> comeQueue;
	Queue<User> goQueue;
	User ME;
	Map<String, User> UserStore;
	ExecutorService threadpool;
	
	private JPanel contentPane;
	private JTree usertree;

	private volatile DefaultMutableTreeNode top;
	private volatile DefaultMutableTreeNode nodeUser;
	private volatile DefaultTreeModel treemodel;
	
	DefaultMutableTreeNode NodeChosen = null;	//被选中的节点
	
	/**
	 * Create the frame.
	 */
	public ChatFrameMain(Queue<User> comequeue, Queue<User> goqueue, User me){
		this.comeQueue = comequeue;
		this.goQueue = goqueue;
		this.ME = me;
		this.UserStore = new ConcurrentHashMap<>();
		this.threadpool = Executors.newCachedThreadPool();
		
		setTitle("wetalk");
		setIconImage(Toolkit.getDefaultToolkit().getImage("./icons/title.png"));
		
		//窗体关闭事件
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				mainFrameExiting(e);
			}
		});
		
				
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 279, 639);
		contentPane = new JPanel();
		contentPane.setBackground(SystemColor.inactiveCaption);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);		
		
		JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addComponent(scrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 591, Short.MAX_VALUE)
		);
		
		usertreeInit();
		usertree = new JTree(treemodel);
		usertree.setForeground(SystemColor.inactiveCaption);
		usertree.setBackground(SystemColor.inactiveCaptionBorder);
		scrollPane.setViewportView(usertree);
		usertree.setCellRenderer(new NewDefaultTreeCellRenderer());
		usertree.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if(e.getClickCount() > 0){
					nodeClicked(e);
				}	
			}
		});
		usertree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
            	DefaultMutableTreeNode node=(DefaultMutableTreeNode)e.getPath().getLastPathComponent();
                if(node.isLeaf()){                	
                	NodeChosen = node;
                }
            }
        });

		contentPane.setLayout(gl_contentPane);
	}
	
	//处理用户操作	
	protected void nodeClicked(MouseEvent e){
		if(e.getClickCount() > 0 && NodeChosen != null){
			String nodestring = NodeChosen.getUserObject().toString();
			if(nodestring == "用户列表" || nodestring == "用户"){
				usertree.clearSelection();
				return;
			}
						
			User user = (User)NodeChosen.getUserObject();
			switch(e.getClickCount()){
			case 0:
				break;
			case 1:
				break;
			case 2:
				startchatting(user);
				break;
			default:					
				break;
			}
				
			usertree.clearSelection();		
		}
			
		return;
	}
	
	
	//基础线程
	 protected abstract class BaseThread extends Thread{
		public abstract void myrun();
		
		public void run(){
				myrun();			
		}				
	}
	
	///////////////////
	//会话
	//////////////////
	protected void startchatting(User peer){
		Thread chatter = new chatting(peer);
		threadpool.submit(chatter);
		
	}
	protected class chatting extends BaseThread{
		User peer;
		
		chatting(User p){
			this.peer = p;
		}
		
		public void myrun(){
			ChatFrameChat.showFrameChat(ME,peer,UserStore);
		}
		
	}
	
	//////////////////
	//维护用户列表
	//////////////////
	public void working(){
		Thread updater = new Updater();
		threadpool.submit(updater);
		return;
	}

	protected class Updater extends BaseThread{
		Map<String,MutableTreeNode> Usermap;
		public void myrun(){
			Usermap = new HashMap<>();
			while(true){
				//有新用户
				while(!comeQueue.isEmpty()){
					User newcomer = comeQueue.poll();
					if (newcomer!=null){
						UserStore.put(newcomer.getIPAddress(), newcomer);					
						updateUserTree(newcomer,true);		
					}
				}
				//用户离开
				while(!goQueue.isEmpty()){
					User newcomer = goQueue.poll();
					if (newcomer!=null){
						User user = UserStore.remove(newcomer.getIPAddress());
						if(user != null)
							updateUserTree(newcomer,false);	
					}
					
				}
			}
			
		}
		//维护用户列表
		protected void updateUserTree(User user,boolean isadd){
			if(isadd){
				log.info("node add:{}",user.toString());
				MutableTreeNode newnode = new DefaultMutableTreeNode(user);
				Usermap.put(user.getIPAddress(), newnode);
				
				nodeUser.add(newnode);
				treemodel.reload();	//刷新
			} else {
				log.info("node del:{}",user.toString());
				MutableTreeNode gonode = Usermap.remove(user.getIPAddress());
				if(gonode != null){
					treemodel.removeNodeFromParent(gonode);
					treemodel.reload();	//刷新
				}
			}
			return;
		}
		
	}	
	
	//主窗口退出，程序结束。
	protected void mainFrameExiting(WindowEvent e){
		//发送下线消息,关闭其线程
		WeTalkDownPart.Offline(ME);
		
		threadpool.shutdownNow();		
		//检查聊天窗口是否全部关闭
		
		log.info("main exit.");
		System.exit(0);
	}

	//显示主窗口.静态方法
	public static void showFrameMain(Queue<User> comequeue,Queue<User> goqueue, User me) {
		try {
			ChatFrameMain framemain;
			framemain = new ChatFrameMain(comequeue, goqueue, me);
			framemain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			// 把窗口置于中心
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension frameSize = framemain.getSize();
			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}
			framemain.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height -
			frameSize.height) / 2);
				
			framemain.setVisible(true);
			framemain.working();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	////////////////
	//一些内部方法
	////////////////
	//初始化关系树
	protected void usertreeInit(){
		top = new DefaultMutableTreeNode("用户列表"); 		
		nodeUser = new DefaultMutableTreeNode("用户");         

		top.add(new DefaultMutableTreeNode(ME));
        top.add(nodeUser); 
		treemodel = new DefaultTreeModel(top);
        return;
	}	
	
	//修改图标
	protected class NewDefaultTreeCellRenderer extends DefaultTreeCellRenderer{  	 
	    private static final long   serialVersionUID    = 1L;  
	  
	    // 重写父类DefaultTreeCellRenderer的方法     
	    public Component getTreeCellRendererComponent(JTree tree, Object value,  
	            boolean sel, boolean expanded, boolean leaf, int row,  
	            boolean hasFocus)  
	    {  	  
	        //执行父类原型操作  
	        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,  
	                row, hasFocus);  
	  
	        if (sel)  
	        {  
	            setForeground(getTextSelectionColor());  
	        }  
	        else  
	        {  
	            setForeground(getTextNonSelectionColor());  
	        } 

        
	       	switch(value.toString()){
	       	case "用户列表":
		       	setText("用户列表");
	       		this.setIcon(new ImageIcon("./icons/allusers24.png")); 
	      		break;
	      	case "用户":
		       	setText("用户");
	       		this.setIcon(new ImageIcon("./icons/users_green24.png"));
	       		break;	        			        	
	       	default:
		        DefaultMutableTreeNode node=(DefaultMutableTreeNode)value;
		        User user = (User)node.getUserObject();
		       	setText(user.getRemark()==null? user.getName() : user.getRemark());
	       		this.setIcon(new ImageIcon("./icons/user24.png"));  	        	
	       	}
	     return this;
	      
	    }
	}

}
