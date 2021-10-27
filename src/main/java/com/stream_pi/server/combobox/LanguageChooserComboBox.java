package com.stream_pi.server.combobox;

import com.stream_pi.server.i18n.I18N;
import com.stream_pi.util.i18n.language.Language;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.util.Locale;

public class LanguageChooserComboBox extends ComboBox<Locale>
{
    public LanguageChooserComboBox()
    {
        Callback<ListView<Locale>, ListCell<Locale>> callbackFactory = new Callback<>() {
            @Override
            public ListCell<Locale> call(ListView<Locale> locale) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Locale locale, boolean b)
                    {
                        super.updateItem(locale, b);

                        if (locale != null)
                        {
                            setText(locale.getDisplayName());
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
        return getSelectionModel().getSelectedItem();
    }
}
