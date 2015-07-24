package message;

public class MsgInternal {
	private String ip;
	private String msg;
	
	public MsgInternal(String ip, String msg){
		this.ip = ip;
		this.msg = msg;
	}
	
	public String getIP(){
		return this.ip;
	}
	
	public String getMsg(){
		return this.msg;
	}
	
	public String toString(){
		return this.ip + "," +this.msg;
	}
}
