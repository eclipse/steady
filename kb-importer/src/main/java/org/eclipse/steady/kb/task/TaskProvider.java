package org.eclipse.steady.kb.task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import org.eclipse.steady.kb.command.Command;

/**
 * <p>TaskProvider class.</p>
 *
 */
public class TaskProvider {
  private static TaskProvider instance = null;
  private static List<Task> importTasks = new ArrayList<Task>();

  private TaskProvider() {}

  /**
   * <p>Getter for the field <code>instance</code>.</p>
   *
   * @return a {@link org.eclipse.steady.kb.task.TaskProvider} object
   */
  public static synchronized TaskProvider getInstance() {
    if (instance == null) {
      return new TaskProvider();
    }
    return instance;
  }

  /**
   * <p>getTasks.</p>
   *
   * @param commandName a {@link org.eclipse.steady.kb.command.Command.NAME} object
   * @return a {@link java.util.List} object
   */
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
