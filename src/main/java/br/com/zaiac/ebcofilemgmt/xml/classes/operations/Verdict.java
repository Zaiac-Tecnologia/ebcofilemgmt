package br.com.zaiac.ebcofilemgmt.xml.classes.operations;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class Verdict {
    private String value;
    private String id;
    
    public String getId() {
        return id;
    }

    @XmlAttribute(name="Id")
    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    @XmlValue
    public void setValue(String value) {
        this.value = value;
    }
    
    public Verdict(String id, String value) {
        this.value = value;
        this.id = id;
    }
    public Verdict() {
        this.value = "";
        this.id = id;
    }
    
    
}
