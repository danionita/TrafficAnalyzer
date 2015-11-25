/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.utwente.trafficanalyzer;

/*
 * Cloud9: A MapReduce Library for Hadoop
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

/**
 * Tool to count words in text.
 *
 */
public class CarCountPerRoadPerDayIncreasedValidity extends Configured implements Tool {

    private static final Logger LOG = Logger.getLogger(CarCountPerRoadPerDayIncreasedValidity.class);

    /*
	 * Mapper
     */
    public static class MyMapper extends
            Mapper<Writable, Text, Text, TwovalueWritable> {
        // have to be equal to the last two type arguments to Mapper<> above

        public static final Class<?> KOUT = Text.class;
        public static final Class<?> VOUT = TwovalueWritable.class;
        private final List<String> validSensors = new ArrayList<>();

        @Override
        public void setup(Context context) throws IOException,
                InterruptedException {
            super.setup(context);
            LOG.info("Starting MyMapper");

            try {
                Path pt = new Path("hdfs://http://ctit048.ewi.utwente.nl:9000/user/ionitad/ReadingsPerSensor.csv");
                FileSystem fs = FileSystem.get(new Configuration());
                BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(pt)));
                String line;
                line = br.readLine();
                while (line != null) {
                    validSensors.add(line.split(",")[0]);
                    LOG.info("Adding sensor: "+line.split(",")[0]);
                }
            } catch (Exception e) {
                   LOG.warn("Could not load valid sensor file");
            }
            LOG.info("TOTAL VALID SENSORS: "+ validSensors.size());

        }

        @Override
        public void map(Writable key, Text line, Context context)
                throws IOException, InterruptedException {
            String[] fields = line.toString().split(",");
            String fileName = ((FileSplit) context.getInputSplit()).getPath().getName();
            double val = 0;

            val = Float.valueOf(fields[5]);

            Text roadDayYear = new Text(fields[2] + "\t" + fields[3] + "\t" + fields[4]);
            //LOG.info("checking sensor: "+fields[0]);
            if(validSensors.contains(fields[0])){
            context.write(roadDayYear, new TwovalueWritable(val, 1));
            LOG.info("VALID meadurement found!");
            }
        }

        @Override
        public void cleanup(Context context) throws IOException,
                InterruptedException {
            super.cleanup(context);
        }
    }

    /*
	 * Combiner Input has to be equal to the output of MyMapper and output has
	 * to be equal to MyReducer.
     */
    public static class MyCombiner extends
            Reducer<Text, TwovalueWritable, Text, TwovalueWritable> {

        @Override
        public void setup(Context context) throws IOException,
                InterruptedException {
            super.setup(context);
        }

        @Override
        public void reduce(Text key, Iterable<TwovalueWritable> values,
                Context context) throws IOException, InterruptedException {
            double sum = 0;
            for (TwovalueWritable counts : values) {
                sum += counts.getFirst();
            }
            context.write(key, new TwovalueWritable(sum, 1));
        }

        @Override
        public void cleanup(Context context) throws IOException,
                InterruptedException {
            super.cleanup(context);
        }
    }


    /*
	 * Partitioner The partitioner determines in which partition a record is
	 * shuffled before the reducer is called.
     */
    public static class MyPartitioner extends Partitioner<Text, IntWritable> {

        @Override
        public int getPartition(Text key, IntWritable value, int numPartitions) {
            return key.toString().hashCode() % numPartitions;
        }
    }

    /*
	 * Reducer
     */
    public static class MyReducer extends
            Reducer<Text, TwovalueWritable, Text, IntWritable> {
        // have to be equal to the last two type arguments to Reducer<> above

        public static final Class<?> KOUT = Text.class;
        public static final Class<?> VOUT = IntWritable.class;

        @Override
        public void setup(Context context) throws IOException,
                InterruptedException {
            super.setup(context);
            LOG.info("Starting reducer");

        }

        @Override
        public void reduce(Text key, Iterable<TwovalueWritable> values,
                Context context) throws IOException, InterruptedException {
            double sum = 0;
            for (TwovalueWritable counts : values) {
                sum += counts.getFirst();
            }
            int roundedSum = (int) Math.round(sum);
            context.write(key, new IntWritable(roundedSum));
        }

        @Override
        public void cleanup(Context context) throws IOException,
                InterruptedException {
            super.cleanup(context);
        }
    }

    public void run(String inputPath, String outPath) throws Exception {
        Configuration conf = getConf();
        Job job = Job.getInstance(conf);
        job.setJarByClass(CarCountPerRoadPerDayIncreasedValidity.class);
        job.setJobName(String.format("%s [%s, %s]", this.getClass()
                .getName(), inputPath, outPath));

        // -- check if output directory already exists; and optionally delete
        String outputAlreadyExistsOption = "exit";
        Path outDir = new Path(outPath);
        if (FileSystem.get(conf).exists(outDir)) {
            if (outputAlreadyExistsOption.equalsIgnoreCase("delete")) {
                FileSystem.get(conf).delete(outDir, true);
            } else {
                System.err.println("Directory " + outPath + " already exists; exiting");
                System.exit(1);
            }
        }

        // ---- Input (Format) Options
        String inputFormat = "text";
        if (inputFormat.equalsIgnoreCase("text")) {
            job.setInputFormatClass(TextInputFormat.class);
        } else if (inputFormat.equalsIgnoreCase("text")) {
            job.setInputFormatClass(SequenceFileInputFormat.class);
        }
        // Utils.recursivelyAddInputPaths(job, new Path(inputPath));
        FileInputFormat.addInputPath(job, new Path(inputPath));
        // Add files that should be available localy at each mapper
        // Utils.addCacheFiles(job, new String[] { });

        // ---- Mapper
        job.setMapperClass(MyMapper.class);
        job.setMapOutputKeyClass(MyMapper.KOUT);
        job.setMapOutputValueClass(MyMapper.VOUT);

        // ---- Combiner
        job.setCombinerClass(MyCombiner.class);

        // ---- Partitioner
        // job.setPartitionerClass(MyPartitioner.class);
        // ---- Reducer
        // set the number of reducers to influence the number of output files
        job.setNumReduceTasks(1);
        job.setReducerClass(MyReducer.class);
        job.setOutputKeyClass(MyReducer.KOUT);
        job.setOutputValueClass(MyReducer.VOUT);

        // ---- Output Options
        String outputFormat = "text";
        if (outputFormat.equalsIgnoreCase("sequence")) {
            job.setOutputFormatClass(SequenceFileOutputFormat.class);
        } else if (outputFormat.equalsIgnoreCase("text")) {
            job.setOutputFormatClass(TextOutputFormat.class);
        } else if (outputFormat.equalsIgnoreCase("null")) {
            job.setOutputFormatClass(NullOutputFormat.class);
        }
        FileOutputFormat.setOutputPath(job, outDir);
        FileOutputFormat.setCompressOutput(job, false);

        // ---- Start job
        job.waitForCompletion(true);
        return;
    }

    @SuppressWarnings("static-access")
    @Override
    public int run(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(OptionBuilder.withArgName("path").hasArg()
                .withDescription("Input").create("input"));
        options.addOption(OptionBuilder.withArgName("path").hasArg()
                .withDescription("Output").create("output"));

        CommandLine cmdline;
        CommandLineParser parser = new GnuParser();
        try {
            cmdline = parser.parse(options, args);
        } catch (ParseException exp) {
            System.err.println("Error parsing command line: "
                    + exp.getMessage());
            return -1;
        }

        String inputPath = cmdline.getOptionValue("input",
                System.getenv("input"));
        String outputPath = cmdline.getOptionValue("output",
                System.getenv("output"));

        if (inputPath == null || outputPath == null) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(this.getClass().getName(), options);
            ToolRunner.printGenericCommandUsage(System.out);
            return -1;
        }

        LOG.info("Tool name: " + this.getClass().getName());
        LOG.info(" - input: " + inputPath);
        LOG.info(" - output file: " + outputPath);

        FileSystem.get(getConf()).delete(new Path(outputPath), true);
        run(inputPath, outputPath);
        return 0;
    }

    public CarCountPerRoadPerDayIncreasedValidity() {
    }

    public static void main(String[] args) throws Exception {
        ToolRunner.run(new CarCountPerRoadPerDayIncreasedValidity(), args);
    }
}
