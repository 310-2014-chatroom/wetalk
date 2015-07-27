package message;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.List;

import usermanager.User;

public class GroupChatMessage implements java.io.Serializable{
	public static final byte BUILD_GROUP_MESSAGE = 0x01;			//broadcast
	public static final byte JION_GROUP_MESSAGE = 0x02;		    	//unicast
	public static final byte LEAVE_GROUP_MESSAGE = 0x03;		    //unicast
	public static final byte CHATING_GROUP_MESSAGE = 0x04;		    //unicast
	
	
	private byte type;
	private User Me;//自己的信息
	private int port;//群聊监听端口号
	InetAddress groupIp;
	private String GroupName;
	private String content;
	
	public  GroupChatMessage(byte msgtype,int port,InetAddress groupIp,String GroupName,String content){
		this.type = msgtype;
		this.port=port;
		this.groupIp=groupIp;
		this.GroupName=GroupName;
		this.content=content;
	}
	
	public byte getType(){
		return type;
	}
	
	public void setUser(User ME){
		this.Me =  ME;
	}
	
	public User getUser(){
		return this.Me;
	}
	public int getPort(){
		return this.port;
	}
	public InetAddress getGroupIp(){
		return this.groupIp;
	}
	public String getContent(){
		return this.content;
	}
	
	public String getGroupName(){
		return this.GroupName;
	}
	
	
	@SuppressWarnings("finally")
	public byte[] srialize(){
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		byte msg[] = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(this);	//顶层输入
			oos.flush();
		    msg = baos.toByteArray();	//底层输出
		} catch (IOException e) {

		} finally{
			return msg;			
		}
				
	}
	@SuppressWarnings("finally")
	public static GroupChatMessage deserialize(byte[] in){
		ByteArrayInputStream bais = new ByteArrayInputStream(in);
		ObjectInputStream ois;
		GroupChatMessage msg = null;
		try {
			ois = new ObjectInputStream(bais);
			msg = (GroupChatMessage)ois.readObject();	
			return msg;			
		} catch (Exception e) {

		} finally {
			return msg; 
		}


	}
	

	
}

	
	
	
	


