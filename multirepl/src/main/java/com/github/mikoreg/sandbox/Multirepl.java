/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.github.mikoreg.sandbox;

import java.io.IOException;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;
import static java.util.stream.Collectors.joining;

/**
 *
 * @author m1k0
 */
public class Multirepl {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome in MultiREPL");
        System.out.print("Enter the number of examples: ");
        int iteractions = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter minimum factor: ");
        int secondMin = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter maximum factor: ");
        int secondMax = Integer.parseInt(scanner.nextLine().trim());
        if (secondMin > secondMax) {
            System.out.println("Entered data is corrupted");
            System.exit(-1);
        }
        LocalTime startTime = LocalTime.now();
        Multirepl multirepl = new Multirepl();
        Collection<String> errors = multirepl.train(secondMin, secondMax, iteractions);
        Path history = Path.of(System.getProperty("java.io.tmpdir"), "multirepl.history.txt");
        String collect = errors.stream().collect(joining("\n"));
        Duration testDuration = Duration.between(startTime, LocalTime.now());
        collect = collect.concat("\nDuration: " + testDuration.toString() + "\n\n");
        Files.write(history, collect.getBytes(UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        System.out.println("history is saved in file " + history);
        scanner.nextLine();
    }

    public Collection<String> train(int secondMin, int secondMax, int iteractions) {
        Producer producer = new Producer(secondMin, secondMax);
        LocalTime startTime = LocalTime.now();
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < iteractions; i++) {
            Factors czynniki = producer.next();
            int a = czynniki.a();
            int b = czynniki.b();
            int iloczyn = a * b;
            int rnd = ThreadLocalRandom.current().nextInt(4);
            boolean error = false;
            do {
                int percent = i * 100 / iteractions;
                final String question;
                final int expected;
                switch (rnd) {
                    case 1: {
                        expected = iloczyn;
                        question = b + " * " + a + " = ";
                        break;
                    }
                    case 2: {
                        expected = b;
                        question = iloczyn + " / " + a + " = ";
                        break;
                    }
                    case 3: {
                        expected = a;
                        question = iloczyn + " / " + b + " = ";
                        break;
                    }
                    default: {
                        expected = iloczyn;
                        question = a + " * " + b + " = ";
                    }
                }
                final Duration between = Duration.between(startTime, LocalTime.now());
                String duration = String.format("%02d:%02d", between.toMinutesPart(), between.toSecondsPart());
                System.out.print(duration + " [" + percent + "%] \t ");
                System.out.print(question);

                Scanner scanner = new Scanner(System.in);

                String input = scanner.nextLine().trim();
                try {
                    int wynik = Integer.parseInt(input);
                    if (wynik != expected) {
                        error = true;
                    } else {
                        error = false;
                    }
                } catch (NumberFormatException e) {
                    error = true;
                }
                if (error) {
                    System.out.println("Błąd! Wynik poprawny to " + expected);
                    errors.add(question + " " + input);
                }
            } while (error);
        }
        if (errors.isEmpty()) {
            System.out.println("Gratulacje, zero błędów");
        } else {
            System.out.println("Błędnych odpowiedzi " + errors.size() + " na " + iteractions + " przykładów");
        }
        return errors;
    }

    record Factors(int a, int b) {

    }

    class Producer {

        Set<Factors> all = Set.of();
        Stack<Factors> remaining = new Stack<>();

        public Producer(int secondMin, int secondMax) {
            Set<Factors> numbers = new LinkedHashSet<>();
            for (int a = 2; a <= 9; a++) {
                for (int b = secondMin; b <= secondMax; b++) {
                    numbers.add(new Factors(min(a, b), max(a, b)));
                }
            }
            List<Factors> list = new ArrayList<>(numbers);
            Collections.shuffle(list);
            all = new LinkedHashSet<>(list);
        }

        public Factors next() {
            if (remaining.isEmpty()) {
                restoreRemaining();
            }
            return remaining.pop();
        }

        private void restoreRemaining() {
            remaining.addAll(all);
            Collections.shuffle(remaining);
        }
    }
}
