package main

import (
	"bytes"
	"crypto/sha256"
	"encoding/base64"
	"encoding/binary"
	"encoding/json"
	"fmt"
	"reflect"
	"strconv"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/hyperledger/fabric/protos/peer"
)

type DownStreamCC struct{}

type Leather struct {
	/*
		皮革的糅制方式
		0 - Chrome
		1 - Vegitable
		2 - Aluminum
		3 - Oil
	*/
	Tanning int `json:"tanning"`
	/*
		皮革使用的是那一层皮料，基本可以代表皮革的质量，同样为了方便就不用枚举了
		0 - FullGrain Layer
		1 - EmbossedGrain
		2 - SplitSuede
		3 - NubuckSuededGrain
		4 - FibreLeather
	*/
	Layer int `json:"layer"`
	// 皮革的生产商，用编号来表示
	Producer int `json:"producer"`
	// 时间戳，代表皮革上链的时间，也就算是交易的时间
	TransactionTime int64 `json:"transaction_time"`
	// 代表了这批皮革是由哪一批生皮生产的
	HideNum string `json:"hide_num"`
}

func (d DownStreamCC) Init(stub shim.ChaincodeStubInterface) peer.Response {

	return shim.Success([]byte("Fully Success!"))
}

func (d DownStreamCC) Invoke(stub shim.ChaincodeStubInterface) peer.Response {

	if method, _ := stub.GetFunctionAndParameters(); method == "query" {
		return query(stub)
	} else if method == "invoke" {
		return invoke(stub)
	}
	return shim.Error("unknown method, should be \"query\" or \"invoke\" ")
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

	leather := new(Leather)
	if err := json.Unmarshal(state, leather); err != nil {
		return fail("The data of this batch of Cargo is wrong")
	}

	return shim.Success(state)
}

func invoke(stub shim.ChaincodeStubInterface) peer.Response {

	_, args := stub.GetFunctionAndParameters()
	if len(args) <= 3 {
		return fail("Wrong number of argument, expect 4")
	}

	// 把输入的参数设置到Leather结构体中
	var leather Leather
	for i := 0; i < 3; i++ {
		arg := args[i]
		res, ok := parse(arg)
		if !ok {
			return fail(fmt.Sprintf("The No.%d argument should be a number", i+1))
		}
		val := reflect.ValueOf(&leather).Elem()
		val.Field(i).SetInt(int64(res))
	}

	// 生皮的序列号应该合法
	if len(args[3]) != 43 {
		return fail("The serial number of the Hide is illegal")
	}
	leather.HideNum = args[3]

	// 设置时间戳字段
	timeStamp, err := stub.GetTxTimestamp()
	if err != nil {
		return fail("Can't get timestamp：" + err.Error())
	}
	leather.TransactionTime = timeStamp.Seconds

	// 把所有信息组合
	var allbyte []byte
	for i := 0; i < 4; i++ {
		var buff []byte
		buffer := bytes.NewBuffer(buff)
		err := binary.Write(buffer, binary.BigEndian, reflect.ValueOf(leather).Field(i).Int())
		if err != nil {
			return fail("Can't transfer int into []byte" + err.Error())
		}
		allbyte = append(allbyte, buffer.Bytes()...)
	}
	allbyte = append(allbyte, []byte(leather.HideNum)...)

	// 生成该批次皮革的序列号
	shaSerial := sha256.Sum256(allbyte)
	s := base64.StdEncoding.EncodeToString(shaSerial[:])
	SerilNum := s[:len(s)-1]

	data, err := json.Marshal(leather)
	if err != nil {
		return fail("Can't Serialize into json：" + err.Error())
	}

	// 保存至账本
	err = stub.PutState(SerilNum, data)
	if err != nil {
		return fail("Can't save the data into ledger： " + err.Error())
	}

	return shim.Success([]byte(SerilNum))
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

func main() {

	err := shim.Start(new(DownStreamCC))
	if err != nil {
		fmt.Println("Can't Start Downstream Channel Chaincode " + err.Error())
	}
}