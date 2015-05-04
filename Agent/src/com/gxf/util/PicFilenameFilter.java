package com.gxf.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * 图片名字过滤器
 * 支持图片格式jpg,jpeg,png,gif
 * @author Administrator
 *
 */
class PicFilenameFilter implements FilenameFilter{

	@Override
	public boolean accept(File dir, String name) {
		if(name.endsWith("jpg") || name.endsWith("jpeg")
				|| name.endsWith("png") || name.endsWith("gif") || name.endsWith("bmp"))
			return true;
		return false;
	}
	
}