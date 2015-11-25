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
				new Text("RWS01_MONICA_00D001074023D007000F,2,A1,2010,7,21258.2,\n"));
                mapDriver.addInput(NullWritable.get(),
				new Text("\"RWS01_MONICA_00D001074023D007000F,3,A1,2010,7,351.17,\n"));


                mapDriver.withOutput(new Text("A1_2010_7"), new TwovalueWritable(21258.19921875,1.0));
		mapDriver.withOutput(new Text("A1_2010_7"), new TwovalueWritable(351.1700134277344,1.0));
                
		mapDriver.runTest();
	}

	@Test
	public void testReduce() throws IOException {

		reduceDriver.withInput(new Text("A1_2010_7"), Lists.newArrayList(
				new TwovalueWritable(351.1700134277344,1.0), new TwovalueWritable(21258.19921875,1.0)));

		reduceDriver.withOutput(new Text("A1_2010_7"), new DoubleWritable(21609.369232177734));

		reduceDriver.runTest();
	}
}
