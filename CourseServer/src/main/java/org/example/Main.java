package org.example;


import java.util.HashMap;

public class Main {     // поток ведущего
    public static void main(String[] args) {
        System.out.println("Program started");
        new Server().run();
    }
}