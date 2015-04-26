package com.gxf.actions;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import com.gxf.gui.PicPlayer;


public class AboutAction extends Action {
	public AboutAction(){
		setText("关于系统");
	}

	@Override
	public void run() {
		MessageBox messageBox = new MessageBox(PicPlayer.curShell, SWT.OK | SWT.ICON_INFORMATION);
		messageBox.setText("关于系统");
		messageBox.setMessage("Developed by GXF!");
		messageBox.open();
	}
	
}
