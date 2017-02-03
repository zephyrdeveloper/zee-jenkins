package com.thed;

import com.thed.service.soap.*;
import com.thed.zephyr.jenkins.model.TestCaseResultModel;
import com.thed.zephyr.jenkins.model.ZephyrConfigModel;
import com.thed.zephyr.jenkins.model.ZephyrInstance;
import com.thed.zephyr.jenkins.reporter.ZeeConstants;
import com.thed.zephyr.jenkins.utils.ZephyrSoapClient;
import com.thed.zephyr.jenkins.utils.rest.Project;
import com.thed.zephyr.jenkins.utils.rest.RestClient;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
import java.lang.Exception;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static com.thed.zephyr.jenkins.reporter.ZeeConstants.*;
import static com.thed.zephyr.jenkins.reporter.ZeeConstants.TEST_CASE_TAG;

/**
 * Junit testcase for {@link com.thed.zephyr.jenkins.utils.ZephyrSoapClient}
 *
 * #1 Get token
 * #2 Create test case
 */
public class ZephyrSoapClientTest  {

    private static ZephyrSoapService client;
    private static final QName SERVICE_NAME = new QName("http://soap.service.thed.com/", "ZephyrSoapService");
    private static String ZEPHYR_URL = "http://localhost:8081/flex/services/soap/zephyrsoapservice-v1?wsdl";

    static String token = null;


    @BeforeClass
    public static void  setUp() throws MalformedURLException {
        URL wsdlURL = new URL(ZEPHYR_URL);
        ZephyrSoapService_Service ss = new ZephyrSoapService_Service(wsdlURL, SERVICE_NAME);
        client = ss.getZephyrSoapServiceImplPort();

        try {
            token = client.login("test.manager","test.manager");
            System.out.println("inside setup");
        }
        catch (ZephyrServiceException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getZephyrRestVersion() {

        RestClient restClient = null;
        Map<Long, String> projects;
        try {
            restClient = getRestclient("https://demo.yourzephyr.com");

            Map<Long, String> aa = Project.getAllProjects(restClient, "v1");
            System.out.print("test");

        }
        catch (Exception e){

        }
    }


        private RestClient getRestclient(String serverAddress) {

            RestClient restClient = new RestClient(serverAddress, "test.manager", "test.manager");
            return restClient;
        }

    /**
     * Verify test case creation, Make sure releaseId and testcase tree id's are updated accordingly
     * verify testcase in UI appeared accodingly.
     * @throws ZephyrServiceException
     */
    @Test
    public void testCreateTestCase() throws ZephyrServiceException {
        RemoteTestcase testcase = new RemoteTestcase();
        RemoteRepositoryTree tree = new RemoteRepositoryTree();
        tree.setReleaseId(6l);  //Hardocde Ids'
        tree.setId(16l);
        RemoteRepositoryTreeTestcase treeTestcase = new RemoteRepositoryTreeTestcase();
        testcase.setName("Jenkins - Mobile Login");
        testcase.setReleaseId(tree.getReleaseId());
        treeTestcase.setRemoteRepositoryId(tree.getId());
        treeTestcase.setTestcase(testcase);
        List<RemoteFieldValue> response = client.createNewTestcase(treeTestcase, token);
    }


    @Test
    /**
     * Create testcase, cycle , cycle phase, and upload results as execution.
     */
    public void testUploadResults() throws ZephyrServiceException, DatatypeConfigurationException {
        ZephyrSoapClient client = new ZephyrSoapClient();

        ZephyrConfigModel zephyrData = new ZephyrConfigModel();
        ZephyrInstance selectedZephyrServer = new ZephyrInstance();
        selectedZephyrServer.setUsername("test.manager");
        selectedZephyrServer.setPassword("test.manager");
        selectedZephyrServer.setServerAddress(ZEPHYR_URL);

        zephyrData.setSelectedZephyrServer(selectedZephyrServer);

        zephyrData.setCyclePrefix("Auto - ");
        zephyrData.setReleaseId(1l);
        zephyrData.setZephyrProjectId(1l);
        zephyrData.setCycleDuration("30 days");
        zephyrData.setTestcases(getTestcases());
        zephyrData.setCycleId(ZeeConstants.NEW_CYCLE_KEY_IDENTIFIER);

        client.uploadTestResults(zephyrData);


        //List<RemoteFieldValue> response = client.createNewCycle(zephyrData, token);

    }

    private List<TestCaseResultModel> getTestcases() {
        List<TestCaseResultModel> testcases = new ArrayList<TestCaseResultModel>();

        RemoteTestcase testcase = new RemoteTestcase();
        testcase.setName("from API");
        testcase.setComments(TEST_CASE_COMMENT);
        testcase.setAutomated(AUTOMATED);
        testcase.setExternalId(EXTERNAL_ID);
        testcase.setPriority(TEST_CASE_PRIORITY);
        testcase.setTag(TEST_CASE_TAG);

        TestCaseResultModel caseWithStatus = new TestCaseResultModel();
        caseWithStatus.setPassed(true);
        caseWithStatus.setRemoteTestcase(testcase);
        testcases.add(caseWithStatus);

        return testcases;
    }




}
