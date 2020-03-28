package Task;

public class DownloadTask extends Task{
    private String filename;

    public DownloadTask(String filename, int socketTimeout) {
        super(TaskType.DOWNLOAD_TASK, socketTimeout);
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

}
