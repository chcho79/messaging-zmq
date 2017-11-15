#!/bin/bash

export GOPATH=$pwd

go get github.com/pebbe/zmq4
go get -u github.com/golang/protobuf/protoc-gen-go
go get -u go.uber.org/zap
cd emf
go build
