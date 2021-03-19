package com.stream_pi.server.window.dashboard.actiongridpane;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.action_api.action.ActionType;
import com.stream_pi.action_api.action.DisplayTextAlignment;
import com.stream_pi.action_api.action.Location;
import com.stream_pi.server.io.Config;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.window.dashboard.actiondetailpane.ActionDetailsPaneListener;
import com.stream_pi.util.exception.MinorException;
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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;

public class ActionBox extends StackPane{

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

    private ActionDetailsPaneListener actionDetailsPaneListener;

    public void clear()
    {
        setStyle(null);
        setAction(null);
        getStyleClass().clear();
        setBackground(Background.EMPTY);
        removeFontIcon();
        getChildren().clear();
        baseInit();
    }

    public void baseInit()
    {
        displayTextLabel = new Label();
        displayTextLabel.setWrapText(true);
        displayTextLabel.setTextAlignment(TextAlignment.CENTER);
        displayTextLabel.getStyleClass().add("action_box_display_text_label");
        displayTextLabel.prefHeightProperty().bind(heightProperty());
        displayTextLabel.prefWidthProperty().bind(widthProperty());


        getChildren().addAll(displayTextLabel);

        setMinSize(size, size);
        setMaxSize(size, size);

        getStyleClass().add("action_box");
        setIcon(null);
        getStyleClass().add("action_box_valid");


        setOnDragOver(dragEvent ->
        {
            if(dragEvent.getDragboard().hasContent(Action.getDataFormat()))
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
                    Action action = (Action) dragEvent.getDragboard().getContent(Action.getDataFormat());

                    action.setLocation(new Location(getRow(),
                            getCol()));

                    action.setParent(actionGridPaneListener.getCurrentParent());

                    action.setIDRandom();


                    actionGridPaneListener.addActionToCurrentClientProfile(action);

                    setAction(action);
                    init();


                    actionDetailsPaneListener.onActionClicked(action, this);
                
                    if(action.isHasIcon())
                        actionDetailsPaneListener.setSendIcon(true);

                    actionDetailsPaneListener.saveAction();
                }
            }
            catch (MinorException e)
            {
                exceptionAndAlertHandler.handleMinorException(e);
                e.printStackTrace();
            }
        });

        setOnDragDetected(mouseEvent -> {
            try {
                if(action!=null)
                {
                    if(action.getActionType() == ActionType.NORMAL)
                    {
                        Dragboard db = startDragAndDrop(TransferMode.ANY);

                        ClipboardContent content = new ClipboardContent();

                        Action newAction = (Action) action.clone();

                        newAction.setIDRandom();
                        newAction.setParent(actionGridPaneListener.getCurrentParent());

                        content.put(Action.getDataFormat(), newAction);

                        db.setContent(content);

                        mouseEvent.consume();
                    }
                }
            }
            catch (CloneNotSupportedException e)
            {
                e.printStackTrace();
            }
        });

        setOnMouseClicked(mouseEvent -> {
            if(action != null)
            {
                if(mouseEvent.getButton().equals(MouseButton.PRIMARY))
                {
                    if(mouseEvent.getClickCount() == 2 && action.getActionType() == ActionType.FOLDER)
                    {
                        getActionDetailsPaneListener().onOpenFolderButtonClicked();
                    }
                    else
                    {
                        try
                        {
                            actionDetailsPaneListener.onActionClicked(action, this);
                        }
                        catch (MinorException e)
                        {
                            exceptionAndAlertHandler.handleMinorException(e);
                            e.printStackTrace();
                        }
                    }
                }
                else if(mouseEvent.getButton().equals(MouseButton.SECONDARY))
                {
                    if(action.getActionType() == ActionType.TOGGLE)
                    {
                        toggleStateContextMenu.show(this, mouseEvent.getScreenX(),
                                mouseEvent.getScreenY());
                    }
                }
            }

        });

        toggleStateContextMenu = new ContextMenu();

        MenuItem showToggleOffMenuItem = new MenuItem("Show Toggle OFF");
        showToggleOffMenuItem.setOnAction(event-> fakeToggle(false));

        MenuItem showToggleOnMenuItem = new MenuItem("Show Toggle ON");
        showToggleOnMenuItem.setOnAction(event-> fakeToggle(true));

        toggleStateContextMenu.getItems().addAll(showToggleOffMenuItem, showToggleOnMenuItem);


        setCache(true);
        setCacheHint(CacheHint.QUALITY);
    }

    ContextMenu toggleStateContextMenu;

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
        baseInit();
    }

    public static Action deserialize(ByteBuffer buffer) {
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(buffer.array());
            ObjectInputStream ois = new ObjectInputStream(is);
            Action obj = (Action) ois.readObject();
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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

                                    new BackgroundSize(100, 100, true, true, true, false))
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
        removeFontIcon();

        fontIcon = new FontIcon();
        fontIcon.getStyleClass().add(styleClass);
        fontIcon.setIconSize((int) (size * 0.8));

        getChildren().add(fontIcon);
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


        baseInit();

        init();

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
        if(action.isShowDisplayText())
            setDisplayTextLabel(action.getDisplayText());
        else
            setDisplayTextLabel("");

        setDisplayTextAlignment(action.getDisplayTextAlignment());
        setBackgroundColour(action.getBgColourHex());
        setDisplayTextFontColour(action.getDisplayTextFontColourHex());

        setInvalid(action.isInvalid());

        Platform.runLater(()->{
            try {
                if(action.getActionType() == ActionType.TOGGLE)
                {
                    fakeToggle(false);
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
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    private void fakeToggle(boolean isON)
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

    public void setDisplayTextFontColour(String colour)
    {
        System.out.println("COLOr : "+colour);
        if(!colour.isEmpty())
            displayTextLabel.setStyle("-fx-text-fill : "+colour+";");
    }

    public void setBackgroundColour(String colour)
    {
        System.out.println("COLOr : "+colour);
        if(!colour.isEmpty())
            setStyle("-fx-background-color : "+colour);
    }
}
