package com.example.mentor.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * <pre>
 * The greeting service definition.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.20.0)",
    comments = "Source: test.proto")
public final class RemoteCallGrpc {

  private RemoteCallGrpc() {}

  public static final String SERVICE_NAME = "GrpcService.RemoteCall";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.example.mentor.proto.GetPictureRequest,
      com.example.mentor.proto.GetPictureResponse> getGetPictureMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetPicture",
      requestType = com.example.mentor.proto.GetPictureRequest.class,
      responseType = com.example.mentor.proto.GetPictureResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.example.mentor.proto.GetPictureRequest,
      com.example.mentor.proto.GetPictureResponse> getGetPictureMethod() {
    io.grpc.MethodDescriptor<com.example.mentor.proto.GetPictureRequest, com.example.mentor.proto.GetPictureResponse> getGetPictureMethod;
    if ((getGetPictureMethod = RemoteCallGrpc.getGetPictureMethod) == null) {
      synchronized (RemoteCallGrpc.class) {
        if ((getGetPictureMethod = RemoteCallGrpc.getGetPictureMethod) == null) {
          RemoteCallGrpc.getGetPictureMethod = getGetPictureMethod = 
              io.grpc.MethodDescriptor.<com.example.mentor.proto.GetPictureRequest, com.example.mentor.proto.GetPictureResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "GrpcService.RemoteCall", "GetPicture"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  com.example.mentor.proto.GetPictureRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  com.example.mentor.proto.GetPictureResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetPictureMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.example.mentor.proto.PostPictureRequest,
      com.example.mentor.proto.PostPictureResponse> getPostPictureMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PostPicture",
      requestType = com.example.mentor.proto.PostPictureRequest.class,
      responseType = com.example.mentor.proto.PostPictureResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<com.example.mentor.proto.PostPictureRequest,
      com.example.mentor.proto.PostPictureResponse> getPostPictureMethod() {
    io.grpc.MethodDescriptor<com.example.mentor.proto.PostPictureRequest, com.example.mentor.proto.PostPictureResponse> getPostPictureMethod;
    if ((getPostPictureMethod = RemoteCallGrpc.getPostPictureMethod) == null) {
      synchronized (RemoteCallGrpc.class) {
        if ((getPostPictureMethod = RemoteCallGrpc.getPostPictureMethod) == null) {
          RemoteCallGrpc.getPostPictureMethod = getPostPictureMethod = 
              io.grpc.MethodDescriptor.<com.example.mentor.proto.PostPictureRequest, com.example.mentor.proto.PostPictureResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "GrpcService.RemoteCall", "PostPicture"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  com.example.mentor.proto.PostPictureRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  com.example.mentor.proto.PostPictureResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getPostPictureMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static RemoteCallStub newStub(io.grpc.Channel channel) {
    return new RemoteCallStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static RemoteCallBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new RemoteCallBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static RemoteCallFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new RemoteCallFutureStub(channel);
  }

  /**
   * <pre>
   * The greeting service definition.
   * </pre>
   */
  public static abstract class RemoteCallImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Sends
     * </pre>
     */
    public void getPicture(com.example.mentor.proto.GetPictureRequest request,
        io.grpc.stub.StreamObserver<com.example.mentor.proto.GetPictureResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetPictureMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<com.example.mentor.proto.PostPictureRequest> postPicture(
        io.grpc.stub.StreamObserver<com.example.mentor.proto.PostPictureResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getPostPictureMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetPictureMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.example.mentor.proto.GetPictureRequest,
                com.example.mentor.proto.GetPictureResponse>(
                  this, METHODID_GET_PICTURE)))
          .addMethod(
            getPostPictureMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                com.example.mentor.proto.PostPictureRequest,
                com.example.mentor.proto.PostPictureResponse>(
                  this, METHODID_POST_PICTURE)))
          .build();
    }
  }

  /**
   * <pre>
   * The greeting service definition.
   * </pre>
   */
  public static final class RemoteCallStub extends io.grpc.stub.AbstractStub<RemoteCallStub> {
    private RemoteCallStub(io.grpc.Channel channel) {
      super(channel);
    }

    private RemoteCallStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RemoteCallStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new RemoteCallStub(channel, callOptions);
    }

    /**
     * <pre>
     * Sends
     * </pre>
     */
    public void getPicture(com.example.mentor.proto.GetPictureRequest request,
        io.grpc.stub.StreamObserver<com.example.mentor.proto.GetPictureResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetPictureMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<com.example.mentor.proto.PostPictureRequest> postPicture(
        io.grpc.stub.StreamObserver<com.example.mentor.proto.PostPictureResponse> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getPostPictureMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * <pre>
   * The greeting service definition.
   * </pre>
   */
  public static final class RemoteCallBlockingStub extends io.grpc.stub.AbstractStub<RemoteCallBlockingStub> {
    private RemoteCallBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private RemoteCallBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RemoteCallBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new RemoteCallBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Sends
     * </pre>
     */
    public com.example.mentor.proto.GetPictureResponse getPicture(com.example.mentor.proto.GetPictureRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetPictureMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * The greeting service definition.
   * </pre>
   */
  public static final class RemoteCallFutureStub extends io.grpc.stub.AbstractStub<RemoteCallFutureStub> {
    private RemoteCallFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private RemoteCallFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RemoteCallFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new RemoteCallFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Sends
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.example.mentor.proto.GetPictureResponse> getPicture(
        com.example.mentor.proto.GetPictureRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetPictureMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_PICTURE = 0;
  private static final int METHODID_POST_PICTURE = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final RemoteCallImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(RemoteCallImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_PICTURE:
          serviceImpl.getPicture((com.example.mentor.proto.GetPictureRequest) request,
              (io.grpc.stub.StreamObserver<com.example.mentor.proto.GetPictureResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_POST_PICTURE:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.postPicture(
              (io.grpc.stub.StreamObserver<com.example.mentor.proto.PostPictureResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (RemoteCallGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .addMethod(getGetPictureMethod())
              .addMethod(getPostPictureMethod())
              .build();
        }
      }
    }
    return result;
  }
}
