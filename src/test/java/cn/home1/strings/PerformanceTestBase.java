/**
 *
 */
package cn.home1.strings;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.*;

/**
 * @author haolun.zhang
 */
public abstract class PerformanceTestBase {

    private static final Logger log = LoggerFactory.getLogger(PerformanceTestBase.class);

    private final int loop;
    private final int minPatternLength;
    private final int maxPatternLength;
    private final int patternLimit;

    @Getter
    private final List<String> patterns;
    @Getter
    private Set<Character> characterSet = new HashSet<>();

    public PerformanceTestBase(final int loop, final int minPatternLength, final int maxPatternLength, final int patternLimit) {
        this.loop = loop;
        this.minPatternLength = minPatternLength;
        this.maxPatternLength = maxPatternLength;
        this.patternLimit = patternLimit;

        this.patterns = this.patterns();
        log.info("patterns lines:{}", this.patterns.size());
    }

    public final void runLoops() {
        int lineCount = 0;
        int matchCount = 0;
        final List<String> texts = readLines("src/test/resources/TheAmorousLotus/text.txt");
        log.info("text lines:{}", texts.size());

        try {
            Thread.sleep(SECONDS.toMillis(2L));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        final long startTime = nanoTime();
        long loopStart;
        for (int i = 0; i < this.loop; i++) {
            loopStart = nanoTime();

            for (final String text : texts) {
                // final List<MatchResult> result = searcher.find(text, searchType.FIND_ALL); // bug, toPosition >= text.length()
                if (text.length() == 0) {
                    // log.info("emptyLine:{}", lineCount);
                    continue;
                }

                // log.debug("line size:{}, text:{}", text.length(), text);
                matchCount += this.runTest(text);

                lineCount++;
            }

            log.info("loop:{}, cost:{}", i, MILLISECONDS.convert(nanoTime() - loopStart, NANOSECONDS));
        }
        final long endTime = nanoTime();
        log.info("lines: {}, matches:{}, timeCost:{}ms", lineCount, matchCount, MILLISECONDS.convert(endTime - startTime, NANOSECONDS));
    }

    /**
     * find all and return match count
     *
     * @param text
     * @return match count
     */
    protected abstract int runTest(String text);

    protected List<String> patterns() {
        final List<String> result = new ArrayList<>();

        int patternCount = 0;
        int lineCount = 0;
        for (final String pattern : readLines("src/test/resources/patterns.txt")) {
            lineCount++;

            final int patternLength = pattern.length();
            if (patternLength < this.minPatternLength) {
                log.info("skip short pattern:'{}', at line:'{}'", pattern, lineCount);
                continue;
            } else if (patternLength > this.maxPatternLength) {
                log.info("skip long pattern:'{}', at line:'{}'", pattern, lineCount);
                continue;
            } else if (this.patternLimit <= patternCount) {
                log.info("skip patterns patternCount > {}", this.patternLimit);
                break;
            }
            result.add(pattern);
            for (char ch : pattern.toCharArray()) {
                this.characterSet.add(ch);
            }
            patternCount++;
        }

        return result;
    }

    static List<String> readLines(final String file) {
        final List<String> result = new LinkedList<>();
        try (final BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/" + file))) {
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
        } catch (final IOException e) {
            log.warn("error reading " + file, e);
            throw new RuntimeException("error reading " + file, e);
        }

        return Collections.unmodifiableList(result);
    }
}
