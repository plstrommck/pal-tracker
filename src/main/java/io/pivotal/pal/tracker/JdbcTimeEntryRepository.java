package io.pivotal.pal.tracker;

import java.sql.PreparedStatement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.Date;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class JdbcTimeEntryRepository implements TimeEntryRepository {

    private DataSource dataSource;
    private JdbcTemplate template;

    private static String FIND_QUERY = "select id, project_id, user_id, date, hours from time_entries where id = ?";
    private static String DELETE_QUERY = "delete from time_entries where id = ?";
    private static String UPDATE_QUERY = "UPDATE time_entries SET project_id = ?, user_id = ?, date = ?,  hours = ? WHERE id = ?";
    private static String INSERT_QUERY = "INSERT INTO time_entries (project_id, user_id, date, hours) VALUES (?, ?, ?, ?)";
    private static String LIST_QUERY = "SELECT id, project_id, user_id, date, hours FROM time_entries";

    public JdbcTimeEntryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        template = new JdbcTemplate(dataSource);
    }

    @Override
    public TimeEntry create(TimeEntry entry) {

        KeyHolder generatedKeyHolder = new GeneratedKeyHolder();

        template.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO time_entries (project_id, user_id, date, hours) " +
                            "VALUES (?, ?, ?, ?)",
                    RETURN_GENERATED_KEYS
            );

            statement.setLong(1, entry.getProjectId());
            statement.setLong(2, entry.getUserId());
            statement.setDate(3, Date.valueOf(entry.getDate()));
            statement.setInt(4, entry.getHours());

            return statement;
        }, generatedKeyHolder);

        return find(generatedKeyHolder.getKey().longValue());
    }

    @Override
    public TimeEntry find(long id) {
        return template.query(FIND_QUERY,new Object[]{id},extractor);
    }

    @Override
    public List<TimeEntry> list() {
        return template.query(LIST_QUERY, mapper);
    }

    @Override
    public TimeEntry update(long id, TimeEntry entry) {
        template.update(UPDATE_QUERY,
                entry.getProjectId(),
                entry.getUserId(),
                Date.valueOf(entry.getDate()),
                entry.getHours(),
                id);

        return find(id);
    }

    @Override
    public void delete(long id) {
        template.update(DELETE_QUERY,id);
    }

    private final RowMapper<TimeEntry> mapper = (rs, rowNum) -> new TimeEntry(
            rs.getLong("id"),
            rs.getLong("project_id"),
            rs.getLong("user_id"),
            rs.getDate("date").toLocalDate(),
            rs.getInt("hours")
    );

    private final ResultSetExtractor<TimeEntry> extractor =
            (rs) -> rs.next() ? mapper.mapRow(rs, 1) : null;


}
