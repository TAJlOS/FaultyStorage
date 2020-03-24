public class FileStorage {
    private File file;
    private Boolean flag;

    public FileStorage() {
        flag = true;
    }

    public synchronized void put(File file){
        while (!flag) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(e);
                return;
            }
        }
        flag = false;
        this.file = file;
        notifyAll();
    }

    public synchronized File get(){
        while (flag) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(e);
                return null;
            }
        }
        flag = true;
        notifyAll();
        return file;
    }
}
