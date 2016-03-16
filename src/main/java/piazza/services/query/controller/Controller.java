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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.job.type;

@RestController
public class Controller {

	private final String API_ROOT = "${api.basepath}";
	@Autowired
	private Client client;

	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "Hello Piazza Search Query! DSL-input endpoint at /api/v1/data";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = API_ROOT + "/data", method = RequestMethod.POST, consumes = "application/json")
	public List<String> getMetadataIds(@RequestBody(required = true) String esDSL) {
		SearchResponse response = client.prepareSearch("pzmetadata").setTypes("DataResource").setSource(esDSL).get();
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
	public List<String> getMetadata(@RequestBody(required = true) String esDSL) {
		SearchResponse response = client.prepareSearch("pzmetadata").setTypes("DataResource").setSource(esDSL).get();
		SearchHit[] hits = response.getHits().getHits();
		List<String> resultsList = new ArrayList<String>();
		for (SearchHit hit : hits) {
//			resultsList.add( hit.sourceAsString() );  // whole dataResource container
			//System.out.println(hit.sourceAsString() + "     whole source");
			// same?
			//Map<String, Object> json = hit.getSource();
			Map<String, Object> json = hit.sourceAsMap();
			//System.out.println(json.get("dataResource").toString());
			//Hmmm, dataResource sub-items are added in reverse order of expected
			// Oh well, for now; won't matter when serialized into Java object
			resultsList.add( json.get("dataResource").toString() );
		}
		return resultsList;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = API_ROOT + "/dsl", method = RequestMethod.POST, consumes = "application/json")
	public List<String> getMetadata(@RequestBody(required = true) SearchQueryJob esDSLJob) {
		
		// get reconstituted DSL string out of job object parameter
		ObjectMapper mapper = new ObjectMapper();
		String reconDSLstring = mapper.writeValueAsString( esDSLJob.getData() );
		
		SearchResponse response = client.prepareSearch("pzmetadata").setTypes("DataResource").setSource(reconDSLstring).get();
		SearchHit[] hits = response.getHits().getHits();
		List<String> resultsList = new ArrayList<String>();
		for (SearchHit hit : hits) {
//			resultsList.add( hit.sourceAsString() );  // whole dataResource container
			//System.out.println(hit.sourceAsString() + "     whole source");
			// same?
			//Map<String, Object> json = hit.getSource();
			Map<String, Object> json = hit.sourceAsMap();
			//System.out.println(json.get("dataResource").toString());
			//Hmmm, dataResource sub-items are added in reverse order of expected
			// Oh well, for now; won't matter when serialized into Java object
			resultsList.add( json.get("dataResource").toString() );
		}
		return resultsList;
	}

}