package com.gxf.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * ��ȡ���ŷ���ʱ���˵�ѹ���ļ�
 * ����֧��.zipѹ���ļ�
 * @author Administrator
 *
 */
public class SolutionNameFilter implements FilenameFilter{

	//name���ļ�������Ҫ���˵�
	@Override
	public boolean accept(File dir, String name) {
		if(name.endsWith("zip"))							//���˵�ѹ���Ĳ��ŷ���
			return false;
		return true;
	}
	
}