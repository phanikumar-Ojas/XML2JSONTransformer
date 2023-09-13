package com.ebsco.platform.shared.cmsimport.rs.repository.term;

import com.ebsco.platform.shared.cmsimport.rs.domain.term.BasicTerm;
import com.ebsco.platform.shared.cmsimport.rs.repository.DatabaseClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Log4j2
@RequiredArgsConstructor
public abstract class BasicTermRepository {

    protected final DatabaseClient databaseClient;

    public Map<String, Set<BasicTerm>> find() {
        Map<String, Set<BasicTerm>> terms = new HashMap<>();
        try (Statement statement = databaseClient.getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(query());
            while (resultSet.next()) {
                BasicTerm term = create(resultSet.getString("term"));
                terms.computeIfAbsent(resultSet.getString(articleRef()), k -> new HashSet<>()).add(term);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return terms;
    }

    public abstract String query();

    public abstract String articleRef();

    public abstract BasicTerm create(String termTitle);
}
