package com.example;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener implements ApplicationListener<OrderEvent> {
    @Override
    public void onApplicationEvent(OrderEvent orderEvent) {
        //当监听到泛型指定的事件触发的时候，就会来调用该方法
        System.out.println("订单状态改变了" + orderEvent.getSource());
    }
}
