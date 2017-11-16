package uk.ac.ebi.idorg;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.*;

/**
 * Created by sarala on 15/07/2016.
 */
public class IdOrgEbiXmlGenerator {

    public static void main(String[] args) {


        ApplicationContext context = new ClassPathXmlApplicationContext("idorg-spring-config.xml");
        CMCreator cmCreator = (CMCreator) context.getBean("cmCreator");
        XMLGenerator xmlGenerator = (XMLGenerator) context.getBean("xmlGenerator");
        HashMap<String, CollectionModel> collectionModels = new HashMap<String, CollectionModel>();

        cmCreator.populateCollectionModelMap(collectionModels);
        cmCreator.populateTags(collectionModels);
        cmCreator.populateResouceModelMap(collectionModels);
        cmCreator.populateReferences(collectionModels);
        xmlGenerator.createRegistryDom(collectionModels);


        /*TODO - proper input testing */

        xmlGenerator.writeXML(args[0]);

    }

}


