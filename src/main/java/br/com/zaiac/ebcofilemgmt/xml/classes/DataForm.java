package br.com.zaiac.ebcofilemgmt.xml.classes;
import br.com.zaiac.ebcofilemgmt.xml.classes.admindata.AdminData;
import br.com.zaiac.ebcofilemgmt.xml.classes.admindata.Container;
import br.com.zaiac.ebcofilemgmt.xml.classes.admindata.Containers;
import br.com.zaiac.ebcofilemgmt.xml.classes.admindata.Trailer;
import br.com.zaiac.ebcofilemgmt.xml.classes.admindata.Trailers;
import br.com.zaiac.ebcofilemgmt.xml.classes.admindata.Vehicle;
import br.com.zaiac.ebcofilemgmt.xml.classes.operations.Operation;
import br.com.zaiac.ebcofilemgmt.xml.classes.operations.Operations;
import br.com.zaiac.ebcofilemgmt.xml.classes.processinstructions.ProcessInstructions;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="DataForm")
public class DataForm {
    private String version;
    private String truckId;
    
    private Integer state;
    private String site;
    private String date;
    private Integer inTraining;
    private Integer inReference;
    private Integer archived;
    private Integer inEdition;
    private Integer analysed;
    private Integer checkedOut;
    private Integer approved;
    private Integer pending;
    private AdminData adminData;
    private ProcessInstructions processInstructions;
    private Operations operations;

    public Operations getOperations() {
        return operations;
    }
    
    @XmlElement(name="Operations")
    public void setOperations(Operations operations) {
        this.operations = operations;
    }

    public ProcessInstructions getProcessInstructions() {
        return processInstructions;
    }

    @XmlElement(name="ProcessInstructions")
    public void setProcessInstructions(ProcessInstructions processInstructions) {
        this.processInstructions = processInstructions;
    }

    public AdminData getAdminData() {
        return adminData;
    }

    @XmlElement(name="AdminData")
    public void setAdminData(AdminData adminData) {
        this.adminData = adminData;
    }

    public String getVersion() {
        return version;
    }
    
    @XmlElement(name="Version")
    public void setVersion(String version) {
        this.version = version;
    }

    public String getTruckId() {
        return truckId;
    }

    @XmlElement(name="TruckId")
    public void setTruckId(String truckId) {
        this.truckId = truckId;
    }

    public Integer getState() {
        return state;
    }

    @XmlElement(name="State")
    public void setState(Integer state) {
        this.state = state;
    }

    public String getSite() {
        return site;
    }

    @XmlElement(name="Site")
    public void setSite(String site) {
        this.site = site;
    }

    public String getDate() {
        return date;
    }

    @XmlElement(name="Date")
    public void setDate(String date) {
        this.date = date;
    }

    public Integer getInTraining() {
        return inTraining;
    }

    @XmlElement(name="InTraining")
    public void setInTraining(Integer inTraining) {
        this.inTraining = inTraining;
    }

    
    public Integer getInReference() {
        return inReference;
    }

    @XmlElement(name="InReference")
    public void setInReference(Integer inReference) {
        this.inReference = inReference;
    }

    public Integer getArchived() {
        return archived;
    }

    @XmlElement(name="Archived")
    public void setArchived(Integer archived) {
        this.archived = archived;
    }

    public Integer getInEdition() {
        return inEdition;
    }

    @XmlElement(name="InEdition")
    public void setInEdition(Integer inEdition) {
        this.inEdition = inEdition;
    }

    public Integer getAnalysed() {
        return analysed;
    }

    @XmlElement(name="Analysed")
    public void setAnalysed(Integer analysed) {
        this.analysed = analysed;
    }

    public Integer getCheckedOut() {
        return checkedOut;
    }

    @XmlElement(name="CheckedOut")
    public void setCheckedOut(Integer checkedOut) {
        this.checkedOut = checkedOut;
    }

    public Integer getApproved() {
        return approved;
    }

    @XmlElement(name="Approved")
    public void setApproved(Integer approved) {
        this.approved = approved;
    }

    public Integer getPending() {
        return pending;
    }

    @XmlElement(name="Pending")
    public void setPending(Integer pending) {
        this.pending = pending;
    }  
    
    public void createAdminData() {
        this.adminData = new AdminData();
    }
    
    public void createVehicle() {
        this.adminData.createVehicle();
    }
    public Vehicle getVehicle() {
        return this.adminData.getVehicle();
    }
    
    
    public void createTrailers() {
        this.adminData.createTrailers();
    }
    
    public Trailers getTrailers() {
        return this.adminData.getVehicle().getTrailers();
    }
    
    public int createTrailer() {
        return this.adminData.getVehicle().getTrailers().createTrailer();
    }
    
    public Trailer getTrailerIndex(int i) {
        return this.adminData.getVehicle().getTrailers().getTrailerIndex(i);
    }
    

    public void createOperations() {
        this.operations = new Operations();
    }
    
    public int createOperation() {
        return this.operations.createOperation();
    }
    
    public Operation getOperationIndex(int i) {
        return this.operations.getOperationIndex(i);
    }
    
    public void createContainers(int trailerIndex) {
        this.adminData.getVehicle().getTrailers().getTrailerIndex(trailerIndex).createContainers();
    }
    
    public Containers getContainers(int trailerIndex) {
        return this.adminData.getVehicle().getTrailers().getTrailerIndex(trailerIndex).getContainers();
    }
    
    
    public int createContainer(int trailerIndex) {
        return this.adminData.getVehicle().getTrailers().getTrailerIndex(trailerIndex).getContainers().createContainer();
    }
    
    public Container getContainer(int trailerIndex, int containerIndex) {
        return this.adminData.getVehicle().getTrailers().getTrailerIndex(trailerIndex).getContainers().getContainerIndex(containerIndex);
    }
    
    
    

    
    
}
