package com.ebsco.platform.shared.cmsimport.rs.repository.term;

import com.ebsco.platform.shared.cmsimport.rs.domain.term.SearchTermSet;
import com.ebsco.platform.shared.cmsimport.rs.repository.DatabaseClient;
import com.ebsco.platform.shared.cmsimport.rs.util.DataUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class SearchTermSetRepository {

    private static final String SEARCH_TERM_SET_QUERY = """
            SELECT ta.article_id, brst_topic, article_title, ta.mfs_an, word_count, lexile
               FROM t_articles ta
               LEFT JOIN t_books tb ON ta.book_id = tb.book_id AND tb.mid IN ('GY4R', 'GY4P', 'HDMJ', 'GYF8', 'GY4S', 'GY4Q')
               LEFT JOIN t_lexiles tl ON ta.mfs_an = tl.mfs_an
              WHERE ta.do_not_use = 'FALSE'
            """;

    private final DatabaseClient databaseClient;

    public List<SearchTermSet> find() {
        List<SearchTermSet> termSets = new ArrayList<>();

        try (Statement statement = databaseClient.getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(SEARCH_TERM_SET_QUERY);

            while (resultSet.next()) {
                String articleType = "Biography".equals(resultSet.getString("brst_topic"))
                        ? "Biography" : "Article";
                Integer wordsCount = DataUtil.parseIntOrRange(resultSet.getString("word_count"));
                String articleTitle = resultSet.getString("article_title");
                SearchTermSet currentSet = SearchTermSet.builder()
                        .articleAn(DataUtil.parseLong(resultSet.getString("mfs_an")))
                        .articleType(articleType)
                        .articleTitle(articleTitle + ".")
                        .docType("Encyclopedia")
                        .lexile(DataUtil.parseIntOrRange(resultSet.getString("lexile")))
                        .pageNumber(DataUtil.calculatePages(wordsCount))
                        .words(wordsCount)
                        .title(articleTitle)
                        .articleId(resultSet.getString("article_id"))
                        .build();

                termSets.add(currentSet);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return termSets;
    }
}
