package br.com.zaiac.ebcofilemgmt.xml.classes.operations;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Operations")
public class Operations {
    
    private List<Operation> operation;
    private int i;
    
    public Operations() {
        this.i = -1;
        this.operation = new ArrayList<Operation>();
    }

    public List<Operation> getOperation() {
        return operation;
    }

    @XmlElement(name="Operation")
    public void setOperation(List<Operation> operation) {
        this.operation = operation;
    }
    
    public int createOperation() {
        Operation operation = new Operation();
        this.i += 1;
        this.operation.add(this.i, operation);
        return this.i;
    }
    
    public Operation getOperationIndex(int i) {
        return this.operation.get(i);
    }
    
    
    
}
