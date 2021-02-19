package com.vivokey.nfcsnoopdecoder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Hex;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        System.out.println("NFCSnoop Decoder v0.01");
        System.out.println("Developed by VivoKey Technologies");
        byte[] byteArr = {};
        // Confirm we have the right number of args
        if (args.length < 2) {
            System.out.println("Args: infile outfile");
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])))) {
            // Read the full file
            String line;
            StringBuilder fileFull = new StringBuilder();
            while ((line = br.readLine()) != null) {
                fileFull.append(line);
            }
            // Convert it to a byte array
            byteArr = Base64.getDecoder().decode(fileFull.toString());

        } catch (FileNotFoundException e) {
            System.out.println("Unable to find infile");
            System.exit(-1);
        } catch (IOException e) {
            System.out.println("File error.");
            System.exit(-1);
        }
        // The file should be decoded successfully
        // Do the whole decoding thing
        // First 9 bytes are a header as follows:
        // byte 1: Version of NFCSnoop, byte
        // byte 2-9: Timestamp, long
        byte[] version = { byteArr[0] };
        // Wrap the long in a ByteBuffer to allow an easy read
        ByteBuffer longReader = ByteBuffer.wrap(byteArr, 1, 8);
        long lastTimestamp = longReader.getLong();
        System.out.println("NFCSnoop version: " + Hex.encodeHexString(version));
        System.out.println("Last timestamp: " + lastTimestamp);
        // Rest of the byte array is a Zlib deflate
        Inflater inflater = new Inflater();
        // Iterate over the compressed data as an arbritary size
        List<Byte> byteBuild = new ArrayList<>();
        try {

            inflater.setInput(byteArr, 9, byteArr.length - 9);
       
            while (!inflater.finished()) {
                byte[] buf = new byte[1024];
                int len = inflater.inflate(buf, 0, buf.length);
                // Easiest way to read the bytes from a byte array to a byte list
                for(int i = 0; i < len; i++) {
                    byteBuild.add(buf[i]);
                }
            }
        } catch (DataFormatException e) {
            System.out.println("Compressed data corrupt.");
            System.exit(-1);
        }
        // Should now be good to go, that the inflater's finished and we can iterate using a ByteBuffer
        Byte[] inflated = byteBuild.toArray(new Byte[0]);
        byte[] infl = new byte[inflated.length];
        // We now need to debox the Byte to byte
        int i = 0;
        for (Byte b: inflated) {
            infl[i++] = b;
        }
        ByteBuffer infbuf = ByteBuffer.wrap(infl);
        List<NFCPacket> pktLst = new ArrayList<>();
        // Loop over the array and create a list of NFCPackets
        while(infbuf.hasRemaining()) {
            infbuf.order(ByteOrder.LITTLE_ENDIAN);
            pktLst.add(new NFCPacket(infbuf));
        }
        // Should now have a big list of packets, from last recorded to first.
        // It makes more sense to have the oldest first, but we'll need to compute that.
        // Have to iterate to get a first timestamp
        long delta = 0;
        for(NFCPacket p: pktLst) {
            delta += p.getDelta();
        }
        long firstTimestamp = lastTimestamp - delta;
        // Now we have the oldest timestamp, as an absolute. 
        // We want to set each packet timestamp relative to this.
        // Once done, we can just reverse the list!
        // Should have a nice properly timed list
        // Reverse it and we're good to go
        Collections.reverse(pktLst);
        // Now print it, i guess
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1])))) {
            // Write the absolute timestamp and details
            bw.write("NFCSnooper version: " + Hex.encodeHexString(version));
            bw.newLine();
            bw.write("Absolute timestamp: " + firstTimestamp);
            bw.newLine();
            // Write the header
            bw.write(NFCPacket.getHeader());
            bw.newLine();
            // Write each packet out
            for(NFCPacket p: pktLst) {
                bw.write(p.format());
                bw.newLine();
            }
            // Done
        } catch (FileNotFoundException e) {
            // File not found?
        } catch (IOException e) {
            // File error
            System.out.println("File error.");
            System.exit(-1);
        }
        System.out.println("File written. Closing.");
        System.exit(0);

    }
}
