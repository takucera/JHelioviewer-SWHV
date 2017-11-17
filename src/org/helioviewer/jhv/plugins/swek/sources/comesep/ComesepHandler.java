package org.helioviewer.jhv.plugins.swek.sources.comesep;

import java.util.ArrayList;
import java.util.List;

import org.helioviewer.jhv.base.JSONUtils;
import org.helioviewer.jhv.base.Pair;
import org.helioviewer.jhv.data.event.JHVEvent;
import org.helioviewer.jhv.data.event.SWEKGroup;
import org.helioviewer.jhv.data.event.SWEKHandler;
import org.helioviewer.jhv.data.event.SWEKParam;
import org.helioviewer.jhv.data.event.SWEKSupplier;
import org.helioviewer.jhv.database.EventDatabase;
import org.helioviewer.jhv.time.TimeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unchecked")
public class ComesepHandler extends SWEKHandler {

    private static final String _baseurl = "http://swhv.oma.be/comesep/comeseprequestapi/getComesep.php?";

    @Override
    protected boolean parseRemote(JSONObject eventJSON, SWEKSupplier supplier) {
        JSONArray results = eventJSON.getJSONArray("results");
        ArrayList<EventDatabase.Event2Db> event2db_list = new ArrayList<>();
        try {
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                byte[] compressed = JSONUtils.compressJSON(result);

                long start = result.getLong("atearliest") * 1000;
                long end = result.getLong("atlatest") * 1000;
                if (result.has("liftoffduration_value")) {
                    long cactusLiftOff = result.getLong("liftoffduration_value");
                    end += cactusLiftOff * 60000;
                }

                long archiv = start;
                String uid = result.getString("alertid");
                event2db_list.add(new EventDatabase.Event2Db(compressed, start, end, archiv, uid, new ArrayList<>()));
            }
            EventDatabase.dump_event2db(event2db_list, supplier);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected boolean parseAssociations(JSONObject eventJSON) {
        JSONArray associations = eventJSON.getJSONArray("associations");
        int len = associations.length();
        Pair<?,?>[] assocs = new Pair<?,?>[len];
        for (int i = 0; i < len; i++) {
            JSONObject asobj = associations.getJSONObject(i);
            assocs[i] = new Pair<>(asobj.getString("parent"), asobj.getString("child"));
        }
        return EventDatabase.dump_association2db((Pair<String,String>[]) assocs) != -1;
    }

    @Override
    protected String createURL(SWEKGroup group, long start, long end, List<SWEKParam> params, int page) {
        StringBuilder baseURL = new StringBuilder(_baseurl);
        baseURL = appendModel(baseURL, params).append('&');
        baseURL.append("startdate=").append(TimeUtils.format(start)).append('&');
        baseURL.append("enddate=").append(TimeUtils.format(end)).append('&');
        return baseURL.toString();
    }

    private static StringBuilder appendModel(StringBuilder baseURL, List<SWEKParam> params) {
        String model = "";
        for (SWEKParam p : params) {
            if (p.param.equals("provider")) {
                model = p.value;
                break;
            }
        }
        return baseURL.append("model=").append(model);
    }

    @Override
    public JHVEvent parseEventJSON(JSONObject json, SWEKSupplier supplier, int id, long start, long end, boolean full) throws JSONException {
        JHVEvent currentEvent = new JHVEvent(supplier, id, start, end);

        ComesepParser.parseResult(json, currentEvent);
        currentEvent.finishParams();

        return currentEvent;
    }

}
