package wetalk;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Queue;

import message.BaseMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import usermanager.User;

//下半部
	public class WeTalkDownPart extends Thread{
		protected static Logger log;
		static int ListenPort = 3726;
		User ME;
		
		static boolean isRunning = true;	//线程运行标志
		
		Queue<User> comeQueue;
		Queue<User> goQueue;
		
		public void run(){
			Online();
		}
		
		public WeTalkDownPart(Queue<User> comequeue, Queue<User> goqueue, User me){
			log = LoggerFactory.getLogger(WeTalkDownPart.class);
			this.comeQueue = comequeue;
			this.goQueue = goqueue;
			this.ME = me;			
		}
		
		//用户上线
		public void Online(){
			log.info("Down part service start.");
		
			DatagramSocket asksocket = null;
			DatagramPacket pkt = null;
			
			try{
				//构造SeckServer消息
				BaseMessage seckservermsg = new BaseMessage(BaseMessage.SECK_SERVER_MESSAGE);
				seckservermsg.setUser(this.ME);
				
		        byte[] msg = seckservermsg.srialize();
		        
		        pkt = new DatagramPacket(msg,msg.length,InetAddress.getByName("255.255.255.255"),ListenPort);
		        asksocket = new DatagramSocket(ListenPort);
		        asksocket.send(pkt);            
		        
		        Working(asksocket);
		        asksocket.close();
		        return;
		        
			} catch (Exception e){
				e.printStackTrace();
			} finally{
				if(!asksocket.isClosed())
					asksocket.close();
			}
		}	
		
		//开始正常工作
		protected void Working(DatagramSocket asksocket){
			DatagramSocket socket = asksocket;
			DatagramPacket pkt = null;
			
			User peer;
		    byte[] buffer = new byte[1024];
		    pkt = new DatagramPacket(buffer,buffer.length);
			while(isRunning){
				try{
					socket.receive(pkt);
					byte[] pktdata = pkt.getData();
					BaseMessage recvmsg = BaseMessage.deserialize(pktdata);
			        peer = recvmsg.getUser();
					switch(recvmsg.getType()){
					case BaseMessage.SECK_SERVER_MESSAGE:
				        //收到此消息，只是更新自己的UserStore				        
				        //去除自己的广播
				        if(peer.getName().equals(ME.getName())&& peer.getIPAddress().equals(ME.getIPAddress()) )
				        	break;		
				        
				        //修正对端的IP				        
				        peer.setIPAddress(pkt.getAddress().getHostAddress());	//忽略用户自己填加的IP
						//因为用户自己添加的ip可能不可达。
				        				        
				        comeQueue.add(peer);	//添加到come队列中
						log.info("A new user coming:{}",peer.toString());
				        
				        //回复SECK_SERVER_ACK_MESSAGE消息给新用户
						BaseMessage ackmsg = new BaseMessage(BaseMessage.SECK_SERVER_ACK_MESSAGE);
						ackmsg.setUser(this.ME);
						
				        byte msg[] = ackmsg.srialize();
				        
				        pkt = new DatagramPacket(msg,msg.length,InetAddress.getByName(peer.getIPAddress()),ListenPort);
				        Thread.sleep( (long)(500*Math.random()) );//等待一个随机时间
				        socket.send(pkt); 
				        
				        break;		
				        
					case BaseMessage.SECK_SERVER_ACK_MESSAGE:    
				        //修正对端的IP
				        peer.setIPAddress(pkt.getAddress().getHostAddress());
						comeQueue.add(peer);	//添加到come队列中
						log.info("Receive a user's reply:{}",peer.toString());
			        	break;
				        
					case BaseMessage.USER_GO_MESSAGE:					
				        if(peer.getName().equals(ME.getName())&& peer.getIPAddress().equals(ME.getIPAddress()) )
				        	break;	
				        //修正对端的IP
				        peer.setIPAddress(pkt.getAddress().getHostAddress());
						goQueue.add(peer);	//添加到come队列中 
						log.info("a user left:{}",peer.toString());
						break;
					default:
						break;

					}
				} catch(Exception e) {
					log.error("Wrong when Down part working!",e);
					socket.close();
				}
			}

			//线程退出
			log.info("Down Part exit.");
			System.exit(0);		
		}
		
		public static void Start(Queue<User> comequeue, Queue<User> goqueue, User me){
			WeTalkDownPart down = new WeTalkDownPart(comequeue, goqueue, me);
			down.start();			
		}
		
		//用户下线。当前台退出时，后台发生送下线广播
		public static void Offline(User me){				
			DatagramSocket asksocket = null;
			DatagramPacket pkt = null;
			
			try{
				//构造UserGo消息
				BaseMessage usergomsg = new BaseMessage(BaseMessage.USER_GO_MESSAGE);
				usergomsg.setUser(me);
				
		        byte msg[] = usergomsg.srialize();
		        
		        //发送广播消息
		        pkt = new DatagramPacket(msg,msg.length,InetAddress.getByName("255.255.255.255"),ListenPort);
		        asksocket = new DatagramSocket();
		        asksocket.send(pkt);            
		        
		        asksocket.close();
		        return;
		        
			} catch (Exception e){
				log.debug("Wrong when send offline message.",e);
			} finally{
				if(!asksocket.isClosed())
					asksocket.close();
				isRunning = false;
			}
		}	
			
	}
