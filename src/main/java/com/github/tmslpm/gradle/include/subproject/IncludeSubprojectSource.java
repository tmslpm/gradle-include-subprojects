package com.github.tmslpm.gradle.include.subproject;

import org.gradle.api.provider.ListProperty;

import java.util.List;

public abstract class IncludeSubprojectSource {
  /**
   * <p>
   * The name of this extension when it is added to a Gradle project.
   * </p>
   */
  public final static String NAME = "includeSubprojectSource";

  /**
   * <p>
   * Default constructor for IncludeSubprojectSource.
   * </p>
   * <ul>
   *     <li>
   *       {@code subprojectNames}: defaults  empty list,
   *     </li>
   * </ul>
   */
  public IncludeSubprojectSource() {
    this.getSubprojectNames().convention(List.of());
  }

  /**
   * <p>
   * The list of subproject name to include
   * </p>
   *
   * @return a {@link ListProperty} of {@link String} objects
   * representing the expected keys.
   */
  public abstract ListProperty<String> getSubprojectNames();

}