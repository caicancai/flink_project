package myflink;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class MySqlSourceExample {
    public static void main(String[] args) throws Exception {
        MySqlSource<String> mySqlSource = MySqlSource.<String>builder()
                .hostname("127.0.0.1")
                .port(3306)
                .includeSchemaChanges(true)
                .databaseList("faker") // set captured database, If you need to synchronize the whole database, Please set tableList to ".*".
                .tableList("faker.student") // set captured table
                .username("root")
                .password("root")
                .deserializer(new JsonDebeziumDeserializationSchema()) // converts SourceRecord to JSON String
                .build();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // enable checkpoint

        // 设置 3s 的 checkpoint 间隔
        env.enableCheckpointing(3000);
        env.fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source")
                // set 4 parallel source tasks
                .print().setParallelism(1); // use parallelism 1 for sink to keep message ordering
        env.execute("Print MySQL Snapshot + Binlog");
    }
}
