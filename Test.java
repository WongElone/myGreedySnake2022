package com.greedysnakeproject;

import java.util.ArrayList;

public class Test {
    public static void main(String[] args) {
        ArrayList<Integer> lst = new ArrayList<>();
        lst.add(0);
        lst.add(1);
        lst.add(2);
        lst.add(3);
        lst.remove(2);
        System.out.println(lst.size());
        System.out.println(lst);
        System.out.println(lst.get(2));
    }
}
