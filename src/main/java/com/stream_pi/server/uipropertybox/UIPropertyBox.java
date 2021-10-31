/*
 * Stream-Pi - Free & Open-Source Modular Cross-Platform Programmable Macro Pad
 * Copyright (C) 2019-2021  Debayan Sutradhar (rnayabed),  Samuel Quiñones (SamuelQuinones)
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

import com.stream_pi.action_api.actionproperty.property.ControlType;
import com.stream_pi.action_api.actionproperty.property.Type;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import org.controlsfx.control.ToggleSwitch;

public class UIPropertyBox
{
    private Node controlNode;
    private boolean canBeBlank;
    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public boolean isCanBeBlank() {
        return canBeBlank;
    }

    private int index;

    private ControlType controlType;
    private Type type;

    public UIPropertyBox(int index, String displayName, Node controlNode, ControlType controlType, Type type, boolean canBeBlank)
    {
        this.index = index;
        this.displayName = displayName;
        this.controlNode = controlNode;
        this.controlType = controlType;
        this.type = type;
        this.canBeBlank = canBeBlank;
    }

    public ControlType getControlType()
    {
        return controlType;
    }

    public Node getControlNode()
    {
        return controlNode;
    }

    public int getIndex() {
        return index;
    }

    public Type getType()
    {
        return type;
    }

    public String getRawValue()
    {
        String rawValue = null;

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
