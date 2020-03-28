
import Task.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.IOException;


/**
 * This class is used to sending HTTP requests to server API
 * @author Ivan Petrov
 */

public class RequestManager {
    private static final String OLD_STORAGE_URL = "http://localhost:8080/oldStorage/files/";
    private static final String NEW_STORAGE_URL = "http://localhost:8080/newStorage/files/";

    public static HttpResponse getFileFromOldStorage(DownloadTask task) throws IOException {
        return Request.Get(OLD_STORAGE_URL + task.getFilename())
                .socketTimeout(task.getSocketTimeout())
                .execute()
                .returnResponse();
    }

    public static int postFileToNewStorage(UploadTask task) throws IOException {
        HttpEntity entity = MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .addBinaryBody("file",
                        task.getFileData().getBytes(),
                        ContentType.MULTIPART_FORM_DATA,
                        task.getFilename())
                .build();

        HttpResponse response = Request.Post(NEW_STORAGE_URL)
                .socketTimeout(task.getSocketTimeout())
                .body(entity)
                .execute().returnResponse();

        return response.getStatusLine().getStatusCode();
    }

    public static int deleteFileFromOldStorage(DeletionTask task) throws IOException{
        HttpResponse response = Request.Delete(OLD_STORAGE_URL + task.getFilename())
                    .socketTimeout(task.getSocketTimeout())
                    .execute()
                    .returnResponse();

        return response.getStatusLine().getStatusCode();
    }

    public static int deleteFileFromNewStorage(DeletionTask task) throws IOException{
        HttpResponse response = Request.Delete(NEW_STORAGE_URL + task.getFilename())
                .socketTimeout(task.getSocketTimeout())
                .execute()
                .returnResponse();

        return response.getStatusLine().getStatusCode();
    }



    public static HttpResponse getOldStorageFilenames() throws IOException {
        return  Request.Get(OLD_STORAGE_URL)
                .execute()
                .returnResponse();
    }

    public static HttpResponse getNewStorageFilenames() throws IOException {
        return  Request.Get(NEW_STORAGE_URL)
                        .execute()
                        .returnResponse();
    }

}
