package com.notamethod.fluppy.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Classe partagée singleton
public class EventBus {
    private static final Map<String, List<Runnable>> listeners = new HashMap<>();

    public static void subscribe(String event, Runnable handler) {
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(handler);
    }

    public static void publish(String event) {
        listeners.getOrDefault(event, List.of()).forEach(Runnable::run);
    }
}