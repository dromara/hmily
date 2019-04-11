<template>
    <div class="fillcontain">
        <headTop></headTop>
        <div class="table_container">
            <div style="margin-top: 15px;margin-bottom: 10px;display: flex;justify-content: flex-start">
                <div style="margin-left: 2%">
                    <el-select v-model="selected" placeholder="请选择Namespace">
                        <el-option
                                v-for="x in options"
                                :key="x"
                                :label="x"
                                :value="x">
                        </el-option>
                    </el-select>
                </div>
                <div style="width: 10rem;margin-left: 2%">
                    <el-input v-model="retryCount" placeholder="请输入重试次数"></el-input>
                </div>
                <div style="width: 10rem;margin-left: 2%">
                    <el-input v-model="transId" placeholder="请输入事务ID"></el-input>
                </div>
                <div style="margin-left: 2%">
                    <el-button @click="query">查询</el-button>
                </div>
                <div style="clear:both"></div>
            </div>
            <el-table
                    :border=true
                    ref="multipleTable"
                    :data="tableData"
                    highlight-current-row
                    style="width: 100%">
                <el-table-column
                        align="center"
                        type="selection"
                        width="40">
                </el-table-column>
                <el-table-column
                        align="center"
                        fixed="right"
                        label="操作"
                        width="100">
                    <template slot-scope="scope">
                        <el-button type="text" @click="editClicked(scope.row)" size="small">编辑</el-button>
                    </template>
                </el-table-column>
                <el-table-column
                        property="transId"
                        width="180"
                        align="center"
                        label="ID">
                </el-table-column>
                <el-table-column
                        width="120"
                        property="retriedCount"
                        align="center"
                        label="重试次数">
                </el-table-column>
                <el-table-column
                        align="center"
                        width="120"
                        :show-overflow-tooltip=true
                        property="version"
                        label="版本">
                </el-table-column>
                <el-table-column
                        align="center"
                        min-width="200"
                        :show-overflow-tooltip=true
                        property="targetClass"
                        label="事务接口">
                </el-table-column>
                <el-table-column
                        align="center"
                        min-width="200"
                        :show-overflow-tooltip=true
                        property="targetMethod"
                        label="事务方法">
                </el-table-column>
                <el-table-column
                        align="center"
                        min-width="200"
                        :show-overflow-tooltip=true
                        property="confirmMethod"
                        label="confirm方法">
                </el-table-column>
                <el-table-column
                        align="center"
                        min-width="200"
                        :show-overflow-tooltip=true
                        property="cancelMethod"
                        label="cancel方法">
                </el-table-column>
                <el-table-column
                        width="200"
                        align="center"
                        :show-overflow-tooltip=true
                        property="createTime"
                        label="创建时间">
                </el-table-column>
                <el-table-column
                        width="240"
                        align="center"
                        property="lastTime"
                        label="最后执行时间">
                </el-table-column>
            </el-table>
            <div style="">
                <div style="margin-top: 20px; margin-left:20px;float: left">
                    <el-button type="danger" @click="deleteAll()">删除勾选数据</el-button>
                </div>
                <div class="Pagination" style="text-align: left;margin-top: 20px;float: right">
                    <el-pagination
                            @size-change="handleSizeChange"
                            @current-change="handleCurrentChange"
                            :current-page="paging.currentPage"
                            :page-sizes="[10,20,50, 100, 200]"
                            :page-size="paging.limit"
                            layout="total, sizes, prev, pager, next, jumper"
                            :total="count">
                    </el-pagination>
                </div>
            </div>
        </div>
        <!-- Form -->

        <el-dialog title="改变重试次数" :visible.sync="dialogFormVisible">
            <el-form :model="form">
                <div style="margin-left: 0px;width: 80%">
                    <el-form-item label="重试次数：" :label-width="formLabelWidth">
                        <el-input v-model="form.newRetryCount" auto-complete="off"></el-input>
                    </el-form-item>
                </div>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button @click="dialogFormVisible = false">取 消</el-button>
                <el-button type="primary" @click="updateRetryCount">确 定</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
    import headTop from '../components/headTop'
    import ElButton from "../../node_modules/element-ui/packages/button/src/button.vue";

    export default {
        data() {
            return {

                tableData: [],
                searchValue: "",
                paging: {
                    limit: 10,
                    currentPage: 1,
                },
                count: 0,
                res: null,
                options: [],
                selected: "",
                retryCount: '',
                txGroupId: "",
                //更改重试次数
                dialogFormVisible: false,
                form: {
                    newRetryCount: null
                },
                formLabelWidth: '120px',
                currentRow:null,
                baseUrl: document.getElementById('serverIpAddress').href
            }
        },
        components: {
            ElButton,
            headTop,
        },
        created() {
            this.$http.post(this.baseUrl + '/compensate/listAppName', {}).then(
                response => {
                    if (response.body.code == 200 && response.body.data != null) {
                        this.options = response.body.data;
                        this.selected = this.options[0];
                    } else {
                        this.$message({
                            type: 'error',
                            message: '获取数据失败或者数据为空!'
                        });
                    }
                    console.log("success!");
                },
                response => {
                    this.$message({
                        type: 'error',
                        message: response
                    });
                }
            );
        },
        methods: {
            editClicked(row) {
                this.dialogFormVisible = true;
                this.currentRow = row;
            },
            updateRetryCount: function () {
                let tData = this.tableData;
                this.$http.post(this.baseUrl + '/compensate/update', {
                    "applicationName": this.selected,
                    "retry": this.form.newRetryCount,
                    "id": this.currentRow.transId
                }).then(
                    response => {
                        if (response.body.code == 200) {
                            for(var i=0,len=tData.length;i<len;i++)
                            {
                                if(this.currentRow.id == tData[i].id)
                                {
                                    tData[i].retriedCount = this.form.newRetryCount;
                                }
                            }
                            this.dialogFormVisible = false;
                            this.$message({
                                type: 'success',
                                message: '更新数据成功!'
                            });
                        }else {
                            this.$message({
                                type: 'error',
                                message: response.body.message
                            });
                        }
                    },
                    response => {
                        this.$message({
                            type: 'error',
                            message: response
                        });
                    }
                )
            },
            query: function () {
                this.$http.post(this.baseUrl + '/compensate/listPage', {
                    "pageParameter": {
                        "pageSize": this.paging.limit,
                    },
                    "applicationName": this.selected,
                    "retry": this.retryCount,
                    "transId": this.transId
                }).then(
                    response => {
                        if (response.body.code == 200 && response.body.data != null) {
                            let rp = response.body;
                            this.count = rp.data.page.totalCount;
                            this.tableData = rp.data.dataList
                        } else {
                            this.$message({
                                type: 'error',
                                message: '获取数据失败或者数据为空!'
                            });
                        }

                        console.log("success!");
                    },
                    response => {
                        this.$message({
                            type: 'error',
                            message: response
                        });
                    }
                )
            },
            deleteAll: function () {
                var Selection = this.$refs.multipleTable.selection;
                var groupIds = [];
                var slen = Selection.length;
                for (var i = 0; i < slen; i++) {
                    groupIds.push(Selection[i].transId);
                }
                //delete row and update tableData but don't send post request to update all data
                var oldTableData = this.tableData;
                var tlen = oldTableData.length;
                this.$http.post(this.baseUrl + '/compensate/batchRemove', {
                    "applicationName": this.selected,
                    "ids": groupIds
                }).then(
                    response => {
                        if (response.body.code == 200) {
                            this.$message({type: 'success', message: '删除数据成功!'});
                            for (let x = 0; x < slen; x++) {
                                for (let j = 0; j < tlen; j++) {
                                    if (groupIds[x] == oldTableData[j].transId) {
                                        oldTableData.splice(j, 1);
                                        tlen = tlen - 1;
                                    }
                                }
                            }
                            this.count = this.count - slen;
                        } else {
                            this.$message({
                                type: 'error',
                                message: '删除数据失败!'
                            });
                        }
                    },
                    response => {
                        this.$message({
                            type: 'error',
                            message: response
                        });
                    }
                )

            },
            subRow: function () {
                return {"font-size": "0.85em"};
            },
            subTableHeader: function () {
                return {"background-color": "red", "font-size": "0.6em"};
            },
            handleSizeChange(val) {
                this.paging.limit = val;
            },
            handleCurrentChange(val) {
                this.paging.currentPage = val;
            },
        },
        watch: {
            paging: {
                handler: function () {
                    this.$http.post(this.baseUrl + '/compensate/listPage', {
                        "pageParameter": {
                            "currentPage": this.paging.currentPage,
                            "pageSize": this.paging.limit,
                        },
                        "applicationName": this.selected,
                        "retry": this.retryCount,
                        "transId": this.transId
                    }).then(
                        response => {
                            if (response.body.code == 200) {
                                let rp = response.body;
                                this.count = rp.data.page.totalCount;
                                this.tableData = rp.data.dataList
                            } else {
                                this.$message({
                                    type: 'error',
                                    message: '获取数据失败!'
                                });
                            }

                            console.log("success!");
                        },
                        response => {
                            this.$message({
                                type: 'error',
                                message: response
                            });
                        }
                    )
                },
                deep: true
            }

        }
    }
</script>

<style lang="less">
    @import '../style/mixin';

    .table_container {
        padding: 0px;
    }

    .subTableHeaderFont {
        font-size: 0.9em;
        padding: 0px;
        text-align: center;
        color: rebeccapurple !important;
        background-color: white !important;
    }

    .el-input {

    }

    .el-table__expanded-cell {
        padding: 5px !important;
    }
</style>
