package main

import (
	"crypto/sha256"
	"encoding/base64"
	"encoding/binary"
	"encoding/json"
	"fmt"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/hyperledger/fabric/protos/peer"
	"strconv"
	"time"
)

// enum Layer of the leather
type Layer int

const (
	FullGrain Layer = iota
	EmbossedGrain
	SplitSuede
	NubuckSuededGrain
	FibreLeather
)

// emum tanning method of the leather
type Tanning int

const (
	Chrome Tanning = iota
	Vegitable
	Aluminum
	Oil
)

// enum animal types
type AnimalType int

const (
	Cow AnimalType = iota
	Goat
	Pig
	Deer
	Fish
	Snake
	Ostrich
	Crocodile
)

type Commodity struct {
	Num      string `json:"num"`       //serial number of the bag
	Retailer int    `json:"retailer"`  //serial number of the retailer
	SellTime int64  `json:"sell_time"` //when the bag is sold
	Queried  bool   `json:"queried"`   //if the bag has been queried before
	Agent    int    `json:"agent"`     //name of the agent
}

type Bag struct {
	Leather int    //serial number of the leather
	Factory int //name of the factory
	ProduceTime int64 //when the bag is produced
}

type Leather struct {
	Num     int     //serial number of this batch
	Com     int  //name of the supplier
	Layer   Layer   //layer(quality) of this batch
	Tanning Tanning //tanning method
	Animal  int     //serial number of the animal
}

type Animal struct {
	Num  int           //serial number of this batch
	Farm int        //the farm where the animals came from
	Type AnimalType    //animal's type
	Age  int //the animal's age
}

type SimpleChaincode struct {
}

func (sc SimpleChaincode) Init(stub shim.ChaincodeStubInterface) peer.Response {
	_, args := stub.GetFunctionAndParameters()
	var A, B string    // Entities
	var Aval, Bval int // Asset holdings
	var err error

	if len(args) != 4 {
		return shim.Error("Incorrect number of arguments. Expecting 4")
	}

	// Initialize the chaincode
	A = args[0]
	Aval, err = strconv.Atoi(args[1])
	if err != nil {
		return shim.Error("Expecting integer value for asset holding")
	}
	B = args[2]
	Bval, err = strconv.Atoi(args[3])
	if err != nil {
		return shim.Error("Expecting integer value for asset holding")
	}
	fmt.Printf("Aval = %d, Bval = %d\n", Aval, Bval)

	// Write the state to the ledger
	err = stub.PutState(A, []byte(strconv.Itoa(Aval)))
	if err != nil {
		return shim.Error(err.Error())
	}

	err = stub.PutState(B, []byte(strconv.Itoa(Bval)))
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(nil)
}

func (sc SimpleChaincode) Invoke(stub shim.ChaincodeStubInterface) peer.Response {
	function, args := stub.GetFunctionAndParameters()
	switch function {
	case "invoke":
		return sc.invoke(stub, args)
	case "query":
		return sc.query(stub, args)
	case "sell":
		return sc.sell(stub, args)
	case "real":
		return sc.real(stub, args)
	}
	return shim.Error("Invalid invoke function name. Expecting \"invoke\" \"delete\" \"query\"")
}

func (sc SimpleChaincode) sell(stub shim.ChaincodeStubInterface, args []string) peer.Response {

	if len(args) != 3 {
		return shim.Error("Expecting serial number of the bag")
	}
	comm := new(Commodity)
	comm.SellTime = time.Now().Unix()
	comm.Num = args[0]
	agent, err := strconv.Atoi(args[1])
	if err != nil {
		return shim.Error("Wrong agent")
	}
	comm.Agent = agent
	retailer, err := strconv.Atoi(args[2])
	if err != nil {
		return shim.Error("Wrong retailer")
	}
	comm.Retailer = retailer
	comm.Queried = false

	key := comm.genearatecode()
	value, err := json.Marshal(comm)
	if err != nil {
		return shim.Error("Wrong commodity")
	}
	err = stub.PutState(key, value)
	if err != nil {
		return shim.Error("Can't put state: " + err.Error())
	}
	return shim.Success([]byte(key))
}

func (sc SimpleChaincode) real(stub shim.ChaincodeStubInterface, args []string) peer.Response {

	if len(args) != 1 {
		return shim.Error("Expecting serial number")
	}

	value, err := stub.GetState(args[0])
	if err != nil || value == nil {
		return shim.Error("no such commodity")
	}

	var comm Commodity
	if err =json.Unmarshal(value, &comm); err != nil {
		return shim.Error("Error in Unmarshal json: " + err.Error())
	}

	//验证base64码是否正确
	if key := comm.genearatecode(); key != args[0] {
		return shim.Error("Wrong with the serial number: not compatible with the commodity attribute")
	}

	//如果是第一次被查询需要将查询状态修改为被查询过
	if comm.Queried == false {
		comm.Queried = true
		value2, err := json.Marshal(comm)
		if err != nil {
			return shim.Error(err.Error())
		}
		err = stub.PutState(args[0], value2)
		if err != nil {
			return shim.Error("Failed to update queried :" + err.Error())
		}
	}

	return shim.Success(value)
}
func (sc SimpleChaincode) invoke(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	var A, B string    // Entities
	var Aval, Bval int // Asset holdings
	var X int          // Transaction value
	var err error

	if len(args) != 3 {
		return shim.Error("Incorrect number of arguments. Expecting 3")
	}

	A = args[0]
	B = args[1]

	// Get the state from the ledger
	// TODO: will be nice to have a GetAllState call to ledger
	Avalbytes, err := stub.GetState(A)
	if err != nil {
		return shim.Error("Failed to get state")
	}
	if Avalbytes == nil {
		return shim.Error("Entity not found")
	}
	Aval, _ = strconv.Atoi(string(Avalbytes))

	Bvalbytes, err := stub.GetState(B)
	if err != nil {
		return shim.Error("Failed to get state")
	}
	if Bvalbytes == nil {
		return shim.Error("Entity not found")
	}
	Bval, _ = strconv.Atoi(string(Bvalbytes))

	// Perform the execution
	X, err = strconv.Atoi(args[2])
	if err != nil {
		return shim.Error("Invalid transaction amount, expecting a integer value")
	}
	Aval = Aval - X
	Bval = Bval + X
	fmt.Printf("Aval = %d, Bval = %d\n", Aval, Bval)

	// Write the state back to the ledger
	err = stub.PutState(A, []byte(strconv.Itoa(Aval)))
	if err != nil {
		return shim.Error(err.Error())
	}

	err = stub.PutState(B, []byte(strconv.Itoa(Bval)))
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(nil)
}

func (sc SimpleChaincode) query(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	var A string // Entities
	var err error

	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting name of the person to query")
	}

	A = args[0]

	// Get the state from the ledger
	Avalbytes, err := stub.GetState(A)
	if err != nil {
		jsonResp := "{\"Error\":\"Failed to get state for " + A + "\"}"
		return shim.Error(jsonResp)
	}

	if Avalbytes == nil {
		jsonResp := "{\"Error\":\"Nil amount for " + A + "\"}"
		return shim.Error(jsonResp)
	}

	jsonResp := "{\"Name\":\"" + A + "\",\"Amount\":\"" + string(Avalbytes) + "\"}"
	fmt.Printf("Query Response:%s\n", jsonResp)
	return shim.Success(Avalbytes)
}

func (c Commodity) genearatecode() string {

	codebytes := []byte(c.Num)

	bs := make([]byte, 4)
	binary.BigEndian.PutUint32(bs, uint32(c.Retailer))
	codebytes = append(codebytes, bs...)

	binary.BigEndian.PutUint32(bs, uint32((c.Agent)))
	codebytes = append(codebytes, bs...)

	bs = make([]byte, 8)
	binary.BigEndian.PutUint64(bs, uint64(c.SellTime))
	codebytes = append(codebytes, bs...)

	oh := sha256.Sum256(codebytes)
	code := base64.StdEncoding.EncodeToString(oh[:])

	return code
}

func main() {
	err := shim.Start(new(SimpleChaincode))
	if err != nil {
		fmt.Printf("Cannot Start Chaincode: %s", err.Error())
	}
}
