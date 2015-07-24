package File;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

import javax.swing.JOptionPane;
public class FileSend extends Thread {
	private final int FILEPORT = 4567;
	private String IPAddress;
	private File sendfile;
		
	public FileSend(String ip, File file){
		this.IPAddress = ip;
		this.sendfile = file;
	}
	
	public void run(){
		try {
			sendFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendFile() throws IOException{
		Socket s = null;	
		FileInputStream fis;
		OutputStream os;
		String reply = null;

		byte[] buf = new byte[4*1024];		//读取回复时用的缓存,
		byte[] buffer = new byte[4*1024];	//发送文件的缓存
		/**与服务器建立连接*/
		try{
			s = new Socket(IPAddress,FILEPORT);
			}catch(IOException e){
				JOptionPane.showMessageDialog(null, "未连接到服务器！");
			}
	    	fis = new FileInputStream(sendfile);
	    	os = s.getOutputStream();
	    	// 1. Send the filename and length to the receiver
	    	os.write((sendfile.getName()+"#"+fis.available()).getBytes());
	    	os.flush();
	    
	    	// 2. Wait for a reply from the receiver
	    	int length = 0;
	    	InputStream is = s.getInputStream();//读取回复ok时需要socket的输入流
	    	while(length<=0){
	    		length = is.read(buf);
	    		if(length>0)
	    			reply= new String(buf,0,length);
	    	}
	    	// 3. Send the content of the file
	    	if(reply.equals("ok")){
	    		int size = 0;//记录每次读取文件的大小
	    		while((size =fis.read(buffer))!=-1){
	    			os.write(buffer,0,size);
	    			os.flush();
	    		}
	    		JOptionPane.showMessageDialog(null, "发送完成！！！");
	    	}else{
	    		JOptionPane.showMessageDialog(null, "发送中断！！！");
	    	}
	    	
	    	if(s != null)
	    		s.close();
	}
	
}
