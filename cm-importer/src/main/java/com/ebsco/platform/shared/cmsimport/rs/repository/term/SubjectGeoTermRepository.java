package com.ebsco.platform.shared.cmsimport.rs.repository.term;

import com.ebsco.platform.shared.cmsimport.rs.domain.term.BasicTerm;
import com.ebsco.platform.shared.cmsimport.rs.domain.term.SubjectGeoTerm;
import com.ebsco.platform.shared.cmsimport.rs.repository.DatabaseClient;

public class SubjectGeoTermRepository extends BasicTermRepository {

    public SubjectGeoTermRepository(DatabaseClient databaseClient) {
        super(databaseClient);
    }

    @Override
    public String query() {
        return """
                SELECT term, term_type, an from t_build_lookup_editorial where an in (select mfs_an
                    FROM t_articles ta
                    LEFT JOIN t_books tb ON ta.book_id = tb.book_id AND tb.mid IN ('GY4R', 'GY4P', 'HDMJ', 'GYF8', 'GY4S', 'GY4Q')
                    WHERE ta.do_not_use = 'FALSE') and term_type='SubjectGeo'
                """;
    }

    @Override
    public String articleRef() {
        return "an";
    }

    @Override
    public BasicTerm create(String termTitle) {
        return new SubjectGeoTerm(termTitle);
    }
}
