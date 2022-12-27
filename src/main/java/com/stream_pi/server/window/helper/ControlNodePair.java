package com.stream_pi.server.window.helper;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;

public class ControlNodePair
{
    private Node controlNode = null;
    private Node UINode = null;

    private boolean changed = false;

    public void setUINode(Node UINode) {
        this.UINode = UINode;
    }
    public Node getUINode()
    {
        return UINode;
    }

    public void setControlNode(Node controlNode) {
        this.controlNode = controlNode;
    }

    public Node getControlNode()
    {
        return controlNode;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }
}