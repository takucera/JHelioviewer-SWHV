package org.helioviewer.jhv.data.container.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.helioviewer.jhv.base.cache.RequestCache;
import org.helioviewer.jhv.base.interval.Interval;
import org.helioviewer.jhv.data.datatype.event.JHVAssociation;
import org.helioviewer.jhv.data.datatype.event.JHVEvent;
import org.helioviewer.jhv.data.datatype.event.JHVEventType;
import org.helioviewer.jhv.data.datatype.event.JHVRelatedEvents;
import org.helioviewer.jhv.data.datatype.event.SWEKEventType;
import org.helioviewer.jhv.data.datatype.event.SWEKSupplier;

public class JHVEventCache {

    /** singleton instance of JHVevent cache */
    private static JHVEventCache instance;

    /** The events received for a certain date */
    public static class SortedDateInterval implements Comparable<SortedDateInterval> {
        public long start;
        public long end;
        private final int id;
        private static int id_gen = Integer.MIN_VALUE;

        public SortedDateInterval(long _start, long _end) {
            start = _start;
            end = _end;
            id = id_gen++;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof SortedDateInterval) {
                return compareTo((SortedDateInterval) o) == 0;
            }
            return false;
        }

        @Override
        public int compareTo(SortedDateInterval o2) {
            if (start < o2.start) {
                return -1;
            } else if (start == o2.start && end < o2.end) {
                return -1;
            } else if (start == o2.start && end == o2.end && o2.id < id) {
                return -1;
            } else if (start == o2.start && end == o2.end && o2.id == id) {
                return 0;
            }
            return 1;
        }
    }

    private final Map<JHVEventType, SortedMap<SortedDateInterval, JHVRelatedEvents>> events;

    private final Map<Integer, JHVRelatedEvents> relEvents = new HashMap<Integer, JHVRelatedEvents>();;

    private final Set<JHVEventType> activeEventTypes;

    private final Map<JHVEventType, RequestCache> downloadedCache;

    private final Map<Integer, ArrayList<JHVAssociation>> assoLeft = new HashMap<Integer, ArrayList<JHVAssociation>>();
    private final Map<Integer, ArrayList<JHVAssociation>> assoRight = new HashMap<Integer, ArrayList<JHVAssociation>>();

    /**
     * private default constructor
     */
    private JHVEventCache() {
        events = new HashMap<JHVEventType, SortedMap<SortedDateInterval, JHVRelatedEvents>>();
        activeEventTypes = new HashSet<JHVEventType>();
        downloadedCache = new HashMap<JHVEventType, RequestCache>();
    }

    /**
     * Gets the singleton instance of the JHV event cache.
     *
     * @return singleton instance of the cache.
     */
    public static JHVEventCache getSingletonInstance() {
        if (instance == null) {
            instance = new JHVEventCache();
        }
        return instance;
    }

    public void add(JHVEvent event) {
        Integer id = event.getUniqueID();
        if (relEvents.containsKey(id)) {
            relEvents.get(id).swapEvent(event, events);
            return;
        }
        checkAssociation(true, event);
        checkAssociation(false, event);
    }

    public JHVRelatedEvents getRelatedEvents(int id) {
        return relEvents.get(id);
    }

    private void checkAssociation(boolean isLeft, JHVEvent event) {
        Map<Integer, ArrayList<JHVAssociation>> assoList = isLeft ? assoLeft : assoRight;
        Map<Integer, ArrayList<JHVAssociation>> assoOther = isLeft ? assoRight : assoLeft;

        Integer uid = event.getUniqueID();
        if (assoList.containsKey(uid)) {
            for (Iterator<JHVAssociation> iterator = assoList.get(uid).iterator(); iterator.hasNext();) {
                JHVAssociation tocheck = iterator.next();
                Integer founduid = isLeft ? tocheck.right : tocheck.left;

                JHVRelatedEvents found = relEvents.get(founduid);
                if (found != null) {
                    if (relEvents.containsKey(uid)) {
                        JHVRelatedEvents revent = relEvents.get(uid);
                        merge(revent, relEvents.get(founduid));
                        revent.addAssociation(tocheck);

                        iterator.remove();
                        for (Iterator<JHVAssociation> it = assoOther.get(founduid).iterator(); it.hasNext();) {
                            JHVAssociation checkrem = it.next();
                            Integer side = isLeft ? checkrem.left : checkrem.right;
                            if (side.equals(uid)) {
                                it.remove();
                                break;
                            }
                        }
                        if (assoOther.get(founduid).isEmpty()) {
                            assoOther.remove(founduid);
                        }
                    } else {
                        createNewRelatedEvent(event);
                    }
                }
            }
            if (assoList.get(uid).isEmpty()) {
                assoList.remove(uid);
            }
        } else {
            createNewRelatedEvent(event);
        }
    }

    private void createNewRelatedEvent(JHVEvent event) {
        if (relEvents.containsKey(event.getUniqueID())) {
            return;
        }
        JHVRelatedEvents revent = new JHVRelatedEvents(event, events);
        relEvents.put(event.getUniqueID(), revent);
    }

    private void merge(JHVRelatedEvents current, JHVRelatedEvents found) {
        if (current == found) {
            return;
        }
        current.merge(found, events);
        for (JHVEvent foundev : found.getEvents()) {
            Integer key = foundev.getUniqueID();
            relEvents.remove(key);
            relEvents.put(key, current);
        }
    }

    private void addAssociation(boolean isLeft, JHVAssociation association) {
        Integer key = isLeft ? association.left : association.right;
        Map<Integer, ArrayList<JHVAssociation>> assoMap = isLeft ? assoLeft : assoRight;
        ArrayList<JHVAssociation> assocs = assoMap.get(key);
        if (assocs == null) {
            assocs = new ArrayList<JHVAssociation>();
            assoMap.put(key, assocs);
        }
        assocs.add(association);
    }

    public void add(JHVAssociation association) {
        if (relEvents.containsKey(association.left) && relEvents.containsKey(association.right)) {
            JHVRelatedEvents ll = relEvents.get(association.left);
            JHVRelatedEvents rr = relEvents.get(association.right);
            if (ll != rr) {
                merge(ll, rr);
                ll.addAssociation(association);
            }
        } else {
            boolean alreadyin = false;
            if (assoLeft.containsKey(association.left)) {
                ArrayList<JHVAssociation> res = assoLeft.get(association.left);
                for (JHVAssociation el : res) {
                    if (el.right.equals(association.right)) {
                        alreadyin = true;
                        break;
                    }
                }
            }
            if (!alreadyin) {
                addAssociation(true, association);
                addAssociation(false, association);
            }
        }

    }

    public JHVEventCacheResult get(Date startDate, Date endDate, Date extendedStart, Date extendedEnd) {

        Map<JHVEventType, SortedMap<SortedDateInterval, JHVRelatedEvents>> eventsResult = new HashMap<JHVEventType, SortedMap<SortedDateInterval, JHVRelatedEvents>>();
        Map<JHVEventType, List<Interval>> missingIntervals = new HashMap<JHVEventType, List<Interval>>();
        for (JHVEventType evt : activeEventTypes) {
            SortedMap<SortedDateInterval, JHVRelatedEvents> sortedEvents = events.get(evt);
            if (sortedEvents != null) {
                long delta = 1000 * 60 * 60 * 24;
                SortedMap<SortedDateInterval, JHVRelatedEvents> submap = sortedEvents.subMap(new SortedDateInterval(startDate.getTime() - delta, startDate.getTime() - delta), new SortedDateInterval(endDate.getTime() + delta, endDate.getTime() + delta));
                eventsResult.put(evt, submap);
            }
            List<Interval> missing = downloadedCache.get(evt).getMissingIntervals(new Interval(startDate, endDate));
            if (!missing.isEmpty()) {
                missing = downloadedCache.get(evt).adaptRequestCache(extendedStart, extendedEnd);
                missingIntervals.put(evt, missing);
            }
        }
        return new JHVEventCacheResult(eventsResult, missingIntervals);
    }

    public void removeEventType(JHVEventType eventType, boolean keepActive) {
        if (!keepActive) {
            activeEventTypes.remove(eventType);
        } else {
            deleteFromCache(eventType);
        }
    }

    private void deleteFromCache(JHVEventType eventType) {
        RequestCache cache = new RequestCache();
        downloadedCache.put(eventType, cache);
        events.remove(eventType);
        Iterator it = relEvents.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, JHVRelatedEvents> pair = (Map.Entry) it.next();
            if (pair.getValue().getJHVEventType() == eventType) {
                it.remove();
            }
        }
    }

    public Collection<Interval> getAllRequestIntervals(JHVEventType eventType) {
        return downloadedCache.get(eventType).getAllRequestIntervals();
    }

    public void removeRequestedIntervals(JHVEventType eventType, Interval interval) {
        downloadedCache.get(eventType).removeRequestedIntervals(interval);
    }

    public void eventTypeActivated(JHVEventType eventType) {
        activeEventTypes.add(eventType);
        if (!downloadedCache.containsKey(eventType)) {
            RequestCache cache = new RequestCache();
            downloadedCache.put(eventType, cache);
        }
    }

    public void reset(SWEKEventType eventType) {
        for (SWEKSupplier supplier : eventType.getSuppliers()) {
            JHVEventType evt = JHVEventType.getJHVEventType(eventType, supplier);
            downloadedCache.remove(evt);
            downloadedCache.put(evt, new RequestCache());
        }
    }

}
