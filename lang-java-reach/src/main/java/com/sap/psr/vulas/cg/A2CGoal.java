package com.sap.psr.vulas.cg;

import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.enums.PathSource;
import com.sap.psr.vulas.shared.util.ConstructIdUtil;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** A2CGoal class. */
public class A2CGoal extends AbstractReachGoal {

  private static final Log log = LogFactory.getLog(A2CGoal.class);

  private Set<com.sap.psr.vulas.shared.json.model.ConstructId> entryPoints = null;

  /** Constructor for A2CGoal. */
  public A2CGoal() {
    super(GoalType.A2C);
  }

  /**
   * Getter for the field <code>entryPoints</code>.
   *
   * @return a {@link java.util.Set} object.
   */
  protected final Set<com.sap.psr.vulas.shared.json.model.ConstructId> getEntryPoints() {
    if (this.entryPoints == null) {
      // Filter app constructs (if requested)
      final String[] filter =
          this.getConfiguration()
              .getConfiguration()
              .getStringArray(ReachabilityConfiguration.REACH_CONSTR_FILTER);
      if (filter != null && filter.length > 0 && !(filter.length == 1 && filter[0].equals(""))) {
        this.entryPoints = ConstructIdUtil.filterWithRegex(this.getAppConstructs(), filter);
      } else {
        this.entryPoints = this.getAppConstructs();
      }
    }
    return this.entryPoints;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Sets the application constructs as entry points of the {@link ReachabilityAnalyzer}.
   */
  protected final void setEntryPoints(ReachabilityAnalyzer _ra) {
    _ra.setEntryPoints(
        this.getEntryPoints(),
        PathSource.A2C,
        this.getConfiguration()
            .getConfiguration()
            .getBoolean(ReachabilityConfiguration.REACH_EXIT_UNKOWN_EP, false));
  }
}
