package br.com.zaiac.ebcofilemgmt.xml.classes.admindata;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Vehicle")
public class Vehicle {
    private String plateNumber;
    private String ocr;
    private Trailers trailers;

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

    public Trailers getTrailers() {
        return trailers;
    }

    @XmlElement(name="Trailers")
    public void setTrailers(Trailers trailers) {
        this.trailers = trailers;
    }
    
    public void createTrailers() {
        this.trailers = new Trailers();
    }
    
    
    
}


