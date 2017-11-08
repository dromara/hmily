<template>
    <div>
        <head-top></head-top>
        <div style="margin-top:20%;margin-left: 20%; alignment: center">
            <h1 style="color: darkmagenta; font-size: 3rem">欢迎使用分布式事务后台管理系统!</h1>
        </div>
        <div style="clear: both"></div>
    </div>
</template>

<script>
	import headTop from '../components/headTop'
    export default {
    	data(){
    		return {
    			apiCount: null,
    		}
    	},
    	components: {
    		headTop,
    	},
    	mounted(){
    	},
        computed: {
        },
    	methods: {
    	}
    }
</script>

<style lang="less">
	@import '../style/mixin';
	.data_section{
		padding: 20px;
		margin-bottom: 40px;
		.section_title{
			text-align: center;
			font-size: 30px;
			margin-bottom: 10px;
		}
		.data_list{
			text-align: center;
			font-size: 14px;
			color: #666;
            border-radius: 6px;
            background: #E5E9F2;
            .data_num{
                color: #333;
                font-size: 26px;

            }
            .head{
                border-radius: 6px;
                font-size: 24px;
                padding: 0 10px;
                color: #fff;
            }
        }
        .today_head{
            background: #FF9800;
        }
        .all_head{
            background: #20A0FF;
        }
	}
    .wan{
        .sc(16px, #333)
    }
</style>
