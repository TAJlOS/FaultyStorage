import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Util {
    public static List<String> getOldStorageFilenamesList() throws IOException {
        int httpStatusCodeOldStorage = 0;
        HttpResponse responseOldStorage = null;
        while (httpStatusCodeOldStorage != HttpStatus.SC_OK){
            responseOldStorage = RequestManager.getOldStorageFilenames();
            httpStatusCodeOldStorage = responseOldStorage.getStatusLine().getStatusCode();
        }

        String oldStorageFilenames = EntityUtils.toString(responseOldStorage.getEntity());

        ObjectMapper mapper = new ObjectMapper();
        return   mapper.readValue(oldStorageFilenames, ArrayList.class);
    }

    public static List<String> getNewStorageFilenamesList() throws IOException {
        int httpStatusCodeNewStorage = 0;
        HttpResponse responseNewStorage = null;
        while (httpStatusCodeNewStorage != HttpStatus.SC_OK){
            responseNewStorage = RequestManager.getNewStorageFilenames();
            httpStatusCodeNewStorage = responseNewStorage.getStatusLine().getStatusCode();
        }

        String newStorageFilenames = EntityUtils.toString(responseNewStorage.getEntity());
        ObjectMapper mapper = new ObjectMapper();
        return   mapper.readValue(newStorageFilenames, ArrayList.class);
    }

    public static String generateFilenameNotContainsInStorage(List<String> storageFilenamesList){
        // generate random filename not contains in storage
        char[] filename = new char[100];
        Random r = new Random();
        do{
            for(int i = 0; i < 100;i++){
                filename[i] = (char)(r.nextInt(26) + 'a');
            }
        }while (storageFilenamesList.contains(new String(filename)));
        return new String(filename);
    }

}
