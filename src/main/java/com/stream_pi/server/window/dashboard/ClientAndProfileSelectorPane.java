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

package com.stream_pi.server.window.dashboard;

import com.stream_pi.server.client.Client;
import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.connection.ClientConnection;
import com.stream_pi.server.connection.ClientConnections;
import com.stream_pi.server.i18n.I18N;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class ClientAndProfileSelectorPane extends HBox {

    private DashboardInterface dashboard;

    public ClientAndProfileSelectorPane(DashboardInterface dashboard)
    {
        this.dashboard = dashboard;

        VBox.setVgrow(this, Priority.NEVER);
        getStyleClass().add("client_and_profile_selector_pane");
        setPadding(new Insets(10));
        setMinHeight(90);
        setMaxHeight(90);

        initUI();
        loadData();

        setCache(true);
        setCacheHint(CacheHint.SPEED);
    }

    private ComboBox<ClientConnection> clientsComboBox;
    private Label noClientsConnectedLabel;
    private ComboBox<ClientProfile> clientProfilesComboBox;


    private ClientProfile currentSelectedClientProfile = null;

    public ClientProfile getCurrentSelectedClientProfile()
    {
        return currentSelectedClientProfile;
    }

    private ClientConnection currentSelectedClientConnection = null;

    public ClientConnection getCurrentSelectedClientConnection()
    {
        return currentSelectedClientConnection;
    }

    public void initUI()
    {
        noClientsConnectedLabel = new Label(I18N.getString("noClientsConnected"));
        noClientsConnectedLabel.getStyleClass().add("client_and_profile_selector_pane_no_clients_connected_label");
        noClientsConnectedLabel.managedProperty().bind(noClientsConnectedLabel.visibleProperty());

        clientsComboBox = new ComboBox<>();
        clientsComboBox.getStyleClass().add("client_and_profile_selector_pane_client_selector_combo_box");
        clientsComboBox.setPromptText(I18N.getString("window.dashboard.ClientAndProfileSelectorPane.chooseClient"));

        clientsComboBox.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            if(oldVal!=newVal && newVal!=null)
            {
                currentSelectedClientConnection = newVal;
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
                            setText(I18N.getString("window.dashboard.ClientAndProfileSelectorPane.chooseClient"));
                        }
                        else
                        {
                            Client client = clientConnection.getClient();
                            setText(client.getName());
                        }
                    }
                };
            }
        };
        clientsComboBox.setCellFactory(clientsComboBoxFactory);
        clientsComboBox.setButtonCell(clientsComboBoxFactory.call(null));



        clientProfilesComboBox = new ComboBox<>();
        clientProfilesComboBox.getStyleClass().add("client_and_profile_selector_pane_profile_selector_combo_box");
        clientProfilesComboBox.setPromptText(I18N.getString("window.dashboard.ClientAndProfileSelectorPane.chooseProfile"));
        clientProfilesComboBox.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            if(oldVal!=newVal && newVal!=null)
            {
                currentSelectedClientProfile = newVal;
                dashboard.newSelectedClientProfile(newVal);
            }
        });


        clientProfilesComboBox.managedProperty().bind(clientProfilesComboBox.visibleProperty());
        Callback<ListView<ClientProfile>, ListCell<ClientProfile>> clientProfilesComboBoxFactory = new Callback<>() {
            @Override
            public ListCell<ClientProfile> call(ListView<ClientProfile> clientProfileListView) {
                return new ListCell<>()
                {
                    @Override
                    protected void updateItem(ClientProfile profile, boolean b) {
                        super.updateItem(profile, b);

                        if(profile == null)
                        {
                            setText(I18N.getString("window.dashboard.ClientAndProfileSelectorPane.chooseProfile"));
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
        stack.getStyleClass().add("client_and_profile_selector_pane_stack");

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
