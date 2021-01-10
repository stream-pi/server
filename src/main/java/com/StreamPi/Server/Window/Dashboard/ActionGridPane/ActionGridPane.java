package com.StreamPi.Server.Window.Dashboard.ActionGridPane;

import com.StreamPi.ActionAPI.Action.Action;
import com.StreamPi.ActionAPI.Action.Location;
import com.StreamPi.ActionAPI.ActionProperty.Property.Property;
import com.StreamPi.ActionAPI.OtherActions.FolderAction;
import com.StreamPi.Server.Client.Client;
import com.StreamPi.Server.Client.ClientProfile;
import com.StreamPi.Server.Connection.ServerListener;
import com.StreamPi.Server.IO.Config;
import com.StreamPi.Server.Window.ExceptionAndAlertHandler;
import com.StreamPi.Server.Window.Dashboard.ActionsDetailPane.ActionDetailsPaneListener;
import com.StreamPi.ThemeAPI.Themes;
import com.StreamPi.Util.Exception.MinorException;
import com.StreamPi.Util.Exception.SevereException;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.logging.Logger;

public class ActionGridPane extends ScrollPane implements ActionGridPaneListener {

    private ExceptionAndAlertHandler exceptionAndAlertHandler;
    private ActionDetailsPaneListener actionDetailsPaneListener;

    public ActionGridPane(ExceptionAndAlertHandler exceptionAndAlertHandler)
    {
        logger = Logger.getLogger(ActionGridPane.class.getName());
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        getStyleClass().add("action_grid_pane_scroll_pane");

        VBox.setVgrow(this, Priority.ALWAYS);

        actionsGridPane = new GridPane();
        actionsGridPane.setPadding(new Insets(5.0));
        actionsGridPane.getStyleClass().add("action_grid_pane");

        actionsGridPane.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);

        setContent(actionsGridPane);
    }

    public void setActionDetailsPaneListener(ActionDetailsPaneListener actionDetailsPaneListener) {
        this.actionDetailsPaneListener = actionDetailsPaneListener;
    }

    private String currentParent;

    public void setCurrentParent(String currentParent) {
        this.currentParent = currentParent;
    }

    public ClientProfile getClientProfile() {
        return clientProfile;
    }

    private Client client;

    public void setClient(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    private int rows, cols;

    private GridPane actionsGridPane;

    private ClientProfile clientProfile;

    public void setClientProfile(ClientProfile clientProfile)
    {
        this.clientProfile = clientProfile;

        setCurrentParent("root");
        setRows(clientProfile.getRows());
        setCols(clientProfile.getCols());
    }


    public String getCurrentParent() {
        return currentParent;
    }

    public StackPane getFolderBackButton() throws SevereException
    {
        StackPane stackPane = new StackPane();
        stackPane.getStyleClass().add("action_box");
        stackPane.getStyleClass().add("action_box_valid");

        stackPane.setPrefSize(
                Config.getInstance().getActionGridActionSize(),
                Config.getInstance().getActionGridActionSize()
        );

        FontIcon fontIcon = new FontIcon("fas-chevron-left");

        fontIcon.setIconSize(Config.getInstance().getActionGridActionSize() - 30);

        stackPane.setAlignment(Pos.CENTER);
        stackPane.getChildren().add(fontIcon);

        stackPane.setOnMouseClicked(e->returnToPreviousParent());

        return stackPane;
    }

    public void renderGrid() throws SevereException {
        clear();

        actionsGridPane.setHgap(Config.getInstance().getActionGridActionGap());
        actionsGridPane.setVgap(Config.getInstance().getActionGridActionGap());

        boolean isFolder = false;

        if(!getCurrentParent().equals("root"))
        {
            isFolder = true;

            actionsGridPane.add(getFolderBackButton(), 0,0);
        }

        for(int row = 0; row<rows; row++)
        {
            for(int col = 0; col<cols; col++)
            {
                if(row == 0 && col == 0 && isFolder)
                    continue;

                ActionBox actionBox = new ActionBox(Config.getInstance().getActionGridActionSize(), actionDetailsPaneListener, this);

                actionBox.setStreamPiParent(currentParent);
                actionBox.setRow(row);
                actionBox.setCol(col);

                actionsGridPane.add(actionBox, row, col);

            }
        }
    }

    public void renderActions()
    {
        StringBuilder errors = new StringBuilder();
        for(String action1x : getClientProfile().getActionsKeySet())
        {
            Action eachAction = getClientProfile().getActionByID(action1x);
            logger.info("Action ID : "+eachAction.getID()+
                    "\nInvalid : "+eachAction.isInvalid());

            try {
                renderAction(eachAction);
            }
            catch (SevereException e)
            {
                exceptionAndAlertHandler.handleSevereException(e);
            }
            catch (MinorException e)
            {
                errors.append("*").append(e.getShortMessage()).append("\n");
            }
        }

        if(!errors.toString().isEmpty())
        {
            exceptionAndAlertHandler.handleMinorException(new MinorException("Error while rendering following actions", errors.toString()));
        }
    }

    public void clear()
    {
        actionsGridPane.getChildren().clear();
    }

    private Logger logger;

    public void renderAction(Action action) throws SevereException, MinorException
    {
        if(!action.getParent().equals(currentParent))
        {
            logger.info("Skipping action "+action.getID()+", not current parent!");
            return;
        }

        if(action.getLocation().getRow()==-1)
        {
            logger.info("Action has -1 rowIndex. Probably Combine Action. Skipping ...");
            return;
        }

        if(action.getLocation().getRow() >= rows || action.getLocation().getCol() >= cols)
        {
            throw new MinorException("Action "+action.getDisplayText()+" ("+action.getID()+") falls outside bounds.\n" +
                    "   Consider increasing rows/cols from client settings and relocating/deleting it.");
        }


        ActionBox actionBox = new ActionBox(Config.getInstance().getActionGridActionSize(), action, actionDetailsPaneListener, exceptionAndAlertHandler, this);

        Location location = action.getLocation();

        actionBox.setStreamPiParent(currentParent);
        actionBox.setRow(location.getRow());
        actionBox.setCol(location.getCol());

        for(Node node : actionsGridPane.getChildren())
        {
            if(GridPane.getColumnIndex(node) == location.getRow() &&
            GridPane.getRowIndex(node) == location.getCol())
            {
                actionsGridPane.getChildren().remove(node);
                break;
            }
        }

        System.out.println(location.getCol()+","+location.getRow());
        actionsGridPane.add(actionBox, location.getRow(), location.getCol());

    }

    public void setRows(int rows)
    {
        this.rows = rows;
    }

    public void setCols(int cols)
    {
        this.cols = cols;
    }

    public int getRows()
    {
        return rows;
    }

    public int getCols()
    {
        return cols;
    }


    @Override
    public void addActionToCurrentClientProfile(Action newAction)  {
        try {
            getClientProfile().addAction(newAction);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    private String previousParent;

    public void setPreviousParent(String previousParent) {
        this.previousParent = previousParent;
    }

    public String getPreviousParent() {
        return previousParent;
    }

    @Override
    public void renderFolder(FolderAction action) {
        setCurrentParent(action.getID());
        setPreviousParent(action.getParent());
        try {
            renderGrid();
            renderActions();
        } catch (SevereException e) {
            e.printStackTrace();
        }
    }

    public void returnToPreviousParent()
    {
        setCurrentParent(getPreviousParent());

        if(!getPreviousParent().equals("root"))
        {
            System.out.println("parent : "+getPreviousParent());
            setPreviousParent(getClientProfile().getActionByID(
                    getPreviousParent()
            ).getParent());
        }

        try {
            renderGrid();
            renderActions();
        } catch (SevereException e) {
            e.printStackTrace();
        }
    }
}
