package com.akapps.sitesurvey.Classes;

/**
 * This class create a project object that contains the project name, address, owner name,
 * and the date of object creation. In addition, it contains RealmLists (which is an ArrayList)
 * that contains project photo locations as well as installation photos, project notes,
 * inverter data, and panel placement.
 */

import io.realm.RealmList;
import io.realm.RealmObject;

public class Project extends RealmObject{

    private String project_Id;
    private String project_Name,
            street_Name,
            city,
            owner_Name,
            date_Created;
    private int street_Number, zip_Code;

    private RealmList<String> project_Photo_Locations,
            installation_Photo_Locations,
            project_Notes;

    private RealmList<Inverter> inverter_Data;      // contains bar-codes of inverters and associated panels
    private RealmList<Boolean> panel_Data;
    private RealmList<Integer> checklist;

    private String panel_Placement_Screenshot_Path;
    private boolean is_Project_Completed;
    private boolean north_Facing_Arrow_Set;
    private int north_Facing_Arrow_Rotation;        // rotation of arrow to match user selection

    public Project(){}

    public Project(String project_Id, String date_Created, String project_Name, String owner_Name){
        this.project_Id = project_Id;
        this.project_Name = project_Name;
        this.owner_Name = owner_Name;
        this.date_Created = date_Created;
        this.street_Number = 0;
        this.street_Name = null;
        this.city = null;
        this.zip_Code = 0;
    }

    public Project(String project_Id, String date_Created, String project_Name, String owner_Name,
                   int street_Number, String street_Name, String city, int zipcode){
        this.project_Id = project_Id;
        this.project_Name = project_Name;
        this.owner_Name = owner_Name;
        this.date_Created = date_Created;
        this.street_Number = street_Number;
        this.street_Name = street_Name;
        this.city = city;
        this.zip_Code = zipcode;
    }

    public String getProject_Id() {
        return project_Id;
    }

    public String getProject_Name() {
        return project_Name;
    }

    public void setProject_Name(String project_Name) {
        this.project_Name = project_Name;
    }

    public String getProject_Address() {
        if(Helper.isDialogAddressEmpty(String.valueOf(street_Number), street_Name, city, String.valueOf(zip_Code)))
            return "N/A";
        return street_Number + " " + street_Name + "\n" + city + ", CA " + zip_Code;
    }

    public boolean isAddressEmpty(){
        try{
            if (street_Number>0 && street_Name.length()>0 && city.length()>0 && zip_Code>0)
                return false;
        }catch (Exception e){
            return false;
        }
        return true;
    }

    public int getNorthFacingArrowRotation() {
        return north_Facing_Arrow_Rotation;
    }

    public void setNorthFacingArrowRotation(int northFacingArrowRotation) {
        this.north_Facing_Arrow_Rotation = northFacingArrowRotation;
    }

    public String getPanel_Placement_Screenshot_Path() {
        if(panel_Placement_Screenshot_Path == null)
            return "";
        return panel_Placement_Screenshot_Path;
    }

    public boolean getNorthFacingArrow() {
        return north_Facing_Arrow_Set;
    }

    public void setNorthFacingArrow(boolean northFacingArrow) {
        this.north_Facing_Arrow_Set = northFacingArrow;
    }

    public void setPanel_Placement_Screenshot_Path(String panel_Placement_Screenshot_Path) {
        this.panel_Placement_Screenshot_Path = panel_Placement_Screenshot_Path;
    }

    public RealmList<Boolean> getPanel_Data() {
        return panel_Data;
    }

    public void setPanel_Data(RealmList<Boolean> panel_Data) {
        this.panel_Data.clear();
        this.panel_Data.addAll(panel_Data);
    }

    public boolean isCompleted() {
        return is_Project_Completed;
    }

    public void setCompleted(boolean completed) {
        is_Project_Completed = completed;
    }

    public String getOwner_Name() {
        return owner_Name;
    }

    public void setOwner_Name(String owner_Name) {
        this.owner_Name = owner_Name;
    }

    public String getDate_Created() {
        return date_Created;
    }

    public RealmList<String> getProject_Photo_Locations() {
        return project_Photo_Locations;
    }

    public RealmList<String> getInstallation_Photo_Locations() {
        return installation_Photo_Locations;
    }

    public RealmList<String> getProject_Notes() {
        return project_Notes;
    }

    public int getStreet_Number() {
        return street_Number;
    }

    public void setStreet_Number(int street_Number) {
        this.street_Number = street_Number;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getZipcode() {
        return zip_Code;
    }

    public void setZipcode(int zipcode) {
        this.zip_Code = zipcode;
    }

    public String getStreet_Name() {
        return street_Name;
    }

    public void setStreet_Name(String street_Name) {
        this.street_Name = street_Name;
    }

    public RealmList<Inverter> getInverter_Data() {
        return inverter_Data;
    }

    public RealmList<Integer> getChecklist() {
        return checklist;
    }

    @Override
    public String toString() {
        return "Project Name : \t" + project_Name + "\n" +
                "Owner : \t" + owner_Name + "\n" +
                "Address: \t" + street_Number + " " + street_Name +
                "\n" + city + ", CA " + zip_Code + "\n" +
                "Date Surveyed: \t" + date_Created;
    }

}
