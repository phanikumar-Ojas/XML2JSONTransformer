package com.ebsco.platform.shared.cmsimport.rs.repository.topic;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.Topic;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicEducation;
import com.ebsco.platform.shared.cmsimport.rs.repository.DatabaseClient;

public class TopicEducationRepository  extends TopicRepository {
	
	public TopicEducationRepository(DatabaseClient databaseClient) {
		super(databaseClient);
	}

	@Override
	protected String query() {
		return """
		   		 SELECT  ta.article_id, ta.article_title, ta.brst_category
		   		 FROM t_articles ta
		   		 LEFT JOIN t_books tb ON ta.book_id = tb.book_id AND tb.mid IN ('GY4R', 'GY4P', 'HDMJ', 'GYF8', 'GY4S', 'GY4Q')
		   		 WHERE ta.do_not_use = 'FALSE' AND ta.brst_topic IN ('Educational Resources', 'Educator Resources')
		   """;
	}

	@Override
	protected Topic create(ResultSet rs) throws SQLException {
		return addBrstCategoryTag(new TopicEducation(), rs);
	}
}
