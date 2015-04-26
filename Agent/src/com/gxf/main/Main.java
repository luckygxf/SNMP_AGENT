package com.gxf.main;

import com.gxf.snmp.MyIP;
import com.gxf.snmp.SnmpReceiveMessage;
import com.gxf.util.ReceiveImage;

public class Main {
	public static void main(String[] args) {
		MyIP myIp = new MyIP();
		SnmpReceiveMessage messageReceiver = new SnmpReceiveMessage();
		myIp.run();											//启动接收广播消息线程
		messageReceiver.run();								//启动消息接收器，这里监听的是16200端口
		ReceiveImage receiveImage = new ReceiveImage();
		receiveImage.listen();								//监听16201端口接收图片
	}

}
