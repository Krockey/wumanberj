package cn.home1.strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author haolun.zhang
 *
 */
public class WuManberJ {

    /**
     * 把text切成长度为B的块(一般2或3).
     * B的算法:假如有k个pat, 那么各pat总长M=k*m, |∑|为c, 那么B=logc(2M).
     * 那SHIFT滑动距离就不由末尾单个字符而由末尾B个byte而定.如果这B个byte跟所有pat都没匹配上, 那么很指针可以后滑m-B+1
     */
    private static final int B = 2;

    private final int SHIFT_TABLE_SIZE; // (256 * 256)?
    private final int HASH_TABLE_SIZE; // == SHIFT_TABLE_SIZE

    /**
     * text中的字符不在模式串的字符集中
     */
    private final int NOT_IN_CHARACTER_TABLE = -1;

    private List<Pattern> patternList;
    /**
     * pattern array
     * sort by pattern's last 'B' bytes's hash value
     */
    private Pattern[] patternArray;
    /**
     * shortest pattern length in bytes
     */
    private int smallestPatternLength;
    /**
     * array of group size, number of patterns in each hash group
     */
    private int[] groupSizeTable;
    /**
     * last 2 byte pattern hash table
     * HASH[i]存放一个指向链表的指针, 链表存着这样的pattern(末B个byte通过hash function计算是i).
     * HASH[]表大小同SHIFT[].但相对就稀疏多了.
     */
    private int[] hashTable;
    /**
     * bad word shift table
     * Shift table, record distance of slide to right.
     * When SHIFT[i]==0, 说明那么多patterns肯定有匹配上的, 这时HASH[]和PREFIX[]就要站出来指明谁暂时匹配上了, 并对它们通通匹配一番.
     * SHIFT[] table's size is |∑|^B, 因为要组建长度为B的str, 其中每个字符均有|∑|种选择.
     */
    private int[] shiftTable;
    /**
     * TODO improvement: Good suffix table
     */
    private int[] shiftTableG;
    /**
     * first 2 characters prefix table
     * pattern's first 'B' bytes's hash value
     */
    private int[] prefixTable;

    //private int nLine = 1;

    public WuManberJ(final int characterTableSize) {
        //log.debug("characterTableSize:{}", characterTableSize);
        SHIFT_TABLE_SIZE = (int) Math.pow(characterTableSize, B);
        HASH_TABLE_SIZE = SHIFT_TABLE_SIZE;
        //log.debug("SHIFT_TABLE_SIZE:{}, HASH_TABLE_SIZE:{}", SHIFT_TABLE_SIZE, HASH_TABLE_SIZE);

        this.patternList = new ArrayList<>();
        this.smallestPatternLength = Integer.MAX_VALUE;
    }

    public void addPattern(final String text) {
        final Pattern pattern = new Pattern(text);

        if (pattern.lenInBytes < this.smallestPatternLength)
            this.smallestPatternLength = pattern.lenInBytes;

        this.patternList.add(pattern);
    }

    public void prepPatterns() {
        this.patternArray = this.patternList.toArray(new Pattern[0]);
        this.groupSizeTable = new int[this.patternCount()];

        this.sort();
        this.prepHashedPatternGroups();
        this.prepShiftTable();
        this.prepPrefixTable();
    }

    public int search(final String text) {
        return this.search(text.getBytes());
    }

    public int search(final byte[] text) {
        int nFound = 0;

        if (text.length < this.smallestPatternLength) {
            return nFound;
        }

        // unsigned char *pText, *textEnd, *window;
        // int textRemaining = text.length;
        int textEnd = text.length;
        int groupStart;

        // Init text to match, slide window
        for (int position = 0, window = position + this.smallestPatternLength - 1; window < textEnd;
        // , textRemaining--
        ) {
            // log.debug("shiftTable:{}, window:{}, text[window - 1]:{}, text[window]:{}", //
            //         new Object[] {this.shiftTable, window, text[window - 1], text[window]});

            final int hashVal = this.hash16(text[window - 1], text[window]);
            int shiftDistance = this.shiftTable[hashVal];

            if (shiftDistance == 0) {
                // shitfDistance = 1;
                shiftDistance = Math.max(this.shiftTableG[hashVal], 1);
                //if(shiftDistance > 1) {
                //    log.info("opt shiftDistance:{}", shiftDistance);
                //}
                
                if ((groupStart = this.hashTable[hashVal]) == NOT_IN_CHARACTER_TABLE) {
                    continue;
                } else {
                    // groupStart = this.hashTable[this.hash16(text[window - 1], text[window])];
                    nFound += this.groupMatch(groupStart, text, position);
                }
            }

            window += shiftDistance;
            position += shiftDistance;
            // textRemaining -= shitfValue;
            if (window >= textEnd) // TODO FIXME Figure out whether window is a proper value
                return nFound;
        }

        return nFound;
    }

    private int groupMatch(int groupStart, byte[] text, int position) {
        // log.debug("groupMatch shiftTableIndex:{}, groupSize:{}, text:{}", //
        //         new Object[] { shiftTableIndex, this.groupSizeTable[shiftTableIndex], new String(text).substring(pText) });

        int nFound = 0;
        int textPrefix = this.hash16(text, position);

        final int indexEnd = groupStart + this.groupSizeTable[groupStart];
        for (int g = groupStart; g < indexEnd; ++g) {
            final Pattern pattern = this.patternArray[g];

            // log.debug("try match pattern:{}, at pattern array index:{}", new String(pattern.text), index);

            // Check if PREFIX[p]==textPrefix.
            // If equals, then let the real pattern(aka. patternArray[p]) try to match text
            if (textPrefix != this.prefixTable[g]) {
                continue;
            } else {
                int j = 0;
                int i = position;

                while (j < pattern.textBytes.length && i < text.length && pattern.textBytes[j] == text[i]) {
                    ++j;
                    ++i;
                }

                if (j == pattern.textBytes.length) {
                    // log.debug("    Match pattern '{}' at line:{} column:{} (cloumn from 1, index:{})", new Object[] { new String(pattern.text), nLine, pText + 1, pText });
                    ++nFound;
                }
            }
        }

        return nFound;
    }

    private int patternCount() {
        return this.patternArray.length;
    }

    private int hash16(final byte[] text) {
        // return (short) (((*text) << 8) | *(text + 1));
        return 0xFF & ((text[0] << 8) | text[1]);
    }

    /**
     * Be used when searching in content inside the slide window
     * 
     * @param b1
     * @param b2
     * @return
     */
    private int hash16(final byte b1, final byte b2) {
        return 0xFF & ((b1 << 8) | b2);
    }

    private int hash16(final byte[] text, final int position) {
        return this.hash16(text[position], text[position + 1]);
    }

    private byte[] prefix(final byte[] text, final int length) {
        final byte[] result = new byte[length];
        for (int i = 0; i < length; ++i) {
            result[i] = text[i];
        }
        return result;
    }

    private byte[] suffix(final byte[] text, final int length) {
        final byte[] result = new byte[length];
        for (int i = 0; i < length; ++i) {
            result[length - 1 - i] = text[text.length - 1 - i];
        }
        return result;
    }

    private byte[] suffixM(final byte[] text) {
        final int m = this.smallestPatternLength;
        return new byte[] { text[m - 2], text[m - 1] };
    }

    /**
     * Compute suffix' hash for every pattern, sort by hash value (from min to max)
     */
    private void sort() {
        for (int i = this.patternCount() - 1, flag = 1; i > 0 && flag > 0; --i) {
            flag = 0;
            for (int j = 0; j < i; ++j) {
                try {
                    if (hash16(this.suffixM(this.patternArray[j + 1].textBytes)) < hash16(this
                            .suffixM(this.patternArray[j].textBytes))) {
                        flag = 1;
                        final Pattern temp = this.patternArray[j + 1];
                        this.patternArray[j + 1] = this.patternArray[j]; // TODO FIXME does not swap the length
                        this.patternArray[j] = temp;
                    }
                } catch (final IndexOutOfBoundsException e) {
                    //log.warn("error smallestPatternLength:{}", this.smallestPatternLength);
                    //log.warn("error patternArray.length:{}, j:{}, patternArray[j + 1]:{}, patternArray[j]:{}", new Object[] { this.patternArray.length, j, new String(this.patternArray[j + 1].text), new String(this.patternArray[j].text) });
                    throw e;
                } catch (final IllegalArgumentException e) {
                    //log.warn("error patternArray[j + 1]:'{}' ('{}')", new String(this.patternArray[j + 1].text), this.patternArray[j + 1].text);
                    //log.warn("error patternArray[j]:'{}' ('{}')", new String(this.patternArray[j].text), this.patternArray[j].text);
                    throw e;
                }
            }
        }
    }

    /**
     * HASH[h]'s value is p(p points to patternList or the first node in patternArray which hash value is h);
     */
    private void prepHashedPatternGroups() {
        this.hashTable = new int[this.HASH_TABLE_SIZE];
        Arrays.fill(this.hashTable, NOT_IN_CHARACTER_TABLE); // init hashTable

        //log.debug("prepHashedPatternGroups patternCount:{}, smallestPatternLength:{}\n", this.patternCount(), this.smallestPatternLength);

        for (int p = 0; p < this.patternCount(); ++p) {
            final int hashTableIndex = hash16(this.suffixM(patternArray[p].textBytes));
            final int groupStart = this.hashTable[hashTableIndex] = p;

            int groupSize = 1; // count of patterns in each hash group
            while ((++p < this.patternCount()) && (hashTableIndex == hash16(this.suffixM(patternArray[p].textBytes)))) {
                ++groupSize;
            }
            this.groupSizeTable[groupStart] = groupSize;
            --p;
        }
    }

    /**
     * SHIFT[hashFun(subpat)]=min{m-B+1,m-b-j}
     * Compute most safe move distance
     */
    private void prepShiftTable() {
        int m = this.smallestPatternLength;

        this.shiftTable = new int[SHIFT_TABLE_SIZE];
        Arrays.fill(this.shiftTable, (m - B + 1));

        // TODO improvement, try to compute a more optimized 'max safe slide distance'
        bs: for (int b = 1; b < B; ++b) {
            // For all bc∈[suffix(bc,b)==prefix(pattern,b)], SHIFT[bc]=m-b;
            patterns: for (final Pattern pattern : this.patternArray) {
                final byte[] pat = pattern.textBytes;

                chars: for (int k = 0; k < m - 1; ++k) {
                    final byte[] bc = new byte[] { pat[k], pat[k + 1] };

                    final byte[] suffixBc = this.suffix(bc, b);
                    final byte[] prefixPat = this.prefix(pat, b);

                    if (Arrays.equals(suffixBc, prefixPat)) {
                        final int bcIndex = this.hash16(bc);
                        final int shift = m - b;
                        this.shiftTable[bcIndex] = shift;
                        // log.info("opt shift bcIndex:{}, shift:{}", bcIndex, shift);
                    }
                }
            }
        }

        for (final Pattern pattern : this.patternArray) {
            final byte[] pat = pattern.textBytes;

            for (int k = 0; k < m - 1; ++k) {
                final byte[] bc = new byte[] { pat[k], pat[k + 1] };
                final int bcIndex = this.hash16(bc);

                final int shift = (m - B - k);
                if (shift < this.shiftTable[bcIndex])
                    this.shiftTable[bcIndex] = shift;
            }
        }
        
        // TODO, pre-compute the suffix table
        this.shiftTableG = Arrays.copyOf(this.shiftTable, this.shiftTable.length);
        // compute suffix, get max(badChar, goodChar)
        // Arrays.fill(this.shiftTableG, 1);
    }

    private void prepPrefixTable() {
        this.prefixTable = new int[this.patternCount()];

        for (int i = 0; i < this.patternCount(); ++i) {
            this.prefixTable[i] = hash16(this.prefix(this.patternArray[i].textBytes, B));
        }
    }

    static final class Pattern {

        /**
         *
         */
        Pattern next;

        /**
         *
         */
        final byte[] textBytes;

        /**
         * in bytes
         */
        final int lenInBytes;

        Pattern(final String pattern) {
            this.textBytes = pattern.getBytes();
            this.lenInBytes = this.textBytes.length;
        }
    }
}
