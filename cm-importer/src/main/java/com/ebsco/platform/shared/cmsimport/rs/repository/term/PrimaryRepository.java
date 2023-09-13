package com.ebsco.platform.shared.cmsimport.rs.repository.term;

import com.ebsco.platform.shared.cmsimport.rs.domain.term.BasicTerm;
import com.ebsco.platform.shared.cmsimport.rs.domain.term.PrimaryTerm;
import com.ebsco.platform.shared.cmsimport.rs.repository.DatabaseClient;

public class PrimaryRepository extends BasicTermRepository {

    public PrimaryRepository(DatabaseClient databaseClient) {
        super(databaseClient);
    }

    @Override
    public String query() {
        return """
                SELECT term, term_type, ui from t_adhoc where ui in (select article_id
                    FROM t_articles ta
                    LEFT JOIN t_books tb ON ta.book_id = tb.book_id AND tb.mid IN ('GY4R', 'GY4P', 'HDMJ', 'GYF8', 'GY4S', 'GY4Q')
                    WHERE ta.do_not_use = 'FALSE') AND term_type='Primary'
                """;
    }

    @Override
    public String articleRef() {
        return "ui";
    }

    @Override
    public BasicTerm create(String termTitle) {
        return new PrimaryTerm(termTitle);
    }
}
