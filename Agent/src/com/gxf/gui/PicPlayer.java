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
	//面板底部控件
	private Button btn_pre;
	private Button btn_next;
	private Button btn_attr;
	private Button btn_realsize;
	private Button btn_fullscreen;
	
	//面板上部分控件
	private Label lb_playtimeinterval_icon;
	private Text txt_playtime_interval;
	private Button btn_play;
	private Button btn_stop;
	private Button btn_continue;
	private Label lb_curPicName;
	private Combo combo_playSolution;
	private Combo combo_display;
	
	// 文件存储
	static private File currentPic = null;
	static private File[] pics;
	static private int picPoint = 0;
	
	static boolean isForward = true;
	//显示图片
	private Canvas canvas_picshow;			
	
	//当前窗口shell
	public static Shell curShell;
	
	//当前需要显示的image
	private Image curImage;
	
	//面板对象
	static private ScrolledComposite scrolledComposite_top;
	private Composite composite_bottom;
	private Composite composite_menu;
	
	//是否导入播放方案
	private boolean isImportSolution = false;
	
	//工具类
	private Util util = new Util();
	private Label lb_curPicIcon;
	
	//暂停播放
	private static boolean isStop = false;
	
	//全屏显示shell
	private PicFullScreen picFullScreen;
	
	//程序退出，线程结束标志
	private static boolean exit = false;
	
	//存放所有播放方案的文件夹
	private final String DIC_NAME_PLAY_SOLUTIONS = "playSolutions";
	
	//显示屏、播放方案名称和配置
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
		
		//控制面板上面按钮
		btn_pre = new Button(composite_bottom, SWT.NONE);
		btn_pre.setBounds(269, 10, 60, 22);
		btn_pre.setText("上一张");
		btn_pre.addSelectionListener(new ButtonSelectionAdapter());
		
		
		btn_next = new Button(composite_bottom, SWT.NONE);		
		btn_next.setText("下一张");
		btn_next.setBounds(357, 10, 60, 22);
		btn_next.addSelectionListener(new ButtonSelectionAdapter());
		
		btn_attr = new Button(composite_bottom, SWT.NONE);
		btn_attr.setText("属性");
		btn_attr.setBounds(447, 10, 60, 22);
		btn_attr.addSelectionListener(new ButtonSelectionAdapter());
		
		//实际大小，暂时不使用隐藏起来
		btn_realsize = new Button(composite_bottom, SWT.NONE);
		btn_realsize.setText("实际大小");
		btn_realsize.setBounds(518, 10, 60, 22);
		btn_realsize.addSelectionListener(new ButtonSelectionAdapter());
		btn_realsize.setVisible(false);
		
		//全屏显示
		btn_fullscreen = new Button(composite_bottom, SWT.NONE);
		btn_fullscreen.setText("全屏显示");
		btn_fullscreen.setBounds(547, 10, 60, 22);
		btn_fullscreen.addSelectionListener(new ButtonSelectionAdapter());
		
		//面板上面部分
		composite_menu = new Composite(container, SWT.NONE);
		composite_menu.setBounds(0, 0, 856, 38);
		
		lb_playtimeinterval_icon = new Label(composite_menu, SWT.NONE);
		lb_playtimeinterval_icon.setBounds(0, 10, 72, 12);
		lb_playtimeinterval_icon.setText("播放时间间隔");
		
		//由配置文件控制播放效果，这里只是显示作用，不能编辑
		txt_playtime_interval = new Text(composite_menu, SWT.BORDER);
		txt_playtime_interval.setBounds(78, 7, 29, 18);
		txt_playtime_interval.setEnabled(false);
		
		Label lb_seconde_icon = new Label(composite_menu, SWT.NONE);
		lb_seconde_icon.setBounds(118, 10, 19, 12);
		lb_seconde_icon.setText("秒");
		
		//播放按钮
		btn_play = new Button(composite_menu, SWT.NONE);
		btn_play.setBounds(423, 7, 64, 22);
		btn_play.setText("播放");
		btn_play.addSelectionListener(new ButtonSelectionAdapter());
		
		//暂停播放
		btn_stop = new Button(composite_menu, SWT.NONE);
		btn_stop.setBounds(493, 7, 64, 22);
		btn_stop.setText("暂停播放");
		btn_stop.addSelectionListener(new ButtonSelectionAdapter());
		
		//继续播放按钮
		btn_continue = new Button(composite_menu, SWT.NONE);
		btn_continue.setText("继续播放");
		btn_continue.setBounds(563, 7, 64, 22);		
		btn_continue.addSelectionListener(new ButtonSelectionAdapter());
		
		//显示当前播放图片的名字
		lb_curPicName = new Label(composite_menu, SWT.NONE);
		lb_curPicName.setBounds(720, 12, 136, 12);
		lb_curPicName.setText("New Label");
		
		lb_curPicIcon = new Label(composite_menu, SWT.NONE);
		lb_curPicIcon.setBounds(633, 12, 88, 12);
		lb_curPicIcon.setText("当前播放图片:");
		
		Label lb_playSolution = new Label(composite_menu, SWT.NONE);
		lb_playSolution.setBounds(271, 8, 54, 12);
		lb_playSolution.setText("播放方案");
		
		//播放方案列表
		combo_playSolution = new Combo(composite_menu, SWT.NONE);
		combo_playSolution.setBounds(331, 7, 86, 20);
		
		Label lb_display = new Label(composite_menu, SWT.NONE);
		lb_display.setBounds(134, 10, 45, 12);
		lb_display.setText("显示屏");
		
		combo_display = new Combo(composite_menu, SWT.NONE);
		combo_display.setBounds(179, 7, 86, 20);
		//获取当前窗口shell
		curShell = parent.getShell();
		
		//对控件进行初始化
		init();
		
		return container;
	}
	
	/**
	 * 对控件进行初始化，初始化控件大小，初始化显示的内容
	 */
	public void init(){
		canvas_picshow.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent arg0) {
				if(curImage != null)
					arg0.gc.drawImage(curImage, 0, 0);
				
			}
		});
		//设置按钮状态
		setButtonEnableOrDis();
		
		//设置应用程序logo
		String logoName = "logo.png";
		String logoPath = util.getCurrentProjectPath() + "\\images\\" + logoName;
		ImageData logoImageData = new ImageData(logoPath);
		Image logoImage = new Image(curShell.getDisplay(), logoImageData);
		curShell.setImage(logoImage);
		
		//设置默认播放时间间隔为1s
		txt_playtime_interval.setText(String.valueOf(1));
		
		//当前文件名不可见
		lb_curPicName.setVisible(false);
		
		//当前shell注册关闭窗口事件
		curShell.addListener(SWT.Close, new ShellCloseListener());
		//为播放方案下拉列表注册监听事件
		combo_playSolution.addSelectionListener(new SolutionChangeListener());
		//初始化显示屏信息
		String displayNames[] = getDisplayNames();
		
		//初始化显示屏信息
		if(displayNames.length != 0){
			combo_display.setItems(displayNames);
			combo_display.select(0);
		}
		
		//初始化播放方案
		String playSolutions[] = getSolutions();
		if(playSolutions.length != 0){
			combo_playSolution.setItems(playSolutions);
			combo_playSolution.select(0);
		}
		
		//没有可以播放的播放方案
		if(combo_display.getItemCount() == 0 || combo_playSolution.getItemCount() == 0)
			return;
		
		//导入要播放的方案
		importPlaySolution();		
		solutionName = combo_playSolution.getItem(combo_playSolution.getSelectionIndex());

		//初始化直接选择0就ok了
		listOfPlayControl = util.parseXml(combo_display.getItem(0), combo_playSolution.getItem(0));
		txt_playtime_interval.setText(String.valueOf(listOfPlayControl.get(picPoint).getTimeInterval()));
		//为组合框添加监听器
		combo_display.addSelectionListener(new ComboSelectionChangeListener());
		combo_playSolution.addSelectionListener(new ComboSelectionChangeListener());
		
		//启动需要的后台线程
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
		
		//创建文件菜单
		MenuManager file = new MenuManager("文件");
		MenuManager help = new MenuManager("帮助");
		
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
		picPlayerShell.setText("图片播放器--by GXF");
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
	 * 按钮监听事件
	 * @author Administrator
	 *
	 */
	class ButtonSelectionAdapter extends SelectionAdapter{

		@Override
		public void widgetSelected(SelectionEvent e) {
			if(e.getSource() == btn_pre){						//上一张按钮
				isForward = true;
				play();
			}
			else if(e.getSource() == btn_next){					//下一张按钮
				isForward = false;
				play();
			}
			else if(e.getSource() == btn_attr){					//属性按钮
				getAttibute();
			}
			else if(e.getSource() == btn_realsize){				//实际大小按钮
				
			}						
			else if(e.getSource() == btn_play || 
					e.getSource() == btn_continue){				//播放和继续播放按钮
				setIsStop(false);
					
				playAuto();
			}
			else if(e.getSource() == btn_stop){					//暂停按钮
				//暂停播放
				setIsStop(true);							
			}
			else if(e.getSource() == btn_fullscreen){			//全屏显示
				curShell.getDisplay();
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						picFullScreen = new PicFullScreen(displayName, solutionName);
						//设置当前shell不可见
						curShell.setVisible(false);						
						//显示shell
						picFullScreen.open();
						
					}
				});
				
			}
		}
		
	}
	
	/**
	 * 下一张和上一张按钮监听事件
	 * @param isForward
	 */
	public void play(){
		if(isForward){						//播放前一张
			picPoint = (picPoint - 1 + pics.length) % pics.length;
			
		}
		else{								//播放后面一张
			picPoint = (picPoint + 1) % pics.length;
		}
		currentPic = pics[picPoint];
		
		//显示图片
		drawImage();
	}
	
	/**
	 * 获取图片属性
	 */
	public void getAttibute(){
		MessageBox messageBox = new MessageBox(curShell, SWT.ICON_INFORMATION | SWT.OK);
		messageBox.setText("图片信息");
		String attri = getPicAttri();
		messageBox.setMessage(attri);
		messageBox.open();
	}
	
	/**
	 * 导入播放方案
	 */
	public void importPlaySolution(){		
		//如果没有播放方案
		if(combo_display.getItemCount() == 0 || combo_playSolution.getItemCount() == 0)
			return;
		//获取显示屏名称和播放方案名
		String displayName = combo_display.getItem(combo_display.getSelectionIndex());
		String playSolutionName = combo_playSolution.getItem(combo_playSolution.getSelectionIndex());
		
		//-----------------------------华丽丽的分割线-------------------------------------
		String projectPath = util.getCurrentProjectPath();
		String filePath = projectPath + File.separator + DIC_NAME_PLAY_SOLUTIONS + File.separator + displayName
							+ File.separator + playSolutionName;
		//设置播放方案名称,全屏是要用到
		PicPlayer.solutionName = playSolutionName;
		PicPlayer.displayName = displayName;
		//文件夹中所有图片,生成File对象
		File dirc = new File(filePath);
		pics = dirc.listFiles(new PicFilter());		
		currentPic = pics[0];
		picPoint = 0;
		//导入配置文件，解析
		listOfPlayControl = util.parseXml(displayName, playSolutionName);
		txt_playtime_interval.setText(String.valueOf(listOfPlayControl.get(picPoint).getTimeInterval()));
		
		//设置按钮状态
		isImportSolution = true;
		setButtonEnableOrDis();
		
		//显示图片到画布上
		drawImage(); 
		
	}
	
	
	
	/**
	 * 将图片显示到画布上
	 */
	public void drawImage(){
		ImageData imageData = new ImageData(currentPic.getPath());
		int width = scrolledComposite_top.getBounds().width;
		int height = scrolledComposite_top.getBounds().height;
		imageData = imageData.scaledTo(width, height);
		curImage = new Image(curShell.getDisplay(), imageData);
		//设置当前文件名可见
		lb_curPicName.setVisible(true);
		lb_curPicName.setText(currentPic.getName());
		canvas_picshow.redraw();
		//显示播放间隔时间
		txt_playtime_interval.setText(String.valueOf(listOfPlayControl.get(picPoint).getTimeInterval()));
		
	}
	
	
	/**
	 * 调整窗体监听事件
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
			drawImage(); 									//更新图片
			
		}
		
	}
	
	/**
	 * 获取图片属性信息
	 * @return
	 */
	public String getPicAttri(){
		String attri = "";
		attri += "文件名：" + currentPic.getName() + "\r\n";
		attri += "位置：" + currentPic.getParent() + "\r\n";
		attri += "大小：" + currentPic.length() / 1024 + "KB\r\n";
		attri += "图像信息\r\n";
		attri += "宽度：" + curImage.getImageData().width + "px\r\n";
		attri += "高度：" + curImage.getImageData().height + "px\r\n";
		
		return attri;
	}
	
	/**
	 * 导入播放方案设置按钮为enable, 否则设置为disable
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
	 * 根据时间间隔自动播放
	 */
	private void playAuto(){
		//播放线程
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
						//获取播放时间间隔 
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
	 * 同步访问isStop
	 * @param isStop
	 */
	public static synchronized void setIsStop(boolean isStop){
		PicPlayer.isStop = isStop;
	}
	public synchronized boolean getIsStop(){
		return PicPlayer.isStop;
	}
	
	/**
	 * 启动需要的后台线程,接收广播消息,接收图片等
	 */
	public void initThread(){
//		MyIP myIp = new MyIP();
//		SnmpReceiveMessage messageReceiver = new SnmpReceiveMessage();
//		myIp.run();											//启动接收广播消息线程
//		messageReceiver.run();								//启动消息接收器，这里监听的是16200端口
		ReceiveImage receiveImage = new ReceiveImage();
		receiveImage.listen();								//监听16201端口接收图片
		ReceiveRequestThread receiveRequestThread = new ReceiveRequestThread();
		receiveRequestThread.start(); 						//监听16202端口接收连接请求
	}
	
	/**
	 * 关闭程序
	 */
	public static synchronized void exit(){
		exit = true;
	}
	public static synchronized boolean getExit(){
		return exit;
	}
	
	/**
	 * 关闭窗口，停止后台运行线程
	 * @author Administrator
	 *
	 */
	class ShellCloseListener implements Listener{

		@Override
		public void handleEvent(Event arg0) {
			exit();									//停止后台线程			
		}
		
	}
	
	/**
	 * 获取所有的播放方案
	 * @return
	 */
	private String[] getSolutions(){
		if(combo_display.getItemCount() == 0)
			return new String[0];
		
		//获取显示屏名
		String displayName = combo_display.getItem(combo_display.getSelectionIndex());
		String solutionsPath = util.getCurrentProjectPath() + File.separator + DIC_NAME_PLAY_SOLUTIONS + File.separator + displayName;

		File solutionsFile = new File(solutionsPath);
		//使用过滤器过滤压缩包
		File solutionsFiles[] = solutionsFile.listFiles(new SolutionNameFilter());
		
		String result[] = new String[solutionsFiles.length];
		
		//获取播放方案名
		for(int i = 0; i < result.length; i++){
			result[i] = solutionsFiles[i].getName();
		}
		
		return result;
	}
		
	/**
	 * 播放方案改变重新加载
	 * @author Administrator
	 *
	 */
	class SolutionChangeListener implements SelectionListener{
		
		
		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
			
			
		}
		//重新加载播放方案
		@Override
		public void widgetSelected(SelectionEvent arg0) {
			importPlaySolution();
			
		}
		
	}
	
	/**
	 * 获取所有的显示屏名字
	 * @return
	 */
	private String[] getDisplayNames(){
		//获取存放播放方案的路径
		String solutionsPath = util.getCurrentProjectPath() + File.separator + DIC_NAME_PLAY_SOLUTIONS;
		File solutionsFile = new File(solutionsPath);
		//使用过滤器过滤压缩包
		File solutionsFiles[] = solutionsFile.listFiles(new SolutionNameFilter());
		
		String result[] = new String[solutionsFiles.length];
		
		//获取播放方案名
		for(int i = 0; i < result.length; i++){
			result[i] = solutionsFiles[i].getName();
		}
		
		return result;
	}
	
	/**
	 * 获取暂停播放时间
	 * @param picIndex
	 * @return
	 * -1表示不能播放
	 * >0表示播放几秒
	 */
	private int getTimeInterval(int picIndex){
		//默认可以播放
		PlayControl playControl = listOfPlayControl.get(picIndex);
		
		//1.比较日期范围是否valid
		//2.比较时间范围是否valid
		//3.星期是否valid
		
		//比较日期+时间
		java.util.Date curDate = new java.util.Date();
		String beginDateStr = playControl.getDateTimeStart().toString() + " " +  playControl.getTimeStart();
		String afterDateStr = playControl.getDateTimeEnd().toString() + " " + playControl.getTimeEnd();

		java.sql.Timestamp beginDate = java.sql.Timestamp.valueOf(beginDateStr);
		java.sql.Timestamp afterDate = java.sql.Timestamp.valueOf(afterDateStr);
		
		if(!(curDate.after(beginDate) && curDate.before(afterDate))){
			return -1;
		}
		
		//比较星期
		String weekdaysStr = playControl.getWeekdays();
		//获取当前星期
		Calendar c = Calendar.getInstance();
        int curWeekday = c.get(Calendar.DAY_OF_WEEK) - 1;
        if(curWeekday < 0)
        	curWeekday = 6;
        if(weekdaysStr.charAt(curWeekday) == '0')
        	return -1;
        
        //日期、时间和星期都valid获取播放间隔时间返回
        int timeInterval = playControl.getTimeInterval();
		
        return timeInterval;
	}
	
	/**
	 * 组合框选择变化
	 * @author Administrator
	 *
	 */
	class ComboSelectionChangeListener extends SelectionAdapter{

		@Override
		public void widgetSelected(SelectionEvent e) {
			if(e.getSource() == combo_display){							//显示屏变化
				String playSolutionNames[] = getSolutions();
				//没有播放方案直接返回
				if(playSolutionNames.length == 0)
					return;
				combo_playSolution.setItems(playSolutionNames);
				combo_playSolution.select(0);
				//重新加载播放方案
				importPlaySolution();
			}
			else if(e.getSource() == combo_playSolution){				//播放方案变化
				//重新加载播放方案
				importPlaySolution();
			}
		}
		
	}
}
