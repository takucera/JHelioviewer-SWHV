package org.helioviewer.jhv.plugins.swek.sources.comesep;

import java.util.Iterator;
import java.util.Locale;

import org.helioviewer.jhv.base.time.TimeUtils;
import org.helioviewer.jhv.data.datatype.event.JHVEvent;
import org.helioviewer.jhv.data.datatype.event.JHVEventType;
import org.helioviewer.jhv.data.datatype.event.SWEKParser;
import org.json.JSONException;
import org.json.JSONObject;

public class ComesepParser implements SWEKParser {

    @Override
    public JHVEvent parseEventJSON(JSONObject json, JHVEventType type, int id, long start, long end, boolean full) throws JSONException {
        JHVEvent currentEvent = new JHVEvent(type, id, start, end);

        currentEvent.initParams();
        parseResult(json, currentEvent);
        currentEvent.finishParams();

        return currentEvent;
    }

    private static void parseResult(JSONObject result, JHVEvent currentEvent) throws JSONException {
        Iterator<String> keys = result.keys();
        while (keys.hasNext()) {
            parseParameter(result, keys.next(), currentEvent);
        }
    }

    private static void parseParameter(JSONObject result, String key, JHVEvent currentEvent) throws JSONException {
        if (result.isNull(key))
            return;

        String lowerKey = key.toLowerCase(Locale.ENGLISH);
        if (!(lowerKey.equals("atearliest") || lowerKey.equals("atlatest") ||
              lowerKey.equals("begin_time_value") || lowerKey.equals("end_time_value") ||
              lowerKey.startsWith("liftoff"))) {
            String value = result.optString(key).trim();
            if (!value.isEmpty()) {
                if (lowerKey.equals("atstrongest")) {
                    try {
                        value = TimeUtils.apiDateFormat.format(Long.parseLong(value) * 1000L);
                    } catch (Exception ignore) {
                    }
                }
                currentEvent.addParameter(lowerKey, value, true);
            }
        }
    }

}
