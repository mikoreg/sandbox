/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.github.mikoreg.sandbox;

import static java.lang.Math.max;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author m1k0
 */
public class Multirepl {
    public static void main(String[] args) {
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
        Multirepl multirepl = new Multirepl(iteractions, secondMin, secondMax);
        scanner.nextLine();
    }

    public Multirepl(int iteractions, int secondMin, int secondMax) {
        Producer producer = new Producer(secondMin, secondMax);
        int errorsCount = 0;
        for (int i = 0; i < iteractions; i++) {
            Factors czynniki = producer.next();
            int a = czynniki.a();
            int b = czynniki.b();
            int iloczyn = a * b;
            int rnd = ThreadLocalRandom.current().nextInt(2);
            boolean error = false;
            do {
                int percent = i*100/iteractions;
                System.out.print("["+percent+"%] \t ");
                final int expected;
                switch(rnd) {
                    case 1: {
                        expected = iloczyn;
                        System.out.print(b + " * " + a + " = ");
                        break;
                    }
                    case 2: {
                        expected = b;
                        System.out.print(iloczyn + " / " + a + " = ");
                        break;
                    }
                    case 3: {
                        expected = a;
                        System.out.print(iloczyn + " / " + b + " = ");
                        break;
                    }
                    default: {
                        expected = iloczyn;
                        System.out.print(a + " * " + b + " = ");
                    }
                }

                Scanner scanner = new Scanner(System.in);

                String input = scanner.nextLine();
                try {
                    int wynik = Integer.parseInt(input.trim());
                    if (wynik != expected) {
                        error = true;
                        errorsCount++;
                    } else {
                        error = false;
                    }
                } catch (NumberFormatException e) {
                    error = true;
                    errorsCount++;
                }
                if (error) {
                    System.out.println("Błąd! Wynik poprawny to " + expected);
                }
            } while (error);
        }
        if (errorsCount == 0) {
            System.out.println("Gratulacje, zero błędów");
        } else {
            System.out.println("Błędnych odpowiedzi " + errorsCount + " na " + iteractions + " przykładów");
        }
    }

    record Factors(int a, int b) {

    }

    class Producer {

        Set<Factors> all = Set.of();
        Stack<Factors> remaining = new Stack<>();

        public Producer(int secondMin, int secondMax) {
            Set<Factors> numbers = new LinkedHashSet<>();
            for (int a = 2; a <= 9; a++) {
                for (int b = secondMin; b <=secondMax; b++) {
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
