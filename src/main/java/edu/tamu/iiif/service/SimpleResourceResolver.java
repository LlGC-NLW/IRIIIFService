package edu.tamu.iiif.service;

import java.net.URL;
import java.net.URISyntaxException;
import java.net.MalformedURLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.RequestEntity.BodyBuilder;
import org.springframework.http.RequestEntity.HeadersBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import edu.tamu.iiif.config.model.ResolverConfig;
import edu.tamu.iiif.exception.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@ConditionalOnProperty(value = "iiif.resolver.type", havingValue = "simple", matchIfMissing = false)
public class SimpleResourceResolver implements ResourceResolver {

    @Autowired
    private ResolverConfig resolver;

    @Autowired
    private RestTemplate restTemplate;

    private final static Logger logger = LoggerFactory.getLogger(SimpleResourceResolver.class);

    public String lookup(String url) throws URISyntaxException, NotFoundException {
        logger.debug("SIMPLE LOOKUP" + url);
        String id;
        try {
            URL newUrl = new URL(url);
            String path = newUrl.getPath();
            path = StringUtils.removeEnd(path, "/JP2");
            String[] segments = path.split("/");
            id = segments[segments.length - 1];
            logger.debug("SIMPLE ID " + id);
        } catch (MalformedURLException e) {
            throw new URISyntaxException(url, String.format("Resource with url %s not found!", url));
        }
        return id;
    }

    public String create(String url) throws URISyntaxException {
        logger.debug("SIMPLE CREATE" + url);
        /*
        URIBuilder uriBuilder = new URIBuilder(resolver.getUrl());
        uriBuilder.addParameter("url", url);
        URI uri = uriBuilder.build();
        BodyBuilder bodyBuilder = RequestEntity.post(uri).accept(MediaType.TEXT_PLAIN);
        if (resolver.hasCredentials()) {
            bodyBuilder.header("Authorization", String.format("Basic %s", resolver.getBase64Credentials()));
        }
        RequestEntity<Void> request = bodyBuilder.build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);
        if (response.getStatusCode().equals(HttpStatus.CREATED) || response.getStatusCode().equals(HttpStatus.OK)) {
            return response.getBody();
        }
        throw new RuntimeException(String.format("Failed to create resource with url %s!", url));
        */
        return "TODO";
    }

    public String resolve(String id) throws NotFoundException {
        logger.debug("SIMPLE RESOLVE" + id);
        /*
        try {
            URIBuilder uriBuilder = new URIBuilder(StringUtils.removeEnd(resolver.getUrl(), "/") + "/" + id);
            URI uri = uriBuilder.build();
            RequestEntity<Void> request = RequestEntity.get(uri).accept(MediaType.TEXT_PLAIN).build();
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                return response.getBody();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        throw new NotFoundException(String.format("Resource with id %s not found!", id));
        */
        return "TODO";
    }

    public void remove(String id) throws NotFoundException {
        logger.debug("SIMPLE REMOVE" + id);
        /*
        try {
            URIBuilder uriBuilder = new URIBuilder(StringUtils.removeEnd(resolver.getUrl(), "/") + "/" + id);
            URI uri = uriBuilder.build();
            HeadersBuilder<?> headerBuilder = RequestEntity.delete(uri);
            if (resolver.hasCredentials()) {
                headerBuilder.header("Authorization", String.format("Basic %s", resolver.getBase64Credentials()));
            }
            RequestEntity<Void> request = headerBuilder.build();
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            if (response.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                throw new NotFoundException(String.format("Resource with id %s not found!", id));
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        */
    }

}
