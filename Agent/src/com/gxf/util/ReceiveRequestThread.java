package com.gxf.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 用于接收request线程，判断agent是否可达
 * 这里用的是TCP
 * @author Administrator
 *
 */
public class ReceiveRequestThread extends Thread {
	//端口号
	private final int NUM_PORT = 16202;
	
	@Override
	public void run(){
		try {
			ServerSocket localSocket = new ServerSocket(NUM_PORT);
			while(true){
				System.out.println("等待连接");
				//获取主动连接的socket
				Socket remote = localSocket.accept();
				//连接成功
				System.out.println(remote.getInetAddress() + "连接成功!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
