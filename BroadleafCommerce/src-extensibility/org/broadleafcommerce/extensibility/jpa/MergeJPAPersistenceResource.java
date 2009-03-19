package org.broadleafcommerce.extensibility.jpa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.extensibility.MergeXmlConfigResource;
import org.broadleafcommerce.extensibility.context.merge.exceptions.MergeException;
import org.broadleafcommerce.extensibility.context.merge.exceptions.MergeManagerSetupException;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.xml.SimpleSaxErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

public class MergeJPAPersistenceResource extends MergeXmlConfigResource {

	private static final Log LOG = LogFactory.getLog(MergeJPAPersistenceResource.class);
	private ErrorHandler handler = new SimpleSaxErrorHandler(LOG);
	
	public Resource getMergedConfigResource(InputStream[] sources) throws BeansException {
		Resource configResource = null;
		InputStream merged = null;
		try {
			List<String> mappingFiles = new ArrayList<String>();
			InputStream[] inMemoryStreams = new InputStream[sources.length];
			for (int j=0;j<sources.length;j++){
				byte[] sourceArray = buildArrayFromStream(sources[j]);
				compileMappingFiles(mappingFiles, sourceArray);
				inMemoryStreams[j] = new ByteArrayInputStream(sourceArray);
			}
			
			merged = merge(inMemoryStreams);
			
			//read the final stream into a byte array
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			boolean eof = false;
			while (!eof) {
				int temp = merged.read();
				if (temp == -1) {
					eof = true;
				} else {
					baos.write(temp);
				}
			}
			configResource = new ByteArrayResource(baos.toByteArray());
			
			if (LOG.isDebugEnabled()) {
				LOG.debug("Merged config: \n" + serialize(configResource));
			}
		} catch (MergeException e) {
			throw new FatalBeanException("Unable to merge source and patch locations", e);
		} catch (MergeManagerSetupException e) {
			throw new FatalBeanException("Unable to merge source and patch locations", e);
		} catch (IOException e) {
			throw new FatalBeanException("Unable to merge source and patch locations", e);
		} catch (SAXException e) {
			throw new FatalBeanException("Unable to merge source and patch locations", e);
		} catch (ParserConfigurationException e) {
			throw new FatalBeanException("Unable to merge source and patch locations", e);
		} finally {
			if (merged != null) {
				try{ merged.close(); } catch (Throwable e) {}
			}
		}
		
		return configResource;
	}
	
	private void compileMappingFiles(List<String> mappingFiles, byte[] sourceArray) throws IOException, ParserConfigurationException, SAXException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder parser = dbf.newDocumentBuilder();
		parser.setErrorHandler(handler);

		Document dom = parser.parse(new ByteArrayInputStream(sourceArray));
		
		NodeList nodes = dom.getElementsByTagName("/persistence/persistence-unit/mapping-file");
		if (nodes != null && nodes.getLength() > 0) {
			int length = nodes.getLength();
			for (int j=0;j<length;j++){
				Node node = nodes.item(j);
				mappingFiles.add(node.getNodeValue());
			}
		}
	}
}
