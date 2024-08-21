package edu.tamu.iiif.service.fedora.pcdm;

import static edu.tamu.iiif.model.ManifestType.IMAGE;
import static edu.tamu.iiif.utility.StringUtility.joinPath;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.stereotype.Service;

import edu.tamu.iiif.controller.ManifestRequest;
import edu.tamu.iiif.model.ManifestType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class FedoraPcdmImageManifestService extends AbstractFedoraPcdmManifestService {

    private final static Logger logger = LoggerFactory.getLogger(FedoraPcdmImageManifestService.class);

    public String generateManifest(ManifestRequest request) throws IOException, URISyntaxException {
        logger.debug("request"  + request.toString());
        String context = request.getContext();
        logger.debug("CONTEXT " + context);
        String fedoraPath = joinPath(config.getUrl(), context);
        URI uri = getImageUri(fedoraPath);
        logger.debug("URI " + uri.toString());
        return fetchImageInfo(uri.toString());
    }

    @Override
    public ManifestType getManifestType() {
        return IMAGE;
    }

}
