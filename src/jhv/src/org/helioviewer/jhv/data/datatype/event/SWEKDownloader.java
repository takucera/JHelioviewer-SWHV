package org.helioviewer.jhv.data.datatype.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.helioviewer.jhv.base.DownloadStream;
import org.helioviewer.jhv.base.JSONUtils;
import org.helioviewer.jhv.base.interval.Interval;
import org.helioviewer.jhv.base.logging.Log;
import org.helioviewer.jhv.database.EventDatabase;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class SWEKDownloader {

    public boolean extern2db(JHVEventType eventType, long start, long end, List<SWEKParam> params) {
        ArrayList<Interval> range = EventDatabase.db2daterange(eventType);
        for (Interval interval : range) {
            if (interval.start <= start && interval.end >= end) {
                return true;
            }
        }

        int page = 0;
        boolean success = true;
        boolean overmax = true;
        try {
            while (overmax && success) {
                JSONObject eventJSON = JSONUtils.getJSONStream(new DownloadStream(createURL(eventType.getEventType(), start, end, params, page)).getInput());
                overmax = eventJSON.has("overmax") && eventJSON.getBoolean("overmax");
                success = parseEvents(eventJSON, eventType) && parseAssociations(eventJSON);
                page++;
            }
            return success;
        } catch (JSONException e) {
            Log.error("JSON parse error: " + e);
        } catch (IOException e) {
            Log.error("Could not create input stream for given URL error: " + e);
        }
        return false;
    }

    protected abstract boolean parseEvents(JSONObject eventJSON, JHVEventType type);

    protected abstract boolean parseAssociations(JSONObject eventJSON);

    protected abstract String createURL(SWEKEventType eventType, long start, long end, List<SWEKParam> params, int page);

}
