package com.ebsco.platform.shared.cmsimport.rs.repository.topic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.Topic;
import com.ebsco.platform.shared.cmsimport.rs.repository.DatabaseClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public abstract class TopicRepository {
	 
	 private static final String MOVEMENT_NAME_BY_BIO_REF_QUERY = """
	            SELECT br.bio_ref, m.name FROM t_bio_ref br, t_movement m WHERE br.movement_id = m.id ORDER BY m.id
	            """;
	
    protected final DatabaseClient databaseClient;
    
    private Function<String, String> titleValueResolver = Function.identity();

    public Map<String, Topic> find(String...onlyThisArticleId) {
        try (Statement statement = databaseClient.getConnection().createStatement()) {
            ResultSet rs = statement.executeQuery(query());
            Map<String, Topic> result = new HashMap<>();
            log.info("Building entities ...");
            while (rs.next()) {
            	String articleId = rs.getString("article_id");
            	if (onlyThisArticleId.length > 0 && !ArrayUtils.contains(onlyThisArticleId, articleId)) { 
            		continue;
            	}
            	Topic topic = create(rs);
            	String title = rs.getString("article_title");
				topic.setTitle(titleValueResolver.apply(title));
            	topic.setArticleIds(Set.of(articleId));
            	result.put(articleId, topic);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected abstract String query();
    
    protected abstract Topic create(ResultSet rs) throws SQLException;
    
    protected Topic addBrstCategoryTag(Topic topic, ResultSet rs) throws SQLException{
    	String brstCategory = rs.getString("brst_category");
    	Collection<String> tags = new LinkedHashSet<>();
		if (StringUtils.isNoneBlank(brstCategory)) {
			tags.add(brstCategory);
		}
		topic.setTags(tags);
		return topic;
    }
    
    public Map<String, Collection<String>> getBioRef2MovementName() {
    	QueryRunner run = new QueryRunner(databaseClient.getDataSource());
		try {
			return run.query(MOVEMENT_NAME_BY_BIO_REF_QUERY, new ResultSetHandler<Map<String, Collection<String>>>() {
				
				public Map<String, Collection<String>> handle(ResultSet rs) throws SQLException {
					Map<String, Collection<String>> result = new HashMap<>();
			        while (rs.next()) {
			        	String key = createKey(rs);
			        	Collection<String> values = result.get(key);
			        	if (Objects.isNull(values)) {
			        		result.put(key, values = Lists.newArrayList());
			        	}
			        	values.add(createRow(rs));
			        }
			        return result;
			    }
				
				private String createKey(ResultSet rs) throws SQLException {
					return rs.getString("bio_ref");
				}

				private String createRow(ResultSet rs) throws SQLException {
					return rs.getString("name");
				}
	        });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setTitleValueResolver(Function<String, String> titleValueResolver) {
        this.titleValueResolver = titleValueResolver;
    }
}
