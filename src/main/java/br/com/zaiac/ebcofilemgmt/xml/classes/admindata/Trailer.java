package br.com.zaiac.ebcofilemgmt.xml.classes.admindata;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Trailer")
public class Trailer {
    private String plateNumber;
    private String ocr;
    private Containers containers;

    public String getPlateNumber() {
        return plateNumber;
    }
    
    @XmlElement(name="PlateNumber")
    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getOcr() {
        return ocr;
    }

    @XmlElement(name="OCR")
    public void setOcr(String ocr) {
        this.ocr = ocr;
    }

    public Containers getContainers() {
        return containers;
    }

    @XmlElement(name="Containers")
    public void setContainers(Containers containers) {
        this.containers = containers;
    }
    
    public void createContainers() {
        this.containers = new Containers();
    }
    
            
    
}
