package File;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class FileReceive extends Thread{	
	Socket s = null;
	InputStream is = null; 
	OutputStream os = null;
	
	public FileReceive(Socket socket){
		this.s = socket;
	}
	
	public void run(){
		startReceive();
	}
	  
	public void startReceive(){	
		byte[] buf= new byte[1024 * 4];
		int len = 0;
		try{		  
			is = s.getInputStream();
			// 1. Wait for a sender to transmit the filename
			len = is.read(buf);			  
		} catch (IOException e) {
	        e.printStackTrace();
	    }

		  String filess,filename;
		  int filesize;
		  filess = new String(buf,0,len);
		  StringTokenizer t =new StringTokenizer(filess,"#");
		  filename = t.nextToken();//获取文件名
		  filesize =new Integer(t.nextToken()).intValue();//获取文件大小
		  int selection=JOptionPane.showConfirmDialog(null, ("是否接收文件: "+filename),"提示",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
		  try {
			  if(selection==JOptionPane.OK_OPTION){
				  // 2. Send an reply containing OK to the sender
				  os = s.getOutputStream();
				  os.write((new String("ok")).getBytes());	
				  savefilebutten_event(filename, filesize);				  
			  }else {
				  if(is != null)
					  is.close();
			  }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	  
	 }

	  private void savefilebutten_event(String name, int totalsize){
		File file;
		FileOutputStream fos = null;
		  
		JFileChooser dlg = new JFileChooser();
		dlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		dlg.setDialogTitle("保存文件"); 
		int result = dlg.showSaveDialog(null);  // 打"开保存文件"对话框
		if (result == JFileChooser.APPROVE_OPTION) {
			String pathname = dlg.getSelectedFile().toString() + "\\" + name;
				
			// 3. Receive the contents of the file
			file = new File(pathname);
			if(!file.exists()){
				 try{
					file.createNewFile();
				}catch(IOException e){
					JOptionPane.showMessageDialog(null,"创建文件失败！");
				}
			}else{
				JOptionPane.showMessageDialog(null, "本路径已存在相同文件， 进行覆盖?");
			}
			
			try{					  
				fos = new FileOutputStream(file);/**将文件包装到文件输出流对象中*/
				int size=0;//记录每次接收的文件长度
				long count=0;//记录已经收到的文件长度
				byte[] buffer= new byte[1024 * 4];
					  
				while(count< totalsize ){
					size= is.read(buffer);/**从输入流中读取一个数据包*/
					fos.write(buffer,0,size);/**将刚刚读取的数据包写到本地文件中去*/
					fos.flush();
					count+=size;/**将已接收到文件的长度+size*/
				}
				JOptionPane.showMessageDialog(null, "接收完成！！！");
				  
			}catch(FileNotFoundException e){
				JOptionPane.showMessageDialog(null, "服务器写文件失败");
			}catch (IOException e) {
			    JOptionPane.showMessageDialog(null, "服务器：客户端断开连接");
			}finally{
			    /**
			     * 将打开的文件关闭
			     * 如有需要，也可以在此关闭socket连接
			     * 
			    */
				try {
					if(s != null)
						s.close();
					if(fos != null )
						fos.close();
				} catch (IOException e) {
						e.printStackTrace();
				}		            
			}
	} else {
				
	}
	  
	}
}

