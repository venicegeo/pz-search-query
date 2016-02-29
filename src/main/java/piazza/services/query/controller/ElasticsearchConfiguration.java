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

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfiguration {

	@Value("${elasticsearch.clustername}")
	private String clustername;

	@Value("${elasticsearch.hostname}")
	private String hostname;

	@Value("${elasticsearch.port}")
	private Integer port;

	@Bean
	public Client client() {
		final Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", clustername).build();
		final TransportClient client = new TransportClient(settings);
		client.addTransportAddress(new InetSocketTransportAddress(hostname, port));

		return client;
	}

}
