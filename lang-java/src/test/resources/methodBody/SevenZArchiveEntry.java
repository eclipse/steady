package org.apache.commons.compress.archivers.sevenz;

import java.util.*;
import org.apache.commons.compress.archivers.*;

public class SevenZArchiveEntry implements ArchiveEntry {
  private String name;
  private boolean hasStream;
  private boolean isDirectory;
  private boolean isAntiItem;
  private boolean hasCreationDate;
  private boolean hasLastModifiedDate;
  private boolean hasAccessDate;
  private long creationDate;
  private long lastModifiedDate;
  private long accessDate;
  private boolean hasWindowsAttributes;
  private int windowsAttributes;
  private boolean hasCrc;
  private long crc;
  private long compressedCrc;
  private long size;
  private long compressedSize;
  private Iterable<? extends SevenZMethodConfiguration> contentMethods;

  public SevenZArchiveEntry() {
    super();
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public boolean hasStream() {
    return hasStream;
  }

  public void setHasStream(final boolean hasStream) {
    this.hasStream = hasStream;
  }

  public boolean isDirectory() {
    return isDirectory;
  }

  public void setDirectory(final boolean isDirectory) {
    this.isDirectory = isDirectory;
  }

  public boolean isAntiItem() {
    return isAntiItem;
  }

  public void setAntiItem(final boolean isAntiItem) {
    this.isAntiItem = isAntiItem;
  }

  public boolean getHasCreationDate() {
    return hasCreationDate;
  }

  public void setHasCreationDate(final boolean hasCreationDate) {
    this.hasCreationDate = hasCreationDate;
  }

  public Date getCreationDate() {
    if (hasCreationDate) {
      return ntfsTimeToJavaTime(creationDate);
    }
    throw new UnsupportedOperationException("The entry doesn't have this timestamp");
  }

  public void setCreationDate(final long ntfsCreationDate) {
    creationDate = ntfsCreationDate;
  }

  public void setCreationDate(final Date creationDate) {
    hasCreationDate = (creationDate != null);
    if (hasCreationDate) {
      this.creationDate = javaTimeToNtfsTime(creationDate);
    }
  }

  public boolean getHasLastModifiedDate() {
    return hasLastModifiedDate;
  }

  public void setHasLastModifiedDate(final boolean hasLastModifiedDate) {
    this.hasLastModifiedDate = hasLastModifiedDate;
  }

  public Date getLastModifiedDate() {
    if (hasLastModifiedDate) {
      return ntfsTimeToJavaTime(lastModifiedDate);
    }
    throw new UnsupportedOperationException("The entry doesn't have this timestamp");
  }

  public void setLastModifiedDate(final long ntfsLastModifiedDate) {
    lastModifiedDate = ntfsLastModifiedDate;
  }

  public void setLastModifiedDate(final Date lastModifiedDate) {
    hasLastModifiedDate = (lastModifiedDate != null);
    if (hasLastModifiedDate) {
      this.lastModifiedDate = javaTimeToNtfsTime(lastModifiedDate);
    }
  }

  public boolean getHasAccessDate() {
    return hasAccessDate;
  }

  public void setHasAccessDate(final boolean hasAcessDate) {
    hasAccessDate = hasAcessDate;
  }

  public Date getAccessDate() {
    if (hasAccessDate) {
      return ntfsTimeToJavaTime(accessDate);
    }
    throw new UnsupportedOperationException("The entry doesn't have this timestamp");
  }

  public void setAccessDate(final long ntfsAccessDate) {
    accessDate = ntfsAccessDate;
  }

  public void setAccessDate(final Date accessDate) {
    hasAccessDate = (accessDate != null);
    if (hasAccessDate) {
      this.accessDate = javaTimeToNtfsTime(accessDate);
    }
  }

  public boolean getHasWindowsAttributes() {
    return hasWindowsAttributes;
  }

  public void setHasWindowsAttributes(final boolean hasWindowsAttributes) {
    this.hasWindowsAttributes = hasWindowsAttributes;
  }

  public int getWindowsAttributes() {
    return windowsAttributes;
  }

  public void setWindowsAttributes(final int windowsAttributes) {
    this.windowsAttributes = windowsAttributes;
  }

  public boolean getHasCrc() {
    return hasCrc;
  }

  public void setHasCrc(final boolean hasCrc) {
    this.hasCrc = hasCrc;
  }

  @Deprecated
  public int getCrc() {
    return (int) crc;
  }

  @Deprecated
  public void setCrc(final int crc) {
    this.crc = crc;
  }

  public long getCrcValue() {
    return crc;
  }

  public void setCrcValue(final long crc) {
    this.crc = crc;
  }

  @Deprecated
  int getCompressedCrc() {
    return (int) compressedCrc;
  }

  @Deprecated
  void setCompressedCrc(final int crc) {
    compressedCrc = crc;
  }

  long getCompressedCrcValue() {
    return compressedCrc;
  }

  void setCompressedCrcValue(final long crc) {
    compressedCrc = crc;
  }

  public long getSize() {
    return size;
  }

  public void setSize(final long size) {
    this.size = size;
  }

  long getCompressedSize() {
    return compressedSize;
  }

  void setCompressedSize(final long size) {
    compressedSize = size;
  }

  public void setContentMethods(final Iterable<? extends SevenZMethodConfiguration> methods) {
    if (methods != null) {
      final LinkedList<SevenZMethodConfiguration> l = new LinkedList<SevenZMethodConfiguration>();
      for (final SevenZMethodConfiguration m : methods) {
        l.addLast(m);
      }
      contentMethods =
          (Iterable<? extends SevenZMethodConfiguration>) Collections.unmodifiableList((List<?>) l);
    } else {
      contentMethods = null;
    }
  }

  public void FileNameUtil(Map<String, String> uncompressSuffix, String defaultExtension) {
    int a = 0;
  }

  public Iterable<? extends SevenZMethodConfiguration> getContentMethods() {
    return contentMethods;
  }

  public static Date ntfsTimeToJavaTime(final long ntfsTime) {
    final Calendar ntfsEpoch = Calendar.getInstance();
    ntfsEpoch.setTimeZone(TimeZone.getTimeZone("GMT+0"));
    ntfsEpoch.set(1601, 0, 1, 0, 0, 0);
    ntfsEpoch.set(14, 0);
    final long realTime = ntfsEpoch.getTimeInMillis() + ntfsTime / 10000L;
    return new Date(realTime);
  }

  public static long javaTimeToNtfsTime(final Date date) {
    final Calendar ntfsEpoch = Calendar.getInstance();
    ntfsEpoch.setTimeZone(TimeZone.getTimeZone("GMT+0"));
    ntfsEpoch.set(1601, 0, 1, 0, 0, 0);
    ntfsEpoch.set(14, 0);
    return (date.getTime() - ntfsEpoch.getTimeInMillis()) * 1000L * 10L;
  }
}
