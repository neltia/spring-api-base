package neltia.bloguide.api.infrastructure.driver;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Configuration
public class ElasticConnector {

    @Value("${spring.elasticsearch.es-hosts-local}")
    private List<String> localEsHosts;

    @Value("${spring.elasticsearch.es-port-local}")
    private int localEsPort;

    @Value("${spring.elasticsearch.es-username-local}")
    private String localEsUsername;

    @Value("${spring.elasticsearch.es-password-local}")
    private String localEsPassword;

    @Value("${spring.elasticsearch.es-secure-local}")
    private String localEsSecure;

    private HttpHost[] getHostList(List<String> addressList, int port, String secure) {
        return addressList.stream()
                .map(host -> new HttpHost(host, port, secure))
                .toArray(HttpHost[]::new);
    }

    @Bean(name = "localElasticClient")
    public ElasticsearchClient localElasticClient() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, (chain, authType) -> true)
                    .build();
            System.out.println("SSL context successfully created.");
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new RuntimeException("Error creating SSL context", e);
        }

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(localEsUsername, localEsPassword)
        );

        String scheme = localEsSecure.equals("true") ? "https" : "http";

        SSLContext finalSslContext = sslContext;
        RestClientBuilder builder = RestClient.builder(getHostList(localEsHosts, localEsPort, scheme))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    if (localEsSecure.equalsIgnoreCase("true")) {
                        httpClientBuilder.setSSLContext(finalSslContext);
                        httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                    }
                    return httpClientBuilder;
                })
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout(10000)  // 연결 타임아웃 (ms)
                        .setSocketTimeout(30000)  // 소켓 타임아웃 (ms)
                );

        RestClient restClient = builder.build();
        System.out.println("RestClient built successfully.");
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        System.out.println("ElasticsearchClient successfully created.");

        return new ElasticsearchClient(transport);
    }
}
