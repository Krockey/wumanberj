package cn.home1.strings;

import net.domesdaybook.matcher.sequence.searcher.WuManberSearch;
import net.domesdaybook.matcher.sequence.searcher.WuManberSearch.MatchResult;
import net.domesdaybook.matcher.sequence.searcher.WuManberSearch.searchType;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author haolun.zhang
 */
public class ByteseekWuManberPerformanceTest extends PerformanceTestBase {

    private static final Logger log = LoggerFactory.getLogger(ByteseekWuManberPerformanceTest.class);

    private WuManberSearch searcher;

    public ByteseekWuManberPerformanceTest() {
        super(100, 3, 50, 500);
    }

    @Before
    public void setUp() {
        //this.getPatterns().forEach(pattern -> log.info("pattern: {}", pattern));

        this.searcher = new WuManberSearch(this.getPatterns());
    }

    @Test
    public void testWuManberSearch() {
        this.runLoops();
    }

    @Override
    protected int runTest(final String text) {
        try {
            final List<MatchResult> result = this.searcher.findWithin(text, 0, text.length() - 1, searchType.FIND_ALL);
            return result.size();
        } catch (final NullPointerException e) {
            log.warn("text: {}", text, e);
            throw e;
        }
    }
}
