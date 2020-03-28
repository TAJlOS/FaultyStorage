import Task.*;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * This class is used to executing tasks {@link Task} that the {@link TaskManager}
 * gives for migrating data from old storage to new storage
 * @author Ivan Petrov
 */
public class TaskExecutor implements Runnable {
    private TaskManager taskManager;
    private final int SOCKET_TIMEOUT = 100;
    private static final Logger logger = LogManager.getLogger(TaskExecutor.class);

    public TaskExecutor(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void run() {
        while (!taskManager.isComplete()){
            Task task = taskManager.nextTask();
            if(task != null){
                try {
                    if(task.getType() == TaskType.DOWNLOAD_TASK){
                        executeDownloadTask(((DownloadTask) task));
                    }else if(task.getType() == TaskType.UPLOAD_TASK){
                        executeUploadTask(((UploadTask) task));
                    }else if(task.getType() == TaskType.DELETION_TASK){
                        executeDeletionTask(((DeletionTask) task));
                    }
                }catch (IOException e){
                    logger.error(e.getMessage(),e);
                    return;
                }

            }
        }
    }

    private void executeDeletionTask(DeletionTask task) throws IOException{
        if(task != null){
            try{
                int httpStatus = RequestManager.deleteFileFromOldStorage(task);
                 /*
                 the task is marked completed if the request's HttpStatus equals 200 (OK)
                 or the file wasn't found (HttpStatus 404) that could happened
                 if the file was already deleted at one of the previous requests,
                 but due to exceeding socket timeout the program did not wait for the result
                 of the server response and the task was returned to the queue

                 in all other cases the task is returned back to the queue
                 */
                if(httpStatus == HttpStatus.SC_OK || httpStatus == HttpStatus.SC_NOT_FOUND){
                   taskManager.incCompletedTaskCount();
                    System.out.println("Файл "+task.getFilename()+" удалён со старого хранилища");
                    logger.info("The file "+task.getFilename()+" deleted from old storage");
                }else{
                    taskManager.addTask(task);
                }
            }catch (SocketTimeoutException e){
                logger.info(e.getMessage(),e);
                taskManager.addTask(task);
            }catch (IOException e){
                throw e;
            }
        }
    }

    private void executeUploadTask(UploadTask task) throws IOException{
        if(task != null){
            try{
                int httpStatus = RequestManager.postFileToNewStorage(task);
                /*
                 the task is marked completed if the request's HttpStatus equals 200 (OK)
                 or the file was already uploaded to new storage (HttpStatus 409) that could
                 happened if the file was already uploaded to new storage at one of the previous
                 requests, but due to exceeding socket timeout the program did not wait for the result
                 of the server response and the task was returned to the queue

                 in all other cases the task is returned back to the queue
                 */
                if(httpStatus == HttpStatus.SC_OK || httpStatus == HttpStatus.SC_CONFLICT){
                    taskManager.incCompletedTaskCount();
                    taskManager.addTask(new DeletionTask(task.getFilename(),SOCKET_TIMEOUT));
                    System.out.println("Файл "+task.getFilename()+" загружен в новое хранилище");
                    logger.info("The file  "+task.getFilename()+" saved to new storage");
                }else{
                    taskManager.addTask(task);
                }
            }catch (SocketTimeoutException e) {
                logger.info(e.getMessage(),e);
                taskManager.addTask(task);
            }catch (IOException e){
                throw e;
            }
        }
    }

    private void executeDownloadTask(DownloadTask task) throws IOException{
        if(task != null){
            try {
                HttpResponse response = RequestManager.getFileFromOldStorage(task);
                int httpStatus = response.getStatusLine().getStatusCode();
                if(httpStatus == HttpStatus.SC_OK){
                    System.out.println("Файл "+task.getFilename()+" скачан со старого хранилища");
                    logger.info("The file "+task.getFilename()+" loaded from old storage");
                    String fileData = EntityUtils.toString(response.getEntity());
                    taskManager.addTask(new UploadTask(task.getFilename(),fileData,SOCKET_TIMEOUT));
                    taskManager.incCompletedTaskCount();
                }else{
                    taskManager.addTask(task);
                }
            }catch (HttpResponseException | SocketTimeoutException e){
                logger.info(e.getMessage(),e);
                taskManager.addTask(task);
            }catch (IOException e){
                throw e;
            }
        }
    }

}
