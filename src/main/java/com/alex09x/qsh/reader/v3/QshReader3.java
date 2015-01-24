package com.alex09x.qsh.reader.v3;

import com.alex09x.qsh.reader.DataReader;
import com.alex09x.qsh.reader.Utils;
import com.alex09x.qsh.reader.type.StreamType;
import com.alex09x.qsh.reader.v3.DealsStream;
import com.alex09x.qsh.reader.v3.StockStream;
import com.alex09x.qsh.reader.v3.Stream;

import java.io.DataInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Iterator;

/**
 * Created by alex on 09.01.14.
 */
public class QshReader3<T> implements Iterator<T> {
    private final DataInputStream dataInput;
    private Timestamp baseDateTime;
    private Timestamp currentDateTime;
    private Stream<T> stream;
    private boolean isRead = true;

    public QshReader3(DataInputStream dataInput) throws IOException {
        this.dataInput = dataInput;

        System.out.println("Application name = " + Utils.readString(dataInput));
        System.out.println("Comment = " + Utils.readString(dataInput));

        baseDateTime = new Timestamp((Utils.readLong(dataInput) - 621355968000000000L) / 10000L);
        System.out.printf("Base Time = %s%n", baseDateTime);


        System.out.printf("Stream count = %d%n", dataInput.readUnsignedByte());
        int streamType = dataInput.readByte();


        if (StreamType.STOCK.getValue() == streamType) {
            stream = new StockStream(dataInput);
            System.out.printf("Stream type = %s%n", StreamType.STOCK);
        } else if (StreamType.DEALS.getValue() == streamType) {
            stream = new DealsStream(dataInput);
            System.out.printf("Stream type = %s%n", StreamType.DEALS);
        }
    }

    private boolean readNextRecordHeader() {
        try {
            currentDateTime = DataReader.readDateTime(dataInput, baseDateTime);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasNext() {
        return isRead = readNextRecordHeader();
    }

    @Override
    public T next() {
        if (isRead) {
            try {
                return stream.read(currentDateTime);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void remove() {
        throw new RuntimeException("Not implemented");
    }
}