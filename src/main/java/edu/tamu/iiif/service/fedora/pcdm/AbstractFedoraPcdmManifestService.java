package edu.tamu.iiif.service.fedora.pcdm;

import static edu.tamu.iiif.constants.Constants.CANVAS_IDENTIFIER;
import static edu.tamu.iiif.constants.Constants.COLLECTION_IDENTIFIER;
import static edu.tamu.iiif.constants.Constants.DUBLIN_CORE_DESCRIPTION_PREDICATE;
import static edu.tamu.iiif.constants.Constants.DUBLIN_CORE_IDENTIFIER_PREDICATE;
import static edu.tamu.iiif.constants.Constants.DUBLIN_CORE_TITLE_PREDICATE;
import static edu.tamu.iiif.constants.Constants.FEDORA_FCR_METADATA;
import static edu.tamu.iiif.constants.Constants.FEDORA_PCDM_CONDITION;
import static edu.tamu.iiif.constants.Constants.IANA_FIRST_PREDICATE;
import static edu.tamu.iiif.constants.Constants.IANA_LAST_PREDICATE;
import static edu.tamu.iiif.constants.Constants.IANA_NEXT_PREDICATE;
import static edu.tamu.iiif.constants.Constants.LDP_CONTAINS_PREDICATE;
import static edu.tamu.iiif.constants.Constants.LDP_HAS_MEMBER_RELATION_PREDICATE;
import static edu.tamu.iiif.constants.Constants.ORE_PROXY_FOR_PREDICATE;
import static edu.tamu.iiif.constants.Constants.PCDM_COLLECTION;
import static edu.tamu.iiif.constants.Constants.PCDM_FILE;
import static edu.tamu.iiif.constants.Constants.PCDM_HAS_FILE_PREDICATE;
import static edu.tamu.iiif.constants.Constants.PCDM_HAS_MEMBER_PREDICATE;
import static edu.tamu.iiif.constants.Constants.PRESENTATION_IDENTIFIER;
import static edu.tamu.iiif.constants.Constants.RDFS_LABEL_PREDICATE;
import static edu.tamu.iiif.constants.Constants.RDF_TYPE_PREDICATE;
import static edu.tamu.iiif.constants.Constants.SEQUENCE_IDENTIFIER;
import static edu.tamu.iiif.utility.RdfModelUtility.getIdByPredicate;
import static edu.tamu.iiif.utility.RdfModelUtility.getObject;
import static edu.tamu.iiif.utility.StringUtility.joinPath;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.digitalcollections.iiif.presentation.model.api.v2.Canvas;
import de.digitalcollections.iiif.presentation.model.api.v2.Image;
import de.digitalcollections.iiif.presentation.model.api.v2.ImageResource;
import de.digitalcollections.iiif.presentation.model.api.v2.Sequence;
import de.digitalcollections.iiif.presentation.model.impl.v2.CanvasImpl;
import de.digitalcollections.iiif.presentation.model.impl.v2.PropertyValueSimpleImpl;
import de.digitalcollections.iiif.presentation.model.impl.v2.SequenceImpl;
import edu.tamu.iiif.controller.ManifestRequest;
import edu.tamu.iiif.exception.NotFoundException;
import edu.tamu.iiif.model.rdf.RdfCanvas;
import edu.tamu.iiif.model.rdf.RdfOrderedResource;
import edu.tamu.iiif.model.rdf.RdfResource;
import edu.tamu.iiif.service.AbstractManifestService;

@ConditionalOnExpression(FEDORA_PCDM_CONDITION)
public abstract class AbstractFedoraPcdmManifestService extends AbstractManifestService {

    @Value("${iiif.fedora.url}")
    protected String fedoraUrl;

    @Value("${iiif.fedora.identifier.fedora-pcdm}")
    protected String fedoraPcdmIdentifier;

    protected Sequence generateSequence(ManifestRequest request, RdfResource rdfResource) throws IOException, URISyntaxException {
        String id = rdfResource.getResource().getURI();
        PropertyValueSimpleImpl label = getLabel(rdfResource);
        Sequence sequence = new SequenceImpl(getFedoraIiifSequenceUri(id), label);
        sequence.setCanvases(getCanvases(request, rdfResource));
        return sequence;
    }

    protected Canvas generateCanvas(ManifestRequest request, RdfResource rdfResource) throws IOException, URISyntaxException {
        String id = rdfResource.getResource().getURI();
        PropertyValueSimpleImpl label = getLabel(rdfResource);

        RdfCanvas rdfCanvas = getFedoraRdfCanvas(request, rdfResource);

        Canvas canvas = new CanvasImpl(getFedoraIiifCanvasUri(id), label, rdfCanvas.getHeight(), rdfCanvas.getWidth());

        canvas.setImages(rdfCanvas.getImages());

        canvas.setMetadata(getDublinCoreMetadata(rdfResource));

        return canvas;
    }

    protected PropertyValueSimpleImpl getLabel(RdfResource rdfResource) {
        Optional<String> title = getObject(rdfResource, RDFS_LABEL_PREDICATE);
        if (!title.isPresent()) {
            title = getObject(rdfResource, DUBLIN_CORE_IDENTIFIER_PREDICATE);
        }
        if (!title.isPresent()) {
            title = getObject(rdfResource, DUBLIN_CORE_TITLE_PREDICATE);
        }
        if (!title.isPresent()) {
            String id = rdfResource.getResource().getURI();
            title = Optional.of(getRepositoryContextIdentifier(id));
        }
        return new PropertyValueSimpleImpl(title.get());
    }

    protected PropertyValueSimpleImpl getDescription(RdfResource rdfResource) {
        Optional<String> description = getObject(rdfResource, DUBLIN_CORE_DESCRIPTION_PREDICATE);
        if (!description.isPresent()) {
            description = Optional.of("No description available!");
        }
        return new PropertyValueSimpleImpl(description.get());
    }

    protected URI getFedoraIiifCollectionUri(String url) throws URISyntaxException {
        return getFedoraIiifUri(url, COLLECTION_IDENTIFIER);
    }

    protected URI getFedoraIiifPresentationUri(String url) throws URISyntaxException {
        return getFedoraIiifUri(url, PRESENTATION_IDENTIFIER);
    }

    protected URI getFedoraIiifSequenceUri(String url) throws URISyntaxException {
        return getFedoraIiifUri(url, SEQUENCE_IDENTIFIER);
    }

    protected URI getFedoraIiifCanvasUri(String url) throws URISyntaxException {
        return getFedoraIiifUri(url, CANVAS_IDENTIFIER);
    }

    protected URI getCanvasUri(String canvasId) throws URISyntaxException {
        return getFedoraIiifCanvasUri(canvasId);
    }

    protected Model getFedoraRdfModel(String url) throws NotFoundException {
        return getRdfModel(url + FEDORA_FCR_METADATA);
    }

    protected boolean isCollection(RdfResource rdfResource) {
        NodeIterator nodes = rdfResource.getNodesOfPropertyWithId(RDF_TYPE_PREDICATE);
        while (nodes.hasNext()) {
            RDFNode node = nodes.next();
            if (node.toString().equals(PCDM_COLLECTION)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isCollection(Model model) {
        NodeIterator nodes = model.listObjectsOfProperty(model.getProperty(RDF_TYPE_PREDICATE));
        while (nodes.hasNext()) {
            RDFNode node = nodes.next();
            if (node.toString().equals(PCDM_COLLECTION)) {
                return true;
            }
        }
        return false;
    }

    protected String getCollectionObjectsMember(RdfResource rdfResource) {
        NodeIterator nodes = rdfResource.getNodesOfPropertyWithId(PCDM_HAS_MEMBER_PREDICATE);
        if (nodes.hasNext()) {
            RDFNode node = nodes.next();
            return node.toString();
        }
        throw new RuntimeException("Collection does not contain its expected member!");
    }

    protected String getIiifImageServiceName() {
        return "Fedora IIIF Image Resource Service";
    }

    @Override
    protected String getMatcherHandle(String uri) {
        return uri;
    }

    @Override
    protected String getIiifServiceUrl() {
        return iiifServiceUrl + "/" + fedoraPcdmIdentifier;
    }

    @Override
    protected String getRepositoryContextIdentifier(String url) {
        return fedoraPcdmIdentifier + ":" + getRepositoryPath(url);
    }

    @Override
    protected String getRepositoryPath(String url) {
        return url.substring(fedoraUrl.length() + 1);
    }

    @Override
    protected String getRepositoryType() {
        return fedoraPcdmIdentifier;
    }

    @Override
    protected String getRdfUrl(String path) {
        return joinPath(fedoraUrl, path);
    }

    // TODO: update to match getDSpaceIiifUrl
    private URI getFedoraIiifUri(String url, String type) throws URISyntaxException {
        return URI.create(url.replace(fedoraUrl + "/", getIiifServiceUrl() + "/" + type + "/"));
    }

    private List<Canvas> getCanvases(ManifestRequest request, RdfResource rdfResource) throws IOException, URISyntaxException {
        List<Canvas> canvases = new ArrayList<Canvas>();

        Optional<String> firstId = getIdByPredicate(rdfResource.getModel(), IANA_FIRST_PREDICATE);

        if (firstId.isPresent()) {
            Optional<String> lastId = getIdByPredicate(rdfResource.getModel(), IANA_LAST_PREDICATE);

            if (lastId.isPresent()) {
                Resource firstResource = rdfResource.getModel().getResource(firstId.get());
                generateOrderedCanvases(request, new RdfOrderedResource(rdfResource.getModel(), firstResource, firstId.get(), lastId.get()), canvases);
            }
        }

        if (canvases.isEmpty()) {

            ResIterator resItr = rdfResource.listResourcesWithPropertyWithId(LDP_CONTAINS_PREDICATE);
            while (resItr.hasNext()) {
                Resource resource = resItr.next();
                if (resource.getProperty(rdfResource.getProperty(PCDM_HAS_FILE_PREDICATE)) != null) {
                    Canvas canvas = generateCanvas(request, new RdfResource(rdfResource, resource));
                    if (canvas.getImages().size() > 0) {
                        canvases.add(canvas);
                    }
                }
            }

        }

        return canvases;
    }

    private void generateOrderedCanvases(ManifestRequest request, RdfOrderedResource rdfOrderedSequence, List<Canvas> canvases) throws IOException, URISyntaxException {

        Model model = getFedoraRdfModel(rdfOrderedSequence.getResource().getURI());

        Optional<String> id = getIdByPredicate(model, ORE_PROXY_FOR_PREDICATE);

        if (!id.isPresent()) {
            id = getIdByPredicate(model, ORE_PROXY_FOR_PREDICATE.replace("#", "/"));
        }

        if (id.isPresent()) {

            Model orderedModel = getFedoraRdfModel(id.get());

            Canvas canvas = generateCanvas(request, new RdfResource(orderedModel, id.get()));
            if (canvas.getImages().size() > 0) {
                canvases.add(canvas);
            }

            Optional<String> nextId = getIdByPredicate(model, IANA_NEXT_PREDICATE);

            if (nextId.isPresent()) {
                Resource resource = rdfOrderedSequence.getModel().getResource(nextId.get());
                rdfOrderedSequence.setResource(resource);
                rdfOrderedSequence.setCurrentId(nextId.get());
                generateOrderedCanvases(request, rdfOrderedSequence, canvases);
            }
        }

    }

    private RdfCanvas getFedoraRdfCanvas(ManifestRequest request, RdfResource rdfResource) throws URISyntaxException, JsonProcessingException, MalformedURLException, IOException {
        RdfCanvas rdfCanvas = new RdfCanvas();

        String canvasId = rdfResource.getResource().getURI();

        Statement canvasStatement = rdfResource.getStatementOfPropertyWithId(LDP_CONTAINS_PREDICATE);

        String parentId = canvasStatement.getObject().toString();

        Model parentModel = getFedoraRdfModel(parentId);

        RdfResource parentRdfResource = new RdfResource(parentModel, parentId);

        if (parentRdfResource.getStatementOfPropertyWithId(LDP_HAS_MEMBER_RELATION_PREDICATE).getResource().toString().equals(PCDM_HAS_FILE_PREDICATE)) {
            NodeIterator nodeItr = parentRdfResource.getNodesOfPropertyWithId(LDP_CONTAINS_PREDICATE);
            while (nodeItr.hasNext()) {
                RDFNode node = nodeItr.next();

                Model fileModel = getFedoraRdfModel(node.toString());

                RdfResource fileRdfResource = new RdfResource(fileModel, node.toString());

                if (fileRdfResource.getStatementOfPropertyWithId(RDF_TYPE_PREDICATE).getResource().toString().equals(PCDM_FILE)) {
                    Optional<Image> image = generateImage(request, fileRdfResource, canvasId);
                    if (image.isPresent()) {
                        rdfCanvas.addImage(image.get());

                        Optional<ImageResource> imageResource = Optional.ofNullable(image.get().getResource());

                        if (imageResource.isPresent()) {
                            int height = imageResource.get().getHeight();
                            if (height > rdfCanvas.getHeight()) {
                                rdfCanvas.setHeight(height);
                            }

                            int width = imageResource.get().getWidth();
                            if (width > rdfCanvas.getWidth()) {
                                rdfCanvas.setWidth(width);
                            }
                        }
                    }
                }

            }
        }
        return rdfCanvas;
    }

}
