package com.gxf.main;

import com.gxf.snmp.MyIP;
import com.gxf.snmp.SnmpReceiveMessage;
import com.gxf.util.ReceiveImage;

public class Main {
	public static void main(String[] args) {
		MyIP myIp = new MyIP();
		SnmpReceiveMessage messageReceiver = new SnmpReceiveMessage();
		myIp.run();											//�������չ㲥��Ϣ�߳�
		messageReceiver.run();								//������Ϣ�������������������16200�˿�
		ReceiveImage receiveImage = new ReceiveImage();
		receiveImage.listen();								//����16201�˿ڽ���ͼƬ
	}

}
