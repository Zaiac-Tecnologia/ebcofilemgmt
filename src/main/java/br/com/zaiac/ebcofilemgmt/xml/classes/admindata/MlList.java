package br.com.zaiac.ebcofilemgmt.xml.classes.admindata;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="MlList")
public class MlList {
    private ArrayList<Level> level;
    private int i;
    
    public MlList() {
        this.i = -1;
        this.level = new ArrayList<Level>();
    }
    

    public ArrayList<Level> getLevel() {
        return level;
    }

    @XmlElement(name="Level")
    public void setLevel(ArrayList<Level> level) {
        this.level = level;
    }
    
    public int createLevel() {
        Level level = new Level("1", "1", "en");
        this.i += 1;
        this.level.add(this.i, level);
        return this.i;
    }
    
    public Level getMlListIndex(int i) {
        return this.level.get(i);
    }
    

    
}
