package hr.algebra.theloop.utils;

import hr.algebra.theloop.model.GameConfiguration;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class XmlUtils {

    private XmlUtils() {}

    private static final String GAME_CONFIGURATION = "GameConfiguration";
    public static final String XML_CONFIG_FILE = "xml/gameConfig.xml";

    public static void saveGameConfiguration(GameConfiguration config) {
        try {
            ensureXmlDirectoryExists();
            Document document = createDocument(GAME_CONFIGURATION);
            appendConfigurationElements(config, document);
            saveDocument(document, XML_CONFIG_FILE);
            GameLogger.success("Game configuration saved to " + XML_CONFIG_FILE);
        } catch (ParserConfigurationException | TransformerException | IOException e) {
            GameLogger.error("Failed to save configuration: " + e.getMessage());
            throw new RuntimeException("Error saving XML configuration", e);
        }
    }

    public static GameConfiguration loadGameConfiguration() {
        if (!Files.exists(Path.of(XML_CONFIG_FILE))) {
            GameLogger.warning("Config file not found, creating default: " + XML_CONFIG_FILE);
            createDefaultConfiguration();
        }

        try {
            Document document = parseDocument(XML_CONFIG_FILE);
            return extractGameConfiguration(document);
        } catch (Exception e) {
            GameLogger.error("Failed to load configuration: " + e.getMessage());
            return createDefaultGameConfiguration();
        }
    }

    private static void ensureXmlDirectoryExists() throws IOException {
        Path xmlDir = Paths.get("xml");
        if (!Files.exists(xmlDir)) {
            Files.createDirectories(xmlDir);
            GameLogger.gameFlow("📁 Created xml directory");
        }
    }

    private static Document createDocument(String rootElement) throws ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.newDocument();
        Element root = document.createElement(rootElement);
        document.appendChild(root);
        return document;
    }

    private static Document parseDocument(String filePath) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
        factory.setValidating(false);
        DocumentBuilder builder = factory.newDocumentBuilder();

        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                GameLogger.warning("XML Warning: " + exception.getMessage());
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                GameLogger.error("XML Error: " + exception.getMessage());
                throw exception;
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                GameLogger.error("XML Fatal Error: " + exception.getMessage());
                throw exception;
            }
        });

        return builder.parse(new File(filePath));
    }

    private static void saveDocument(Document document, String filename) throws TransformerException {
        Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(document), new StreamResult(new File(filename)));
    }

    private static Node createElement(Document document, String tagName, String data) {
        Element element = document.createElement(tagName);
        Text text = document.createTextNode(data);
        element.appendChild(text);
        return element;
    }

    private static void appendConfigurationElements(GameConfiguration config, Document document) {
        Element root = document.getDocumentElement();

        Element gameSettings = document.createElement("GameSettings");
        root.appendChild(gameSettings);
        gameSettings.appendChild(createElement(document, "maxCycles", String.valueOf(config.getMaxCycles())));
        gameSettings.appendChild(createElement(document, "missionsToWin", String.valueOf(config.getMissionsToWin())));
        gameSettings.appendChild(createElement(document, "maxVortexes", String.valueOf(config.getMaxVortexes())));
        gameSettings.appendChild(createElement(document, "maxHandSize", String.valueOf(config.getMaxHandSize())));

        Element networkSettings = document.createElement("NetworkSettings");
        root.appendChild(networkSettings);
        networkSettings.appendChild(createElement(document, "serverPort", String.valueOf(config.getServerPort())));
        networkSettings.appendChild(createElement(document, "chatPort", String.valueOf(config.getChatPort())));
        networkSettings.appendChild(createElement(document, "connectionTimeout", String.valueOf(config.getConnectionTimeout())));

        Element playerSettings = document.createElement("PlayerSettings");
        root.appendChild(playerSettings);
        playerSettings.appendChild(createElement(document, "startingEra", config.getStartingEra()));
        playerSettings.appendChild(createElement(document, "startingEnergy", String.valueOf(config.getStartingEnergy())));
        playerSettings.appendChild(createElement(document, "freeBatteryUses", String.valueOf(config.getFreeBatteryUses())));
    }

    private static GameConfiguration extractGameConfiguration(Document document) {
        GameConfiguration config = new GameConfiguration();
        Element root = document.getDocumentElement();

        NodeList gameSettingsNodes = root.getElementsByTagName("GameSettings");
        if (gameSettingsNodes.getLength() > 0) {
            Element gameSettings = (Element) gameSettingsNodes.item(0);
            config.setMaxCycles(getIntValue(gameSettings, "maxCycles", 3));
            config.setMissionsToWin(getIntValue(gameSettings, "missionsToWin", 4));
            config.setMaxVortexes(getIntValue(gameSettings, "maxVortexes", 3));
            config.setMaxHandSize(getIntValue(gameSettings, "maxHandSize", 3));
        }

        NodeList networkSettingsNodes = root.getElementsByTagName("NetworkSettings");
        if (networkSettingsNodes.getLength() > 0) {
            Element networkSettings = (Element) networkSettingsNodes.item(0);
            config.setServerPort(getIntValue(networkSettings, "serverPort", 12345));
            config.setChatPort(getIntValue(networkSettings, "chatPort", 1099));
            config.setConnectionTimeout(getIntValue(networkSettings, "connectionTimeout", 5000));
        }

        NodeList playerSettingsNodes = root.getElementsByTagName("PlayerSettings");
        if (playerSettingsNodes.getLength() > 0) {
            Element playerSettings = (Element) playerSettingsNodes.item(0);
            config.setStartingEra(getStringValue(playerSettings, "startingEra", "DAWN_OF_TIME"));
            config.setStartingEnergy(getIntValue(playerSettings, "startingEnergy", 1));
            config.setFreeBatteryUses(getIntValue(playerSettings, "freeBatteryUses", 1));
        }

        return config;
    }

    private static int getIntValue(Element parent, String tagName, int defaultValue) {
        try {
            NodeList nodes = parent.getElementsByTagName(tagName);
            if (nodes.getLength() > 0) {
                return Integer.parseInt(nodes.item(0).getTextContent().trim());
            }
        } catch (NumberFormatException e) {
            GameLogger.warning("Invalid integer value for " + tagName + ", using default: " + defaultValue);
        }
        return defaultValue;
    }

    private static String getStringValue(Element parent, String tagName, String defaultValue) {
        try {
            NodeList nodes = parent.getElementsByTagName(tagName);
            if (nodes.getLength() > 0) {
                String value = nodes.item(0).getTextContent().trim();
                return value.isEmpty() ? defaultValue : value;
            }
        } catch (Exception e) {
            GameLogger.warning("Invalid string value for " + tagName + ", using default: " + defaultValue);
        }
        return defaultValue;
    }

    private static void createDefaultConfiguration() {
        GameConfiguration defaultConfig = createDefaultGameConfiguration();
        saveGameConfiguration(defaultConfig);
    }

    private static GameConfiguration createDefaultGameConfiguration() {
        GameConfiguration config = new GameConfiguration();
        config.setMaxCycles(3);
        config.setMissionsToWin(4);
        config.setMaxVortexes(3);
        config.setMaxHandSize(3);
        config.setServerPort(12345);
        config.setChatPort(1099);
        config.setConnectionTimeout(5000);
        config.setStartingEra("DAWN_OF_TIME");
        config.setStartingEnergy(1);
        config.setFreeBatteryUses(1);
        return config;
    }
}