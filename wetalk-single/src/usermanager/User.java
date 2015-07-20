package usermanager;

import java.io.Serializable;

public class User implements Serializable {
	String name = null;	//用户名
	String remark = null;	//备注
	String MAC = null;		//MAC地址
	String  IP = null;		//IP地址
	
	public User(String na,String re,String ma,String ip){
		name = na;
		remark = re;
		MAC = ma;
		IP = ip;		
	}
	
	public String getName(){
		return this.name;
	}
	public String getRemark(){
		return this.remark;
	}
	
	public String getMACAddress(){
		return this.MAC;
	}
	
	public String getIPAddress(){
		return this.IP;
	}
	
	//设置备注
	public void setRemark(String re){
		this.remark = re;
	}
	
	public void setMACAddress(String mac){
		this.MAC = mac;
	}
	
	public void setIPAddress(String ip){
		this.IP = ip;
	}
	
	public String toString(){
		return name + "," + (remark ==null? "NO-REMARK" :remark) + "," + (MAC ==null? "NO-MAC" :MAC) + "," + IP;
	}
	
}
