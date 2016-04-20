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

import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.data.DataResource;
import model.response.DataResourceListResponse;
import util.PiazzaLogger;

import piazza.services.query.controller.DataResourceContainer;

@RestController
public class SearchController {

	private PiazzaLogger logger = new PiazzaLogger();
	private final String API_ROOT = "search";
	
	@Autowired
	private Client client;
	
	@RequestMapping(value = API_ROOT + "/data/{keyValuePairs}", method = RequestMethod.GET)
	public DataResourceListResponse searchData(@PathVariable("keyValuePairs") String keyValuePairs) {
		try {
			Map<String,String> keyValuePairMap = getKeyValuePairsFromParameters(keyValuePairs);
	
			if( keyValuePairMap.containsKey("keyword") ) {
				return searchUsingGenericKeyword( keyValuePairMap.get("keyword") );
			}
			else {
				return searchUsingPiazzaFields( keyValuePairMap );
			}
		} 
		catch( Exception e ) {
			System.out.println("Error: " + e);
		}
		return null;
	}
	
	private DataResourceListResponse searchUsingGenericKeyword(String keyword) throws JsonParseException, JsonMappingException, IOException {
		// Construct DSL
		String dsl = new String();
		
		
		return queryElasticSearchWithDSL(dsl);
	}
	
	private DataResourceListResponse searchUsingPiazzaFields(Map<String,String> keyValuePairs) throws JsonParseException, JsonMappingException, IOException {
		// Construct DSL
		String dsl = new String();
		
		
		return queryElasticSearchWithDSL(dsl);
	}
	
	
	private DataResourceListResponse queryElasticSearchWithDSL(String dsl) throws JsonParseException, JsonMappingException, IOException {
		SearchHit[] hits = client.prepareSearch("pzmetadata").setTypes("DataResource").setSource(dsl).get().getHits().getHits();
		List<DataResource> responsePojos = new ArrayList<DataResource>();
		ObjectMapper mapper = new ObjectMapper();

		for (SearchHit hit : hits) {
//			DataResourceContainer drc =  ((DataResourceContainer)(mapper.readValue( hit.sourceAsString(), DataResourceContainer.class)));
			responsePojos.add( ((DataResourceContainer)(mapper.readValue( hit.sourceAsString(), DataResourceContainer.class))).dataResource );
		}
//			logger.log("\n\nResponse: " + mapper.writeValueAsString(responsePojos), PiazzaLogger.INFO);
		return new DataResourceListResponse( responsePojos );
	}

	private Map<String,String> getKeyValuePairsFromParameters(String keyValuePairs) {
		String[] keyValuePairArray = keyValuePairs.split("&");
		Map<String,String> keyValuePairMap = new HashMap<String,String>();
		for( int i=0; i<keyValuePairArray.length; i++) {
			String keyValuePairString =  keyValuePairArray[i];
			if( keyValuePairString.contains("=") && StringUtils.countOccurrencesOf(keyValuePairString, "=") == 1 
					&& keyValuePairString.split("=").length == 2 ) {
				keyValuePairMap.put(keyValuePairString.split("=")[0].toLowerCase(), keyValuePairString.split("=")[1]);
				System.out.println("Key: " + keyValuePairString.split("=")[0] + ", Value: " + keyValuePairString.split("=")[1]);
			}
		}
		return keyValuePairMap;
	}
}
