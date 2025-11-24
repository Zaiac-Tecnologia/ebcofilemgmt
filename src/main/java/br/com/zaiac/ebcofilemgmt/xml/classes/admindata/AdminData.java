package br.com.zaiac.ebcofilemgmt.xml.classes.admindata;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "AdminData")
public class AdminData {
    private String fileId;
    private String comments;
    private Vehicle vehicle;

    private String custom1;
    private String custom2;
    private String custom3;
    private String custom4;

    public String getFileId() {
        return fileId;
    }

    @XmlElement(name = "FileId")
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getComments() {
        return comments;
    }

    @XmlElement(name = "Comments")
    public void setComments(String comments) {
        this.comments = comments;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    @XmlElement(name = "Vehicle")
    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void createVehicle() {
        // System.out.println("Creating Vehicle in AdminData");
        this.vehicle = new Vehicle();
    }

    public void createTrailers() {
        this.vehicle.createTrailers();
    }

    public void createTrailer() {
        this.vehicle.getTrailers().createTrailer();
    }

    public String getCustom1() {
        return custom1;
    }

    @XmlElement(name = "Custom1", nillable = false)
    public void setCustom1(String custom1) {
        this.custom1 = custom1;
    }

    public String getCustom2() {
        return custom2;
    }

    @XmlElement(name = "Custom2", nillable = false)
    public void setCustom2(String custom2) {
        this.custom2 = custom2;
    }

    public String getCustom3() {
        return custom3;
    }

    @XmlElement(name = "Custom3", nillable = false)
    public void setCustom3(String custom3) {
        this.custom3 = custom3;
    }

    public String getCustom4() {
        return custom4;
    }

    @XmlElement(name = "Custom4", nillable = false)
    public void setCustom4(String custom4) {
        this.custom4 = custom4;
    }

}
