package com.gxf.intrefaces;

import java.util.List;

public interface Snmp_aip_interface {
	/**
	 * ����ip,oid����get��Ϣ
	 * @param ip
	 * @param oid
	 * @return
	 */
	public String get(String ip, String oid_str);
	
	/**
	 * ����ip,oid����ֵ����oid�ڵ��ֵ
	 * @param ip
	 * @param oid_str
	 * @param value_new
	 */
	public void set(String ip, String oid_str, String value_new);
	
	/**
	 * ����ip,oid����getNext��Ϣ
	 * @param ip
	 * @param oid_str
	 * @return
	 */
	public String getNext(String ip, String oid_str);
	
	/**
	 * ����ip,oid��ȡbulk��Ϣ
	 * @param ip
	 * @param oid_str
	 * @return
	 */
	public List<String> getBulk(String ip, String oid_str);
	
	/**
	 * snmp v3��get api
	 * @param ip
	 * @param oid_str
	 * @return
	 */
	public String getV3(String ip, String oid_str);
}
