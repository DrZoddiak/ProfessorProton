package io.github.nucleuspowered.proton.data;

import io.github.nucleuspowered.proton.ProfessorProton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class H2Database implements Database {

    public H2Database() throws SQLException {
        initialize();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:./professorproton");
    }

    private void initialize() throws SQLException {
        String createWarningsTable = "create table if not exists warning ( "
                + "id uuid default random_uuid() primary key, "
                + "member long not null, "
                + "datetime timestamp not null, "
                + "reason varchar not null, "
                + "message varchar not null, "
                + ")";
        try (Connection connection = getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(createWarningsTable);
        }
    }

    @Override
    public void logWarning(Warning warning) {
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("insert into warning values (default, ?, ?, ?, ?)")) {
            statement.setLong(1, warning.getMember());
            statement.setObject(2, warning.getDateTime());
            statement.setString(3, warning.getReason());
            statement.setString(4, warning.getMessage());
            statement.execute();
        } catch (SQLException e) {
            ProfessorProton.LOGGER.error("Failed to log warning to DB.", e);
        }
    }

    @Override
    public List<Warning> getWarnings(long member) {
        List<Warning> warnings = new ArrayList<>();
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("select datetime, reason, message from warning where member = ?")) {
            statement.setLong(1, member);
            final ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                OffsetDateTime dateTime = resultSet.getObject(1, OffsetDateTime.class);
                String reason = resultSet.getString(2);
                String message = resultSet.getString(3);
                warnings.add(new Warning(member, dateTime, reason, message));
            }
        } catch (SQLException e) {
            ProfessorProton.LOGGER.error("Failed to read warnings from DB.", e);
        }
        return warnings;
    }

    @Override
    public int getWarningCount(long member) {
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("select count(*) from warning where member = ?")) {
            statement.setLong(1, member);
            final ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            ProfessorProton.LOGGER.error("Failed to count warnings in DB.", e);
        }
        return 0;
    }

    @Override
    public Optional<Warning> getLastWarning(long member) {
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "select datetime, reason, message from warning where member = ? order by datetime desc limit 1")) {
            statement.setLong(1, member);
            final ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                OffsetDateTime dateTime = resultSet.getObject(1, OffsetDateTime.class);
                String reason = resultSet.getString(2);
                String message = resultSet.getString(3);
                return Optional.of(new Warning(member, dateTime, reason, message));
            }
        } catch (SQLException e) {
            ProfessorProton.LOGGER.error("Failed to get last warning from DB.", e);
        }
        return Optional.empty();
    }
}
