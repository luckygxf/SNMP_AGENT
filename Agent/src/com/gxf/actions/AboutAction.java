package com.gxf.actions;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import com.gxf.gui.PicPlayer;


public class AboutAction extends Action {
	public AboutAction(){
		setText("����ϵͳ");
	}

	@Override
	public void run() {
		MessageBox messageBox = new MessageBox(PicPlayer.curShell, SWT.OK | SWT.ICON_INFORMATION);
		messageBox.setText("����ϵͳ");
		messageBox.setMessage("Developed by GXF!");
		messageBox.open();
	}
	
}
