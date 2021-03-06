package org.helioviewer.jhv.plugins.eve.lines;

import java.net.URI;

import javax.annotation.Nullable;

import org.helioviewer.jhv.io.JSONUtils;
import org.helioviewer.jhv.threads.JHVWorker;
import org.helioviewer.jhv.timelines.Timelines;
import org.helioviewer.jhv.timelines.band.Band;
import org.helioviewer.jhv.timelines.band.BandType;
import org.helioviewer.jhv.timelines.draw.DrawController;

class LoadThread extends JHVWorker<EVEResponse, Void> {

    private final URI uri;

    LoadThread(URI _uri) {
        uri = _uri;
    }

    @Nullable
    @Override
    protected EVEResponse backgroundWork() {
        try {
            return new EVEResponse(JSONUtils.get(uri)/*.getJSONObject("org.helioviewer.jhv.request.timeline")*/);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void done() {
        if (!isCancelled()) {
            try {
                EVEResponse r = get();
                if (r != null) {
                    Band band = new Band(r.bandType == null ? BandType.getBandType(r.bandName) : r.bandType);
                    band.addToCache(r.values, r.dates);
                    Timelines.getLayers().add(band);
                    if (r.dates.length > 0)
                        DrawController.setSelectedInterval(r.dates[0], r.dates[r.dates.length - 1]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
