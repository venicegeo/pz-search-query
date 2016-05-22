package piazza.services.query.controller;

import model.service.metadata.Service;

//import org.elasticsearch.common.geo.GeoPoint;

/*
 * Shell containing object for DataResource annotated for ElasticSearch _mapping
 * @author C. Smith
 * @Document(indexName = "pzmetadata", type = "DataResource")
 */

//@Document(indexName = "pzservices", type = "ServiceContainer")
public class ServiceContainer {
//	@Id
	public String serviceContainerId;
	
//	@Field(type = FieldType.Nested)
	public Service service;

	public ServiceContainer( ) { }
	
	public ServiceContainer( Service s )
	{
		service = s;
		serviceContainerId = service.getServiceId();
	}

}
