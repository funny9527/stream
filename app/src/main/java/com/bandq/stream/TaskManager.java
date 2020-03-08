package com.bandq.stream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskManager {

	private static ExecutorService service = Executors.newFixedThreadPool(5);
	
	public static void addTask(Runnable task) {
		service.submit(task);
	}
	
	public static void close() {
		service.shutdown();
	}
}
