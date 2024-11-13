package com.github.tmslpm.gradle.include.subproject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.AbstractCopyTask;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Main implements Plugin<Object> {
  public static final String PLUGIN_IDENTIFIER = "gradle-include-subproject-source";
  public static final Logger LOGGER = Logging.getLogger(Main.class);

  @Override
  public void apply(@NotNull Object target) {
    if (target instanceof Project project) {
      this.applyFrom(project);
    } else {
      LOGGER.error(String.format(
          "Failed to apply plugin '%s': Unsupported target type '%s'. "
          + "This plugin can only be applied to a Gradle Project (build.gradle).",
          PLUGIN_IDENTIFIER,
          target.getClass().getName()
      ));
    }
  }

  private void applyFrom(@NotNull Project project) {
    project.getExtensions().create(IncludeSubprojectSource.NAME, IncludeSubprojectSource.class);
    project.afterEvaluate(this::applyAfterEvaluateFrom);
  }

  private void applyAfterEvaluateFrom(@NotNull Project project) {
    var ext = project.getExtensions().getByType(IncludeSubprojectSource.class);

    var subprojectNamesToInclude = ext.getSubprojectNames().get();

    if (!subprojectNamesToInclude.isEmpty()) {
      project.getLogger().lifecycle(" Include subprojects");
      List<Project> listProjectToInclude = new ArrayList<>();

      for (String projectName : subprojectNamesToInclude) {
        try {
          listProjectToInclude.add(project.project(projectName));
        } catch (Exception e) {
          project.getLogger().error("Failed include subproject source", e);
        }
      }

      for (Project projectToInclude : listProjectToInclude) {
        project.subprojects(subproject -> {
          if (!subproject.getName().equals(projectToInclude.getName())) {
            this.includeSourceTo(subproject, projectToInclude);
          }
        });
      }
    }

  }

  public void includeSourceTo(@NotNull Project toProject, @NotNull Project projectToInclude) {
    final Logger logger = toProject.getLogger();
    final SourceSet sourceSet = projectToInclude.getExtensions()
        .getByType(SourceSetContainer.class)
        .getByName("main");

    // Resources
    if (!sourceSet.getResources().isEmpty()) {
      toProject
          .getTasks()
          .named("processResources", AbstractCopyTask.class)
          .configure(v -> v.from(sourceSet.getResources()));
    } else {
      logger.warn(
          " \u001B[33m!\u001B[0m No Resource found in project: {}",
          projectToInclude.getName()
      );
    }

    // Java Sources
    if (!sourceSet.getAllSource().isEmpty()) {
      toProject
          .getTasks()
          .withType(JavaCompile.class)
          .configureEach(v -> v.source(sourceSet.getAllSource()));
    } else {
      logger.warn(
          " \u001B[33m!\u001B[0m No source found in project: {}",
          projectToInclude.getName()
      );
    }

    // Implementation
    if (!toProject.getConfigurations().getByName("implementation").getDependencies().contains(projectToInclude)) {
      toProject
          .getDependencies()
          .add("implementation", toProject.project(projectToInclude.getPath()));
    } else {
      logger.warn(
          " \u001B[33m!\u001B[0m Dependency implementation already included: {}",
          projectToInclude.getName()
      );
    }

    logger.lifecycle(" \u001B[32m+\u001B[0m Added {} project sources to {} project",
        projectToInclude.getName(),
        toProject.getName()
    );
  }

}
