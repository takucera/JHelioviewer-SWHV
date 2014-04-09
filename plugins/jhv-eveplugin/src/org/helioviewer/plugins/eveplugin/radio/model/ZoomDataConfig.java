package org.helioviewer.plugins.eveplugin.radio.model;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ZoomDataConfig implements ZoomManagerListener{
	private double minY;
	private double maxY;
	private Date minX;
	private Date maxX;
	private Rectangle displaySize;
	private long ID;
	
	private List<ZoomDataConfigListener> listeners;

	public ZoomDataConfig(double minY, double maxY, Date minX, Date maxX, Rectangle displaySize, long ID){
		listeners = new ArrayList<ZoomDataConfigListener>();
		
		this.maxX = maxX;
		this.minX = minX;
		this.maxY = maxY;
		this.minY = minY;
		this.displaySize = displaySize;
		if (displaySize != null){
			requestData();
		}
		this.ID = ID;
	}
	
	public void addListener(ZoomDataConfigListener l){
		listeners.add(l);
		double xRatio = 1.0 * (maxX.getTime()-minX.getTime())/displaySize.getWidth();
		double yRatio = 1.0 * (maxY-minY)/displaySize.getHeight();
		Thread t = new Thread((new Runnable(){
			ZoomDataConfigListener l;
			double xRatio;
			double yRatio;
			
			@Override
			public void run() {
				l.requestData(minX, maxX, minY, maxY, xRatio, yRatio, ID);				
			}
			
			public Runnable init(ZoomDataConfigListener l,double xRatio, double yRatio ){
				this.l = l;
				this.xRatio = xRatio;
				this.yRatio = yRatio;
				return this;
			}
			
		}).init(l, xRatio, yRatio));
		t.start();
		
		
	}
	
	
	
	public long getID() {
		return ID;
	}

	public void setID(long iD) {
		ID = iD;
	}

	public void removeListener(ZoomDataConfigListener l){
		listeners.remove(l);
	}
	
	public double getMinY() {
		return minY;
	}

	public void setMinY(double minY) {
		this.minY = minY;
	}

	public double getMaxY() {
		return maxY;
	}

	public void setMaxY(double maxY) {
		this.maxY = maxY;
	}

	public Date getMinX() {
		return minX;
	}

	public void setMinX(Date minX) {
		this.minX = minX;
	}

	public Date getMaxX() {
		return maxX;
	}

	public void setMaxX(Date maxX) {
		this.maxX = maxX;
	}

	public Rectangle getDisplaySize() {
		return displaySize;
	}

	public void update(){
		requestData();		
	}
	
	public void setDisplaySize(Rectangle displaySize) {
		this.displaySize = displaySize;
		requestData();
	}

	@Override
	public void displaySizeChanged(Rectangle area) {
		this.displaySize = area;
		requestData();
	}
	
	private void requestData(){
		double xRatio = 1.0 * (maxX.getTime()-minX.getTime())/displaySize.getWidth();
		double yRatio = 1.0 * (maxY-minY)/displaySize.getHeight();
		for (ZoomDataConfigListener l : listeners){
			l.requestData(minX, maxX, minY, maxY, xRatio, yRatio, ID);
		}
	}

	@Override
	public void XValuesChanged(Date minX, Date maxX) {
		this.minX = minX;
		this.maxX = maxX;
		requestData();		
	}
}
