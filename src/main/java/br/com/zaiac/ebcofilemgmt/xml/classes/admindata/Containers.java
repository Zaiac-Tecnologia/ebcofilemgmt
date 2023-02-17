package br.com.zaiac.ebcofilemgmt.xml.classes.admindata;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Containers")
public class Containers {
    private ArrayList<Container> container;
    private int i;

    public ArrayList<Container> getContainer() {
        return container;
    }
    
    public Containers() {
        this.i = -1;
        this.container = new ArrayList<Container>();        
    }
    

    @XmlElement(name="Container")
    public void setContainer(ArrayList<Container> container) {
        this.container = container;
    }
    
    
    
    public int createContainer() {
        Container container = new Container();
        this.i += 1;
        this.container.add(this.i, container);
        return this.i;
    }
    
    public Container getContainerIndex(int i) {
        return this.container.get(i);
    }
    
    
    
    
    
}
