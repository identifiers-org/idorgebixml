package uk.ac.ebi.idorg;

import java.math.BigDecimal;

/**
 * Created by sarala on 22/07/2016.
 */
public class ResourceModel {
    String resource_id;
    String url_resource;
    String info;
    String institution;
    String location;
    Boolean official;
    String type="resource";
    BigDecimal uptime;
    String providerCode ="";

    public String getOfficial() {
        return String.valueOf(official ? true : false);
    }

    public String getUptime(){
        return uptime.setScale(2,BigDecimal.ROUND_CEILING).toString();
    }
}
