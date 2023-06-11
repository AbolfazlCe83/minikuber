package org.example;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost", 5050);
        client.run();
    }
}