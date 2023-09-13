package com.ebsco.platform.shared.cmsimport.rs.repository.topic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.Topic;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBiography;
import com.ebsco.platform.shared.cmsimport.rs.repository.DatabaseClient;
import com.ebsco.platform.shared.cmsimport.rs.util.DataUtil;

import lombok.Getter;
import lombok.Setter;

public class TopicBiographyRepository extends TopicRepository {
	
	private static final String SALEM_NAMES_QUERY = """
            SELECT "KEY" as key, "First" as first_name, "Last" as last_name FROM t_salem_names WHERE ("First" IS NOT NULL OR "Last" IS NOT NULL)
            """;
	
	public TopicBiographyRepository(DatabaseClient databaseClient) {
		super(databaseClient);
	}

	@Override
	protected String query() {
		return """
		   		 SELECT  ta.article_id, ta.article_title, ta.brst_category
		   		 FROM t_articles ta
		   		 LEFT JOIN t_books tb ON ta.book_id = tb.book_id AND tb.mid IN ('GY4R', 'GY4P', 'HDMJ', 'GYF8', 'GY4S', 'GY4Q')
		   		 WHERE ta.do_not_use = 'FALSE' AND ta.brst_topic IN ('Biography')
		   """;
	}

	@Override
	protected Topic create(ResultSet rs) throws SQLException {
		return addBrstCategoryTag(new TopicBiography(), rs);
	}
	
	public Map<String, SalemName> getSalemNames() {
    	QueryRunner run = new QueryRunner(databaseClient.getDataSource());
		try {
			return run.query(SALEM_NAMES_QUERY, new ResultSetHandler<Map<String, SalemName>>() {
				
				public Map<String, SalemName> handle(ResultSet rs) throws SQLException {
					Map<String, SalemName> result = new HashMap<>();
			        while (rs.next()) {
			        	String key = createKey(rs);
			        	if (Objects.nonNull(key)) {
			        		result.put(key, createRow(rs));
			        	}
			        }
			        return result;
			    }
				
				private String createKey(ResultSet rs) throws SQLException {
					String key = rs.getString("key");
					return DataUtil.onlyDigitsOrNull(key);
				}

				private SalemName createRow(ResultSet rs) throws SQLException {
					SalemName name = new SalemName();
					name.setFirstName( rs.getString("first_name"));
					name.setFirstName( rs.getString("last_name"));
					return name;
				}
	        });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
	
	@Getter
	@Setter
	public static class SalemName {
		private String firstName;
		private String lastName;
	}
}
