import Task.DeletionTask;
import Task.DownloadTask;
import Task.*;

import java.util.LinkedList;
import java.util.List;

/**
 * The class is used to management tasks {@link Task} for migrating files from old storage to new storage
 * by take tasks to executors {@link TaskExecutor}
 * After a successful complete a task the executor {@link TaskExecutor} must inform about it using call the
 * {@link #incCompletedTaskCount()} method or inform the {@link TaskManager} that the task wasn't completed using
 * call the {@link #addTask(Task)} method
 *
 * @author Ivan Petrov
 */

public class TaskManager {
    private final int BASIC_SOCKET_TIMEOUT = 100;
    private LinkedList<Task> taskList;
    private int completedTaskCount;
    private int totalTaskCount;


    public TaskManager(List<String> downloadQueue, List<String> deletionQueue) {

        taskList = new LinkedList<Task>();

        if(downloadQueue != null){
            for(String filename: downloadQueue){
                this.taskList.add(new DownloadTask(filename, BASIC_SOCKET_TIMEOUT));
            }
        }

        if(deletionQueue != null){
            for(String filename: deletionQueue){
                this.taskList.add(new DeletionTask(filename, BASIC_SOCKET_TIMEOUT));
            }
        }

        /*
        because every file need to download from old storage and then
        upload to new storage and then delete from old storage so
        total task numbers is 3 * number files for download
         */
        totalTaskCount = downloadQueue != null ? downloadQueue.size()*3 : 0;

        /*
        because can exist some files were uploaded to new storage
        but weren't deleted from old storage so need to increase total number
        of tasks
         */
        totalTaskCount+= deletionQueue != null ? deletionQueue.size() : 0;

        completedTaskCount = 0;
    }

    public synchronized boolean isComplete(){
        return completedTaskCount == totalTaskCount;
    }

    public synchronized Task nextTask(){
        Task task = null;
        if(isComplete()) return null;
        if(taskList.size() != 0){
            task = taskList.poll();
        }
        return task;
    }

    public synchronized void addTask(Task task){
        taskList.addFirst(task);
    }

    public synchronized void incCompletedTaskCount(){
        completedTaskCount++;
    }

}
