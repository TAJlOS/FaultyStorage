import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * The entry point at application for migration data from old storage to new storage
 * @author Ivan Petrov
 */
public class Main {
    private static final int N_THREAD = 1;

    private static final Logger logger = LogManager.getLogger(TaskExecutor.class);

    public static void main(String[] args){
        logger.trace("The program started");
        long startTime = System.currentTimeMillis();
        try{
            TaskManager taskManager = createTaskManager();
            for (int i = 0; i < N_THREAD; i++) {
                TaskExecutor executor = new TaskExecutor(taskManager);
                Thread thread = new Thread(executor);
                thread.start();
                thread.join();
            }
        }catch (InterruptedException | IOException e){
            logger.error(e.getMessage(),e);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Время работы программы: "+(new SimpleDateFormat("mm:ss").format(new Date(endTime-startTime))));

        logger.trace("The program ended");

    }

    public static TaskManager createTaskManager() throws IOException {
        List<String> oldStorageFilenamesList =  getOldStorageFilenamesList();
        List<String> newStorageFilenamesList =  getNewStorageFilenamesList();

        List<String> deletionFileList = new ArrayList<>();
        List<String> downloadFileList = new ArrayList<>();

        HashSet<String> oldStorageSet = new HashSet<>(oldStorageFilenamesList);
        HashSet<String> newStorageSet = new HashSet<>(newStorageFilenamesList);

        /*
        due to at unexpected interruption the application some files
        could already have been migrated to new storage but wasn't deleted
        at old storage so these files must add to deletion queue
         */
        for(String file : newStorageSet){
            if(oldStorageSet.contains(file)){
                deletionFileList.add(file);
            }
        }
        /*
        moreover these files mustn't add to download queue
         */
        for(String file : oldStorageSet){
            if(!newStorageSet.contains(file)){
                downloadFileList.add(file);
            }
        }
        return new TaskManager(downloadFileList, deletionFileList);
    }

    private static List<String> getOldStorageFilenamesList() throws IOException {
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

    private static List<String> getNewStorageFilenamesList() throws IOException {
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



}
