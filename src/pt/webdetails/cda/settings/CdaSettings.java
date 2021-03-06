package pt.webdetails.cda.settings;

import java.util.HashMap;
import java.util.List;

import javax.naming.OperationNotSupportedException;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.pentaho.reporting.libraries.formula.FormulaContext;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.UnsupportedConnectionException;
import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.dataaccess.UnsupportedDataAccessException;
import pt.webdetails.cda.discovery.DiscoveryOptions;
import pt.webdetails.cda.utils.TableModelUtils;
import pt.webdetails.cda.utils.Util;

/**
 * CdaSettings class
 * <p/>
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 2:41:44 PM
 */
public class CdaSettings {

  private static final Log logger = LogFactory.getLog(CdaSettings.class);
  private String id;
  private ResourceKey contextKey;
  private Element root;
  private HashMap<String, Connection> connectionsMap;
  private HashMap<String, DataAccess> dataAccessMap;
  private FormulaContext formulaContext;

  /**
   * Creates a representation of an existing CDA file.
   *
   * @param doc - XML document
   * @param id  - File id
   * @param key - Context key - representation of the id in the solution rep.
   * @throws UnsupportedConnectionException
   * @throws UnsupportedDataAccessException
   */
  public CdaSettings(final Document doc,
          final String id,
          final ResourceKey key) throws UnsupportedConnectionException, UnsupportedDataAccessException {

    this.contextKey = key;
    this.id = id;
    this.root = doc.getRootElement();

    connectionsMap = new HashMap<String, Connection>();
    dataAccessMap = new HashMap<String, DataAccess>();

    parseDocument();

  }

  /**
   * Creates a representation of a CDA file and handles the xml generation
   *
   * @param id
   * @param key
   * @throws UnsupportedConnectionException
   * @throws UnsupportedDataAccessException
   */
  public CdaSettings(final String id,
          final ResourceKey key) throws UnsupportedConnectionException, UnsupportedDataAccessException {

    this.contextKey = key;
    this.id = id;

    Document doc = DocumentFactory.getInstance().createDocument("UTF-8");

    doc.addElement("CDADescriptor");
    this.root = doc.getRootElement();
    this.root.add(doc.addElement("DataSources"));

    connectionsMap = new HashMap<String, Connection>();
    dataAccessMap = new HashMap<String, DataAccess>();


  }
  
	public CdaSettings(final String id, final ResourceKey key, boolean garbage) throws UnsupportedConnectionException, UnsupportedDataAccessException {
				
				this.contextKey = key;
				this.id = id;
				
				connectionsMap = new HashMap<String, Connection>();
				dataAccessMap = new HashMap<String, DataAccess>();
	}

  public TableModel listQueries(DiscoveryOptions discoveryOptions) {


    return TableModelUtils.getInstance().dataAccessMapToTableModel(dataAccessMap);

  }
  
  public void setFormulaContext(FormulaContext formulaContext){
  	this.formulaContext = formulaContext;
  	for(DataAccess dataAccess : this.dataAccessMap.values()){
  		dataAccess.setFormulaContext(formulaContext);
  	}
  }

  private void parseDocument() throws UnsupportedConnectionException, UnsupportedDataAccessException {

    // 1 - Parse Connections
    // 2 - Parse DataAccesses

    logger.debug("Creating CdaSettings - parsing document");

    parseConnections();

    parseDataAccess();


    logger.debug("CdaSettings created successfully");


  }

  private void parseDataAccess() throws UnsupportedDataAccessException {

    // Parsing DataAccess.

    // 1 - Parse data access, and then parse the CompoundDataAccess


    final List<Element> dataAccessesList = root.selectNodes("/CDADescriptor/DataAccess | /CDADescriptor/CompoundDataAccess");

    for (final Element element : dataAccessesList) {

      final String elementName = element.getName();
      final String id = element.attributeValue("id");
      final String type = element.attributeValue("type");

      // Initialize this ConnectionType

      try {

        final String className = "pt.webdetails.cda.dataaccess."
                + type.substring(0, 1).toUpperCase() + type.substring(1, type.length()) + elementName;

        final Class clazz = Class.forName(className);
        final Class[] params = {Element.class};

        final DataAccess dataAccess = (DataAccess) clazz.getConstructor(params).newInstance(new Object[]{element});
        dataAccess.setCdaSettings(this);
        addInternalDataAccess(dataAccess);

      } catch (Exception e) {
        throw new UnsupportedDataAccessException("Error initializing connection class: " + Util.getExceptionDescription(e), e);
      }


    }


  }

  private void parseConnections() throws UnsupportedConnectionException {

    final List<Element> connectionList = root.selectNodes("/CDADescriptor/DataSources/Connection");

    for (final Element element : connectionList) {

      final String id = element.attributeValue("id");
      final String type = element.attributeValue("type");

      // Initialize this ConnectionType

      try {

        // Convert sql.jdbc to sql.Jdbc

        int lastDot = type.lastIndexOf('.');
        final String className = "pt.webdetails.cda.connections."
                + type.substring(0, lastDot + 1) + type.substring(lastDot + 1, lastDot + 2).toUpperCase() + type.substring(lastDot + 2, type.length()) + "Connection";

        final Class clazz = Class.forName(className);
        final Class[] params = {Element.class};

        final Connection connection = (Connection) clazz.getConstructor(params).newInstance(new Object[]{element});
        connection.setCdaSettings(this);
        addInternalConnection(connection);

      } catch (Exception e) {
        throw new UnsupportedConnectionException("Error initializing connection class: " + Util.getExceptionDescription(e), e);
      }


    }

  }

  public String asXML() throws OperationNotSupportedException {

    return this.root.asXML();

  }
  
 /**
  * 
  * @param dataAccess
  */
  public void addDataAccess(final DataAccess dataAccess){
  	addInternalDataAccess(dataAccess);
  	dataAccess.setCdaSettings(this);
  }
  
  /**
   * 
   * @param connection
   */
   public void addConnection(final Connection connection){
  	 addInternalConnection(connection);
  	 connection.setCdaSettings(this);
   }

  private void addInternalConnection(final Connection connectionSettings) {

    connectionsMap.put(connectionSettings.getId(), connectionSettings);

  }

  private void addInternalDataAccess(final DataAccess dataAccess) {

  	if(this.formulaContext != null) dataAccess.setFormulaContext(this.formulaContext);
    dataAccessMap.put(dataAccess.getId(), dataAccess);

  }

  public Connection getConnection(final String id) throws UnknownConnectionException {

    if (!connectionsMap.containsKey(id)) {
      throw new UnknownConnectionException("Unknown connection with id " + id, null);
    }
    return connectionsMap.get(id);
  }

  public DataAccess getDataAccess(final String id) throws UnknownDataAccessException {

    if (!dataAccessMap.containsKey(id)) {
      throw new UnknownDataAccessException("Unknown dataAccess with id " + id, null);
    }
    return dataAccessMap.get(id);
  }

  public String getId() {
    return id;
  }

  public ResourceKey getContextKey() {
    return contextKey;
  }
}
