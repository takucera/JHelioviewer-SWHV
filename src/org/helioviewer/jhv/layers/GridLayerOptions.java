package org.helioviewer.jhv.layers;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Objects;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import org.helioviewer.jhv.display.Display;
import org.helioviewer.jhv.gui.ComponentUtils;
import org.helioviewer.jhv.gui.components.base.TerminatedFormatterFactory;
import org.helioviewer.jhv.gui.components.base.WheelSupport;

@SuppressWarnings("serial")
class GridLayerOptions extends JPanel {

    private static final double min = 5, max = 90;

    private JSpinner gridResolutionXSpinner;
    private JSpinner gridResolutionYSpinner;
    private JComboBox<GridLayer.GridType> gridTypeBox;
    private final GridLayer grid;

    GridLayerOptions(GridLayer _grid) {
        grid = _grid;
        createGridResolutionX();
        createGridResolutionY();

        setLayout(new GridBagLayout());

        GridBagConstraints c0 = new GridBagConstraints();
        c0.fill = GridBagConstraints.HORIZONTAL;
        c0.weightx = 1.;
        c0.weighty = 1.;

        c0.gridy = 0;

        c0.gridx = 1;
        c0.anchor = GridBagConstraints.EAST;
        JCheckBox axis = new JCheckBox("Solar axis", grid.getShowAxis());
        axis.setHorizontalTextPosition(SwingConstants.LEFT);
        axis.addActionListener(e -> {
            grid.showAxis(axis.isSelected());
            Display.display();
        });
        add(axis, c0);

        c0.gridx = 3;
        c0.anchor = GridBagConstraints.EAST;
        JCheckBox labels = new JCheckBox("Grid labels", grid.getShowLabels());
        labels.setHorizontalTextPosition(SwingConstants.LEFT);
        labels.addActionListener(e -> {
            grid.showLabels(labels.isSelected());
            Display.display();
        });
        add(labels, c0);

        c0.gridy = 1;

        c0.gridx = 1;
        c0.anchor = GridBagConstraints.EAST;
        JCheckBox radial = new JCheckBox("Radial grid", grid.getShowRadial());
        radial.setHorizontalTextPosition(SwingConstants.LEFT);
        radial.addActionListener(e -> {
            grid.showRadial(radial.isSelected());
            Display.display();
        });
        add(radial, c0);

        c0.gridx = 2;
        c0.anchor = GridBagConstraints.EAST;
        add(new JLabel("Grid type", JLabel.RIGHT), c0);
        c0.gridx = 3;
        c0.anchor = GridBagConstraints.WEST;
        createGridTypeBox();
        add(gridTypeBox, c0);

        c0.gridy = 2;

        c0.gridx = 0;
        c0.anchor = GridBagConstraints.EAST;
        add(new JLabel("Longitude", JLabel.RIGHT), c0);

        JFormattedTextField fx = ((JSpinner.DefaultEditor) gridResolutionXSpinner.getEditor()).getTextField();
        fx.setFormatterFactory(new TerminatedFormatterFactory("%.1f", "\u00B0", min, max));

        c0.gridx = 1;
        c0.anchor = GridBagConstraints.WEST;
        add(gridResolutionXSpinner, c0);

        c0.gridx = 2;
        c0.anchor = GridBagConstraints.EAST;
        add(new JLabel("Latitude", JLabel.RIGHT), c0);

        JFormattedTextField fy = ((JSpinner.DefaultEditor) gridResolutionYSpinner.getEditor()).getTextField();
        fy.setFormatterFactory(new TerminatedFormatterFactory("%.1f", "\u00B0", min, max));

        c0.gridx = 3;
        c0.anchor = GridBagConstraints.WEST;
        add(gridResolutionYSpinner, c0);

        ComponentUtils.smallVariant(this);
    }

    private void createGridTypeBox() {
        gridTypeBox = new JComboBox<>(GridLayer.GridType.values());
        gridTypeBox.setSelectedItem(grid.getGridType());
        gridTypeBox.addActionListener(e -> {
            grid.setGridType((GridLayer.GridType) Objects.requireNonNull(gridTypeBox.getSelectedItem()));
            Display.display();
        });
    }

    private void createGridResolutionX() {
        gridResolutionXSpinner = new JSpinner(new SpinnerNumberModel(Double.valueOf(grid.getLonStep()), Double.valueOf(min), Double.valueOf(max), Double.valueOf(0.1)));
        gridResolutionXSpinner.addChangeListener(e -> {
            grid.setLonStep((Double) gridResolutionXSpinner.getValue());
            Display.display();
        });
        WheelSupport.installMouseWheelSupport(gridResolutionXSpinner);
    }

    private void createGridResolutionY() {
        gridResolutionYSpinner = new JSpinner(new SpinnerNumberModel(Double.valueOf(grid.getLatStep()), Double.valueOf(min), Double.valueOf(max), Double.valueOf(0.1)));
        gridResolutionYSpinner.addChangeListener(e -> {
            grid.setLatStep((Double) gridResolutionYSpinner.getValue());
            Display.display();
        });
        WheelSupport.installMouseWheelSupport(gridResolutionYSpinner);
    }

}
