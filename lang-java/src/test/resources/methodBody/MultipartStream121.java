package org.apache.commons.fileupload;

import java.io.*;
import org.apache.commons.fileupload.util.*;

public class MultipartStream
{
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

    MultipartStream(final InputStream input, final byte[] boundary, final int bufSize, final ProgressNotifier pNotifier) {
        super();
        input = input;
        bufSize = bufSize;
        buffer = new byte[bufSize];
        notifier = pNotifier;
        boundary = new byte[boundary.length + BOUNDARY_PREFIX.length];
        boundaryLength = boundary.length + BOUNDARY_PREFIX.length;
        keepRegion = boundary.length;
        System.arraycopy(BOUNDARY_PREFIX, 0, boundary, 0, BOUNDARY_PREFIX.length);
        System.arraycopy(boundary, 0, boundary, BOUNDARY_PREFIX.length, boundary.length);
        head = 0;
        tail = 0;
    }

    MultipartStream(final InputStream input, final byte[] boundary, final ProgressNotifier pNotifier) {
        this(input, boundary, 4096, pNotifier);
    }

    public MultipartStream(final InputStream input, final byte[] boundary) {
        this(input, boundary, 4096, null);
    }

    public String getHeaderEncoding() {
        return headerEncoding;
    }

    public void setHeaderEncoding(final String encoding) {
        headerEncoding = encoding;
    }

    public byte readByte() throws IOException {
        if (head == tail) {
            head = 0;
            tail = input.read(buffer, head, bufSize);
            if (tail == -1) {
                throw new IOException("No more data is available");
            }
            if (notifier != null) {
                notifier.noteBytesRead(tail);
            }
        }
        return buffer[head++];
    }

    public boolean readBoundary() throws MalformedStreamException {
        final byte[] marker = new byte[2];
        boolean nextChunk = false;
        head += boundaryLength;
        try {
            marker[0] = this.readByte();
            if (marker[0] == 10) {
                return true;
            }
            marker[1] = this.readByte();
            if (arrayequals(marker, STREAM_TERMINATOR, 2)) {
                nextChunk = false;
            }
            else {
                if (!arrayequals(marker, FIELD_SEPARATOR, 2)) {
                    throw new MalformedStreamException("Unexpected characters follow a boundary");
                }
                nextChunk = true;
            }
        }
        catch (IOException e) {
            throw new MalformedStreamException("Stream ended unexpectedly");
        }
        return nextChunk;
    }

    public void setBoundary(final byte[] boundary) throws IllegalBoundaryException {
        if (boundary.length != boundaryLength - BOUNDARY_PREFIX.length) {
            throw new IllegalBoundaryException("The length of a boundary token can not be changed");
        }
        System.arraycopy(boundary, 0, this.boundary, BOUNDARY_PREFIX.length, boundary.length);
    }

    public String readHeaders() throws MalformedStreamException {
        int i = 0;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int size = 0;
        while (i < HEADER_SEPARATOR.length) {
            byte b;
            try {
                b = this.readByte();
            }
            catch (IOException e) {
                throw new MalformedStreamException("Stream ended unexpectedly");
            }
            if (++size > 10240) {
                throw new MalformedStreamException("Header section has more than 10240 bytes (maybe it is not properly terminated)");
            }
            if (b == HEADER_SEPARATOR[i]) {
                ++i;
            }
            else {
                i = 0;
            }
            baos.write(b);
        }
        String headers = null;
        if (headerEncoding != null) {
            try {
                headers = baos.toString(headerEncoding);
            }
            catch (UnsupportedEncodingException e2) {
                headers = baos.toString();
            }
        }
        else {
            headers = baos.toString();
        }
        return headers;
    }

    public int readBodyData(final OutputStream output) throws MalformedStreamException, IOException {
        final InputStream istream = this.newInputStream();
        return (int)Streams.copy(istream, output, false);
    }

    ItemInputStream newInputStream() {
        return new ItemInputStream();
    }

    public int discardBodyData() throws MalformedStreamException, IOException {
        return this.readBodyData(null);
    }

    public boolean skipPreamble() throws IOException {
        System.arraycopy(boundary, 2, boundary, 0, boundary.length - 2);
        boundaryLength = boundary.length - 2;
        try {
            this.discardBodyData();
            return this.readBoundary();
        }
        catch (MalformedStreamException e) {
            return false;
        }
        finally {
            System.arraycopy(boundary, 0, boundary, 2, boundary.length - 2);
            boundaryLength = boundary.length;
            boundary[0] = 13;
            boundary[1] = 10;
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
        for (int i = pos; i < tail; ++i) {
            if (buffer[i] == value) {
                return i;
            }
        }
        return -1;
    }

    protected int findSeparator() {
        int match;
        int maxpos;
        int first;
        for (match = 0, maxpos = tail - boundaryLength, first = head; first <= maxpos && match != boundaryLength; ++first) {
            first = this.findByte(boundary[0], first);
            if (first == -1 || first > maxpos) {
                return -1;
            }
            for (match = 1; match < boundaryLength && buffer[first + match] == boundary[match]; ++match) {}
        }
        if (match == boundaryLength) {
            return first - 1;
        }
        return -1;
    }

    static /* synthetic */ int access$000(final MultipartStream x0) {
        return x0.tail;
    }

    static /* synthetic */ int access$100(final MultipartStream x0) {
        return x0.head;
    }

    static /* synthetic */ int access$200(final MultipartStream x0) {
        return x0.keepRegion;
    }

    static /* synthetic */ byte[] access$300(final MultipartStream x0) {
        return x0.buffer;
    }

    static /* synthetic */ int access$108(final MultipartStream x0) {
        return x0.head++;
    }

    static /* synthetic */ int access$112(final MultipartStream x0, final int x1) {
        return x0.head += x1;
    }

    static /* synthetic */ InputStream access$400(final MultipartStream x0) {
        return x0.input;
    }

    static /* synthetic */ int access$114(final MultipartStream x0, final long x1) {
        return x0.head += (int)x1;
    }

    static /* synthetic */ int access$102(final MultipartStream x0, final int x1) {
        return x0.head = x1;
    }

    static /* synthetic */ int access$002(final MultipartStream x0, final int x1) {
        return x0.tail = x1;
    }

    static /* synthetic */ int access$500(final MultipartStream x0) {
        return x0.bufSize;
    }

    static /* synthetic */ ProgressNotifier access$600(final MultipartStream x0) {
        return x0.notifier;
    }

    static /* synthetic */ int access$012(final MultipartStream x0, final int x1) {
        return x0.tail += x1;
    }

    static {
        HEADER_SEPARATOR = new byte[] { 13, 10, 13, 10 };
        FIELD_SEPARATOR = new byte[] { 13, 10 };
        STREAM_TERMINATOR = new byte[] { 45, 45 };
        BOUNDARY_PREFIX = new byte[] { 13, 10, 45, 45 };
    }
}
