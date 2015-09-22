package org.helioviewer.jhv.gui.components.base;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.text.DefaultFormatter;

import org.helioviewer.base.logging.Log;

@SuppressWarnings("serial")
public class DegreeFormatterFactory extends AbstractFormatterFactory {

    private final String format;
    private final double min, max;

    public DegreeFormatterFactory(String format, double min, double max) {
        super();
        this.format = format;

        if (min > max)
            max = min;

        this.min = min;
        this.max = max;
    }

    @Override
    public AbstractFormatter getFormatter(JFormattedTextField tf) {
        return new DefaultFormatter() {
            @Override
            public Object stringToValue(String string) {
                Double value = new Double(0.);
                if (string != null && string.length() != 0) {
                    if (string.charAt(string.length() - 1) == '\u00B0') {
                        try {
                            value = Double.parseDouble(string.substring(0, string.length() - 1));
                        } catch (NumberFormatException ex2) {
                            Log.warn("Could not parse number");
                        }
                    } else {
                        try {
                            value = Double.parseDouble(string);
                        } catch (NumberFormatException ex) {
                            Log.warn("Could not parse number");
                        }
                    }
                }

                if (value < min)
                    value = min;
                else if (value > max)
                    value = max;

                return value;
            }

            @Override
            public String valueToString(Object value) {
                return String.format(format, value);
            }

            @Override
            public Class<?> getValueClass() {
                return Double.class;
            }
        };
    }

}
