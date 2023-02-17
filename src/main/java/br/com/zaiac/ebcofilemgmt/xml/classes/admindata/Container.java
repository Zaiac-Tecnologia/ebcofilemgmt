package br.com.zaiac.ebcofilemgmt.xml.classes.admindata;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Container")
public class Container {
    private String containerId;
    private String ocr;
    private Load load;

    public String getContainerId() {
        return containerId;
    }
    
    @XmlElement(name="ContainerId")
    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getOcr() {
        return ocr;
    }
    
    @XmlElement(name="OCR")
    public void setOcr(String ocr) {
        this.ocr = ocr;
    }
    
    public Load getLoad() {
        return load;
    }
    
    @XmlElement(name="Load")
    public void setLoad(Load load) {
        this.load = load;
    }
    
    public void createLoad() {
        this.load = new Load();
    }
    
    
    
}
