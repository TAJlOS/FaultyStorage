import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.List;

public class UploadManager implements Runnable{
    private FileStorage fileStorage;
    private HashSet<String> filenames;
    private final String BASE_URL = "http://localhost:8080/newStorage/files/";

    public UploadManager(FileStorage fileStorage, List<String> filenames) {
        this.fileStorage = fileStorage;
        this.filenames = new HashSet<String>(filenames);
    }

    public void run() {
        int i = 1;
        while (!filenames.isEmpty()){
            try {
                File file = fileStorage.get();
                System.out.println("Файл "+i+" взят из хранилища");
                postFile(file);
                System.out.println("Файл "+i+" помещен на сервер");
                filenames.remove(file.getName());
            }catch (IOException e){
                System.out.println(e);
                return;
            }
            i++;

        }
    }

    private void postFile(File file) throws IOException {
        int httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        while (httpStatus != HttpStatus.SC_OK){
            HttpEntity entity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addBinaryBody("file", file.getData().getBytes(), ContentType.MULTIPART_FORM_DATA, file.getName())
                    .build();
            try {
                HttpResponse response = Request.Post(BASE_URL)
                        .body(entity)
                        .execute().returnResponse();
                httpStatus = response.getStatusLine().getStatusCode();
            }catch (SocketTimeoutException e){
                System.out.println(e);
            }

        }
    }
}
