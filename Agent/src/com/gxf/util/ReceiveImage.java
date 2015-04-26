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
 * �Զ���Э��                                                                                                                                                                                                                  *
 *                    																	   *
 * ����������																			   *
 * (image_start)(image_file_name)(image_file_name_end)(image_file_length)(image)(image_end)*
 * 																						   *
 * *****************************************************************************************/

/**
 * �����߳̽���ͼƬ
 * ֻ�������ѹ���ʼۣ��������ѹ�ļ�
 * @author Administrator
 *
 */
public class ReceiveImage {
	private ServerSocket serverSocket;				//������socket
	private Thread listenThread;					//�����߳�
	private Util util = new Util();
	private String imageDir;
	
	
	//�׽��ּ����˿�
	private final int PORT = 16201;
	
	//Э���е������ֶ�
	//image_start
	private final String IMAGE_START = "image:";	
	//image_file_name_end
	private final String IMAGE_FILE_NAME_END = "?";
	//image_end
	private final String MESSGE_END = "over";
	
	//For Test
	private static int writeSize = 0;
	//���뷽ʽ
    private final String DEFAULT_ENCODE = "UTF-8";
    private final String ISO_ENCODE = "ISO-8859-1";
    
    //���ŷ�����
    private String solutionName;
	
	public ReceiveImage(){
		try {
			serverSocket = new ServerSocket(PORT);			
			this.imageDir = util.getCurrentProjectPath();			//��ȡ��ǰ��Ŀ·�������ڴ��Imag
			String playSolutionsPath = this.imageDir + File.separator + "playSolutions";
			File fileDir = new File(playSolutionsPath);
			//���playSolutions�ļ��в����ڣ�������
			if(!fileDir.exists())
				fileDir.mkdir();										//�����ļ���
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * ��ʼ�����˿�
	 */
	public void listen(){
		listenThread = new Thread(new Runnable(){
			
			//�����˿�
			@Override
			public void run() {
				while(!PicPlayer.getExit()){									//����û���˳�,�̲߳�����
					try {
						final Socket clientSocket = serverSocket.accept();
						//ÿ������һ���߳�
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
		
		//���������߳�
		listenThread.start();
	}
	
	/**
	 * �ӿͻ���socket��ȡ����
	 * @param clientSocket
	 */
	public void read(Socket clientSocket){
		try {
			InputStream is = clientSocket.getInputStream();
			StringBuffer sb = new StringBuffer();
			System.out.println("��ʼ������...");
			//��ʼ��ȡ�������е�����
			while(!clientSocket.isClosed()){
				int imageStartIndex = -1;
				System.out.println("��ʼ��image_start...");
				//��ȡIMAGE_START
				while((imageStartIndex = sb.indexOf(IMAGE_START)) < 0){
					readToBuffer(is, sb);
				}
				System.out.println("��ʼ��image_start..." + imageStartIndex);
				System.out.println("image_start = " + sb.substring(0, imageStartIndex + IMAGE_START.length()).toString());
				sb.delete(0, imageStartIndex + IMAGE_START.length());
				
				//��ȡimage name
				System.out.println("��ʼ��image name...");
				String imageName;
				int file_name_end;
				while((file_name_end = sb.indexOf(IMAGE_FILE_NAME_END)) < 0){
					readToBuffer(is, sb);
				}
				System.out.println("��ʼ��image name..." + file_name_end);
				imageName = new String(sb.substring(0, file_name_end).getBytes(ISO_ENCODE), DEFAULT_ENCODE);
				System.out.println("imageName = " + imageName);
				//��ȡ���ŷ�����
				int solutionNameEndIndex = imageName.indexOf('.');
				solutionName = imageName.substring(0, solutionNameEndIndex);
				
				sb.delete(0, file_name_end + IMAGE_FILE_NAME_END.length());
				
				//��ȡimage length
				System.out.println("��ʼ��image length...");
				while(sb.length() < 8)
					readToBuffer(is, sb);
				String imageLengthStr = sb.substring(0, 8);
				byte byteOfImageLength[] = imageLengthStr.getBytes(ISO_ENCODE);
				long imageLengthLong = util.bytesToLong(byteOfImageLength);
				System.out.println("file length = " + imageLengthLong);
				sb.delete(0, 8);
				
				//��ȡimage content
				System.out.println("��ʼ��image content...");
				String imagePath = this.imageDir + File.separator + "playSolutions" + File.separator + imageName;				
				File imageFile  = new File(imagePath);
				OutputStream os = new FileOutputStream(imageFile);					//�ļ������
				
				byte image[] = sb.toString().getBytes(ISO_ENCODE);
				if(image.length < imageLengthLong){									//�ļ�û����ȫ��ȡ
					writeSize += image.length;
					System.out.println("image.length = " + writeSize);
					os.write(image); 				
					writeImage(is, os, imageLengthLong - image.length); 
					sb.delete(0, sb.length());
				}
				else{																//�ļ���ȫ��ȡ
					writeSize += image.length;
					os.write(image, 0, (int) imageLengthLong);
					sb.delete(0, (int) imageLengthLong);
				}
				System.out.println("writeSize = " + writeSize);
				os.flush();
				os.close();															//�ر��ļ������

				
				//���¼��ز��ŷ���
				reLoadSolution(this.solutionName);
				System.out.println("�µĲ��ŷ����������!");
				
				//�ر�clientSocket
				clientSocket.close();
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		} finally{

		}
		
	}
	
	/**
	 * ��socket�ж�ȡ���ݵ�stringbuffer��
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
	 * ��ͼƬ�����ж�ȡ����д�뵽ͼƬ�������
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
			if(readLength + image.length <= length){					//����  > 1024�ֽ�û�ж�ȡ	
				try {
					oneTimeReadLength = is.read(image);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}//if
			else{
				try {												// < 1024�ֽ�û�ж�ȡ
					oneTimeReadLength = is.read(image, 0, (int) (length - readLength));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}//else
			
			try {													//��ȡ��������д���ļ���
				if(oneTimeReadLength == -1)							//˵��socket�Ѿ�����
				{
					os.write(image, 0, (int) (length - readLength));
					break;
				}
				writeSize += oneTimeReadLength;
				os.write(image, 0, oneTimeReadLength);				//����Խ��
			} catch (IOException e) {
				e.printStackTrace();
			}
			readLength += oneTimeReadLength;
		}       
	}
	
	/**
	 * ���¼��ز��ŷ���
	 * @param solutionName
	 */
	public void reLoadSolution(String solutionName){
		//�ý�ѹ���ŷ���
//		util.unzipSolution(PicPlayer.solutionName);
		
		PicFullScreen.solutionName = solutionName;
		PicFullScreen.setIsReloadSolution(true);
	}
}
