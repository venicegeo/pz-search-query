/**
 * Copyright 2016, RadiantBlue Technologies, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package piazza.services.query.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.sort.SortOrder;
//import static org.elasticsearch.index.query.FilterBuilders.*;
import org.elasticsearch.index.query.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import piazza.services.query.controller.DataResourceContainer;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.job.type.SearchQueryJob;
import model.data.DataResource;
import model.response.DataResourceListResponse;
import model.response.ServiceListResponse;
import model.service.metadata.Service;
import util.PiazzaLogger;

@RestController
public class Controller {
	
	static final String DATAINDEX = "pzmetadata";
	static final String DATATYPE = "DataResourceContainer";
	static final String SERVICESINDEX = "pzservices";
	static final String SERVICESTYPE = "ServiceContainer";
	static final int maxreturncount = 1000;
	static final String DEFAULT_PAGE_SIZE = "10";
	static final String DEFAULT_PAGE = "0";
	static final String DEFAULT_SORTBY = "dataResource.metadata.createdOn";
	static final String DEFAULT_SERVICE_SORTBY = "service.serviceId";
	static final String DEFAULT_ORDER = "desc";

	@Autowired
	private PiazzaLogger logger;
	private final String API_ROOT = "${api.basepath}";
	@Autowired
	private Client client;

	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "Hello Piazza Search Query! DSL-input endpoint at /api/v1/datafull";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = API_ROOT + "/recordcount", method = RequestMethod.POST, consumes = "application/json")
	public Long getRecordCount(@RequestBody(required = true) String esDSL) {
		WrapperQueryBuilder qsqb = new WrapperQueryBuilder( esDSL );
		//QueryBuilder qsqb = QueryBuilders.wrapperQuery( esDSL ); //YES! Also works
		/*   e.g. { "match_all" : { } }
		 or
			{
			"match" : {
				"_all" : "kitten"
			}
}
		 */
		CountResponse response = client.prepareCount(DATAINDEX)
		        .setQuery( qsqb )
		        .execute()
		        .actionGet();
		return response.getCount();
	}
	
	/**
	 * Statistics from Spring Boot
	 * 
	 * @return json as statistics
	 */
	@RequestMapping(value = "/admin/stats",  method = RequestMethod.GET)
	public void stats(HttpServletResponse response) throws IOException {
		 response.sendRedirect("/metrics");
	}

	@RequestMapping(value = "/voyagerJSON",  method = RequestMethod.GET)
	@ResponseBody
	public String voyagerJSON() throws IOException {
		response.sendRedirect( "${voyager.json}" );
	}

	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = API_ROOT + "/dataIds", method = RequestMethod.POST, consumes = "application/json")
	public List<String> getMetadataIds(@RequestBody(required = true) String esDSL,
			@RequestParam(value = "page", required = false, defaultValue = DEFAULT_PAGE) Integer page,
			@RequestParam(value = "perPage", required = false, defaultValue = DEFAULT_PAGE_SIZE) Integer perPage,
			@RequestParam(value = "order", required = false, defaultValue = DEFAULT_ORDER) String order,
			@RequestParam(value = "sortBy", required = false, defaultValue = DEFAULT_SORTBY) String sortBy
	) {
		
		SearchResponse response = client.prepareSearch(DATAINDEX).setTypes(DATATYPE).
				setFrom( page.intValue() * perPage.intValue() ).
				setSize( perPage.intValue() ).
				addSort( sortBy, SortOrder.valueOf( order.toUpperCase() ) ).
				setQuery(esDSL).get();
		SearchHit[] hits = response.getHits().getHits();
		List<String> resultsList = new ArrayList<String>();
		for (SearchHit hit : hits) {
			Map<String, Object> json = hit.getSource();
			resultsList.add((String) ((HashMap<Object, Object>) json.get("dataResource")).get("dataId"));
		}
		return resultsList;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = API_ROOT + "/datafull", method = RequestMethod.POST, consumes = "application/json")
	public List<String> getMetadataFull(@RequestBody(required = true) String esDSL,
			@RequestParam(value = "page", required = false, defaultValue = DEFAULT_PAGE) Integer page,
			@RequestParam(value = "perPage", required = false, defaultValue = DEFAULT_PAGE_SIZE) Integer perPage,
			@RequestParam(value = "order", required = false, defaultValue = DEFAULT_ORDER) String order,
			@RequestParam(value = "sortBy", required = false, defaultValue = DEFAULT_SORTBY) String sortBy
	)   throws Exception {
		
		SearchResponse response = client.prepareSearch(DATAINDEX).setTypes(DATATYPE).
				setFrom( page.intValue() * perPage.intValue() ).
				setSize( perPage.intValue() ).
				addSort( sortBy, SortOrder.valueOf( order.toUpperCase() ) ).
				setQuery(esDSL).get();
//		SearchResponse response = client.prepareSearch(DATAINDEX).setTypes(DATATYPE).setSize(maxreturncount).setExtraSource(esDSL).get();
//		SearchResponse response = client.prepareSearch(DATAINDEX).setTypes(DATATYPE).setSize(maxreturncount).setExtraSource(esDSL).execute().actionGet();
		SearchHit[] hits = response.getHits().getHits();
		ObjectMapper mapper = new ObjectMapper();
		List<String> resultsList = new ArrayList<String>();
		try {
			for (SearchHit hit : hits) {
	//			resultsList.add( hit.sourceAsString() );  // whole dataResource container
				System.out.println(hit.sourceAsString() + "     whole source");
				DataResourceContainer drc =  mapper.readValue( hit.sourceAsString(), DataResourceContainer.class);
				DataResource dr = drc.dataResource;
				resultsList.add( mapper.writeValueAsString( dr ) );
				//resultsList.add( dr.toString() );
/*	
				Map<String, Object> json = hit.sourceAsMap();
				System.out.println(json.get("dataResource").toString());
				DataResource dr =  mapper.readValue( json.get("dataResource").toString(), DataResource.class);
				responsePojos.add( dr );
*/
				
	//			SearchHitField drField = hit.field("data");
	//			resultsList.add( drField.getValues().toString() );
	//			mapper.writeValueAsString( esDSLJob.getData() );
				// same?
				//Map<String, Object> json = hit.getSource();
				//Map<String, Object> json = hit.sourceAsMap();
				//System.out.println(json.get("dataResource"));
				//Hmmm, dataResource sub-items are added in reverse order of expected
				// Oh well, for now; won't matter when serialized into Java object
				//resultsList.add( json.get("dataResource").toString() );
				//System.out.println("Ready to add dataResource:");
				//resultsList.add( (String) json.get("dataResource") );
			}
			return resultsList;
		} catch (Exception exception) {
			String message = String.format("Error completing DSL to Elasticsearch: %s", exception.getMessage());
			logger.log(message, PiazzaLogger.ERROR);
			throw new Exception(message);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = API_ROOT + "/dslforJSON", method = RequestMethod.POST, consumes = "application/json")
	public List<String> getMetadataJobToJSON(@RequestBody(required = true) SearchQueryJob esDSLJob,
			@RequestParam(value = "page", required = false, defaultValue = DEFAULT_PAGE) Integer page,
			@RequestParam(value = "perPage", required = false, defaultValue = DEFAULT_PAGE_SIZE) Integer perPage,
			@RequestParam(value = "order", required = false, defaultValue = DEFAULT_ORDER) String order,
			@RequestParam(value = "sortBy", required = false, defaultValue = DEFAULT_SORTBY) String sortBy
	)  throws Exception {
		
		// get reconstituted DSL string out of job object parameter
		String reconDSLstring;
		try {
			ObjectMapper mapper = new ObjectMapper();
			reconDSLstring = mapper.writeValueAsString( esDSLJob.getData() );
		} catch (Exception exception) {
			String message = String.format("Error Reconstituting DSL from SearchQueryJob: %s", exception.getMessage());
			logger.log(message, PiazzaLogger.ERROR);
			throw new Exception(message);
		}
			
		try {
			ObjectMapper mapper = new ObjectMapper();
			SearchResponse response = client.prepareSearch(DATAINDEX).setTypes(DATATYPE).
					setFrom( page.intValue() * perPage.intValue() ).
					setSize( perPage.intValue() ).
					addSort( sortBy, SortOrder.valueOf( order.toUpperCase() ) ).
					setQuery(reconDSLstring).get();
			SearchHit[] hits = response.getHits().getHits();
			List<String> resultsList = new ArrayList<String>();
			for (SearchHit hit : hits) {
				DataResourceContainer drc =  mapper.readValue( hit.sourceAsString(), DataResourceContainer.class);
				DataResource dr = drc.dataResource;
				resultsList.add( mapper.writeValueAsString( dr ) );
			}
			return resultsList;
		} catch (Exception exception) {
			String message = String.format("Error completing DSL to Elasticsearch: %s", exception.getMessage());
			logger.log(message, PiazzaLogger.ERROR);
			throw new Exception(message);
		}
	}

	/* 
	 * endpoint ingesting DSL string
	 * @input Elasticsearch DSL 
	 * @return list of dataResource objects matching criteria
	 * nice JSON formatting for Postman!
	 */

		@SuppressWarnings("unchecked")
		@RequestMapping(value = API_ROOT + "/dslfordataresources", method = RequestMethod.POST, consumes = "application/json")
		public DataResourceListResponse getDSLtoDRs(@RequestBody(required = true) String esDSL,
				@RequestParam(value = "page", required = false, defaultValue = DEFAULT_PAGE) Integer page,
				@RequestParam(value = "perPage", required = false, defaultValue = DEFAULT_PAGE_SIZE) Integer perPage,
				@RequestParam(value = "order", required = false, defaultValue = DEFAULT_ORDER) String order,
				@RequestParam(value = "sortBy", required = false, defaultValue = DEFAULT_SORTBY) String sortBy
		)  throws Exception {
		
			
		SearchHit[] hits = null;
		try {
			SearchResponse response = client.prepareSearch(DATAINDEX).setTypes(DATATYPE).
					setFrom( page.intValue() * perPage.intValue() ).
					setSize( perPage.intValue() ).
					addSort( sortBy, SortOrder.valueOf( order.toUpperCase() ) ).
					setQuery(esDSL).get();
			hits = response.getHits().getHits();
		} catch (Exception exception) {
			exception.printStackTrace();
			String message = String.format("Error constructing SearchResponse, client.prepareSearch- page.intValue:%d,  perPage.intValue:%d,  sortBy:%s,  order:%s,  exception:%s, query DSL: %s", 
														page.intValue(), perPage.intValue(), sortBy, order, exception.getMessage(), esDSL );
			System.out.println(message);
			logger.log(message, PiazzaLogger.ERROR);
			//throw new Exception(message);
		}
		
//		List<String> resultsList = new ArrayList<String>();
		ObjectMapper mapper = new ObjectMapper();
		List<DataResource> responsePojos = new ArrayList<DataResource>();
		for (SearchHit hit : hits) {
//			resultsList.add( hit.sourceAsString() );  // whole dataResource container
			//System.out.println(hit.sourceAsString() + "     whole source of hit");
			// same?
			//Map<String, Object> json = hit.getSource();
			DataResourceContainer drc =  mapper.readValue( hit.sourceAsString(), DataResourceContainer.class);
			responsePojos.add( drc.dataResource );
			//Hmmm, dataResource sub-items are added in reverse order of expected
			// Oh well, for now; won't matter when serialized into Java object
			//resultsList.add( json.get("dataResource").toString() );
		}
		System.out.println("\n\nResponse: " + mapper.writeValueAsString(responsePojos));
		return new DataResourceListResponse( responsePojos );
	}

	/* 
	 * endpoint ingesting SearchQueryJob containing DSL string
	 * @input Elasticsearch SearchQueryJob containing DSL 
	 * @return list of dataResource objects matching criteria
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = API_ROOT + "/dsl", method = RequestMethod.POST, consumes = "application/json")
	public DataResourceListResponse getMetadataJob(@RequestBody(required = true) SearchQueryJob esDSLJob,
			@RequestParam(value = "page", required = false, defaultValue = DEFAULT_PAGE) Integer page,
			@RequestParam(value = "perPage", required = false, defaultValue = DEFAULT_PAGE_SIZE) Integer perPage,
			@RequestParam(value = "order", required = false, defaultValue = DEFAULT_ORDER) String order,
			@RequestParam(value = "sortBy", required = false, defaultValue = DEFAULT_SORTBY) String sortBy
	)  throws Exception {
		
		ObjectMapper mapper = new ObjectMapper();
		
		// get reconstituted DSL string out of job object parameter
		String reconDSLstring;
		try {
			reconDSLstring = mapper.writeValueAsString( esDSLJob.getData() );
			//System.out.println("The Re-Constituted DSL query:");
			//System.out.println( reconDSLstring );
		} catch (Exception exception) {
			String message = String.format("Error Reconstituting DSL from SearchQueryJob: %s", exception.getMessage());
			logger.log(message, PiazzaLogger.ERROR);
			throw new Exception(message);
		}
		
		SearchResponse response;
		SearchHit[] hits;
		try {
			response = client.prepareSearch(DATAINDEX).setTypes(DATATYPE).
					setFrom( page.intValue() * perPage.intValue() ).
					setSize( perPage.intValue() ).
					addSort( sortBy, SortOrder.valueOf( order.toUpperCase() ) ).
					setQuery(reconDSLstring).get();
			hits = response.getHits().getHits();
		} catch (Exception exception) {
			String message = String.format("Error completing DSL to Elasticsearch from SearchQueryJob: %s", exception.getMessage());
			logger.log(message, PiazzaLogger.ERROR);
			throw new Exception(message);
		}
		List<DataResource> responsePojos = new ArrayList<DataResource>();
		for (SearchHit hit : hits) {
			/*
			Map<String, Object> json = hit.sourceAsMap();
			System.out.println(json.get("dataResource").toString());
			DataResource dr =  mapper.readValue( json.get("dataResource").toString(), DataResource.class);
			responsePojos.add( dr );
			//resultsList.add( json.get("dataResource").toString() );
			 * */
			//Map<String, Object> json = hit.sourceAsMap();
			//System.out.println(json.get("dataResource").toString());
			DataResourceContainer drc =  mapper.readValue( hit.sourceAsString(), DataResourceContainer.class);
			responsePojos.add( drc.dataResource );
			//resultsList.add( json.get("dataResource").toString() );
		}
		logger.log("\n\nResponse: " + mapper.writeValueAsString(responsePojos), PiazzaLogger.INFO);
		//System.out.println("\n\nResponse: " + mapper.writeValueAsString(responsePojos));
		return new DataResourceListResponse( responsePojos );
	}

	/* 
	 * endpoint containing DSL string for service search
	 * @input Elasticsearch  DSL 
	 * @return list of Service objects matching criteria
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = API_ROOT + "/dslservices", method = RequestMethod.POST, consumes = "application/json")
	public ServiceListResponse getServices(@RequestBody(required = true) Object esDSL,
			@RequestParam(value = "page", required = false, defaultValue = DEFAULT_PAGE) Integer page,
			@RequestParam(value = "perPage", required = false, defaultValue = DEFAULT_PAGE_SIZE) Integer perPage,
			@RequestParam(value = "order", required = false, defaultValue = DEFAULT_ORDER) String order,
			@RequestParam(value = "sortBy", required = false, defaultValue = DEFAULT_SERVICE_SORTBY) String sortBy
	)  throws Exception {
		
		ObjectMapper mapper = new ObjectMapper();
		
		// get reconstituted DSL string out of job object parameter
		String reconDSLstring;
		try {
			reconDSLstring = mapper.writeValueAsString( esDSL );
			//System.out.println("The Re-Constituted DSL query:");
			//System.out.println( reconDSLstring );
		} catch (Exception exception) {
			String message = String.format("Error Reconstituting DSL from SearchQueryJob: %s", exception.getMessage());
			logger.log(message, PiazzaLogger.ERROR);
			throw new Exception(message);
		}
		
		SearchResponse response;
		SearchHit[] hits;
		try {
			response = client.prepareSearch(SERVICESINDEX).setTypes(SERVICESTYPE).
					setFrom( page.intValue() * perPage.intValue() ).
					setSize( perPage.intValue() ).
					addSort( sortBy, SortOrder.valueOf( order.toUpperCase() ) ).
					setQuery(reconDSLstring).get();
			hits = response.getHits().getHits();
		} catch (Exception exception) {
			String message = String.format("Error completing DSL to Elasticsearch from Services Search: %s", exception.getMessage());
			logger.log(message, PiazzaLogger.ERROR);
			throw new Exception(message);
		}
		List<Service> responsePojos = new ArrayList<Service>();
		for (SearchHit hit : hits) {
			/*
			Map<String, Object> json = hit.sourceAsMap();
			System.out.println(json.get("dataResource").toString());
			DataResource dr =  mapper.readValue( json.get("dataResource").toString(), DataResource.class);
			responsePojos.add( dr );
			//resultsList.add( json.get("dataResource").toString() );
			 * */
			//Map<String, Object> json = hit.sourceAsMap();
			//System.out.println(json.get("dataResource").toString());
			ServiceContainer sc =  mapper.readValue( hit.sourceAsString(), ServiceContainer.class);
			responsePojos.add( sc.service );
			//resultsList.add( json.get("dataResource").toString() );
		}
		logger.log("\n\nResponse: " + mapper.writeValueAsString(responsePojos), PiazzaLogger.INFO);
		//System.out.println("\n\nResponse: " + mapper.writeValueAsString(responsePojos));
		return new ServiceListResponse( responsePojos );
	}

}