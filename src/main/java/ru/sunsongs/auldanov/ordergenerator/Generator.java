package ru.sunsongs.auldanov.ordergenerator;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

import static java.util.Objects.requireNonNull;

/**
 * @author kraken
 * @since 6/3/16.
 */
@Component
public class Generator {
    private static final Logger LOG = LogManager.getLogger(Generator.class);
    private static final Random random = new Random(System.currentTimeMillis());
    private final URI sellApiUri;
    private final URI buyApiUri;
    private final String quantityParam = "quantity";
    private final String priceParam = "price";

    @Autowired
    public Generator(@Value("${api.sellUrl}") String sellUrl,
                     @Value("${api.buyUrl}") String buyUrl) throws URISyntaxException {
        this.sellApiUri = new URI(requireNonNull(sellUrl));
        this.buyApiUri = new URI(requireNonNull(buyUrl));
    }

    /**
     * Sends request to REST API service to add sell and buy orders
     */
    @Scheduled(fixedRateString = "${schedule.time}")
    public void reportCurrentTime() {
        try (CloseableHttpClient httpclient = HttpClients.custom().build()) {
            final URI uri = getRandomApiUri();
            HttpUriRequest sendOrder = RequestBuilder.post()
                    .setUri(uri)
                    .addParameter(quantityParam, String.valueOf(random.nextInt(1000)))
                    .addParameter(priceParam, String.valueOf(random.nextInt(1000)))
                    .build();
            try (CloseableHttpResponse response2 = httpclient.execute(sendOrder)) {
                LOG.info("Order sent. Http response code: " + response2.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            LOG.error("Error on send order", e);
        }
    }

    private URI getRandomApiUri() {
        return random.nextBoolean() ? sellApiUri : buyApiUri;
    }
}