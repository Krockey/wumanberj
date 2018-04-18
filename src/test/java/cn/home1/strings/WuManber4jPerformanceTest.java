package cn.home1.strings;

import com.song.WordMatch;
import com.song.WuManber;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author haolun.zhang
 */
public class WuManber4jPerformanceTest extends PerformanceTestBase {

    private static final Logger log = LoggerFactory.getLogger(WuManberJPerformanceTest.class);

    private WuManber wuManber;

    public WuManber4jPerformanceTest() {
        super(100, 3, 50, 500);
    }

    @Before
    public void setUp() throws Exception {
        //this.getPatterns().forEach(pattern -> log.info("pattern: {}", pattern));

        final List<WordMatch> patterns = this.getPatterns().stream().map(WordMatch::new).collect(toList());
        this.wuManber = new WuManber();
        this.wuManber.initialize(patterns);
    }

    @Test
    public void testWuManberJ() {
        this.runLoops();
    }

    @Override
    protected int runTest(final String text) {
        return this.wuManber.search(text).getIndex();
    }
}
