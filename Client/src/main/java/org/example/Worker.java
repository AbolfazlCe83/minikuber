package org.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Worker {

    public static int idNumber = 1;
    private Socket socket;
    private String id;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private int MAX_TASK_NUMBER = 4;
    private static ArrayList<Worker> workers = new ArrayList<>();
    private ArrayList<Task> myTasks;


    public Worker(String host, int port, String id) throws IOException {
        socket = new Socket(host, port);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        this.myTasks = new ArrayList<>();
        this.id = id;
    }

    public static void addWorker(Worker w) {
        workers.add(w);
    }

    public void run() throws IOException {
        System.out.println(dataInputStream.readUTF());
    }

    public static Worker getFreeWorker() {
        for (Worker worker : workers)
            if (worker.getMAX_TASK_NUMBER() > 0)
                return worker;
        return null;
    }

    public int getMAX_TASK_NUMBER() {
        return MAX_TASK_NUMBER;
    }

    public String getId() {
        return id;
    }

    public void decreaseTaskNumber() {
        this.MAX_TASK_NUMBER -= 1;
    }

    public void addTask(Task task) {
        this.myTasks.add(task);
    }

    public Task getLastTask() {
        return this.myTasks.get(this.myTasks.size() - 1);
    }

    public Socket getSocket() {
        return socket;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

}
