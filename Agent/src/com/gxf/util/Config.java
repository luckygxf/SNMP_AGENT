package com.gxf.util;


/**
 * 播放方案配置文件
 * @author Administrator
 *
 */
public class Config {
	private int playStyle;						//播放方式1--普通播放2--定时播放
	private int playTimeInterval;				//播放时间间隔
	
	private int year_start;						//开始结束日期和时间
	private int month_start;
	private int day_start;
	private int year_end;
	private int month_end;
	private int day_end;
	
	private int hour_start;
	private int min_start;
	private int sec_start;
	private int hour_end;
	private int min_end;
	private int sec_end;
	
	private String playStartTime;				//播放开始时间
	private String playEndTime;					//播放结束时间
	private boolean weekdays[] = new boolean[7];//记录周一到周日
	public int getPlayStyle() {
		return playStyle;
	}
	public void setPlayStyle(int playStyle) {
		this.playStyle = playStyle;
	}
	public int getPlayTimeInterval() {
		return playTimeInterval;
	}
	public void setPlayTimeInterval(int playTimeInterval) {
		this.playTimeInterval = playTimeInterval;
	}
	
	public int getYear_start() {
		return year_start;
	}
	public void setYear_start(int year_start) {
		this.year_start = year_start;
	}
	public int getMonth_start() {
		return month_start;
	}
	public void setMonth_start(int month_start) {
		this.month_start = month_start;
	}
	public int getDay_start() {
		return day_start;
	}
	public void setDay_start(int day_start) {
		this.day_start = day_start;
	}
	public int getYear_end() {
		return year_end;
	}
	public void setYear_end(int year_end) {
		this.year_end = year_end;
	}
	public int getMonth_end() {
		return month_end;
	}
	public void setMonth_end(int month_end) {
		this.month_end = month_end;
	}
	public int getDay_end() {
		return day_end;
	}
	public void setDay_end(int day_end) {
		this.day_end = day_end;
	}
	public int getHour_start() {
		return hour_start;
	}
	public void setHour_start(int hour_start) {
		this.hour_start = hour_start;
	}
	public int getMin_start() {
		return min_start;
	}
	public void setMin_start(int min_start) {
		this.min_start = min_start;
	}
	public int getSec_start() {
		return sec_start;
	}
	public void setSec_start(int sec_start) {
		this.sec_start = sec_start;
	}
	public int getHour_end() {
		return hour_end;
	}
	public void setHour_end(int hour_end) {
		this.hour_end = hour_end;
	}
	public int getMin_end() {
		return min_end;
	}
	public void setMin_end(int min_end) {
		this.min_end = min_end;
	}
	public int getSec_end() {
		return sec_end;
	}
	public void setSec_end(int sec_end) {
		this.sec_end = sec_end;
	}
	public String getPlayStartTime() {
		return playStartTime;
	}
	public void setPlayStartTime(String playStartTime) {
		this.playStartTime = playStartTime;
	}
	public String getPlayEndTime() {
		return playEndTime;
	}
	public void setPlayEndTime(String playEndTime) {
		this.playEndTime = playEndTime;
	}
	public boolean[] getWeekdays() {
		return weekdays;
	}
	public void setWeekdays(boolean[] weekdays) {
		this.weekdays = weekdays;
	}
	
	
	
}
