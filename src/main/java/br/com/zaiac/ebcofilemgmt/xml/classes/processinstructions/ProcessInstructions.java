package br.com.zaiac.ebcofilemgmt.xml.classes.processinstructions;

import javax.xml.bind.annotation.XmlElement;

public class ProcessInstructions {
    private InspectionType inspectionType;
    private EnergyLevel energyLevel;
    private String pendingRequired;
    private String scanPosition;
    private String hEDRequired;
    private String speed;
    private String isContainerEmpty;

    public EnergyLevel getEnergyLevel() {
        return energyLevel;
    }

    public void setEnergyLevel(EnergyLevel energyLevel) {
        this.energyLevel = energyLevel;
    }
    
    
    public InspectionType getInspectionType() {
        return inspectionType;
    }

    @XmlElement(name="InspectionType")
    public void setInspectionType(InspectionType inspectionType) {
        this.inspectionType = inspectionType;
    }

    public String getPendingRequired() {
        return pendingRequired;
    }

    @XmlElement(name="PendingRequired")
    public void setPendingRequired(String pendingRequired) {
        this.pendingRequired = pendingRequired;
    }

    public String getScanPosition() {
        return scanPosition;
    }
    
    @XmlElement(name="ScanPosition")
    public void setScanPosition(String scanPosition) {
        this.scanPosition = scanPosition;
    }

    public String getHEDRequired() {
        return hEDRequired;
    }

    @XmlElement(name="HEDRequired")
    public void setHEDRequired(String hEDRequired) {
        this.hEDRequired = hEDRequired;
    }

    public String getSpeed() {
        return speed;
    }

    @XmlElement(name="Speed")
    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getIsContainerEmpty() {
        return isContainerEmpty;
    }

    @XmlElement(name="IsContainerEmpty")
    public void setIsContainerEmpty(String isContainerEmpty) {
        this.isContainerEmpty = isContainerEmpty;
    }
    
    
}
