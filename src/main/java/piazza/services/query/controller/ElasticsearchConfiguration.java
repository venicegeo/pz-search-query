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
