package net.syntio.beam;

import com.google.gson.*;
import net.syntio.beam.models.Order;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class PubSubToJDBC {
    private static final Logger LOG = Logger.getLogger(PubSubToJDBC.class.getName());

    static class TransformOrders extends DoFn<Order, String> {

        @DoFn.ProcessElement
        public void processElement(ProcessContext c) {
            Order order = c.element();
            Gson gson = new Gson();
            JsonObject orderJson = gson.toJsonTree(order).getAsJsonObject();

            // Flatten nested objects.
            flatten(orderJson, "Customer");
            flatten(orderJson, "SalesPerson");
            flatten(orderJson, "DeliveryMethod");
            // Remove orderLines as they go into another table.
            orderJson.remove("OrderLines");
            // Turn all "null" occurrences to null.
            removeNull(orderJson);

            c.output(orderJson.toString());
        }
    }

    static class TransformOrderLines extends DoFn<Order, String> {
        @DoFn.ProcessElement
        public void processElement(ProcessContext c) {
            Order order = c.element();
            Gson gson = new Gson();
            int orderId = order.getOrderID();
            JsonArray orderLinesJson = gson.toJsonTree(order.getOrderLines()).getAsJsonArray();

            for (JsonElement je : orderLinesJson) {
                if (je.isJsonObject()) {
                    // Flatten nested objects.
                    JsonObject orderLine = je.getAsJsonObject();
                    flatten(orderLine, "PackageType");
                    flatten(orderLine, "StockItem");
                    // Add the foreign key.
                    orderLine.addProperty("OrderID", orderId);

                    // Turn all "null" occurrences to null.
                    removeNull(orderLine);

                    c.output(orderLine.toString());
                } else {
                    throw new RuntimeException("Each element in orderLines is supposed to be a json object.");
                }
            }
        }
    }

    /**
     * Replace a nested object under key <code>key</code> in jsonObject
     * with elements from inside the nested object, concatenating keys with an underscore.
     *
     * @param jsonObject
     * @param key
     */
    public static void flatten(JsonObject jsonObject, String key) {
        JsonObject nestedObject = jsonObject.getAsJsonObject(key);
        for (String nestedKey : nestedObject.keySet()) {
            jsonObject.add(key + "_" + nestedKey, nestedObject.get(nestedKey));
        }
        jsonObject.remove(key);
    }


    /**
     * Removes all occurrences of the string "null" so that they are replaced by the statement setter to NULL.
     * @param jsonObject
     */
    public static void removeNull(JsonObject jsonObject) {
        List<String> toRemove = new LinkedList<>();
        for (String key : jsonObject.keySet()) {
            JsonElement je = jsonObject.get(key);
            if (je.isJsonPrimitive() && je.getAsString().equalsIgnoreCase("null")) {
                toRemove.add(key);
            }
        }

        for (String key : toRemove) {
            jsonObject.remove(key);
        }
    }

    public static void main(String[] args) {
        PubSubToJDBCOptions options = PipelineOptionsFactory.fromArgs(args)
                .withValidation()
                .as(PubSubToJDBCOptions.class);

        String ordersStatement;
        try {
            ordersStatement = Files.readString(Paths.get(options.getOrdersStatementPath()));
            LOG.info("ordersStatement: " + ordersStatement);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Could not read ordersStatement from path " + options.getOrdersStatementPath(), e);
        }
        String orderLinesStatement;
        try {
            orderLinesStatement = Files.readString(Paths.get(options.getOrderLinesStatementPath()));
            LOG.info("orderLinesStatement: " + orderLinesStatement);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Could not read ordersStatement from path " + options.getOrderLinesStatementPath(), e);
        }

        Pipeline pipeline = Pipeline.create(options);

        JdbcIO.Write<String> configuredJdbcWrite = JdbcIO.<String>write().withDataSourceConfiguration(
                JdbcIO.DataSourceConfiguration.create(
                                options.getDriver(),
                                options.getDatabaseUrl()
                        )
                        .withUsername(options.getUsername())
                        .withPassword(options.getPassword()));

        PCollection<Order> orders = pipeline.apply(
                "Read from Pub/Sub",
                PubsubIO.readAvros(Order.class).fromSubscription(options.getSubscription())
        );
        PCollection<String> ordersJson = orders.apply("Transform orders", ParDo.of(new TransformOrders()));
        PCollection<String> orderLinesJson = orders.apply("Transform orderLines", ParDo.of(new TransformOrderLines()));

         ordersJson.apply(
                     "Write Orders to JDBC",
                     configuredJdbcWrite
                        .withStatement(ordersStatement)
                        .withPreparedStatementSetter(JsonStringToQueryMapper.fromStatement(ordersStatement))
                );
        orderLinesJson.apply(
                "Write OrderLines to JDBC",
                configuredJdbcWrite
                    .withStatement(orderLinesStatement)
                    .withPreparedStatementSetter(JsonStringToQueryMapper.fromStatement(orderLinesStatement))
        );
        pipeline.run();
    }
}
