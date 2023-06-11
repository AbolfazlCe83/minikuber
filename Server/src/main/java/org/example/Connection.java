package org.example;

import com.google.gson.JsonSyntaxException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;

public class Connection extends Thread {
    Socket socket;
    final DataInputStream dataInputStream;
    final DataOutputStream dataOutputStream;

    public Connection(Socket socket) throws IOException {
        System.out.println("New connection form: " + socket.getInetAddress() + ":" + socket.getPort());
        this.socket = socket;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public synchronized void run() {
        try {
            String clientType;
            while ((clientType = get_intro()).equals("unknown")) ;
            if (clientType.equals("client")) {
                while (true) {
                    handelClient();
                }
            } else {
                Worker worker = new Worker("localhost", 5050);
                Worker.addWorker(worker);
                while (true) {
                    handelWorker();
                }
            }
        } catch (IOException e) {
            System.out.println("Connection " + socket.getInetAddress() + ":" + socket.getPort() + " lost!");
        }
    }

    private synchronized void handelWorker() throws IOException {
        if (Master.getCurrentWorker() != null && Master.getCurrentWorker().getLastTask() != null) {
            String data = Master.getCurrentWorker().getLastTask().toJson();
            Master.getCurrentWorker().removeLastTask();
            dataOutputStream.writeUTF(data);
        }
    }

    private synchronized void handelClient() throws IOException {
        String data = dataInputStream.readUTF();
        Matcher matcher;
        try {
            if ((matcher = Commands.getMatcher(data, Commands.CREATE_TASK)) != null) {
                String taskName = matcher.group("taskName");
                Worker worker = Worker.getFreeWorker();
                if (Task.getTaskByName(taskName) != null)
                    dataOutputStream.writeUTF("This task already exists!");
                else if (worker == null) {
                    createPendingTask(taskName);
                    dataOutputStream.writeUTF("No workers available at this moment!");
                } else {
                    createRunningTask(worker, taskName);
                    dataOutputStream.writeUTF("Your task(" + taskName + ") is being performed by the (" + worker.getId() + ")");
                }
            } else if ((matcher = Commands.getMatcher(data, Commands.CREATE_SPECIAL_TASK)) != null) {
                String taskName = matcher.group("taskName");
                String workerId = matcher.group("workerId");
                if (Task.getTaskByName(taskName) != null)
                    dataOutputStream.writeUTF("This task already exists!");
                else if (Worker.getWorkerById(workerId) == null)
                    dataOutputStream.writeUTF("There is no worker with this id!");
                else if (!Worker.getWorkerById(workerId).isActive())
                    dataOutputStream.writeUTF("the worker is inactive!");
                else if (Worker.getWorkerById(workerId).getMAX_TASK_NUMBER() <= 0) {
                    createPendingTask(taskName, workerId);
                    dataOutputStream.writeUTF("The worker you want is busy!");
                } else {
                    createRunningTask(Worker.getWorkerById(workerId), taskName);
                    dataOutputStream.writeUTF("Your task(" + taskName + ") is being performed by the (" + workerId + ")");
                }
            } else if (Commands.getMatcher(data, Commands.GET_TASKS) != null) {
                String taskDetails = getTaskDetails();
                if (taskDetails.equals(""))
                    dataOutputStream.writeUTF("There is no task at this moment!");
                else dataOutputStream.writeUTF(taskDetails);
            } else if (Commands.getMatcher(data, Commands.GET_NODES) != null) {
                String workerDetails = getWorkerDetails();
                if (workerDetails.equals(""))
                    dataOutputStream.writeUTF("There is no worker at this moment!");
                else dataOutputStream.writeUTF(workerDetails);
            } else if ((matcher = Commands.getMatcher(data, Commands.DELETE_TASK)) != null) {
                String taskName = matcher.group("taskName");
                if (Task.getTaskByName(taskName) == null)
                    dataOutputStream.writeUTF("There is no task with this name!");
                else {
                    String workerId = removeTask(taskName);
                    if (workerId.equals(""))
                        dataOutputStream.writeUTF("task: (" + taskName + ") successfully deleted!");
                    else dataOutputStream.writeUTF("task: (" + taskName + ") successfully deleted!\n" +
                            "Your pending task is being performed by the (" + workerId + ")");
                }
            } else if ((matcher = Commands.getMatcher(data, Commands.CORDON)) != null) {
                String workerId = matcher.group("workerName");
                Worker worker = Worker.getWorkerById(workerId);
                if (worker == null)
                    dataOutputStream.writeUTF("there is not any worker with this id!");
                else if (!worker.isActive())
                    dataOutputStream.writeUTF("the worker is already inactive!");
                else {
                    String tasks = deactivate(worker);
                    if (tasks.equals("worker tasks:\n"))
                        dataOutputStream.writeUTF("the worker haven't any task!");
                    else dataOutputStream.writeUTF(tasks);
                }
            }
        } catch (JsonSyntaxException e) {
            dataOutputStream.writeUTF("400: Missing topic or value or command fields.");
        }
    }

    private String deactivate(Worker worker) {
        worker.setActive(false);
        ArrayList<Task> tasks = worker.getTasks();
        String taskDetails = "worker tasks:\n";
        for (Task task : tasks)
            taskDetails += task.getName() + "\n";
        for (Task task : tasks) {
            createRunningTask(Worker.getFreeWorker(), task.getName());
            Task.removeTask(task.getName());
        }
        return taskDetails;
    }

    private void createPendingTask(String taskName, String workerId) {
        Task task = new Task(taskName, workerId);
        task.setStatus("pending");
        Task.addPendingTask(task);
    }

    private String removeTask(String taskName) {
        Task task = Task.getTaskByName(taskName);
        Worker worker = Worker.getWorkerById(task.getWorkerId());
        worker.removeTask(taskName);
        worker.increaseTaskNumber();
        Task.removeTask(taskName);
        return checkPendingTasks();
    }

    private String checkPendingTasks() {
        ArrayList<Task> pendingTasks = Task.getPendingTasks();
        if (pendingTasks.size() > 0) {
            Task firstTask = pendingTasks.get(0);
            Worker worker = null;
            if (firstTask.getWorkerId().equals("0"))
                worker = Worker.getFreeWorker();
            else if (Worker.getWorkerById(firstTask.getWorkerId()).getMAX_TASK_NUMBER() > 0)
                worker = Worker.getWorkerById(firstTask.getWorkerId());
            if (worker != null) {
                Task.removePendingTask(firstTask);
                createRunningTask(worker, firstTask.getName());
                return worker.getId();
            }
        }
        return "";
    }

    private String getWorkerDetails() {
        String details = "";
        ArrayList<Worker> workers = Worker.getWorkers();
        if (workers.size() > 0) {
            for (Worker worker : workers)
                details += "worker id: <" + worker.getId() + ">/ ip address: <" + worker.getSocket().getInetAddress() +
                        ">/ port: <" + worker.getSocket().getPort() + ">\n";
        }
        return details;
    }

    private String getTaskDetails() {
        String details = "";
        ArrayList<Task> runningTask = Task.getRunningTasks();
        ArrayList<Task> pendingTasks = Task.getPendingTasks();
        if (runningTask.size() > 0) {
            for (Task task : runningTask)
                details += "task name: <" + task.getName() + ">/ status: <running>/ worker id: <" + task.getWorkerId() + ">\n";
        }
        if (pendingTasks.size() > 0) {
            for (Task task : pendingTasks)
                details += "task name: <" + task.getName() + ">/ status: <pending>\n";
        }
        return details;
    }


    private void createRunningTask(Worker worker, String taskName) {
        Task task = new Task(taskName, worker.getId());
        task.setStatus("running");
        Task.addRunningTask(task);
        worker.decreaseTaskNumber();
        worker.addTask(task);
        worker.addTask(taskName);
        Master.setCurrentWorker(worker);
    }

    private void createPendingTask(String taskName) {
        Task task = new Task(taskName, "0");
        task.setStatus("pending");
        Task.addPendingTask(task);
    }

    private String get_intro() throws IOException {
        String intro = dataInputStream.readUTF();
        switch (intro) {
            case "client" -> {
                dataOutputStream.writeUTF("200: You are registered as a client.");
                return "client";
            }
            case "worker" -> {
                dataOutputStream.writeUTF("200: Your id is:" + Worker.idNumber);
                return "worker";
            }
            default -> {
                dataOutputStream.writeUTF("400: Invalid intro");
                return "unknown";
            }
        }
    }

}
