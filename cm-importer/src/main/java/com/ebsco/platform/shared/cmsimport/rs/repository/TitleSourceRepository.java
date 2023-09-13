package com.ebsco.platform.shared.cmsimport.rs.repository;

import com.ebsco.platform.shared.cmsimport.rs.domain.ContentTypeReference;
import com.ebsco.platform.shared.cmsimport.rs.domain.Product;
import com.ebsco.platform.shared.cmsimport.rs.domain.TitleSource;
import com.ebsco.platform.shared.cmsimport.rs.util.DataUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.math.NumberUtils;

@RequiredArgsConstructor
@Log4j2
public class TitleSourceRepository {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");
	
	private static final String MAIN_QUERY = """
			SELECT distinct tb.book_id, tb.book_title, tb.series_id, tb.publisher, tb.mid, tb.isbn, tb.series_id_xml,
				            tb.pub_date, tb.source, tb.doctype, tb.book_note, tb.pub_year, tb.publisher_loc, tb.copyright_statement, tb.copyright_holder,
			                tb.date_added, tb.data_format, tb.rights
			FROM t_articles ta, t_books tb, t_product_assignment pa
			WHERE ta.book_id = tb.book_id and ta.mfs_an = pa.an and ta.do_not_use = 'FALSE' and tb.mid in ('GY4R', 'GY4P', 'HDMJ', 'GYF8', 'GY4S', 'GY4Q')
	""";
	
	private static final String BOOK_ID_2_PRODUCT_QUERY = """
			SELECT DISTINCT tb.book_id, pa.product 
			FROM t_product_assignment pa, t_articles ta, t_books tb
			WHERE tb.book_id = ta.book_id AND ta.article_an = pa.an AND tb.mid in ('GY4R', 'GY4P', 'HDMJ', 'GYF8', 'GY4S', 'GY4Q')
	""";
	
	private static final String ID_COLUMN = "book_id";
	
	private final DatabaseClient databaseClient;

    public List<TitleSource> find(ReferenceBinder<String, Product> numCode2ProductBinder) {
        try (Statement statement = databaseClient.getConnection().createStatement()) {
            
        	ResultSet rs = statement.executeQuery(MAIN_QUERY);
            
            log.info("Building entities ...");
            Map<String, TitleSource> idToEntities = new HashMap<>();
            while (rs.next()) {
            	String id = rs.getString(ID_COLUMN);
            	var entity = TitleSource.builder()
            			.title(id)
            			.bookTitle(rs.getString("book_title"))
            			.bookSeriesId(rs.getString("series_id"))
            			.publisher(rs.getString("publisher"))
            			.mid(rs.getString("mid"))
            			.isbn(rs.getString("isbn"))
            			.seriesIdXml(rs.getString("series_id_xml"))
            			.publicationDate(DataUtil.from(rs.getString("pub_date"), FORMATTER))
            			.source(rs.getString("source"))
            			.docType(rs.getString("doctype"))
            			.bookNote(rs.getString("book_note"))
            			.publisherLocation(rs.getString("publisher_loc"))
            			.copyrightStatement(rs.getString("copyright_statement"))
            			.copyrightHolder(rs.getString("copyright_holder"))
            			.dateAdded(DataUtil.from(rs.getString("date_added"), FORMATTER))
            			.dataFormat(rs.getString("data_format"))
            			.rights(rs.getString("rights")).build();
            	String pubYear = rs.getString("pub_year");
            	if (NumberUtils.isParsable(pubYear)) {
            		entity.setPublicationYear(Integer.valueOf(pubYear));
            	} else if (Objects.nonNull(entity.getPublicationDate())) {
            		entity.setPublicationYear(entity.getPublicationDate().getYear());
            	} else {
            		log.warn("Could not set publicationYear, pub_year={}, pub_date={}, {}",
            				pubYear, rs.getString("pub_date"), entity);
            	}
            	idToEntities.putIfAbsent(id, entity);
            }
            
            log.info("Reading products references from db ...");
            rs = statement.executeQuery(BOOK_ID_2_PRODUCT_QUERY);
            while (rs.next()) {
            	String id = rs.getString(ID_COLUMN);
            	TitleSource entity = idToEntities.get(id);
            	if (entity != null) {
            		if (entity.getProducts() == null) {
            			entity.setProducts(new HashSet<>());
            		}
            		
            		ContentTypeReference<Product> ref = new ContentTypeReference<>(numCode2ProductBinder.bind(rs.getString("product")));
            		entity.getProducts().add(ref);
            	}
            }
            return idToEntities.values().stream().toList();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
