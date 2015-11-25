/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.utwente.trafficanalyzer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;


/**
 * @author Unmesha SreeVeni U.B
 *
 */
public class TwoObjectWritable implements Writable {
    private String first;
    private double second;

    public  TwoObjectWritable() {
        set(first, second);
    }
    public  TwoObjectWritable(String first, double second) {
        set(first, second);
    }
    public void set(String first, double second) {
        this.first = first;
        this.second = second;
    }
    public String getFirst() {
        return first;
    }
    public double getSecond() {
        return second;
    }
    @Override
    public void write(DataOutput out) throws IOException {
        out.writeChars(first);
        out.writeDouble(second);
    }
    @Override
    public void readFields(DataInput in) throws IOException {
        first = in.readLine();
        second = in.readDouble();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Long.parseLong(first);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(second);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TwoObjectWritable)) {
            return false;
        }
        TwoObjectWritable other = (TwoObjectWritable) obj;
        if (!first.equals(other.first)) {
            return false;
        }
        if (Double.doubleToLongBits(second) != Double
                .doubleToLongBits(other.second)) {
            return false;
        }
        return true;
    }
    @Override
    public String toString() {
        return first + "," + second;
    }
}