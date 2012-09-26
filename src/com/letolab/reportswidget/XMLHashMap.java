package com.letolab.reportswidget;

import java.util.HashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLHashMap<K, V> extends HashMap<K, V> {
	/*
	 * XML document represented as a HashMap
	 * 
	 * get(key) returns the first available value or empty string if nothing found
	 * 
	 * You can use XPath as a key.
	 * 
	 * For example to make a compex request:
	 * 
	 * 
	 * Log.d(LOG_TAG, "productID = " + ret.get("productId"));
	 * for (int i=1; i<=ret.getItemLength("sku"); i++) {
	 *     Log.d(LOG_TAG, "SKU ID = " + ret.get("sku[" + String.valueOf(i) + "]/skuId"));
	 *     Log.d(LOG_TAG, "Size " + ret.get("sku[" + String.valueOf(i) + "]//optionName[.=\"Size\"]/../optionChoice"));
	 *     Log.d(LOG_TAG, "Size Code " + ret.get("sku[" + String.valueOf(i) + "]//optionName[.=\"Size\"]/../optionCode"));
	 * }
	 * 
	 * Outputs:
	 * 
	 * productID = 360837
	 * SKU ID = 6397-360837-L
	 * Size Large
	 * Size Code L
	 * SKU ID = 6397-360837-M
	 * Size Medium
	 * Size Code M
	 * SKU ID = 6397-360837-S
	 * Size Small
	 * Size Code S
	 * SKU ID = 6397-360837-XL
	 * Size XL
	 * Size Code XL
	 * SKU ID = 6397-360837-XXL
	 * Size XXL
	 * Size Code XXL
	 * SKU ID = 6397-360837-XXXL
	 * Size XXXL
	 * Size Code XXXL
	 * 
	 * 
	 * NOTE: You can test your XPath here http://www.xpathtester.com/test
	 * 
	 */
	private Document xml;
	
	public XMLHashMap(Document xml) {
		this.xml = xml;
	}
	
	@Override
	public V get(Object key) {
		return this.getXPath(key);
	}

	public int getItemLength(Object key) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		
	    // XPath Query for showing all nodes value
	    XPathExpression expr;
	    String query = (String) key;
	    if (!query.startsWith("//")) {
	    	query = "//" + query;
	    }
	    
		try {
			expr = xpath.compile(query);

		    Object result = expr.evaluate(this.xml, XPathConstants.NODESET);
		    NodeList nodes = (NodeList) result;
			return nodes.getLength();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	public Element getElement(Object key) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		
	    // XPath Query for showing all nodes value
	    XPathExpression expr;
	    String query = (String) key;
	    if (!query.startsWith("//")) {
	    	query = "//" + query;
	    }
	    
		try {
			expr = xpath.compile(query);

		    Object result = expr.evaluate(this.xml, XPathConstants.NODESET);
		    NodeList nodes = (NodeList) result;
			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);
			    return element;
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public V getXPath(Object key) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		
	    // XPath Query for showing all nodes value
	    XPathExpression expr;
	    String query = (String) key;
	    if (!query.startsWith("//")) {
	    	query = "//" + query;
	    }
	    
		try {
			expr = xpath.compile(query);

		    Object result = expr.evaluate(this.xml, XPathConstants.NODESET);
		    NodeList nodes = (NodeList) result;
			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);
			    String value = getCharacterDataFromElement(element);
			    if (!value.equals("")) {
			    	return (V) value;
			    }
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (V) "";
	}
	
	private static String getCharacterDataFromElement(Element e) {
	    Node child = e.getFirstChild();
	    if (child instanceof CharacterData) {
	      CharacterData cd = (CharacterData) child;
	      return cd.getData();
	    }
	    return "";
    }
}