package com.stream_pi.server.window.dashboard.actiongridpane;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.action_api.action.ActionType;
import com.stream_pi.action_api.action.DisplayTextAlignment;
import com.stream_pi.action_api.action.Location;
import com.stream_pi.action_api.actionproperty.ClientProperties;
import com.stream_pi.action_api.actionproperty.gaugeproperties.GaugeProperties;
import com.stream_pi.action_api.actionproperty.gaugeproperties.SerializableColor;
import com.stream_pi.action_api.externalplugin.ExternalPlugin;
import com.stream_pi.server.controller.ActionDataFormats;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.window.dashboard.actiondetailspane.ActionDetailsPaneListener;
import com.stream_pi.util.exception.MinorException;
import eu.hansolo.medusa.Gauge;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

public class ActionBox extends StackPane
{
    private Label displayTextLabel;

    private int row;
    private int col;

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    private final ActionDetailsPaneListener actionDetailsPaneListener;

    public void clear()
    {
        setStyle(null);
        setAction(null);
        getStyleClass().clear();
        setBackground(Background.EMPTY);
        removeFontIcon();
        getChildren().clear();
        displayTextLabel = null;
        gauge = null;
        baseInit();
    }

    public void initMouseAndTouchListeners()
    {
        setOnDragOver(dragEvent ->
        {
            if(dragEvent.getDragboard().hasContent(ActionDataFormats.ACTION_TYPE))
            {
                dragEvent.acceptTransferModes(TransferMode.ANY);

                dragEvent.consume();
            }
        });

        setOnDragDropped(dragEvent ->
        {
            try
            {
                if(action == null)
                {
                    Dragboard db = dragEvent.getDragboard();

                    ActionType actionType = (ActionType) db.getContent(ActionDataFormats.ACTION_TYPE);

                    if(actionType == ActionType.NORMAL || actionType == ActionType.TOGGLE || actionType == ActionType.GAUGE)
                    {
                        String moduleName = (String) dragEvent.getDragboard().getContent(ActionDataFormats.MODULE_NAME);


                        ExternalPlugin newAction = actionGridPaneListener.createNewActionFromExternalPlugin(moduleName);

                        boolean isNew = (boolean) db.getContent(ActionDataFormats.IS_NEW);

                        if(isNew)
                        {
                            newAction.getClientProperties().resetToDefaults();

                            newAction.setDisplayText(newAction.getName());

                            if(newAction.getActionType() == ActionType.TOGGLE)
                            {
                                newAction.setShowDisplayText(false);
                                newAction.setDisplayTextAlignment(DisplayTextAlignment.BOTTOM);
                            }
                            else
                            {
                                newAction.setShowDisplayText(true);
                                newAction.setDisplayTextAlignment(DisplayTextAlignment.CENTER);
                            }


                            if(actionType == ActionType.TOGGLE)
                                newAction.setCurrentIconState("false__false");
                        }
                        else
                        {
                            newAction.setClientProperties((ClientProperties) db.getContent(ActionDataFormats.CLIENT_PROPERTIES));
                            newAction.setIcons((HashMap<String, byte[]>) db.getContent(ActionDataFormats.ICONS));
                            newAction.setCurrentIconState((String) db.getContent(ActionDataFormats.CURRENT_ICON_STATE));
                            newAction.setBgColourHex((String) db.getContent(ActionDataFormats.BACKGROUND_COLOUR));
                            newAction.setDisplayTextFontColourHex((String) db.getContent(ActionDataFormats.DISPLAY_TEXT_FONT_COLOUR));
                            newAction.setDisplayText((String) db.getContent(ActionDataFormats.DISPLAY_TEXT));
                            newAction.setDisplayTextAlignment((DisplayTextAlignment) db.getContent(ActionDataFormats.DISPLAY_TEXT_ALIGNMENT));
                            newAction.setShowDisplayText((boolean) db.getContent(ActionDataFormats.DISPLAY_TEXT_SHOW));
                        }



                        newAction.setLocation(new Location(getRow(),
                                getCol()));


                        newAction.setParent(actionGridPaneListener.getCurrentParent());
                        newAction.setSocketAddressForClient(actionGridPaneListener.getClientConnection().getRemoteSocketAddress());

                        try
                        {
                            newAction.onActionCreate();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            exceptionAndAlertHandler.handleMinorException(new MinorException(I18N.getString("window.dashboard.actiongridpane.ActionBox.onCreateFailed", newAction.getModuleName(), e.getMessage())));
                        }



                        newAction.setProfileID(actionGridPaneListener.getCurrentProfile().getID());
                        newAction.setSocketAddressForClient(actionGridPaneListener.getClientConnection().getRemoteSocketAddress());

                        actionGridPaneListener.addActionToCurrentClientProfile(newAction);

                        setAction(newAction);
                        init();

                        actionDetailsPaneListener.onActionClicked(newAction, this);

                        if(newAction.isHasIcon())
                            actionDetailsPaneListener.setSendIcon(true);


                        actionDetailsPaneListener.saveAction(true, false);

                    }
                    else
                    {
                        Action newAction = actionGridPaneListener.createNewOtherAction(actionType);

                        newAction.setLocation(new Location(getRow(),
                                getCol()));

                        newAction.setParent(actionGridPaneListener.getCurrentParent());

                        newAction.setProfileID(actionGridPaneListener.getCurrentProfile().getID());
                        newAction.setSocketAddressForClient(actionGridPaneListener.getClientConnection().getRemoteSocketAddress());

                        actionGridPaneListener.addActionToCurrentClientProfile(newAction);

                        setAction(newAction);
                        init();


                        actionDetailsPaneListener.onActionClicked(newAction, this);

                        if(newAction.isHasIcon())
                            actionDetailsPaneListener.setSendIcon(true);

                        actionDetailsPaneListener.saveAction(true, false);
                    }
                }
            }
            catch (MinorException e)
            {
                exceptionAndAlertHandler.handleMinorException(e);
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        setOnDragDetected(mouseEvent -> {
            if(getAction()!=null)
            {
                if(getAction().getActionType() == ActionType.NORMAL ||
                        getAction().getActionType() == ActionType.TOGGLE ||
                        getAction().getActionType() == ActionType.GAUGE)
                {
                    Dragboard db = startDragAndDrop(TransferMode.ANY);

                    ClipboardContent content = new ClipboardContent();

                    content.put(ActionDataFormats.CLIENT_PROPERTIES, getAction().getClientProperties());
                    content.put(ActionDataFormats.ICONS, getAction().getIcons());
                    content.put(ActionDataFormats.CURRENT_ICON_STATE, getAction().getCurrentIconState());
                    content.put(ActionDataFormats.BACKGROUND_COLOUR, getAction().getBgColourHex());
                    content.put(ActionDataFormats.DISPLAY_TEXT_FONT_COLOUR, getAction().getDisplayTextFontColourHex());
                    content.put(ActionDataFormats.DISPLAY_TEXT, getAction().getDisplayText());
                    content.put(ActionDataFormats.DISPLAY_TEXT_ALIGNMENT, getAction().getDisplayTextAlignment());
                    content.put(ActionDataFormats.DISPLAY_TEXT_SHOW, getAction().isShowDisplayText());

                    content.put(ActionDataFormats.IS_NEW, false);
                    content.put(ActionDataFormats.ACTION_TYPE, getAction().getActionType());
                    content.put(ActionDataFormats.MODULE_NAME, getAction().getModuleName());

                    db.setContent(content);

                    mouseEvent.consume();
                }
            }
        });

        setOnMouseClicked(mouseEvent -> {
            if(action != null)
            {
                if(mouseEvent.getClickCount() == 2 && getAction().getActionType() == ActionType.FOLDER)
                {
                    getActionDetailsPaneListener().onOpenFolderButtonClicked();
                }
                else
                {
                    try
                    {
                        actionDetailsPaneListener.onActionClicked(action, this);

                        if(mouseEvent.getButton().equals(MouseButton.SECONDARY))
                        {
                            actionContextMenu.show(this, mouseEvent.getScreenX(),
                                    mouseEvent.getScreenY());
                        }
                    }
                    catch (MinorException e)
                    {
                        exceptionAndAlertHandler.handleMinorException(e);
                        e.printStackTrace();
                    }
                }
            }

        });
    }

    public void baseInit()
    {

        setMinSize(size, size);
        setMaxSize(size, size);


        GridPane.setRowSpan(this, 1);
        GridPane.setColumnSpan(this, 1);

        getStyleClass().add("action_box");
        setIcon(null);
        getStyleClass().add("action_box_valid");




        actionContextMenu = new ContextMenu();

        MenuItem deleteActionMenuItem = new MenuItem(I18N.getString("window.dashboard.actiongridpane.ActionBox.deleteAction"));
        deleteActionMenuItem.getStyleClass().add("action_box_delete_menu_item");
        FontIcon deleteIcon = new FontIcon("fas-trash");
        deleteIcon.getStyleClass().add("action_box_delete_menu_item_icon");
        deleteActionMenuItem.setGraphic(deleteIcon);
        deleteActionMenuItem.setOnAction(event-> deleteAction());

        showToggleOffMenuItem = new MenuItem(I18N.getString("window.dashboard.actiongridpane.ActionBox.showToggleOff"));
        showToggleOffMenuItem.getStyleClass().add("action_box_toggle_off_menu_item");
        FontIcon toggleOffIcon = new FontIcon("fas-toggle-off");
        toggleOffIcon.getStyleClass().add("action_box_toggle_off_menu_item_icon");
        showToggleOffMenuItem.setGraphic(toggleOffIcon);
        showToggleOffMenuItem.setOnAction(event-> fakeToggle(false));

        showToggleOnMenuItem = new MenuItem(I18N.getString("window.dashboard.actiongridpane.ActionBox.showToggleOn"));
        showToggleOnMenuItem.getStyleClass().add("action_box_toggle_on_menu_item");
        FontIcon toggleOnIcon = new FontIcon("fas-toggle-on");
        toggleOnIcon.getStyleClass().add("action_box_toggle_on_menu_item_icon");
        showToggleOnMenuItem.setGraphic(toggleOnIcon);
        showToggleOnMenuItem.setOnAction(event-> fakeToggle(true));

        actionContextMenu.getItems().addAll(deleteActionMenuItem, showToggleOffMenuItem, showToggleOnMenuItem);
    }

    private MenuItem showToggleOffMenuItem;
    private MenuItem showToggleOnMenuItem;

    private void deleteAction()
    {
        actionDetailsPaneListener.onDeleteButtonClicked();
    }

    ContextMenu actionContextMenu;

    public void setInvalid(boolean invalid)
    {
        if(invalid)
        {
            getStyleClass().remove("action_box_valid");
            getStyleClass().add("action_box_invalid");
        }
        else
        {
            getStyleClass().remove("action_box_invalid");
            getStyleClass().add("action_box_valid");
        }

    }

    public ActionDetailsPaneListener getActionDetailsPaneListener() {
        return actionDetailsPaneListener;
    }


    public ActionGridPaneListener getActionGridPaneListener() {
        return actionGridPaneListener;
    }

    private int size;
    private ActionGridPaneListener actionGridPaneListener;
    public ActionBox(int size, ActionDetailsPaneListener actionDetailsPaneListener, ActionGridPaneListener actionGridPaneListener,
                     int col, int row)
    {
        this.actionGridPaneListener = actionGridPaneListener;
        this.actionDetailsPaneListener = actionDetailsPaneListener;
        this.size = size;

        this.col = col;
        this.row = row;


        this.managedProperty().bind(visibleProperty());


        setCache(true);
        setCacheHint(CacheHint.QUALITY);

        baseInit();

        initMouseAndTouchListeners();

    }
    
    public void setIcon(byte[] iconByteArray)
    {
        removeFontIcon();

        if(iconByteArray == null)
        { 
            getStyleClass().remove("action_box_icon_present");
            getStyleClass().add("action_box_icon_not_present");
            setBackground(null);
        }
        else
        {
            getStyleClass().add("action_box_icon_present");
            getStyleClass().remove("action_box_icon_not_present");

            setBackground(
                    new Background(
                            new BackgroundImage(new Image(
                                    new ByteArrayInputStream(iconByteArray), size, size, false, true
                            ), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,

                                    new BackgroundSize(100, 100, true, true, true, true))
                    )
            );
        }
    }

    public void setDefaultToggleIcon(boolean isToggleOn)
    {

        String styleClass;

        if(isToggleOn)
        {
            styleClass = "action_box_toggle_on";
        }
        else
        {
            styleClass = "action_box_toggle_off";
        }

        setBackground(null);


        if(fontIcon!=null)
        {
            fontIcon.getStyleClass().removeIf(s -> s.equals("action_box_toggle_off") || s.equals("action_box_toggle_on"));
        }
        else
        {
            fontIcon = new FontIcon();
            fontIcon.setIconSize((int) (size * 0.8));
            getChildren().add(fontIcon);
        }

        fontIcon.getStyleClass().add(styleClass);

        fontIcon.toBack();
    }

    public void removeFontIcon()
    {
        if(fontIcon!=null)
        {
            getChildren().remove(fontIcon);
            fontIcon = null;
        }
    }

    FontIcon fontIcon = null;

    private Action action = null;
    private ExceptionAndAlertHandler exceptionAndAlertHandler;


    public ActionBox(int size, Action action, ActionDetailsPaneListener actionDetailsPaneListener, ExceptionAndAlertHandler exceptionAndAlertHandler, ActionGridPaneListener actionGridPaneListener,
                     int col, int row)
    {
        this.actionGridPaneListener = actionGridPaneListener;
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        this.action = action;
        this.actionDetailsPaneListener = actionDetailsPaneListener;
        this.size = size;

        this.col = col;
        this.row = row;


        this.managedProperty().bind(visibleProperty());

        setCache(true);
        setCacheHint(CacheHint.QUALITY);

        baseInit();

        initMouseAndTouchListeners();

        init();



    }

    public void configureSize()
    {
        int rowSpan = getAction().getLocation().getRowSpan(), colSpan = getAction().getLocation().getColSpan();


        GridPane.setRowSpan(this, rowSpan);
        GridPane.setColumnSpan(this, colSpan);

        int actionWidth = (size*colSpan) + (actionGridPaneListener.getCurrentProfile().getActionGap()*(colSpan-1));
        int actionHeight = (size*rowSpan) + (actionGridPaneListener.getCurrentProfile().getActionGap()*(rowSpan-1));

        setMinSize(actionWidth, actionHeight);
        setMaxSize(actionWidth, actionHeight);

        for (int i = getCol(); i< getCol()+colSpan; i++)
        {
            for (int j = getRow(); j<getRow()+rowSpan; j++)
            {
                if (i == getAction().getLocation().getCol() && j == getAction().getLocation().getRow())
                {
                    continue;
                }

                actionGridPaneListener.getActionBox(i, j).setVisible(false);
            }
        }

    }


    public Action getAction() {
        return action;
    }

    public void setAction(Action action)
    {
        this.action = action;
    }

    public void init()
    {
        init(false);
    }

    private Gauge gauge = null;

    public void init(boolean start)
    {
        setBackground(null);
        setStyle(null);

        configureSize();

        showToggleOffMenuItem.setVisible(getAction().getActionType() == ActionType.TOGGLE);
        showToggleOnMenuItem.setVisible(getAction().getActionType() == ActionType.TOGGLE);


        setInvalid(action.isInvalid());

        Platform.runLater(()->{
            try
            {
                if(getAction().getActionType() == ActionType.GAUGE)
                {
                    if (gauge == null)
                    {
                        gauge = new Gauge();
                        gauge.setOnMouseClicked(getOnMouseClicked());
                        gauge.setAnimated(getAction().isGaugeAnimated());

                        getChildren().add(gauge);
                    }

                    updateGauge(getAction().getGaugeProperties());


                    setGaugeTitle(getAction().getDisplayText());
                }
                else
                {
                    displayTextLabel = new Label();
                    displayTextLabel.setWrapText(true);
                    displayTextLabel.setTextAlignment(TextAlignment.CENTER);
                    displayTextLabel.getStyleClass().add("action_box_display_text_label");
                    displayTextLabel.prefHeightProperty().bind(heightProperty());
                    displayTextLabel.prefWidthProperty().bind(widthProperty());


                    getChildren().addAll(displayTextLabel);


                    if(getAction().isShowDisplayText())
                    {
                        setDisplayTextAlignment(action.getDisplayTextAlignment());
                        setDisplayTextFontColourAndSize(action.getDisplayTextFontColourHex());
                        setDisplayTextLabel(getAction().getDisplayText());
                    }
                    else
                    {
                        setDisplayTextLabel("");
                    }


                }



                if(action.getActionType() == ActionType.TOGGLE)
                {
                    fakeToggle(start);
                }
                else
                {
                    if(action.isHasIcon())
                    {
                        if(!action.getCurrentIconState().isBlank())
                        {
                            setIcon(action.getCurrentIcon());
                        }
                    }
                    else
                    {
                        setIcon(null);
                    }
                }
                setBackgroundColour(action.getBgColourHex());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    public void setGaugeTitle(String text)
    {
        gauge.setTitle(text);
    }

    public void updateGauge(GaugeProperties gaugeProperties)
    {
        gauge.setSkinType(gaugeProperties.getSkinType());
        gauge.setMinValue(gaugeProperties.getMinValue());
        gauge.setMaxValue(gaugeProperties.getMaxValue());
        gauge.setSections(gaugeProperties.getSections());
        gauge.setUnit(gaugeProperties.getUnit());
        gauge.setSubTitle(gaugeProperties.getSubTitle());
        gauge.setDecimals(gaugeProperties.getDecimals());

        gauge.setSectionsVisible(gaugeProperties.isSectionsVisible());

        setGaugeForegroundBaseColor(gaugeProperties.getForegroundBaseColor());
        setGaugeBarColor(gaugeProperties.getBarColor());

        setGaugeTextColour(getAction().getDisplayTextFontColourHex());


        updateGaugeValue(gaugeProperties.getValue());
    }

    public void updateGaugeValue(double value)
    {
        gauge.setValue(value);
    }


    public void fakeToggle(boolean isON)
    {
        String[] toggleStatesHiddenStatus = action.getCurrentIconState().split("__");

        boolean isToggleOffHidden = toggleStatesHiddenStatus[0].equals("true");
        boolean isToggleOnHidden = toggleStatesHiddenStatus[1].equals("true");

        if(isON) // ON
        {
            if(action.isHasIcon())
            {
                boolean isToggleOnPresent = action.getIcons().containsKey("toggle_on");

                if(isToggleOnPresent)
                {
                    if(isToggleOnHidden)
                    {
                        setDefaultToggleIcon(true);
                    }
                    else
                    {
                        setIcon(action.getIcons().get("toggle_on"));
                    }
                }
                else
                {
                    setDefaultToggleIcon(true);
                }
            }
            else
            {
                setDefaultToggleIcon(true);
            }
        }
        else // OFF
        {
            if(action.isHasIcon())
            {
                boolean isToggleOffPresent = action.getIcons().containsKey("toggle_off");

                if(isToggleOffPresent)
                {
                    if(isToggleOffHidden)
                    {
                        setDefaultToggleIcon(false);
                    }
                    else
                    {
                        setIcon(action.getIcons().get("toggle_off"));
                    }
                }
                else
                {
                    setDefaultToggleIcon(false);
                }
            }
            else
            {
                setDefaultToggleIcon(false);
            }
        }
    }

    public void setDisplayTextLabel(String text)
    {
        displayTextLabel.setText(text);
    }

    public void setDisplayTextAlignment(DisplayTextAlignment displayTextAlignment)
    {
        if(displayTextAlignment == DisplayTextAlignment.CENTER)
            displayTextLabel.setAlignment(Pos.CENTER);
        else if (displayTextAlignment == DisplayTextAlignment.BOTTOM)
            displayTextLabel.setAlignment(Pos.BOTTOM_CENTER);
        else if (displayTextAlignment == DisplayTextAlignment.TOP)
            displayTextLabel.setAlignment(Pos.TOP_CENTER);
    }

    public void setDisplayTextFontColourAndSize(String colour)
    {
        String totalStyle = "";
        if(!colour.isEmpty())
        {
            totalStyle+="-fx-text-fill : "+colour+";";
        }

        if(getAction().getDisplayTextFontSize() > -1)
        {
            totalStyle+="-fx-font-size: "+getAction().getDisplayTextFontSize()+";";
        }

        if(!totalStyle.isBlank())
        {
            displayTextLabel.setStyle(totalStyle);
        }
    }

    public void setGaugeTextColour(String colorStr)
    {
        Color color = Color.valueOf("#242424");
        if (!colorStr.isEmpty())
        {
            color = Color.valueOf(colorStr);
        }

        gauge.setTitleColor(color);
        gauge.setSubTitleColor(color);
        gauge.setUnitColor(color);
        gauge.setValueColor(color);
    }
    public void setGaugeBarColor(SerializableColor newCol)
    {
        if (newCol != null)
        {
            gauge.setBarColor(newCol.getColor());
        }
    }

    public void setGaugeForegroundBaseColor(SerializableColor newCol)
    {
        if (newCol != null)
        {
            gauge.setForegroundBaseColor(newCol.getColor());
        }
    }



    public void setBackgroundColour(String colour)
    {
        if(!colour.isEmpty())
            setStyle("-fx-background-color : "+colour);
    }

    public void setSelected(boolean status)
    {
        if(status)
        {
            getStyleClass().add("action_box_selected");
        }
        else
        {
            getStyleClass().remove("action_box_selected");
        }
    }
}
