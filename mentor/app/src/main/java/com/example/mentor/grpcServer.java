package com.example.mentor;

import com.example.mentor.proto.RemoteCallGrpc;
import com.example.mentor.proto.GetPictureRequest;
import com.example.mentor.proto.GetPictureResponse;
import com.google.protobuf.ByteString;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class grpcServer {
    private Server server;
    private int port;
    private boolean active;

    public grpcServer(int portv) {
        port = portv;
        active = false;
        server = null;
    }

    public boolean isActive() {
        return active;
    }

    public void start() throws IOException {
        if (server == null) {
            server = ServerBuilder.forPort(port)
                    .addService(new RemoteCallImpl())
                    .build();
        }
        server.start();
        System.out.println("service start...localhost:" + port);
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            @Override
//            public void run() {
//                grpcServer.this.stop();
//                System.err.println("*** server shut down");
//            }
//        });
        active = true;
        new ServerThread(this).start();
    }

//    public void run() {
//        new ServerThread(this).start();
//    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
        active = false;
    }

    public void waitForRequest() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private class RemoteCallImpl extends RemoteCallGrpc.RemoteCallImplBase {
        public void remoteCall(GetPictureRequest req, StreamObserver<GetPictureResponse> responseObserver) {
            System.out.println("service:" + req.getFrom());
            ByteString bs = ByteString.copyFrom(new String("hello world").getBytes());
            GetPictureResponse.Builder bResp = GetPictureResponse.newBuilder();
            bResp.setFrom(req.getFrom());
            bResp.setStatus(0);
            bResp.setName("ff.jpg");
            bResp.setBody(bs);
            responseObserver.onNext(bResp.build());
            responseObserver.onCompleted();
        }
    }


    public class ServerThread extends Thread {
        private grpcServer server;

        public ServerThread(grpcServer serv){
            server = serv;
        }
        @Override
        public void run() {
            if (server != null && server.isActive()) {
                try {
                    server.waitForRequest();
                }catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("error when wait for request : " + e.getMessage());
                }
            }
        }
    }
}
