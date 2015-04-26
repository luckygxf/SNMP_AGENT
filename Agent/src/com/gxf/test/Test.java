package com.gxf.test;

import com.gxf.util.ReceiveImage;


public class Test {

	public static void main(String[] args) {
		ReceiveImage receiveImage = new ReceiveImage();
		receiveImage.listen();
	}
}

