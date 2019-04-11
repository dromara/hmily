<template>
    <div class="header_container">
		<el-breadcrumb separator="/">
			<el-breadcrumb-item :to="{ path: '/manage' }" >首页</el-breadcrumb-item>
			<el-breadcrumb-item v-for="(item, index) in $route.meta" key="index">{{item}}</el-breadcrumb-item>
		</el-breadcrumb>
		<el-dropdown @command="handleCommand" menu-align='start'>
			<img src="static/assets/patrick.jpg" class="avator">
			<el-dropdown-menu slot="dropdown">
				<el-dropdown-item command="home">首页</el-dropdown-item>
				<el-dropdown-item command="singout">退出</el-dropdown-item>
			</el-dropdown-menu>
		</el-dropdown>
    </div>
</template>

<script>
    import srcImage from '../assets/patrick.jpg'

    export default {
    	data(){
    		return {
                baseUrl: document.getElementById('serverIpAddress').href
    		}
    	},
    	created(){
    	},
    	computed: {
    	},
		methods: {
            signOut: function () {
                this.$http.post(this.baseUrl + '/logout', {
                }).then(
                    response => {
                        if (response.body.code == 200) {
                            this.$message({
                                type: 'success',
                                message: '签退成功!'
                            });
                            this.$router.push('/');
                        } else if (response.body.data == false) {
                            this.$message({
                                type: 'error',
                                message: '签退错误，请联系管理员!'
                            });
                        }

                        console.log(response.body);
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
			async handleCommand(command) {
				if (command == 'home') {
					this.$router.push('/manage');
				}else if(command == 'singout'){
					await this.signOut();
					}else{
						this.$message({
	                        type: 'error',
	                        message: res.message
	                    });
					}
				}
			},
    }
</script>

<style lang="less">
	@import '../style/mixin';
	.header_container{
		background-color: #EFF2F7;
		height: 60px;
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding-left: 20px;
	}
	.avator{
		.wh(36px, 36px);
		border-radius: 50%;
		margin-right: 37px;
	}
	.el-dropdown-menu__item{
        text-align: center;
    }
</style>
