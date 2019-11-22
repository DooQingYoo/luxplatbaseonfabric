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

type MarketCC struct {
}

type Commodity struct {
	// 产品代工厂的编号
	Factory int `json:"factory"`
	// 该批次商品的数量，每卖掉一个就会减去1件
	TotalCount int `json:"total_count"`
	// 时间戳，工厂生产完毕交付给代理商的时间
	TransactionTime int64 `json:"transaction_time"`
	// 还未售出的商品件数
	Unsold int `json:"unsold"`
	// 该批次商品使用的是哪一批皮革
	LeatherNum string `json:"leather_num"`
}

func (m MarketCC) Init(stub shim.ChaincodeStubInterface) peer.Response {
	return shim.Success([]byte("Fully Success!"))
}

func (m MarketCC) Invoke(stub shim.ChaincodeStubInterface) peer.Response {
	if method, _ := stub.GetFunctionAndParameters(); method == "query" {
		return query(stub)
	} else if method == "invoke" {
		return invoke(stub)
	} else if method == "sell" {
		return sell(stub)
	}
	return shim.Error("unknown method, should be \"query\", \"invoke\" or \"sell\"")
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

	commodity := new(Commodity)
	if err := json.Unmarshal(state, commodity); err != nil {
		return fail("The data of this batch of Cargo is wrong")
	}

	return shim.Success(state)
}

func invoke(stub shim.ChaincodeStubInterface) peer.Response {

	_, args := stub.GetFunctionAndParameters()
	if len(args) < 3 {
		return fail("Wrong number of argument, expect 3")
	}

	// 把输入的参数设置到Commodity结构体中
	var commodity Commodity
	for i := 0; i < 2; i++ {
		arg := args[i]
		res, ok := parse(arg)
		if !ok {
			return fail(fmt.Sprintf("The No.%d argument should be a number", i+1))
		}
		val := reflect.ValueOf(&commodity).Elem()
		val.Field(i).SetInt(int64(res))
	}
	// 所有商品都还没被卖出
	commodity.Unsold = commodity.TotalCount

	// 皮革的序列号应该合法
	if len(args[2]) != 43 {
		return fail("The serial number of the Leather is illegal")
	}
	commodity.LeatherNum = args[2]

	// 设置时间戳字段
	timeStamp, err := stub.GetTxTimestamp()
	if err != nil {
		return fail("Can't get timestamp：" + err.Error())
	}
	commodity.TransactionTime = timeStamp.Seconds

	// 把所有信息组合
	var allbyte []byte
	// 未售出数量不应该被当做生成序列号的参数，因为会变动
	for i := 0; i < 3; i++ {
		var buff []byte
		buffer := bytes.NewBuffer(buff)
		err := binary.Write(buffer, binary.BigEndian, reflect.ValueOf(commodity).Field(i).Int())
		if err != nil {
			return fail("Can't transfer int into []byte" + err.Error())
		}
		allbyte = append(allbyte, buffer.Bytes()...)
	}
	allbyte = append(allbyte, []byte(commodity.LeatherNum)...)

	// 生成该批次商品的序列号
	shaSerial := sha256.Sum256(allbyte)
	s := base64.StdEncoding.EncodeToString(shaSerial[:])
	SerilNum := s[:len(s)-1]

	data, err := json.Marshal(commodity)
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

// 该批次商品中每有一件商品被售出就要调用一次该方法
func sell(stub shim.ChaincodeStubInterface) peer.Response {

	_, arg := stub.GetFunctionAndParameters()
	if len(arg) != 2 {
		return fail("The argument should be the Serial Number and sold quantity")
	}
	sold, ok := parse(arg[1])
	if !ok {
		return fail("The second argument should be a number")
	}

	state, err := stub.GetState(arg[0])
	if err != nil {
		return fail("Can't query the result：" + err.Error())
	}
	if state == nil {
		return fail("The Serial Number dose not exist")
	}

	commodity := new(Commodity)
	if err := json.Unmarshal(state, commodity); err != nil {
		return fail("The data of this batch of Cargo is wrong")
	}

	// 售出数量大于未售出数量说明出错了
	if sold > commodity.Unsold {
		return fail("Please check if you input the right number: not enough commodity left")
	}

	// 修改未售出商品的数量
	commodity.Unsold = commodity.Unsold - sold

	data, err := json.Marshal(commodity)
	if err != nil {
		return fail("Can't serialize to json: " + err.Error())
	}

	err = stub.PutState(arg[0], data)
	if err != nil {
		return fail("Can't save the data into ledger: " + err.Error())
	}
	return shim.Success([]byte("Selling Success"))
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

	err := shim.Start(new(MarketCC))
	if err != nil {
		fmt.Println("Can't Start Market Channel Chaincode")
	}
}
