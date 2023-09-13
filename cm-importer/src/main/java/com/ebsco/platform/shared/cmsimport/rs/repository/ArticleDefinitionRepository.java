package com.ebsco.platform.shared.cmsimport.rs.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ebsco.platform.shared.cmsimport.rs.domain.ArticleDefinition;
import com.ebsco.platform.shared.cmsimport.rs.domain.ContentTypeReference;
import com.ebsco.platform.shared.cmsimport.rs.domain.TitleSource;
import com.ebsco.platform.shared.cmsimport.rs.util.DataUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class ArticleDefinitionRepository {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yy");
    
    private static final String QUERY = """
    		 SELECT  ta.mfs_an, tb.mid, ta.review_date, tb.book_id
    		 FROM t_articles ta
    		 LEFT JOIN t_books tb ON ta.book_id = tb.book_id AND tb.mid IN ('GY4R', 'GY4P', 'HDMJ', 'GYF8', 'GY4S', 'GY4Q')
    		 WHERE ta.do_not_use = 'FALSE'
    """;
    
    private final DatabaseClient databaseClient;

    public List<ArticleDefinition> find(ReferenceBinder<String, ContentTypeReference<TitleSource>> titleSourceBinder) {

        try (Statement statement = databaseClient.getConnection().createStatement()) {

            ResultSet rs = statement.executeQuery(QUERY);
            List<ArticleDefinition> articleDefinitions = new ArrayList<>();
            log.info("Building entities ...");
            while (rs.next()) {
                LocalDate dtFormat = DataUtil.from(rs.getString("review_date"), FORMATTER);

                ArticleDefinition entry = ArticleDefinition.builder()
                        .an(rs.getString("mfs_an"))
                        .dtformat(dtFormat)
                        .title(rs.getString("mid"))
                        .build();
                
                String bookId = rs.getString("book_id");
                if (Objects.nonNull(bookId)){
                	if (Objects.isNull(entry.getTitleSource())) {
                    	entry.setTitleSource(new HashSet<>());
            		}
                    entry.getTitleSource().add(titleSourceBinder.bind(bookId));
                } else {
                	log.warn("ArticleDefinition book_id=null, {}", entry);
                }
                articleDefinitions.add(entry);
            }
            return articleDefinitions;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static class TitleSourceReferenceBinder implements ReferenceBinder<String, ContentTypeReference<TitleSource>> {
    	
    	private final Map<String, TitleSource> bookId2titleSource;
    	
    	public TitleSourceReferenceBinder(Collection<TitleSource> titleSources) {
    		log.info("Init titleSources reference binder ...");
    		this.bookId2titleSource = titleSources.stream().collect(Collectors.toMap(TitleSource::getTitle, Function.identity()));
			log.info("done");
		}

		public ContentTypeReference<TitleSource> bind(String bookId) {
    		ContentTypeReference<TitleSource> ref = new ContentTypeReference<>(bookId2titleSource.get(bookId));
    		return ref;
    	}
    }
}
