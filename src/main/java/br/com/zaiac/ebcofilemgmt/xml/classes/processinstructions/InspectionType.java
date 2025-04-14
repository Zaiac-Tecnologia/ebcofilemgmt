package br.com.zaiac.ebcofilemgmt.xml.classes.processinstructions;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;


@XmlRootElement(name="InspectionType")
public class InspectionType {
    private String value;
    private String status;
    
    public InspectionType() {
        
    }
    
    public InspectionType(String value, String status) {
        this.value = value;
        this.status =  status;
        
    }

    public String getStatus() {
        return status;
    }

    @XmlValue()
    public void setStatus(String status) {
        this.status = status;
    }

    public String getValue() {
        return value;
    }

    @XmlAttribute(name="Value")
    public void setValue(String value) {
        this.value = value;
    }
    
    
}
