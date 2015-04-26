package com.gxf.snmp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.gxf.intrefaces.Snmp_aip_interface;


/**
 * ��װsnmp v 2����Ϣ��ʽ�������ṩ�ӿ�
 * ʹ�ÿ�Դsnmp4j���з�װ
 * ����˼���udp 161�˿ڣ�����˼�������udp 162�˿�
 * snmp2c�ṩ����Ϣ��ʽ��Get,GetNext,Set,Reponse,Trap,GetBulk,
 * @author Administrator
 *
 */
public class Snmp_api implements Snmp_aip_interface{
	private Snmp snmp;
	private TransportMapping<?> transportMapping;
	private int timeOut;
	private int timesRetry;
	
	public Snmp_api(){
//		Thread thread_rec = new receiveTrapThread();
//		thread_rec.start();
//		listen();
	}
	
	private void init(){
		try {
			transportMapping = new DefaultUdpTransportMapping();
		} catch (IOException e) {
			System.out.println("init transportMapping failed!");
			System.out.println(e.getMessage());
		}
		snmp = new Snmp(transportMapping);
		timeOut = 1500;
		timesRetry = 2;
	}
	
	/**
	 * ����ip,oid����get��Ϣ
	 * @param ip
	 * @param oid
	 * @return
	 */
	public String get(String ip, String oid_str){
		init();
		String ret = "";	
		//����PDU
		PDU pdu = new PDU();
		OID oid = new OID(oid_str);
		VariableBinding vb = new VariableBinding(oid);
		pdu.add(vb);
		pdu.setType(PDU.GET);
		
		//����target
		String address_str = "udp:" + ip + "/161";
//		String address_str = "udp:210.38.235.184/161";
		Address address = GenericAddress.parse(address_str);
		OctetString name_com = new OctetString("public");
		CommunityTarget target = new CommunityTarget();
		target.setAddress(address);										//����address
		target.setRetries(timesRetry);
		target.setTimeout(timeOut);
		target.setVersion(SnmpConstants.version2c);						//���ð汾��
		target.setCommunity(name_com);									//����������
		
		try {
			this.transportMapping.listen();
		} catch (IOException e) {
			System.out.println("transportMapping.listen() failed!");
			System.out.println(e.getMessage());
		}
		ResponseEvent responseEvent = null;
		//������Ϣ
		try {
			responseEvent = snmp.send(pdu, target);
		} catch (IOException e) {
			System.out.println("snmp.send(pdu, target) failed");
			System.out.println(e.getMessage());
		}
		//��ȡ���
		if(responseEvent != null){
			PDU pdu_rep = responseEvent.getResponse();
			for(int i = 0; i < pdu_rep.size(); i++){
				VariableBinding vb_temp = pdu_rep.get(i);
				Variable var_temp = vb_temp.getVariable();
				ret += var_temp.toString();
			}//for
		}//if
		try {
			transportMapping.close();									//�ر����ڽ��еļ���				
		} catch (IOException e) {
			System.out.println("transportMapping.close() failed!");
			System.out.println(e.getMessage());
		}
		return ret;
	}
	
	
	/**
	 * ����ip,oid����ֵ����oid�ڵ��ֵ
	 * @param ip
	 * @param oid_str
	 * @param value_new
	 */
	public void set(String ip, String oid_str, String value_new){
		init();
		//����PDU
		PDU pdu = new PDU();
		OID oid = new OID(oid_str);
		VariableBinding vb = null;
		
		vb = new VariableBinding(oid, new OctetString(value_new));
		
		pdu.add(vb);
		pdu.setType(PDU.SET);
		
		//����target
		String address_str = "udp:" + ip + "/161";
		CommunityTarget target = new CommunityTarget();
		OctetString com_oct = new OctetString("public");
		target.setCommunity(com_oct);									//����������
		Address address = GenericAddress.parse(address_str);
		target.setAddress(address);										//����address
		target.setVersion(SnmpConstants.version2c); 					//���ð汾��
		target.setTimeout(1500);
		target.setRetries(timesRetry);
		
		//����pdu
		try {
			transportMapping.listen();
		} catch (IOException e) {
			System.out.println("listen failed in set pdu");
			System.out.println(e.getMessage());
		}
		try {
			snmp.send(pdu, target);
		} catch (IOException e) {
			System.out.println("send failed in send set message!");
			System.out.println(e.getMessage());
		} finally{
			try {
				transportMapping.close();								//�ر����ڽ��еļ���
			} catch (IOException e) {
				System.out.println("transportMapping.close() failed!");
				System.out.println(e.getMessage());
			}
		}
	}

	/**
	 * ����ip,oid����getNext��Ϣ
	 * @param ip
	 * @param oid_str
	 * @return
	 */
	public String getNext(String ip, String oid_str){
		init();
		String ret = "";
		//����PDU
		PDU pdu = new PDU();
		OID oid = new OID(oid_str);
		VariableBinding vb = new VariableBinding(oid);
		pdu.add(vb);
		pdu.setType(PDU.GETNEXT);
		
		//����target
		String address_str = "udp:" + ip + "/161";									//UDP��ip
		CommunityTarget target = new CommunityTarget();
		Address address = GenericAddress.parse(address_str);
		target.setCommunity(new OctetString("public"));
		target.setAddress(address);
		target.setTimeout(timeOut);
		target.setRetries(timesRetry);
		target.setVersion(SnmpConstants.version2c); 								//���ð汾��
		
		ResponseEvent responseEvent = null;
		
		try {
			this.transportMapping.listen();											//��ʼ����
		} catch (IOException e1) {
			System.out.println("transportMapping.listen() failed!");
			System.out.println(e1.getMessage());
		}
		
		//������Ϣ
		try {
			responseEvent = snmp.send(pdu, target);				
			if(responseEvent != null){												//��װ���ص���Ϣ
				PDU pdu_reponse = responseEvent.getResponse();
				for(int i = 0; pdu_reponse != null && i < pdu_reponse.size(); i++){
					VariableBinding vb_temp = pdu_reponse.get(i);
					Variable var_temp = vb_temp.getVariable();
					ret += var_temp.toString();
				}
			}
		} catch (IOException e) {
			System.out.println("snmp.send(pdu, target) failed! in getNext");
			e.printStackTrace();
		} finally{																	//�رռ���
			try {
				this.transportMapping.close();
				snmp.close();
			} catch (IOException e) {
				System.out.println("transportMapping.close() failed!");
				System.out.println(e.getMessage());
			}
		}//finally
		
		return ret;
	}

	/**
	 * ����ip,oid��ȡbulk��Ϣ
	 * @param ip
	 * @param oid_str
	 * @return
	 */
	public List<String> getBulk(String ip, String oid_str){
		init();
		List<String> ret = new ArrayList<String>();
		//����PDU 
		PDU pdu = new PDU();
		pdu.setType(PDU.GETBULK);
		pdu.setMaxRepetitions(200);
		pdu.setNonRepeaters(0);
		OID oid = new OID(oid_str);
		VariableBinding vb = new VariableBinding(oid);
		pdu.add(vb);
		
		//����target
		String address_str = "udp:" + ip + "/161";									//UDP��ip
		CommunityTarget target = new CommunityTarget();
		Address address = GenericAddress.parse(address_str);
		target.setCommunity(new OctetString("public"));
		target.setVersion(SnmpConstants.version2c);
		target.setAddress(address);
		target.setTimeout(timeOut);
		target.setRetries(timesRetry);
		
		try {																		//��ʼ����UDP�˿�
			this.transportMapping.listen();
		} catch (IOException e) {
			System.out.println("transportMapping.listen() failed!");
			System.out.println(e.getMessage());
		}
		
		//������Ϣ
		ResponseEvent responseEvent = null;
		try {
			responseEvent = this.snmp.send(pdu, target);
		} catch (IOException e) {
			System.out.println(".snmp.send(pdu, target) failed! in getBulk");
			e.printStackTrace();
		}
		
		if(responseEvent != null){
			PDU pdu_rec = responseEvent.getResponse();
			
			System.out.println(pdu_rec.size());											//���Ϊ0,˵��ǰ��û�л�ȡ������
			for(int i = 0; i < pdu_rec.size(); i++){
				String str_temp = "";
				VariableBinding vb_temp = pdu_rec.get(i);
				str_temp += vb_temp.getOid() + " ";
				str_temp += vb_temp.getVariable().toString();
				ret.add(str_temp);
			}//for
		}//if
		
		try {
			this.transportMapping.close();
		} catch (IOException e) {
			System.out.println("transportMapping.close() failed!");
			System.out.println(e.getMessage());
		}
		
		return ret;
	}
	
	/**
	 * ����trap��Ϣ�߳�
	 * @author Administrator
	 *
	 */
	@SuppressWarnings("unused")
	private class receiveTrapThread extends Thread{
		public void run(){
			System.out.println("start run thread..");
			
			try {
				transportMapping.listen();
			} catch (IOException e) {
				System.out.println("listen failed in receiveTrapThread.run()!");
				e.printStackTrace();
			}
			CommandResponder commandResponder = new CommandResponder(){
				@Override
				public void processPdu(CommandResponderEvent event) {
					System.out.println("enter processPdu()...");
					PDU pdu_receive = event.getPDU();
					StringBuffer sb = new StringBuffer();
					
					for(int i = 0; i < pdu_receive.size(); i++){
						VariableBinding vb_temp = pdu_receive.get(i);
						sb.append(vb_temp.getVariable().toString());
					}//for
					System.out.println(sb.toString());
				}
				
			};
			snmp.addCommandResponder(commandResponder);
		}
	}
	
	public synchronized void listen(){
		System.out.println("start listen..");
		try {
			this.wait();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
	}

	
	/* (non-Javadoc)
	 * @see com.gxf.snmp.interfaces.Snmp_aip_interface#getV3(java.lang.String, java.lang.String)
	 */
	@Override
	public String getV3(String ip, String oid_str) {
		Snmp snmp = null;
		try {
			snmp = new Snmp(new DefaultUdpTransportMapping());
		
		USM usm = new USM(SecurityProtocols.getInstance(),
						  new OctetString(MPv3.createLocalEngineID()), 0);
		SecurityModels.getInstance().addSecurityModel(usm);
		
		snmp.listen();
		} catch (IOException e) {
			e.printStackTrace();
		}
		UsmUser user = new UsmUser(
					new OctetString("luckygxf"),
					AuthMD5.ID, new OctetString("luckygxf"),
					PrivDES.ID, new OctetString("luckygxf")
				);
		OctetString contextEngineId = new OctetString("0002651100[02]");
		snmp.getUSM().addUser(new OctetString("nmsAdmin"), new OctetString("0002651100"), user);
		snmp.getUSM().addUser(new OctetString("luckygxf"), user);
		String address_str = ip + "/161";	
//		System.out.println("address_str = " + address_str);
		Address address = new UdpAddress(address_str);
		UserTarget target = new UserTarget();
		target.setVersion(SnmpConstants.version3);
		target.setAddress(address);
		target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
		target.setSecurityName(new OctetString("luckygxf"));
		target.setTimeout(2000);
		target.setRetries(1);
		
		ScopedPDU pdu = new ScopedPDU();
		pdu.setType(PDU.GET);
		pdu.setContextEngineID(contextEngineId);
		VariableBinding vb = new VariableBinding(new OID(oid_str));
		pdu.add(vb);
		
		ResponseEvent responseEvent = null;
		try {
			responseEvent = snmp.send(pdu, target);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		StringBuffer sb = new StringBuffer();						//���ص��ַ���
		if(null != responseEvent){
			PDU pdu_rec = responseEvent.getResponse();
//			System.out.println("pdu_rec.size() = " + pdu_rec.size());
			if(pdu_rec != null){
				Vector<? extends VariableBinding> vbs = pdu_rec.getVariableBindings();
				for(VariableBinding vb_temp : vbs){
					sb.append(vb_temp.getVariable().toString());
				}//for
			}
		}//if
		
		try {
			snmp.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
		
	}
}
