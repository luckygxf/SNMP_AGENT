package com.gxf.util;

import java.io.File;
import java.io.FilenameFilter;


//ÎÄ¼ş¹ıÂËÆ÷
public class PicFilter implements FilenameFilter
{
	@Override
	public boolean accept(File dir, String name)
	{
		if (name.endsWith("jpg") || name.endsWith("gif")||name.endsWith("png"))
			return true;
		else
			return false;
	}
}
