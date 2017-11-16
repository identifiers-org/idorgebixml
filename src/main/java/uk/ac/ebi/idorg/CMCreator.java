package uk.ac.ebi.idorg;

import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * Created by sarala on 21/07/2016.
 */
public class CMCreator {

    private JdbcTemplate jdbcTemplate;

    public CMCreator(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void populateCollectionModelMap(HashMap<String, CollectionModel> collectionModels) {
        List<Map<String, Object>> rows = this.jdbcTemplate.queryForList(getCollectionListQuery());
        for (Map row : rows) {
            String datatype_id = (String)row.get("datatype_id");

            CollectionModel collectionModel = collectionModels.get(datatype_id);

            if(collectionModel==null){
                collectionModel = new CollectionModel();
                collectionModel.datatype_id = datatype_id;
                collectionModel.name = (String)row.get("name");
                collectionModel.definition = (String)row.get("definition");
                collectionModel.pattern = (String)row.get("pattern") ;
                collectionModel.obsolete = (Boolean)row.get("obsolete");
                collectionModel.uri = ((String)row.get("uri")).replace("urn:miriam:","http://identifiers.org/");
                collectionModel.namespace = ((String)row.get("uri")).substring(11);
                collectionModels.put(datatype_id,collectionModel);
            }
            //collectionModel.tags.add((String)row.get("tag"));
        }
    }

    public void populateTags(HashMap<String, CollectionModel> collectionModels){
        List<Map<String, Object>> rows = this.jdbcTemplate.queryForList(getTagsQuery());
        for (Map row : rows) {
            String datatype_id = (String)row.get("ptr_datatype");

            CollectionModel collectionModel = collectionModels.get(datatype_id);

            if(collectionModel != null){
                collectionModel.tags.add((String)row.get("tag"));
            }
        }
    }


    public void populateResouceModelMap(HashMap<String, CollectionModel> collectionModels) {
        List<Map<String, Object>> rows = this.jdbcTemplate.queryForList(getResourcesListQuery());
        for (Map row : rows) {
            String datatype_id = (String)row.get("ptr_datatype");

            ResourceModel resourceModel = new ResourceModel();
            resourceModel.resource_id = (String)row.get("resource_id");
            resourceModel.info = (String)row.get("info");
            resourceModel.institution = (String)row.get("institution");
            resourceModel.location = (String)row.get("location");
            resourceModel.url_resource = (String)row.get("url_resource");
            resourceModel.official = (Boolean)row.get("official");
            resourceModel.uptime = (BigDecimal)row.get("uptime");
            resourceModel.providerCode = (String)row.get("prefix");

            CollectionModel collectionModel = collectionModels.get(datatype_id);

            if(collectionModel != null){
                collectionModel.resources.add(resourceModel);
            }
        }
    }

    public void populateReferences(HashMap<String, CollectionModel> collectionModels) {
        List<Map<String, Object>> rows = this.jdbcTemplate.queryForList(getReferencesQuery());
        for (Map row : rows) {
            String datatype_id = (String)row.get("ptr_datatype");

            ReferenceModel referenceModel = new ReferenceModel();
            referenceModel.urn = (String)row.get("uri");
            referenceModel.refType = (String)row.get("uri_type");

            CollectionModel collectionModel = collectionModels.get(datatype_id);

            if(collectionModel != null && !referenceModel.refType.equals("URL")){
                if(referenceModel.refType.equals("PMID"))
                    referenceModel.refType="PUBMED";
                collectionModel.references.add(referenceModel);
            }
        }
    }

    private String getCollectionListQuery(){
        String query = "SELECT d.datatype_id, d.name, d.pattern, d.definition, d.obsolete, u.uri \n" +
                "FROM mir_datatype as d join mir_uri as u \n" +
                "where d.datatype_id = u.ptr_datatype and u.uri_type='URN' and u.deprecated=0;";

/*        String query = "SELECT d.datatype_id, d.name, d.pattern, d.definition, d.obsolete, u.uri, t.tag\n" +
                "FROM mir_datatype as d join mir_uri as u join mir_tag as t join mir_tag_link as tl\n" +
                "where d.datatype_id = u.ptr_datatype and u.uri_type='URN' and u.deprecated=0 \n" +
                "and tl.ptr_datatype = d.datatype_id and tl.ptr_tag = t.id;";*/

        return query;
    }

    private String getTagsQuery() {
         String query = "SELECT tl.ptr_datatype, t.tag FROM mir_tag as t join mir_tag_link as tl where tl.ptr_tag = t.id;";
        return query;
    }


    private String getResourcesListQuery(){
        String query = "SELECT r.resource_id, r.url_resource, r.info, r.institution, r.location, r.official, r.ptr_datatype, r.prefix, \n" +
                "(c.uptime/(c.uptime+c.downtime+c.unknown)*100) as uptime\n" +
                "FROM mir_resource as r join mir_url_check as c\n" +
                "where r.obsolete = 0 and r.resource_id=c.resource_id;";
        return query;
    }

    private String getReferencesQuery(){
        String query = "SELECT m.uri, m.uri_type, m.ptr_datatype FROM mir_doc as m;";
        return query;
    }
}
