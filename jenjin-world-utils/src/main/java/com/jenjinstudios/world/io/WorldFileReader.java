package com.jenjinstudios.world.io;

import com.jenjinstudios.world.Location;
import com.jenjinstudios.world.LocationProperties;
import com.jenjinstudios.world.World;
import com.jenjinstudios.world.Zone;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.TreeMap;

/**
 * This class handles the reading of and construction from world xml files.
 * @author Caleb Brinkman
 */
public class WorldFileReader
{
	/** The tag name for the root "zone" tags. */
	public static final String ZONE_TAG_NAME = "zone";
	/** The tag name for the "location" tags. */
	public static final String LOCATION_TAG_NAME = "location";
	/** The {@code Document} generated by parsing the world XML file. */
	private final Document worldDocument;
	/** The byte array containing the world file checksum. */
	private final byte[] worldFileChecksum;
	/** The bytes in the world file. */
	private final byte[] worldFileBytes;

	/**
	 * Construct a new WorldFileReader pointing to the specified file.
	 * @param worldFile The file containing the world information.
	 * @throws java.io.IOException If there is an error reading the file.
	 * @throws org.xml.sax.SAXException If there is an error parsing the world xml file.
	 * @throws javax.xml.parsers.ParserConfigurationException If there is an error configuring the XML parser.
	 * @throws java.security.NoSuchAlgorithmException If there is an error getting the checksum.
	 * @throws javax.xml.transform.TransformerException If there's an error configuring the world file.
	 */
	public WorldFileReader(File worldFile) throws IOException, SAXException, ParserConfigurationException, NoSuchAlgorithmException, TransformerException {
		this(new FileInputStream(worldFile));

	}

	/**
	 * Construct a new WorldFileReader pointing to the specified file.
	 * @param inputStream The input stream containing the world information.
	 * @throws java.io.IOException If there is an error reading the file.
	 * @throws org.xml.sax.SAXException If there is an error parsing the world xml file.
	 * @throws javax.xml.parsers.ParserConfigurationException If there is an error configuring the XML parser.
	 * @throws java.security.NoSuchAlgorithmException If there is an error getting the MD5 algorithm.
	 * @throws javax.xml.transform.TransformerException If there's an error configuring the world file.
	 */
	public WorldFileReader(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException, NoSuchAlgorithmException, TransformerException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		worldDocument = builder.parse(inputStream);
		worldDocument.getDocumentElement().normalize();
		worldFileBytes = readBytes();
		worldFileChecksum = ChecksumUtil.getMD5Checksum(readBytes());
	}

	/**
	 * Parse the read file and return the appropriate World object.
	 * @return The World object represented by the XML file.
	 */
	public World read() {
		World world;

		NodeList zoneNodes = worldDocument.getElementsByTagName(ZONE_TAG_NAME);
		world = new World(parseZoneNodes(zoneNodes));

		return world;
	}

	/**
	 * Read the World file into an array of bytes.
	 * @return An array of bytes containing the world file.
	 * @throws TransformerException If there's an error parsing the world file.
	 */
	public byte[] readBytes() throws TransformerException {
		byte[] fileBytes;
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(bos);
		transformer.transform(new DOMSource(worldDocument), result);
		fileBytes = bos.toByteArray();
		return fileBytes;
	}

	/**
	 * Get the MD5 Checksum of the world file.
	 * @return The MD5 Checksum of the world file.
	 */
	public byte[] getWorldFileChecksum() {
		return worldFileChecksum;
	}

	/**
	 * Get the bytes contained in the world file.
	 * @return The bytes contained in the world file.
	 */
	public byte[] getWorldFileBytes() {
		return worldFileBytes;
	}

	/**
	 * Read the Zone node XML located in {@code zoneNodes} and parse the created zone into {@code zones}.
	 * @param zoneNodes The list of nodes containing Zone XML.
	 * @return The zones parsed from the node list.
	 */
	private Zone[] parseZoneNodes(NodeList zoneNodes) {
		Zone[] zones = new Zone[zoneNodes.getLength()];

		for (int i = 0; i < zoneNodes.getLength(); i++)
		{
			Element currentZoneElement = (Element) zoneNodes.item(i);
			NodeList locationNodes = currentZoneElement.getElementsByTagName(LOCATION_TAG_NAME);
			int id = Integer.parseInt(zoneNodes.item(i).getAttributes().getNamedItem("id").getTextContent());
			int xSize = Integer.parseInt(zoneNodes.item(i).getAttributes().getNamedItem("xSize").getTextContent());
			int ySize = Integer.parseInt(zoneNodes.item(i).getAttributes().getNamedItem("ySize").getTextContent());

			zones[i] = new Zone(id, xSize, ySize, parseLocationNodes(locationNodes));
		}

		return zones;
	}

	/**
	 * Parse the specified XML nodes into the specified Location array.
	 * @param locationNodes The XML nodes to parse.
	 * @return The locations parsed.
	 */
	private Location[] parseLocationNodes(NodeList locationNodes) {
		Location[] locations = new Location[locationNodes.getLength()];
		for (int i = 0; i < locationNodes.getLength(); i++)
		{
			Node currentLocationNode = locationNodes.item(i);
			NamedNodeMap attributes = currentLocationNode.getAttributes();
			int x = Integer.parseInt(attributes.getNamedItem("x").getTextContent());
			int y = Integer.parseInt(attributes.getNamedItem("y").getTextContent());
			TreeMap<String, String> properties = new TreeMap<>();
			for (int j = 0; j < attributes.getLength(); j++)
			{
				Attr item = (Attr) attributes.item(j);
				if (item != null && !"x".equals(item.getName()) && !"y".equals(item.getName()))
				{
					String name = item.getName();
					String value = item.getValue();
					properties.put(name, value);
				}
			}
			LocationProperties locationProperties = new LocationProperties(properties);
			locations[i] = new Location(x, y, locationProperties);
		}
		return locations;
	}

}
