package com.stream_pi.server.window.helper;

import com.stream_pi.action_api.actionproperty.property.ControlType;
import com.stream_pi.action_api.actionproperty.property.ListValue;
import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.util.exception.MinorException;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.util.Callback;

public class Helper
{
    public static Node getControlNode(Property property) throws MinorException
    {
        if(property.getControlType() == ControlType.COMBO_BOX)
        {
            ComboBox<ListValue> comboBox = new ComboBox<>();
            comboBox.getItems().addAll(property.getListValue());

            Callback<ListView<ListValue>, ListCell<ListValue>> clientsComboBoxFactory = new Callback<>() {
                @Override
                public ListCell<ListValue> call(ListView<ListValue> clientConnectionListView) {

                    return new ListCell<>() {
                        @Override
                        protected void updateItem(ListValue listValue, boolean b)
                        {
                            super.updateItem(listValue, b);

                            if(listValue == null)
                            {
                                setText("Choose value");
                            }
                            else
                            {
                                setText(listValue.getDisplayName());
                            }
                        }
                    };
                }
            };
            comboBox.setCellFactory(clientsComboBoxFactory);
            comboBox.setButtonCell(clientsComboBoxFactory.call(null));

            comboBox.getSelectionModel().select(property.getSelectedIndex());
            return comboBox;
        }
        else if(property.getControlType() == ControlType.TEXT_FIELD)
        {
            return new TextField(property.getRawValue());
        }
        else if(property.getControlType() == ControlType.TEXT_FIELD_MASKED)
        {
            PasswordField textField = new PasswordField();
            textField.setText(property.getRawValue());

            return textField;
        }
        else if(property.getControlType() == ControlType.TOGGLE)
        {
            ToggleButton toggleButton = new ToggleButton();
            toggleButton.setSelected(property.getBoolValue());

            if(property.getBoolValue())
                toggleButton.setText("ON");
            else
                toggleButton.setText("OFF");

            toggleButton.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
                if(t1)
                    toggleButton.setText("ON");
                else
                    toggleButton.setText("OFF");
            });

            return toggleButton;
        }
        else if(property.getControlType() == ControlType.SLIDER_DOUBLE)
        {
            Slider slider = new Slider();
            slider.setValue(property.getDoubleValue());
            slider.setMax(property.getMaxDoubleValue());
            slider.setMin(property.getMinDoubleValue());

            return slider;
        }
        else if(property.getControlType() == ControlType.SLIDER_INTEGER)
        {
            Slider slider = new Slider();
            slider.setValue(property.getIntValue());

            slider.setMax(property.getMaxIntValue());
            slider.setMin(property.getMinIntValue());
            slider.setBlockIncrement(1.0);
            slider.setSnapToTicks(true);

            return slider;
        }

        return null;
    }
}
