package org.broadleafcommerce.extensibility.context.merge.handlers;

import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AttributeValueMerge extends BaseHandler {

	@Override
	public Node[] merge(NodeList nodeList1, NodeList nodeList2, Node[] exhaustedNodes) {
		Node node1 = nodeList1.item(0);
		Node node2 = nodeList2.item(0);
		String[] items1 = node1.getNodeValue().split("[\\s\\n\\r]+");
		String[] items2 = node2.getNodeValue().split("[\\s\\n\\r]+");
		ArrayList<String> finalItems = new ArrayList<String>();
		for (int j=0;j<items1.length;j++){
			finalItems.add(items1[j]);
		}
		for (int j=0;j<items2.length;j++){
			finalItems.add(items2[j]);
		}
		StringBuffer sb = new StringBuffer();
		Iterator<String> itr = finalItems.iterator();
		while (itr.hasNext()) {
			sb.append(itr.next());
			if (itr.hasNext()) {
				sb.append(" ");
			}
		}
		node1.setNodeValue(sb.toString());
		
		Node[] response = new Node[nodeList2.getLength()];
		for (int j=0;j<response.length;j++){
			response[j] = nodeList2.item(j);
		}
		return response;
	}

}
