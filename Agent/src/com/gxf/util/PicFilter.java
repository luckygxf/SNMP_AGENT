package com.gxf.util;

import java.io.File;
import java.io.FilenameFilter;


//�ļ�������
public class PicFilter implements FilenameFilter
{
	@Override
	public boolean accept(File dir, String name)
	{
		if (name.endsWith("jpg") || name.endsWith("gif")||name.endsWith("png") || name.endsWith("bmp"))
			return true;
		else
			return false;
	}
}
