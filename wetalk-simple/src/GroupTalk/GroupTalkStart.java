package GroupTalk;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.tree.MutableTreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import usermanager.User;
import GUI.ChatFrameOnline;

public class GroupTalkStart extends Thread{
	protected static Logger log = 
			LoggerFactory.getLogger(ChatFrameOnline.class);	
	
	private Map<String, User> UserStore;
	private Map<String,MutableTreeNode> ChosenUsermap;
	
	public  GroupTalkStart(Map<String, User> userstore){
		this.UserStore = userstore; 
		ChosenUsermap = new ConcurrentHashMap<>();
	}
	
	public void run(){
		showFrameChoose(UserStore, ChosenUsermap);
	}
	
	private void showFrameChoose(Map<String, User> userstore, Map<String,MutableTreeNode> chosenusermap) {
		try {		
			FrameChooseGroup framechoose = new FrameChooseGroup(userstore, chosenusermap);
			framechoose.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	
			// 把窗口置于中心
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension frameSize = framechoose.getSize();
			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}
			framechoose.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height -
				frameSize.height) / 2);
	
			framechoose.setVisible(true);
			log.debug("choose frame show.");       
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}