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

/**
上游供应链企业的通道链码。上游企业主要由生皮供应商和皮革供应商组成。
存储的数据也就是生皮交易的所有信息了
*/
type UpStreamCC struct {}

// 代表生皮的属性结构体
type Hide struct {
	/*
	动物类型，为了方便就不用枚举了，直接用
	0 - Cow 牛
	1 - Goat 羊
	2 - Pig 猪
	3 - Deer 鹿
	4 - Fish 鱼
	5 - Snake 蛇
	6 - Ostrich 鸵鸟
	7 - Crocodile 鳄鱼
	*/
	AnimalType int `json:"animal_type"`
	/*
	生皮的防腐措施，同样为了方便不用枚举
	0 - 鲜皮
	1 - 冻鲜皮
	2 - 干皮
	3 - 冻干皮
	4 - 干腌皮
	5 - 湿腌皮
	6 - 浸酸皮
	7 - 照射皮
	*/
	ReserveType int `json:"reserve_type"`
	// 生皮制造者，用编号表示
	Producer int `json:"producer"`
	// 这个时间戳指的是该份数据上链的时间，大体可以看做是生皮交易的时间
	TransactionTime int64 `json:"transaction_time"`
}

func (u UpStreamCC) Init(stub shim.ChaincodeStubInterface) peer.Response {
	//初始化完全不用作什么
	return shim.Success([]byte("Fully Success!"))
}

func (u UpStreamCC) Invoke(stub shim.ChaincodeStubInterface) peer.Response {

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

	hide := new(Hide)
	if err := json.Unmarshal(state, hide); err != nil {
		return fail("The data of this batch of Cargo is wrong")
	}

	return shim.Success(state)
}

func invoke(stub shim.ChaincodeStubInterface) peer.Response {

	_, args := stub.GetFunctionAndParameters()
	if len(args) != 3 {
		return fail("Wrong number of argument, expect 4")
	}

	// 把输入的参数设置到Hide结构体中
	var hide Hide
	for i, arg := range args {
		res, ok := parse(arg)
		if !ok {
			return fail(fmt.Sprintf("The No.%d argument should be a number", i))
		}
		val := reflect.ValueOf(&hide).Elem()
		val.Field(i).SetInt(int64(res))
	}

	// 设置时间戳字段
	timeStamp, err := stub.GetTxTimestamp()
	if err != nil {
		return fail("Can't get timestamp：" + err.Error())
	}
	hide.TransactionTime = timeStamp.Seconds

	// 把所有信息组合
	var allbyte []byte
	for i := 0; i < 4; i++ {
		var buff []byte
		buffer := bytes.NewBuffer(buff)
		err := binary.Write(buffer, binary.BigEndian, reflect.ValueOf(hide).Field(i).Int())
		if err != nil {
			return fail("Can't transfer int into []byte" + err.Error())
		}
		allbyte = append(allbyte, buffer.Bytes()...)
	}

	// 生成该批次生皮的序列号
	shaSerial := sha256.Sum256(allbyte)
	s := base64.StdEncoding.EncodeToString(shaSerial[:])
	SerilNum := s[:len(s)-1]

	data, err := json.Marshal(hide)
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

	err := shim.Start(new(UpStreamCC))
	if err != nil {
		fmt.Print("Can't Start Upstream Channel Chaincode" + err.Error())
	}
}
