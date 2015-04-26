package com.gxf.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * 获取播放方案时过滤掉压缩文件
 * 这里支持.zip压缩文件
 * @author Administrator
 *
 */
public class SolutionNameFilter implements FilenameFilter{

	//name是文件名，需要过滤的
	@Override
	public boolean accept(File dir, String name) {
		if(name.endsWith("zip"))							//过滤到压缩的播放方案
			return false;
		return true;
	}
	
}