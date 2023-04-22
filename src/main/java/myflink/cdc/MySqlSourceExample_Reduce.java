package myflink.cdc;

import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class MySqlSourceExample_Reduce {
    public static void main(String[] args) throws Exception {

        MySqlSource<String> mySqlSource = MySqlSource.<String>builder()
                .hostname("192.168.11.150")
                .port(3306)
                .includeSchemaChanges(true)
                .databaseList("faker") // set captured database
                .tableList("faker.users") // set captured table
                .username("root")
                .password("root")
                .deserializer(new JsonDebeziumDeserializationSchema()) // converts SourceRecord to JSON String
                .build();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.enableCheckpointing(3000);

        DataStream<String> stream = env.fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source");

        KeyedStream<String, Character> keyed = stream.keyBy(x -> x.charAt(0));

        DataStream<String> reduced = keyed.reduce((x, y) -> x + y);

        reduced.print();
        env.execute();
    }

}
