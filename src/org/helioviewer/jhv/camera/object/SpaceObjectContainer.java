package org.helioviewer.jhv.camera.object;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.helioviewer.jhv.astronomy.SpaceObject;
import org.helioviewer.jhv.astronomy.UpdateViewpoint;
import org.helioviewer.jhv.gui.components.base.JHVTableCellRenderer;
import org.json.JSONArray;

@SuppressWarnings("serial")
public class SpaceObjectContainer extends JScrollPane {

    private static final int ICON_WIDTH = 12;
    private static final int NUMBEROFVISIBLEROWS = 5;

    private static final int SELECTED_COL = 0;
    private static final int OBJECT_COL = 1;
    private static final int STATUS_COL = 2;

    private final UpdateViewpoint uv;
    private final String frame;
    private final boolean exclusive;

    private final SpaceObjectModel model = new SpaceObjectModel();

    private long startTime;
    private long endTime;

    public SpaceObjectContainer(JSONArray ja, UpdateViewpoint _uv, String _frame, boolean _exclusive, long _startTime, long _endTime) {
        uv = _uv;
        frame = _frame;
        exclusive = _exclusive;
        startTime = _startTime;
        endTime = _endTime;

        JTable grid = new JTable(model);
        grid.setTableHeader(null);
        grid.setShowGrid(false);
        grid.setRowSelectionAllowed(true);
        grid.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        grid.setColumnSelectionAllowed(false);
        grid.setIntercellSpacing(new Dimension(0, 0));

        if (exclusive)
            grid.getColumnModel().getColumn(SELECTED_COL).setCellRenderer(new SelectedExclusiveRenderer());
        else
            grid.getColumnModel().getColumn(SELECTED_COL).setCellRenderer(new SelectedRenderer());
        grid.getColumnModel().getColumn(SELECTED_COL).setPreferredWidth(ICON_WIDTH + 8);
        grid.getColumnModel().getColumn(SELECTED_COL).setMaxWidth(ICON_WIDTH + 8);
        grid.getColumnModel().getColumn(OBJECT_COL).setCellRenderer(new ObjectRenderer());
        grid.getColumnModel().getColumn(STATUS_COL).setCellRenderer(new StatusRenderer());

        grid.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point pt = e.getPoint();
                int row = grid.rowAtPoint(pt);
                if (row < 0)
                    return;

                int col = grid.columnAtPoint(pt);
                if (col == SELECTED_COL)
                    selectElement((SpaceObjectElement) grid.getValueAt(row, col));
            }
        });

        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        setViewportView(grid);
        getViewport().setBackground(grid.getBackground());
        setPreferredSize(new Dimension(-1, getGridRowHeight(grid) * NUMBEROFVISIBLEROWS + 1));
        grid.setRowHeight(getGridRowHeight(grid));

        uv.clear();
        for (int i = 0; i < ja.length(); i++)
            selectObject(SpaceObject.get(ja.optString(i, "Earth")));
    }

    private void selectObject(SpaceObject object) {
        SpaceObjectElement element = model.elementOf(object);
        if (element != null) // found
            selectElement(element);
    }

    public void setTime(long _startTime, long _endTime) {
        if (startTime == _startTime && endTime == _endTime)
            return;

        startTime = _startTime;
        endTime = _endTime;
        for (SpaceObjectElement element : model.getSelected())
            element.load(uv, frame, startTime, endTime);
    }

    private void selectElement(SpaceObjectElement element) {
        if (exclusive) {
            for (SpaceObjectElement e : model.getSelected())
                e.unload(uv);
            element.load(uv, frame, startTime, endTime);
        } else {
            if (element.isSelected())
                element.unload(uv);
            else
                element.load(uv, frame, startTime, endTime);
        }
    }

    private int rowHeight = -1;

    private int getGridRowHeight(JTable grid) {
        if (rowHeight == -1) {
            rowHeight = grid.getRowHeight() + 4;
        }
        return rowHeight;
    }

    public JSONArray toJson() {
        JSONArray ja = new JSONArray();
        for (SpaceObjectElement element : model.getSelected())
            ja.put(element);
        return ja;
    }

    private static class ObjectRenderer extends JHVTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof SpaceObjectElement) {
                SpaceObjectElement element = (SpaceObjectElement) value;
                label.setText(element.toString());
                label.setBorder(((SpaceObjectElement) value).getObject().getBorder());
            }
            return label;
        }
    }

    private static class SelectedRenderer extends JHVTableCellRenderer {

        private final JCheckBox checkBox = new JCheckBox();

        SelectedRenderer() {
            setHorizontalAlignment(CENTER);
            checkBox.putClientProperty("JComponent.sizeVariant", "small");
            checkBox.setBorderPainted(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof SpaceObjectElement) {
                SpaceObjectElement element = (SpaceObjectElement) value;
                checkBox.setSelected(element.isSelected());
                checkBox.setBorder(element.getObject().getBorder());
            }
            checkBox.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return checkBox;
        }

    }

    private static class SelectedExclusiveRenderer extends JHVTableCellRenderer {

        private final JRadioButton radio = new JRadioButton();

        SelectedExclusiveRenderer() {
            setHorizontalAlignment(CENTER);
            radio.putClientProperty("JComponent.sizeVariant", "small");
            radio.setBorderPainted(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof SpaceObjectElement) {
                SpaceObjectElement element = (SpaceObjectElement) value;
                radio.setSelected(element.isSelected());
                radio.setBorder(element.getObject().getBorder());
            }
            radio.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return radio;
        }

    }

    private static class StatusRenderer extends JHVTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof SpaceObjectElement) {
                SpaceObjectElement element = (SpaceObjectElement) value;
                label.setText(element.getStatus());
                label.setBorder(((SpaceObjectElement) value).getObject().getBorder());
            }
            return label;
        }
    }

}
