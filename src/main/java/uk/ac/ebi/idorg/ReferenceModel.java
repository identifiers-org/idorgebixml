package uk.ac.ebi.idorg;

/**
 * Created by sarala on 27/07/2016.
 */
public class ReferenceModel {
    String urn;
    String refType;

    public String getRefId(){
        return urn.substring(urn.lastIndexOf(":")+1);
    }

}
