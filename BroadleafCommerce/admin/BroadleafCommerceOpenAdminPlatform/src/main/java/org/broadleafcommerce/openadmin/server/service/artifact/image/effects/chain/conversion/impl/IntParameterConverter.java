package org.broadleafcommerce.openadmin.server.service.artifact.image.effects.chain.conversion.impl;

import org.broadleafcommerce.openadmin.server.service.artifact.image.effects.chain.conversion.ConversionException;
import org.broadleafcommerce.openadmin.server.service.artifact.image.effects.chain.conversion.Parameter;
import org.broadleafcommerce.openadmin.server.service.artifact.image.effects.chain.conversion.ParameterConverter;

public class IntParameterConverter implements ParameterConverter {

	/* (non-Javadoc)
	 * @see com.xpressdocs.email.asset.effects.chain.conversion.ParameterConverter#convert(java.lang.String, double)
	 */
	public Parameter convert(String value, Double factor, boolean applyFactor) throws ConversionException {
		Parameter param = new Parameter();
		param.setParameterClass(int.class);
		param.setParameterInstance((int) (applyFactor&&factor!=null?Integer.parseInt(value)/factor:Integer.parseInt(value)));
		
		return param;
	}

}
