package message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import usermanager.User;

public class BaseMessage implements java.io.Serializable {
	
	public static final byte SECK_SERVER_MESSAGE = 0x01;		//broadcast
	public static final byte SECK_SERVER_ACK_MESSAGE = 0x02;	//unicast
	public static final byte USER_GO_MESSAGE = 0x03;			//broadcast
	
	private byte type;
	private User USER;

	public BaseMessage(byte msgtype){
		this.type = msgtype;
	}
	
	public byte getType(){
		return type;
	}
	
	public void setUser(User user){
		USER = user;
	}
	
	public User getUser(){
		return USER;
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
	public static BaseMessage deserialize(byte[] in){
		ByteArrayInputStream bais = new ByteArrayInputStream(in);
		ObjectInputStream ois;
		BaseMessage msg = null;
		try {
			ois = new ObjectInputStream(bais);
			msg = (BaseMessage)ois.readObject();	
			return msg;			
		} catch (Exception e) {

		} finally {
			return msg; 
		}


	}
	

	
}
