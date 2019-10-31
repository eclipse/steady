package org.apache.commons.compress.compressors.bzip2;

import java.io.*;
import org.apache.commons.compress.compressors.*;

public class BZip2CompressorOutputStream extends CompressorOutputStream implements BZip2Constants {
  public static final int MIN_BLOCKSIZE = 1;
  public static final int MAX_BLOCKSIZE = 9;
  private static final int GREATER_ICOST = 15;
  private static final int LESSER_ICOST = 0;
  private int last;
  private final int blockSize100k;
  private int bsBuff;
  private int bsLive;
  private final CRC crc;
  private int nInUse;
  private int nMTF;
  private int currentChar;
  private int runLength;
  private int blockCRC;
  private int combinedCRC;
  private final int allowableBlockSize;
  private Data data;
  private BlockSort blockSorter;
  private OutputStream out;

  private static void hbMakeCodeLengths(
      final byte[] len, final int[] freq, final Data dat, int alphaSize, final int maxLen) {
    final int[] heap = dat.heap;
    final int[] weight = dat.weight;
    final int[] parent = dat.parent;
    while (--alphaSize >= 0) {
      weight[alphaSize + 1] = ((freq[alphaSize] == 0) ? 1 : freq[alphaSize]) << 8;
    }
    boolean tooLong = true;
    while (tooLong) {
      tooLong = false;
      int nNodes = alphaSize;
      int nHeap = 0;
      heap[0] = 0;
      parent[weight[0] = 0] = -2;
      for (int i = 1; i <= alphaSize; ++i) {
        parent[i] = -1;
        ++nHeap;
        heap[nHeap] = i;
        int zz;
        int tmp;
        for (zz = nHeap, tmp = heap[zz]; weight[tmp] < weight[heap[zz >> 1]]; zz >>= 1) {
          heap[zz] = heap[zz >> 1];
        }
        heap[zz] = tmp;
      }
      while (nHeap > 1) {
        final int n1 = heap[1];
        heap[1] = heap[nHeap];
        --nHeap;
        int yy = 0;
        int zz2 = 1;
        int tmp2 = heap[1];
        while (true) {
          yy = zz2 << 1;
          if (yy > nHeap) {
            break;
          }
          if (yy < nHeap && weight[heap[yy + 1]] < weight[heap[yy]]) {
            ++yy;
          }
          if (weight[tmp2] < weight[heap[yy]]) {
            break;
          }
          heap[zz2] = heap[yy];
          zz2 = yy;
        }
        heap[zz2] = tmp2;
        final int n2 = heap[1];
        heap[1] = heap[nHeap];
        --nHeap;
        yy = 0;
        zz2 = 1;
        tmp2 = heap[1];
        while (true) {
          yy = zz2 << 1;
          if (yy > nHeap) {
            break;
          }
          if (yy < nHeap && weight[heap[yy + 1]] < weight[heap[yy]]) {
            ++yy;
          }
          if (weight[tmp2] < weight[heap[yy]]) {
            break;
          }
          heap[zz2] = heap[yy];
          zz2 = yy;
        }
        heap[zz2] = tmp2;
        ++nNodes;
        parent[n1] = (parent[n2] = nNodes);
        final int weight_n1 = weight[n1];
        final int weight_n2 = weight[n2];
        weight[nNodes] =
            ((weight_n1 & 0xFFFFFF00) + (weight_n2 & 0xFFFFFF00)
                | 1
                    + (((weight_n1 & 0xFF) > (weight_n2 & 0xFF))
                        ? (weight_n1 & 0xFF)
                        : (weight_n2 & 0xFF)));
        parent[nNodes] = -1;
        ++nHeap;
        heap[nHeap] = nNodes;
        tmp2 = 0;
        zz2 = nHeap;
        tmp2 = heap[zz2];
        for (int weight_tmp = weight[tmp2]; weight_tmp < weight[heap[zz2 >> 1]]; zz2 >>= 1) {
          heap[zz2] = heap[zz2 >> 1];
        }
        heap[zz2] = tmp2;
      }
      for (int i = 1; i <= alphaSize; ++i) {
        int j = 0;
        int parent_k;
        for (int k = i; (parent_k = parent[k]) >= 0; k = parent_k, ++j) {}
        len[i - 1] = (byte) j;
        if (j > maxLen) {
          tooLong = true;
        }
      }
      if (tooLong) {
        for (int i = 1; i < alphaSize; ++i) {
          int j = weight[i] >> 8;
          j = 1 + (j >> 1);
          weight[i] = j << 8;
        }
      }
    }
  }

  public static int chooseBlockSize(final long inputLength) {
    return (inputLength > 0L) ? ((int) Math.min(inputLength / 132000L + 1L, 9L)) : 9;
  }

  public BZip2CompressorOutputStream(final OutputStream out) throws IOException {
    this(out, 9);
  }

  public BZip2CompressorOutputStream(final OutputStream out, final int blockSize)
      throws IOException {
    super();
    crc = new CRC();
    currentChar = -1;
    runLength = 0;
    if (blockSize < 1) {
      throw new IllegalArgumentException("blockSize(" + blockSize + ") < 1");
    }
    if (blockSize > 9) {
      throw new IllegalArgumentException("blockSize(" + blockSize + ") > 9");
    }
    blockSize100k = blockSize;
    out = out;
    allowableBlockSize = blockSize100k * 100000 - 20;
    this.init();
  }

  public void write(final int b) throws IOException {
    if (out != null) {
      this.write0(b);
      return;
    }
    throw new IOException("closed");
  }

  private void writeRun() throws IOException {
    final int lastShadow = last;
    if (lastShadow < allowableBlockSize) {
      final int currentCharShadow = currentChar;
      final Data dataShadow = data;
      dataShadow.inUse[currentCharShadow] = true;
      final byte ch = (byte) currentCharShadow;
      int runLengthShadow = runLength;
      crc.updateCRC(currentCharShadow, runLengthShadow);
      switch (runLengthShadow) {
        case 1:
          {
            dataShadow.block[lastShadow + 2] = ch;
            last = lastShadow + 1;
            break;
          }
        case 2:
          {
            dataShadow.block[lastShadow + 2] = ch;
            dataShadow.block[lastShadow + 3] = ch;
            last = lastShadow + 2;
            break;
          }
        case 3:
          {
            final byte[] block = dataShadow.block;
            block[lastShadow + 2] = ch;
            block[lastShadow + 4] = (block[lastShadow + 3] = ch);
            last = lastShadow + 3;
            break;
          }
        default:
          {
            runLengthShadow -= 4;
            dataShadow.inUse[runLengthShadow] = true;
            final byte[] block = dataShadow.block;
            block[lastShadow + 3] = (block[lastShadow + 2] = ch);
            block[lastShadow + 5] = (block[lastShadow + 4] = ch);
            block[lastShadow + 6] = (byte) runLengthShadow;
            last = lastShadow + 5;
            break;
          }
      }
    } else {
      this.endBlock();
      this.initBlock();
      this.writeRun();
    }
  }

  protected void finalize() throws Throwable {
    this.finish();
    super.finalize();
  }

  public void finish() throws IOException {
    if (out != null) {
      try {
        if (runLength > 0) {
          this.writeRun();
        }
        currentChar = -1;
        this.endBlock();
        this.endCompression();
      } finally {
        out = null;
        data = null;
        blockSorter = null;
      }
    }
  }

  public void close() throws IOException {
    if (out != null) {
      final OutputStream outShadow = out;
      this.finish();
      outShadow.close();
    }
  }

  public void flush() throws IOException {
    final OutputStream outShadow = out;
    if (outShadow != null) {
      outShadow.flush();
    }
  }

  private void init() throws IOException {
    this.bsPutUByte(66);
    this.bsPutUByte(90);
    data = new Data(blockSize100k);
    blockSorter = new BlockSort(data);
    this.bsPutUByte(104);
    this.bsPutUByte(48 + blockSize100k);
    combinedCRC = 0;
    this.initBlock();
  }

  private void initBlock() {
    crc.initialiseCRC();
    last = -1;
    final boolean[] inUse = data.inUse;
    int i = 256;
    while (--i >= 0) {
      inUse[i] = false;
    }
  }

  private void endBlock() throws IOException {
    blockCRC = crc.getFinalCRC();
    combinedCRC = (combinedCRC << 1 | combinedCRC >>> 31);
    combinedCRC ^= blockCRC;
    if (last == -1) {
      return;
    }
    this.blockSort();
    this.bsPutUByte(49);
    this.bsPutUByte(65);
    this.bsPutUByte(89);
    this.bsPutUByte(38);
    this.bsPutUByte(83);
    this.bsPutUByte(89);
    this.bsPutInt(blockCRC);
    this.bsW(1, 0);
    this.moveToFrontCodeAndSend();
  }

  private void endCompression() throws IOException {
    this.bsPutUByte(23);
    this.bsPutUByte(114);
    this.bsPutUByte(69);
    this.bsPutUByte(56);
    this.bsPutUByte(80);
    this.bsPutUByte(144);
    this.bsPutInt(combinedCRC);
    this.bsFinishedWithStream();
  }

  public final int getBlockSize() {
    return blockSize100k;
  }

  public void write(final byte[] buf, int offs, final int len) throws IOException {
    if (offs < 0) {
      throw new IndexOutOfBoundsException("offs(" + offs + ") < 0.");
    }
    if (len < 0) {
      throw new IndexOutOfBoundsException("len(" + len + ") < 0.");
    }
    if (offs + len > buf.length) {
      throw new IndexOutOfBoundsException(
          "offs(" + offs + ") + len(" + len + ") > buf.length(" + buf.length + ").");
    }
    if (out == null) {
      throw new IOException("stream closed");
    }
    final int hi = offs + len;
    while (offs < hi) {
      this.write0(buf[offs++]);
    }
  }

  private void write0(int b) throws IOException {
    if (currentChar != -1) {
      b &= 0xFF;
      if (currentChar == b) {
        if (++runLength > 254) {
          this.writeRun();
          currentChar = -1;
          runLength = 0;
        }
      } else {
        this.writeRun();
        runLength = 1;
        currentChar = b;
      }
    } else {
      currentChar = (b & 0xFF);
      ++runLength;
    }
  }

  private static void hbAssignCodes(
      final int[] code,
      final byte[] length,
      final int minLen,
      final int maxLen,
      final int alphaSize) {
    int vec = 0;
    for (int n = minLen; n <= maxLen; ++n) {
      for (int i = 0; i < alphaSize; ++i) {
        if ((length[i] & 0xFF) == n) {
          code[i] = vec;
          ++vec;
        }
      }
      vec <<= 1;
    }
  }

  private void bsFinishedWithStream() throws IOException {
    while (bsLive > 0) {
      final int ch = bsBuff >> 24;
      out.write(ch);
      bsBuff <<= 8;
      bsLive -= 8;
    }
  }

  private void bsW(final int n, final int v) throws IOException {
    final OutputStream outShadow = out;
    int bsLiveShadow = bsLive;
    int bsBuffShadow = bsBuff;
    while (bsLiveShadow >= 8) {
      outShadow.write(bsBuffShadow >> 24);
      bsBuffShadow <<= 8;
      bsLiveShadow -= 8;
    }
    bsBuff = (bsBuffShadow | v << 32 - bsLiveShadow - n);
    bsLive = bsLiveShadow + n;
  }

  private void bsPutUByte(final int c) throws IOException {
    this.bsW(8, c);
  }

  private void bsPutInt(final int u) throws IOException {
    this.bsW(8, u >> 24 & 0xFF);
    this.bsW(8, u >> 16 & 0xFF);
    this.bsW(8, u >> 8 & 0xFF);
    this.bsW(8, u & 0xFF);
  }

  private void sendMTFValues() throws IOException {
    final byte[][] len = data.sendMTFValues_len;
    final int alphaSize = nInUse + 2;
    int t = 6;
    while (--t >= 0) {
      final byte[] len_t = len[t];
      int v = alphaSize;
      while (--v >= 0) {
        len_t[v] = 15;
      }
    }
    final int nGroups =
        (nMTF < 200) ? 2 : ((nMTF < 600) ? 3 : ((nMTF < 1200) ? 4 : ((nMTF < 2400) ? 5 : 6)));
    this.sendMTFValues0(nGroups, alphaSize);
    final int nSelectors = this.sendMTFValues1(nGroups, alphaSize);
    this.sendMTFValues2(nGroups, nSelectors);
    this.sendMTFValues3(nGroups, alphaSize);
    this.sendMTFValues4();
    this.sendMTFValues5(nGroups, nSelectors);
    this.sendMTFValues6(nGroups, alphaSize);
    this.sendMTFValues7();
  }

  private void sendMTFValues0(final int nGroups, int alphaSize) {
    final byte[][] len = data.sendMTFValues_len;
    final int[] mtfFreq = data.mtfFreq;
    int remF = nMTF;
    int gs = 0;
    for (int nPart = nGroups; nPart > 0; --nPart) {
      final int tFreq = remF / nPart;
      int ge = gs - 1;
      int aFreq = 0;
      for (int a = alphaSize - 1; aFreq < tFreq && ge < a; aFreq += mtfFreq[++ge]) {}
      if (ge > gs && nPart != nGroups && nPart != 1 && (nGroups - nPart & 0x1) != 0x0) {
        aFreq -= mtfFreq[ge--];
      }
      final byte[] len_np = len[nPart - 1];
      while (--alphaSize >= 0) {
        if (alphaSize >= gs && alphaSize <= ge) {
          len_np[alphaSize] = 0;
        } else {
          len_np[alphaSize] = 15;
        }
      }
      gs = ge + 1;
      remF -= aFreq;
    }
  }

  private int sendMTFValues1(int nGroups, int alphaSize) {
    final Data dataShadow = data;
    final int[][] rfreq = dataShadow.sendMTFValues_rfreq;
    final int[] fave = dataShadow.sendMTFValues_fave;
    final short[] cost = dataShadow.sendMTFValues_cost;
    final char[] sfmap = dataShadow.sfmap;
    final byte[] selector = dataShadow.selector;
    final byte[][] len = dataShadow.sendMTFValues_len;
    final byte[] len_0 = len[0];
    final byte[] len_ = len[1];
    final byte[] len_2 = len[2];
    final byte[] len_3 = len[3];
    final byte[] len_4 = len[4];
    final byte[] len_5 = len[5];
    final int nMTFShadow = nMTF;
    int nSelectors = 0;
    for (int iter = 0; iter < 4; ++iter) {
      while (--nGroups >= 0) {
        fave[nGroups] = 0;
        final int[] rfreqt = rfreq[nGroups];
        while (--alphaSize >= 0) {
          rfreqt[alphaSize] = 0;
        }
      }
      nSelectors = 0;
      int ge;
      for (int gs = 0; gs < nMTF; gs = ge + 1) {
        ge = Math.min(gs + 50 - 1, nMTFShadow - 1);
        if (nGroups == 6) {
          short cost2 = 0;
          short cost3 = 0;
          short cost4 = 0;
          short cost5 = 0;
          short cost6 = 0;
          short cost7 = 0;
          for (int i = gs; i <= ge; ++i) {
            final int icv = sfmap[i];
            cost2 += (short) (len_0[icv] & 0xFF);
            cost3 += (short) (len_[icv] & 0xFF);
            cost4 += (short) (len_2[icv] & 0xFF);
            cost5 += (short) (len_3[icv] & 0xFF);
            cost6 += (short) (len_4[icv] & 0xFF);
            cost7 += (short) (len_5[icv] & 0xFF);
          }
          cost[0] = cost2;
          cost[1] = cost3;
          cost[2] = cost4;
          cost[3] = cost5;
          cost[4] = cost6;
          cost[5] = cost7;
        } else {
          while (--nGroups >= 0) {
            cost[nGroups] = 0;
          }
          for (int j = gs; j <= ge; ++j) {
            final int icv2 = sfmap[j];
            while (--nGroups >= 0) {
              final short[] array = cost;
              array[nGroups] += (short) (len[nGroups][icv2] & 0xFF);
            }
          }
        }
        int bt = -1;
        int bc = 999999999;
        while (--nGroups >= 0) {
          final int cost_t = cost[nGroups];
          if (cost_t < bc) {
            bc = cost_t;
            bt = nGroups;
          }
        }
        final int[] array2 = fave;
        final int n = bt;
        ++array2[n];
        selector[nSelectors] = (byte) bt;
        ++nSelectors;
        final int[] rfreq_bt = rfreq[bt];
        for (int k = gs; k <= ge; ++k) {
          final int[] array3 = rfreq_bt;
          final char c = sfmap[k];
          ++array3[c];
        }
      }
      for (int t = 0; t < nGroups; ++t) {
        hbMakeCodeLengths(len[t], rfreq[t], data, alphaSize, 20);
      }
    }
    return nSelectors;
  }

  private void sendMTFValues2(int nGroups, final int nSelectors) {
    final Data dataShadow = data;
    final byte[] pos = dataShadow.sendMTFValues2_pos;
    while (--nGroups >= 0) {
      pos[nGroups] = (byte) nGroups;
    }
    for (int i = 0; i < nSelectors; ++i) {
      byte ll_i;
      byte tmp;
      int j;
      byte tmp2;
      for (ll_i = dataShadow.selector[i], tmp = pos[0], j = 0;
          ll_i != tmp;
          tmp = pos[j], pos[j] = tmp2) {
        ++j;
        tmp2 = tmp;
      }
      pos[0] = tmp;
      dataShadow.selectorMtf[i] = (byte) j;
    }
  }

  private void sendMTFValues3(final int nGroups, int alphaSize) {
    final int[][] code = data.sendMTFValues_code;
    final byte[][] len = data.sendMTFValues_len;
    for (int t = 0; t < nGroups; ++t) {
      int minLen = 32;
      int maxLen = 0;
      final byte[] len_t = len[t];
      while (--alphaSize >= 0) {
        final int l = len_t[alphaSize] & 0xFF;
        if (l > maxLen) {
          maxLen = l;
        }
        if (l < minLen) {
          minLen = l;
        }
      }
      hbAssignCodes(code[t], len[t], minLen, maxLen, alphaSize);
    }
  }

  private void sendMTFValues4() throws IOException {
    final boolean[] inUse = data.inUse;
    final boolean[] inUse2 = data.sentMTFValues4_inUse16;
    int i = 16;
    while (--i >= 0) {
      inUse2[i] = false;
      final int i2 = i * 16;
      int j = 16;
      while (--j >= 0) {
        if (inUse[i2 + j]) {
          inUse2[i] = true;
        }
      }
    }
    for (i = 0; i < 16; ++i) {
      this.bsW(1, inUse2[i] ? 1 : 0);
    }
    final OutputStream outShadow = out;
    int bsLiveShadow = bsLive;
    int bsBuffShadow = bsBuff;
    for (int k = 0; k < 16; ++k) {
      if (inUse2[k]) {
        final int i3 = k * 16;
        for (int l = 0; l < 16; ++l) {
          while (bsLiveShadow >= 8) {
            outShadow.write(bsBuffShadow >> 24);
            bsBuffShadow <<= 8;
            bsLiveShadow -= 8;
          }
          if (inUse[i3 + l]) {
            bsBuffShadow |= 1 << 32 - bsLiveShadow - 1;
          }
          ++bsLiveShadow;
        }
      }
    }
    bsBuff = bsBuffShadow;
    bsLive = bsLiveShadow;
  }

  private void sendMTFValues5(final int nGroups, final int nSelectors) throws IOException {
    this.bsW(3, nGroups);
    this.bsW(15, nSelectors);
    final OutputStream outShadow = out;
    final byte[] selectorMtf = data.selectorMtf;
    int bsLiveShadow = bsLive;
    int bsBuffShadow = bsBuff;
    for (int i = 0; i < nSelectors; ++i) {
      for (int j = 0, hj = selectorMtf[i] & 0xFF; j < hj; ++j) {
        while (bsLiveShadow >= 8) {
          outShadow.write(bsBuffShadow >> 24);
          bsBuffShadow <<= 8;
          bsLiveShadow -= 8;
        }
        bsBuffShadow |= 1 << 32 - bsLiveShadow - 1;
        ++bsLiveShadow;
      }
      while (bsLiveShadow >= 8) {
        outShadow.write(bsBuffShadow >> 24);
        bsBuffShadow <<= 8;
        bsLiveShadow -= 8;
      }
      ++bsLiveShadow;
    }
    bsBuff = bsBuffShadow;
    bsLive = bsLiveShadow;
  }

  private void sendMTFValues6(final int nGroups, final int alphaSize) throws IOException {
    final byte[][] len = data.sendMTFValues_len;
    final OutputStream outShadow = out;
    int bsLiveShadow = bsLive;
    int bsBuffShadow = bsBuff;
    for (final byte[] len_t : len) {
      int curr = len_t[0] & 0xFF;
      while (bsLiveShadow >= 8) {
        outShadow.write(bsBuffShadow >> 24);
        bsBuffShadow <<= 8;
        bsLiveShadow -= 8;
      }
      bsBuffShadow |= curr << 32 - bsLiveShadow - 5;
      bsLiveShadow += 5;
      for (int i = 0; i < alphaSize; ++i) {
        int lti;
        for (lti = (len_t[i] & 0xFF); curr < lti; ++curr) {
          while (bsLiveShadow >= 8) {
            outShadow.write(bsBuffShadow >> 24);
            bsBuffShadow <<= 8;
            bsLiveShadow -= 8;
          }
          bsBuffShadow |= 2 << 32 - bsLiveShadow - 2;
          bsLiveShadow += 2;
        }
        while (curr > lti) {
          while (bsLiveShadow >= 8) {
            outShadow.write(bsBuffShadow >> 24);
            bsBuffShadow <<= 8;
            bsLiveShadow -= 8;
          }
          bsBuffShadow |= 3 << 32 - bsLiveShadow - 2;
          bsLiveShadow += 2;
          --curr;
        }
        while (bsLiveShadow >= 8) {
          outShadow.write(bsBuffShadow >> 24);
          bsBuffShadow <<= 8;
          bsLiveShadow -= 8;
        }
        ++bsLiveShadow;
      }
    }
    bsBuff = bsBuffShadow;
    bsLive = bsLiveShadow;
  }

  private void sendMTFValues7() throws IOException {
    final Data dataShadow = data;
    final byte[][] len = dataShadow.sendMTFValues_len;
    final int[][] code = dataShadow.sendMTFValues_code;
    final OutputStream outShadow = out;
    final byte[] selector = dataShadow.selector;
    final char[] sfmap = dataShadow.sfmap;
    final int nMTFShadow = nMTF;
    int selCtr = 0;
    int bsLiveShadow = bsLive;
    int bsBuffShadow = bsBuff;
    int ge;
    for (int gs = 0; gs < nMTFShadow; gs = ge + 1, ++selCtr) {
      ge = Math.min(gs + 50 - 1, nMTFShadow - 1);
      final int selector_selCtr = selector[selCtr] & 0xFF;
      final int[] code_selCtr = code[selector_selCtr];
      final byte[] len_selCtr = len[selector_selCtr];
      while (gs <= ge) {
        final int sfmap_i = sfmap[gs];
        while (bsLiveShadow >= 8) {
          outShadow.write(bsBuffShadow >> 24);
          bsBuffShadow <<= 8;
          bsLiveShadow -= 8;
        }
        final int n = len_selCtr[sfmap_i] & 0xFF;
        bsBuffShadow |= code_selCtr[sfmap_i] << 32 - bsLiveShadow - n;
        bsLiveShadow += n;
        ++gs;
      }
    }
    bsBuff = bsBuffShadow;
    bsLive = bsLiveShadow;
  }

  private void moveToFrontCodeAndSend() throws IOException {
    this.bsW(24, data.origPtr);
    this.generateMTFValues();
    this.sendMTFValues();
  }

  private void blockSort() {
    blockSorter.blockSort(data, last);
  }

  private void generateMTFValues() {
    final int lastShadow = last;
    final Data dataShadow = data;
    final boolean[] inUse = dataShadow.inUse;
    final byte[] block = dataShadow.block;
    final int[] fmap = dataShadow.fmap;
    final char[] sfmap = dataShadow.sfmap;
    final int[] mtfFreq = dataShadow.mtfFreq;
    final byte[] unseqToSeq = dataShadow.unseqToSeq;
    final byte[] yy = dataShadow.generateMTFValues_yy;
    int nInUseShadow = 0;
    for (int i = 0; i < 256; ++i) {
      if (inUse[i]) {
        unseqToSeq[i] = (byte) nInUseShadow;
        ++nInUseShadow;
      }
    }
    nInUse = nInUseShadow;
    int j;
    int eob;
    for (eob = (j = nInUseShadow + 1); j >= 0; --j) {
      mtfFreq[j] = 0;
    }
    j = nInUseShadow;
    while (--j >= 0) {
      yy[j] = (byte) j;
    }
    int wr = 0;
    int zPend = 0;
    for (int k = 0; k <= lastShadow; ++k) {
      byte ll_i;
      byte tmp;
      int l;
      byte tmp2;
      for (ll_i = unseqToSeq[block[fmap[k]] & 0xFF], tmp = yy[0], l = 0;
          ll_i != tmp;
          tmp = yy[l], yy[l] = tmp2) {
        ++l;
        tmp2 = tmp;
      }
      yy[0] = tmp;
      if (l == 0) {
        ++zPend;
      } else {
        if (zPend > 0) {
          --zPend;
          while (true) {
            if ((zPend & 0x1) == 0x0) {
              sfmap[wr] = '\0';
              ++wr;
              final int[] array = mtfFreq;
              final int n = 0;
              ++array[n];
            } else {
              sfmap[wr] = '\u0001';
              ++wr;
              final int[] array2 = mtfFreq;
              final int n2 = 1;
              ++array2[n2];
            }
            if (zPend < 2) {
              break;
            }
            zPend = zPend - 2 >> 1;
          }
          zPend = 0;
        }
        sfmap[wr] = (char) (l + 1);
        ++wr;
        final int[] array3 = mtfFreq;
        final int n3 = l + 1;
        ++array3[n3];
      }
    }
    if (zPend > 0) {
      --zPend;
      while (true) {
        if ((zPend & 0x1) == 0x0) {
          sfmap[wr] = '\0';
          ++wr;
          final int[] array4 = mtfFreq;
          final int n4 = 0;
          ++array4[n4];
        } else {
          sfmap[wr] = '\u0001';
          ++wr;
          final int[] array5 = mtfFreq;
          final int n5 = 1;
          ++array5[n5];
        }
        if (zPend < 2) {
          break;
        }
        zPend = zPend - 2 >> 1;
      }
    }
    sfmap[wr] = (char) eob;
    final int[] array6 = mtfFreq;
    final int n6 = eob;
    ++array6[n6];
    nMTF = wr + 1;
  }
}
