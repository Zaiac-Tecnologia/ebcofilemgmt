package br.com.zaiac.ebcofilemgmt.xml.classes.admindata;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Trailers")
public class Trailers {
    
    private ArrayList<Trailer> trailer;
    private int i;

    public ArrayList<Trailer> getTrailer() {
        return trailer;
    }

    @XmlElement(name="Trailer")
    public void setTrailer(ArrayList<Trailer> trailer) {
        this.trailer = trailer;
    }
    public Trailers() {
        this.i = -1;
        this.trailer = new ArrayList<Trailer>();
        
    }
    
    public int createTrailer() {
        Trailer trailer = new Trailer();
        this.i += 1;
        this.trailer.add(this.i, trailer);
        return this.i;
    }
    
    public Trailer getTrailerIndex(int i) {
        return this.trailer.get(i);
    }
    
    
}
