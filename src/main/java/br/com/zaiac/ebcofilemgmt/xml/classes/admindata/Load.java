package br.com.zaiac.ebcofilemgmt.xml.classes.admindata;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Load")
public class Load {
    private List<MlList> mlList;
    private int i;
    
    public Load() {
        this.i = -1;
        this.mlList = new ArrayList<MlList>();
    }
    

    public List<MlList> getMlList() {
        return mlList;
    }

    @XmlElement(name="MlList")
    public void setMlList(List<MlList> mlList) {
        this.mlList = mlList;
    }
    
    public int createMlList() {
        MlList mlList = new MlList();
        this.i += 1;
        this.mlList.add(this.i, mlList);
        return this.i;
    }
    
    public MlList getMlListIndex(int i) {
        return this.mlList.get(i);
    }
    
    
}
