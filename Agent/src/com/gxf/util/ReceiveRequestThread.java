package com.gxf.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ���ڽ���request�̣߳��ж�agent�Ƿ�ɴ�
 * �����õ���TCP
 * @author Administrator
 *
 */
public class ReceiveRequestThread extends Thread {
	//�˿ں�
	private final int NUM_PORT = 16202;
	
	@Override
	public void run(){
		try {
			ServerSocket localSocket = new ServerSocket(NUM_PORT);
			while(true){
				System.out.println("�ȴ�����");
				//��ȡ�������ӵ�socket
				Socket remote = localSocket.accept();
				//���ӳɹ�
				System.out.println(remote.getInetAddress() + "���ӳɹ�!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
