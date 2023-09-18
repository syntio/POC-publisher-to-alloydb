package net.syntio.beam;

import com.google.common.base.Splitter;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public class JsonStringToQueryMapper implements JdbcIO.PreparedStatementSetter<String> {

    /* Logger for class.*/
    private static final Logger LOG = LoggerFactory.getLogger(JsonStringToQueryMapper.class);

    List<String> keyOrder;

    public JsonStringToQueryMapper(List<String> keyOrder) {
        this.keyOrder = keyOrder;
    }

    @Override
    public void setParameters(String element, @Nonnull PreparedStatement query) throws SQLException {
        try {
            JSONObject object = new JSONObject(element);
            for (int i = 0; i < keyOrder.size(); i++) {
                String key = keyOrder.get(i);
                if (!object.has(key) || object.get(key) == JSONObject.NULL) {
                    query.setNull(i + 1, Types.NULL);
                } else {
                    query.setObject(i + 1, object.get(key));
                }
            }
        } catch (Exception e) {
            LOG.error("Error while mapping Pub/Sub strings to JDBC", e);
            throw e;
        }
    }

    public static JsonStringToQueryMapper fromStatement(String statement) {
        int startIndex = statement.indexOf("(");
        int endIndex = statement.indexOf(")");
        String data = statement.substring(startIndex + 1, endIndex);
        List<String> keyOrder = Splitter.on(',').trimResults().splitToList(data);

        return new JsonStringToQueryMapper(keyOrder);
    }
}