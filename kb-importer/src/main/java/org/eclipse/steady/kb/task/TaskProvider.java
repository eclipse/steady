package org.eclipse.steady.kb.task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import org.eclipse.steady.kb.command.Command;

public class TaskProvider {
  private static TaskProvider instance = null;
  private static List<Task> importTasks = new ArrayList<Task>();

  private TaskProvider() {}

  public static synchronized TaskProvider getInstance() {
    if (instance == null) {
      return new TaskProvider();
    }
    return instance;
  }

  public List<Task> getTasks(Command.NAME commandName) {
    if (!importTasks.isEmpty()) {
      return importTasks;
    }

    ServiceLoader<Task> serviceProviders = ServiceLoader.load(Task.class);
    Iterator<Task> iterator = serviceProviders.iterator();
    while (iterator.hasNext()) {
      Task task = iterator.next();
      if (task.getCommandName().equals(Command.NAME.IMPORT)) {
        importTasks.add(task);
      }
    }
    return importTasks;
  }
}
