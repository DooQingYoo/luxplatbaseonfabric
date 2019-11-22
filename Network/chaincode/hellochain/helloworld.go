package main

import (
	"fmt"
	"strings"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/hyperledger/fabric/protos/peer"
)

type SimpleChaincode struct {
}

func (s SimpleChaincode) Init(stub shim.ChaincodeStubInterface) peer.Response {
	return shim.Success(nil)
}

func (s SimpleChaincode) Invoke(stub shim.ChaincodeStubInterface) peer.Response {

	fun, args := stub.GetFunctionAndParameters()
	if fun == "hello" {
		return hello(args)
	} else if fun == "bye" {
		return bye(args)
	} else {
		return shim.Error("The function should be 'hello' or 'bye'")
	}
}

func hello(args []string) peer.Response {
	var resp []string
	resp = append(resp, "hello")
	resp = append(resp, args...)
	str := strings.Join(resp, " ")
	return shim.Success([]byte(str))
}

func bye(args []string) peer.Response {
	var resp []string
	resp = append(resp, "goodbye")
	resp = append(resp, args...)
	str := strings.Join(resp, " ")
	return shim.Success([]byte(str))
	//so what
}

func main() {
	err := shim.Start(new(SimpleChaincode))
	if err != nil {
		fmt.Println("Can't start SimpleChaincode" + err.Error())
	}
}
