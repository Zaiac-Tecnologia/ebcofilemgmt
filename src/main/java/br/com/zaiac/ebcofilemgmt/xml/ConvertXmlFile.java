package br.com.zaiac.ebcofilemgmt.xml;

import br.com.zaiac.ebcofilemgmt.exception.XMLFileException;
import br.com.zaiac.ebcofilemgmt.tools.Constants;
import br.com.zaiac.ebcofilemgmt.xml.classes.DataForm;
import br.com.zaiac.ebcofilemgmt.xml.classes.admindata.AdminData;
import br.com.zaiac.ebcofilemgmt.xml.classes.admindata.Container;
import br.com.zaiac.ebcofilemgmt.xml.classes.admindata.Trailer;
//import br.com.zaiac.ebcofilemgmt.xml.classes.admindata.Trailers;
import br.com.zaiac.ebcofilemgmt.xml.classes.admindata.Vehicle;
import br.com.zaiac.ebcofilemgmt.xml.classes.operations.Operation;
import br.com.zaiac.ebcofilemgmt.xml.classes.processinstructions.EnergyLevel;
import br.com.zaiac.ebcofilemgmt.xml.classes.processinstructions.InspectionType;
import br.com.zaiac.ebcofilemgmt.xml.classes.processinstructions.ProcessInstructions;
import br.com.zaiac.ebcolibrary.LogApp;
import br.com.zaiac.ebcolibrary.exceptions.WriteLogFileException;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConvertXmlFile {

    // private static final String FILENAME =
    // "C:\\EBCO\\CARGOVISION\\20230210001524002A.xml";
    private static DataForm dataForm;
    private static DocumentBuilderFactory documentBuilderFactory;
    private static DocumentBuilder documentBuilder;
    private static Document document;

    private static String logDirectory;
    public static Boolean debugMode;

    public static int setDataFormValues(NodeList nlDataForm) {
        int items = 0;

        for (int iDataForm = 0; iDataForm < nlDataForm.getLength(); iDataForm++) {
            Node noDataForm = nlDataForm.item(iDataForm);
            if (noDataForm.getNodeType() == Node.ELEMENT_NODE) {
                items += 1;
                Element eDataForm = (Element) noDataForm;
                dataForm.setVersion("V01.07");
                dataForm.setTruckId(eDataForm.getElementsByTagName("TruckId").item(0).getTextContent());
                dataForm.setState(Integer.valueOf(eDataForm.getElementsByTagName("State").item(0).getTextContent()));
                dataForm.setSite(eDataForm.getElementsByTagName("Site").item(0).getTextContent());
                dataForm.setDate(eDataForm.getElementsByTagName("Date").item(0).getTextContent());
                dataForm.setInTraining(
                        Integer.valueOf(eDataForm.getElementsByTagName("InTraining").item(0).getTextContent()));
                dataForm.setInReference(
                        Integer.valueOf(eDataForm.getElementsByTagName("InReference").item(0).getTextContent()));
                dataForm.setArchived(
                        Integer.valueOf(eDataForm.getElementsByTagName("Archived").item(0).getTextContent()));
                dataForm.setInEdition(
                        Integer.valueOf(eDataForm.getElementsByTagName("InEdition").item(0).getTextContent()));
                dataForm.setAnalysed(1);
                dataForm.setCheckedOut(0);
                dataForm.setApproved(0);
                dataForm.setPending(0);
            }
        }
        return items;
    }

    public static int setAdminDataValues(NodeList nlAdminData) {
        int items = 0;
        dataForm.createAdminData();
        for (int iAdminData = 0; iAdminData < nlAdminData.getLength(); iAdminData++) {
            Node noAdminData = nlAdminData.item(iAdminData);
            items += 1;
            if (noAdminData.getNodeType() == Node.ELEMENT_NODE) {
                // Element eAdminData = (Element) noAdminData;
                AdminData adminData = dataForm.getAdminData();
                adminData.setFileId("");
                adminData.setComments("");
            }
        }
        return items;
    }

    public static int setVehicleValues(NodeList nlVehicle) {
        int items = 0;
        dataForm.createVehicle();

        for (int iVehicle = 0; iVehicle < nlVehicle.getLength(); iVehicle++) {
            Node noVehicle = nlVehicle.item(iVehicle);
            items += 1;
            if (noVehicle.getNodeType() == Node.ELEMENT_NODE) {
                Element eVehicle = (Element) noVehicle;
                AdminData adminData = dataForm.getAdminData();
                Vehicle vehicle = adminData.getVehicle();
                vehicle.setOcr(eVehicle.getElementsByTagName("OCR").item(0).getTextContent());
                vehicle.setPlateNumber("");
            }
        }

        return items;
    }

    public static int setTrailersValues(NodeList nlTrailers) {
        int items = 0;

        for (int iTrailers = 0; iTrailers < nlTrailers.getLength(); iTrailers++) {
            Node noTrailers = nlTrailers.item(iTrailers);
            items += 1;
            if (noTrailers.getNodeType() == Node.ELEMENT_NODE) {
                // Element eTrailers = (Element) noTrailers;
                // NodeList nlTrailer = eTrailers.getElementsByTagName("Trailer");
                dataForm.createTrailers();
            }
        }
        return items;
    }

    public static int setTrailerValues(NodeList nlTrailer) {
        int items = 0;
        for (int iTrailer = 0; iTrailer < nlTrailer.getLength(); iTrailer++) {
            Node noTrailer = nlTrailer.item(iTrailer);
            items += 1;
            if (noTrailer.getNodeType() == Node.ELEMENT_NODE) {
                Element eTrailer = (Element) noTrailer;
                int i = dataForm.createTrailer();
                Trailer thrailer = dataForm.getAdminData().getVehicle().getTrailers().getTrailerIndex(i);
                thrailer.setOcr(eTrailer.getElementsByTagName("OCR").item(0).getTextContent());
                thrailer.setPlateNumber("");
            }
        }
        return items;
    }

    public static int setContainersValues(NodeList nlContainers, int nlTrailerItem) {
        int items = 0;
        for (int iContainers = 0; iContainers < nlContainers.getLength(); iContainers++) {
            dataForm.getAdminData().getVehicle().getTrailers().getTrailerIndex(nlTrailerItem).createContainers();
        }
        return items;
    }

    public static int setContainerValues(NodeList nlContainer, int nlTrailerItem) {
        int items = 0;
        for (int iContainer = 0; iContainer < nlContainer.getLength(); iContainer++) {
            Node noContainer = nlContainer.item(iContainer);
            if (noContainer.getNodeType() == Node.ELEMENT_NODE) {
                Element eContainer = (Element) noContainer;
                int x = dataForm.createContainer(nlTrailerItem);
                Container container = dataForm.getContainer(nlTrailerItem, x);
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
        return items;
    }

    public static int setProcessInstructionsValues(NodeList nlProcessInstructions) {
        int items = 0;

        for (int iProcessInstructions = 0; iProcessInstructions < nlProcessInstructions
                .getLength(); iProcessInstructions++) {
            ProcessInstructions processInstructions = new ProcessInstructions();
            Node nodeProcessInstructions = nlProcessInstructions.item(iProcessInstructions);
            if (nodeProcessInstructions.getNodeType() == Node.ELEMENT_NODE) {
                items += 1;
                Element eProcessInstructions = (Element) nodeProcessInstructions;
                Element eInspectionType = (Element) eProcessInstructions.getElementsByTagName("InspectionType").item(0);
                Element eEnergyLevel = (Element) eProcessInstructions.getElementsByTagName("EnergyLevel").item(0);
                InspectionType inspectionType = new InspectionType(eInspectionType.getAttribute("Value"),
                        eInspectionType.getTextContent());
                EnergyLevel energyLevel = new EnergyLevel(eEnergyLevel.getAttribute("Value"),
                        eEnergyLevel.getTextContent());

                processInstructions.setInspectionType(inspectionType);
                processInstructions.setEnergyLevel(energyLevel);
                processInstructions.setPendingRequired(
                        eProcessInstructions.getElementsByTagName("PendingRequired").item(0).getTextContent());
                processInstructions.setScanPosition(
                        eProcessInstructions.getElementsByTagName("ScanPosition").item(0).getTextContent());
                processInstructions.setHEDRequired(
                        eProcessInstructions.getElementsByTagName("HEDRequired").item(0).getTextContent());
                processInstructions
                        .setSpeed(eProcessInstructions.getElementsByTagName("Speed").item(0).getTextContent());
                processInstructions.setIsContainerEmpty("");
                dataForm.setProcessInstructions(processInstructions);
            }
        }
        return items;
    }

    public static int setOperationsValues(NodeList nlOperations) {
        int items = 0;

        for (int iOperations = 0; iOperations < nlOperations.getLength(); iOperations++) {
            Node nodeOperations = nlOperations.item(iOperations);
            if (nodeOperations.getNodeType() == Node.ELEMENT_NODE) {
                items += 1;
                dataForm.createOperations();
            }
        }
        return items;
    }

    public static int setOperationValues(NodeList nlOperation) {
        int items = 0;

        for (int iOperation = 0; iOperation < nlOperation.getLength(); iOperation++) {
            // Operation operation = new Operation();
            Node nodeOperation = nlOperation.item(iOperation);
            if (nodeOperation.getNodeType() == Node.ELEMENT_NODE) {
                items += 1;
                Element eOperation = (Element) nodeOperation;
                String type = eOperation.getAttribute("Type");
                // System.out.println("Elemento 5 " + type + " " +
                // eOperation.getElementsByTagName("Start").item(0).getTextContent());
                int i = dataForm.createOperation();
                Operation operation = dataForm.getOperationIndex(i);

                operation.setType(type);
                operation.setStart(eOperation.getElementsByTagName("Start").item(0).getTextContent());
                operation.setEnd(eOperation.getElementsByTagName("End").item(0).getTextContent());
                operation.setSite(eOperation.getElementsByTagName("Site").item(0).getTextContent());

                if (type.endsWith("1")) {
                } else if (type.endsWith("3")) {
                    Element eVerdict = (Element) eOperation.getElementsByTagName("Verdict").item(0);
                    operation.setLogin(eOperation.getElementsByTagName("Login").item(0).getTextContent());
                    operation.setComments(eOperation.getElementsByTagName("Comments").item(0).getTextContent());
                    operation.setWorkstation(eOperation.getElementsByTagName("Workstation").item(0).getTextContent());
                    // System.out.println("Type " + eVerdict.getAttribute("Value") + " Text " +
                    // eVerdict.getTextContent());
                    operation.createVerdict(eVerdict.getAttribute("Value"), eVerdict.getAttribute("Value"));
                }
            }
        }
        return items;
    }

    public static Element getElement(NodeList nodeList, int i) {
        Node node = nodeList.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            return (Element) node;
        }
        return null;
    }

    public static void convertXmlFileCargo(String baseDir, String trkId) {
        try {
            logDirectory = new File("").getCanonicalPath() + "\\\\logs";
        } catch (IOException e) {
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);
        }
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        dataForm = new DataForm();
        try {

            File xmlSourceFile = new File(baseDir + "\\" + trkId, trkId + ".xml");
            if (!xmlSourceFile.exists()) {
                throw new XMLFileException(String.format("File %s not found", xmlSourceFile.getAbsolutePath()));
            }
            ;

            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(xmlSourceFile);
            document.getDocumentElement().normalize();
            // System.out.println("Root Element :" +
            // document.getDocumentElement().getNodeName() + " " +
            // document.getDocumentElement().getNodeType()+ " " +
            // document.getDocumentElement().getNodeValue());
            // System.out.println("------");

            NodeList nlFormMan = document.getElementsByTagName("FormMan");
            Element eFormMan = getElement(nlFormMan, 0);

            NodeList nlDataForm = eFormMan.getElementsByTagName("DataForm");
            int nlDataFormItems = setDataFormValues(nlDataForm);

            if (nlDataFormItems == 0) {
                try {
                    LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Invalid XML File (FormData)", 2);
                    throw new XMLFileException("Invalid XML File");
                } catch (WriteLogFileException e) {
                    System.err.println(
                            "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                    System.exit(10);
                }
            }
            Element eDataForm = getElement(nlDataForm, 0);
            NodeList nlAdminData = eDataForm.getElementsByTagName("AdminData");
            int nlAdminDataItems = setAdminDataValues(nlAdminData);
            if (nlAdminDataItems == 0) {
                try {
                    LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Invalid XML File (AdminData)", 2);
                    throw new XMLFileException("Invalid XML File");
                } catch (WriteLogFileException e) {
                    System.err.println(
                            "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                    System.exit(10);
                }
            }

            /*
             * 
             * AdminData
             * 
             */
            Element eAdminData = getElement(nlAdminData, 0);

            NodeList nlVehicle = eAdminData.getElementsByTagName("Vehicle");
            // int nlVehicleItems = setVehicleValues(nlVehicle);
            // AdminData adminData = dataForm.getAdminData(); //Somente uma entrada

            Element eVehicle = getElement(nlVehicle, 0);
            // Vehicle vehicle = dataForm.getVehicle(); //Somente uma entrada

            NodeList nlTrailers = eVehicle.getElementsByTagName("Trailers");
            // int nlTrailersItems = setTrailersValues(nlTrailers);
            Element eTrailers = getElement(nlTrailers, 0); // Somente uma entrada
            // Trailers trailers = dataForm.getTrailers();

            NodeList nlTrailer = eTrailers.getElementsByTagName("Trailer");
            int nlTrailerItems = setTrailerValues(nlTrailer);

            for (int nlTrailerItem = 0; nlTrailerItem < nlTrailerItems; nlTrailerItem++) {
                // Element eTrailer = getElement(nlTrailer, nlTrailerItem);

                // NodeList nlContainers = eTrailer.getElementsByTagName("Containers");
                // int nlContainersItems = setContainersValues(nlContainers, nlTrailerItem);
                // Element eContainers = getElement(nlContainers, 0);
                // NodeList nlContainer = eContainers.getElementsByTagName("Container");

                // int nlContainerItems = setContainerValues(nlContainer, nlTrailerItem);
            }

            /*
             * 
             * ProcessInstructions
             * 
             */

            NodeList nlProcessInstructions = eDataForm.getElementsByTagName("ProcessInstructions");
            setProcessInstructionsValues(nlProcessInstructions);

            /*
             * 
             * Operations
             * 
             */

            NodeList nlOperations = eDataForm.getElementsByTagName("Operations");
            setOperationsValues(nlOperations);
            Element eOperations = getElement(nlOperations, 0);

            NodeList nlOperation = eOperations.getElementsByTagName("Operation");
            setOperationValues(nlOperation);

            File xmlTargetFile = new File(baseDir + "\\" + trkId, trkId + "_converted.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(DataForm.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(dataForm, xmlTargetFile);// this line create customer.xml file in specified path.

            StringWriter sw = new StringWriter();
            jaxbMarshaller.marshal(dataForm, sw);
            // String xmlString = sw.toString();
        } catch (XMLFileException | IOException | JAXBException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
    }

}
