package org.helioviewer.filter.runningdifference;

import java.util.LinkedList;
import java.util.List;

import org.helioviewer.base.logging.Log;
import org.helioviewer.viewmodel.filter.FilterListener;
import org.helioviewer.viewmodel.filter.FrameFilter;
import org.helioviewer.viewmodel.filter.ObservableFilter;
import org.helioviewer.viewmodel.filter.StandardFilter;
import org.helioviewer.viewmodel.imagedata.ColorMask;
import org.helioviewer.viewmodel.imagedata.ImageData;
import org.helioviewer.viewmodel.imagedata.SingleChannelByte8ImageData;
import org.helioviewer.viewmodel.imagetransport.Byte8ImageTransport;
import org.helioviewer.viewmodel.view.TimeMachineData;

/**
 * Filter applying running difference to some movie
 * 
 * @author Helge Dietert
 */
public class RunningDifferenceFilter implements FrameFilter, StandardFilter, ObservableFilter {
    /**
     * Flag to indicate whether this filter should be considered active
     */
    private boolean isActive = true;
    /**
     * Observer listener
     */
    private List<FilterListener> listeners = new LinkedList<FilterListener>();
    /**
     * Given time machine to access the previous frame
     */
    private TimeMachineData timeMachineData;

    /**
     * @see org.helioviewer.viewmodel.filter.ObservableFilter#addFilterListener(org.helioviewer.viewmodel.filter.FilterListener)
     */
    public void addFilterListener(FilterListener l) {
        listeners.add(l);
    }

    /**
     * @see org.helioviewer.viewmodel.filter.StandardFilter#apply(org.helioviewer.viewmodel.imagedata.ImageData)
     */
    public ImageData apply(ImageData data) {
        // If its not active we don't filter at all
        if (!isActive)
            return data;

        if (data == null)
            return null;

        if (timeMachineData == null)
            return data;

        ImageData previousFrame = timeMachineData.getPreviousFrame(1);
        // If this is the first frame and therefore 0 we take the frame before
        // to get some similar picture
        if (previousFrame == null) {
            Log.debug("No previous frame available, take ahead");
            previousFrame = timeMachineData.getPreviousFrame(-1);
        }
        if (previousFrame != null) {
            // Filter according to the data type
            if (data.getImageTransport() instanceof Byte8ImageTransport) {
                // Just one channel
                byte[] newPixelData = ((Byte8ImageTransport) data.getImageTransport()).getByte8PixelData();
                byte[] prevPixelData = ((Byte8ImageTransport) previousFrame.getImageTransport()).getByte8PixelData();
                if (newPixelData.length != prevPixelData.length) {
                    Log.warn("Pixel data has not the same size!! New size " + newPixelData.length + " old size " + prevPixelData.length);
                    return null;
                }
                byte[] pixelData = new byte[newPixelData.length];
                double tr=16;
                for (int i = 0; i < newPixelData.length; i++) {

                    //pixelData[i] = (byte) ((((newPixelData[i] << 4>>>1) - (prevPixelData[i] << 4>>>1))) + 0x80);
                	int h1 = (int)newPixelData[i];
                	int h2 = (int) prevPixelData[i];
                	int diff = h1-h2;
                	if(diff<-tr){ 
                		diff=(int)(-tr);
                	}
                	else if(diff>tr){diff=(int)tr;}
                	
                	pixelData[i] = (byte)((int)((((diff)/tr+1)*(255./2.))));
                	
                	
                }
                final ColorMask colorMask = new ColorMask();

                return new SingleChannelByte8ImageData(data.getWidth(), data.getHeight(), pixelData, colorMask);
            }
        }
        System.out.println("No other frame available");
        return data;
    }

    /**
     * @see org.helioviewer.viewmodel.filter.Filter#forceRefilter()
     */
    public void forceRefilter() {
        // This plugin always filter the data
    }

    /**
     * @return the isActive
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * @see org.helioviewer.viewmodel.filter.Filter#isMajorFilter()
     */
    public boolean isMajorFilter() {
        return true;
    }

    /**
     * Inform all listener about a change of the state
     */
    protected void notifyAllListeners() {
        for (FilterListener f : listeners) {
            f.filterChanged(this);
        }
    }

    /**
     * @see org.helioviewer.viewmodel.filter.ObservableFilter#removeFilterListener(org.helioviewer.viewmodel.filter.FilterListener)
     */
    public void removeFilterListener(FilterListener l) {
        listeners.remove(l);
    }

    /**
     * @param isActive
     *            the isActive to set
     */
    public void setActive(boolean isActive) {
        this.isActive = isActive;
        notifyAllListeners();
    }

    /**
     * @see org.helioviewer.viewmodel.filter.FrameFilter#setTimeMachineData(org.helioviewer.viewmodel.view.TimeMachineData)
     */
    public void setTimeMachineData(TimeMachineData data) {
        System.out.println("Time machine data is set");
        timeMachineData = data;
        if (timeMachineData != null)
            timeMachineData.setPreviousCache(1);
        else
            System.out.println("Empty time machine");
    }

	public void setState(String state) {
		// TODO Auto-generated method stub
		
	}

	public String getState() {
		// TODO Auto-generated method stub
		return null;
	}
}
