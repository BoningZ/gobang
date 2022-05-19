import 
<template>
  <Navi/>

  <div class="app-container">
    <el-form :inline="true" :model="form" class="form-inline-query">
      <el-form-item label="房间号" >
        <el-input v-model="form.rid" placeholder="请输入房主号" />
      </el-form-item>
      <el-form-item label="房主号">
        <el-input v-model="form.hostId" placeholder="请输入房主号"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button class="commButton" size="mini" @click="doQuery()">查询</el-button>
      </el-form-item>
      <el-form-item>
        <el-button class="commButton" size="mini" @click="doAdd()">新建</el-button>
      </el-form-item>
      <el-form-item>
        <el-button class="commButton" size="mini" @click="doQueryMy()">我参与的</el-button>
      </el-form-item>
    </el-form>
    <el-table  class="table-content" :data="dataList" border style="width: 100%;" size="mini" height="500">

      <el-table-column label="序号" fixed="left" width="50" align="center" color="black">
        <template v-slot="scope">
          {{ scope.$index+1 }}
        </template>
      </el-table-column><!--第一列-->

      <el-table-column label="房间号" align="center" color="black" sortable prop="rid" /><!--courseNum-->
      <el-table-column label="房主" align="center" color="black" sortable prop="host" /><!--courseName-->
      <el-table-column label="访客" align="center" color="black" sortable prop="guest" />
      <el-table-column label="操作" align="center" color="black">
        <template v-slot="scope">
          <el-button type="text" size="mini" @click="doJoin(scope.row.rid)">进入</el-button>
        </template>
      </el-table-column><!--编辑删除按钮-->
    </el-table>
  </div>

  <div class="home">
<!--    <input type="button" value="登出" v-on:click="logout()">-->
    <el-button type="danger" @click='logout'>登出</el-button>
  </div>
</template>

<script>
import Navi from '@/components/Navi'
import router from '@/router/index.js'
import {getRoomList,addRoom,joinRoom} from '@/service/genServ.js'

export default {
  name: 'Home',
  components:{ Navi},
  data() {
    return {
      form: {
        rid:'',
        hostId:'',
      },
      dataList:[]
    }
  },
  created() {
    this.doQuery();
  },
  methods:{
    logout() {
      this.$store.commit('logout')
      router.push('/login')
    },
    doQuery() {
      getRoomList({'rid':this.form.rid.toString(), 'hostId':this.form.hostId.toString}).then(res => {
        this.dataList = res.data
      })
    },
    doQueryMy(){
      getRoomList({ 'my':true}).then(res => {
        this.dataList = res.data
      })
    },
    doJoin(rid) {
      joinRoom({'rid':rid}).then(res=>{
        if(res.code==='0')this.$router.push({ path: 'Room', query: { 'rid':rid ,'isHost':res.data.isHost,'userId':res.data.userId}})
        else {
          this.$message({
            message: '加入失败，已自动刷新',
            type: 'warning',
          })
          this.doQuery();
        }
      })
    },
    doAdd() {
      addRoom().then(res=>{if(res.code==='0')this.doQuery();});
    },
  }
}
</script>
