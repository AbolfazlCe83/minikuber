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

    public static Task getTaskByName(String taskName) {
        for (Task task : pendingTasks)
            if (task.getName().equals(taskName))
                return task;
        for (Task task : runningTasks)
            if (task.getName().equals(taskName))
                return task;
        return null;
    }

    public static ArrayList<Task> getRunningTasks() {
        return runningTasks;
    }

    public static ArrayList<Task> getPendingTasks() {
        return pendingTasks;
    }

    public String getWorkerId() {
        return workerId;
    }

    public static void removeTask(String taskName) {
        for (Task task : pendingTasks)
            if (task.getName().equals(taskName)) {
                pendingTasks.remove(task);
                return;
            }
        for (Task task : runningTasks)
            if (task.getName().equals(taskName)) {
                runningTasks.remove(task);
                return;
            }
    }

    public static void removePendingTask(Task task) {
        pendingTasks.remove(task);
    }
}
