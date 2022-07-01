package com.stream_pi.server.window.settings.general;

import com.stream_pi.server.combobox.IPChooserComboBox;
import com.stream_pi.server.combobox.LanguageChooserComboBox;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.config.record.*;
import com.stream_pi.util.uihelper.*;
import com.stream_pi.util.validation.ValidationResult;
import com.stream_pi.util.validation.ValidationSupport;
import com.stream_pi.util.validation.Validator;
import javafx.application.Platform;
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
import java.io.File;

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
    private final ValidatedTextField soundOnActionClickedFilePathTextField;
    private final ToggleSwitch soundOnActionClickedToggleSwitch;
    private final ToggleSwitch minimiseToSystemTrayOnCloseToggleSwitch;
    private final ToggleSwitch showAlertsPopupToggleSwitch;
    private final Button saveButton;
    private final Button checkForUpdatesButton;
    private final Button factoryResetButton;
    private final Button restartButton;
    private final LanguageChooserComboBox languageChooserComboBox;

    private ValidationSupport validationSupport = new ValidationSupport();

    public GeneralSettingsView(GeneralSettingsViewListener generalSettingsViewListener)
    {
        serverNameTextField = new ValidatedTextField();
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
        actionGridPaneActionBoxSizeIsDefaultCheckBox = new CheckBox(I18N.getString("window.settings.GeneralSettings.followProfileDefaults"));

        actionGridPaneActionBoxGapDoubleField = new DoubleField(validationSupport, 0);
        actionGridPaneActionBoxGapIsDefaultCheckBox = new CheckBox(I18N.getString("window.settings.GeneralSettings.followProfileDefaults"));

        actionGridActionDisplayTextFontSizeTextField = new DoubleField(validationSupport, 1);
        actionGridPaneActionDisplayTextFontSizeIsDefaultCheckBox = new CheckBox(I18N.getString("window.settings.GeneralSettings.followProfileDefaults"));

        startOnBootToggleSwitch = new ToggleSwitch();

        soundOnActionClickedToggleSwitch = new ToggleSwitch();

        soundOnActionClickedFilePathTextField = new ValidatedTextField();

        FileChooserField soundOnActionClickedFilePathFileChooserField =  new FileChooserField(soundOnActionClickedFilePathTextField,
                new FileChooser.ExtensionFilter("Sounds","*.mp3","*.mp4", "*.m4a", "*.m4v","*.wav","*.aif", "*.aiff","*.fxm","*.flv","*.m3u8"));
        soundOnActionClickedFilePathFileChooserField.setAlignment(Pos.TOP_RIGHT);
        soundOnActionClickedFilePathFileChooserField.setUseLast(false);
        soundOnActionClickedFilePathFileChooserField.setRememberThis(false);
        soundOnActionClickedFilePathFileChooserField.getFileChooseButton().disableProperty().bind(soundOnActionClickedToggleSwitch.selectedProperty().not());

        soundOnActionClickedFilePathFileChooserField.getFileChooseButton().disabledProperty().addListener((observableValue, oldValue, newValue) -> {
            if(newValue)
            {
                validationSupport.deRegisterValidator(soundOnActionClickedFilePathTextField);
            }
            else
            {
                validationSupport.registerValidator(soundOnActionClickedFilePathTextField, (validatedControl, o) -> {

                    ValidationResult validationResult = new ValidationResult();
                    String soundFileAbsolutePath = ((ValidatedTextField) validatedControl).getText();

                    if(soundFileAbsolutePath.trim().isEmpty())
                    {
                        validationResult.addErrorIf(I18N.getString("window.settings.GeneralSettings.soundFileCannotBeEmpty"), ((TextField) validatedControl.getControl()).getText().isBlank());
                    }
                    else
                    {
                        File soundFile = new File(soundFileAbsolutePath);
                        if (!soundFile.exists() || !soundFile.isFile()) {
                            validationResult.addErrorIf(I18N.getString("window.settings.GeneralSettings.soundFileNotFound"), ((TextField) validatedControl.getControl()).getText().isBlank());
                        }
                    }

                    return validationResult;
                });

                validationSupport.manuallyValidateControl(soundOnActionClickedFilePathTextField);
            }
        });



        minimiseToSystemTrayOnCloseToggleSwitch = new ToggleSwitch();
        
        showAlertsPopupToggleSwitch = new ToggleSwitch();

        checkForUpdatesButton = new Button(I18N.getString("window.settings.GeneralSettings.checkForUpdates"));
        checkForUpdatesButton.setOnAction(event-> generalSettingsViewListener.onCheckForUpdatesButtonClicked(checkForUpdatesButton));

        factoryResetButton = new Button(I18N.getString("window.settings.GeneralSettings.factoryReset"));
        factoryResetButton.setOnAction(actionEvent -> generalSettingsViewListener.onFactoryResetButtonClicked());

        restartButton = new Button(I18N.getString("window.settings.GeneralSettings.restart"));
        restartButton.setOnAction(actionEvent -> generalSettingsViewListener.onRestartButtonClicked());

        saveButton = new Button(I18N.getString("save"));
        saveButton.disableProperty().bind(validationSupport.invalidProperty().or(generalSettingsViewListener.isSettingsBeingSaved()));
        saveButton.setOnAction(actionEvent -> {
            generalSettingsViewListener.onSaveButtonClicked(saveButton, new GeneralSettings(
                    new ConnectionSettings(serverNameTextField.getText(), serverPortTextField.getIntegerValue(), ipChooserComboBox.getSelectedIP()),
                    new ActionGridSettings(
                            actionGridPaneActionBoxSizeDoubleField.getDoubleValue(), actionGridPaneActionBoxSizeIsDefaultCheckBox.isSelected(),
                            actionGridPaneActionBoxGapDoubleField.getDoubleValue(), actionGridPaneActionBoxGapIsDefaultCheckBox.isSelected(),
                            actionGridActionDisplayTextFontSizeTextField.getDoubleValue(), actionGridPaneActionDisplayTextFontSizeIsDefaultCheckBox.isSelected()
                    ),
                    new LocationsSettings(pluginsPathTextField.getText(), themesPathTextField.getText()),
                    new SoundOnActionClickedSettings(soundOnActionClickedFilePathTextField.getText(), soundOnActionClickedToggleSwitch.isSelected()),
                    new OthersSettings(languageChooserComboBox.getSelectedLocale(), minimiseToSystemTrayOnCloseToggleSwitch.isSelected(), startOnBootToggleSwitch.isSelected(), showAlertsPopupToggleSwitch.isSelected())
            ));
        });


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

                .addRow(generateSubHeading(I18N.getString("window.settings.GeneralSettings.soundOnActionClicked")))
                .addRow("Status", soundOnActionClickedToggleSwitch) //TODO: Implement new I18N for Sound On Action Clicked
                .addRow("Sound File", soundOnActionClickedFilePathFileChooserField)

                .addRow(generateSubHeading(I18N.getString("window.settings.GeneralSettings.others")))
                .addRow(I18N.getString("window.settings.GeneralSettings.language"), languageChooserComboBox);

        if(SystemTray.isSupported())
        {
            form.addRow(I18N.getString("window.settings.GeneralSettings.minimiseToTrayOnClose"), minimiseToSystemTrayOnCloseToggleSwitch);
        }

        form.addRow(I18N.getString("window.settings.GeneralSettings.startOnBoot"), startOnBootToggleSwitch)
                .addRow(I18N.getString("window.settings.GeneralSettings.showPopupOnAlert"), showAlertsPopupToggleSwitch)
                .addRow(I18N.getString("window.settings.GeneralSettings.factoryReset"), factoryResetButton)
                .addRow(I18N.getString("window.settings.GeneralSettings.restart"), restartButton);

        // TODO: checkForUpdatesButton removed until Update API is finalised



        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setMaxWidth(800);
        //scrollPane.maxWidthProperty().bind(widthProperty().multiply(0.8));
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

    public void updateFields(GeneralSettings generalSettings)
    {
        Platform.runLater(()->
        {
            ConnectionSettings connectionSettings = generalSettings.connection();
            serverNameTextField.setText(connectionSettings.serverName());
            serverPortTextField.setText(connectionSettings.port()+"");
            ipChooserComboBox.configureOptions(connectionSettings.IP());

            ActionGridSettings actionGridSettings = generalSettings.actionGrid();
            actionGridPaneActionBoxSizeDoubleField.setText(actionGridSettings.actionGridActionDisplayTextFontSize()+"");
            actionGridPaneActionBoxSizeIsDefaultCheckBox.setSelected(actionGridSettings.actionGridUseSameActionSizeAsProfile());
            actionGridPaneActionBoxGapDoubleField.setText(actionGridSettings.actionGridActionGap()+"");
            actionGridPaneActionBoxGapIsDefaultCheckBox.setSelected(actionGridSettings.actionGridUseSameActionGapAsProfile());
            actionGridActionDisplayTextFontSizeTextField.setText(actionGridSettings.actionGridActionDisplayTextFontSize()+"");
            actionGridPaneActionDisplayTextFontSizeIsDefaultCheckBox.setSelected(actionGridSettings.actionGridUseSameActionDisplayTextFontSizeAsProfile());

            LocationsSettings locationsSettings = generalSettings.locations();
            pluginsPathTextField.setText(locationsSettings.pluginsPath());
            themesPathTextField.setText(locationsSettings.themesPath());

            SoundOnActionClickedSettings soundOnActionClickedSettings = generalSettings.soundOnActionClicked();
            soundOnActionClickedFilePathTextField.setText(soundOnActionClickedSettings.soundOnActionClickedFilePath());
            soundOnActionClickedToggleSwitch.setSelected(soundOnActionClickedSettings.soundOnActionClickedStatus());

            OthersSettings othersSettings = generalSettings.others();
            languageChooserComboBox.getSelectionModel().select(I18N.getLanguage(othersSettings.currentLanguageLocale()));
            minimiseToSystemTrayOnCloseToggleSwitch.setSelected(othersSettings.minimiseToSystemTrayOnClose());
            startOnBootToggleSwitch.setSelected(othersSettings.startOnBoot());
            showAlertsPopupToggleSwitch.setSelected(othersSettings.showAlertsPopup());
        });
    }
}
