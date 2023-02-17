package br.com.zaiac.ebcofilemgmt.xml.classes.processinstructions;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name="EnergyLevel")
public class EnergyLevel {
    private String value;
    private String status;
    
    public EnergyLevel() {
        
    }
    
    public EnergyLevel(String value, String status) {
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
