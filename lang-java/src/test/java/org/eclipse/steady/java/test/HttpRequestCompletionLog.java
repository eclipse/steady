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
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.java.test;

/**
 * Real-world example.
 *
 */
public interface HttpRequestCompletionLog {

  /*
   * Use builder pattern for extensibility and to avoid complaints from Checkstyle (more than 7 params for a method or
   * construct is frowned upon).
   */
  class Builder {

    private final String destination;
    private final String targetPath;
    private long startTimeInMillis = -1L;
    private long durationInMillis = -1L;
    private long requestEntitySizeInBytes = -1L;
    private long responseEntitySizeInBytes = -1L;
    private int responseStatus = -1;
    private String responseReasonPhrase;
    private String responseContentType;

    public static Builder forTarget(final String destination, final String targetPath) {
      return new Builder(destination, targetPath);
    }

    public Builder(final String destination, final String targetPath) {
      this.destination = destination;
      this.targetPath = targetPath;
    }

    public Builder withStartTimeInMillis(final long startTimeInMillis) {
      this.startTimeInMillis = startTimeInMillis;
      return this;
    }

    public Builder withDurationInMillis(final long durationInMillis) {
      this.durationInMillis = durationInMillis;
      return this;
    }

    public Builder withRequestEntitySizeInBytes(final long requestEntitySizeInBytes) {
      this.requestEntitySizeInBytes = requestEntitySizeInBytes;
      return this;
    }

    public Builder withResponseEntitySizeInBytes(final long responseEntitySizeInBytes) {
      this.responseEntitySizeInBytes = responseEntitySizeInBytes;
      return this;
    }

    public Builder withResponseStatus(final int responseStatus) {
      this.responseStatus = responseStatus;
      return this;
    }

    public Builder withResponseReasonPhrase(final String responseReasonPhrase) {
      this.responseReasonPhrase = responseReasonPhrase;
      return this;
    }

    public Builder withResponseContentType(final String responseContentType) {
      this.responseContentType = responseContentType;
      return this;
    }

    public HttpRequestCompletionLog build() {
      /*
       * Avoid side-effects when reusing the builder, e.g., for tests
       */
      final String destination = this.destination;
      final String targetPath = this.targetPath;
      final long startTimeInMillis = this.startTimeInMillis;
      final long durationInMillis = this.durationInMillis;
      final long requestEntitySizeInBytes = this.requestEntitySizeInBytes;
      final long responseEntitySizeInBytes = this.responseEntitySizeInBytes;
      final int responseStatus = this.responseStatus;
      final String responseReasonPhrase = this.responseReasonPhrase;
      final String responseContentType = this.responseContentType;

      return new HttpRequestCompletionLog() {

        @SuppressWarnings("nls")
        @Override
        public String toString() {
          return "HttpRequestCompletionLog [destination="
              + destination
              + ", targetPath="
              + targetPath
              + ", startTimeInMillis="
              + startTimeInMillis
              + ", durationInMillis="
              + durationInMillis
              + ", requestEntitySizeInBytes="
              + requestEntitySizeInBytes
              + ", responseEntitySizeInBytes="
              + responseEntitySizeInBytes
              + ", responseStatus="
              + responseStatus
              + ", responseReasonPhrase="
              + responseReasonPhrase
              + ", responseContentType="
              + responseContentType
              + "]";
        }

        @Override
        public String getDestination() {
          return destination;
        }

        @Override
        public String getTargetPath() {
          return targetPath;
        }

        @Override
        public long getStartTimeInMillis() {
          return startTimeInMillis;
        }

        @Override
        public long getDurationInMillis() {
          return durationInMillis;
        }

        @Override
        public long getRequestEntitySizeInBytes() {
          return requestEntitySizeInBytes;
        }

        @Override
        public long getResponseEntitySizeInBytes() {
          return responseEntitySizeInBytes;
        }

        @Override
        public int getResponseStatus() {
          return responseStatus;
        }

        @Override
        public String getResponseReasonPhrase() {
          return responseReasonPhrase;
        }

        @Override
        public String getResponseContentType() {
          return responseContentType;
        }
      };
    }
  }

  String getDestination();

  String getTargetPath();

  long getStartTimeInMillis();

  long getDurationInMillis();

  long getRequestEntitySizeInBytes();

  long getResponseEntitySizeInBytes();

  int getResponseStatus();

  String getResponseReasonPhrase();

  String getResponseContentType();
}
