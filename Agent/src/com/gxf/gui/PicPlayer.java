package com.gxf.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Canvas;

import com.gxf.actions.AboutAction;
import com.gxf.actions.ImportAction;
import com.gxf.snmp.MyIP;
import com.gxf.snmp.SnmpReceiveMessage;
import com.gxf.util.Config;
import com.gxf.util.PicFilter;
import com.gxf.util.PlayControl;
import com.gxf.util.ReceiveImage;
import com.gxf.util.ReceiveRequestThread;
import com.gxf.util.SolutionNameFilter;
import com.gxf.util.Util;

import org.eclipse.swt.widgets.Combo;

public class PicPlayer extends ApplicationWindow {
	//���ײ��ؼ�
	private Button btn_pre;
	private Button btn_next;
	private Button btn_attr;
	private Button btn_realsize;
	private Button btn_fullscreen;
	
	//����ϲ��ֿؼ�
	private Label lb_playtimeinterval_icon;
	private Text txt_playtime_interval;
	private Button btn_play;
	private Button btn_stop;
	private Button btn_continue;
	private Label lb_curPicName;
	private Combo combo_playSolution;
	private Combo combo_display;
	
	// �ļ��洢
	static private File currentPic = null;
	static private File[] pics;
	static private int picPoint = 0;
	
	static boolean isForward = true;
	//��ʾͼƬ
	private Canvas canvas_picshow;			
	
	//��ǰ����shell
	public static Shell curShell;
	
	//��ǰ��Ҫ��ʾ��image
	private Image curImage;
	
	//������
	static private ScrolledComposite scrolledComposite_top;
	private Composite composite_bottom;
	private Composite composite_menu;
	
	//�Ƿ��벥�ŷ���
	private boolean isImportSolution = false;
	
	//������
	private Util util = new Util();
	private Label lb_curPicIcon;
	
	//��ͣ����
	private static boolean isStop = false;
	
	//ȫ����ʾshell
	private PicFullScreen picFullScreen;
	
	//�����˳����߳̽�����־
	private static boolean exit = false;
	
	//������в��ŷ������ļ���
	private final String DIC_NAME_PLAY_SOLUTIONS = "playSolutions";
	
	//��ʾ�������ŷ������ƺ�����
//	public static Config config;
	public static String solutionName;
	public static String displayName;
	
	private List<PlayControl> listOfPlayControl = new ArrayList<PlayControl>();
	
	/**
	 * Create the application window.
	 */
	public PicPlayer() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	/**
	 * Create contents of the application window.
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		
		scrolledComposite_top = new ScrolledComposite(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite_top.setBounds(0, 38, 856, 410);
		scrolledComposite_top.setExpandHorizontal(true);
		scrolledComposite_top.setExpandVertical(true);
		
		canvas_picshow = new Canvas(scrolledComposite_top, SWT.NONE);
		scrolledComposite_top.setContent(canvas_picshow);
		scrolledComposite_top.setMinSize(canvas_picshow.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		composite_bottom = new Composite(container, SWT.NONE);
		composite_bottom.setBounds(0, 447, 856, 32);
		
		//����������水ť
		btn_pre = new Button(composite_bottom, SWT.NONE);
		btn_pre.setBounds(269, 10, 60, 22);
		btn_pre.setText("��һ��");
		btn_pre.addSelectionListener(new ButtonSelectionAdapter());
		
		
		btn_next = new Button(composite_bottom, SWT.NONE);		
		btn_next.setText("��һ��");
		btn_next.setBounds(357, 10, 60, 22);
		btn_next.addSelectionListener(new ButtonSelectionAdapter());
		
		btn_attr = new Button(composite_bottom, SWT.NONE);
		btn_attr.setText("����");
		btn_attr.setBounds(447, 10, 60, 22);
		btn_attr.addSelectionListener(new ButtonSelectionAdapter());
		
		//ʵ�ʴ�С����ʱ��ʹ����������
		btn_realsize = new Button(composite_bottom, SWT.NONE);
		btn_realsize.setText("ʵ�ʴ�С");
		btn_realsize.setBounds(518, 10, 60, 22);
		btn_realsize.addSelectionListener(new ButtonSelectionAdapter());
		btn_realsize.setVisible(false);
		
		//ȫ����ʾ
		btn_fullscreen = new Button(composite_bottom, SWT.NONE);
		btn_fullscreen.setText("ȫ����ʾ");
		btn_fullscreen.setBounds(547, 10, 60, 22);
		btn_fullscreen.addSelectionListener(new ButtonSelectionAdapter());
		
		//������沿��
		composite_menu = new Composite(container, SWT.NONE);
		composite_menu.setBounds(0, 0, 856, 38);
		
		lb_playtimeinterval_icon = new Label(composite_menu, SWT.NONE);
		lb_playtimeinterval_icon.setBounds(0, 10, 72, 12);
		lb_playtimeinterval_icon.setText("����ʱ����");
		
		//�������ļ����Ʋ���Ч��������ֻ����ʾ���ã����ܱ༭
		txt_playtime_interval = new Text(composite_menu, SWT.BORDER);
		txt_playtime_interval.setBounds(78, 7, 29, 18);
		txt_playtime_interval.setEnabled(false);
		
		Label lb_seconde_icon = new Label(composite_menu, SWT.NONE);
		lb_seconde_icon.setBounds(118, 10, 19, 12);
		lb_seconde_icon.setText("��");
		
		//���Ű�ť
		btn_play = new Button(composite_menu, SWT.NONE);
		btn_play.setBounds(423, 7, 64, 22);
		btn_play.setText("����");
		btn_play.addSelectionListener(new ButtonSelectionAdapter());
		
		//��ͣ����
		btn_stop = new Button(composite_menu, SWT.NONE);
		btn_stop.setBounds(493, 7, 64, 22);
		btn_stop.setText("��ͣ����");
		btn_stop.addSelectionListener(new ButtonSelectionAdapter());
		
		//�������Ű�ť
		btn_continue = new Button(composite_menu, SWT.NONE);
		btn_continue.setText("��������");
		btn_continue.setBounds(563, 7, 64, 22);		
		btn_continue.addSelectionListener(new ButtonSelectionAdapter());
		
		//��ʾ��ǰ����ͼƬ������
		lb_curPicName = new Label(composite_menu, SWT.NONE);
		lb_curPicName.setBounds(720, 12, 136, 12);
		lb_curPicName.setText("New Label");
		
		lb_curPicIcon = new Label(composite_menu, SWT.NONE);
		lb_curPicIcon.setBounds(633, 12, 88, 12);
		lb_curPicIcon.setText("��ǰ����ͼƬ:");
		
		Label lb_playSolution = new Label(composite_menu, SWT.NONE);
		lb_playSolution.setBounds(271, 8, 54, 12);
		lb_playSolution.setText("���ŷ���");
		
		//���ŷ����б�
		combo_playSolution = new Combo(composite_menu, SWT.NONE);
		combo_playSolution.setBounds(331, 7, 86, 20);
		
		Label lb_display = new Label(composite_menu, SWT.NONE);
		lb_display.setBounds(134, 10, 45, 12);
		lb_display.setText("��ʾ��");
		
		combo_display = new Combo(composite_menu, SWT.NONE);
		combo_display.setBounds(179, 7, 86, 20);
		//��ȡ��ǰ����shell
		curShell = parent.getShell();
		
		//�Կؼ����г�ʼ��
		init();
		
		return container;
	}
	
	/**
	 * �Կؼ����г�ʼ������ʼ���ؼ���С����ʼ����ʾ������
	 */
	public void init(){
		canvas_picshow.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent arg0) {
				if(curImage != null)
					arg0.gc.drawImage(curImage, 0, 0);
				
			}
		});
		//���ð�ť״̬
		setButtonEnableOrDis();
		
		//����Ӧ�ó���logo
		String logoName = "logo.png";
		String logoPath = util.getCurrentProjectPath() + "\\images\\" + logoName;
		ImageData logoImageData = new ImageData(logoPath);
		Image logoImage = new Image(curShell.getDisplay(), logoImageData);
		curShell.setImage(logoImage);
		
		//����Ĭ�ϲ���ʱ����Ϊ1s
		txt_playtime_interval.setText(String.valueOf(1));
		
		//��ǰ�ļ������ɼ�
		lb_curPicName.setVisible(false);
		
		//��ǰshellע��رմ����¼�
		curShell.addListener(SWT.Close, new ShellCloseListener());
		//Ϊ���ŷ��������б�ע������¼�
		combo_playSolution.addSelectionListener(new SolutionChangeListener());
		//��ʼ����ʾ����Ϣ
		String displayNames[] = getDisplayNames();
		
		//��ʼ����ʾ����Ϣ
		if(displayNames.length != 0){
			combo_display.setItems(displayNames);
			combo_display.select(0);
		}
		
		//��ʼ�����ŷ���
		String playSolutions[] = getSolutions();
		if(playSolutions.length != 0){
			combo_playSolution.setItems(playSolutions);
			combo_playSolution.select(0);
		}
		
		//û�п��Բ��ŵĲ��ŷ���
		if(combo_display.getItemCount() == 0 || combo_playSolution.getItemCount() == 0)
			return;
		
		//����Ҫ���ŵķ���
		importPlaySolution();		
		solutionName = combo_playSolution.getItem(combo_playSolution.getSelectionIndex());

		//��ʼ��ֱ��ѡ��0��ok��
		listOfPlayControl = util.parseXml(combo_display.getItem(0), combo_playSolution.getItem(0));
		txt_playtime_interval.setText(String.valueOf(listOfPlayControl.get(picPoint).getTimeInterval()));
		//Ϊ��Ͽ���Ӽ�����
		combo_display.addSelectionListener(new ComboSelectionChangeListener());
		combo_playSolution.addSelectionListener(new ComboSelectionChangeListener());
		
		//������Ҫ�ĺ�̨�߳�
		initThread();
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		
	}

	/**
	 * Create the menu manager.
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager main = new MenuManager("main");
		
		//�����ļ��˵�
		MenuManager file = new MenuManager("�ļ�");
		MenuManager help = new MenuManager("����");
		
		main.add(file);
		main.add(help);
		
		help.add(new AboutAction());
		file.add(new ImportAction());
		
		return main;
	}

	/**
	 * Create the toolbar manager.
	 * @return the toolbar manager
	 */
	@Override
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);
		return toolBarManager;
	}

	/**
	 * Create the status line manager.
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		return statusLineManager;
	}

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			PicPlayer window = new PicPlayer();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the shell.
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell picPlayerShell) {
		super.configureShell(picPlayerShell);
		picPlayerShell.setText("ͼƬ������--by GXF");
//		picPlayerShell.setLayout(new FillLayout());
//		picPlayerShell.setBounds(Display.getDefault().getPrimaryMonitor().getBounds());
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(864, 572);
	}
	
	/**
	 * ��ť�����¼�
	 * @author Administrator
	 *
	 */
	class ButtonSelectionAdapter extends SelectionAdapter{

		@Override
		public void widgetSelected(SelectionEvent e) {
			if(e.getSource() == btn_pre){						//��һ�Ű�ť
				isForward = true;
				play();
			}
			else if(e.getSource() == btn_next){					//��һ�Ű�ť
				isForward = false;
				play();
			}
			else if(e.getSource() == btn_attr){					//���԰�ť
				getAttibute();
			}
			else if(e.getSource() == btn_realsize){				//ʵ�ʴ�С��ť
				
			}						
			else if(e.getSource() == btn_play || 
					e.getSource() == btn_continue){				//���źͼ������Ű�ť
				setIsStop(false);
					
				playAuto();
			}
			else if(e.getSource() == btn_stop){					//��ͣ��ť
				//��ͣ����
				setIsStop(true);							
			}
			else if(e.getSource() == btn_fullscreen){			//ȫ����ʾ
				curShell.getDisplay();
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						picFullScreen = new PicFullScreen(displayName, solutionName);
						//���õ�ǰshell���ɼ�
						curShell.setVisible(false);						
						//��ʾshell
						picFullScreen.open();
						
					}
				});
				
			}
		}
		
	}
	
	/**
	 * ��һ�ź���һ�Ű�ť�����¼�
	 * @param isForward
	 */
	public void play(){
		if(isForward){						//����ǰһ��
			picPoint = (picPoint - 1 + pics.length) % pics.length;
			
		}
		else{								//���ź���һ��
			picPoint = (picPoint + 1) % pics.length;
		}
		currentPic = pics[picPoint];
		
		//��ʾͼƬ
		drawImage();
	}
	
	/**
	 * ��ȡͼƬ����
	 */
	public void getAttibute(){
		MessageBox messageBox = new MessageBox(curShell, SWT.ICON_INFORMATION | SWT.OK);
		messageBox.setText("ͼƬ��Ϣ");
		String attri = getPicAttri();
		messageBox.setMessage(attri);
		messageBox.open();
	}
	
	/**
	 * ���벥�ŷ���
	 */
	public void importPlaySolution(){		
		//���û�в��ŷ���
		if(combo_display.getItemCount() == 0 || combo_playSolution.getItemCount() == 0)
			return;
		//��ȡ��ʾ�����ƺͲ��ŷ�����
		String displayName = combo_display.getItem(combo_display.getSelectionIndex());
		String playSolutionName = combo_playSolution.getItem(combo_playSolution.getSelectionIndex());
		
		//-----------------------------�������ķָ���-------------------------------------
		String projectPath = util.getCurrentProjectPath();
		String filePath = projectPath + File.separator + DIC_NAME_PLAY_SOLUTIONS + File.separator + displayName
							+ File.separator + playSolutionName;
		//���ò��ŷ�������,ȫ����Ҫ�õ�
		PicPlayer.solutionName = playSolutionName;
		PicPlayer.displayName = displayName;
		//�ļ���������ͼƬ,����File����
		File dirc = new File(filePath);
		pics = dirc.listFiles(new PicFilter());		
		currentPic = pics[0];
		picPoint = 0;
		//���������ļ�������
		listOfPlayControl = util.parseXml(displayName, playSolutionName);
		txt_playtime_interval.setText(String.valueOf(listOfPlayControl.get(picPoint).getTimeInterval()));
		
		//���ð�ť״̬
		isImportSolution = true;
		setButtonEnableOrDis();
		
		//��ʾͼƬ��������
		drawImage(); 
		
	}
	
	
	
	/**
	 * ��ͼƬ��ʾ��������
	 */
	public void drawImage(){
		ImageData imageData = new ImageData(currentPic.getPath());
		int width = scrolledComposite_top.getBounds().width;
		int height = scrolledComposite_top.getBounds().height;
		imageData = imageData.scaledTo(width, height);
		curImage = new Image(curShell.getDisplay(), imageData);
		//���õ�ǰ�ļ����ɼ�
		lb_curPicName.setVisible(true);
		lb_curPicName.setText(currentPic.getName());
		canvas_picshow.redraw();
		//��ʾ���ż��ʱ��
		txt_playtime_interval.setText(String.valueOf(listOfPlayControl.get(picPoint).getTimeInterval()));
		
	}
	
	
	/**
	 * ������������¼�
	 * @author Administrator
	 *
	 */
	class ShellControlListener implements ControlListener{

		@Override
		public void controlMoved(ControlEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void controlResized(ControlEvent arg0) {
			drawImage(); 									//����ͼƬ
			
		}
		
	}
	
	/**
	 * ��ȡͼƬ������Ϣ
	 * @return
	 */
	public String getPicAttri(){
		String attri = "";
		attri += "�ļ�����" + currentPic.getName() + "\r\n";
		attri += "λ�ã�" + currentPic.getParent() + "\r\n";
		attri += "��С��" + currentPic.length() / 1024 + "KB\r\n";
		attri += "ͼ����Ϣ\r\n";
		attri += "��ȣ�" + curImage.getImageData().width + "px\r\n";
		attri += "�߶ȣ�" + curImage.getImageData().height + "px\r\n";
		
		return attri;
	}
	
	/**
	 * ���벥�ŷ������ð�ťΪenable, ��������Ϊdisable
	 */
	private void setButtonEnableOrDis(){
		btn_play.setEnabled(isImportSolution);
		btn_stop.setEnabled(isImportSolution);
		btn_continue.setEnabled(isImportSolution);
		btn_pre.setEnabled(isImportSolution);
		btn_next.setEnabled(isImportSolution);
		btn_attr.setEnabled(isImportSolution);
		btn_realsize.setEnabled(isImportSolution);
		btn_fullscreen.setEnabled(isImportSolution);
	}
	
	/**
	 * ����ʱ�����Զ�����
	 */
	private void playAuto(){
		//�����߳�
		Thread playPicThread = new Thread(){
			public void run(){
				while(!getIsStop() && !curShell.isDisposed()){
					picPoint = (picPoint + 1) % pics.length;
					currentPic = pics[picPoint];			
					curShell.getDisplay();
					Display.getDefault().syncExec(new Runnable() {						
						@Override
						public void run() {
							drawImage();							
						}
					});
					try {
						//��ȡ����ʱ���� 
						int timeInterval = getTimeInterval(picPoint);
						if(timeInterval == -1){
							continue;
						}
						sleep((long)(timeInterval * 1000));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		playPicThread.start();
	}
	
	/**
	 * ͬ������isStop
	 * @param isStop
	 */
	public static synchronized void setIsStop(boolean isStop){
		PicPlayer.isStop = isStop;
	}
	public synchronized boolean getIsStop(){
		return PicPlayer.isStop;
	}
	
	/**
	 * ������Ҫ�ĺ�̨�߳�,���չ㲥��Ϣ,����ͼƬ��
	 */
	public void initThread(){
//		MyIP myIp = new MyIP();
//		SnmpReceiveMessage messageReceiver = new SnmpReceiveMessage();
//		myIp.run();											//�������չ㲥��Ϣ�߳�
//		messageReceiver.run();								//������Ϣ�������������������16200�˿�
		ReceiveImage receiveImage = new ReceiveImage();
		receiveImage.listen();								//����16201�˿ڽ���ͼƬ
		ReceiveRequestThread receiveRequestThread = new ReceiveRequestThread();
		receiveRequestThread.start(); 						//����16202�˿ڽ�����������
	}
	
	/**
	 * �رճ���
	 */
	public static synchronized void exit(){
		exit = true;
	}
	public static synchronized boolean getExit(){
		return exit;
	}
	
	/**
	 * �رմ��ڣ�ֹͣ��̨�����߳�
	 * @author Administrator
	 *
	 */
	class ShellCloseListener implements Listener{

		@Override
		public void handleEvent(Event arg0) {
			exit();									//ֹͣ��̨�߳�			
		}
		
	}
	
	/**
	 * ��ȡ���еĲ��ŷ���
	 * @return
	 */
	private String[] getSolutions(){
		if(combo_display.getItemCount() == 0)
			return new String[0];
		
		//��ȡ��ʾ����
		String displayName = combo_display.getItem(combo_display.getSelectionIndex());
		String solutionsPath = util.getCurrentProjectPath() + File.separator + DIC_NAME_PLAY_SOLUTIONS + File.separator + displayName;

		File solutionsFile = new File(solutionsPath);
		//ʹ�ù���������ѹ����
		File solutionsFiles[] = solutionsFile.listFiles(new SolutionNameFilter());
		
		String result[] = new String[solutionsFiles.length];
		
		//��ȡ���ŷ�����
		for(int i = 0; i < result.length; i++){
			result[i] = solutionsFiles[i].getName();
		}
		
		return result;
	}
		
	/**
	 * ���ŷ����ı����¼���
	 * @author Administrator
	 *
	 */
	class SolutionChangeListener implements SelectionListener{
		
		
		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
			
			
		}
		//���¼��ز��ŷ���
		@Override
		public void widgetSelected(SelectionEvent arg0) {
			importPlaySolution();
			
		}
		
	}
	
	/**
	 * ��ȡ���е���ʾ������
	 * @return
	 */
	private String[] getDisplayNames(){
		//��ȡ��Ų��ŷ�����·��
		String solutionsPath = util.getCurrentProjectPath() + File.separator + DIC_NAME_PLAY_SOLUTIONS;
		File solutionsFile = new File(solutionsPath);
		//ʹ�ù���������ѹ����
		File solutionsFiles[] = solutionsFile.listFiles(new SolutionNameFilter());
		
		String result[] = new String[solutionsFiles.length];
		
		//��ȡ���ŷ�����
		for(int i = 0; i < result.length; i++){
			result[i] = solutionsFiles[i].getName();
		}
		
		return result;
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
		String beginDateStr = playControl.getDateTimeStart().toString() + " " +  playControl.getTimeStart();
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
	
	/**
	 * ��Ͽ�ѡ��仯
	 * @author Administrator
	 *
	 */
	class ComboSelectionChangeListener extends SelectionAdapter{

		@Override
		public void widgetSelected(SelectionEvent e) {
			if(e.getSource() == combo_display){							//��ʾ���仯
				String playSolutionNames[] = getSolutions();
				//û�в��ŷ���ֱ�ӷ���
				if(playSolutionNames.length == 0)
					return;
				combo_playSolution.setItems(playSolutionNames);
				combo_playSolution.select(0);
				//���¼��ز��ŷ���
				importPlaySolution();
			}
			else if(e.getSource() == combo_playSolution){				//���ŷ����仯
				//���¼��ز��ŷ���
				importPlaySolution();
			}
		}
		
	}
}
