#!/bin/bash

# 用于控制区块链网络

function printHelp() {
  echo "使用方法：$0 [\$option]"
  echo "option选项有："
  echo "generate：生成证书和交易文件等"
  echo "up：启动整个区块链网络"
  echo "down：结束整个区块链网络"
  echo "clear：清除证书和交易文件等"
  echo "channel：创建所有的通道"
  echo "join：所有的节点加入相应的通道"
  echo "install：所有的节点安装相应的链码"
  echo "instantiate：将链码进行初始化"
}

function bcnetclear() {
  echo
  echo "####################正在清除证书和交易文件####################"
  echo
  set -x
  rm -rf crypto-config
  rm -f channel-artifacts/*
  set +x
  echo
  echo "####################清除证书和交易文件成功####################"
  echo
}

function bcnetup() {
  echo ""
  echo "####################正在创建docker容器####################"
  echo ""
  docker-compose -f docker-compose.yaml up -d
  docker_stat=$?
  # 等1秒看看起了多少个容器
  sleep 1s
  docker_numb=$(docker ps | wc -l)
  # 应该有21个容器成功运行起来的，如果没有说明出了问题
  if [ "$docker_numb" -lt 21 ] || [ "$docker_stat" -ne 0 ]; then
    echo
    echo "!!!!!!!!!!!!!!!!!!!!docker容器创建失败!!!!!!!!!!!!!!!!!!!!"
    echo
    exit 1
  fi
  set -x
  docker ps
  set +x
  echo
  echo "####################docker容器创建成功####################"
  echo
}

function bcnetdown() {
  echo
  echo "####################正在关闭docker容器与网络####################"
  echo
  docker-compose -f docker-compose.yaml down
  CONTAINER_IDS=$(docker ps -a | awk '($2 ~ /dev-peer.*/) {print $1}')
  if [ -z "$CONTAINER_IDS" -o "$CONTAINER_IDS" == "" ]; then
    echo
    echo "####################没有发现链码容器####################"
    echo
  else
    docker rm -f $CONTAINER_IDS
  fi
  # 每次实例化链码都会生成新的docker镜像，必须把镜像删掉，否则以后实例化同名的链码将不会被编译
  # 所有实例化链码的镜像都是以"dev-peer."开头的
  DOCKER_IMAGE_IDS=$(docker images | awk '($1 ~ /dev-peer.*/) {print $3}')
  if [ -z "$DOCKER_IMAGE_IDS" -o "$DOCKER_IMAGE_IDS" == " " ]; then
    echo
    echo "####################没有发现链码镜像####################"
    echo
  else
    docker rmi -f $DOCKER_IMAGE_IDS
  fi
  echo
  echo "####################成功关闭docker容器与网络####################"
  echo
}

function bcnetgenerate() {
  if [ -e "./crypto-config" ] && [ -d "./crypto-config" ]; then
    echo "文件已存在，不需要创建"
    return 0
  fi
  echo
  echo "####################正在创建证书和交易文件####################"
  echo
  cryptogen generate --config=./crypto_config.yaml
  configtxgen -profile DefaultGenesis -outputBlock ./channel-artifacts/genesis.block
  configtxgen -profile UpstreamChannel -outputCreateChannelTx ./channel-artifacts/UpstreamChannel.tx -channelID upstreamchannel
  configtxgen -profile DownstreamChannel -outputCreateChannelTx ./channel-artifacts/DownstreamChannel.tx -channelID downstreamchannel
  configtxgen -profile MarketChannel -outputCreateChannelTx ./channel-artifacts/MarketChannel.tx -channelID marketchannel
  configtxgen -profile QueryChannel -outputCreateChannelTx ./channel-artifacts/QueryChannel.tx -channelID querychannel
  configtxgen -profile UpstreamChannel -outputAnchorPeersUpdate ./channel-artifacts/Upstream_Brand.tx -channelID upstreamchannel -asOrg Brand
  configtxgen -profile UpstreamChannel -outputAnchorPeersUpdate ./channel-artifacts/Upstream_RawHide1.tx -channelID upstreamchannel -asOrg RawHide1
  configtxgen -profile UpstreamChannel -outputAnchorPeersUpdate ./channel-artifacts/Upstream_RawHide2.tx -channelID upstreamchannel -asOrg RawHide2
  configtxgen -profile UpstreamChannel -outputAnchorPeersUpdate ./channel-artifacts/Upstream_Leather1.tx -channelID upstreamchannel -asOrg Leather1
  configtxgen -profile UpstreamChannel -outputAnchorPeersUpdate ./channel-artifacts/Upstream_Leather2.tx -channelID upstreamchannel -asOrg Leather2
  configtxgen -profile DownstreamChannel -outputAnchorPeersUpdate ./channel-artifacts/Downstream_Brand.tx -channelID downstreamchannel -asOrg Brand
  configtxgen -profile DownstreamChannel -outputAnchorPeersUpdate ./channel-artifacts/Downstream_Leather1.tx -channelID downstreamchannel -asOrg Leather1
  configtxgen -profile DownstreamChannel -outputAnchorPeersUpdate ./channel-artifacts/Downstream_Leather2.tx -channelID downstreamchannel -asOrg Leather2
  configtxgen -profile DownstreamChannel -outputAnchorPeersUpdate ./channel-artifacts/Downstream_Factory1.tx -channelID downstreamchannel -asOrg Factory1
  configtxgen -profile DownstreamChannel -outputAnchorPeersUpdate ./channel-artifacts/Downstream_Factory2.tx -channelID downstreamchannel -asOrg Factory2
  configtxgen -profile MarketChannel -outputAnchorPeersUpdate ./channel-artifacts/Market_Brand.tx -channelID marketchannel -asOrg Brand
  configtxgen -profile MarketChannel -outputAnchorPeersUpdate ./channel-artifacts/Market_Factory1.tx -channelID marketchannel -asOrg Factory1
  configtxgen -profile MarketChannel -outputAnchorPeersUpdate ./channel-artifacts/Market_Factory2.tx -channelID marketchannel -asOrg Factory2
  configtxgen -profile MarketChannel -outputAnchorPeersUpdate ./channel-artifacts/Market_Agent1.tx -channelID marketchannel -asOrg Agent1
  configtxgen -profile MarketChannel -outputAnchorPeersUpdate ./channel-artifacts/Market_Agent2.tx -channelID marketchannel -asOrg Agent2
  configtxgen -profile MarketChannel -outputAnchorPeersUpdate ./channel-artifacts/Market_Retailer1.tx -channelID marketchannel -asOrg Retailer1
  configtxgen -profile MarketChannel -outputAnchorPeersUpdate ./channel-artifacts/Market_Retailer2.tx -channelID marketchannel -asOrg Retailer2
  configtxgen -profile MarketChannel -outputAnchorPeersUpdate ./channel-artifacts/Market_Retailer3.tx -channelID marketchannel -asOrg Retailer3
  configtxgen -profile MarketChannel -outputAnchorPeersUpdate ./channel-artifacts/Market_Retailer4.tx -channelID marketchannel -asOrg Retailer4
  configtxgen -profile QueryChannel -outputAnchorPeersUpdate ./channel-artifacts/Query_Brand.tx -channelID querychannel -asOrg Brand
  configtxgen -profile QueryChannel -outputAnchorPeersUpdate ./channel-artifacts/Query_Retailer1.tx -channelID querychannel -asOrg Retailer1
  configtxgen -profile QueryChannel -outputAnchorPeersUpdate ./channel-artifacts/Query_Retailer2.tx -channelID querychannel -asOrg Retailer2
  configtxgen -profile QueryChannel -outputAnchorPeersUpdate ./channel-artifacts/Query_Retailer3.tx -channelID querychannel -asOrg Retailer3
  configtxgen -profile QueryChannel -outputAnchorPeersUpdate ./channel-artifacts/Query_Retailer4.tx -channelID querychannel -asOrg Retailer4
  echo
  echo "####################创建证书和交易文件完毕####################"
  echo
}

function bcnetcli() {
  if ! docker exec cli ./cli.sh "$1"; then
    echo
    echo "!!!!!!!!!!!!!!!!!!!! ${1} 操 作 失 败 !!!!!!!!!!!!!!!!!!!!"
    echo
  else
    echo
    echo "#################### ${1} 操 作 成 功 ####################"
    echo
  fi
}

# 判断要用哪个方法
if [ "$1" == "up" ]; then
  bcnetup
elif [ "$1" == "down" ]; then
  bcnetdown
elif [ "$1" == "generate" ]; then
  bcnetgenerate
elif [ "$1" == "clear" ]; then
  bcnetclear
elif [ "$1" == "channel" ] || [ "$1" == "join" ] || [ "$1" == "install" ] || [ "$1" == "instantiate" ]; then
  bcnetcli "$1"
else
  printHelp
fi
