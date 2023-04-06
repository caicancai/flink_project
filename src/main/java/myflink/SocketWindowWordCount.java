package myflink;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

public class SocketWindowWordCount {
    public static void main(String[] args) throws Exception {

        //入口类，可以用来设置参数河创建数据源以及提交任务
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //创建一个本地端口号9000的socket中读取数据的数据源`
        DataStream<String> text = env.socketTextStream("localhost",9000,"\n");

        DataStream<Tuple2<String, Integer>> wordCounts = text
                .flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
                        for (String word : value.split("\\s")) {
                            out.collect(Tuple2.of(word, 1));
                        }
                    }
                });

        DataStream<Tuple2<String, Integer>> windowCounts = wordCounts
                .keyBy(0)
                .timeWindow(Time.seconds(5))
                .sum(1);

        // 将结果打印到控制台，注意这里使用的是单线程打印，而非多线程
        windowCounts.print().setParallelism(1);
        env.execute("Socket Window WordCount");
    }
}
