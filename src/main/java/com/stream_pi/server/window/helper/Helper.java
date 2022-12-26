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

package com.stream_pi.server.window.helper;

import com.stream_pi.action_api.actionproperty.property.*;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.uipropertybox.UIPropertyBox;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.uihelper.HBoxInputBoxWithFileChooser;
import com.stream_pi.util.uihelper.HBoxWithSpaceBetween;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.controlsfx.control.ToggleSwitch;

public class Helper
{

    public static ControlNodePair createControlNodePair(Property property) throws MinorException
    {
        return createControlNodePair(property, null);
    }

    public static ControlNodePair createControlNodePair(Property property, IntegerProperty unsavedChanges) throws MinorException
    {
        ControlNodePair controlNodePair = new ControlNodePair();

        Node controlNode = null, uiNode = null;
        
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

            comboBox.getSelectionModel().select(property.getSelectedIndex());

            if (unsavedChanges != null)
            {
                comboBox.getSelectionModel().selectedIndexProperty()
                        .addListener((observableValue, v1, v2) -> {
                    if (!v1.equals(v2) && !controlNodePair.isChanged())
                    {
                        unsavedChanges.setValue(unsavedChanges.getValue() + 1);
                        controlNodePair.setChanged(true);
                    }
                });
            }

            controlNode = comboBox;
        }
        else if(property.getControlType() == ControlType.TEXT_FIELD)
        {
            TextField textField = new TextField(property.getRawValue());

            if (unsavedChanges != null)
            {
                textField.textProperty().addListener((observableValue, v1, v2) -> {
                    if (!v1.equals(v2) && !controlNodePair.isChanged())
                    {
                        unsavedChanges.setValue(unsavedChanges.getValue() + 1);
                        controlNodePair.setChanged(true);
                    }
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
                textField.textProperty().addListener((observableValue, v1, v2) -> {
                    if (!v1.equals(v2) && !controlNodePair.isChanged())
                    {
                        unsavedChanges.setValue(unsavedChanges.getValue() + 1);
                        controlNodePair.setChanged(true);
                    }
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
                toggleSwitch.selectedProperty().addListener((observableValue, v1, v2) -> {
                    if (!v1.equals(v2) && !controlNodePair.isChanged())
                    {
                        unsavedChanges.setValue(unsavedChanges.getValue() + 1);
                        controlNodePair.setChanged(true);
                    }
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
                slider.valueProperty().addListener((observableValue, v1, v2) -> {
                    if (!v1.equals(v2) && !controlNodePair.isChanged())
                    {
                        unsavedChanges.setValue(unsavedChanges.getValue() + 1);
                        controlNodePair.setChanged(true);
                    }
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
                slider.valueProperty().addListener((observableValue, v1, v2) -> {
                    if (!v1.equals(v2) && !controlNodePair.isChanged())
                    {
                        unsavedChanges.setValue(unsavedChanges.getValue() + 1);
                        controlNodePair.setChanged(true);
                    }
                });
            }

            controlNode = slider;
        }
        else if(property.getControlType() == ControlType.FILE_PATH)
        {
            TextField textField = new TextField(property.getRawValue());

            if (unsavedChanges != null)
            {
                textField.textProperty().addListener((observableValue, v1, v2) -> {
                    if (!v1.equals(v2) && !controlNodePair.isChanged())
                    {
                        unsavedChanges.setValue(unsavedChanges.getValue() + 1);
                        controlNodePair.setChanged(true);
                    }
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

            uiNode = new HBoxInputBoxWithFileChooser(property.getDisplayName(),
                    textField, null, extensionFilters);

            controlNode = textField;
        }

        if(property.getControlType() != ControlType.FILE_PATH)
        {
            uiNode = new HBoxWithSpaceBetween(new Label(property.getDisplayName()), controlNode);
        }

        controlNodePair.setUINode(uiNode);
        controlNodePair.setControlNode(controlNode);

        return controlNodePair;
    }

    public static String validateProperty(String value, UIPropertyBox property)
    {
        String error = null;

        if (property.getControlType() == ControlType.TEXT_FIELD || property.getControlType() == ControlType.TEXT_FIELD_MASKED)
        {
            if(property.getType() == Type.INTEGER)
            {
                try
                {
                    Integer.parseInt(value);
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
                    Double.parseDouble(value);
                }
                catch (NumberFormatException e)
                {
                    error = PropertyValidation.doubleValueRequired(property.getDisplayName());
                }
            }
            else
            {
                if(value.isBlank() && !property.isCanBeBlank())
                {

                    error = PropertyValidation.cannotBeBlank(property.getDisplayName());
                }
            }
        }

        return error;
    }
}
