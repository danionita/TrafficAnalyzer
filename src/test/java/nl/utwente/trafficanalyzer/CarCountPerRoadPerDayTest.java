package nl.utwente.trafficanalyzer;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import org.apache.hadoop.io.DoubleWritable;

public class CarCountPerRoadPerDayTest {

	private MapDriver<Writable, Text, Text, TwovalueWritable> mapDriver;
	private ReduceDriver<Text, TwovalueWritable, Text, DoubleWritable> reduceDriver;

	@Before
	public void setUp() {
		CarCountPerRoadPerDay.MyMapper mapper = new CarCountPerRoadPerDay.MyMapper();
		CarCountPerRoadPerDay.MyReducer reducer = new CarCountPerRoadPerDay.MyReducer();

		mapDriver = MapDriver.newMapDriver(mapper);
		reduceDriver = ReduceDriver.newReduceDriver(reducer);
	}

	@Test
	public void testMapper() throws IOException {
		mapDriver.addInput(NullWritable.get(),
				new Text("RWS01_MONICA_10D001009C00D0050009,2,A1,2010,7,16282.03,"));

		mapDriver.withOutput(new Text("A1"), new IntWritable(1));

		mapDriver.runTest();
	}

	@Test
	public void testReduce() throws IOException {

		reduceDriver.withInput(new Text("hello"), Lists.newArrayList(
				new IntWritable(1), new IntWritable(2), new IntWritable(3)));

		reduceDriver.withOutput(new Text("hello"), new IntWritable(6));

		reduceDriver.runTest();
	}
}
