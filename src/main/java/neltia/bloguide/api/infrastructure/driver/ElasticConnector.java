package neltia.bloguide.api.infrastructure.driver;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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

    /*
     * Input: List<String> addressList
     * @return HttpHost[]
     */
    public HttpHost[] getHostList(List<String> addressList, int port, String secure) {
        List<HttpHost> hostList = new ArrayList<>();
        addressList.forEach(s -> {
            hostList.add(new HttpHost(s, port, secure));
        });

        return hostList.toArray(new HttpHost[0]);
    }
    /*
     * Input: List<String> addressList
     * @return String[]
     */
    public String[] getHostStrArr(List<String> addressList, int port) {
        List<String> hostArr = new ArrayList<>();
        addressList.forEach(s -> {
            hostArr.add(s + ":" + Integer.toString(port));
        });
        return hostArr.toArray(new String[hostArr.size()]);
    }

    /*
     * Elasticsearch 클러스터에 연결하는 데 사용되는 RestHighLevelClient 객체 생성
     * @return RestHighLevelClient
     */
    @Bean(name="localElasticClientEx")
    @Qualifier("localElasticClientEx")
    public ElasticsearchClient localElasticClientEx() {
        SSLContextBuilder sslBuilder = null;
        SSLContext sslContext = null;
        try {
            sslBuilder = SSLContexts.custom().loadTrustMaterial(null, (x509Certificates, s) -> true);
            sslContext = sslBuilder.build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            System.out.println(e.getMessage());
        }

        final CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(localEsUsername, localEsPassword));

        String httpSecure = localEsSecure.equals("true") ? "https" : "http";

        SSLContext finalSslContext = sslContext;
        RestClientBuilder builder = RestClient.builder(getHostList(localEsHosts, localEsPort, httpSecure)).
                setHttpClientConfigCallback(httpClientBuilder -> {
                    httpClientBuilder.disableAuthCaching();
                    if(localEsSecure.equals("true")) {
                        return httpClientBuilder
                                .setDefaultCredentialsProvider(credentialsProvider).setSSLContext(finalSslContext).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                    } else {
                        return httpClientBuilder
                                .setDefaultCredentialsProvider(credentialsProvider);
                    }
                });

        ElasticsearchTransport transport = new RestClientTransport(
                builder.build(), new JacksonJsonpMapper());

        ElasticsearchClient client = new ElasticsearchClient(transport);

        return client;
    }

    @Bean(destroyMethod = "close", name = "localElasticClient")
    @Qualifier("localElasticClient")
    public RestHighLevelClient localElasticClient() {
        SSLContextBuilder sslBuilder = null;
        SSLContext sslContext = null;
        try {
            sslBuilder = SSLContexts.custom()
                    .loadTrustMaterial(null, (x509Certificates, s) -> true);
            sslContext = sslBuilder.build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            System.out.println(e.getMessage());
        }

        ClientConfiguration clientConfiguration = null;
        if(localEsSecure.equals("true")) {
            // 클러스터 주소를 제공하기 위해 builder를 사용한다. 디폴트 HttpHeaders나 사용가능한 SSL로 셋한다.
            clientConfiguration = ClientConfiguration.builder()
                    .connectedTo(getHostStrArr(localEsHosts, localEsPort))
                    .usingSsl(sslContext, NoopHostnameVerifier.INSTANCE)
                    .withBasicAuth(localEsUsername, localEsPassword)
                    .withSocketTimeout(1000000)
                    .build();
        } else {
            clientConfiguration = ClientConfiguration.builder()
                    .connectedTo(getHostStrArr(localEsHosts, localEsPort))
                    .withSocketTimeout(1000000)
                    .build();
        }
        // RestHighLevelClient를 만든다.
        return RestClients.create(clientConfiguration).rest();
    }

    @Bean
    public ElasticsearchOperations elasticsearchOperations() {
        return new ElasticsearchRestTemplate(localElasticClient());
    }
}
