package org.example;

import com.google.gson.Gson;

import java.util.ArrayList;

public class Task {
    private String name;
    private String workerId;
    private String status;
    private static ArrayList<Task> pendingTasks = new ArrayList<>();
    private static ArrayList<Task> runningTasks = new ArrayList<>();

    public Task(String name, String workerId) {
        this.name = name;
        this.workerId = workerId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static void addPendingTask(Task task) {
        pendingTasks.add(task);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static void addRunningTask(Task task) {
        runningTasks.add(task);
    }

    public String getName() {
        return name;
    }

    public String getWorkerId() {
        return workerId;
    }
}
