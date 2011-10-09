package org.broadleafcommerce.openadmin.server.service.artifact.upload;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.openadmin.client.dto.*;
import org.broadleafcommerce.openadmin.client.service.DynamicEntityService;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.web.bind.ServletRequestParameterPropertyValues;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jfischer
 */
public class UploadAddOrUpdateController extends SimpleFormController {

    private static final Log LOG = LogFactory.getLog(UploadAddOrUpdateController.class);

    protected DynamicEntityService dynamicEntityRemoteService;

    protected Long maximumFileSizeInBytes = 20L * 1000L * 1000L; // 20mb max by default

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> model = new HashMap<String, String>();
        String callbackName = null;
        try {
            MutablePropertyValues mpvs = new ServletRequestParameterPropertyValues(request);
            if (request instanceof MultipartRequest) {
                MultipartRequest multipartRequest = (MultipartRequest) request;
                bindMultipart(multipartRequest.getMultiFileMap(), mpvs);
            }

            PersistencePackage persistencePackage = new PersistencePackage();
            persistencePackage.setPersistencePerspective(new PersistencePerspective());
            String ceilingEntity = (String) mpvs.getPropertyValue("ceilingEntityFullyQualifiedClassname").getValue();
            callbackName = (String) mpvs.getPropertyValue("callbackName").getValue();
            String operation = (String) mpvs.getPropertyValue("operation").getValue();
            String customCriteria = (String) mpvs.getPropertyValue("customCriteria").getValue();
            mpvs.removePropertyValue("ceilingEntityFullyQualifiedClassname");
            mpvs.removePropertyValue("sandbox");
            mpvs.removePropertyValue("callbackName");
            mpvs.removePropertyValue("operation");
            mpvs.removePropertyValue("customCriteria");
            persistencePackage.setCeilingEntityFullyQualifiedClassname(ceilingEntity);
            persistencePackage.setCustomCriteria(new String[]{customCriteria});
            Entity entity = new Entity();
            persistencePackage.setEntity(entity);
            entity.setType(new String[]{ceilingEntity});
            List<Property> propertyList = new ArrayList<Property>();
            for (PropertyValue propertyValue : mpvs.getPropertyValues()) {
                if (propertyValue.getValue() instanceof MultipartFile) {
                    MultipartFile file = (MultipartFile) propertyValue.getValue();
                    if (file.getSize() > maximumFileSizeInBytes) {
                        throw new MaxUploadSizeExceededException(maximumFileSizeInBytes);
                    }
                    if (file.getOriginalFilename() == null || file.getOriginalFilename().indexOf(".") < 0) {
                        throw new FileExtensionUnavailableException("Unable to determine file extension for uploaded file. The filename for the uploaded file is: " + file.getOriginalFilename());
                    }
                    Map<String, MultipartFile> fileMap = UploadedFile.getUpload();
                    fileMap.put(propertyValue.getName(), (MultipartFile) propertyValue.getValue());
                    UploadedFile.setUpload(fileMap);
                    entity.setMultiPartAvailableOnThread(true);
                } else {
                    Property property = new Property();
                    property.setName(propertyValue.getName());
                    property.setValue((String) propertyValue.getValue());
                    propertyList.add(property);
                }
            }
            entity.setProperties(propertyList.toArray(new Property[]{}));

            Entity result = null;

            if (operation.equals("add")) {
                result = dynamicEntityRemoteService.add(persistencePackage);
            } else if (operation.equals("update")) {
                result = dynamicEntityRemoteService.update(persistencePackage);
            }

            model.put("callbackName", callbackName);
            model.put("result", buildJSON(result));

            return new ModelAndView("blUploadCompletedView", model);
        } catch (MaxUploadSizeExceededException e) {
            if (callbackName != null) {
                model.put("callbackName", callbackName);
                model.put("error", buildErrorJSON(e.getMessage()));
            }

            return new ModelAndView("blUploadCompletedView", model);
        } catch (FileExtensionUnavailableException e) {
            if (callbackName != null) {
                model.put("callbackName", callbackName);
                model.put("error", buildErrorJSON(e.getMessage()));
            }

            return new ModelAndView("blUploadCompletedView", model);
        } catch (Exception e) {
            e.printStackTrace();
            if (callbackName != null) {
                model.put("callbackName", callbackName);
                model.put("error", buildErrorJSON(e.getMessage()));
            }

            return new ModelAndView("blUploadCompletedView", model);
        } finally {
            UploadedFile.remove();
        }
    }

    protected String buildJSON(Entity entity) {
        StringBuffer sb = new StringBuffer();
        sb.append("{\"type\" : \"");
        sb.append(entity.getType()[0]);
        sb.append("\", \"properties\" : [");
        for (int j=0;j<entity.getProperties().length;j++) {
            sb.append("{\"name\" : \"");
            sb.append(entity.getProperties()[j].getName());
            sb.append("\", \"value\" : \"");
            sb.append(entity.getProperties()[j].getValue());
            sb.append("\"}");
            if (j<entity.getProperties().length - 1) {
                sb.append(",");
            }
         }
        sb.append("]}");

        return sb.toString();
    }

    protected String buildErrorJSON(String errorString) {
        StringBuffer sb = new StringBuffer();
        sb.append("{\"error\" : \"");
        sb.append(errorString);
        sb.append("\"}");

        return sb.toString();
    }

    protected void bindMultipart(Map<String, List<MultipartFile>> multipartFiles, MutablePropertyValues mpvs) {
		for (Map.Entry<String, List<MultipartFile>> entry : multipartFiles.entrySet()) {
			String key = entry.getKey();
			List<MultipartFile> values = entry.getValue();
			if (values.size() == 1) {
				MultipartFile value = values.get(0);
				mpvs.add(key, value);
			} else {
				mpvs.add(key, values);
			}
		}
	}

    public DynamicEntityService getDynamicEntityRemoteService() {
        return dynamicEntityRemoteService;
    }

    public void setDynamicEntityRemoteService(DynamicEntityService dynamicEntityRemoteService) {
        this.dynamicEntityRemoteService = dynamicEntityRemoteService;
    }

    public Long getMaximumFileSizeInBytes() {
        return maximumFileSizeInBytes;
    }

    public void setMaximumFileSizeInBytes(Long maximumFileSizeInBytes) {
        this.maximumFileSizeInBytes = maximumFileSizeInBytes;
    }
}
