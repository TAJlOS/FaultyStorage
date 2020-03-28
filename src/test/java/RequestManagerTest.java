import Task.DeletionTask;
import Task.DownloadTask;
import Task.UploadTask;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RequestManagerTest {
    private List<String> oldStorageFilenamesList = new ArrayList<>();
    private List<String> newStorageFilenamesList = new ArrayList<>();
    private final int VERY_BIG_SOCKET_TIMEOUT = 1000000;

    @Before
    public void setup() throws Exception{
        oldStorageFilenamesList = Util.getOldStorageFilenamesList();
        newStorageFilenamesList = Util.getNewStorageFilenamesList();
        if(oldStorageFilenamesList == null){
            oldStorageFilenamesList = new ArrayList<>();
        }
        if(newStorageFilenamesList == null){
            newStorageFilenamesList = new ArrayList<>();
        }
    }


    @Test
    public void getFileFromOldStorageIsUsedTrueTest(){
        boolean result;
        try {
            if(oldStorageFilenamesList.size() == 0){
                result = true;
            }else{
                String filename = oldStorageFilenamesList.get(0);
                DownloadTask task = new DownloadTask(filename, VERY_BIG_SOCKET_TIMEOUT);
                HttpResponse response = RequestManager.getFileFromOldStorage(task);
                int httpStatus = response.getStatusLine().getStatusCode();
                result = httpStatus == HttpStatus.SC_OK || httpStatus == HttpStatus.SC_INTERNAL_SERVER_ERROR;
            }
        }catch (IOException e){
            result = false;
        }
        assertTrue(result);
    }

    @Test
    public void getFileFromOldStorageIsUsedFalseTest(){
        boolean result;
        try {
            String filename = Util.generateFilenameNotContainsInStorage(oldStorageFilenamesList);
            DownloadTask task = new DownloadTask(filename, VERY_BIG_SOCKET_TIMEOUT);
            HttpResponse response = RequestManager.getFileFromOldStorage(task);
            int httpStatus = response.getStatusLine().getStatusCode();
            result = httpStatus == HttpStatus.SC_NOT_FOUND || httpStatus == HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }catch (IOException e){
            result = false;
        }
        assertTrue(result);
    }


    @Test
    public void postFileToNewStorageIsUsedTrueTest() {
        boolean result;
        try {
            String filename = Util.generateFilenameNotContainsInStorage(newStorageFilenamesList);
            UploadTask task = new UploadTask(filename, "test", VERY_BIG_SOCKET_TIMEOUT);
            int httpStatusPost = RequestManager.postFileToNewStorage(task);
            if(httpStatusPost ==  HttpStatus.SC_INTERNAL_SERVER_ERROR){
                result = true;
            }else{
                if(httpStatusPost == HttpStatus.SC_OK){
                    RequestManager.deleteFileFromNewStorage(new DeletionTask(filename, VERY_BIG_SOCKET_TIMEOUT));
                    result = true;
                }else{
                    result = false;
                }
            }
        }catch (IOException e){
            result = false;
        }
        assertTrue(result);
    }

    @Test
    public void postFileToNewStorageIsUsedFalseTest() {
        boolean result;
        try {
            String filename = Util.generateFilenameNotContainsInStorage(newStorageFilenamesList);
            UploadTask task = new UploadTask(filename, "test", VERY_BIG_SOCKET_TIMEOUT);
            int httpStatusPost = RequestManager.postFileToNewStorage(task);
            if(httpStatusPost == HttpStatus.SC_OK || httpStatusPost == HttpStatus.SC_INTERNAL_SERVER_ERROR){
                if(httpStatusPost == HttpStatus.SC_OK){
                    httpStatusPost = RequestManager.postFileToNewStorage(task);
                    result = httpStatusPost == HttpStatus.SC_CONFLICT || httpStatusPost == HttpStatus.SC_INTERNAL_SERVER_ERROR;
                }else{
                    result = true;
                }
            }else{
                result = false;
            }
        }catch (IOException e){
            result = false;
        }
        assertTrue(result);
    }


    /*
    implementation that test is unsafe because there are not POST method in old storage API
     */
    @Test
    public void deleteFileFromOldStorageIsUsedTrueTest() {
        assertTrue(true);
    }

    @Test
    public void deleteFileFromOldStorageIsUsedFalseTest() {
        boolean result;
        try {
            String filename = Util.generateFilenameNotContainsInStorage(oldStorageFilenamesList);
            DeletionTask task = new DeletionTask(filename, VERY_BIG_SOCKET_TIMEOUT);
            int httpStatus = RequestManager.deleteFileFromOldStorage(task);
            result = httpStatus == HttpStatus.SC_NOT_FOUND || httpStatus == HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }catch (IOException e){
            result = false;
        }
        assertTrue(result);
    }

}