/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.backend.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.enums.PathSource;

/**
 * <p>TouchPoint class.</p>
 *
 */
@Embeddable
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(
    ignoreUnknown =
        true) // On allowGetters: https://github.com/FasterXML/jackson-databind/issues/95
public class TouchPoint {

  public enum Direction {
    A2L,
    L2A
  };

  @ManyToOne(
      optional = false,
      cascade = {},
      fetch = FetchType.EAGER)
  @JoinColumn(name = "fromConstructId") // Required for the unique constraint
  private ConstructId from;

  @ManyToOne(
      optional = false,
      cascade = {},
      fetch = FetchType.EAGER)
  @JoinColumn(name = "toConstructId") // Required for the unique constraint
  private ConstructId to;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Direction direction;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private PathSource source;

  /**
   * <p>Constructor for TouchPoint.</p>
   */
  public TouchPoint() {
    super();
  }

  /**
   * <p>Constructor for TouchPoint.</p>
   *
   * @param from a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
   * @param to a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
   * @param direction a {@link com.sap.psr.vulas.backend.model.TouchPoint.Direction} object.
   */
  public TouchPoint(ConstructId from, ConstructId to, Direction direction) {
    super();
    this.from = from;
    this.to = to;
    this.direction = direction;
  }

  /**
   * <p>Getter for the field <code>from</code>.</p>
   *
   * @return a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
   */
  public ConstructId getFrom() {
    return from;
  }
  /**
   * <p>Setter for the field <code>from</code>.</p>
   *
   * @param from a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
   */
  public void setFrom(ConstructId from) {
    this.from = from;
  }

  /**
   * <p>Getter for the field <code>to</code>.</p>
   *
   * @return a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
   */
  public ConstructId getTo() {
    return to;
  }
  /**
   * <p>Setter for the field <code>to</code>.</p>
   *
   * @param to a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
   */
  public void setTo(ConstructId to) {
    this.to = to;
  }

  /**
   * <p>Getter for the field <code>direction</code>.</p>
   *
   * @return a {@link com.sap.psr.vulas.backend.model.TouchPoint.Direction} object.
   */
  public Direction getDirection() {
    return direction;
  }
  /**
   * <p>Setter for the field <code>direction</code>.</p>
   *
   * @param direction a {@link com.sap.psr.vulas.backend.model.TouchPoint.Direction} object.
   */
  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  /**
   * <p>Getter for the field <code>source</code>.</p>
   *
   * @return a {@link com.sap.psr.vulas.shared.enums.PathSource} object.
   */
  public PathSource getSource() {
    return source;
  }
  /**
   * <p>Setter for the field <code>source</code>.</p>
   *
   * @param source a {@link com.sap.psr.vulas.shared.enums.PathSource} object.
   */
  public void setSource(PathSource source) {
    this.source = source;
  }

  /** {@inheritDoc} */
  @Override
  public final String toString() {
    final StringBuilder builder = new StringBuilder();
    builder
        .append("[from=")
        .append(this.getFrom().getQname())
        .append(", to=")
        .append(this.getTo().getQname())
        .append("]");
    return builder.toString();
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 11 * hash + (this.from != null ? this.from.hashCode() : 0);
    hash = 11 * hash + (this.to != null ? this.to.hashCode() : 0);
    hash = 11 * hash + (this.direction != null ? this.direction.hashCode() : 0);
    hash = 11 * hash + (this.source != null ? this.source.hashCode() : 0);
    return hash;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final TouchPoint other = (TouchPoint) obj;
    if (this.from != other.from && (this.from == null || !this.from.equals(other.from))) {
      return false;
    }
    if (this.to != other.to && (this.to == null || !this.to.equals(other.to))) {
      return false;
    }
    if (this.direction != other.direction) {
      return false;
    }
    if (this.source != other.source) {
      return false;
    }
    return true;
  }
}
