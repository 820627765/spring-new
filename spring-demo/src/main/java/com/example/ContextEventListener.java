package com.example;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 如下是基于注解的监听器
 */
@Component
public class ContextEventListener {
    //表示监听 ContextStartedEvent 事件
    @EventListener(ContextStartedEvent.class)
    public void onApplicationEvent(ContextStartedEvent event){
        System.out.println("ContextStartedEvent 事件监听被执行");
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent event){
        System.out.println("ContextRefreshedEvent 事件监听被执行");
    }
}
