package uk.ac.ebi.idorg;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by sarala on 22/07/2016.
 */
public class XMLGenerator {

    DocumentBuilder docBuilder = null;
    Document doc;

    public XMLGenerator() {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

     public void createRegistryDom(HashMap<String, CollectionModel> collectionModels){
        doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("database");
        doc.appendChild(rootElement);

        //database info
        rootElement.appendChild(createBasicElements("name","IdentifiersRegistry"));
        rootElement.appendChild(createBasicElements("description","Identifiers.org is a system providing resolvable persistent URIs used to identify data for the scientific community, with a current focus on the Life Sciences domain."));

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        rootElement.appendChild(createBasicElements("release_date",dateFormat.format(cal.getTime())));
        rootElement.appendChild(createBasicElements("entry_count", String.valueOf(getEntryCount(collectionModels))));

        Element entriesElement = doc.createElement("entries");
        rootElement.appendChild(entriesElement);

        for (Map.Entry<String, CollectionModel> collectionMap : collectionModels.entrySet()) {
            CollectionModel collectionModel = collectionMap.getValue();

            Element entry = doc.createElement("entry");
            entriesElement.appendChild(entry);

            // set attribute to entry element
            Attr attr = doc.createAttribute("id");
            attr.setValue(collectionModel.datatype_id);
            entry.setAttributeNode(attr);

            entry.appendChild(createBasicElements("name",collectionModel.name));
            entry.appendChild(createBasicElements("description",collectionModel.definition));


            Element additionalFields = doc.createElement("additional_fields");
            entry.appendChild(additionalFields);

            additionalFields.appendChild(createFieldElement("namespace",collectionModel.namespace));
            additionalFields.appendChild(createFieldElement("pattern",collectionModel.pattern));
            additionalFields.appendChild(createFieldElement("obsolete",collectionModel.getObsolete()));
            additionalFields.appendChild(createFieldElement("uri",collectionModel.uri));
            additionalFields.appendChild(createFieldElement("type",collectionModel.type));

            for (String tag: collectionModel.tags) {
                additionalFields.appendChild(createFieldElement("tag",tag));
            }

            if(!collectionModel.references.isEmpty()) {
                Element cross_references = doc.createElement("cross_references");
                entry.appendChild(cross_references);

                for (ReferenceModel referenceModel : collectionModel.references) {
                      cross_references.appendChild(createCrossRefs(referenceModel.getRefId(), referenceModel.refType));
                }
            }



            for (ResourceModel resourceModel:collectionModel.resources) {
                entry = doc.createElement("entry");
                entriesElement.appendChild(entry);

                // set attribute to entry element
                attr = doc.createAttribute("id");
                attr.setValue(resourceModel.resource_id);
                entry.setAttributeNode(attr);

                entry.appendChild(createBasicElements("name",resourceModel.info));
                entry.appendChild(createBasicElements("description", collectionModel.definition));

                additionalFields = doc.createElement("additional_fields");
                entry.appendChild(additionalFields);

                if(!resourceModel.providerCode.isEmpty())
                    additionalFields.appendChild(createFieldElement("provider_code",resourceModel.providerCode));

                additionalFields.appendChild(createFieldElement("institution",resourceModel.institution));
                additionalFields.appendChild(createFieldElement("uri","http://www.ebi.ac.uk/miriam/main/resources/"+resourceModel.resource_id));
                additionalFields.appendChild(createFieldElement("official",resourceModel.getOfficial()));
                additionalFields.appendChild(createFieldElement("type",resourceModel.type));

                String [] locations = resourceModel.location.split("/");
                for(String location:locations)
                    additionalFields.appendChild(createFieldElement("location",location.trim()));

                if(resourceModel.uptime!=null) {
                    additionalFields.appendChild(createFieldElement("uptime", resourceModel.getUptime()));

                    int uptime = resourceModel.uptime.setScale(0,BigDecimal.ROUND_HALF_UP).intValue();

                    if(uptime>95){
                        additionalFields.appendChild(createFieldElement("uptime-range", "Over 95%"));
                    }else if(uptime<75){
                        additionalFields.appendChild(createFieldElement("uptime-range", "Under 75%"));
                    }else{
                        additionalFields.appendChild(createFieldElement("uptime-range", "75% - 95%"));
                    }

                }

                for (String tag: collectionModel.tags) {
                    additionalFields.appendChild(createFieldElement("tag",tag));
                }

                Element cross_references = doc.createElement("cross_references");
                entry.appendChild(cross_references);
                cross_references.appendChild(createCrossRefs(collectionModel.datatype_id,"COLLECTION"));
            }
        }
    }

    private Element createCrossRefs(String dbkeyVal, String dbnameVal){
        Element ref = doc.createElement("ref");
        Attr refAttr = doc.createAttribute("dbkey");
        refAttr.setValue(dbkeyVal);
        ref.setAttributeNode(refAttr);
        refAttr = doc.createAttribute("dbname");
        refAttr.setValue(dbnameVal);
        ref.setAttributeNode(refAttr);
        return ref;
    }

    private int getEntryCount(HashMap<String, CollectionModel> collectionModels){
        int entrycount = collectionModels.size();
        for (Map.Entry<String, CollectionModel> collectionModelEntry : collectionModels.entrySet()) {
            entrycount += collectionModelEntry.getValue().resources.size();
        }
        return entrycount;

    }

    private Element createBasicElements(String elementName, String elementValue){
        Element element = doc.createElement(elementName);
        element.appendChild(doc.createTextNode(elementValue));
        return element;
    }


    private Element createFieldElement(String fieldName, String fieldValue){
        Element field = doc.createElement("field");
        field.appendChild(doc.createTextNode(fieldValue));
        Attr attr = doc.createAttribute("name");
        attr.setValue(fieldName);
        field.setAttributeNode(attr);
        return field;
    }

    public void writeXML(String fileName){
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(fileName));

            // Output to console for testing
            //StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);
            } catch (TransformerException e) {
                e.printStackTrace();
        }
    }
}
