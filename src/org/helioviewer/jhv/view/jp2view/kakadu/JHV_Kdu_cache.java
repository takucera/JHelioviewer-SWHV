package org.helioviewer.jhv.view.jp2view.kakadu;

import java.io.IOException;
import java.util.ArrayList;

import kdu_jni.KduException;
import kdu_jni.Kdu_cache;

import org.helioviewer.jhv.metadata.HelioviewerMetaData;
import org.helioviewer.jhv.metadata.MetaData;
import org.helioviewer.jhv.time.JHVDate;
import org.helioviewer.jhv.view.jp2view.JP2View;
import org.helioviewer.jhv.view.jp2view.io.jpip.JPIPCache;
import org.helioviewer.jhv.view.jp2view.io.jpip.JPIPDataSegment;
import org.helioviewer.jhv.view.jp2view.io.jpip.JPIPDatabinClass;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class JHV_Kdu_cache extends Kdu_cache implements JPIPCache {
    private static final Cache< String,JPIPCombined> cache =CacheBuilder.newBuilder().maximumSize(10000).build();
    
    public boolean isDataBinCompleted(JPIPDatabinClass binClass, int streamID, int binID) throws JHV_KduException {
        boolean complete[] = new boolean[1];
        try {
            Get_databin_length(binClass.kakaduClassID, streamID, binID, complete);
        } catch (KduException e) {
            throw new JHV_KduException("Internal Kakadu error: " + e.getMessage(), e);
        }
        return complete[0];
    }

    @Override
    public void addJPIPDataSegment(JPIPDataSegment data, JP2View v, int level) throws IOException {
        try {
        	String k = getKey(v, level, (int)data.codestreamID);

        	if(k!=null) {
            	JPIPCombined c = cache.getIfPresent(k);
            	if(c==null) {
            	    c = new JPIPCombined();
            	}
        	    c.ds.add(data);
        	    cache.put(k, c);
        	}
            Add_to_databin(data.classID.kakaduClassID, data.codestreamID, data.binID, data.data, data.offset, data.length, data.isFinal, true, false);
        } catch (KduException e) {
            throw new IOException("Internal Kakadu error: " + e.getMessage(), e);
        }
    }
    
    public void setComplete(JP2View v, int level, int frameno) {
    	String k = getKey(v, level, frameno);
    	if(k!=null) {
        	JPIPCombined c = cache.getIfPresent(k);
        	c.is_complete=true;
    	}	
    }
    
    private String getKey(JP2View v, int level, int frameno) {
    	JHVDate t = v.getFrameTime(frameno);
    	MetaData _m = v.getMetaData(t);
    	if(level>=0 && _m instanceof HelioviewerMetaData) {
    	    HelioviewerMetaData m = (HelioviewerMetaData) (v.getMetaData(t));        	
    	    String uid = m.getObservatory() + m.getDetector() + m.getInstrument();
    	    return uid + level + t;
    	}
    	return null;
    }
    
    public boolean addCachedData(JP2View v, int level, int frameno) throws IOException, KduException{
    	String k = getKey(v, level, frameno);
    	if(k!=null) {
        	JPIPCombined c = cache.getIfPresent(k);
        	if(c!=null && c.is_complete) {
        		for(JPIPDataSegment segment: c.ds){
                    Add_to_databin(segment.classID.kakaduClassID, segment.codestreamID, segment.binID, segment.data, segment.offset, segment.length, segment.isFinal, true, false);
        		}
        		return true;
        	}
    	}
    	return false;
    }
    
    private class JPIPCombined {
    	public ArrayList<JPIPDataSegment> ds =  new ArrayList<JPIPDataSegment>();
    	public boolean is_complete = false;
    }
}


