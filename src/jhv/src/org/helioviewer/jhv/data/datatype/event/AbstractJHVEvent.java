package org.helioviewer.jhv.data.datatype.event;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractJHVEvent implements JHVEvent {

    protected boolean highlighted;
    protected Set<JHVEventHighlightListener> listeners;

    public AbstractJHVEvent() {
        highlighted = false;
        listeners = new HashSet<JHVEventHighlightListener>();
    }

    @Override
    public void highlight(boolean isHighlighted) {
        if (isHighlighted != highlighted) {
            highlighted = isHighlighted;
            fireHighlightChanged();
        }
    }

    @Override
    public void addHighlightListener(JHVEventHighlightListener l) {
        listeners.add(l);
    }

    @Override
    public void removeHighlightListener(JHVEventHighlightListener l) {
        listeners.remove(l);
    }

    @Override
    public boolean isHighlighted() {
        return highlighted;
    }

    private void fireHighlightChanged() {
        for (JHVEventHighlightListener l : listeners) {
            l.eventHightChanged(AbstractJHVEvent.this);
        }
    }
}
