package com.example;

import org.springframework.context.ApplicationEvent;

public class OrderEvent extends ApplicationEvent {
    private String name;

    public OrderEvent(Object source,String name) {
        super(source);
        this.name = name;
    }
}
