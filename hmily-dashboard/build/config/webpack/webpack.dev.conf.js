var path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin')
var webpack = require('webpack')
const ExtractTextPlugin = require("extract-text-webpack-plugin");
const BabiliPlugin = require("babili-webpack-plugin");
var CompressionPlugin = require("compression-webpack-plugin");
var curDir = process.cwd()
module.exports = {
    entry: './src/app.js',
    output: {
        filename: 'bundle.js',
        path: curDir + '/dist',
    },
    resolve: {
        extensions: ['.js', '.vue', '.json', 'less'],
        alias: {
            'vue$': 'vue/dist/vue.esm.js',
            '@': curDir + '/src'
        }
    },
    module: {
        rules: [
            {
                test: /\.vue$/,
                loader: 'vue-loader'
            },
            {
                test: /\.css$/,
                use: [
                    'style-loader',
                    'css-loader'
                ]
            },
            {
                test: /\.(eot|ttf|woff|woff2)(\?\S*)?$/,
                loader: 'file-loader',
                query: {useRelativePath: true}
            },
            {
                test: /\.(png|svg|jpg|jpeg|gif)$/,
                use: [
                    {
                        loader: 'file-loader',
                        options: {
                            name: '[name].[ext]',
                            outputPath: '/static/assets/',
                            //useRelativePath: true,
                            //emitFile:false
                        }
                    }
                ]
            },
            {
                test: /\.less$/,
                use: [
                    {
                        loader: "style-loader" // creates style nodes from JS strings
                    }, {
                        loader: "css-loader" // translates CSS into CommonJS
                    }, {
                        loader: "less-loader" // compiles Less to CSS
                    }
                ]
            },
            {
                test: /\.js$/,
                loader: 'babel-loader',
                exclude: /node_modules/
            }
        ]
    },
    plugins: [
        new webpack.DefinePlugin({
            'process.env': {
                NODE_ENV: JSON.stringify('production')
            }
        }),
        // new CompressionPlugin({
        //         filename: function (asset) {
        //             console.log(asset.replace(/.gz/,''))
        //             return asset.replace(/.gz/,'')
        //         }
        //     }),
        //new ExtractTextPlugin("[name].css"),
        //new BabiliPlugin(),
        new HtmlWebpackPlugin({
            filename: 'index.html',
            template: curDir + '/src/index.html',
            inject: 'body'
        }),
        new webpack.HotModuleReplacementPlugin()
    ],
    devtool: 'source-map',
    devServer: {
        contentBase: curDir + "/dist",
        port: 9001,
        hot: true,
        disableHostCheck: true,
        compress: true
    }
};