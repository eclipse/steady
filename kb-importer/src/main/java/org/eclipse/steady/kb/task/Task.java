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
   */
  void execute(Vulnerability vuln, HashMap<String, Object> args, BackendConnector backendConnector)
      throws Exception;
}
