/*
 * #%L
 * BroadleafCommerce Framework
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License” located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License” located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.core.search.service.solr;

import org.broadleafcommerce.common.exception.ExceptionHelper;
import org.broadleafcommerce.core.search.service.SearchService;
import org.broadleafcommerce.core.search.service.solr.index.IndexStatusInfo;
import org.broadleafcommerce.core.search.service.solr.index.SolrIndexStatusProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * @author Jeff Fischer
 */
public class FileSystemSolrIndexStatusProviderImpl implements SolrIndexStatusProvider {

    @Qualifier("blCatalogSolrConfiguration")
    @Autowired(required = false)
    protected SolrConfiguration solrConfiguration;

    @Resource(name="blSearchService")
    protected SearchService searchService;

    protected DocumentBuilder builder;

    protected XPath xPath;

    protected SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    public FileSystemSolrIndexStatusProviderImpl() {
        XPathFactory factory=XPathFactory.newInstance();
        xPath=factory.newXPath();
    }

    @Override
    public synchronized void handleUpdateIndexStatus(IndexStatusInfo status) {
        try {
            if (searchService instanceof SolrSearchServiceImpl) {
                File statusFile = getStatusFile((SolrSearchServiceImpl) searchService);
                boolean exists = statusFile.exists();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setIgnoringElementContentWhitespace(true);
                dbf.setNamespaceAware(true);
                if (builder == null) {
                    builder = dbf.newDocumentBuilder();
                }
                Document document;
                Element indexElement;
                if (exists) {
                    document = builder.parse(statusFile);
                    NodeList temp1 = (NodeList) xPath.evaluate("/status/index", document, XPathConstants.NODESET);
                    indexElement = (Element) temp1.item(0);
                } else {
                    document = builder.newDocument();
                    Element root = document.createElement("status");
                    document.appendChild(root);
                    indexElement = document.createElement("index");
                    indexElement.setAttribute("dateProcessed", "");
                    root.appendChild(indexElement);
                }
                String dateString = format.format(status.getLastIndexDate());
                if (!dateString.equals(indexElement.getAttribute("dateProcessed"))) {
                    indexElement.setAttribute("dateProcessed", dateString);
                    NodeList children = (NodeList) xPath.evaluate("info", indexElement, XPathConstants.NODESET);
                    for (int j = 0; j < children.getLength(); j++) {
                        indexElement.removeChild(children.item(j));
                    }
                    children = indexElement.getChildNodes();
                    for (int j = 0; j < children.getLength(); j++) {
                        if (children.item(j).getNodeName().equalsIgnoreCase("#text")) {
                            indexElement.removeChild(children.item(j));
                        }
                    }
                }
                for (Map.Entry<String, String> entry : status.getAdditionalInfo().entrySet()) {
                    NodeList infos = (NodeList) xPath.evaluate("info[@key='" + entry.getKey() + "']", indexElement, XPathConstants.NODESET);
                    if (infos.getLength() == 0) {
                        Element addlInfo = document.createElement("info");
                        addlInfo.setAttribute("key", entry.getKey());
                        addlInfo.setAttribute("val", entry.getValue());
                        indexElement.appendChild(addlInfo);
                    }
                }

                TransformerFactory tFactory = TransformerFactory.newInstance();
                Transformer xmlTransformer = tFactory.newTransformer();
                xmlTransformer.setOutputProperty(OutputKeys.VERSION, "1.0");
                xmlTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                xmlTransformer.setOutputProperty(OutputKeys.METHOD, "xml");
                xmlTransformer.setOutputProperty(OutputKeys.INDENT, "yes");

                DOMSource source = new DOMSource(document);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(statusFile, false), "UTF-8"));
                StreamResult result = new StreamResult(writer);
                xmlTransformer.transform(source, result);
            }
        } catch (ParserConfigurationException e) {
            throw ExceptionHelper.refineException(e);
        } catch (SAXException e) {
            throw ExceptionHelper.refineException(e);
        } catch (IOException e) {
            throw ExceptionHelper.refineException(e);
        } catch (XPathExpressionException e) {
            throw ExceptionHelper.refineException(e);
        } catch (TransformerException e) {
            throw ExceptionHelper.refineException(e);
        }
    }

    @Override
    public synchronized IndexStatusInfo readIndexStatus(IndexStatusInfo status) {
        try {
            if (searchService instanceof SolrSearchServiceImpl) {
                File statusFile = getStatusFile((SolrSearchServiceImpl) searchService);
                boolean exists = statusFile.exists();
                if (exists) {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(true);
                    if (builder == null) {
                        builder = dbf.newDocumentBuilder();
                    }
                    Document document = builder.parse(statusFile);
                    NodeList temp1 = (NodeList) xPath.evaluate("/status/index", document, XPathConstants.NODESET);
                    Element indexElement = (Element) temp1.item(0);
                    status.setLastIndexDate(format.parse(indexElement.getAttribute("dateProcessed")));
                    NodeList infos = (NodeList) xPath.evaluate("info", indexElement, XPathConstants.NODESET);
                    for (int j = 0; j < infos.getLength(); j++) {
                        Element info = (Element) infos.item(j);
                        status.getAdditionalInfo().put(info.getAttribute("key"), info.getAttribute("val"));
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            throw ExceptionHelper.refineException(e);
        } catch (SAXException e) {
            throw ExceptionHelper.refineException(e);
        } catch (IOException e) {
            throw ExceptionHelper.refineException(e);
        } catch (XPathExpressionException e) {
            throw ExceptionHelper.refineException(e);
        } catch (ParseException e) {
            throw ExceptionHelper.refineException(e);
        }
        return status;
    }
    
    protected File getStatusFile(SolrSearchServiceImpl searchService) {
        String statusDirectory = getStatusDirectory(searchService);
        File statusFile = new File(new File(statusDirectory), "solr_status.xml");
        return statusFile;
    }

    protected String getStatusDirectory(SolrSearchServiceImpl searchService) {
        String solrHome = solrConfiguration.getSolrHomePath();
        if (solrHome == null) {
            return System.getProperty("java.io.tmpdir");
        }
        return solrHome;
    }
    
}
