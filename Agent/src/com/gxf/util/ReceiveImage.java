package com.gxf.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.gxf.gui.PicFullScreen;
import com.gxf.gui.PicPlayer;


/*******************************************************************************************
 * 自定义协议                                                                                                                                                                                                                  *
 *                    																	   *
 * 完整的数据																			   *
 * (image_start)(image_file_name)(image_file_name_end)(image_file_length)(image)(image_end)*
 * 																						   *
 * *****************************************************************************************/

/**
 * 开启线程接收图片
 * 只负责接收压缩问价，不负责解压文件
 * @author Administrator
 *
 */
public class ReceiveImage {
	private ServerSocket serverSocket;				//服务器socket
	private Thread listenThread;					//监听线程
	private Util util = new Util();
	private String imageDir;
	
	
	//套接字监听端口
	private final int PORT = 16201;
	
	//协议中的三个字段
	//image_start
	private final String IMAGE_START = "image:";	
	//image_file_name_end
	private final String IMAGE_FILE_NAME_END = "?";
	//image_end
	private final String MESSGE_END = "over";
	
	//For Test
	private static int writeSize = 0;
	//编码方式
    private final String DEFAULT_ENCODE = "UTF-8";
    private final String ISO_ENCODE = "ISO-8859-1";
    
    //播放方案名
    private String solutionName;
	
	public ReceiveImage(){
		try {
			serverSocket = new ServerSocket(PORT);			
			this.imageDir = util.getCurrentProjectPath();			//获取当前项目路径，用于存放Imag
			String playSolutionsPath = this.imageDir + File.separator + "playSolutions";
			File fileDir = new File(playSolutionsPath);
			//如果playSolutions文件夹不存在，创建该
			if(!fileDir.exists())
				fileDir.mkdir();										//创建文件夹
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 开始监听端口
	 */
	public void listen(){
		listenThread = new Thread(new Runnable(){
			
			//监听端口
			@Override
			public void run() {
				while(!PicPlayer.getExit()){									//程序没有退出,线程不结束
					try {
						final Socket clientSocket = serverSocket.accept();
						//每个请求开一个线程
						new Thread(new Runnable(){
							public void run(){
								read(clientSocket);
							}
						}).start();
					} catch (IOException e) {						
						e.printStackTrace();
					}
				}
				
			}
			
		});
		
		//启动监听线程
		listenThread.start();
	}
	
	/**
	 * 从客户端socket读取数据
	 * @param clientSocket
	 */
	public void read(Socket clientSocket){
		try {
			InputStream is = clientSocket.getInputStream();
			StringBuffer sb = new StringBuffer();
			System.out.println("开始读数据...");
			//开始读取输入流中的数据
			while(!clientSocket.isClosed()){
				int imageStartIndex = -1;
				System.out.println("开始读image_start...");
				//读取IMAGE_START
				while((imageStartIndex = sb.indexOf(IMAGE_START)) < 0){
					readToBuffer(is, sb);
				}
				System.out.println("开始读image_start..." + imageStartIndex);
				System.out.println("image_start = " + sb.substring(0, imageStartIndex + IMAGE_START.length()).toString());
				sb.delete(0, imageStartIndex + IMAGE_START.length());
				
				//读取image name
				System.out.println("开始读image name...");
				String imageName;
				int file_name_end;
				while((file_name_end = sb.indexOf(IMAGE_FILE_NAME_END)) < 0){
					readToBuffer(is, sb);
				}
				System.out.println("开始读image name..." + file_name_end);
				imageName = new String(sb.substring(0, file_name_end).getBytes(ISO_ENCODE), DEFAULT_ENCODE);
				System.out.println("imageName = " + imageName);
				//获取播放方案名
				int solutionNameEndIndex = imageName.indexOf('.');
				solutionName = imageName.substring(0, solutionNameEndIndex);
				
				sb.delete(0, file_name_end + IMAGE_FILE_NAME_END.length());
				
				//读取image length
				System.out.println("开始读image length...");
				while(sb.length() < 8)
					readToBuffer(is, sb);
				String imageLengthStr = sb.substring(0, 8);
				byte byteOfImageLength[] = imageLengthStr.getBytes(ISO_ENCODE);
				long imageLengthLong = util.bytesToLong(byteOfImageLength);
				System.out.println("file length = " + imageLengthLong);
				sb.delete(0, 8);
				
				//读取image content
				System.out.println("开始读image content...");
				String imagePath = this.imageDir + File.separator + "playSolutions" + File.separator + imageName;				
				File imageFile  = new File(imagePath);
				OutputStream os = new FileOutputStream(imageFile);					//文件输出流
				
				byte image[] = sb.toString().getBytes(ISO_ENCODE);
				if(image.length < imageLengthLong){									//文件没有完全读取
					writeSize += image.length;
					System.out.println("image.length = " + writeSize);
					os.write(image); 				
					writeImage(is, os, imageLengthLong - image.length); 
					sb.delete(0, sb.length());
				}
				else{																//文件完全读取
					writeSize += image.length;
					os.write(image, 0, (int) imageLengthLong);
					sb.delete(0, (int) imageLengthLong);
				}
				System.out.println("writeSize = " + writeSize);
				os.flush();
				os.close();															//关闭文件输出流

				
				//重新加载播放方案
				reLoadSolution(this.solutionName);
				System.out.println("新的播放方案接收完成!");
				
				//关闭clientSocket
				clientSocket.close();
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		} finally{

		}
		
	}
	
	/**
	 * 从socket中读取内容到stringbuffer中
	 * @param clientSocket
	 * @param sb
	 */
	public void readToBuffer(InputStream inputStream, StringBuffer sb){
		byte bytes[] = new byte[1024];
		int readLength = 0;
		try {
			readLength = inputStream.read(bytes);
			if(readLength > 0){
				String temp = new String(bytes, 0, readLength, ISO_ENCODE);
				sb.append(temp);
			}
				
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * 从图片输入中读取数据写入到图片输出流中
	 * @param is
	 * @param os
	 * @param length
	 */
	public void writeImage(InputStream is, OutputStream os, long length){
		byte image[] = new byte[1024];
		int oneTimeReadLength = 0;
		System.out.println("length = " + length);
		long readLength = 0;
		for(; readLength < length;){
			if(readLength + image.length <= length){					//还有  > 1024字节没有读取	
				try {
					oneTimeReadLength = is.read(image);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}//if
			else{
				try {												// < 1024字节没有读取
					oneTimeReadLength = is.read(image, 0, (int) (length - readLength));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}//else
			
			try {													//读取到的数据写到文件中
				if(oneTimeReadLength == -1)							//说明socket已经结束
				{
					os.write(image, 0, (int) (length - readLength));
					break;
				}
				writeSize += oneTimeReadLength;
				os.write(image, 0, oneTimeReadLength);				//数组越界
			} catch (IOException e) {
				e.printStackTrace();
			}
			readLength += oneTimeReadLength;
		}       
	}
	
	/**
	 * 重新加载播放方案
	 * @param solutionName
	 */
	public void reLoadSolution(String solutionName){
		//该解压播放方案
//		util.unzipSolution(PicPlayer.solutionName);
		
		PicFullScreen.solutionName = solutionName;
		PicFullScreen.setIsReloadSolution(true);
	}
}
