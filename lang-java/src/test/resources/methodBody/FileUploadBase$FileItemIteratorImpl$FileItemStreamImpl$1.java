package org.apache.commons.fileupload;

import org.apache.commons.fileupload.util.*;
import java.io.*;

final class FileUploadBase$FileItemIteratorImpl$FileItemStreamImpl$1 extends LimitedInputStream {
    private final /* synthetic */ FileItemIteratorImpl val$this$1 = val$this$1;
    private final /* synthetic */ MultipartStream.ItemInputStream val$itemStream = val$itemStream;
    private final /* synthetic */ FileItemStreamImpl this$2 = this$2;
    
    FileUploadBase$FileItemIteratorImpl$FileItemStreamImpl$1(final FileItemStreamImpl this$2, final InputStream x0, final long x1, final FileItemIteratorImpl val$this$1, final MultipartStream.ItemInputStream val$itemStream) {
        super(x0, x1);
    }
    
    protected void raiseError(final long pSizeMax, final long pCount) throws IOException {
        this.val$itemStream.close(true);
        final FileUploadException e = new FileSizeLimitExceededException("The field " + FileItemStreamImpl.access$300(this.this$2) + " exceeds its maximum permitted " + " size of " + pSizeMax + " characters.", pCount, pSizeMax);
        throw new FileUploadIOException(e);
    }
}