package br.com.zaiac.ebcofilemgmt.xml.classes.operations;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Operation")
public class Operation {
    private String type;
    
    private String start;
    private String end;
    private String login;
    private String comments;
    private String workstation;
    private String site;
    private Verdict verdict;

    public String getType() {
        return type;
    }

    @XmlAttribute(name="Type")
    public void setType(String type) {
        this.type = type;
    }

    public String getStart() {
        return start;
    }

    @XmlElement(name="Start")
    public void setStart(String start) {
        this.start = start;
    }

    @XmlElement(name="End")
    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getLogin() {
        return login;
    }

    @XmlElement(name="Login")
    public void setLogin(String login) {
        this.login = login;
    }

    public String getComments() {
        return comments;
    }

    @XmlElement(name="Comments")
    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getWorkstation() {
        return workstation;
    }

    @XmlElement(name="Workstation")
    public void setWorkstation(String workstation) {
        this.workstation = workstation;
    }

    public String getSite() {
        return site;
    }

    @XmlElement(name="Site")
    public void setSite(String site) {
        this.site = site;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    @XmlElement(name="Verdict")
    public void setVerdict(Verdict verdict) {
        this.verdict = verdict;
    }
    
    public void createVerdict(String id, String value) {
        Verdict veredict = new Verdict(id, value);
        this.verdict = veredict;
    }
}
