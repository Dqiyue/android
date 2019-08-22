package com.example.mentor;

//import com.example.mentor.proto.GrpcService;
import com.example.mentor.proto.RemoteCallGrpc;
import com.example.mentor.proto.GetPictureRequest;
import com.example.mentor.proto.GetPictureResponse;
import com.example.mentor.proto.PostPictureRequest;
import com.example.mentor.proto.PostPictureResponse;
import com.google.protobuf.ByteString;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.lang.Byte;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class ErrRet {
    public Throwable err;
    public int ret;
    public ErrRet() {err = null;ret = 0;}
    public void catchErr(Throwable t) { err = t; }
    public void setRet(int retCode) {ret = retCode;}
}


public class ClientChannel {
    private final ManagedChannel channel;
    private final RemoteCallGrpc.RemoteCallBlockingStub blockingStub;
    private final RemoteCallGrpc.RemoteCallStub asyncStub;
    private static  int GRPC_MAX_DATA_LEN = 64*1024;

    public ClientChannel(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host,port).usePlaintext(true).build();
        if (channel == null) {
            System.out.println("create channel error!");
            blockingStub = null;
            asyncStub = null;
        }else {
            blockingStub = RemoteCallGrpc.newBlockingStub(channel);
            asyncStub = RemoteCallGrpc.newStub(channel);
        }
    }

    public byte[] RemoteGetPicture(int from) {
        GetPictureResponse resp = null;
        try {
            if (blockingStub == null) {
                return null;
            }
            GetPictureRequest req = GetPictureRequest.newBuilder().setFrom(from).build();
            if (req == null) {
                return null;
            }
            resp = blockingStub.getPicture(req);
            //channel.shutdown();
            if (resp == null || resp.getStatus() != 0 || resp.getBody().isEmpty() == true) {
                return null;
            }
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("grpc service error : " + e.getMessage());
            return null;
        }
        return resp.getBody().toByteArray();
    }


    public int RemotePostPicture(int from, String picName, byte[] picData, double longitude, double latitude)
            throws InterruptedException, RuntimeException {
        //PostPictureResponse resp = null;

//        if (asyncStub == null) {
//            System.out.println("grpc service error : asyncStub == null");
//            return 2;
//        }

        final ErrRet ec = new ErrRet();

        final CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<PostPictureResponse> resps = new StreamObserver<PostPictureResponse>(){
            @Override
            public void onNext(PostPictureResponse res) {
                ec.setRet(res.getStatus());
            }

            @Override
            public void onError(Throwable t) {
                ec.catchErr(t);
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }

        };
        StreamObserver<PostPictureRequest> reqs = asyncStub.postPicture(resps);
        try {
            int len = 0;
            int copylen = 0;
            while (len < picData.length) {
                PostPictureRequest.Builder rb = PostPictureRequest.newBuilder();
                rb.setFrom(from);
                rb.setLongitude(longitude);
                rb.setLatitude(latitude);
                //rb.setName(picName);
                copylen = picData.length - len;
                copylen = copylen > GRPC_MAX_DATA_LEN ? GRPC_MAX_DATA_LEN : copylen;
                rb.setBody(ByteString.copyFrom(picData,len,copylen));
                PostPictureRequest req = rb.build();
                reqs.onNext(req);
                len += copylen;
            }
        }catch (RuntimeException e) {
            reqs.onError(e);
            throw e;
        }

        reqs.onCompleted();
        //channel.shutdown();
        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            throw new RuntimeException(
                    "Could not finish rpc within 1 minute, the server is likely down");
        }

        if (ec.err != null) {
            throw new RuntimeException(ec.err);
        }

        return ec.ret;
    }

    public String GetPicture(int from,String toPath) {
        GetPictureResponse resp = null;
        try {
            if (blockingStub == null) {
                return "";
            }
            GetPictureRequest req = GetPictureRequest.newBuilder().setFrom(from).build();
            if (req == null) {
                return "";
            }
            resp = blockingStub.getPicture(req);
            if (resp == null || resp.getStatus() != 0 || resp.getBody().isEmpty() == true) {
                return "";
            }
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("grpc service error : " + e.getMessage());
            return "";
        }

        //File temp = null;
        try {
            toPath += (File.separatorChar + "pic.jpg");
            //long ts = System.currentTimeMillis();
            //String pf = "" + ts;
            File temp = new File(toPath);
            if (temp == null) {
                System.out.println("new file " + toPath + " error!");
                return "";
            }
            //temp = File.createTempFile("pio", ".jpg");
            temp.createNewFile();
            //FileWriter w = new FileWriter(temp);
            FileOutputStream fs = new FileOutputStream(temp);
            fs.write(resp.getBody().toByteArray());
            fs.flush();

//            Charset cs = null;
//            CharsetDecoder decoder = null;
//            CharBuffer charBuffer = null;
//            ByteBuffer buffer = ByteBuffer.allocate(resp.getBody().toByteArray().length);
//            buffer.put(resp.getBody().toByteArray());
//            buffer.flip();
//            try {
//                cs = Charset.forName("ISO-8859-1");
//                decoder = cs.newDecoder();
//                charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                System.out.println("decode body error : " + ex.getMessage());
//                return "";
//            }
//            //ByteBuffer bbf = new ByteBuffer(resp.toByteArray());
//
//            //ByteToCharConverter converter = ByteToCharConverter.getConverter(encoding);
//            w.write(charBuffer.array());
//            //w.write(resp.getBody().toByteArray());
//            w.flush();
//            w.close();
            return temp.getAbsolutePath().toString();
            //return temp.getCanonicalPath();
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("create temp file error : " + e.getMessage());
            return "";
        }
    }
}

