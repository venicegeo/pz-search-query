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
package services.query.test;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.data.type.RasterDataType;
import piazza.services.query.controller.DataResourceContainer;
import piazza.services.query.controller.ServiceContainer;

/**
 * Tests container objects serialization/deserialization.
 * 
 * @author Patrick.Doody
 *
 */
public class ContainerTests {
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * Tests Service container serialization/deserialization
	 */
	@Test
	public void testServiceContainer() throws Exception {
		// Mock
		String mockServiceContainer = "{\"serviceContainerId\": \"test-container\",\"service\": {\"serviceId\": \"test-service-id\",\"url\": \"http://test.com\",\"contractUrl\": \"http://contract.com\",\"method\": \"GET\",\"resourceMetadata\": {\"name\": \"Testing\",\"classType\": {\"classification\": \"unclassified\"},\"createdBy\": \"tester\"}}}";

		// Test
		ServiceContainer serviceContainer = mapper.readValue(mockServiceContainer, ServiceContainer.class);

		// Verify
		Assert.assertTrue(serviceContainer != null);
		Assert.assertTrue(serviceContainer.serviceContainerId.equals("test-container"));
		Assert.assertTrue(serviceContainer.service.getServiceId().equals("test-service-id"));
		Assert.assertTrue(serviceContainer.service.getMethod().equals("GET"));
		Assert.assertTrue(serviceContainer.service.getResourceMetadata().getName().equals("Testing"));
		Assert.assertTrue(serviceContainer.service.getUrl().equals("http://test.com"));

		// Serialize back to String. Ensure no exceptions.
		String serialized = mapper.writeValueAsString(serviceContainer);

		Assert.assertTrue(serialized != null);
		Assert.assertTrue(serialized.isEmpty() == false);
	}

	/**
	 * Test an error with deserializing a ServiceContainer due to invalid JSON
	 * 
	 * @throws Exception
	 */
	@Test(expected = JsonParseException.class)
	public void testServiceContainerParseError() throws Exception {
		// Mock an incorrect Service Container String (missing end bracket)
		String mockServiceContainer = "{\"serviceContainerId\": \"test-container\",\"service\": {\"serviceId\": \"test-service-id\",\"url\": \"http://test.com\",\"contractUrl\": \"http://contract.com\",\"method\": \"GET\",\"resourceMetadata\": {\"name\": \"Testing\",\"classType\": {\"classification\": \"unclassified\"},\"createdBy\": \"tester\"}}";

		// Test
		mapper.readValue(mockServiceContainer, ServiceContainer.class);
	}

	/**
	 * Test an error with deserializing a ServiceContainer due to invalid schema in the JSON
	 * 
	 * @throws Exception
	 */
	@Test(expected = JsonMappingException.class)
	public void testServiceContainerMappingError() throws Exception {
		// Mock an incorrect Service Container String (incorrect prop name for serviceContainerId)
		String mockServiceContainer = "{\"servieContaerId\": \"test-container\",\"service\": {\"serviceId\": \"test-service-id\",\"url\": \"http://test.com\",\"contractUrl\": \"http://contract.com\",\"method\": \"GET\",\"resourceMetadata\": {\"name\": \"Testing\",\"classType\": {\"classification\": \"unclassified\"},\"createdBy\": \"tester\"}}";

		// Test
		mapper.readValue(mockServiceContainer, ServiceContainer.class);
	}

	/**
	 * Tests Data Resource container serialization/deserialization
	 */
	@Test
	public void testDataContainer() throws Exception {
		// Mock
		String mockDataContainer = "{\"dataResourceContainerId\": \"container-id\",\"dataResource\": {\"dataId\": \"test-data-id\",\"dataType\": {\"type\": \"raster\",\"type\": null,\"location\": {\"type\": \"s3\",\"bucketName\": \"external-public-access-test\",\"fileName\": \"elevation.tif\",\"fileSize\": 90074,\"domainName\": \"s3.amazonaws.com\"},\"mimeType\": null},\"spatialMetadata\": {\"coordinateReferenceSystem\": \"CRS\",\"epsgCode\": 32632,\"minX\": 496147.97,\"minY\": 5422119.88,\"maxX\": 496545.97,\"maxY\": 5422343.88},\"metadata\": {\"name\": \"elevation\",\"description\": \"geotiff_test\"}}}";

		// Test
		DataResourceContainer dataContainer = mapper.readValue(mockDataContainer, DataResourceContainer.class);

		// Verify
		Assert.assertTrue(dataContainer != null);
		Assert.assertTrue(dataContainer.dataResourceContainerId.equals("container-id"));
		Assert.assertTrue(dataContainer.dataResource.getDataId().equals("test-data-id"));
		Assert.assertTrue(dataContainer.dataResource.getDataType() instanceof RasterDataType);
		Assert.assertTrue(dataContainer.dataResource.getSpatialMetadata().getCoordinateReferenceSystem().equals("CRS"));
		Assert.assertTrue(dataContainer.dataResource.getMetadata().getName().equals("elevation"));

		// Serialize back out
		String serialized = mapper.writeValueAsString(dataContainer);

		// Ensure correct
		Assert.assertTrue(serialized.isEmpty() == false);
	}

	/**
	 * Test an error with deserializing a DataContainer due to invalid JSON
	 * 
	 * @throws Exception
	 */
	@Test(expected = JsonParseException.class)
	public void testDataContainerParseError() throws Exception {
		// Mock an incorrect Data Container String (missing end bracket)
		String mockDataContainer = "{\"dataResourceContainerId\": \"container-id\",\"dataResource\": {\"dataId\": \"test-data-id\",\"dataType\": {\"type\": \"raster\",\"type\": null,\"location\": {\"type\": \"s3\",\"bucketName\": \"external-public-access-test\",\"fileName\": \"elevation.tif\",\"fileSize\": 90074,\"domainName\": \"s3.amazonaws.com\"},\"mimeType\": null},\"spatialMetadata\": {\"coordinateReferenceSystem\": \"CRS\",\"epsgCode\": 32632,\"minX\": 496147.97,\"minY\": 5422119.88,\"maxX\": 496545.97,\"maxY\": 5422343.88},\"metadata\": {\"name\": \"elevation\",\"description\": \"geotiff_test\"}}";

		// Test
		mapper.readValue(mockDataContainer, DataResourceContainer.class);
	}

	/**
	 * Test an error with deserializing a DataContainer due to invalid schema in the JSON
	 * 
	 * @throws Exception
	 */
	@Test(expected = JsonMappingException.class)
	public void testDataContainerMappingError() throws Exception {
		// Mock an incorrect Data Container String (incorrect prop name for containerId)
		String mockDataContainer = "{\"datResourcContainrId\": \"container-id\",\"dataResource\": {\"dataId\": \"test-data-id\",\"dataType\": {\"type\": \"raster\",\"type\": null,\"location\": {\"type\": \"s3\",\"bucketName\": \"external-public-access-test\",\"fileName\": \"elevation.tif\",\"fileSize\": 90074,\"domainName\": \"s3.amazonaws.com\"},\"mimeType\": null},\"spatialMetadata\": {\"coordinateReferenceSystem\": \"CRS\",\"epsgCode\": 32632,\"minX\": 496147.97,\"minY\": 5422119.88,\"maxX\": 496545.97,\"maxY\": 5422343.88},\"metadata\": {\"name\": \"elevation\",\"description\": \"geotiff_test\"}}}";

		// Test
		mapper.readValue(mockDataContainer, DataResourceContainer.class);
	}
}
