package org.helioviewer.jhv.view.jp2view.io.jpip;

import java.io.IOException;

import org.helioviewer.jhv.view.jp2view.JP2View;

import kdu_jni.KduException;

public interface JPIPCache {

	void addJPIPDataSegment(JPIPDataSegment data, JP2View v, int level) throws IOException;
    void setComplete(JP2View v, int level, int frameno);
    public boolean addCachedData(JP2View v, int level, int frameno) throws IOException, KduException;

}
