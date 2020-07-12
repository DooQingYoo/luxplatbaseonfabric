#!/bin/bash

# 开始

function netchannel() {
  echo
  echo "#################### 开 始 创 建 通 道 ####################"
  echo
  echo
  echo "#################### 开 始 创 建 Upstream Channel ####################"
  echo
  set -x
  peer channel create -o orderer.Brand:7050 -c upstreamchannel -f ./channel-artifacts/UpstreamChannel.tx
  res1=$?
  set +x
  verifyResult $res1 "创 建 Upstream Channel"
  set -x
  peer channel create -o orderer.Brand:7050 -c downstreamchannel -f ./channel-artifacts/DownstreamChannel.tx
  res2=$?
  set +x
  verifyResult $res2 "创 建 Downstream Channel"
  set -x
  peer channel create -o orderer.Brand:7050 -c marketchannel -f ./channel-artifacts/MarketChannel.tx
  res3=$?
  set +x
  verifyResult $res3 "创 建 Market Channel"
  set -x
  peer channel create -o orderer.Brand:7050 -c querychannel -f ./channel-artifacts/QueryChannel.tx
  res4=$?
  set +x
  verifyResult $res4 "创 建 Query Channel"
  if [ $res1 -eq 0 ] && [ $res2 -eq 0 ] && [ $res3 -eq 0 ] && [ $res4 -eq 0 ]; then
    return 0
  fi
  return 1
}

function netjoin() {
  echo
  echo "#################### 节 点 开 始 加 入 通 道 ####################"
  echo
  joinChannel "0" "Brand" "upstream" \
  &&  updateAnchor "0" "Brand" "Upstream" \
  &&  joinChannel "0" "Brand" "downstream" \
  &&  updateAnchor "0" "Brand" "Downstream" \
  &&  joinChannel "0" "Brand" "market" \
  &&  updateAnchor "0" "Brand" "Market" \
  &&  joinChannel "0" "Brand" "query" \
  &&  updateAnchor "0" "Brand" "Query" \
  &&  setEnv "0" "RawHide1" "8051" \
  &&  joinChannel "0" "RawHide1" "upstream" \
  &&  setEnv "1" "RawHide1" "9051" \
  &&  joinChannel "1" "RawHide1" "upstream" \
  &&  updateAnchor "1" "RawHide1" "Upstream" \
  &&  setEnv "0" "RawHide2" "10051" \
  &&  joinChannel "0" "RawHide2" "upstream" \
  &&  setEnv "1" "RawHide2" "11051" \
  &&  joinChannel "1" "RawHide2" "upstream" \
  &&  updateAnchor "1" "RawHide2" "Upstream" \
  &&  setEnv "0" "Leather1" "12051" \
  &&  joinChannel "0" "Leather1" "upstream" \
  &&  joinChannel "0" "Leather1" "downstream" \
  &&  setEnv "1" "Leather1" "13051" \
  &&  joinChannel "1" "Leather1" "upstream" \
  &&  updateAnchor "1" "Leather1" "Upstream" \
  &&  joinChannel "1" "Leather1" "downstream" \
  &&  updateAnchor "1" "Leather1" "Downstream" \
  &&  setEnv "0" "Leather2" "14051" \
  &&  joinChannel "0" "Leather2" "upstream" \
  &&  joinChannel "0" "Leather2" "downstream" \
  &&  setEnv "1" "Leather2" "15051" \
  &&  joinChannel "1" "Leather2" "upstream" \
  &&  updateAnchor "1" "Leather2" "Upstream" \
  &&  joinChannel "1" "Leather2" "downstream" \
  &&  updateAnchor "1" "Leather2" "Downstream" \
  &&  setEnv "0" "Factory1" "16051" \
  &&  joinChannel "0" "Factory1" "downstream" \
  &&  joinChannel "0" "Factory1" "market" \
  &&  setEnv "1" "Factory1" "17051" \
  &&  joinChannel "1" "Factory1" "downstream" \
  &&  updateAnchor "1" "Factory1" "Downstream" \
  &&  joinChannel "1" "Factory1" "market" \
  &&  updateAnchor "1" "Factory1" "Market" \
  &&  setEnv "0" "Factory2" "18051" \
  &&  joinChannel "0" "Factory2" "downstream" \
  &&  joinChannel "0" "Factory2" "market" \
  &&  setEnv "1" "Factory2" "19051" \
  &&  joinChannel "1" "Factory2" "downstream" \
  &&  updateAnchor "1" "Factory2" "Downstream" \
  &&  joinChannel "1" "Factory2" "market" \
  &&  updateAnchor "1" "Factory2" "Market" \
  &&  setEnv "0" "Retailer1" "20051" \
  &&  joinChannel "0" "Retailer1" "market" \
  &&  updateAnchor "0" "Retailer1" "Market" \
  &&  joinChannel "0" "Retailer1" "query" \
  &&  updateAnchor "0" "Retailer1" "Query" \
  &&  setEnv "0" "Retailer2" "21051" \
  &&  joinChannel "0" "Retailer2" "market" \
  &&  updateAnchor "0" "Retailer2" "Market" \
  &&  joinChannel "0" "Retailer2" "query" \
  &&  updateAnchor "0" "Retailer2" "Query" \
  &&  setEnv "0" "Retailer3" "22051" \
  &&  joinChannel "0" "Retailer3" "market" \
  &&  updateAnchor "0" "Retailer3" "Market" \
  &&  joinChannel "0" "Retailer3" "query" \
  &&  updateAnchor "0" "Retailer3" "Query" \
  &&  setEnv "0" "Retailer4" "23051" \
  &&  joinChannel "0" "Retailer4" "market" \
  &&  updateAnchor "0" "Retailer4" "Market" \
  &&  joinChannel "0" "Retailer4" "query" \
  &&  updateAnchor "0" "Retailer4" "Query"
}

function joinChannel() {
  echo
  echo "#################### peer${1}.${2} 正 在 加 入 ${3}channel ####################"
  echo
  set -x
  peer channel join -b ${3}channel.block
  res=$?
  set +x
  echo
  if [ $res -eq 0 ]; then
    echo "#################### peer${1}.${2} 加 入 ${3}channel 成 功 ####################"
  else
    echo "!!!!!!!!!!!!!!!!!!!! peer${1}.${2} 加 入 ${3}channel 失 败 !!!!!!!!!!!!!!!!!!!!"
  fi
  echo
}

function updateAnchor() {
  echo
  echo "#################### 正在将peer${1}.${2}升级为${3}通道的锚节点 ####################"
  echo
  # 这个方法用来把字符串全部转为小写
  chID=$(echo "$3" | tr [:upper:] [:lower:])
  set -x
  peer channel update -o orderer.Brand:7050 -c ${chID}channel -f /opt/gopath/src/github.com/hyperledger/fabric/peer/channel-artifacts/${3}_${2}.tx
  res=$?
  set +x
  echo
  if [ $res -eq 0 ]; then
    echo "#################### peer${1}.${2}于${3}通道成功升级为锚节点 ####################"
  else
    echo "!!!!!!!!!!!!!!!!!!!! peer${1}.${2}于${3}通道升级锚节点失败 !!!!!!!!!!!!!!!!!!!!"
    return 1
  fi
}

function netinstall() {
  echo
  echo "#################### 开 始 安 装 链 码 ####################"
  echo
  setEnv "0" "Brand" "7051" \
  &&  installcc "0" "Brand" "Upstream" \
  &&  installcc "0" "Brand" "Downstream" \
  &&  installcc "0" "Brand" "Market" \
  &&  installcc "0" "Brand" "Query" \
  &&  setEnv "0" "RawHide1" "8051" \
  &&  installcc "0" "RawHide1" "Upstream" \
  &&  setEnv "0" "RawHide2" "10051" \
  &&  installcc "0" "RawHide2" "Upstream" \
  &&  setEnv "0" "Leather1" "12051" \
  &&  installcc "0" "Leather1" "Upstream" \
  &&  installcc "0" "Leather1" "Downstream" \
  &&  setEnv "0" "Leather2" "14051" \
  &&  installcc "0" "Leather2" "Upstream" \
  &&  installcc "0" "Leather2" "Downstream" \
  &&  setEnv "0" "Factory1" "16051" \
  &&  installcc "0" "Factory1" "Downstream" \
  &&  installcc "0" "Factory1" "Market" \
  &&  setEnv "0" "Factory2" "18051" \
  &&  installcc "0" "Factory2" "Downstream" \
  &&  installcc "0" "Factory2" "Market" \
  &&  setEnv "0" "Retailer1" "20051" \
  &&  installcc "0" "Retailer1" "Market" \
  &&  installcc "0" "Retailer1" "Query" \
  &&  setEnv "0" "Retailer2" "21051" \
  &&  installcc "0" "Retailer2" "Market" \
  &&  installcc "0" "Retailer2" "Query" \
  &&  setEnv "0" "Retailer3" "22051" \
  &&  installcc "0" "Retailer3" "Market" \
  &&  installcc "0" "Retailer3" "Query" \
  &&  setEnv "0" "Retailer4" "23051" \
  &&  installcc "0" "Retailer4" "Market" \
  &&  installcc "0" "Retailer4" "Query"
}

function installcc() {
  echo
  echo "#################### peer${1}.${2}正在安装${3}通道链码 ####################"
  echo
  set -x
  peer chaincode install -n ${3}CC -p chaincode/${3}Channel -v 1.0
  res=$?
  set +x
  echo
  if [ $res == 0 ]; then
    echo "#################### peer${1}.${2}在${3}通道链码安装成功 ####################"
  else
    echo "!!!!!!!!!!!!!!!!!!!! peer${1}.${2}在${3}通道链码安装失败 !!!!!!!!!!!!!!!!!!!!"
    return 1
  fi
  echo
}

function netinstantiate() {
  echo
  echo "#################### 开 始 实 例 化 链 码 ####################"
  echo
  setEnv "0" "Brand" "7051"
  instantiatecc "Upstream"
  if [ $? -ne 0 ]; then
    return 1
  fi
  instantiatecc "Downstream"
  if [ $? -ne 0 ]; then
    return 1
  fi
  instantiatecc "Market"
  if [ $? -ne 0 ]; then
    return 1
  fi
  instantiatecc "Query"
  if [ $? -ne 0 ]; then
    return 1
  fi
}

function instantiatecc() {
  echo
  echo "#################### 正 在 实 例 化 ${1} 通 道 链 码 ####################"
  echo
  chID=$(echo $1 | tr [:upper:] [:lower:])
  set -x
  peer chaincode instantiate -o orderer.Brand:7050 -C ${chID}channel -c '{"Args":["init"]}' -n ${1}CC -v 1.0
  res=$?
  set +x
  echo
  if [ $res -eq 0 ]; then
    echo "#################### ${1} 通 道 链 码 实 例 化 成 功 ####################"
  else
    echo "!!!!!!!!!!!!!!!!!!!! ${1} 通 道 链 码 实 例 化 失 败 !!!!!!!!!!!!!!!!!!!!"
    return 1
  fi
  echo
}

function verifyResult() {
  echo
  if [ $1 -ne 0 ]; then
    echo "!!!!!!!!!!!!!!!!!!!! $2 失 败 !!!!!!!!!!!!!!!!!!!!"
  else
    echo "#################### $2 成 功 ####################"
  fi
  echo
}

function setEnv() {
  CORE_PEER_LOCALMSPID=${2}MSP
  CORE_PEER_ADDRESS=peer${1}.${2}:${3}
  CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/${2}/users/Admin@${2}/msp
}

if [ "$1" == "channel" ]; then
  netchannel
elif [ "$1" == "join" ]; then
  netjoin
elif [ "$1" == "install" ]; then
  netinstall
elif [ "$1" == "instantiate" ]; then
  netinstantiate
fi
