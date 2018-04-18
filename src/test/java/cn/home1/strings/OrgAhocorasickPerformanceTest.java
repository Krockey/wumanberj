package cn.home1.strings;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.ahocorasick.trie.Trie.TrieBuilder;
import org.ahocorasick.trie.TrieConfig;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * @author haolun.zhang
 */
public class OrgAhocorasickPerformanceTest extends PerformanceTestBase {

    private static final Logger log = LoggerFactory.getLogger(WuManberJPerformanceTest.class);

    private Trie trie;

    public OrgAhocorasickPerformanceTest() {
        super(100, 2, 50, 500);
    }

    @Before
    public void setUp() {
        final List<String> patterns = this.getPatterns();

        final TrieConfig trieConfig = new TrieConfig();
        trieConfig.setOnlyWholeWords(true);
        final TrieBuilder trieBuilder = Trie.builder().onlyWholeWords();
        this.getPatterns().forEach(pattern -> trieBuilder.addKeyword(pattern));
        this.trie = trieBuilder.build();
    }

    @Test
    public void testWuManberJ() {
        this.runLoops();
    }

    @Override
    protected int runTest(final String text) {
        final Collection<Emit> emits = this.trie.parseText(text);
        return emits.size();
    }
}
