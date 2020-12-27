package com.akapps.sitesurvey.Classes;

/**
 * This class is a inverter object that contains that name of each inverter that associates to
 * a panel name and its data.
 */

import io.realm.RealmObject;

public class Inverter  extends RealmObject {

    private int numberOfPanel;
    private String data;

    public Inverter(){}

    public Inverter(int numberOfPanel, String data) {
        this.numberOfPanel = numberOfPanel;
        this.data = data;
    }

    public int getNumberOfPanel() {
        return numberOfPanel;
    }

    public void setNumberOfPanel(int nameOfPanel) {
        this.numberOfPanel = nameOfPanel;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
