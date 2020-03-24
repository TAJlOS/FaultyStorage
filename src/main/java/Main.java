import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String BASE_URL = "http://localhost:8080/oldStorage/files/";
    public static void main(String[] args) throws InterruptedException {
        FileStorage fileStorage = new FileStorage();
        List<String> filenamesList = getFilenamesList();
        DownloadManager downloadManager = new DownloadManager(fileStorage, filenamesList);
        UploadManager uploadManager = new UploadManager(fileStorage, filenamesList);
        Thread downloadThread = new Thread(downloadManager);
        Thread uploadThread = new Thread(uploadManager);
        downloadThread.start();
        uploadThread.start();
        downloadThread.join();
        uploadThread.join();
    }

    private static String getFilenames() throws IOException {
        int httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        String fileData = null;
        while (httpStatus != HttpStatus.SC_OK){
            HttpResponse response = Request.Get(BASE_URL).execute().returnResponse();
            httpStatus = response.getStatusLine().getStatusCode();
            fileData = EntityUtils.toString(response.getEntity());
        }
        return fileData;
    }

    private static List<String> getFilenamesList() {
        while (true){
            try{
                String result = getFilenames();
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(result, ArrayList.class);
            }catch (ClientProtocolException e){
                System.out.println(e);
            }
            catch(IOException e){
                System.out.println(e);
            }
        }

    }
}
