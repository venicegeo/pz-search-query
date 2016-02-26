package piazza.services.query.controller;

//import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

//import com.fasterxml.jackson.core.JsonParseException;
//import com.fasterxml.jackson.databind.JsonMappingException;
//import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class Controller {

	private final String API_ROOT = "${api.basepath}";
	@Autowired
	private Client client;

	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "Hello Piazza Search Query!";
	}

	@RequestMapping(value = API_ROOT + "/esDSL", method = RequestMethod.POST, consumes = "application/json")
	public List<String> getMetadata(@RequestBody(required = true) String esDSL)/* throws JsonParseException, JsonMappingException, IOException */{
		SearchResponse response = client.prepareSearch("pzmetadata").setTypes("DataResource").setSource(esDSL).get();
		SearchHit[] hits = response.getHits().getHits();
		List<String> resultsList = new ArrayList<String>();
//		ObjectMapper mapper = new ObjectMapper();
		for (SearchHit hit : hits) {
//			resultsList.add(mapper.readValue(hit.sourceAsString(), Metadata.class));
			resultsList.add(hit.sourceAsString());
		}
		return resultsList;
	}

}
