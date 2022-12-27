/*
 * Stream-Pi - Free, Open-Source, Modular, Cross-Platform and Programmable Macro Pad
 * Copyright (C) 2019-2022 Debayan Sutradhar (rnayabed),  Samuel Quiñones (SamuelQuinones)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package com.stream_pi.server.uipropertybox;


import java.util.List;

import com.stream_pi.action_api.actionproperty.property.*;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.uihelper.HBoxInputBoxWithFileChooser;
import com.stream_pi.util.uihelper.HBoxWithSpaceBetween;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.controlsfx.control.ToggleSwitch;

public class UIPropertyBox
{
    private int index;
    private Property property;
    private Node controlNode;
    private Node UINode = null;
    private BooleanProperty changed;

    public UIPropertyBox(int index, Property property) throws MinorException
    {
        this(index, property, null);
    }

    public UIPropertyBox(int index, Property property, IntegerProperty unsavedChanges) throws MinorException
    {
        Node controlNode = null, UINode = null;

        changed = new SimpleBooleanProperty(false);

        if (unsavedChanges != null)
        {
            changed.addListener((ob, oldValue, newValue)->{
                if (oldValue == newValue) return;
                unsavedChanges.set(
                        newValue == property.getRawValue().equals("true")
                                ? (unsavedChanges.get() - 1)
                                : (unsavedChanges.get() + 1)
                );
            });
        }


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
                                setText(I18N.getString("window.helper.Helper.chooseValue"));
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

            comboBox.getSelectionModel().select(property.getSelectedListIndex());

            if (unsavedChanges != null)
            {
                comboBox.getSelectionModel().selectedIndexProperty()
                        .addListener((observableValue, oldValue, newValue) -> {
                            if (oldValue.equals(newValue)) return;
                            changed.set(!newValue.equals(Integer.parseInt(property.getRawValue())));
                        });
            }

            controlNode = comboBox;
        }
        else if(property.getControlType() == ControlType.TEXT_FIELD)
        {
            TextField textField = new TextField(property.getRawValue());

            if (unsavedChanges != null)
            {
                textField.textProperty().addListener((observableValue, oldValue, newValue) -> {
                    if (oldValue.equals(newValue)) return;
                    changed.set(!newValue.equals(property.getRawValue()));
                });
            }

            controlNode = textField;
        }
        else if(property.getControlType() == ControlType.TEXT_FIELD_MASKED)
        {
            PasswordField textField = new PasswordField();
            textField.setText(property.getRawValue());

            if (unsavedChanges != null)
            {
                textField.textProperty().addListener((observableValue, oldValue , newValue) -> {
                    if (oldValue.equals(newValue)) return;
                    changed.set(!newValue.equals(property.getRawValue()));
                });
            }

            controlNode = textField;
        }
        else if(property.getControlType() == ControlType.TOGGLE)
        {
            ToggleSwitch toggleSwitch = new ToggleSwitch();
            toggleSwitch.setSelected(property.getBoolValue());

            if (unsavedChanges != null)
            {
                toggleSwitch.selectedProperty().addListener((observableValue, oldValue , newValue) -> {
                    if (oldValue.equals(newValue)) return;
                    changed.set(!newValue.equals(property.getRawValue().equals("true")));
                });
            }

            controlNode = toggleSwitch;
        }
        else if(property.getControlType() == ControlType.SLIDER_DOUBLE)
        {
            Slider slider = new Slider();
            slider.setValue(property.getDoubleValue());
            slider.setMax(property.getMaxDoubleValue());
            slider.setMin(property.getMinDoubleValue());

            if (unsavedChanges != null)
            {
                slider.valueProperty().addListener((observableValue, oldValue , newValue) -> {
                    if (oldValue.equals(newValue)) return;
                    changed.set(!newValue.equals(Double.parseDouble(property.getRawValue())));
                });
            }

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

            if (unsavedChanges != null)
            {
                slider.valueProperty().addListener((observableValue, oldValue , newValue) -> {
                    if (oldValue.equals(newValue)) return;
                    changed.set(!newValue.equals(Integer.parseInt(property.getRawValue())));
                });
            }

            controlNode = slider;
        }
        else if(property.getControlType() == ControlType.FILE_PATH)
        {
            TextField textField = new TextField(property.getRawValue());

            if (unsavedChanges != null)
            {
                textField.textProperty().addListener((observableValue, oldValue , newValue) -> {
                    if (oldValue.equals(newValue)) return;
                    changed.set(!newValue.equals(property.getRawValue()));
                });
            }

            FileExtensionFilter[] fileExtensionFilters = property.getExtensionFilters();
            FileChooser.ExtensionFilter[] extensionFilters = new FileChooser.ExtensionFilter[fileExtensionFilters.length];

            for(int x = 0;x<fileExtensionFilters.length;x++)
            {
                extensionFilters[x] = new FileChooser.ExtensionFilter(
                        fileExtensionFilters[x].getDescription(),
                        fileExtensionFilters[x].getExtensions()
                );
            }

            UINode = new HBoxInputBoxWithFileChooser(property.getDisplayName(),
                    textField, null, extensionFilters);

            controlNode = textField;
        }

        if(property.getControlType() != ControlType.FILE_PATH)
        {
            UINode = new HBoxWithSpaceBetween(new Label(property.getDisplayName()), controlNode);
        }

        this.index = index;
        this.property = property;
        this.controlNode = controlNode;
        this.UINode = UINode;
    }

    public void reloadValue()
    {
        if(property.getControlType() == ControlType.COMBO_BOX)
        {
            ((ComboBox<ListValue>) controlNode).getSelectionModel().select(Integer.parseInt(property.getRawValue()));
        }
        else if(property.getControlType() == ControlType.TEXT_FIELD || property.getControlType() == ControlType.FILE_PATH)
        {
            ((TextField) controlNode).setText(property.getRawValue());
        }
        else if(property.getControlType() == ControlType.TEXT_FIELD_MASKED)
        {
            ((PasswordField) controlNode).setText(property.getRawValue());
        }
        else if(property.getControlType() == ControlType.TOGGLE)
        {
            ((ToggleSwitch) controlNode).setSelected(property.getRawValue().equals("true"));
        }
        else if(property.getControlType() == ControlType.SLIDER_DOUBLE)
        {
            ((Slider) controlNode).setValue(Double.parseDouble(property.getRawValue()));
        }
        else if(property.getControlType() == ControlType.SLIDER_INTEGER)
        {
            ((Slider) controlNode).setValue(Integer.parseInt(property.getRawValue()));
        }
    }

    public void saveValue()
    {
        getProperty().setRawValue(getControlNodeRawValue());
        changed.set(false);
    }
    
    public int getIndex() {
        return index;
    }

    public Property getProperty() {
        return property;
    }

    public Node getControlNode()
    {
        return controlNode;
    }

    public Node getUINode() {
        return UINode;
    }

    public void setChanged(boolean changed) {
        this.changed.set(changed);
    }

    public String validateProperty()
    {
        String error = null;

        if (property.getControlType() == ControlType.TEXT_FIELD || property.getControlType() == ControlType.TEXT_FIELD_MASKED)
        {
            if(property.getType() == Type.INTEGER)
            {
                try
                {
                    Integer.parseInt(property.getRawValue());
                }
                catch (NumberFormatException e)
                {
                    error = PropertyValidation.integerValueRequired(property.getDisplayName());
                }
            }
            else if(property.getType() == Type.DOUBLE)
            {
                try
                {
                    Double.parseDouble(property.getRawValue());
                }
                catch (NumberFormatException e)
                {
                    error = PropertyValidation.doubleValueRequired(property.getDisplayName());
                }
            }
            else
            {
                if(property.getRawValue().isBlank() && !property.isCanBeBlank())
                {
                    error = PropertyValidation.cannotBeBlank(property.getDisplayName());
                }
            }
        }

        return error;
    }
    
    public String getControlNodeRawValue()
    {
        String rawValue = null;
        ControlType controlType = property.getControlType();

        if (List.of(ControlType.TEXT_FIELD, ControlType.TEXT_FIELD_MASKED, ControlType.FILE_PATH)
            .contains(controlType))
            rawValue = ((TextField) controlNode).getText();
        else if (controlType == ControlType.COMBO_BOX)
            rawValue = ((ComboBox<String>) controlNode).getSelectionModel().getSelectedIndex() + "";
        else if (controlType == ControlType.SLIDER_DOUBLE)
            rawValue = ((Slider) controlNode).getValue() + "";
        else if (controlType == ControlType.SLIDER_INTEGER)
            rawValue = Math.round(((Slider) controlNode).getValue()) + "";
        else if (controlType == ControlType.TOGGLE) {
            ToggleSwitch toggleSwitch = ((ToggleSwitch) controlNode);
            if (toggleSwitch.isSelected())
                rawValue = "true";
            else
                rawValue = "false";
        }

        return rawValue;
    }
}
