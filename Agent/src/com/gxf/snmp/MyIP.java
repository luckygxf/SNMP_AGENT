package com.gxf.snmp;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.gxf.gui.PicPlayer;


/**
 * 思路
 * 开一个接收线程
 * @author Administrator
 *
 */
public class MyIP {
	public static void main(String args[]){
		
	}
	
	/**
	 * 启动线程
	 */
	public void run(){
		this.new ReceiveThread().start();					//接收广播消息
	}
	
	/**
	 * 接收消息线程类
	 * @author Administrator
	 *
	 */
	private class ReceiveThread extends Thread{
		private final String REQUEST = "TELL ME YOUR IP";
		private final int PACKAGESIZE = 1024;
//		private final String MYIP = "210.38.235.184";
		
		public void run(){			
			while(!PicPlayer.getExit()){						//程序没有退出,线程不结束
				
				byte arrayOfByte[] = new byte[PACKAGESIZE];
				InetAddress inetAddress = null;
				try {
//					inetAddress = InetAddress.getByName("210.38.235.184");
					inetAddress = InetAddress.getLocalHost();
					System.out.println("my ip is : " + inetAddress.getHostAddress());
					int portNum = 10000;
					DatagramPacket datagramPacket = new DatagramPacket(arrayOfByte, arrayOfByte.length);
					DatagramSocket socket_rec = new DatagramSocket(portNum);
					socket_rec.receive(datagramPacket);
					
					String str_rec = new String(datagramPacket.getData(), 0,datagramPacket.getLength());
					System.out.println("str_rec = " + str_rec);
					//发送ip
					if(str_rec.equals(REQUEST)){
						
						InetAddress inetAddress_send = datagramPacket.getAddress();					//获取发送者的IP
						int portNum_send = datagramPacket.getPort();								//获取发送者的端口号
						System.out.println("src ip = " + inetAddress_send.getHostAddress() + "src postNum = " + portNum_send);
						
						//发送给GetAllIP
						String MYIP = InetAddress.getLocalHost().getHostAddress();
						arrayOfByte = MYIP.getBytes();	
						DatagramPacket datagramPacket_send = new DatagramPacket(arrayOfByte, arrayOfByte.length, inetAddress_send, portNum_send);
						DatagramSocket datagramSocket_send = new DatagramSocket();
						datagramSocket_send.send(datagramPacket_send);
//						datagramSocket_send.send(datagramPacket_send);					//发送两次，模拟多个客户端
						datagramSocket_send.close();
						socket_rec.close();
//						break;
					}//if
					socket_rec.close();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
	}
}
