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

import model.data.DataResource;
import piazza.services.query.util.GeoJsonDeserializer;
import piazza.services.query.util.GeoJsonSerializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vividsolutions.jts.geom.Geometry;

/*
 * Shell containing object for DataResource annotated for ElasticSearch _mapping
 * @author C. Smith
 * @Document(indexName = "pzmetadataalias", type = "DataResource")
 */

//@Document(indexName = "pzmetadataalias", type = "DataResourceContainer")
public class DataResourceContainer {
//	@Id
	public String dataResourceContainerId;
	
	// 8/9/16 need representation of <lat>,<lon> for correct entry,
	// without geohash, into Elasticsearch mapping of geo_point
	// 1/12/17 ObjectMapper serializes into lat,lon AND geohash (added!)
	// thus, GeoPoint in ES mapping, array representation in Java
	public Double[] locationCenterPoint; // lon, lat  - note order!
	
	// serialize into ES GeoShape
	@JsonSerialize(using = GeoJsonSerializer.class)
	@JsonDeserialize(using = GeoJsonDeserializer.class)
	public Geometry boundingArea = null;
	
//	@Field(type = FieldType.Nested)
	public DataResource dataResource;

	public DataResourceContainer() {
		// Empty constructor required by Jackson
	}
	
	public DataResourceContainer( DataResource dr )
	{
		dataResource = dr;
		dataResourceContainerId = dataResource.getDataId();
	}
	
	public Double[] getLocationCenterPoint() {
		return locationCenterPoint;
	}

	public void setLocationCenterPoint(Double[] gp) {
		this.locationCenterPoint = gp;
	}
	
	public Geometry getBoundingArea() {
		return boundingArea;
	}
	public void setBoundingArea(
		Geometry boundingArea ) {
		this.boundingArea = boundingArea;
	}

}

