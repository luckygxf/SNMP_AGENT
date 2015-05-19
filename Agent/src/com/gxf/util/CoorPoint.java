package com.gxf.util;

/**
 * 封装坐标系统的坐标，横坐标和纵坐标
 * @author Administrator
 *
 */
public class CoorPoint {
	private int x;
	private int y;
	public CoorPoint(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}
	public CoorPoint() {
		super();
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	
	
	
}
