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

import java.io.IOException;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletResponse;

import piazza.services.query.controller.Controller;
import util.PiazzaLogger;

/**
 * Tests logic in the Controller.
 * 
 * @author Patrick.Doody
 *
 */
public class ControllerTests {
	@Mock
	private PiazzaLogger logger;
	@InjectMocks
	private Controller controller;

	/**
	 * Initial test setup
	 */
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests admin/stats endpoint
	 */
	@Test
	public void testAdminStats() throws IOException {
		// Ensuring no Exceptions are thrown
		MockHttpServletResponse mockResponse = new MockHttpServletResponse();
		controller.stats(mockResponse);
	}

	/**
	 * Testing querying of data exception
	 */
	@Test(expected = Exception.class)
	public void testQueryDataException() throws Exception {
		controller.getDSLtoDRs("DSL", new Integer(0), new Integer(10), "asc", "metadata.name");
	}

	/**
	 * Testing querying of services exception
	 */
	@Test(expected = Exception.class)
	public void testQueryServiceException() throws Exception {
		controller.getServices("DSL", new Integer(0), new Integer(10), "asc", "metadata.name");
	}

	/**
	 * Test getting metadata exception
	 */
	@Test(expected = Exception.class)
	public void testMetadataException() throws Exception {
		controller.getMetadataFull("DSL", new Integer(0), new Integer(10), "asc", "metadata.name");
	}

	/**
	 * Test getting metadata IDs
	 */
	@Test(expected = Exception.class)
	public void testMetadataIdsException() throws Exception {
		controller.getMetadataIds("DSL", new Integer(0), new Integer(10), "asc", "metadata.name");
	}
}
