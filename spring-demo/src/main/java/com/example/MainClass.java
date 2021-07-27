package com.example;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainClass {
    public static void main(String[] args) {
        ApplicationContext ac = new AnnotationConfigApplicationContext(MyConfig.class);
        //发布事件
        ac.publishEvent(new OrderEvent(new Order(),"订单状态改变了"));
    }
}
