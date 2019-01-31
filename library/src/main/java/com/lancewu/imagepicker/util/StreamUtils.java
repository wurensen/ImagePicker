package com.lancewu.imagepicker.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * IO相关工具类
 */
public class StreamUtils {

    public static final int IO_BUFFER_SIZE = 8 * 1024;

    private static final int END_OF_STREAM = -1;

    /**
     * 关闭指定的流
     *
     * @param stream 要关闭的流
     */
    public static final void close(final Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 比较两个输入流是否相等
     *
     * @param input1 输入流1
     * @param input2 输入流2
     * @return true相等，false不相等
     * @throws IOException
     */
    public static boolean contentEquals(InputStream input1, InputStream input2)
            throws IOException {
        if (!(input1 instanceof BufferedInputStream)) {
            input1 = new BufferedInputStream(input1);
        }
        if (!(input2 instanceof BufferedInputStream)) {
            input2 = new BufferedInputStream(input2);
        }

        int ch = input1.read();
        while (-1 != ch) {
            int ch2 = input2.read();
            if (ch != ch2) {
                return false;
            }
            ch = input1.read();
        }

        int ch2 = input2.read();
        return (ch2 == -1);
    }

    /**
     * 输入流读入字节数据到字节数组中
     *
     * @param pInputStream 输入流
     * @param pData        存放数据的字节数组
     * @throws IOException
     */
    public static final void copy(final InputStream pInputStream, final byte[] pData)
            throws IOException {
        int dataOffset = 0;
        final byte[] buf = new byte[StreamUtils.IO_BUFFER_SIZE];
        int read;
        while ((read = pInputStream.read(buf)) != StreamUtils.END_OF_STREAM) {
            System.arraycopy(buf, 0, pData, dataOffset, read);
            dataOffset += read;
        }
    }

    /**
     * 复制InputStream到ByteBuffer中
     *
     * @param inputStream 输入流
     * @param byteBuffer  字节缓存
     * @throws IOException
     */
    public static final void copy(final InputStream inputStream, final ByteBuffer byteBuffer)
            throws IOException {
        final byte[] buf = new byte[StreamUtils.IO_BUFFER_SIZE];
        int read;
        while ((read = inputStream.read(buf)) != StreamUtils.END_OF_STREAM) {
            byteBuffer.put(buf, 0, read);
        }
    }

    /**
     * 从输入流赋值数据到输出流
     *
     * @param inputStream  输入流
     * @param outputStream 输出流
     * @throws IOException
     * @see #copy(InputStream, OutputStream, int)
     */
    public static final void copy(final InputStream inputStream, final OutputStream outputStream)
            throws IOException {
        StreamUtils.copy(inputStream, outputStream, StreamUtils.END_OF_STREAM);
    }

    /**
     * 把输入流的内容复制到输出流中，使用一个大小为{@link #IO_BUFFER_SIZE}的字节数组作为缓冲区
     *
     * @param inputStream  输入流
     * @param outputStream 输出流.
     * @param byteLimit    要读入的最大字节数量，若设为{@link StreamUtils#END_OF_STREAM}则不做限制.
     * @throws IOException
     */
    public static final void copy(final InputStream inputStream, final OutputStream outputStream, final int byteLimit)
            throws IOException {
        if (byteLimit == StreamUtils.END_OF_STREAM) {
            final byte[] buf = new byte[StreamUtils.IO_BUFFER_SIZE];
            int read;
            while ((read = inputStream.read(buf)) != StreamUtils.END_OF_STREAM) {
                outputStream.write(buf, 0, read);
            }
        } else {
            final byte[] buf = new byte[StreamUtils.IO_BUFFER_SIZE];
            final int bufferReadLimit = Math.min((int) byteLimit, StreamUtils.IO_BUFFER_SIZE);
            long pBytesLeftToRead = byteLimit;

            int read;
            while ((read = inputStream.read(buf, 0, bufferReadLimit)) != StreamUtils.END_OF_STREAM) {
                if (pBytesLeftToRead > read) {
                    outputStream.write(buf, 0, read);
                    pBytesLeftToRead -= read;
                } else {
                    outputStream.write(buf, 0, (int) pBytesLeftToRead);
                    break;
                }
            }
        }
        outputStream.flush();
    }

    /**
     * 复制InputStream到OutputStream，并关闭stream
     *
     * @param inputStream  输入流
     * @param outputStream 输出流
     * @return true操作成功，false操作失败
     */
    public static final boolean copyAndClose(final InputStream inputStream, final OutputStream outputStream) {
        try {
            StreamUtils.copy(inputStream, outputStream, StreamUtils.END_OF_STREAM);
            return true;
        } catch (final IOException e) {
            return false;
        } finally {
            StreamUtils.close(inputStream);
            StreamUtils.close(outputStream);
        }
    }

    /**
     * flush并关闭输出流
     *
     * @param outputStream 要关闭的输出流
     */
    public static final void flushCloseStream(final OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.flush();
            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                StreamUtils.close(outputStream);
            }
        }
    }

    /**
     * flush并关闭writer
     *
     * @param pWriter 要关闭的writer
     */
    public static final void flushCloseWriter(final Writer pWriter) {
        if (pWriter != null) {
            try {
                pWriter.flush();
            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                StreamUtils.close(pWriter);
            }
        }
    }

    /**
     * 获取用于读取asset目录下文件的输入流
     *
     * @param pAssetManager AssetManager实例
     * @param pAssetPath    文件路径
     * @return 用于读取asset目录下文件的输入流
     * @throws IOException
     */
    public static InputStream getInputStreamFromAsset(final AssetManager pAssetManager, final String pAssetPath)
            throws IOException {
        return pAssetManager.open(pAssetPath);
    }

    /**
     * 获取用于读取asset目录下文件的输入流
     *
     * @param context       上下文
     * @param assetFilePath 文件路径
     * @return 用于读取asset目录下文件的输入流
     * @throws IOException
     */
    public static InputStream getInputStreamFromAsset(Context context, final String assetFilePath)
            throws IOException {
        return context.getAssets().open(assetFilePath);
    }

    /**
     * bytes转InputStream
     *
     * @param bytes  源byte数据
     * @param offset 数据起始点
     * @param length 数据总长度
     * @return 转换后的InputStream
     */
    public static InputStream getInputStreamFromByteArray(final byte[] bytes, final int offset, final int length) {
        return new ByteArrayInputStream(bytes, offset, length);
    }

    /**
     * file文件转InputStream
     *
     * @param filePath 文件路径
     * @return 转换后的InputStream
     * @throws IOException
     */
    public static InputStream getInputStreamFromFile(final String filePath)
            throws IOException {
        return new FileInputStream(filePath);
    }

    /**
     * resource文件转InputStream
     *
     * @param resourceID 资源ID
     * @return 用于读取资源的InputStream
     */
    public static InputStream getInputStreamFromResource(Context context, final int resourceID) {
        return context.getResources().openRawResource(resourceID);
    }

    /**
     * Resources转 InputStream ，这个主要作用于声音等文件
     *
     * @param pResources  Resources实例
     * @param pResourceID 资源ID
     * @return 用于读取资源的InputStream
     */
    public static InputStream getInputStreamFromResource(final Resources pResources, final int pResourceID) {
        return pResources.openRawResource(pResourceID);
    }

    /**
     * file文件转OutputStream
     *
     * @param filePath 文件路径
     * @return 用于写入文件的OutputStream
     * @throws IOException
     */
    public static OutputStream getOutputStreamFromFile(final String filePath)
            throws IOException {
        return new FileOutputStream(filePath);
    }

    /**
     * 将InputStream转String
     *
     * @param is 输入流
     * @return 转换后的String
     */
    public static String inputStream2String(InputStream is)
            throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = null;
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }

    /**
     * InputStream转String
     *
     * @param inputStream 输入流
     * @return 转换后的String
     * @throws IOException
     */
    public static final String readFully(final InputStream inputStream)
            throws IOException {
        final StringWriter writer = new StringWriter();
        final char[] buf = new char[StreamUtils.IO_BUFFER_SIZE];
        try {
            final Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            int read;
            while ((read = reader.read(buf)) != StreamUtils.END_OF_STREAM) {
                writer.write(buf, 0, read);
            }
        } finally {
            StreamUtils.close(inputStream);
        }
        return writer.toString();
    }

    /**
     * 从InputStream中按行读取字符串
     *
     * @param input 输入流
     * @return 从InputStream中读取的字符串列表
     * @throws IOException
     */
    public static List<String> readLines(InputStream input)
            throws IOException {
        InputStreamReader reader = new InputStreamReader(input);
        return readLines(reader);
    }

    /**
     * 从Reader中按行读取字符串
     *
     * @param input Reader实例
     * @return 从Reader中读取的字符串列表
     * @throws IOException
     */
    public static List<String> readLines(Reader input)
            throws IOException {
        BufferedReader reader = new BufferedReader(input);
        List<String> list = new ArrayList<String>();
        String line = reader.readLine();
        while (line != null) {
            list.add(line);
            line = reader.readLine();
        }
        return list;
    }

    /**
     * InputStream转byte数组
     *
     * @param inputStream 输入流
     * @return 从InputStream中读取的byte数组
     * @throws IOException
     * @see #streamToBytes(InputStream, int)
     */
    public static final byte[] streamToBytes(final InputStream inputStream)
            throws IOException {
        return StreamUtils.streamToBytes(inputStream, StreamUtils.END_OF_STREAM);
    }

    /**
     * InputStream转byte数组
     *
     * @param inputStream 输入流
     * @param readLimit   读取字节限制
     * @return 从InputStream中读取的byte数组
     * @throws IOException
     */
    public static final byte[] streamToBytes(final InputStream inputStream, final int readLimit)
            throws IOException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream(
                (readLimit == StreamUtils.END_OF_STREAM) ? StreamUtils.IO_BUFFER_SIZE : readLimit);
        StreamUtils.copy(inputStream, os, readLimit);
        return os.toByteArray();
    }

    /**
     * 从InputStream都取数据到byte数组
     *
     * @param inputStream 输入流
     * @param byteLimit   写入的字节数
     * @param data        存放读入数据的byte数组
     * @throws IOException
     * @see StreamUtils#streamToBytes(InputStream, int, byte[], int)
     */
    public static final void streamToBytes(final InputStream inputStream, final int byteLimit, final byte[] data)
            throws IOException {
        StreamUtils.streamToBytes(inputStream, byteLimit, data, 0);
    }

    /**
     * 从InputStream都取数据到byte数组
     *
     * @param inputStream 输入流
     * @param byteLimit   写入的字节数
     * @param data        存放读入数据的byte数组
     * @param pOffset     写入byte数组的起始位置
     * @throws IOException
     */
    public static final void streamToBytes(final InputStream inputStream, final int byteLimit, final byte[] data,
                                           final int pOffset)
            throws IOException {
        if (byteLimit > data.length - pOffset) {
            throw new IOException("data is not big enough.");
        }

        int pBytesLeftToRead = byteLimit;
        int readTotal = 0;
        int read;
        while ((read = inputStream.read(data, pOffset + readTotal, pBytesLeftToRead)) != StreamUtils.END_OF_STREAM) {
            readTotal += read;
            if (pBytesLeftToRead > read) {
                pBytesLeftToRead -= read;
            } else {
                break;
            }
        }

        if (readTotal != byteLimit) {
            throw new IOException("ReadLimit: '" + byteLimit + "', Read: '" + readTotal + "'.");
        }
    }

    /**
     * 文件转字节数组
     *
     * @param filePath 文件路径
     * @return 字节数组
     * @throws IOException
     */
    public static final byte[] streamToBytes(final String filePath)
            throws IOException {
        return StreamUtils.streamToBytes(new FileInputStream(filePath), StreamUtils.END_OF_STREAM);
    }

    /**
     * InputStream转字符数组
     *
     * @param is 输入流
     * @return 字符数组
     * @throws IOException
     */
    public static char[] toCharArray(InputStream is)
            throws IOException {
        return readFully(is).toCharArray();
    }

    /**
     * 把字节数组写入到输出流
     *
     * @param data   字节数组
     * @param output 输出流
     * @throws IOException
     */
    public static void writeByte2OutputStream(byte[] data, OutputStream output)
            throws IOException {
        if (data != null) {
            output.write(data);
        }
    }

}
