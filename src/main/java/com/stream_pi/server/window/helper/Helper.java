package com.stream_pi.server.window.helper;

import com.stream_pi.action_api.actionproperty.property.ControlType;
import com.stream_pi.action_api.actionproperty.property.FileExtensionFilter;
import com.stream_pi.action_api.actionproperty.property.ListValue;
import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.uihelper.HBoxInputBoxWithFileChooser;
import com.stream_pi.util.uihelper.HBoxWithSpaceBetween;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.controlsfx.control.ToggleSwitch;

public class Helper
{
    public class ControlNodePair
    {
        private Node controlNode = null;
        private Node UINode = null;
        
        public ControlNodePair(Node controlNode, Node UINode)
        {
            this.controlNode = controlNode;
            this.UINode = UINode;
        }

        public Node getUINode()
        {
            return UINode;
        }

        public Node getControlNode()
        {
            return controlNode;
        }
    }
    
    public ControlNodePair getControlNode(Property property) throws MinorException
    {
        Node UINode = null, controlNode = null;
        
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
            
            controlNode = comboBox;
        }
        else if(property.getControlType() == ControlType.TEXT_FIELD)
        {
            controlNode = new TextField(property.getRawValue());
        }
        else if(property.getControlType() == ControlType.TEXT_FIELD_MASKED)
        {
            PasswordField textField = new PasswordField();
            textField.setText(property.getRawValue());

            controlNode = textField;
        }
        else if(property.getControlType() == ControlType.TOGGLE)
        {
            ToggleSwitch toggleSwitch = new ToggleSwitch();
            toggleSwitch.setSelected(property.getBoolValue());
            controlNode = toggleSwitch;
        }
        else if(property.getControlType() == ControlType.SLIDER_DOUBLE)
        {
            Slider slider = new Slider();
            slider.setValue(property.getDoubleValue());
            slider.setMax(property.getMaxDoubleValue());
            slider.setMin(property.getMinDoubleValue());

            controlNode = slider;
        }
        else if(property.getControlType() == ControlType.SLIDER_INTEGER)
        {
            Slider slider = new Slider();
            slider.setValue(property.getIntValue());

            slider.setMax(property.getMaxIntValue());
            slider.setMin(property.getMinIntValue());
            slider.setBlockIncrement(1.0);
            slider.setSnapToTicks(true);

            controlNode = slider;
        }
        else if(property.getControlType() == ControlType.FILE_PATH)
        {
            TextField textField = new TextField(property.getRawValue());

            FileExtensionFilter[] fileExtensionFilters = property.getExtensionFilters();
            FileChooser.ExtensionFilter[] extensionFilters = new FileChooser.ExtensionFilter[fileExtensionFilters.length];

            for(int x = 0;x<fileExtensionFilters.length;x++)
            {
                extensionFilters[x] = new FileChooser.ExtensionFilter(
                        fileExtensionFilters[x].getDescription(),
                        fileExtensionFilters[x].getExtensions()
                );
            }

            UINode = new HBoxInputBoxWithFileChooser(property.getDisplayName(), textField, null,
                    extensionFilters);

            controlNode =textField;
        }

        if(property.getControlType() != ControlType.FILE_PATH)
        {
            UINode = new HBoxWithSpaceBetween(new Label(property.getDisplayName()), controlNode);
        }

        return new ControlNodePair(controlNode, UINode);
    }
}
