package Task;

public class UploadTask extends Task{
    private String filename;
    private String fileData;

    public UploadTask(String filename, String fileData, int socketTimeout) {
        super(TaskType.UPLOAD_TASK, socketTimeout);
        this.filename = filename;
        this.fileData = fileData;
    }

    public String getFilename() {
        return filename;
    }

    public String getFileData() {
        return fileData;
    }

}
