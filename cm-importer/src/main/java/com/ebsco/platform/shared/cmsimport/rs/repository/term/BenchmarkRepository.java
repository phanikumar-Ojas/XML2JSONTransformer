package com.ebsco.platform.shared.cmsimport.rs.repository.term;

import com.ebsco.platform.shared.cmsimport.rs.domain.term.BasicTerm;
import com.ebsco.platform.shared.cmsimport.rs.domain.term.BenchmarkTerm;
import com.ebsco.platform.shared.cmsimport.rs.repository.DatabaseClient;

public class BenchmarkRepository extends BasicTermRepository {

    public BenchmarkRepository(DatabaseClient databaseClient) {
        super(databaseClient);
    }

    @Override
    public String query() {
        return """
                SELECT term, term_type, mfs_an from benchmarks where mfs_an in (select mfs_an
                    FROM t_articles ta
                    LEFT JOIN t_books tb ON ta.book_id = tb.book_id AND tb.mid IN ('GY4R', 'GY4P', 'HDMJ', 'GYF8', 'GY4S', 'GY4Q')
                    WHERE ta.do_not_use = 'FALSE')
                """;
    }

    @Override
    public String articleRef() {
        return "mfs_an";
    }

    @Override
    public BasicTerm create(String termTitle) {
        return new BenchmarkTerm(termTitle);
    }


}
