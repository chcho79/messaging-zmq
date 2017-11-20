#!/bin/bash

export GOPATH=$pwd

go get github.com/pebbe/zmq4
go get -u github.com/golang/protobuf/protoc-gen-go
go get -u go.uber.org/zap

mkdir ./src
mkdir ./src/go
cp -r  emf ./src/go
cd ./src/go/emf

CGO_ENABLED=1 CC=/usr/bin/aarch64-linux-gnu-gcc-4.8 CXX=/usr/bin/aarch64-linux-gnu-g++-4.8 GOOS=linux GOARCH=arm64 go build
CGO_ENABLED=1 CC=/usr/bin/aarch64-linux-gnu-gcc-4.8 CXX=/usr/bin/aarch64-linux-gnu-g++-4.8 GOOS=linux GOARCH=arm64 go install

