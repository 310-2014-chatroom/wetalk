package GroupTalk;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Queue;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
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
import GUI.ChatFrameOnline;

import java.awt.Window.Type;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


//窗体操作内部类
public class FrameChooseGroup extends JFrame{
	protected static Logger log = 
			LoggerFactory.getLogger(ChatFrameOnline.class);	
	
	private Map<String, User> UserStore;				//全部用户
	private Map<String,MutableTreeNode> ChosenUsermap;	//存放已选择的用户
	
	private JPanel contentPane;
	
	private JTree all_usertree;
	private JTree chosen_usertree;
	private DefaultTreeModel all_treemodel;
	private DefaultTreeModel chosen_treemodel;
	private DefaultMutableTreeNode all_top;
	private DefaultMutableTreeNode chosen_top;
	
	DefaultMutableTreeNode NodeChosen_all = null;	//被选中的节点
	DefaultMutableTreeNode NodeChosen_chosen = null;	//被选中的节点
	
	
	/**
	 * Create the frame.
	 */
	FrameChooseGroup(Map<String, User> userstore, Map<String,MutableTreeNode> chosenusermap) {
		this.UserStore = userstore;
		this.ChosenUsermap = chosenusermap;

		setType(Type.UTILITY);		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				finish_action(null);
			}
		});		
		setIconImage(Toolkit.getDefaultToolkit().getImage("./icons/title.png"));
		setTitle("选择用户");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 310, 452);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JLayeredPane layeredPane = new JLayeredPane();
		contentPane.add(layeredPane, BorderLayout.CENTER);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 30, 138, 322);
		layeredPane.add(scrollPane);
		
		all_usertreeInit();
		all_usertree = new JTree(all_treemodel);
		scrollPane.setViewportView(all_usertree);
		all_usertree.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if(e.getClickCount() > 0){
					nodeClicked_add(e);
				}	
			}
		});
		all_usertree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
            	DefaultMutableTreeNode node=(DefaultMutableTreeNode)e.getPath().getLastPathComponent();
                if(node.isLeaf()){                	
                	NodeChosen_all = node;
                }
            }
        });
		
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(148, 30, 136, 322);
		layeredPane.add(scrollPane_1);
		
		chosen_usertreeInit();
		chosen_usertree = new JTree(chosen_treemodel);
		scrollPane_1.setViewportView(chosen_usertree);
		chosen_usertree.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if(e.getClickCount() > 0){
					nodeClicked_del(e);
				}	
			}
		});
		chosen_usertree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
            	DefaultMutableTreeNode node=(DefaultMutableTreeNode)e.getPath().getLastPathComponent();
                if(node.isLeaf()){                	
                	NodeChosen_chosen = node;
                }
            }
        });
		
		
		JButton btnNewButton = new JButton("确定");
		btnNewButton.setBounds(191, 362, 93, 39);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finish_action(e);
			}
		});
		layeredPane.add(btnNewButton);
		
		JLabel remind_Label = new JLabel("提示：双击用户选择或放弃");
		remind_Label.setBounds(0, 362, 174, 39);
		layeredPane.add(remind_Label);
		
	}

	//选择结束
	protected void finish_action(ActionEvent e){
	
		
		log.debug("choose frame shutdown.");
		this.dispose();
	}

	protected void nodeClicked_add(MouseEvent e){
		if(e.getClickCount() > 0 && NodeChosen_all != null){
			String nodestring = NodeChosen_all.getUserObject().toString();
			if(nodestring == "可选用户列表"){
				all_usertree.clearSelection();
				return;
			}
						
			User user = (User)NodeChosen_all.getUserObject();
			switch(e.getClickCount()){
			case 0:
				break;
			case 1:
				break;
			case 2:
				MutableTreeNode node = new DefaultMutableTreeNode(user);
				ChosenUsermap.put(user.getIPAddress(), node);
				chosen_top.add(node);
				chosen_treemodel.reload();	//刷新
				break;
			default:					
				break;
			}
				
			all_usertree.clearSelection();		
		}
			
		return;
	}
	
	protected void nodeClicked_del(MouseEvent e){
		if(e.getClickCount() > 0 && NodeChosen_chosen != null){
			String nodestring = NodeChosen_chosen.getUserObject().toString();
			if(nodestring == "已选择用户" ){
				chosen_usertree.clearSelection();
				return;
			}
						
			User user = (User)NodeChosen_chosen.getUserObject();
			switch(e.getClickCount()){
			case 0:
				break;
			case 1:
				break;
			case 2:
				MutableTreeNode node = ChosenUsermap.remove(user.getIPAddress());	//
				chosen_treemodel.removeNodeFromParent(node);
				chosen_treemodel.reload();	//刷新
				break;
			default:					
				break;
			}
				
			chosen_usertree.clearSelection();		
		}
			
		return;
	}
	
	
	//内部方法		
	//初始化关系树
	protected void all_usertreeInit(){
		all_top = new DefaultMutableTreeNode("可选用户列表");
		
		for(Entry<String, User> entry : UserStore.entrySet()){
			User user = entry.getValue();
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(user); 				
			all_top.add(node);
		}
		all_treemodel = new DefaultTreeModel(all_top);
        return;
	}
	protected void chosen_usertreeInit(){
		chosen_top = new DefaultMutableTreeNode("已选择用户");
		chosen_treemodel = new DefaultTreeModel(chosen_top);
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
	    	   case "可选用户列表" :
	    		   this.setText("可选用户列表");
	    		   this.setIcon(new ImageIcon("./icons/user_select_24.png"));
	    		   break;
	    	   case "已选择用户" :	
	    		   this.setText("已选择用户");	    		   
	    		   this.setIcon(new ImageIcon("./icons/user_selected_24.png"));
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

