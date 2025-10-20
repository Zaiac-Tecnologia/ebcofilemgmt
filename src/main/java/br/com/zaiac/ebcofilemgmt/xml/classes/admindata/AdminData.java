package br.com.zaiac.ebcofilemgmt.xml.classes.admindata;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "AdminData")
public class AdminData {
    private String fileId;
    private String comments;
    private Vehicle vehicle;

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
}
