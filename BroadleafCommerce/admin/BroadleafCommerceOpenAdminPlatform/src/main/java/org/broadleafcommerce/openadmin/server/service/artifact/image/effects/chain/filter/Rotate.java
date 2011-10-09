package org.broadleafcommerce.openadmin.server.service.artifact.image.effects.chain.filter;

import org.broadleafcommerce.openadmin.server.service.artifact.image.Operation;
import org.broadleafcommerce.openadmin.server.service.artifact.image.effects.chain.UnmarshalledParameter;
import org.broadleafcommerce.openadmin.server.service.artifact.image.effects.chain.conversion.ParameterTypeEnum;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.InputStream;
import java.util.Map;

public class Rotate extends BaseFilter {

	private RenderingHints hints;
	private double amount;

    public Rotate() {
        //do nothing
    }

	public Rotate(double amount, RenderingHints hints) {
		this.hints = hints;
		this.amount = amount;
	}

    @Override
    public Operation buildOperation(Map<String, String[]> parameterMap, InputStream artifactStream, String mimeType) {
        String key = FilterTypeEnum.ROTATE.toString().toLowerCase();
        if (parameterMap.containsKey("filterType") && key.equals(parameterMap.get("filterType")[0])) {
            Operation operation = new Operation();
            operation.setName(key);
            String[] factor = parameterMap.get(key + "-factor");
            operation.setFactor(factor==null?null:Double.valueOf(factor[0]));

            UnmarshalledParameter rotate = new UnmarshalledParameter();
            String[] rotateApplyFactor = parameterMap.get(key + "-rotate-apply-factor");
            rotate.setApplyFactor(rotateApplyFactor == null ? false : Boolean.valueOf(rotateApplyFactor[0]));
            rotate.setName("rotate");
            rotate.setType(ParameterTypeEnum.DOUBLE.toString());
            rotate.setValue(parameterMap.get(key + "-rotate-amount")[0]);

            operation.setParameters(new UnmarshalledParameter[]{rotate});
            return operation;
        }

        return null;
    }

	/* (non-Javadoc)
	 * @see java.awt.image.BufferedImageOp#filter(java.awt.image.BufferedImage, java.awt.image.BufferedImage)
	 */
	public BufferedImage filter(BufferedImage src, BufferedImage dst) {
		if (src == null) {
            throw new NullPointerException("src image is null");
        }
        if (src == dst) {
            throw new IllegalArgumentException("src image cannot be the "+
                                               "same as the dst image");
        }
        
        boolean needToConvert = false;
        ColorModel srcCM = src.getColorModel();
        ColorModel dstCM;
        BufferedImage origDst = dst;
        
        if (srcCM instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel) srcCM;
            src = icm.convertToIntDiscrete(src.getRaster(), false);
            srcCM = src.getColorModel();
        }
        
        int neww;
        int newh;
        int w = src.getWidth();
        int h = src.getHeight();
        if (dst == null) {
        	 double sin = Math.abs(Math.sin(Math.toRadians(amount))), cos = Math.abs(Math.cos(Math.toRadians(amount)));
             neww = (int)Math.floor(w*cos+h*sin);
             newh = (int)Math.floor(h*cos+w*sin);
             dst = createCompatibleDestImage(src, null, neww, newh);
            dstCM = srcCM;
            origDst = dst;
        } else {
            dstCM = dst.getColorModel();
            if (srcCM.getColorSpace().getType() !=
                dstCM.getColorSpace().getType())
            {
                needToConvert = true;
                dst = createCompatibleDestImage(src, null);
                dstCM = dst.getColorModel();
            }
            else if (dstCM instanceof IndexColorModel) {
                dst = createCompatibleDestImage(src, null);
                dstCM = dst.getColorModel();
            }
            neww = dst.getWidth();
            newh = dst.getHeight();
        }
        
        Graphics2D g = dst.createGraphics();
        g.translate((neww-w)/2, (newh-h)/2);
        g.rotate(Math.toRadians(amount), w/2, h/2);
        g.drawRenderedImage(src, null);
        g.dispose();
        origDst = dst;

	    if (needToConvert) {
            ColorConvertOp ccop = new ColorConvertOp(hints);
            ccop.filter(dst, origDst);
        }
        else if (origDst != dst) {
            Graphics2D g2 = origDst.createGraphics();
	    try {
            g2.drawImage(dst, 0, 0, null);
	    } finally {
	        g2.dispose();
	    }
        }

        return origDst;
	}

}
