package com.stream_pi.server.window.dashboard.actiongridpane;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.action_api.action.Location;
import com.stream_pi.action_api.otheractions.FolderAction;
import com.stream_pi.server.client.Client;
import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.io.Config;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.window.dashboard.actiondetailpane.ActionDetailsPaneListener;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.logging.Logger;

public class ActionGridPane extends ScrollPane implements ActionGridPaneListener {

    private ExceptionAndAlertHandler exceptionAndAlertHandler;
    private ActionDetailsPaneListener actionDetailsPaneListener;

    public ActionGridPane(ExceptionAndAlertHandler exceptionAndAlertHandler)
    {
        logger = Logger.getLogger(ActionGridPane.class.getName());
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        getStyleClass().add("action_grid_pane_parent");

        VBox.setVgrow(this, Priority.ALWAYS);

        actionsGridPane = new GridPane();
        actionsGridPane.setPadding(new Insets(5.0));
        actionsGridPane.getStyleClass().add("action_grid_pane");

        actionsGridPane.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);

        setContent(actionsGridPane);

        setCache(true);
        setCacheHint(CacheHint.SPEED);
    }

    public void setActionDetailsPaneListener(ActionDetailsPaneListener actionDetailsPaneListener) {
        this.actionDetailsPaneListener = actionDetailsPaneListener;
    }

    private String currentParent;

    @Override
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


    @Override
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
        fontIcon.getStyleClass().add("folder_action_back_button_icon");
        fontIcon.setIconSize(Config.getInstance().getActionGridActionSize() - 30);

        stackPane.setAlignment(Pos.CENTER);
        stackPane.getChildren().add(fontIcon);

        stackPane.setOnMouseClicked(e->returnToPreviousParent());

        return stackPane;
    }

    private ActionBox[][] actionBoxes;
    private boolean isFreshRender = true;
    private Node folderBackButton = null;
    public void renderGrid() throws SevereException
    {

        actionsGridPane.setHgap(Config.getInstance().getActionGridActionGap());
        actionsGridPane.setVgap(Config.getInstance().getActionGridActionGap());

        if(isFreshRender)
        {
            clear();
            actionBoxes = new ActionBox[cols][rows];
        }

        boolean isFolder = false;

        if(getCurrentParent().equals("root"))
        {
            if(folderBackButton != null)
            {
                actionsGridPane.getChildren().remove(folderBackButton);
                folderBackButton = null;

                actionBoxes[0][0] = addBlankActionBox(0,0);
            }
        }
        else
        {
            isFolder = true;

            if(folderBackButton != null)
            {
                actionsGridPane.getChildren().remove(folderBackButton);
                folderBackButton = null;
            }
            else
            {
                actionsGridPane.getChildren().remove(actionBoxes[0][0]);
            }

            folderBackButton = getFolderBackButton();
            actionsGridPane.add(folderBackButton, 0,0);
        }

        for(int row = 0; row<rows; row++)
        {
            for(int col = 0; col<cols; col++)
            {
                if(row == 0 && col == 0 && isFolder)
                    continue;

                if(isFreshRender)
                {
                    actionBoxes[col][row] = addBlankActionBox(col, row);
                }
                else
                {
                    if(actionBoxes[col][row].getAction() != null)
                    {
                        actionBoxes[col][row].clear();
                    }
                }
            }
        }

        isFreshRender = false;
    }

    public void setFreshRender(boolean freshRender)
    {
        isFreshRender = freshRender;
    }

    public ActionBox addBlankActionBox(int col, int row) throws SevereException {
        ActionBox actionBox = new ActionBox(Config.getInstance().getActionGridActionSize(), actionDetailsPaneListener, this,
                col, row);

        actionsGridPane.add(actionBox, row, col);
        return actionBox;
    }

    public void renderActions()
    {
        StringBuilder errors = new StringBuilder();
        for(String action1x : getClientProfile().getActionsKeySet())
        {
            Action eachAction = getClientProfile().getActionByID(action1x);
            logger.info("action ID : "+eachAction.getID()+
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
            logger.info("action has -1 rowIndex. Probably Combine action. Skipping ...");
            return;
        }

        if(action.getLocation().getRow() >= rows || action.getLocation().getCol() >= cols)
        {
            throw new MinorException("action "+action.getDisplayText()+" ("+action.getID()+") falls outside bounds.\n" +
                    "   Consider increasing rows/cols from client settings and relocating/deleting it.");
        }


        Location location = action.getLocation();

        ActionBox actionBox = actionBoxes[location.getCol()][location.getRow()];
        actionBox.clear();
        actionBox.setAction(action);

        actionBox.init();

        /*ActionBox actionBox = new ActionBox(Config.getInstance().getActionGridActionSize(), action, actionDetailsPaneListener, exceptionAndAlertHandler, this);

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
        actionsGridPane.add(actionBox, location.getRow(), location.getCol());*/

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
