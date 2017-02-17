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

import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
//import static org.elasticsearch.index.query.FilterBuilders.*;
//import static org.elasticsearch.index.query.FilterBuilders.*;
import org.elasticsearch.index.query.WrapperQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import exception.PiazzaJobException;
import model.data.DataResource;
import model.logger.AuditElement;
import model.logger.Severity;
import model.response.DataResourceListResponse;
import model.response.ServiceListResponse;
import model.service.metadata.Service;
import util.PiazzaLogger;

@RestController
public class Controller {

	static final String DATAINDEX = "pzmetadataalias";
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

	private final static Logger LOGGER = LoggerFactory.getLogger(Controller.class);

	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "Hello Piazza Search Query! DSL-input endpoint at /api/v1/datafull";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = API_ROOT + "/recordcount", method = RequestMethod.POST, consumes = "application/json")
	public Long getRecordCount(@RequestBody(required = true) String esDSL) {
		WrapperQueryBuilder qsqb = new WrapperQueryBuilder(esDSL);
		// QueryBuilder qsqb = QueryBuilders.wrapperQuery( esDSL ); //YES! Also works
		/*
		 * e.g. { "match_all" : { } } or { "match" : { "_all" : "kitten" } }
		 */
		CountResponse response = client.prepareCount(DATAINDEX).setQuery(qsqb).execute().actionGet();
		return response.getCount();
	}

	/**
	 * Statistics from Spring Boot
	 * 
	 * @return json as statistics
	 */
	@RequestMapping(value = "/admin/stats", method = RequestMethod.GET)
	public void stats(HttpServletResponse response) throws IOException {
		response.sendRedirect("/metrics");
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = API_ROOT + "/dataIds", method = RequestMethod.POST, consumes = "application/json")
	public List<String> getMetadataIds(@RequestBody(required = true) String esDSL,
			@RequestParam(value = "page", required = false, defaultValue = DEFAULT_PAGE) Integer page,
			@RequestParam(value = "perPage", required = false, defaultValue = DEFAULT_PAGE_SIZE) Integer perPage,
			@RequestParam(value = "order", required = false, defaultValue = DEFAULT_ORDER) String order,
			@RequestParam(value = "sortBy", required = false ) String sortBy) {

		SearchResponse response;
		
		if( sortBy != null)
			response = client.prepareSearch(DATAINDEX).setTypes(DATATYPE).setFrom(page.intValue() * perPage.intValue())
			.setSize(perPage.intValue()).addSort(sortBy, SortOrder.valueOf(order.toUpperCase())).setQuery(esDSL).get();
		else
			response = client.prepareSearch(DATAINDEX).setTypes(DATATYPE).setFrom(page.intValue() * perPage.intValue())
				.setSize(perPage.intValue()).setQuery(esDSL).get();
		
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
			@RequestParam(value = "sortBy", required = false ) String sortBy) throws PiazzaJobException {

		SearchResponse response;
		
		if( sortBy != null)
			response = client.prepareSearch(DATAINDEX).setTypes(DATATYPE).setFrom(page.intValue() * perPage.intValue())
			.setSize(perPage.intValue()).addSort(sortBy, SortOrder.valueOf(order.toUpperCase())).setQuery(esDSL).get();
		else
			response = client.prepareSearch(DATAINDEX).setTypes(DATATYPE).setFrom(page.intValue() * perPage.intValue())
				.setSize(perPage.intValue()).setQuery(esDSL).get();
		SearchHit[] hits = response.getHits().getHits();
		ObjectMapper mapper = new ObjectMapper();
		List<String> resultsList = new ArrayList<String>();
		try {
			for (SearchHit hit : hits) {
				// resultsList.add( hit.sourceAsString() ); // whole dataResource container
				logger.log(String.format("%s whole source", hit.sourceAsString()), Severity.INFORMATIONAL);
				DataResourceContainer drc = mapper.readValue(hit.sourceAsString(), DataResourceContainer.class);
				DataResource dr = drc.dataResource;
				resultsList.add(mapper.writeValueAsString(dr));
			}
			return resultsList;
		} catch (Exception exception) {
			String message = String.format("Error completing DSL to Elasticsearch: %s", exception.getMessage());
			LOGGER.error(message, exception);
			logger.log(message, Severity.ERROR);
			logger.log(message, Severity.ERROR, new AuditElement("searchQuery", "search", "queryString"));
			
			throw new PiazzaJobException(message);
		}
	}

	/*
	 * endpoint ingesting DSL string
	 * 
	 * @input Elasticsearch DSL
	 * 
	 * @return list of dataResource objects matching criteria nice JSON formatting for Postman!
	 */

	@SuppressWarnings("unchecked")
	@RequestMapping(value = API_ROOT + "/dslfordataresources", method = RequestMethod.POST, consumes = "application/json")
	public DataResourceListResponse getDSLtoDRs(@RequestBody(required = true) String esDSL,
			@RequestParam(value = "page", required = false, defaultValue = DEFAULT_PAGE) Integer page,
			@RequestParam(value = "perPage", required = false, defaultValue = DEFAULT_PAGE_SIZE) Integer perPage,
			@RequestParam(value = "order", required = false, defaultValue = DEFAULT_ORDER) String order,
			@RequestParam(value = "sortBy", required = false, defaultValue = DEFAULT_SORTBY) String sortBy) throws JsonParseException, JsonMappingException, IOException {

		SearchHit[] hits = null;
		try {
			SearchResponse response = client.prepareSearch(DATAINDEX).setTypes(DATATYPE).setFrom(page.intValue() * perPage.intValue())
					.setSize(perPage.intValue()).addSort(sortBy, SortOrder.valueOf(order.toUpperCase())).setQuery(esDSL).get();
			hits = response.getHits().getHits();
			logger.log(String.format("Searching for list of dataResource objects matching criteria %s", esDSL), Severity.INFORMATIONAL, new AuditElement("searchquery", "searchListOfDataResourceObjects", "query"));
		} catch (Exception exception) {
			String message = String.format(
					"Error constructing SearchResponse, client.prepareSearch- page.intValue:%d,  perPage.intValue:%d,  sortBy:%s,  order:%s,  exception:%s, query DSL: %s",
					page.intValue(), perPage.intValue(), sortBy, order, exception.getMessage(), esDSL);
			LOGGER.error(message, exception);
			logger.log(message, Severity.ERROR);
			logger.log(message, Severity.ERROR, new AuditElement("searchQuery", "search", "queryString"));
		}

		// List<String> resultsList = new ArrayList<String>();
		ObjectMapper mapper = new ObjectMapper();
		List<DataResource> responsePojos = new ArrayList<DataResource>();
		for (SearchHit hit : hits) {
			DataResourceContainer drc = mapper.readValue(hit.sourceAsString(), DataResourceContainer.class);
			responsePojos.add(drc.dataResource);
		}

		logger.log(String.format("\n\nResponse: %s", mapper.writeValueAsString(responsePojos)), Severity.INFORMATIONAL);
		return new DataResourceListResponse(responsePojos);
	}

	/**
	 * Endpoint containing DSL string for service search
	 * 
	 * @input Elasticsearch DSL
	 * 
	 * @return list of Service objects matching criteria
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = API_ROOT + "/dslservices", method = RequestMethod.POST, consumes = "application/json")
	public ServiceListResponse getServices(@RequestBody(required = true) Object esDSL,
			@RequestParam(value = "page", required = false, defaultValue = DEFAULT_PAGE) Integer page,
			@RequestParam(value = "perPage", required = false, defaultValue = DEFAULT_PAGE_SIZE) Integer perPage,
			@RequestParam(value = "order", required = false, defaultValue = DEFAULT_ORDER) String order,
			@RequestParam(value = "sortBy", required = false, defaultValue = DEFAULT_SERVICE_SORTBY) String sortBy) throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();

		// get reconstituted DSL string out of job object parameter
		String reconDSLstring;
		try {
			reconDSLstring = mapper.writeValueAsString(esDSL);
		} catch (Exception exception) {
			String message = String.format("Error Reconstituting DSL from SearchQueryJob: %s", exception.getMessage());
			LOGGER.error(message, exception);
			logger.log(message, Severity.ERROR);
			logger.log(message, Severity.ERROR, new AuditElement("searchQuery", "returnListOfServiceObjects", "queryString"));
			throw new IOException(message);
		}

		SearchResponse response;
		SearchHit[] hits;
		try {
			response = client.prepareSearch(SERVICESINDEX).setTypes(SERVICESTYPE).setFrom(page.intValue() * perPage.intValue())
					.setSize(perPage.intValue()).addSort(sortBy, SortOrder.valueOf(order.toUpperCase())).setQuery(reconDSLstring).get();
			hits = response.getHits().getHits();
			logger.log(String.format("Searching for list of Service objects matching criteria %s", reconDSLstring), Severity.INFORMATIONAL, new AuditElement("searchquery", "searchListOfServiceObjects", ""));
		} catch (Exception exception) {
			String message = String.format("Error completing DSL to Elasticsearch from Services Search: %s", exception.getMessage());
			LOGGER.error(message, exception);
			logger.log(message, Severity.ERROR);
			logger.log(message, Severity.ERROR, new AuditElement("searchQuery", "returnListOfServiceObjects", "queryString"));
			throw new IOException(message);
		}
		List<Service> responsePojos = new ArrayList<Service>();
		for (SearchHit hit : hits) {
			/*
			 * Map<String, Object> json = hit.sourceAsMap();
			 * DataResource dr = mapper.readValue( json.get("dataResource").toString(), DataResource.class);
			 * responsePojos.add( dr ); //resultsList.add( json.get("dataResource").toString() );
			 */
			// Map<String, Object> json = hit.sourceAsMap();
			ServiceContainer sc = mapper.readValue(hit.sourceAsString(), ServiceContainer.class);
			responsePojos.add(sc.service);
			// resultsList.add( json.get("dataResource").toString() );
		}
		logger.log("\n\nResponse: " + mapper.writeValueAsString(responsePojos), Severity.INFORMATIONAL);
		return new ServiceListResponse(responsePojos);
	}

}