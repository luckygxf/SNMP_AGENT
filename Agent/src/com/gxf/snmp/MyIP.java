package com.gxf.snmp;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.gxf.gui.PicPlayer;


/**
 * ˼·
 * ��һ�������߳�
 * @author Administrator
 *
 */
public class MyIP {
	public static void main(String args[]){
		
	}
	
	/**
	 * �����߳�
	 */
	public void run(){
		this.new ReceiveThread().start();					//���չ㲥��Ϣ
	}
	
	/**
	 * ������Ϣ�߳���
	 * @author Administrator
	 *
	 */
	private class ReceiveThread extends Thread{
		private final String REQUEST = "TELL ME YOUR IP";
		private final int PACKAGESIZE = 1024;
//		private final String MYIP = "210.38.235.184";
		
		public void run(){			
			while(!PicPlayer.getExit()){						//����û���˳�,�̲߳�����
				
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
					//����ip
					if(str_rec.equals(REQUEST)){
						
						InetAddress inetAddress_send = datagramPacket.getAddress();					//��ȡ�����ߵ�IP
						int portNum_send = datagramPacket.getPort();								//��ȡ�����ߵĶ˿ں�
						System.out.println("src ip = " + inetAddress_send.getHostAddress() + "src postNum = " + portNum_send);
						
						//���͸�GetAllIP
						String MYIP = InetAddress.getLocalHost().getHostAddress();
						arrayOfByte = MYIP.getBytes();	
						DatagramPacket datagramPacket_send = new DatagramPacket(arrayOfByte, arrayOfByte.length, inetAddress_send, portNum_send);
						DatagramSocket datagramSocket_send = new DatagramSocket();
						datagramSocket_send.send(datagramPacket_send);
//						datagramSocket_send.send(datagramPacket_send);					//�������Σ�ģ�����ͻ���
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
