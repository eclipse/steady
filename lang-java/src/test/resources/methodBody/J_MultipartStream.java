package org.apache.commons.fileupload;

import java.io.*;
import org.apache.commons.fileupload.util.*;

public class MultipartStream {
  public static final byte CR = 13;
  public static final byte LF = 10;
  public static final byte DASH = 45;
  public static final int HEADER_PART_SIZE_MAX = 10240;
  protected static final int DEFAULT_BUFSIZE = 4096;
  protected static final byte[] HEADER_SEPARATOR;
  protected static final byte[] FIELD_SEPARATOR;
  protected static final byte[] STREAM_TERMINATOR;
  protected static final byte[] BOUNDARY_PREFIX;
  private final InputStream input;
  private int boundaryLength;
  private int keepRegion;
  private byte[] boundary;
  private final int bufSize;
  private final byte[] buffer;
  private int head;
  private int tail;
  private String headerEncoding;
  private final ProgressNotifier notifier;

  public MultipartStream() {
    this(null, null, null);
  }

  public MultipartStream(final InputStream input, final byte[] boundary, final int bufSize) {
    this(input, boundary, bufSize, null);
  }

  public MultipartStream(
      final InputStream input,
      final byte[] boundary,
      final int bufSize,
      final ProgressNotifier pNotifier) {
    if (boundary == null) {
      throw new IllegalArgumentException("boundary may not be null");
    }
    this.input = input;
    this.bufSize = bufSize;
    this.buffer = new byte[bufSize];
    this.notifier = pNotifier;
    this.boundaryLength = boundary.length + MultipartStream.BOUNDARY_PREFIX.length;
    if (bufSize < this.boundaryLength + 1) {
      throw new IllegalArgumentException(
          "The buffer size specified for the MultipartStream is too small");
    }
    this.boundary = new byte[this.boundaryLength];
    this.keepRegion = this.boundary.length;
    System.arraycopy(
        MultipartStream.BOUNDARY_PREFIX,
        0,
        this.boundary,
        0,
        MultipartStream.BOUNDARY_PREFIX.length);
    System.arraycopy(
        boundary, 0, this.boundary, MultipartStream.BOUNDARY_PREFIX.length, boundary.length);
    this.head = 0;
    this.tail = 0;
  }

  MultipartStream(
      final InputStream input, final byte[] boundary, final ProgressNotifier pNotifier) {
    this(input, boundary, 4096, pNotifier);
  }

  public MultipartStream(final InputStream input, final byte[] boundary) {
    this(input, boundary, 4096, null);
  }

  public String getHeaderEncoding() {
    return this.headerEncoding;
  }

  public void setHeaderEncoding(final String encoding) {
    this.headerEncoding = encoding;
  }

  public byte readByte() throws IOException {
    if (this.head == this.tail) {
      this.head = 0;
      this.tail = this.input.read(this.buffer, this.head, this.bufSize);
      if (this.tail == -1) {
        throw new IOException("No more data is available");
      }
      if (this.notifier != null) {
        this.notifier.noteBytesRead(this.tail);
      }
    }
    return this.buffer[this.head++];
  }

  public boolean readBoundary()
      throws FileUploadBase.FileUploadIOException, MalformedStreamException {
    final byte[] marker = new byte[2];
    boolean nextChunk = false;
    this.head += this.boundaryLength;
    try {
      marker[0] = this.readByte();
      if (marker[0] == 10) {
        return true;
      }
      marker[1] = this.readByte();
      if (arrayequals(marker, MultipartStream.STREAM_TERMINATOR, 2)) {
        nextChunk = false;
      } else {
        if (!arrayequals(marker, MultipartStream.FIELD_SEPARATOR, 2)) {
          throw new MalformedStreamException("Unexpected characters follow a boundary");
        }
        nextChunk = true;
      }
    } catch (FileUploadBase.FileUploadIOException e) {
      throw e;
    } catch (IOException e2) {
      throw new MalformedStreamException("Stream ended unexpectedly");
    }
    return nextChunk;
  }

  public void setBoundary(final byte[] boundary) throws IllegalBoundaryException {
    if (boundary.length != this.boundaryLength - MultipartStream.BOUNDARY_PREFIX.length) {
      throw new IllegalBoundaryException("The length of a boundary token can not be changed");
    }
    System.arraycopy(
        boundary, 0, this.boundary, MultipartStream.BOUNDARY_PREFIX.length, boundary.length);
  }

  public String readHeaders()
      throws FileUploadBase.FileUploadIOException, MalformedStreamException {
    int i = 0;
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int size = 0;
    while (i < MultipartStream.HEADER_SEPARATOR.length) {
      byte b;
      try {
        b = this.readByte();
      } catch (FileUploadBase.FileUploadIOException e) {
        throw e;
      } catch (IOException e2) {
        throw new MalformedStreamException("Stream ended unexpectedly");
      }
      if (++size > 10240) {
        throw new MalformedStreamException(
            String.format(
                "Header section has more than %s bytes (maybe it is not properly terminated)",
                10240));
      }
      if (b == MultipartStream.HEADER_SEPARATOR[i]) {
        ++i;
      } else {
        i = 0;
      }
      baos.write(b);
    }
    String headers = null;
    if (this.headerEncoding != null) {
      try {
        headers = baos.toString(this.headerEncoding);
      } catch (UnsupportedEncodingException e3) {
        headers = baos.toString();
      }
    } else {
      headers = baos.toString();
    }
    return headers;
  }

  public int readBodyData(final OutputStream output) throws MalformedStreamException, IOException {
    final InputStream istream = this.newInputStream();
    return (int) Streams.copy(istream, output, false);
  }

  ItemInputStream newInputStream() {
    return new ItemInputStream();
  }

  public int discardBodyData() throws MalformedStreamException, IOException {
    return this.readBodyData(null);
  }

  public boolean skipPreamble() throws IOException {
    System.arraycopy(this.boundary, 2, this.boundary, 0, this.boundary.length - 2);
    this.boundaryLength = this.boundary.length - 2;
    try {
      this.discardBodyData();
      return this.readBoundary();
    } catch (MalformedStreamException e) {
      return false;
    } finally {
      System.arraycopy(this.boundary, 0, this.boundary, 2, this.boundary.length - 2);
      this.boundaryLength = this.boundary.length;
      this.boundary[0] = 13;
      this.boundary[1] = 10;
    }
  }

  public static boolean arrayequals(final byte[] a, final byte[] b, final int count) {
    for (int i = 0; i < count; ++i) {
      if (a[i] != b[i]) {
        return false;
      }
    }
    return true;
  }

  protected int findByte(final byte value, final int pos) {
    for (int i = pos; i < this.tail; ++i) {
      if (this.buffer[i] == value) {
        return i;
      }
    }
    return -1;
  }

  protected int findSeparator() {
    int match;
    int maxpos;
    int first;
    for (match = 0, maxpos = this.tail - this.boundaryLength, first = this.head;
        first <= maxpos && match != this.boundaryLength;
        ++first) {
      first = this.findByte(this.boundary[0], first);
      if (first == -1 || first > maxpos) {
        return -1;
      }
      for (match = 1;
          match < this.boundaryLength && this.buffer[first + match] == this.boundary[match];
          ++match) {}
    }
    if (match == this.boundaryLength) {
      return first - 1;
    }
    return -1;
  }

  static /* synthetic */ int access$114(final MultipartStream x0, final long x1) {
    return x0.head += (int) x1;
  }

  static {
    HEADER_SEPARATOR = new byte[] {13, 10, 13, 10};
    FIELD_SEPARATOR = new byte[] {13, 10};
    STREAM_TERMINATOR = new byte[] {45, 45};
    BOUNDARY_PREFIX = new byte[] {13, 10, 45, 45};
  }

  public static class ProgressNotifier {
    private final ProgressListener listener;
    private final long contentLength;
    private long bytesRead;
    private int items;

    ProgressNotifier(final ProgressListener pListener, final long pContentLength) {
      this.listener = pListener;
      this.contentLength = pContentLength;
    }

    void noteBytesRead(final int pBytes) {
      this.bytesRead += pBytes;
      this.notifyListener();
    }

    void noteItem() {
      ++this.items;
      this.notifyListener();
    }

    private void notifyListener() {
      if (this.listener != null) {
        this.listener.update(this.bytesRead, this.contentLength, this.items);
      }
    }
  }

  public static class MalformedStreamException extends IOException {
    private static final long serialVersionUID = 6466926458059796677L;

    public MalformedStreamException() {}

    public MalformedStreamException(final String message) {
      super(message);
    }
  }

  public static class IllegalBoundaryException extends IOException {
    private static final long serialVersionUID = -161533165102632918L;

    public IllegalBoundaryException() {}

    public IllegalBoundaryException(final String message) {
      super(message);
    }
  }

  public class ItemInputStream extends InputStream implements Closeable {
    private long total;
    private int pad;
    private int pos;
    private boolean closed;
    private static final int BYTE_POSITIVE_OFFSET = 256;

    ItemInputStream() {
      this.findSeparator();
    }

    private void findSeparator() {
      this.pos = MultipartStream.this.findSeparator();
      if (this.pos == -1) {
        if (MultipartStream.this.tail - MultipartStream.this.head
            > MultipartStream.this.keepRegion) {
          this.pad = MultipartStream.this.keepRegion;
        } else {
          this.pad = MultipartStream.this.tail - MultipartStream.this.head;
        }
      }
    }

    public long getBytesRead() {
      return this.total;
    }

    public int available() throws IOException {
      if (this.pos == -1) {
        return MultipartStream.this.tail - MultipartStream.this.head - this.pad;
      }
      return this.pos - MultipartStream.this.head;
    }

    public int read() throws IOException {
      if (this.closed) {
        throw new FileItemStream.ItemSkippedException();
      }
      if (this.available() == 0 && this.makeAvailable() == 0) {
        return -1;
      }
      ++this.total;
      final int b = MultipartStream.this.buffer[MultipartStream.this.head++];
      if (b >= 0) {
        return b;
      }
      return b + 256;
    }

    public int read(final byte[] b, final int off, final int len) throws IOException {
      if (this.closed) {
        throw new FileItemStream.ItemSkippedException();
      }
      if (len == 0) {
        return 0;
      }
      int res = this.available();
      if (res == 0) {
        res = this.makeAvailable();
        if (res == 0) {
          return -1;
        }
      }
      res = Math.min(res, len);
      System.arraycopy(MultipartStream.this.buffer, MultipartStream.this.head, b, off, res);
      MultipartStream.this.head += res;
      this.total += res;
      return res;
    }

    public void close() throws IOException {
      this.close(false);
    }

    public void close(final boolean pCloseUnderlying) throws IOException {
      if (this.closed) {
        return;
      }
      if (pCloseUnderlying) {
        this.closed = true;
        MultipartStream.this.input.close();
      } else {
        while (true) {
          int av = this.available();
          if (av == 0) {
            av = this.makeAvailable();
            if (av == 0) {
              break;
            }
          }
          this.skip(av);
        }
      }
      this.closed = true;
    }

    public long skip(final long bytes) throws IOException {
      if (this.closed) {
        throw new FileItemStream.ItemSkippedException();
      }
      int av = this.available();
      if (av == 0) {
        av = this.makeAvailable();
        if (av == 0) {
          return 0L;
        }
      }
      final long res = Math.min(av, bytes);
      MultipartStream.access$114(MultipartStream.this, res);
      return res;
    }

    private int makeAvailable() throws IOException {
      if (this.pos != -1) {
        return 0;
      }
      this.total += MultipartStream.this.tail - MultipartStream.this.head - this.pad;
      System.arraycopy(
          MultipartStream.this.buffer,
          MultipartStream.this.tail - this.pad,
          MultipartStream.this.buffer,
          0,
          this.pad);
      MultipartStream.this.head = 0;
      MultipartStream.this.tail = this.pad;
      while (true) {
        final int bytesRead =
            MultipartStream.this.input.read(
                MultipartStream.this.buffer,
                MultipartStream.this.tail,
                MultipartStream.this.bufSize - MultipartStream.this.tail);
        if (bytesRead == -1) {
          final String msg = "Stream ended unexpectedly";
          throw new MalformedStreamException("Stream ended unexpectedly");
        }
        if (MultipartStream.this.notifier != null) {
          MultipartStream.this.notifier.noteBytesRead(bytesRead);
        }
        MultipartStream.this.tail += bytesRead;
        this.findSeparator();
        final int av = this.available();
        if (av > 0 || this.pos != -1) {
          return av;
        }
      }
    }

    public boolean isClosed() {
      return this.closed;
    }
  }
}
