package Task;


public abstract class Task {
    private TaskType type;
    private int socketTimeout;

    public Task(TaskType type, int socketTimeout) {
        this.type = type;
        this.socketTimeout = socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public TaskType getType() {
        return type;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }
}
