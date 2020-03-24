import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;


import java.io.IOException;
import java.net.SocketTimeoutException;

import java.util.List;



public class DownloadManager  implements Runnable{
    private FileStorage fileStorage;
    private List<String> filenames;
    private final String BASE_URL = "http://localhost:8080/oldStorage/files/";
    public DownloadManager(FileStorage fileStorage, List<String> filenames) {
        this.fileStorage = fileStorage;
        this.filenames = filenames;
    }

    public void run() {
        int i = 1;
        for(String filename: filenames){
            try {
                String fileData = getFileData(filename);
                System.out.println("Файл "+i+" скачан");
                fileStorage.put(new File(filename,fileData));
                System.out.println("Файл "+i+" помещен в хранилище");
                deleteFile(filename);
                System.out.println("Файл "+i+" удалён");
            }catch (IOException e){
                System.out.println(e);
                return;
            }
            i++;

        }
    }


    private String getFileData(String filename) throws IOException {
        int httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        String fileData = null;
        while (httpStatus != HttpStatus.SC_OK){
            try {
                HttpResponse response = Request.Get(BASE_URL + filename)
                        .execute()
                        .returnResponse();
                httpStatus = response.getStatusLine().getStatusCode();
                fileData = EntityUtils.toString(response.getEntity());
            }catch (SocketTimeoutException e){
                System.out.println(e);
            }
        }
        return fileData;
    }

    private void deleteFile(String filename) throws IOException {
        int httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        while (httpStatus != HttpStatus.SC_OK){
            try{
                HttpResponse response = Request.Delete(BASE_URL + filename)
                        .execute()
                        .returnResponse();
                httpStatus = response.getStatusLine().getStatusCode();
            }catch (SocketTimeoutException e){
                System.out.println(e);
            }

        }
    }
}
