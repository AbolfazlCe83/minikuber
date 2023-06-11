package org.example;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;

public class Client {
    private Socket socket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;

    private ArrayList<String> taskNames;

    public Client(String host, int port) throws IOException {
        socket = new Socket(host, port);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        this.taskNames = new ArrayList<>();
    }

    public void run() throws IOException {
        System.out.println("The user interface is running...");
        System.out.println("please specify your client type: [client/worker]");
        String clientType = "";
        Scanner scanner = new Scanner(System.in);
        while (!clientType.equals("worker") && !clientType.equals("client")) {
            clientType = scanner.nextLine();
            if (clientType.equals("worker") || clientType.equals("client")) {

            } else System.out.println("invalid client type!");
        }
        dataOutputStream.writeUTF(clientType);
        String data = dataInputStream.readUTF();
        System.out.println(data);
        if (clientType.equals("worker")) {
            String id = data.substring(data.length() - 1);
            Worker worker = new Worker("localhost", 5050, "worker" + id);
            Worker.addWorker(worker);
            while (true)
                handleWorker(worker);
        } else {
            System.out.println("Enter your tasks and commands...");
            while (true)
                handleClient(scanner);
        }

    }

    private void handleWorker(Worker worker) throws IOException {
        String data = dataInputStream.readUTF();
        Task task = new Gson().fromJson(data, Task.class);
        if (task.getWorkerId().equals(worker.getId()))
            System.out.println("You are running task:(" + task.getName() + ")");
    }

    private void handleClient(Scanner scanner) throws IOException {
        while (true) {
            String input = scanner.nextLine();
            Matcher matcher;
            if ((matcher = Commands.getMatcher(input, Commands.CREATE_TASK)) != null) {
                this.taskNames.add(matcher.group("taskName"));
                dataOutputStream.writeUTF(input);
                System.out.println(dataInputStream.readUTF());
            } else if ((matcher = Commands.getMatcher(input, Commands.CREATE_SPECIAL_TASK)) != null) {
                this.taskNames.add(matcher.group("taskName"));
                dataOutputStream.writeUTF(input);
                System.out.println(dataInputStream.readUTF());
            } else if ((Commands.getMatcher(input, Commands.GET_TASKS)) != null) {
                dataOutputStream.writeUTF(input);
                System.out.println(dataInputStream.readUTF());
            } else if ((Commands.getMatcher(input, Commands.GET_NODES)) != null) {
                dataOutputStream.writeUTF(input);
                System.out.println(dataInputStream.readUTF());
            } else if ((Commands.getMatcher(input, Commands.DELETE_TASK)) != null) {
                dataOutputStream.writeUTF(input);
                System.out.println(dataInputStream.readUTF());
            } else if ((Commands.getMatcher(input, Commands.CORDON)) != null) {
                dataOutputStream.writeUTF(input);
                System.out.println(dataInputStream.readUTF());
            } else System.out.println("invalid command!");
        }
    }
}
