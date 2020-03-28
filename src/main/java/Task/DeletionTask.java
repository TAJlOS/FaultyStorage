package Task;

public class DeletionTask extends Task{
    private String filename;

    public DeletionTask(String filename, int socketTimeout) {
        super(TaskType.DELETION_TASK, socketTimeout);
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
