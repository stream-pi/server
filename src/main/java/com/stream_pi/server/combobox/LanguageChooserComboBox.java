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

package com.stream_pi.server.combobox;

import com.stream_pi.server.i18n.I18N;
import com.stream_pi.util.i18n.language.Language;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.util.Locale;

public class LanguageChooserComboBox extends ComboBox<Language>
{
    public LanguageChooserComboBox()
    {
        Callback<ListView<Language>, ListCell<Language>> callbackFactory = new Callback<>() {
            @Override
            public ListCell<Language> call(ListView<Language> locale) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Language language, boolean b)
                    {
                        super.updateItem(language, b);

                        if (language != null)
                        {
                            setText(language.getDisplayName());
                        }
                    }
                };
            }
        };

        setCellFactory(callbackFactory);
        setButtonCell(callbackFactory.call(null));

        setItems(FXCollections.observableArrayList(I18N.getLanguages()));
    }

    public Locale getSelectedLocale()
    {
        return getSelectionModel().getSelectedItem().getLocale();
    }
}
