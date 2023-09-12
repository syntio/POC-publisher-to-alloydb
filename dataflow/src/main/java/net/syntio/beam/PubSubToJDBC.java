package net.syntio.beam;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.syntio.beam.models.Order;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class PubSubToJDBC {
    private static final Logger LOG = Logger.getLogger(PubSubToJDBC.class.getName());

    static class TransformNestedObjects extends DoFn<Order, String> {

        @DoFn.ProcessElement
        public void processElement(ProcessContext c) {
            Order order = c.element();
            Gson gson = new Gson();
            JsonObject orderJson = gson.toJsonTree(order).getAsJsonObject();

            // Flatten nested objects.
            flatten(orderJson, "Customer");
            flatten(orderJson, "SalesPerson");
            flatten(orderJson, "DeliveryMethod");
            // Convert JsonArray to string for writing as "json".
            JsonElement orderLines = orderJson.get("OrderLines");
            orderJson.remove("OrderLines");
            orderJson.addProperty("OrderLines", orderLines.toString());

            c.output(orderJson.toString());
        }

        /**
         * Replace a nested object under key <code>key</code> in jsonObject
         * with elements from inside the nested object, concatenating keys with an underscore.
         *
         * @param jsonObject
         * @param key
         */
        private static void flatten(JsonObject jsonObject, String key) {
            JsonObject nestedObject = jsonObject.getAsJsonObject(key);
            for (String nestedKey : nestedObject.keySet()) {
                jsonObject.add(key + "_" + nestedKey, nestedObject.get(nestedKey));
            }
            jsonObject.remove(key);
        }
    }

    public static void main(String[] args) {
        PubSubToJDBCOptions options = PipelineOptionsFactory.fromArgs(args)
                .withValidation()
                .as(PubSubToJDBCOptions.class);

        String statement;
        try {
            statement = Files.readString(Paths.get(options.getStatementPath()));
            LOG.info("statement: " + statement);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Could not read statement from path " + options.getStatementPath(), e);
        }

        Pipeline pipeline = Pipeline.create(options);
        pipeline.apply("Read from PubSub", PubsubIO.readAvros(Order.class).fromSubscription(options.getSubscription()))
                .apply("Transform nested objects", ParDo.of(new TransformNestedObjects()))
                .apply("Write to JDBC", JdbcIO.<String>write()
                        .withDataSourceConfiguration(
                                JdbcIO.DataSourceConfiguration.create(
                                                options.getDriver(),
                                                options.getDatabaseUrl()
                                        )
                                        .withUsername(options.getUsername())
                                        .withPassword(options.getPassword())
                        )
                        .withStatement(statement)
                        .withPreparedStatementSetter(JsonStringToQueryMapper.fromStatement(statement)));
        pipeline.run();
    }
}
