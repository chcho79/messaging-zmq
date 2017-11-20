#!/bin/bash

export GOPATH=$PWD
go get github.com/pebbe/zmq4
go get -u github.com/golang/protobuf/protoc-gen-go
go get -u go.uber.org/zap

mkdir ./src
mkdir ./src/go
cp -r  emf ./src/go
cd ./src/go/emf
go build
go install
