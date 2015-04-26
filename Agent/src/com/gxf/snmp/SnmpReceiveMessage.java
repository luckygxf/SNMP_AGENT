package com.gxf.snmp;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Vector;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;


public class SnmpReceiveMessage implements CommandResponder{
	private Snmp snmp;
	private ThreadPool threadPool;					//���ڽ���trap��Ϣ���̳߳�,ͨ���鿴Դ����Կ����̳߳���ʵ
	@SuppressWarnings("rawtypes")
	private TransportMapping transportMapping;
	private MessageDispatcher messageDispatcher;
	private Address listenAddress;
	
	public void run(){
		init();
		snmp.addCommandResponder(this);
	}
	
	@Override
	public void processPdu(CommandResponderEvent event) {
		if(null != event && null != event.getPDU()){
			PDU pdu_rev = event.getPDU();
			@SuppressWarnings("unchecked")
			Vector<VariableBinding> vbs = (Vector<VariableBinding>) pdu_rev.getVariableBindings();
			for(int i = 0; i < vbs.size(); i++){
				VariableBinding vb = vbs.get(i);
				System.out.println(vb.getOid() + ":" + vb.getVariable().toString());
			}
		}
		
	}
	
	
	/**
	 * ��ʼ����Ա����
	 */
	private void init(){
		threadPool = ThreadPool.create("Trap", 2);	//���������̵߳��̳߳�
		messageDispatcher = new MultiThreadedMessageDispatcher(threadPool, 
			new MessageDispatcherImpl());			
		try {
			InetAddress localAddress = InetAddress.getLocalHost();	//��ȡ����ip
			String url = "udp:" + localAddress.getHostAddress() + "/16200";	//����162�˿�
			System.out.println("url = " + url);
			listenAddress = GenericAddress.parse(url);
			transportMapping = new DefaultUdpTransportMapping((UdpAddress)listenAddress);
		} catch (IOException e) {
			e.printStackTrace();
		}		//ʹ��Ĭ�ϵ�UDP
		
		snmp = new Snmp(messageDispatcher, transportMapping);
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());
		USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3
				.createLocalEngineID()), 0);
		SecurityModels.getInstance().addSecurityModel(usm);
		try {
			snmp.listen();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	public static void main(String args[]){
//		MultiThreadedMessageTrapReceiver trapReceiver = new MultiThreadedMessageTrapReceiver();
//		System.out.println("��ʼ����trap��Ϣ");
//		trapReceiver.run();
//		System.out.println("afer run");
//		
//	}
}
