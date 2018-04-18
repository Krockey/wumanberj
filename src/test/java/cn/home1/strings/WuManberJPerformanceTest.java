package cn.home1.strings;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author haolun.zhang
 */
public class WuManberJPerformanceTest extends PerformanceTestBase {

    private static final Logger log = LoggerFactory.getLogger(WuManberJPerformanceTest.class);

    private WuManberJ wuManberJ;

    public WuManberJPerformanceTest() {
        super(100, 2, 50, 500);
    }

    @Before
    public void setUp() {
        final List<String> patterns = this.getPatterns();
        this.wuManberJ = new WuManberJ(this.getCharacterSet().size());
        for (final String pattern : patterns) {
            this.wuManberJ.addPattern(pattern);
        }
        this.wuManberJ.prepPatterns();
    }

    @Test
    public void testWuManberJ() {
        this.runLoops();
    }

    @Override
    protected int runTest(final String text) {
        return this.wuManberJ.search(text);
    }
}
