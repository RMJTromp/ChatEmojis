package com.rmjtromp.chatemojis.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.google.common.io.CharStreams;

public final class GZIPUtils {

    private GZIPUtils() {}
    
    public static void compressToFile(File file, String content) throws IOException {
        com.google.common.io.Files.createParentDirs(file);
        if(file.exists()) file.createNewFile();
        
    	try(InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            FileOutputStream fos = new FileOutputStream(file);
            GZIPOutputStream os = new GZIPOutputStream(fos)) {
            byte[] buffer = new byte[1024];
            int len;
            while((len=in.read(buffer)) != -1){
                os.write(buffer, 0, len);
            }
        }
    }
    
    public static void compressToFile(File file, InputStream stream) throws IOException {
        compressToFile(file, CharStreams.toString(new InputStreamReader(stream, StandardCharsets.UTF_8)));
    }
    
    public static String decompress(File file) throws IOException {
    	try(ByteArrayInputStream bain = new ByteArrayInputStream(Files.readAllBytes(file.toPath()));
    		GZIPInputStream in = new GZIPInputStream(bain);
        	ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len =in.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
                
            return os.toString("UTF-8");
        }
    }
    
    public static String decompress(InputStream stream) throws IOException {
    	try (InputStream ungzippedResponse = new GZIPInputStream(stream);
    		 Reader reader = new InputStreamReader(ungzippedResponse, StandardCharsets.UTF_8);
    		 Writer writer = new StringWriter()) {
    		 char[] buffer = new char[1024];
    		 for (int length = 0; (length = reader.read(buffer)) > 0;) {
    			 writer.write(buffer, 0, length);
    		 }
    		 return writer.toString();
    	}
    }

    public static byte[] compress(String data) throws IOException {
    	try(ByteArrayOutputStream baostream = new ByteArrayOutputStream();
    		OutputStream outStream = new GZIPOutputStream(baostream);) {
            outStream.write(data.getBytes(StandardCharsets.UTF_8));
            return baostream.toByteArray();
    	}
    }

    public static String decompress(byte[] compressed) throws IOException {
    	try(InputStream inStream = new GZIPInputStream(new ByteArrayInputStream(compressed));
    		ByteArrayOutputStream baoStream2 = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inStream.read(buffer)) > 0) {
                baoStream2.write(buffer, 0, len);
            }
            
            return baoStream2.toString("UTF-8");
    	}
    }

    public static void write(File file, String content) throws IOException {
        byte[] compressedContent = compress(content);
        com.google.common.io.Files.createParentDirs(file);
        if(file.exists()) file.createNewFile();
        
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(compressedContent);
        }
    }

    public static String read(File file) throws IOException {
        return decompress(Files.readAllBytes(file.toPath()));
    }

    public static String read(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return decompress(buffer.toByteArray());
    }

}