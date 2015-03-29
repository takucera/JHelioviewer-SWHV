package org.helioviewer.jhv.gui.components.layerTable;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.helioviewer.jhv.layers.LayersListener;
import org.helioviewer.jhv.layers.LayersModel;
import org.helioviewer.viewmodel.view.LayeredView;
import org.helioviewer.viewmodel.view.View;
import org.helioviewer.viewmodel.view.jp2view.JHVJP2View;

/**
 * A TableModel representing the state of visible Layers, internally using the
 * LayersModel
 *
 * @author Malte Nuhn
 *
 */
public class LayerTableModel extends AbstractTableModel implements LayersListener {

    private static final long serialVersionUID = 1167923521718778146L;

    public static final int COLUMN_VISIBILITY = 0;
    public static final int COLUMN_TITLE = 1;
    public static final int COLUMN_TIMESTAMP = 2;
    public static final int COLUMN_BUTTON_REMOVE = 3;

    private static final LayerTableModel layerTableModel = new LayerTableModel();

    private final LayersModel layersModel = LayersModel.getSingletonInstance();
    private final LayeredView layeredView = layersModel.getLayeredView();
    private static final ArrayList<JHVJP2View> views = new ArrayList<JHVJP2View>();

    /**
     * Returns the only instance of this class.
     *
     * @return the only instance of this class.
     * */
    public static LayerTableModel getSingletonInstance() {
        return layerTableModel;
    }

    private LayerTableModel() {
        layersModel.addLayersListener(this);
    }

    public void setVisible(int index, boolean visible) {
        if (index >= 0 && index < views.size()) {
            layersModel.setVisibleLink(views.get(index), visible);
        }
    }

    public boolean isVisible(int index) {
        if (index >= 0 && index < views.size()) {
            return layersModel.isVisible(views.get(index));
        }
        return false;
    }

    public void removeLayer(int index) {
        if (index >= 0 && index < views.size()) {
            layersModel.removeLayer(views.get(index));
        }
    }

    public View getViewAt(int index) {
        if (index >= 0 && index < views.size()) {
            return views.get(index);
        }
        return null;
    }

    public void moveLayerUp(int index) {
        if (index >= 0 && index < views.size()) {
            int newLevel = index;
            if (newLevel < views.size() - 1) {
                newLevel++;
            }

            layeredView.moveView(views.get(index), newLevel);
            updateData();
            layersModel.setActiveLayer(views.get(newLevel));
            fireTableRowsUpdated(0, views.size()); // tbd
        }
    }

    public void moveLayerDown(int index) {
        if (index >= 0 && index < views.size()) {
            int newLevel = index;
            if (newLevel > 0) {
                newLevel--;
            }

            layeredView.moveView(views.get(index), newLevel);
            updateData();
            layersModel.setActiveLayer(views.get(newLevel));
            fireTableRowsUpdated(0, views.size()); // tbd
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowCount() {
        return views.size();
    }

    /**
     * {@inheritDoc} Hardcoded value of columns. This value is dependent on the
     * actual design of the LayerTable
     */
    @Override
    public int getColumnCount() {
        return 4;
    }

    /**
     * Return the LayerDescriptor for the given row of the table, regardless
     * which column is requested.
     */
    @Override
    public Object getValueAt(int idx, int col) {
        if (idx >= 0 && idx < views.size()) {
            return layersModel.getDescriptor(views.get(idx));
        }
        return null;
    }

    /**
     * Method part of the LayersListener interface, itself calling the
     * appropriate TableModel notification methods
     */
    @Override
    public void layerAdded(int newIndex) {
        updateData();
        fireTableRowsInserted(newIndex, newIndex);
    }

    /**
     * Method part of the LayersListener interface, itself calling the
     * appropriate TableModel notification methods
     */
    @Override
    public void layerRemoved(View oldView, int oldIndex) {
        updateData();
        fireTableRowsDeleted(oldIndex, oldIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activeLayerChanged(View view) {
    }

    public void layerVisibilityChanged(int idx) {
        fireTableRowsUpdated(idx, idx);
    }

    private void updateData() {
        views.clear();
        for (int i = layeredView.getNumLayers() - 1; i >= 0; i--) {
            views.add(layeredView.getLayer(i));
        }
    }

}
