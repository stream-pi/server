package com.stream_pi.server.window.dashboard.actiongridpane;

import com.stream_pi.actionapi.action.Action;
import com.stream_pi.actionapi.action.ActionType;
import com.stream_pi.actionapi.action.DisplayTextAlignment;
import com.stream_pi.actionapi.action.Location;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.window.dashboard.actiondetailpane.ActionDetailsPaneListener;
import com.stream_pi.util.exception.MinorException;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.Label;
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
        setAction(null);
        setInvalid(false);
        setBackground(Background.EMPTY);
        setStyle(null);
        getChildren().clear();
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
        getStyleClass().add("action_box_icon_not_present");
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

                    action.setParent(getStreamPiParent());

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
                        newAction.setParent(getStreamPiParent());

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
            if(action != null && mouseEvent.getButton().equals(MouseButton.PRIMARY))
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
        });

        setCache(true);
        setCacheHint(CacheHint.SPEED);
    }

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
    public ActionBox(int size, ActionDetailsPaneListener actionDetailsPaneListener, ActionGridPaneListener actionGridPaneListener)
    {
        this.actionGridPaneListener = actionGridPaneListener;
        this.actionDetailsPaneListener = actionDetailsPaneListener;
        this.size = size;
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
        if(iconByteArray == null)
        { 
            getStyleClass().remove("action_box_icon_present");
            getStyleClass().add("action_box_icon_not_present");
            setBackground(null);
            return;
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

    private Action action;
    private ExceptionAndAlertHandler exceptionAndAlertHandler;

    private String parent;

    public String getStreamPiParent() {
        return parent;
    }

    public void setStreamPiParent(String parent) {
        this.parent = parent;
    }

    public ActionBox(int size, Action action, ActionDetailsPaneListener actionDetailsPaneListener, ExceptionAndAlertHandler exceptionAndAlertHandler, ActionGridPaneListener actionGridPaneListener)
    {
        this.actionGridPaneListener = actionGridPaneListener;
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        this.action = action;
        this.actionDetailsPaneListener = actionDetailsPaneListener;
        this.size = size;



        baseInit();

        init();

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
            if(action.isHasIcon() && action.isShowIcon())
            {
                setIcon(action.getIconAsByteArray());
            }
            else
            {
                setIcon(null);
            }
        });
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
        if(!colour.isEmpty() && action.getIconAsByteArray() == null)
            setStyle("-fx-background-color : "+colour);
    }
}
