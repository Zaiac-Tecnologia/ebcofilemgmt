package br.com.zaiac.ebcofilemgmt.rest;

import br.com.zaiac.ebcofilemgmt.exception.SendInformationException;
import br.com.zaiac.ebcofilemgmt.tools.Constants;
import br.com.zaiac.ebcofilemgmt.tools.SendFiles;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

public class MethodPost {
    
    public static void httpPing(String obj, String url) throws Exception {
        String apiKey = SendFiles.convertStringToBase64(Constants.API_KEY);
        String restUrl = url + "/v2/monitor/ping";
        try {
            HttpPost req = new HttpPost(restUrl);
            req.addHeader("Content-Type", "application/json; charset=UTF-8");        
            req.addHeader("Authorization", apiKey);
            req.setEntity(new StringEntity(obj, "UTF-8"));                
            HttpClient client = HttpClientBuilder.create().build();        
            HttpResponse response  = client.execute(req);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new SendInformationException(String.format("Request Error: Return Code: %d", response.getStatusLine().getStatusCode()));
            }
        } catch (IOException e) {
            throw new Exception(String.format("Error accessing backend %s. %s", restUrl, e.toString()));
        } catch (SendInformationException e) {
            throw new SendInformationException(e.toString());
        } catch (Exception e) {
            throw new Exception(String.format("Error not recognized %s. %s", restUrl, e.toString()));
        }
    }
    
    
}
