package com.coalesce.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Streams {
    public static <T> Stream<T> reverse(Stream<T> stream) {
        List<T> list = stream.collect(Collectors.toList());
        List<T> reversed = new LinkedList<>();
        for (int i = list.size()-1; i >= 0; i--) {
            reversed.add(list.get(i));
        }
        return reversed.stream();
    }
}
