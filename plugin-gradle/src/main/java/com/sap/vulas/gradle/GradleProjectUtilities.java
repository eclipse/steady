package com.sap.vulas.gradle;

import static com.sap.vulas.gradle.VulasPluginCommon.*;

import java.util.HashMap;
import java.util.Map;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

public class GradleProjectUtilities {

  private static final Map<String, ProjectOutputTypes> knownPlugins;

  static {
    knownPlugins = new HashMap<>();
    knownPlugins.put("java", ProjectOutputTypes.JAR);
    knownPlugins.put("com.android.library", ProjectOutputTypes.AAR);
    knownPlugins.put("com.android.application", ProjectOutputTypes.APK);
  }

  protected static String getMandatoryProjectProperty(
      Project project, GradleGavProperty property, Logger logger) {

    final String propertyName = property.name();

    logger.debug("Looking for property [{}] in project", propertyName);

    String propertyValue = null;

    if (project.hasProperty(propertyName)) {
      propertyValue = project.getProperties().get(propertyName).toString();
    }

    if (propertyValue == null || propertyValue.isEmpty() || propertyValue.equals("undefined")) {
      logger.error("Property [{}] is not defined, please define it!", propertyName);
      throw new GradleException();
    }

    logger.debug("Property found: {}={}", propertyName, propertyValue);

    return propertyValue;
  }

  protected static ProjectOutputTypes determineProjectOutputType(Project project, Logger logger) {

    ProjectOutputTypes projectOutputType = null;

    for (String kp : knownPlugins.keySet()) {
      if (project.getPlugins().hasPlugin(kp)) {
        logger.quiet("Found plugin: " + kp);
        projectOutputType = knownPlugins.get(kp);
        break;
      }
    }

    if (projectOutputType != null) {
      logger.quiet("Project type determined: {}", projectOutputType.toString());
    }

    return projectOutputType;
  }

  protected static boolean hasKnownProjectOutputType(Project project, Logger logger) {
    return determineProjectOutputType(project, logger) != null;
  }
}
