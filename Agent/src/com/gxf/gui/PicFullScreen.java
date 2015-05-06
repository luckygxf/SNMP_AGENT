package com.gxf.gui;

import java.awt.Toolkit;
import java.io.File;
import java.util.Calendar;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.gxf.util.Config;
import com.gxf.util.PicFilter;
import com.gxf.util.PlayControl;
import com.gxf.util.Util;

public class PicFullScreen {
	private Display display;
	private Shell curShell;
	
	// �ļ��洢
	private File currentPic = null;
	private File[] pics;
	private int picPoint = 0;
	
	
	
	//��ʾͼƬ
	private Canvas canvas_picshow;		
	
	//��ǰ��Ҫ��ʾ��image
	private Image curImage;
	
	//ֹͣ����
	private boolean isStop = false;

	
	//���ŷ�������
	private Config config;
	private List<PlayControl> listOfPlayControl;
	
	//���ŷ�������
	public static String solutionName;
	public static String displayName;
	
	//������
	private Util util = new Util();
	//�Ƿ����µ��벥�ŷ���
	public static boolean isReloadSolution = false;
	
	//������в��ŷ������ļ���
	private final String DIC_NAME_PLAY_SOLUTIONS = "playSolutions";

	public PicFullScreen(String displayName, String solutionName){
		PicFullScreen.solutionName = solutionName;
		PicFullScreen.displayName = displayName;
	}
	public void open(){
		//��ʼ���ؼ�
		init();		
		curShell.open();
		
		while(!curShell.isDisposed()){
			if(!display.readAndDispatch()){
				display.sleep();
			}
		}
	}
	
	//��ȡ�����ò��ŷ�����
	public String getSolutionName() {
		return solutionName;
	}
	public void setSolutionName(String solutionName) {
		PicFullScreen.solutionName = solutionName;
	}
	/**
	 * �Կؼ����г�ʼ��
	 */
	public void init(){		
		display = Display.getDefault();
		curShell = new Shell(display, SWT.NO_TRIM);	
		curShell.setText("ȫ����ʾ");

		//��ȡ��Ļ��Ⱥ͸߶�
		int iSreenWith = Toolkit.getDefaultToolkit().getScreenSize().width;
		int iSreenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
		
		//����shell��Ⱥ͸߶�
		curShell.setSize(iSreenWith, iSreenHeight);
		
		//��Ӽ��̺���������
		curShell.addKeyListener(new KeyListenerImpl());		

		curShell.setLocation(0, 0);
		
		//����composite��canvas��ʾͼƬ
		Composite composite_pic = new Composite(curShell, SWT.NONE);
		composite_pic.setBounds(curShell.getBounds().x, curShell.getBounds().y, curShell.getBounds().width, curShell.getBounds().height);
		canvas_picshow = new Canvas(composite_pic, SWT.NONE);
		canvas_picshow.setBounds(curShell.getBounds().x, curShell.getBounds().y, curShell.getBounds().width, curShell.getBounds().height);
		canvas_picshow.addMouseListener(new MouseListenerImp());
		canvas_picshow.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent arg0) {
				if(curImage != null)
					arg0.gc.drawImage(curImage, 0, 0);
				
			}
		});		
		
		//���벥�ŷ���
		importPlaySolution(displayName, solutionName);
		
		//����curPic
		currentPic = pics[picPoint];	
				
		//�����Զ������߳�
		PlayPicThread picPlayThread = new PlayPicThread();
		picPlayThread.start();
	}
		
	/**
	 * ������������������Ҫ��Ϊ�˼���esc�˳�ȫ����ʾ
	 * @author Administrator
	 *
	 */
	class KeyListenerImpl implements KeyListener{

		@Override
		public void keyPressed(KeyEvent arg0) {
			if(arg0.keyCode == SWT.ESC)
			{
				//�˳�ȫ��������ǰ��shell��ʾ����
				curShell.dispose();
				//ֹͣȫ�������߳�
				setIsStop(true);
				//����ǰ��shell�ɼ�
				PicPlayer.curShell.setVisible(true);
			}
			
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/**
	 * ������Ҫ���ŵ�ͼƬ
	 * @param pics
	 */
	public void setPic(File pics[]){
		this.pics = pics;
	}
	
	/**
	 * ��picPoint��ʼ����
	 * @param picPoint
	 */
	public void setPicPoint(int picPoint){
		this.picPoint = picPoint;
	}
		
	/**
	 * ��ͼƬ��ʾ��������
	 */
	public void drawImage(){
		ImageData imageData = new ImageData(currentPic.getPath());
		int width = curShell.getBounds().width;
		int height = curShell.getBounds().height;
		imageData = imageData.scaledTo(width, height);
		curImage = new Image(curShell.getDisplay(), imageData);
		
		canvas_picshow.redraw();
		
	}
	
	/**
	 * �Զ�����ͼƬ�߳�
	 * @author Administrator
	 *
	 */
	class PlayPicThread extends Thread{
		public void run(){
			while(!getIsStop()){
				if(getIsReloadSolution()){							//���µ��벥�ŷ���
					System.out.println("���յ��²��ŷ��������¼��ز��ŷ���!");
					importPlaySolution(displayName, solutionName);
					setIsReloadSolution(false);						//����һֱ����	
				}
				picPoint = picPoint % pics.length;
				currentPic = pics[picPoint];			
				curShell.getDisplay();
				Display.getDefault().syncExec(new Runnable() {
					
					@Override
					public void run() {
						drawImage();
						
					}
				});
				try {
//					sleep((long)(config.getPlayTimeInterval() * 1000));
					//��ȡ���ż��
					int timeInterval = getTimeInterval(picPoint);
					if(timeInterval == -1)
					{
						picPoint++;
						continue;
					}
					sleep((long)(timeInterval * 1000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				picPoint++;
			}//while				
		}
	}
	
	//ͬ������isStop
	public synchronized void setIsStop(boolean isStop){
		this.isStop = isStop;
	}
	public synchronized boolean getIsStop(){
		return isStop;
	}
	
	/**
	 * ��������
	 * @author Administrator
	 *
	 */
	class MouseListenerImp implements MouseListener{
		
		//˫������˳�ȫ��
		@Override
		public void mouseDoubleClick(MouseEvent arg0) {
			//�˳�ȫ��������ǰ��shell��ʾ����
			curShell.dispose();
			//ֹͣȫ�������߳�
			setIsStop(true);
			//����ǰ��shell�ɼ�
			PicPlayer.curShell.setVisible(true);
		}

		@Override
		public void mouseDown(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseUp(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/**
	 * ���벥�ŷ���
	 */
	public void importPlaySolution(String displayName, String solutionName){		
		String projectPath = util.getCurrentProjectPath();
//		String filePath = projectPath + File.separator + DIC_NAME_PLAY_SOLUTIONS 
//							+ File.separator + solutionName;
		String filePath = projectPath + File.separator + DIC_NAME_PLAY_SOLUTIONS + File.separator + displayName
				+ File.separator + solutionName;
		//�Ƚ�ѹ�ļ�
		util.unzipSolution(displayName, solutionName);
		//�ļ���������ͼƬ,����File����
		File dirc = new File(filePath);
		pics = dirc.listFiles(new PicFilter());
		
		currentPic = pics[0];
		picPoint = 0;
		//��ȡ���ŷ�������
//		config = util.parseConfigXml(solutionName);
		listOfPlayControl = util.parseXml(displayName, solutionName);
		//��ʾͼƬ��������
//		drawImage(); 
		
	}
	//isReloadSolution get��set����
	public static synchronized boolean getIsReloadSolution() {
		return isReloadSolution;
	}
	public synchronized  static void  setIsReloadSolution(boolean isReloadSolution) {
		PicFullScreen.isReloadSolution = isReloadSolution;
	}
	
	/**
	 * ��ȡ��ͣ����ʱ��
	 * @param picIndex
	 * @return
	 * -1��ʾ���ܲ���
	 * >0��ʾ���ż���
	 */
	private int getTimeInterval(int picIndex){
		//Ĭ�Ͽ��Բ���
		PlayControl playControl = listOfPlayControl.get(picIndex);
		
		//1.�Ƚ����ڷ�Χ�Ƿ�valid
		//2.�Ƚ�ʱ�䷶Χ�Ƿ�valid
		//3.�����Ƿ�valid
		
		//�Ƚ�����+ʱ��
		java.util.Date curDate = new java.util.Date();
		String beginDateStr = playControl.getDateTimeStart().toString() + " " + playControl.getTimeStart();
		String afterDateStr = playControl.getDateTimeEnd().toString() + " " + playControl.getTimeEnd();
		
		java.sql.Timestamp beginDate = java.sql.Timestamp.valueOf(beginDateStr);
		java.sql.Timestamp afterDate = java.sql.Timestamp.valueOf(afterDateStr);
		
		if(!(curDate.after(beginDate) && curDate.before(afterDate))){
			return -1;
		}
		
		//�Ƚ�����
		String weekdaysStr = playControl.getWeekdays();
		//��ȡ��ǰ����
		Calendar c = Calendar.getInstance();
        int curWeekday = c.get(Calendar.DAY_OF_WEEK) - 1;
        if(curWeekday < 0)
        	curWeekday = 6;
        if(weekdaysStr.charAt(curWeekday) == '0')
        	return -1;
        
        //���ڡ�ʱ������ڶ�valid��ȡ���ż��ʱ�䷵��
        int timeInterval = playControl.getTimeInterval();
		
        return timeInterval;
	}
}
