package myflink;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import com.ververica.cdc.connectors.sqlserver.SqlServerSource;
public class SqlServerExample {
    public static void main(String[] args) throws Exception {
        SourceFunction<String> sourceFunction = SqlServerSource.<String>builder()
                .hostname("127.0.0.1")
                .port(1433)
                .database("my_database") // monitor sqlserver database
                .tableList("dbo.Employee") // monitor products table
                .username("sa")
                .password("yourStrong(!)Password")
                .deserializer(new JsonDebeziumDeserializationSchema()) // converts SourceRecord to JSON String
                .build();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(3000);
        env
                .addSource(sourceFunction)
                .print().setParallelism(1); // use parallelism 1 for sink to keep message ordering

        env.execute();
    }
}
