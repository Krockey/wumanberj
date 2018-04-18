# wumanberj
Java implementation of WuManber multi pattern searching algorithm.
About 2.7 times slower than aho-corasick ([org.ahocorasick:ahocorasick:0.4.0](http://mvnrepository.com/artifact/org.ahocorasick/ahocorasick/0.4.0)).

[com.song:wumanber4j:8d7d34b8104474a7ed43031d86280f0da475f9e9](https://github.com/aCoder2013/wumanber4j) can not finish the test, and it has a short pattern limit of 3.
```
java.lang.OutOfMemoryError: Requested array size exceeds VM limit

	at com.song.WuManber.initialize(WuManber.java:84)
```

[com.google.code.byteseek:byteseek:1.1.1](https://mvnrepository.com/artifact/com.google.code.byteseek/byteseek/1.1.1) can not pass the test.
```
java.lang.NullPointerException: null
	at net.domesdaybook.matcher.sequence.searcher.WuManberSearch.verifyMatches(WuManberSearch.java:118) ~[byteseek-1.1.1.jar:1.1.1]
	at net.domesdaybook.matcher.sequence.searcher.WuManberSearch.search(WuManberSearch.java:98) ~[byteseek-1.1.1.jar:1.1.1]
	at net.domesdaybook.matcher.sequence.searcher.WuManberSearch.findWithin(WuManberSearch.java:87) ~[byteseek-1.1.1.jar:1.1.1]
```

References:
1. [Sun Wu, Udi Manber, A FAST ALGORITHM FOR MULTI-PATTERN SEARCHING, 1994](http://webglimpse.net/pubs/TR94-17.pdf)
2. [Wu Mamber (String Algorithms 2007)](https://www.slideshare.net/mailund/wu-mamber-string-algorithms-2007)
3. [Flexible Pattern Matching in Strings: Practical On-Line Search Algorithms for Texts and Biological Sequences 1st Edition](https://www.amazon.com/Flexible-Pattern-Matching-Strings-Line/dp/0521039932)
