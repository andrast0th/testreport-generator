package com.hpe.junit.testreport.generator;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class App {

    public static final String SUITE_CLASS_NAME = "com.hpe.junit.testreport.generator.AppTest";

    public static enum TestResult {
        FAILED, PASSED, SKIPPED
    }

    public static void main(String[] args) {

        final Options options = new Options();

        options
                .addOption(Option.builder().argName("failedTestCount").longOpt("failedTestCount").required().hasArg().build())
                .addOption(Option.builder().argName("passedTestCount").longOpt("passedTestCount").required().hasArg().build())
                .addOption(Option.builder().argName("skippedTestCount").longOpt("skippedTestCount").required().hasArg().build())
                .addOption(Option.builder().argName("testDuration").longOpt("testDuration").required().hasArg().build())
                .addOption(Option.builder().argName("resultXmlFolder")
                        .longOpt("resultXmlFolder")
                        .desc("Make sure to add a / at the end")
                        .hasArg()
                        .build());

        // create the parser
        CommandLineParser parser = new DefaultParser();
        CommandLine line;
        try {
            // parse the command line arguments
            line = parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            return;
        }

        int failedTestCount = Integer.valueOf(line.getOptionValue("failedTestCount"));
        int passedTestCount = Integer.valueOf(line.getOptionValue("passedTestCount"));
        int skippedTestCount = Integer.valueOf(line.getOptionValue("skippedTestCount"));
        double testDuration = Double.valueOf(line.getOptionValue("testDuration"));
        double perTestDuration = testDuration / (failedTestCount + passedTestCount);
        String resultXmlFolder = line.getOptionValue("resultXmlFolder", "");

        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final Document doc = docBuilder.newDocument();

            // root elements
            Element rootElement = doc.createElement("testsuite");
            doc.appendChild(rootElement);
            rootElement.setAttribute("name", SUITE_CLASS_NAME);
            rootElement.setAttribute("tests", failedTestCount + passedTestCount + skippedTestCount + "");
            rootElement.setAttribute("failures", failedTestCount + "");
            rootElement.setAttribute("errors", 0 + "");
            rootElement.setAttribute("skipped", skippedTestCount + "");
            rootElement.setAttribute("time", testDuration + "");

            // Add empty props
            Element properties = doc.createElement("properties");
            rootElement.appendChild(properties);

            Integer testIndex = 1;

            for (int i = 0; i < passedTestCount; i++) {
                rootElement.appendChild(createTestElement(doc, TestResult.PASSED, testIndex, perTestDuration));
                testIndex += 1;
            }

            for (int i = 0; i < failedTestCount; i++) {
                rootElement.appendChild(createTestElement(doc, TestResult.FAILED, testIndex, perTestDuration));
                testIndex += 1;
            }

            for (int i = 0; i < skippedTestCount; i++) {
                rootElement.appendChild(createTestElement(doc, TestResult.SKIPPED, testIndex, perTestDuration));
                testIndex += 1;
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer;

            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(resultXmlFolder + "TEST-" + SUITE_CLASS_NAME + ".xml"));
            transformer.transform(source, result);

            // Output to console for testing
            if (resultXmlFolder != null && !resultXmlFolder.trim().isEmpty()) {
                System.out.println("File saved to location: " + resultXmlFolder + "TEST-" + SUITE_CLASS_NAME + ".xml");
            } else {
                if (!resultXmlFolder.trim().isEmpty()) {
                    System.out.println("File saved to wd: " + "TEST-" + SUITE_CLASS_NAME + ".xml");
                }
            }
            result = new StreamResult(System.out);
            transformer.transform(source, result);

        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    private static Element createTestElement(Document doc, TestResult testResult, int testNo, double duration) {
        Element element = doc.createElement("testcase");
        element.setAttribute("classname", SUITE_CLASS_NAME);
        element.setAttribute("name", "test" + testNo);
        element.setAttribute("time", duration + "");

        if (TestResult.FAILED == testResult) {
            Element failure = doc.createElement("failure");
            failure.setAttribute("type", "junit.framework.AssertionFailedError");
            failure.setTextContent("Assert failed, beacuse it was supposed to");
            element.appendChild(failure);
        }

        if (TestResult.SKIPPED == testResult) {
            Element skipped = doc.createElement("skipped");
            element.appendChild(skipped);
        }

        return element;
    }

}