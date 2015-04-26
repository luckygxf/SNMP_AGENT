package com.gxf.snmp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class Snmp_api_sendTrap {
	
	/**
	 * 向主机ip发送trap消息
	 * @param ip
	 */
	public void sendTrap(String ip){
		//构造trap PDU
		PDU pdu = new PDU();
		VariableBinding vb = new VariableBinding(new OID("1.3.6.1.2.3377.10.1.1.1.1"), new OctetString("SnmpTrap"));		
		pdu.add(vb);
		pdu.setType(PDU.TRAP);
		
		//构造target
		String address_str = "udp:" + ip + "/16200";
		CommunityTarget target = new CommunityTarget();
		Address address = GenericAddress.parse(address_str);
		target.setAddress(address);
		target.setVersion(SnmpConstants.version2c);
		target.setCommunity(new OctetString("public"));
		target.setTimeout(2000);
		target.setRetries(2);
		TransportMapping<UdpAddress> tranportMapping = null;
		try {
			tranportMapping = new DefaultUdpTransportMapping();
			tranportMapping.listen();
		} catch (IOException e) {
			System.out.println("sendTrap(String ip) failed in Snmp_api.sendTrap(String ip)!");
			e.printStackTrace();
		}
		Snmp snmp = new Snmp(tranportMapping);
		try {
			ResponseEvent responseEvent = snmp.send(pdu, target);				//得到回复的pdu
			if(null != responseEvent){
				StringBuffer sb = new StringBuffer();
				PDU pdu_rec = responseEvent.getResponse();
				for(int i = 0;pdu_rec != null && i < pdu_rec.size(); i++){
					sb.append(vb.getVariable().toInt());
				}//for
				
				System.out.println(sb.toString());
			}
		} catch (IOException e) {
			System.out.println("snmp.send(pdu, target) failed in Snmp_api.sendTrap(String ip)!");
		}
		
		try {
			tranportMapping.close();
			snmp.close();																		//关闭tansportmapping and snmp 释放资源
		} catch (IOException e) {
			System.out.println("tranportMapping.close() and snmp.close() failed in Snmp_api.sendTrap(String ip)!");
			e.printStackTrace();
		}
		
	}
	public static void main(String args[]){
		Snmp_api_sendTrap snmp_api = new Snmp_api_sendTrap();
		InetAddress localAddress = null;
		try {
			localAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		String ip = localAddress.getHostAddress();
		
		snmp_api.sendTrap(ip);
	}
}
