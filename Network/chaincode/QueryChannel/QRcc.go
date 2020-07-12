package main

import (
	"bytes"
	"crypto/sha256"
	"encoding/binary"
	"encoding/json"
	"fmt"
	"math/big"
	"strconv"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/hyperledger/fabric/protos/peer"
)

type QueryCC struct{}

type SoldCommodity struct {
	// 零售商的编号
	Retailer int `json:"retailer"`
	// 售出的时间戳
	TransactionTime int64 `json:"transaction_time"`
	// 该被售出商品所对应的货物批次
	ComNum string `json:"com_num"`
	// 是否被查询过
	QueryTimes int `json:"query_times"`
	// 上次查询的时间
	LastQuery int64 `json:"last_query"`
}

func (q QueryCC) Init(stub shim.ChaincodeStubInterface) peer.Response {

	return shim.Success([]byte("Fully Success!"))
}

func (q QueryCC) Invoke(stub shim.ChaincodeStubInterface) peer.Response {

	if method, _ := stub.GetFunctionAndParameters(); method == "query" {
		return query(stub)
	} else if method == "sell" {
		return sell(stub)
	}
	return fail("unknown method, should be \"query\" or \"sell\" ")
}

func query(stub shim.ChaincodeStubInterface) peer.Response {

	_, arg := stub.GetFunctionAndParameters()
	if len(arg) != 1 {
		return fail("The argument should be the Serial Number")
	}

	state, err := stub.GetState(arg[0])
	if err != nil {
		return fail("Can't query the result：" + err.Error())
	}
	if state == nil {
		return fail("The Serial Number dose not exist")
	}

	soldCommodity := new(SoldCommodity)
	if err := json.Unmarshal(state, soldCommodity); err != nil {
		return fail("The data of this batch of Cargo is wrong")
	}

	now, err := stub.GetTxTimestamp()
	if err != nil {
		return fail("Can't get time :" + err.Error())
	}

	newSoldCommodity := &SoldCommodity{
		Retailer:        soldCommodity.Retailer,
		TransactionTime: soldCommodity.TransactionTime,
		ComNum:          soldCommodity.ComNum,
		QueryTimes:      soldCommodity.QueryTimes + 1,
		LastQuery:       now.Seconds,
	}

	newData, err := json.Marshal(newSoldCommodity)
	err = stub.PutState(arg[0], newData)
	if err != nil {
		return fail("Can't save to ledger" + err.Error())
	}

	return shim.Success(state)
}

func sell(stub shim.ChaincodeStubInterface) peer.Response {

	_, args := stub.GetFunctionAndParameters()
	if len(args) != 2 {
		return fail("Wrong number of arguments, expect 2")
	}

	// 给SoldCommodity赋值
	var soldCom SoldCommodity
	retailer, ok := parse(args[0])
	if !ok {
		return fail("The first argument should be a number")
	}
	if len(args[1]) != 43 {
		return fail("The serial number of commodity is illegal")
	}
	soldCom.Retailer = retailer
	soldCom.ComNum = args[1]
	soldCom.QueryTimes = 0
	soldCom.LastQuery = 0
	timeStamp, err := stub.GetTxTimestamp()
	if err != nil {
		return fail("Can't get timestamp: " + err.Error())
	}
	soldCom.TransactionTime = timeStamp.Seconds

	// 所有信息整合
	var bufbyte []byte
	var allbyte []byte
	buffer := bytes.NewBuffer(bufbyte)
	err1 := binary.Write(buffer, binary.BigEndian, int64(soldCom.Retailer))
	err2 := binary.Write(buffer, binary.BigEndian, soldCom.TransactionTime)
	if err1 != nil {
		return fail("Can't transfer retailer into []byte" + err1.Error())
	}
	if err2 != nil {
		return fail("Can't transfer timestamp into []byte" + err2.Error())
	}
	allbyte = append(allbyte, buffer.Bytes()...)
	allbyte = append(allbyte, []byte(soldCom.ComNum)...)

	// 生成该商品的防伪序列号，也就是查询所用的序列号
	shaSerial := sha256.Sum256(allbyte)
	SerialNum := string(Base58Encode(shaSerial[:]))

	data, err := json.Marshal(soldCom)
	if err != nil {
		return fail("Can't Serialize into json：" + err.Error())
	}

	// 保存至账本
	err = stub.PutState(SerialNum, data)
	if err != nil {
		return fail("Can't save the data into ledger： " + err.Error())
	}

	return shim.Success([]byte(SerialNum))

}

func fail(string string) peer.Response {

	return shim.Error(string)
}

func parse(arg string) (int, bool) {

	i, err := strconv.Atoi(arg)
	if err != nil {
		return -1, false
	}
	return i, true
}

func Base58Encode(input []byte) []byte {
	var b58Alphabet = []byte("123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz")
	var result []byte

	x := big.NewInt(0).SetBytes(input)

	base := big.NewInt(int64(len(b58Alphabet)))
	zero := big.NewInt(0)
	mod := &big.Int{}

	for x.Cmp(zero) != 0 {
		x.DivMod(x, base, mod)
		result = append(result, b58Alphabet[mod.Int64()])
	}

	// ReverseBytes(result)

	for _, b := range input {
		if b == 0x00 {
			result = append([]byte{b58Alphabet[0]}, result...)

		} else {
			break
		}
	}

	return result

}

func main() {
	err := shim.Start(new(QueryCC))
	if err != nil {
		fmt.Println("Can't Start Query Channel Chaincode")
	}
}
