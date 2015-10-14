package org.helioviewer.jhv.gui.components.calendar;

import java.util.EventObject;

/**
 * An event which indicates that a component-defined action occured. This event
 * is generated by a component (such as a JHVCalendar) when the
 * component-specific action occurs (such as a date has been selected). The
 * event is passed to every JHVActionListener object that registered to receive
 * such events using the component's addJHVActionListener method.
 * 
 * @author Stephan Pagel
 */
@SuppressWarnings("serial")
public class JHVCalendarEvent extends EventObject {
    /**
     * @param source
     *            The object on which the Event initially occurred.
     */
    public JHVCalendarEvent(Object source) {
        super(source);
    }

}
