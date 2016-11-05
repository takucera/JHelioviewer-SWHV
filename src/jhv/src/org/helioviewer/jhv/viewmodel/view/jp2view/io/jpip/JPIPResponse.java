package org.helioviewer.jhv.viewmodel.view.jp2view.io.jpip;

import java.util.LinkedList;

import org.helioviewer.jhv.viewmodel.view.jp2view.io.http.HTTPResponse;

/**
 * A response to a JPIPRequest. Encapsulates both the HTTPResponse headers and
 * the JPIPDataSegments.
 *
 * @author caplins
 *
 */
public class JPIPResponse extends HTTPResponse {

    /** The status... could be EOR_WINDOW_DONE or EOR_IMAGE_DONE */
    private long status = -1;

    /** A list of the data segments. */
    private final LinkedList<JPIPDataSegment> jpipDataList = new LinkedList<>();
    private long size = 0;

    /**
     * Used to form responses.
     *
     * @param res
     * @throws IOException
     */
    public JPIPResponse(HTTPResponse res) {
        super(res.getCode(), res.getReason());

        for (String key : res.getHeaders())
            this.setHeader(key, res.getHeader(key));
    }

    /**
     * Adds the data segment to this object.
     *
     * @param data
     */
    public void addJpipDataSegment(JPIPDataSegment data) {
        if (data.isEOR) {
            status = data.binID;
        }
        jpipDataList.add(data);
        size += data.length;
    }

    /**
     * Removes a data segment from this object.
     *
     * @return The removed data segment, null if the list was empty
     */
    public JPIPDataSegment removeJpipDataSegment() {
        if (!jpipDataList.isEmpty()) {
            JPIPDataSegment jpr = jpipDataList.remove();
            size -= jpr.length;
            return jpr;
        }
        return null;
    }

    /**
     * Determines the response size.
     *
     * @return Response size
     */
    public long getResponseSize() {
        return size;
    }

    /**
     * Tells if the response completes the last request.
     *
     * @return True, if the response is complete
     */
    public boolean isResponseComplete() {
        return (status == JPIPConstants.EOR_WINDOW_DONE || status == JPIPConstants.EOR_IMAGE_DONE);
    }

}
