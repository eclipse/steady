package org.eclipse.steady.kb.task;

import java.util.HashMap;
import org.eclipse.steady.backend.BackendConnector;
import org.eclipse.steady.kb.command.Command;
import org.eclipse.steady.kb.model.Vulnerability;

/**
 * Execute Command tasks
 */
public interface Task {
  /**
   * command name the task belongs to
   *
   * @return name of command
   */
  Command.NAME getCommandName();

  /**
   * Task Executor
   *
   * @param vuln a {@link org.eclipse.steady.kb.model.Vulnerability} object
   * @param args a {@link java.util.HashMap} object
   * @param backendConnector a {@link org.eclipse.steady.backend.BackendConnector} object
   * @throws java.lang.Exception if any.
   */
  void execute(Vulnerability vuln, HashMap<String, Object> args, BackendConnector backendConnector)
      throws Exception;
}
