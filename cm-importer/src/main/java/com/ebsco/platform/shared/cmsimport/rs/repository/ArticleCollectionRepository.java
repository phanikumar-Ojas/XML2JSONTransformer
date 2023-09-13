package com.ebsco.platform.shared.cmsimport.rs.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.ebsco.platform.shared.cmsimport.rs.domain.ArticleCollection;
import com.ebsco.platform.shared.cmsimport.rs.util.DataUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class ArticleCollectionRepository {

    private static final String RS_PUBLICATION_LOCATION = "Ipswich, MA";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");

    private static final String QUERY = """
    		SELECT book_id, book_title, publisher, series_id_xml, pub_date, copyright_statement, copyright_holder,
    			   rights, article_count, old_book_title, reading_level, pub_year
    		FROM t_books 
    		WHERE book_title in (
    			   'Salem Press Primary Encyclopedia',
    			   'Salem Press Encyclopedia of Science', 
    			   'Salem Press Encyclopedia',
    			   'Salem Press Biographical Encyclopedia',
    			   'Salem Press Encyclopedia of Health',
    			   'Salem Press Encyclopedia of Literature'
    			   )
    """;

    private final DatabaseClient databaseClient;

    public List<ArticleCollection> getCollections() {

        try (Statement statement = databaseClient.getConnection().createStatement()) {
            ResultSet rs = statement.executeQuery(QUERY);
            List<ArticleCollection> collections = new ArrayList<>();

            while (rs.next()) {
                LocalDate publicationDate = DataUtil.from(rs.getString("pub_date"), FORMATTER);
                int publicationYear = publicationDate != null ? publicationDate.getYear() : 0;

                ArticleCollection collection = ArticleCollection.builder()
                        .title(rs.getString("book_id"))
                        .collection_title(rs.getString("book_title"))
                        .publisher(rs.getString("publisher"))
                        .seriesIdXml(rs.getString("series_id_xml"))
                        .publicationDate(publicationDate)
                        .publicationYear(publicationYear)
                        .copyrightStatement(rs.getString("copyright_statement"))
                        .copyrightHolder(rs.getString("copyright_holder"))
                        .rights(rs.getString("rights"))
                        .publicationLocation(RS_PUBLICATION_LOCATION)
                        .articleCount(rs.getInt("article_count"))
                        .oldBookTitle(rs.getString("old_book_title"))
                        .readingLevel(rs.getString("reading_level"))
                        .build();

                collections.add(collection);
            }
            return collections;
        } catch (SQLException e) {
            log.error("Cannot process query to the database", e);
            throw new RuntimeException(e);
        }
    }
}
