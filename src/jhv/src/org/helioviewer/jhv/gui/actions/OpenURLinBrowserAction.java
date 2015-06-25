package org.helioviewer.jhv.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.helioviewer.jhv.JHVGlobals;

/**
 * Action to open a browser showing any given URL.
 * This function is platform dependent and tries to open the URL in a browser.
 * @author Markus Langenberg
 */
@SuppressWarnings("serial")
public class OpenURLinBrowserAction extends AbstractAction {

    private String urlToOpen;

    /**
     * @param name
     *            name of the action that should be displayed on a button
     * @param url
     *            URL to open on click
     */
    public OpenURLinBrowserAction(String name, String url) {
        super(name);
        urlToOpen = url;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JHVGlobals.openURL(urlToOpen);
    }

}
