package org.helioviewer.jhv.plugins.swhvhekplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.helioviewer.base.math.Interval;
import org.helioviewer.jhv.layers.LayersListener;
import org.helioviewer.jhv.layers.LayersModel;
import org.helioviewer.jhv.plugins.swhvhekplugin.cache.SWHVHEKData;
import org.helioviewer.viewmodel.changeevent.ChangeEvent;
import org.helioviewer.viewmodel.changeevent.SubImageDataChangedReason;
import org.helioviewer.viewmodel.view.View;
import org.helioviewer.viewmodelplugin.overlay.OverlayPanel;

/**
 * Represents the UI components which manage the HEK event catalog.
 *
 * @author Malte Nuhn
 * */
public class SWHVHEKPluginPanel extends OverlayPanel implements ActionListener, LayersListener {

    private static final long serialVersionUID = 1L;

    // UI Components
    private final JPanel buttonPanel = new JPanel(new BorderLayout());
    private final JProgressBar progressBar = new JProgressBar();
    private final JButton cancelButton = new JButton(new ImageIcon(SWHVHEKPlugin.getResourceUrl("/images/hekCancel.png")));
    private final JButton reloadButton = new JButton(new ImageIcon(SWHVHEKPlugin.getResourceUrl("/images/hekReload.png")));

    /**
     * Default constructor
     *
     * @param hekCache
     * */
    public SWHVHEKPluginPanel() {
        SWHVHEKData.getSingletonInstance();

        // set up visual components
        initVisualComponents();

        // register as layers listener
        LayersModel.getSingletonInstance().addLayersListener(this);
    }

    /**
     * Force a redraw of the main window
     */
    private void fireRedraw() {
        LayersModel.getSingletonInstance().viewChanged(null, new ChangeEvent(new SubImageDataChangedReason(null)));
    }

    /**
     * Update the plugin's currently displayed interval.
     *
     * The plugin is currently stafeFUL, so keep in mind that just calling this
     * method without triggering any other update method might not be a good
     * decision.
     *
     * @param newInterval
     *            - the interval that should be displayed
     *
     * @see org.helioviewer.jhv.plugins.overlay.hek.cache.HEKCacheModel#setCurInterval
     *
     */
    public void setCurInterval(Interval<Date> newPosition) {

    }

    /**
     * Request the plugin to download and display the Events available in the
     * catalogue
     *
     * The interval to be requested depends on the current state of the plug-in.
     *
     * @see org.helioviewer.jhv.plugins.overlay.hek.cache.HEKCacheController#requestStructure
     *
     */
    public void getStructure() {
    }

    /**
     * Sets up the visual sub components and the visual part of the component
     * itself.
     * */
    private void initVisualComponents() {

        // set general appearance
        setLayout(new GridBagLayout());

        this.setPreferredSize(new Dimension(150, 200));

        progressBar.setIndeterminate(true);

        cancelButton.addActionListener(this);
        reloadButton.addActionListener(this);

        setEnabled(true);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;

        GridBagConstraints c2 = new GridBagConstraints();
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.weightx = 1.0;
        c2.weighty = 0.0;
        c2.gridx = 0;
        c2.gridy = 1;

        this.add(progressBar, c2);
        this.setLoading(false);

        buttonPanel.add(reloadButton, BorderLayout.EAST);
        buttonPanel.add(cancelButton, BorderLayout.EAST);

        GridBagConstraints c3 = new GridBagConstraints();
        c3.fill = GridBagConstraints.NONE;
        c3.anchor = GridBagConstraints.EAST;
        c3.weightx = 0.0;
        c3.weighty = 0.0;
        c3.gridx = 1;
        c3.gridy = 1;

        this.add(reloadButton, c3);
        this.add(cancelButton, c3);
    }

    /**
     * Updates components.
     * */
    public void updateComponents() {
    }

    @Override
    public void actionPerformed(ActionEvent act) {

        if (act.getSource().equals(reloadButton)) {
            getStructure();
        }

        if (act.getActionCommand().equals("request")) {
        }

    }

    @Override
    public void setEnabled(boolean b) {
        // super.setEnabled(b);
    }

    @Override
    public void activeLayerChanged(int idx) {
        View view = LayersModel.getSingletonInstance().getActiveView();
    }

    @Override
    public void layerAdded(int idx) {
        Thread threadUpdate = new Thread(new Runnable() {
            @Override
            public void run() {
                Date start = LayersModel.getSingletonInstance().getFirstDate();
                Date end = LayersModel.getSingletonInstance().getLastDate();
                if (start != null && end != null) {
                    Interval<Date> range = new Interval<Date>(start, end);
                    getStructure();
                }
            }
        }, "HEKLAYERADDED");
        threadUpdate.start();
    }

    @Override
    public void layerChanged(int idx) {
    }

    @Override
    public void layerRemoved(View oldView, int oldIdx) {
    }

    @Override
    public void subImageDataChanged() {
    }

    @Override
    public void timestampChanged(int idx) {
        // Not used anymore
    }

    @Override
    public void viewportGeometryChanged() {
    }

    private void setLoading(boolean loading) {
        progressBar.setVisible(loading);
        cancelButton.setVisible(loading);
        reloadButton.setVisible(!loading);
    }

    /**
     * {@inheritDoc}
     */
    public void regionChanged() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void layerDownloaded(int idx) {
    }
}
