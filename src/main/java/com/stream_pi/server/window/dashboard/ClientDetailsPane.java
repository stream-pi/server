package com.stream_pi.server.window.dashboard;

import com.stream_pi.server.client.Client;
import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.connection.ClientConnection;
import com.stream_pi.server.connection.ClientConnections;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class ClientDetailsPane extends HBox {

    private DashboardInterface dashboard;

    public ClientDetailsPane(DashboardInterface dashboard)
    {
        this.dashboard = dashboard;

        VBox.setVgrow(this, Priority.NEVER);
        getStyleClass().add("client_details_pane");
        setPadding(new Insets(10));
        setMinHeight(90);

        initUI();
        loadData();

        setCache(true);
        setCacheHint(CacheHint.SPEED);
    }

    private ComboBox<ClientConnection> clientsComboBox;
    private Label noClientsConnectedLabel;
    private ComboBox<ClientProfile> clientProfilesComboBox;


    public void initUI()
    {
        noClientsConnectedLabel = new Label("No Clients Connected");
        noClientsConnectedLabel.managedProperty().bind(noClientsConnectedLabel.visibleProperty());

        clientsComboBox = new ComboBox<>();
        clientsComboBox.setPromptText("Choose client");

        clientsComboBox.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            if(oldVal!=newVal && newVal!=null)
            {
                dashboard.newSelectedClientConnection(newVal);
                clientProfilesComboBox.setItems(FXCollections.observableArrayList(newVal.getClient().getAllClientProfiles()));
                clientProfilesComboBox.setVisible(true);
            }
        });

        clientsComboBox.managedProperty().bind(clientsComboBox.visibleProperty());


        Callback<ListView<ClientConnection>, ListCell<ClientConnection>> clientsComboBoxFactory = new Callback<>() {
            @Override
            public ListCell<ClientConnection> call(ListView<ClientConnection> clientConnectionListView) {

                return new ListCell<>() {
                    @Override
                    protected void updateItem(ClientConnection clientConnection, boolean b) {
                        super.updateItem(clientConnection, b);

                        if(clientConnection == null)
                        {
                            setText("Choose client");
                        }
                        else
                        {
                            Client client = clientConnection.getClient();
                            setText(client.getNickName());
                        }
                    }
                };
            }
        };
        clientsComboBox.setCellFactory(clientsComboBoxFactory);
        clientsComboBox.setButtonCell(clientsComboBoxFactory.call(null));



        clientProfilesComboBox = new ComboBox<>();
        clientProfilesComboBox.setPromptText("Choose Profile");
        clientProfilesComboBox.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            if(oldVal!=newVal && newVal!=null)
            {
                dashboard.newSelectedClientProfile(newVal);
            }
        });


        clientProfilesComboBox.managedProperty().bind(clientProfilesComboBox.visibleProperty());
        Callback<ListView<ClientProfile>, ListCell<ClientProfile>> clientProfilesComboBoxFactory = new Callback<ListView<ClientProfile>, ListCell<ClientProfile>>() {
            @Override
            public ListCell<ClientProfile> call(ListView<ClientProfile> clientProfileListView) {
                return new ListCell<>()
                {
                    @Override
                    protected void updateItem(ClientProfile profile, boolean b) {
                        super.updateItem(profile, b);

                        if(profile == null)
                        {
                            setText("Choose Profile");
                        }
                        else
                        {
                            setText(profile.getName());
                        }
                    }
                };
            }
        };
        clientProfilesComboBox.setCellFactory(clientProfilesComboBoxFactory);
        clientProfilesComboBox.setButtonCell(clientProfilesComboBoxFactory.call(null));

        VBox stack = new VBox(noClientsConnectedLabel, clientsComboBox, clientProfilesComboBox);
        stack.setSpacing(10);

        getChildren().addAll(stack);

    }


    private void loadData()
    {
        clientsComboBox.getSelectionModel().clearSelection();
        clientProfilesComboBox.getSelectionModel().clearSelection();

        if(ClientConnections.getInstance().getConnections().size() == 0)
        {
            noClientsConnectedLabel.setVisible(true);

            clientsComboBox.setVisible(false);

            clientProfilesComboBox.setVisible(false);


            dashboard.newSelectedClientConnection(null);

        }
        else
        {
            noClientsConnectedLabel.setVisible(false);

            clientsComboBox.setVisible(true);
            clientProfilesComboBox.setVisible(false);

            clientsComboBox.setItems(FXCollections.observableArrayList(ClientConnections.getInstance().getConnections()));
        }
    }

    public void refresh()
    {
        loadData();
    }
}
