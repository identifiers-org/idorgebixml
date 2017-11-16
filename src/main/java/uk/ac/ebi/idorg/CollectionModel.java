package uk.ac.ebi.idorg;

import java.util.ArrayList;

/**
 * Created by sarala on 21/07/2016.
 */
public class CollectionModel {
    String datatype_id;
    String name;
    String pattern;
    String definition;
    Boolean obsolete;
    String uri;
    String namespace;
    String type="collection";
    ArrayList<String> tags = new ArrayList<String>();
    ArrayList<ResourceModel> resources = new ArrayList<ResourceModel>();
    ArrayList<ReferenceModel> references = new ArrayList<ReferenceModel>();
    /*ArrayList<String> alias = new ArrayList<String>();*/

    public String getObsolete() {
        return String.valueOf(obsolete ? true : false);
    }
}
