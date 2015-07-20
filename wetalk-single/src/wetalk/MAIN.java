package wetalk;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import usermanager.User;
import GUI.ChatFrameOnline;
public class MAIN{
	protected static Logger log = LoggerFactory.getLogger(MAIN.class);
	boolean packFrame = false;
	static Queue<User> comeQueue;
	static Queue<User> goQueue;
	
		
	public static void main(String[] args) {
		comeQueue = new ConcurrentLinkedQueue<>();
		goQueue = new ConcurrentLinkedQueue<>();
		
		ChatFrameOnline.showFrameOnline(comeQueue, goQueue);		
	}


}
