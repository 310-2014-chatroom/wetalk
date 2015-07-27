package groupmanager;

import java.net.InetAddress;

public class group {
    private String groupName;
    private InetAddress groupIp;
    public group(String groupName,InetAddress groupIp){
    	this.groupIp=groupIp;
    	this.groupName=groupName;
    	
    }
    public String getGroupName(){
    	return this.groupName;
    }
    public InetAddress getGroupIp(){
    	return this.groupIp;
    }
   
}
