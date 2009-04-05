package org.xidea.lite.parser.test;

import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xidea.lite.Template;
import org.xidea.lite.parser.ParseContext;
import org.xidea.lite.parser.ParseContextImpl;
import org.xidea.lite.parser.XMLParser;


public class XMLParserTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testSelect() throws Exception {
		URL url = this.getClass().getResource("include-test.xml");
		ParseContextImpl context = new ParseContextImpl(url); 
		org.w3c.dom.Document doc = new XMLParser().loadXML(url, context);
		
		DocumentFragment node = new XMLParser().selectNodes("//xhtml:body",doc);
		Assert.assertTrue(node.getChildNodes().getLength()==1);
	}

	@Test
	public void testFormat() throws Exception {
		URL url = this.getClass().getResource("asciitable.xhtml");
		ParseContextImpl parseContext = new ParseContextImpl(url); 
		parseContext.setFormat(true);
		HashMap context = new HashMap();
		context.put("data", Arrays.asList("0", "1", "2", "3", "4", "5", "6",
				"7", "8", "9", "A", "B", "C", "D", "E", "F"));
		context.put("name", "test");
		context.put("border", "1px");
		context.put("testString", "1");
		
		Template t = new Template(new XMLParser().parse(url, parseContext));
		Writer out=new StringWriter();
		t.render(context, out);
		System.out.println(out);
	}

}
