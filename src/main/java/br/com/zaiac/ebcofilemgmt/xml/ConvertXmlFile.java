package br.com.zaiac.ebcofilemgmt.xml;

import br.com.zaiac.ebcofilemgmt.xml.classes.DataForm;
import br.com.zaiac.ebcofilemgmt.xml.classes.admindata.AdminData;
import br.com.zaiac.ebcofilemgmt.xml.classes.admindata.Container;
import br.com.zaiac.ebcofilemgmt.xml.classes.admindata.Containers;
import br.com.zaiac.ebcofilemgmt.xml.classes.admindata.Trailer;
import br.com.zaiac.ebcofilemgmt.xml.classes.admindata.Vehicle;
import br.com.zaiac.ebcofilemgmt.xml.classes.processinstructions.EnergyLevel;
import br.com.zaiac.ebcofilemgmt.xml.classes.processinstructions.InspectionType;
import br.com.zaiac.ebcofilemgmt.xml.classes.processinstructions.ProcessInstructions;
import java.io.File;
import java.io.StringWriter;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConvertXmlFile {
    
    private static final String FILENAME = "C:\\EBCO\\CARGOVISION\\20230210001524002A.xml";
    
    
    public static void convertXmlFile() {
    

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilderFactory dbfOut = DocumentBuilderFactory.newInstance();
        
        DataForm dataForm = new DataForm();
        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(FILENAME));
            DocumentBuilder dBuilder = dbfOut.newDocumentBuilder();
            doc.getDocumentElement().normalize();
            System.out.println("Root Element :" + doc.getDocumentElement().getNodeName() + " " +  doc.getDocumentElement().getNodeType()+ " " +  doc.getDocumentElement().getNodeValue());
            System.out.println("------");
            
            NodeList nlDataForm = doc.getElementsByTagName("DataForm");
            for (int iDataForm = 0; iDataForm < nlDataForm.getLength(); iDataForm++) {
                Node noDataForm = nlDataForm.item(iDataForm);
                if (noDataForm.getNodeType() == Node.ELEMENT_NODE) {
                    Element eDataForm = (Element) noDataForm;
                    
                    
                    dataForm.setVersion("V01.07");
                    dataForm.setTruckId(eDataForm.getElementsByTagName("TruckId").item(0).getTextContent());
                    dataForm.setState(Integer.parseInt(eDataForm.getElementsByTagName("State").item(0).getTextContent()));
                    dataForm.setSite(eDataForm.getElementsByTagName("Site").item(0).getTextContent());
                    //Ver Isso Depois     dataForm.setDate(new Date(eDataForm.getElementsByTagName("Date").item(0).getTextContent()));
                    dataForm.setInTraining(Integer.parseInt(eDataForm.getElementsByTagName("InTraining").item(0).getTextContent()));
                    dataForm.setInReference(Integer.parseInt(eDataForm.getElementsByTagName("InReference").item(0).getTextContent()));
                    dataForm.setArchived(Integer.parseInt(eDataForm.getElementsByTagName("Archived").item(0).getTextContent()));
                    dataForm.setInEdition(Integer.parseInt(eDataForm.getElementsByTagName("InEdition").item(0).getTextContent()));
                    dataForm.setAnalysed(1);
                    dataForm.setCheckedOut(0);
                    dataForm.setApproved(0);
                    dataForm.setPending(0);
                    //dataForm.setCargoSet(eDataForm.getElementsByTagName("CargoSet").item(0).getTextContent());
                    //dataForm.setBackToCount(eDataForm.getElementsByTagName("BackToCount").item(0).getTextContent());
                    //dataForm.setClearImgCount(eDataForm.getElementsByTagName("ClearImgCount").item(0).getTextContent());
                    
                    
                    
                    
                    
                    
                    
                    NodeList nlAdminData = eDataForm.getElementsByTagName("AdminData");
                    dataForm.createAdminData();
                    for (int iAdminData = 0; iAdminData < nlAdminData.getLength(); iAdminData++) {
                        Node noAdminData = nlAdminData.item(iAdminData);
                        if (noAdminData.getNodeType() == Node.ELEMENT_NODE) {
                            Element eAdminData = (Element) noAdminData;
                            AdminData adminData = dataForm.getAdminData();
                            adminData.setFileId("");
                            adminData.setComments("");

                            NodeList nlVehicle = eAdminData.getElementsByTagName("Vehicle");
                            dataForm.createVehicle();
                            dataForm.getAdminData().getVehicle().createTrailers();
                            
                            for (int iVehicle = 0; iVehicle < nlVehicle.getLength(); iVehicle++) {
                                Node noVehicle = nlVehicle.item(iVehicle);
                                if (noVehicle.getNodeType() == Node.ELEMENT_NODE) {
                                    Element eVehicle = (Element) noVehicle;
                                    Vehicle vehicle = adminData.getVehicle();
                                    vehicle.setOcr(eVehicle.getElementsByTagName("OCR").item(0).getTextContent());
                                    vehicle.setPlateNumber("");                                    
                                    
                                    NodeList nlTrailers = eVehicle.getElementsByTagName("Trailers");
                                    for (int iTrailers = 0; iTrailers < nlTrailers.getLength(); iTrailers++) {
                                        Node noTrailers = nlTrailers.item(iTrailers);
                                        if (noTrailers.getNodeType() == Node.ELEMENT_NODE) {
                                            Element eTrailers = (Element) noTrailers;
                                            NodeList nlTrailer = eTrailers.getElementsByTagName("Trailer");
                                            dataForm.getAdminData().getVehicle().createTrailers();
                                            for (int iTrailer = 0; iTrailer < nlTrailer.getLength(); iTrailer++) {
                                                Node noTrailer = nlTrailer.item(iTrailer);
                                                if (noTrailer.getNodeType() == Node.ELEMENT_NODE) {
                                                    Element eTrailer = (Element) noTrailer;
                                                    int i = dataForm.getAdminData().getVehicle().getTrailers().createTrailer();
                                                    Trailer thrailer = dataForm.getAdminData().getVehicle().getTrailers().getTrailerIndex(i);
                                                    thrailer.setOcr(eVehicle.getElementsByTagName("OCR").item(0).getTextContent());
                                                    thrailer.setPlateNumber("");                                                    
                                                    
                                                    NodeList nlContainers = eTrailer.getElementsByTagName("Containers");
                                                    thrailer.createContainers();
                                                    for (int iContainers = 0; iContainers < nlContainers.getLength(); iContainers++) {
                                                        Node noContainers = nlContainers.item(iContainers);
                                                        if (noContainers.getNodeType() == Node.ELEMENT_NODE) {
                                                            Element eContainers = (Element) noContainers;
                                                            //System.out.println("Containers " + eContainers.getNodeName());
                                                            //System.out.println("(1)");
                                                            
                                                            NodeList nlContainer = eContainers.getElementsByTagName("Container");
                                                            //thrailer.createContainer();                                                                
                                                            for (int iContainer = 0; iContainer < nlContainer.getLength(); iContainer++) {                                                                
                                                                Node noContainer = nlContainer.item(iContainer);
                                                                System.out.println("(2)");
                                                                if (noContainer.getNodeType() == Node.ELEMENT_NODE) {
                                                                    Element eContainer = (Element) noContainer;
                                                                    Containers containers = thrailer.getContainers();
                                                                    int x = containers.createContainer();
                                                                    Container container = containers.getContainerIndex(x);
                                                                    try {
                                                                        container.setContainerId(eContainer.getElementsByTagName("ContainerId").item(0).getTextContent());
                                                                    } catch (NullPointerException e) {
                                                                        container.setContainerId("");                                                                        
                                                                    }
                                                                    try {
                                                                        container.setOcr(eContainer.getElementsByTagName("OCR").item(0).getTextContent());
                                                                    } catch (NullPointerException e) {
                                                                        container.setOcr("-1");                                                                        
                                                                    }
                                                                    container.createLoad();
                                                                    container.getLoad().createMlList();
                                                                    container.getLoad().getMlListIndex(0).createLevel();
                                                                 }
                                                            }
                                                         }
                                                    }
                                                 }
                                            }
                                            
                                         }
                                    }

                                 }
                            }
                            
                         }
                    }
                    
                    NodeList nlProcessInstructions = eDataForm.getElementsByTagName("ProcessInstructions");
                    ProcessInstructions processInstructions = new ProcessInstructions();
                    for (int iProcessInstructions = 0; iProcessInstructions < nlProcessInstructions.getLength(); iProcessInstructions++) {
                        
                        System.out.println("Entrou no Loop iProcessInstructions");
                        
                        Node nodeProcessInstructions = nlProcessInstructions.item(iProcessInstructions);
                        if (nodeProcessInstructions.getNodeType() == Node.ELEMENT_NODE) {
                            Element eProcessInstructions = (Element) nodeProcessInstructions;
                            Element eInspectionType = (Element) eProcessInstructions.getElementsByTagName("InspectionType").item(0);
                            Element eEnergyLevel = (Element) eProcessInstructions.getElementsByTagName("EnergyLevel").item(0);
                            InspectionType inspectionType = new InspectionType(eInspectionType.getAttribute("Value"), eInspectionType.getTextContent());
                            EnergyLevel energyLevel = new EnergyLevel(eEnergyLevel.getAttribute("Value"), eEnergyLevel.getTextContent());
                            
                            processInstructions.setInspectionType(inspectionType);
                            processInstructions.setEnergyLevel(energyLevel);
                            processInstructions.setPendingRequired(eProcessInstructions.getElementsByTagName("PendingRequired").item(0).getTextContent());
                            processInstructions.setScanPosition(eProcessInstructions.getElementsByTagName("ScanPosition").item(0).getTextContent());
                            processInstructions.setHEDRequired(eProcessInstructions.getElementsByTagName("HEDRequired").item(0).getTextContent());
                            processInstructions.setSpeed(eProcessInstructions.getElementsByTagName("Speed").item(0).getTextContent());
                            processInstructions.setIsContainerEmpty("");                            
                         }
                    }
                    dataForm.setProcessInstructions(processInstructions);
                    
                    NodeList processData = eDataForm.getElementsByTagName("ProcessData");
                    for (int iProcessData = 0; iProcessData < processData.getLength(); iProcessData++) {
                        Node nodeProcessData = processData.item(iProcessData);
                        if (nodeProcessData.getNodeType() == Node.ELEMENT_NODE) {
                            Element eProcessData = (Element) nodeProcessData;
                            System.out.println("Elemento 4 " + eProcessData.getElementsByTagName("Image").item(0).getTextContent());
                         }
                    }
                    
                    NodeList operations = eDataForm.getElementsByTagName("Operations");
                    for (int iOperations = 0; iOperations < operations.getLength(); iOperations++) {
                        Node nodeOperations = operations.item(iOperations);
                        if (nodeOperations.getNodeType() == Node.ELEMENT_NODE) {
                            Element eOperations = (Element) nodeOperations;
                            
                            NodeList operation = eOperations.getElementsByTagName("Operation");
                            for (int iOperation = 0; iOperation < operation.getLength(); iOperation++) {
                                Node nodeOperation = operation.item(iOperation);
                                if (nodeOperation.getNodeType() == Node.ELEMENT_NODE) {
                                    Element eOperation = (Element) nodeOperation;
                                    String type = eOperation.getAttribute("Type");
                                    System.out.println("Elemento 5 " + type + " " + eOperation.getElementsByTagName("Start").item(0).getTextContent());
                                }
                            }
                        }
                    }
                }
            }
            //TransformerFactory transformerFactory =  TransformerFactory.newInstance();
            //Transformer transformer = transformerFactory.newTransformer();
            //System.out.println("DocOut " + docOut.toString());
            //DOMSource source = new DOMSource(docOut);
            File file = new File("C:\\EBCO\\CARGOVISION\\20230210001524002A_123.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(DataForm.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(dataForm, file);// this line create customer.xml file in specified path.

            StringWriter sw = new StringWriter();
            jaxbMarshaller.marshal(dataForm, sw);
            String xmlString = sw.toString();

            System.out.println(xmlString);            

            //StreamResult result =  new StreamResult(new File("D:\\testing.xml"));
            //transformer.transform(source, result);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        

//        } catch (ParserConfigurationException | SAXException | IOException e) {
//            e.printStackTrace();
//        } catch (JAXBException ex) {
//             Logger.getLogger(ConvertXmlFile.class.getName()).log(Level.SEVERE, null, ex);
//        }



        System.out.println("Hello World!");
        
    }
    
}
