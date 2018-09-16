package com.pavelsklenar.portscanner;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Value("${threads.count:20}")
    private int threadsCount;

    @Override
    public Executor getAsyncExecutor() {
        return Executors.newFixedThreadPool(threadsCount);
    }

}
