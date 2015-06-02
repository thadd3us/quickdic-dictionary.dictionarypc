
package com.hughes.android.dictionary.wiktionary;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

public class WiktionaryScraper {

    static final int articlesPerFile = 1000;

    static final Logger log = Logger.getGlobal();
    static final int numThreads = 10;
    private static final ExecutorService pool = Executors.newFixedThreadPool(numThreads);
    
    public static Future<byte[]> startDownloading(final URL url) throws IOException {
        return pool.submit(new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                return getStream(url.openStream());
            }
        });
    }

    public static void main(String[] args) throws IOException {
        final String wiktionary = args[0];
        final File outputDir = new File(args[1]);
        final int startIndex = Integer.valueOf(args[2]);
        final int endIndex = Integer.valueOf(args[3]);

        int current_out_file = -1;
        DataOutputStream out_stream = null;
        BufferedWriter error_stream = null;

        for (int page_id = startIndex; page_id < endIndex;) {
            if (page_id / articlesPerFile != current_out_file) {
                current_out_file = page_id / articlesPerFile;
                if (out_stream != null) {
                    out_stream.close();
                    error_stream.close();
                }
                out_stream = new DataOutputStream(
                        new GZIPOutputStream(
                        new BufferedOutputStream(
                        new FileOutputStream(new File(
                        outputDir,
                        String.format("static_html_%04d.gz", current_out_file))))));
                error_stream = new BufferedWriter(new FileWriter(new File(outputDir, String.format(
                        "errors_%04d.txt", current_out_file))));
            }

            try {
                final String format = "format=xml&prop=displaytitle|text|revid&disabletoc&mobileformat";
                Map<Integer, Future<byte[]>> futures = new LinkedHashMap<Integer, Future<byte[]>>();
                for (int offset = 0; offset < numThreads; ++offset) {
                    URL url = new URL(
                            String.format(
                                    "http://%s/w/api.php?action=parse&pageid=%d&%s",
                                    wiktionary, page_id, format));
                    futures.put(page_id, startDownloading(url));
                    ++page_id;
                    
                }
                
                for (Map.Entry<Integer, Future<byte[]>> entry : futures.entrySet()) {
                    System.out.println(String.format("Writing page: %d", entry.getKey()));
                    out_stream.writeInt(entry.getKey());
                    out_stream.writeInt(entry.getValue().get().length);
                    out_stream.write(entry.getValue().get());
                }
                
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        out_stream.close();
        error_stream.close();
        pool.shutdown();
    }

    public static byte[] getStream(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ReadableByteChannel inChannel = Channels.newChannel(in);
        WritableByteChannel outChannel = Channels.newChannel(baos);
        copy(inChannel, outChannel);
        final byte[] result = baos.toByteArray();
        inChannel.close();
        outChannel.close();
        return result;
    }

    public static void copy(ReadableByteChannel in, WritableByteChannel out) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(128 * 1024);
        while (in.read(buffer) != -1 || buffer.position() > 0) {
            buffer.flip();
            out.write(buffer);
            buffer.compact();
        }
    }
}
