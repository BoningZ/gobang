<template>
  <Navi/>
  <div>
    <el-tag >我方棋子：{{isHost==="true"?"黑":"白"}}</el-tag>
    <el-tag >房号：{{rid}}</el-tag>
    <el-tag type="success" v-show="isBlack.toString()==isHost.toString()">我方行棋</el-tag>
    <el-tag type="error" v-show="isBlack.toString()!=isHost.toString()">对方行棋</el-tag>
    <el-tag type="success" v-show="(win===1&&isHost==='true')||(win===2&&isHost==='false')">我方胜利</el-tag>
    <el-tag type="error" v-show="(this.win===2&&isHost==='true')||(this.win===1&&isHost==='false')">对方胜利</el-tag>
  </div>
  <div class="gobang">
    <canvas id="gobang" width="800" height="600"></canvas>
  </div>
</template>

<script>
import Navi from '@/components/Navi'
import {ElMessage} from "element-plus";
export default {
  name: "Room",
  components:{ Navi},
  data() {
    return {
      rid:"",
      chess:"",
      isHost:false,
      isBlack:false,
      win:0,
      ctx: null,
    };
  },
  created() {
    this.rid = this.$route.query.rid;
    this.isHost=this.$route.query.isHost;
  },
  mounted() {
    this.rid = this.$route.query.rid;
    this.isHost=this.$route.query.isHost;
    let _this = this;
    let container = document.getElementById("gobang");

    container.addEventListener("click", _this.handleClick);

    _this.ctx = container.getContext("2d");
    _this.ctx.translate(70,70);
    _this.drawCheckerboard();


    console.log("建立连接")
      if ("WebSocket" in window) {
        this.webSocket = new WebSocket("ws://81.68.176.199:9090/room/" + this.rid)
      } else {
        ElMessage({
          showClose: true,
          message: '不支持socket连接！',
          type: 'warning',
        })
      }
      //连接发生错误的回调方法
      this.webSocket.onerror = function () {
        ElMessage({
          showClose: true,
          message: '连接失败',
          type: 'error',
        })
      }

      //接收到消息的回调方法
      let that = this;
      this.webSocket.onmessage = function (event) {
        let infoList = eval("(" + event.data + ")")
        if(infoList.room==that.rid) {

          if (infoList.type === 1) {
            ElMessage({
              showClose: true,
              message: infoList.msg,
              type: 'error',
            })
          } else {
            that.chess = infoList.chess;
            that.isBlack = infoList.isBlack;
            that.win = infoList.win;
            that.redraw();
          }
        }
      };

      this.webSocket.onclose = function (event) {
        console.log("关闭：" + event)
        that.webSocket.close()
      }

  },
  methods: {
    redraw(){
      var k=0;
      for(var i=0;i<15;i++)
        for(var j=0;j<15;j++){
          if(this.chess.charAt(k)==='1')this.drawChess(i,j,true);
          if(this.chess.charAt(k)==='2')this.drawChess(i,j,false);
          k++;
        }

    },
    drawCheckerboard() {
      // 画棋盘
      let _this = this;
      _this.ctx.beginPath();
      _this.ctx.fillStyle = "#fff";
      _this.ctx.rect(0, 0, 450, 450);
      _this.ctx.fill();
      for (var i = 0; i < 15; i++) {
        _this.ctx.beginPath();
        _this.ctx.strokeStyle = "#D6D1D1";
        _this.ctx.moveTo(15 + i * 30, 15); //垂直方向画15根线，相距30px;
        _this.ctx.lineTo(15 + i * 30, 435);
        _this.ctx.stroke();
        _this.ctx.moveTo(15, 15 + i * 30); //水平方向画15根线，相距30px;棋盘为14*14；
        _this.ctx.lineTo(435, 15 + i * 30);
        _this.ctx.stroke();


      }
    },
    drawChess(xLine, yLine,isBlack) {
      let _this = this;
      let grd = _this.ctx.createRadialGradient(
          xLine * 30 + 15,
          yLine * 30 + 15,
          4,
          xLine * 30 + 15,
          yLine * 30 + 15,
          10
      );
      grd.addColorStop(0, !isBlack ? "#fff" : "#4c4c4c");
      grd.addColorStop(1, !isBlack ? "#dadada" : "#000");
      _this.ctx.beginPath();
      _this.ctx.fillStyle = grd;
      _this.ctx.arc(
          xLine * 30 + 15,
          yLine * 30 + 15,
          10,
          0,
          2 * Math.PI,
          false
      );
      _this.ctx.fill();
      _this.ctx.closePath();


    },




    handleClick(event) {
      let x = event.offsetX - 70;
      let y = event.offsetY - 70;
      if (x < 15 || x > 435 || y < 15 || y > 435) {
        // 点出界的
        return;
      }
      let xLine = Math.round((x - 15) / 30); // 竖线第x条
      let yLine = Math.round((y - 15) / 30); // 横线第y条
      let socketMsg = {x: xLine,y:yLine,id:this.$route.query.userId};

      // console.log(JSON.stringify(socketMsg))
      this.webSocket.send(JSON.stringify(socketMsg));
    }
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="css">
.gobang {

    background: #2a4546;

}
</style>