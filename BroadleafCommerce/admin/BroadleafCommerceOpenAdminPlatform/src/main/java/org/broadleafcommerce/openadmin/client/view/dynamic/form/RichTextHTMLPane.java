package org.broadleafcommerce.openadmin.client.view.dynamic.form;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Element;
import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.form.DynamicForm;
import org.broadleafcommerce.openadmin.client.BLCMain;

public class RichTextHTMLPane extends HTMLPane {

    static {
        exposeGetHTMLDisabled();
        exposeGetHTMLValue();
    }

    private static int counter = 0;
    private static Map<String, Map<String, Object>> valueMap = new HashMap<String, Map<String, Object>>();

    private final int myId;
    private String editorPath;
    private DynamicForm form;

	public RichTextHTMLPane(String editorPath, DynamicForm form) {
		super();
        this.editorPath = editorPath;
        counter++;
        myId = counter;
        valueMap.put(String.valueOf(myId), new HashMap<String, Object>());
        valueMap.get(String.valueOf(myId)).put("disabled", false);
        valueMap.get(String.valueOf(myId)).put("form", form);
	}
	
    public void setValue(String value) {
        valueMap.get(String.valueOf(myId)).put("value", value);
        init();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (valueMap.size() > 0) {
            valueMap.clear();
        }
    }

    public String getValue()  {
    	Node node = findIFrame();
    	String response = findRichTextValue(node);
        setValue(response);

        return response;
    }
    
    public Node findIFrame() {
    	Element element = getDOM();
    	Node iframe =  findIFrame(element.getChildNodes());
    	return iframe;
    }
    
    public Node findIFrame(NodeList<Node> childNodes) {
    	for (int i = 0; i < childNodes.getLength(); i++) {
    		Node item = childNodes.getItem(i);
    		if (item instanceof Element && "IFRAME".equals(((Element) item).getTagName())) {
				return item;
			} else {
				Node childIFrame = findIFrame(item.getChildNodes());
				if (childIFrame != null) {
					return childIFrame;
				}
			}
    	}
    	return null;
    }

    public void init() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", String.valueOf(myId));
        setContentsURLParams(map);
        setContentsURL(BLCMain.webAppContext + editorPath);
    }

    public void setDisabled(Boolean disabled) {
        if (!valueMap.get(String.valueOf(myId)).get("disabled").equals(disabled.toString())) {
            valueMap.get(String.valueOf(myId)).put("disabled", disabled);
            init();
        }
    }

    public static String getHTMLValue(String id) {
        String response = (String) valueMap.get(id).get("value");
        return response==null?"":response;
    }

    public static boolean getHTMLDisabled(String id) {
        if (!((DynamicForm) valueMap.get(id).get("form")).getDisabled()) {
            return ((Boolean) valueMap.get(id).get("disabled"));
        }
        return true;
    }
   
	public static native String findRichTextValue(Node iframeNode) /*-{
		return iframeNode.contentWindow.tinyMCE.get('richTextContent').getContent();
	}-*/;

    private static native void exposeGetHTMLDisabled() /*-{
		$wnd.getHTMLDisabled = function(id) {
			return @org.broadleafcommerce.openadmin.client.view.dynamic.form.RichTextHTMLPane::getHTMLDisabled(Ljava/lang/String;)(id);
		}
	}-*/;

    private static native void exposeGetHTMLValue() /*-{
		$wnd.getHTMLValue = function(id) {
			return @org.broadleafcommerce.openadmin.client.view.dynamic.form.RichTextHTMLPane::getHTMLValue(Ljava/lang/String;)(id);
		}
	}-*/;
}
