package GroupTalk;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Queue;

import message.GroupChatMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import usermanager.User;
import GUI.ChatFrameGroupChat;

public class groupMessageReceive {
	protected static Logger log = 
			LoggerFactory.getLogger(groupMessageReceive.class);
	
	MulticastSocket ms;
    int GroupListenPort=5555;
    boolean isRunning=true;
    private Queue<GroupChatMessage> chatcontent;
    User Me;
    GroupChatMessage recvmsg = null;
    
    public groupMessageReceive(Queue<GroupChatMessage> chatcontent,User Me){
    	this.chatcontent=chatcontent;
    	this.Me=Me;
    }
    
    //群消息记录队列
   public  void MessageReceive(){
	while(true){
    try {
		 ms=new MulticastSocket(GroupListenPort);
		 byte[] buffer = new byte[1024];
	     DatagramPacket pkt;
	     pkt = new DatagramPacket(buffer,buffer.length);
		 while(isRunning){
			 ms.receive(pkt);
			 byte[] pktdata = pkt.getData();
			 
			 recvmsg = GroupChatMessage.deserialize(pktdata);

			 //群聊界面的工作需要分消息类型进行处理
			 switch(recvmsg.getType()){
			 case GroupChatMessage.BUILD_GROUP_MESSAGE:
				 //加入群组
				 ms.joinGroup(recvmsg.getGroupIp());	
				 //新建群聊界面，打开群聊界面：ChatFrameGroup,开启群聊窗口
				 Thread t = new newGroupChat();
				 t.start();
				 break;
			 case GroupChatMessage.JION_GROUP_MESSAGE:
				 break;
			 case GroupChatMessage.LEAVE_GROUP_MESSAGE:
				 break;
				 //消息生产者
			 case GroupChatMessage.CHATING_GROUP_MESSAGE:
				 if(pkt.getAddress().getHostAddress().equals(Me.getIPAddress())) break;
				
				 //用一个消息并发队列进行存储
				 chatcontent.add(recvmsg);
				 break;
				 //前端界面线程调用队列中消息并显示
			 default:
				 break;
			 }
		 }
	} catch (IOException e) {
		log.error("Wrong at receiver group messages.", e);
	}	
   }
   }
   
	 private class newGroupChat extends Thread{
		 public void run(){
			 ChatFrameGroupChat.showFrameChat(recvmsg.getGroupName(),recvmsg.getGroupIp(),Me,chatcontent,ms);
		 }
	 }
   
}
