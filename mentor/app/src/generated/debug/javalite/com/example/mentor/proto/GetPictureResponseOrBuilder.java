// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: test.proto

package com.example.mentor.proto;

public interface GetPictureResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:GrpcService.GetPictureResponse)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>optional uint32 from = 1;</code>
   */
  int getFrom();

  /**
   * <code>optional uint32 status = 2;</code>
   */
  int getStatus();

  /**
   * <code>optional string name = 3;</code>
   */
  java.lang.String getName();
  /**
   * <code>optional string name = 3;</code>
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>optional bytes body = 4;</code>
   */
  com.google.protobuf.ByteString getBody();
}
