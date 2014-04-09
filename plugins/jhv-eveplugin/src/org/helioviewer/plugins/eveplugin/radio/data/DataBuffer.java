package org.helioviewer.plugins.eveplugin.radio.data;



public class DataBuffer {
	private byte[] data1;
	private byte[] data2;
	
	private boolean useData1;
	private boolean newData;
	private boolean extraData;
	
	public DataBuffer(){
		useData1 = true;
		newData = false;
		extraData = false;
	}
	
	public void addData(byte[] data){
		synchronized (this) {	
			if(!newData){
				if(useData1){
					data1 = data;
				}else{
					data2 = data;
				}
				newData = true;
			}else{
				if(useData1){
					data2 = data;
				}else{
					data1 = data;
				}
				extraData = true;
			}
		}
	}
	
	public byte[] getData(){
		synchronized(this){
			if(extraData){
				if(useData1){
					extraData = false;
					useData1 = false;
					return data1;
				}else{
					extraData = false;
					useData1 = true;
					return data2;
				}
			}else{
				if(newData){
					newData = false;
					if(useData1){
						return data1;
					}else{
						return data2;
					}
				}else{
					return new byte[0];
				}
			}
		}
	}
	
	public boolean hasData(){
		synchronized (this) {
			return newData;
		}
	}
}
