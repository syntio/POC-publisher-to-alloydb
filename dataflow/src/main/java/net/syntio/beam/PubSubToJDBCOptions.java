package net.syntio.beam;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.Validation;

public interface PubSubToJDBCOptions extends DataflowPipelineOptions {

    @Description("Pub/Sub subscription from which messages are consumed.")
    @Validation.Required
    String getSubscription();

    void setSubscription(String subscription);

    @Description("JDBC driver fqcn.")
    @Default.String("org.postgresql.Driver")
    String getDriver();

    void setDriver(String driver);

    @Description("Database URL.")
    @Validation.Required
    String getDatabaseUrl();

    void setDatabaseUrl(String databaseUrl);

    @Description("Username of the database user.")
    @Validation.Required
    String getUsername();

    void setUsername(String username);

    @Description("Password of the database user.")
    @Validation.Required
    String getPassword();

    void setPassword(String password);

    @Description("Path to the SQL statement to execute against the database.")
    @Validation.Required
    String getStatementPath();

    void setStatementPath(String statementPath);
}