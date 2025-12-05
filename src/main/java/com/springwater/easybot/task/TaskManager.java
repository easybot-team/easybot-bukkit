package com.springwater.easybot.task;

import java.util.*;
import java.util.concurrent.*;

public class TaskManager {

    // 存储任务名和对应的任务信息
    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    // 存储任务的执行间隔（秒）
    private final Map<String, Long> taskIntervals = new HashMap<>();
    // 存储每个任务的Runnable（业务逻辑）
    private final Map<String, Runnable> taskRunnables = new HashMap<>();

    // 添加任务，任务名、间隔时间（秒）和自定义的Runnable任务
    public void addTask(String taskName, long intervalSeconds, Runnable taskRunnable) {
        // 如果任务已存在，则先停止当前任务
        if (tasks.containsKey(taskName)) {
            stopTask(taskName);
        }

        // 记录任务的执行间隔
        taskIntervals.put(taskName, intervalSeconds);
        taskRunnables.put(taskName, taskRunnable);

        // 创建并调度任务
        Runnable task = createTask(taskName);
        ScheduledFuture<?> scheduledTask = scheduler.scheduleWithFixedDelay(task, 0, intervalSeconds, TimeUnit.SECONDS);
        tasks.put(taskName, scheduledTask);
    }

    // 创建任务的具体执行逻辑
    private Runnable createTask(String taskName) {
        return () -> {
            try {
                // 执行用户传入的自定义任务
                Runnable taskRunnable = taskRunnables.get(taskName);
                if (taskRunnable != null) {
                    taskRunnable.run();
                } else {
                    System.err.println("任务 " + taskName + " 的Runnable未定义！");
                }
            } catch (Exception e) {
                // 任务出错时，不做处理，按要求忽略
                System.err.println("任务 " + taskName + " 执行失败: " + e.getMessage());
            }
        };
    }

    // 手动触发任务，触发后重置计时器
    public void triggerTask(String taskName) {
        if (!tasks.containsKey(taskName)) {
            System.out.println("任务 " + taskName + " 不存在！");
            return;
        }

        // 重置定时器，手动触发任务
        long interval = taskIntervals.get(taskName);
        stopTask(taskName);
        addTask(taskName, interval, taskRunnables.get(taskName));
    }

    // 停止单个任务
    private void stopTask(String taskName) {
        ScheduledFuture<?> scheduledTask = tasks.get(taskName);
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            tasks.remove(taskName);
            taskRunnables.remove(taskName);
        }
    }

    // 重新开始所有任务
    public void restartAllTasks() {
        for (String taskName : tasks.keySet()) {
            long interval = taskIntervals.get(taskName);
            stopTask(taskName);
            addTask(taskName, interval, taskRunnables.get(taskName));
        }
    }

    // 暂停所有任务
    public void pauseAllTasks() {
        for (String taskName : tasks.keySet()) {
            stopTask(taskName);
        }
    }

    // 清空所有任务
    public void clearAllTasks() {
        for (String taskName : tasks.keySet()) {
            stopTask(taskName);
        }
        tasks.clear();
        taskIntervals.clear();
        taskRunnables.clear();
    }
}
