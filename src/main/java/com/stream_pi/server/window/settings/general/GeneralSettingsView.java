package com.stream_pi.server.window.settings.general;

import com.stream_pi.server.combobox.IPChooserComboBox;
import com.stream_pi.server.combobox.LanguageChooserComboBox;
import com.stream_pi.server.connection.ClientConnections;
import com.stream_pi.server.controller.ServerListener;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.util.uihelper.*;
import com.stream_pi.util.validation.ValidationSupport;
import com.stream_pi.util.validation.Validator;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.controlsfx.control.ToggleSwitch;

import java.awt.*;
import java.util.Locale;
import java.util.logging.Logger;

public class GeneralSettingsView extends VBox
{
    private final ValidatedTextField serverNameTextField;
    private final DoubleField actionGridActionDisplayTextFontSizeTextField;
    private final IntegerField serverPortTextField;
    private final IPChooserComboBox ipChooserComboBox;
    private final ValidatedTextField pluginsPathTextField;
    private final ValidatedTextField themesPathTextField;
    private final DoubleField actionGridPaneActionBoxSizeDoubleField;
    private final CheckBox actionGridPaneActionBoxSizeIsDefaultCheckBox;
    private final DoubleField actionGridPaneActionBoxGapDoubleField;
    private final CheckBox actionGridPaneActionBoxGapIsDefaultCheckBox;
    private final CheckBox actionGridPaneActionDisplayTextFontSizeIsDefaultCheckBox;
    private final ToggleSwitch startOnBootToggleSwitch;
    private final HBoxWithSpaceBetween startOnBootHBox;
    private final TextField soundOnActionClickedFilePathTextField;
    private final ToggleSwitch soundOnActionClickedToggleSwitch;
    private final HBoxWithSpaceBetween soundOnActionClickedToggleSwitchHBox;
    private final ToggleSwitch minimiseToSystemTrayOnCloseToggleSwitch;
    private final HBoxWithSpaceBetween minimiseToSystemTrayOnCloseHBox;
    private final ToggleSwitch showAlertsPopupToggleSwitch;
    private final HBoxWithSpaceBetween showAlertsPopupHBox;
    private final Button saveButton;
    private final Button checkForUpdatesButton;
    private final Button factoryResetButton;
    private final Button restartButton;
    private final LanguageChooserComboBox languageChooserComboBox;

    private ValidationSupport validationSupport = new ValidationSupport();

    public GeneralSettingsView(GeneralSettingsViewListener generalSettingsViewListener)
    {
        serverNameTextField = new ValidatedTextField();
        serverNameTextField.setPrefWidth(200);
        validationSupport.registerValidator(serverNameTextField, Validator.createEmptyValidator(I18N.getString("serverNameCannotBeBlank")));

        serverPortTextField = new IntegerField(validationSupport, 1024, 65535);

        ipChooserComboBox = new IPChooserComboBox();

        languageChooserComboBox = new LanguageChooserComboBox();

        pluginsPathTextField = new ValidatedTextField();
        validationSupport.registerValidator(pluginsPathTextField, Validator.createEmptyValidator(I18N.getString("window.settings.GeneralSettings.pluginsPathMustNotBeBlank")));
        DirectoryChooserField pluginsPathDirectoryChooserField = new DirectoryChooserField(pluginsPathTextField);
        pluginsPathDirectoryChooserField.setAlignment(Pos.TOP_RIGHT);

        themesPathTextField = new ValidatedTextField();
        validationSupport.registerValidator(themesPathTextField, Validator.createEmptyValidator(I18N.getString("window.settings.GeneralSettings.themesPathMustNotBeBlank")));
        DirectoryChooserField themesPathDirectoryChooserField = new DirectoryChooserField(themesPathTextField);
        themesPathDirectoryChooserField.setAlignment(Pos.TOP_RIGHT);

        actionGridPaneActionBoxSizeDoubleField = new DoubleField(validationSupport, 1);
        HBox.setHgrow(actionGridPaneActionBoxSizeDoubleField, Priority.ALWAYS);
        actionGridPaneActionBoxSizeIsDefaultCheckBox = new CheckBox(I18N.getString("window.settings.GeneralSettings.followProfileDefaults"));

        actionGridPaneActionBoxGapDoubleField = new DoubleField(validationSupport, 0);
        HBox.setHgrow(actionGridPaneActionBoxGapDoubleField, Priority.ALWAYS);
        actionGridPaneActionBoxGapIsDefaultCheckBox = new CheckBox(I18N.getString("window.settings.GeneralSettings.followProfileDefaults"));

        actionGridActionDisplayTextFontSizeTextField = new DoubleField(validationSupport, 1);
        HBox.setHgrow(actionGridActionDisplayTextFontSizeTextField, Priority.ALWAYS);
        actionGridPaneActionDisplayTextFontSizeIsDefaultCheckBox = new CheckBox(I18N.getString("window.settings.GeneralSettings.followProfileDefaults"));

        startOnBootToggleSwitch = new ToggleSwitch();
        startOnBootHBox = new HBoxWithSpaceBetween(I18N.getString("window.settings.GeneralSettings.startOnBoot"), startOnBootToggleSwitch);
        startOnBootHBox.managedProperty().bind(startOnBootHBox.visibleProperty());

        soundOnActionClickedToggleSwitch = new ToggleSwitch();
        soundOnActionClickedToggleSwitchHBox = new HBoxWithSpaceBetween(I18N.getString("window.settings.GeneralSettings.soundOnActionClicked"), soundOnActionClickedToggleSwitch);


        soundOnActionClickedFilePathTextField = new TextField();

        FileChooserField soundOnActionClickedFilePathFileChooserField =  new FileChooserField(soundOnActionClickedFilePathTextField,
                new FileChooser.ExtensionFilter("Sounds","*.mp3","*.mp4", "*.m4a", "*.m4v","*.wav","*.aif", "*.aiff","*.fxm","*.flv","*.m3u8"));
        soundOnActionClickedFilePathFileChooserField.setAlignment(Pos.TOP_RIGHT);
        soundOnActionClickedFilePathFileChooserField.setUseLast(false);
        soundOnActionClickedFilePathFileChooserField.setRememberThis(false);
        soundOnActionClickedFilePathFileChooserField.getFileChooseButton().disableProperty().bind(soundOnActionClickedToggleSwitch.selectedProperty().not());

        minimiseToSystemTrayOnCloseToggleSwitch = new ToggleSwitch();
        minimiseToSystemTrayOnCloseHBox = new HBoxWithSpaceBetween(I18N.getString("window.settings.GeneralSettings.minimiseToTrayOnClose"), minimiseToSystemTrayOnCloseToggleSwitch);
        minimiseToSystemTrayOnCloseHBox.managedProperty().bind(minimiseToSystemTrayOnCloseHBox.visibleProperty());
        minimiseToSystemTrayOnCloseHBox.setVisible(SystemTray.isSupported());
        
        showAlertsPopupToggleSwitch = new ToggleSwitch();
        showAlertsPopupHBox = new HBoxWithSpaceBetween(I18N.getString("window.settings.GeneralSettings.showPopupOnAlert"), showAlertsPopupToggleSwitch);

        checkForUpdatesButton = new Button(I18N.getString("window.settings.GeneralSettings.checkForUpdates"));
        checkForUpdatesButton.setOnAction(event-> generalSettingsViewListener.onCheckForUpdatesButtonClicked(checkForUpdatesButton));

        factoryResetButton = new Button(I18N.getString("window.settings.GeneralSettings.factoryReset"));
        factoryResetButton.setOnAction(actionEvent -> generalSettingsViewListener.onFactoryResetButtonClicked());

        restartButton = new Button(I18N.getString("window.settings.GeneralSettings.restart"));
        restartButton.setOnAction(actionEvent -> generalSettingsViewListener.onRestartButtonClicked());

        saveButton = new Button(I18N.getString("save"));
        saveButton.disableProperty().bind(validationSupport.invalidProperty().or(generalSettingsViewListener.isSettingsBeingSaved()));
        saveButton.setOnAction(actionEvent -> generalSettingsViewListener.onSaveButtonClicked(saveButton,
                new GeneralSettingsRecord(
                        serverNameTextField.getText(), serverPortTextField.getIntegerValue(), actionGridActionDisplayTextFontSizeTextField.getDoubleValue(),
                        pluginsPathTextField.getText(), themesPathTextField.getText(),
                        actionGridPaneActionBoxSizeDoubleField.getDoubleValue(), actionGridPaneActionBoxSizeIsDefaultCheckBox.isSelected(), actionGridPaneActionBoxGapIsDefaultCheckBox.isSelected(),
                        actionGridPaneActionDisplayTextFontSizeIsDefaultCheckBox.isSelected(),
                        actionGridPaneActionBoxGapDoubleField.getDoubleValue(),
                        minimiseToSystemTrayOnCloseToggleSwitch.isSelected(), showAlertsPopupToggleSwitch.isSelected(), startOnBootToggleSwitch.isSelected(),
                        soundOnActionClickedToggleSwitch.isSelected(), soundOnActionClickedFilePathTextField.getText(),
                        ipChooserComboBox.getSelectedIP(), languageChooserComboBox.getSelectedLocale()
                )));


        FormBuilder form = new FormBuilder();

        form.getStyleClass().add("general_settings_grid_pane");

        form.addRow(generateSubHeading(I18N.getString("window.settings.GeneralSettings.connection")))
                .addRow(I18N.getString("serverName"), serverNameTextField)
                .addRow(I18N.getString("serverPort"), serverPortTextField)
                .addRow(I18N.getString("serverIPBinding"), ipChooserComboBox)

                .addRow(generateSubHeading(I18N.getString("window.settings.GeneralSettings.actionGrid")))
                .addRow(I18N.getString("actionBoxSize"), actionGridPaneActionBoxSizeDoubleField, actionGridPaneActionBoxSizeIsDefaultCheckBox)
                .addRow(I18N.getString("actionBoxGap"), actionGridPaneActionBoxGapDoubleField, actionGridPaneActionBoxGapIsDefaultCheckBox)
                .addRow(I18N.getString("window.settings.GeneralSettings.actionBoxDisplayTextFontSize"), actionGridActionDisplayTextFontSizeTextField, actionGridPaneActionDisplayTextFontSizeIsDefaultCheckBox)

                .addRow(generateSubHeading(I18N.getString("window.settings.GeneralSettings.locations")))
                .addRow(I18N.getString("window.settings.GeneralSettings.plugins"), pluginsPathDirectoryChooserField)
                .addRow(I18N.getString("window.settings.GeneralSettings.themes"), themesPathDirectoryChooserField)
                .addRow(I18N.getString("window.settings.GeneralSettings.soundOnActionClicked"), soundOnActionClickedFilePathFileChooserField)

                .addRow(generateSubHeading(I18N.getString("window.settings.GeneralSettings.others")))
                .addRow(I18N.getString("window.settings.GeneralSettings.language"), languageChooserComboBox)
                .addRow(I18N.getString("window.settings.GeneralSettings.soundOnActionClicked"), soundOnActionClickedToggleSwitch)
                .addRow(I18N.getString("window.settings.GeneralSettings.minimiseToTrayOnClose"), minimiseToSystemTrayOnCloseToggleSwitch)
                .addRow(I18N.getString("window.settings.GeneralSettings.startOnBoot"), startOnBootToggleSwitch)
                .addRow(I18N.getString("window.settings.GeneralSettings.showPopupOnAlert"), showAlertsPopupToggleSwitch)
                .addRow(I18N.getString("window.settings.GeneralSettings.factoryReset"), factoryResetButton)
                .addRow(I18N.getString("window.settings.GeneralSettings.restart"), restartButton);

        // TODO: checkForUpdatesButton removed until Update API is finalised



        ScrollPane scrollPane = new ScrollPane();
        scrollPane.maxWidthProperty().bind(widthProperty().multiply(0.8));
        scrollPane.getStyleClass().add("general_settings_scroll_pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(form);

        getStyleClass().add("general_settings");

        HBox saveButtonHBox = new HBox(saveButton);
        VBox.setMargin(saveButtonHBox, new Insets(0,10, 0, 0));
        saveButtonHBox.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(scrollPane, saveButtonHBox);



        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private Label generateSubHeading(String text)
    {
        Label label = new Label(text);
        label.getStyleClass().add("general_settings_sub_heading");
        return label;
    }

    public void updateFields(GeneralSettingsRecord generalSettingsRecord)
    {
        Platform.runLater(()->
        {
            serverNameTextField.setText(generalSettingsRecord.serverName());
            serverPortTextField.setText(generalSettingsRecord.port()+"");
            actionGridActionDisplayTextFontSizeTextField.setText(generalSettingsRecord.actionGridActionDisplayTextFontSize()+"");
            pluginsPathTextField.setText(generalSettingsRecord.pluginsPath());
            themesPathTextField.setText(generalSettingsRecord.themesPath());
            actionGridPaneActionBoxSizeDoubleField.setText(generalSettingsRecord.actionGridActionDisplayTextFontSize()+"");
            actionGridPaneActionBoxSizeIsDefaultCheckBox.setSelected(generalSettingsRecord.actionGridUseSameActionSizeAsProfile());
            actionGridPaneActionBoxGapIsDefaultCheckBox.setSelected(generalSettingsRecord.actionGridUseSameActionGapAsProfile());
            actionGridPaneActionDisplayTextFontSizeIsDefaultCheckBox.setSelected(generalSettingsRecord.actionGridUseSameActionDisplayTextFontSizeAsProfile());
            actionGridPaneActionBoxGapDoubleField.setText(generalSettingsRecord.actionGridActionGap()+"");

            minimiseToSystemTrayOnCloseToggleSwitch.setSelected(generalSettingsRecord.minimiseToSystemTrayOnClose());
            showAlertsPopupToggleSwitch.setSelected(generalSettingsRecord.showAlertsPopup());
            startOnBootToggleSwitch.setSelected(generalSettingsRecord.startOnBoot());

            soundOnActionClickedToggleSwitch.setSelected(generalSettingsRecord.soundOnActionClickedStatus());
            soundOnActionClickedFilePathTextField.setText(generalSettingsRecord.soundOnActionClickedFilePath());

            ipChooserComboBox.configureOptions(generalSettingsRecord.IP());

            languageChooserComboBox.getSelectionModel().select(I18N.getLanguage(generalSettingsRecord.currentLanguageLocale()));
        });
    }
}
