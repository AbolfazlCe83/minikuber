package org.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.TreeSet;

public class Worker {

    public static int idNumber = 1;
    private Socket socket;
    private String id;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private int MAX_TASK_NUMBER = 4;
    private static ArrayList<Worker> workers = new ArrayList<>();
    private ArrayList<Task> myTasks;
    private ArrayList<String> tasks;
    private boolean active;


    public Worker(String host, int port) throws IOException {
        socket = new Socket(host, port);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        this.id = "worker" + idNumber;
        Worker.idNumber++;
        this.myTasks = new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.active = true;
    }

    public static void addWorker(Worker w) {
        workers.add(w);
    }

    public static Worker getFreeWorker() {
        for (Worker worker : workers)
            if (worker.getMAX_TASK_NUMBER() > 0 && worker.active)
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
        if (this.myTasks.size() > 0)
            return this.myTasks.get(this.myTasks.size() - 1);
        return null;
    }

    public Socket getSocket() {
        return socket;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public void removeLastTask() {
        if (myTasks.size() > 0)
            this.myTasks.remove(this.myTasks.size() - 1);
    }

    public static Worker getWorkerById(String id) {
        for (Worker worker : workers)
            if (worker.getId().equals(id))
                return worker;
        return null;
    }

    public static ArrayList<Worker> getWorkers() {
        return workers;
    }
    public void addTask(String taskName) {
        this.tasks.add(taskName);
    }

    public void removeTask(String taskName) {
        this.tasks.remove(taskName);
    }

    public void increaseTaskNumber() {
        this.MAX_TASK_NUMBER += 1;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ArrayList<Task> getTasks() {
        ArrayList<Task> tasks1 = new ArrayList<>();
        for (String task : tasks)
            tasks1.add(Task.getTaskByName(task));
        return tasks1;
    }
}
