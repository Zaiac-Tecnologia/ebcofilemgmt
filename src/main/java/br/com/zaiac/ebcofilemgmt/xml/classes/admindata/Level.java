package br.com.zaiac.ebcofilemgmt.xml.classes.admindata;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Level")
public class Level {
    private String id;
    private Value value;    

    public String getId() {
        return id;
    }

    @XmlAttribute(name="Id")
    public void setId(String id) {
        this.id = id;
    }

    public Value getValue() {
        return value;
    }

    @XmlElement(name="Value")
    public void setValue(Value value) {
        this.value = value;
    }
    
    public Level(String id, String value, String lang) {
        this.value = new Value(value, lang);
        this.id = id;
    }
    public Level() {
        this.value = new Value("1", "en");
        this.id = id;
    }
    
}


class Value {
    String value;
    String lang;

    public String getValue() {
        return value;
    }

    @XmlElement(name="Version")
    public void setValue(String value) {
        this.value = value;
    }

    public String getLang() {
        return lang;
    }
    @XmlAttribute(name="Lang")
    public void setLang(String lang) {
        this.lang = lang;
    }
    
    public Value(String value, String lang) {
        this.value = value;
        this.lang = lang;
    }
    
}