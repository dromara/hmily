// see http://vuejs-templates.github.io/webpack for documentation.
var path = require('path')
var processCWD = process.cwd();

module.exports = {
  build: {
    index: path.resolve(__dirname, '../manage/index.html'),
    assetsRoot: processCWD+'/dist',
    assetsSubDirectory: 'manage',
    assetsPublicPath: '/manage/',
    productionSourceMap: false,
    // Gzip off by default as many popular static hosts such as
    // Surge or Netlify already gzip all static assets for you.
    // Before setting to `true`, make sure to:
    // npm install --save-dev compression-webpack-plugin
    productionGzip: false,
    productionGzipExtensions: ['js', 'css'],
    // Run the build command with an extra argument to
    // View the bundle analyzer report after build finishes:
    // `npm run build --report`
    // Set to `true` or `false` to always turn it on or off
    bundleAnalyzerReport: process.env.npm_config_report
  }
}
