const path = require('path');

module.exports = {
  entry: './app/views/index.js',
  mode: 'none',
  output: {
    filename: 'main.js',
    path: path.resolve(__dirname, '../public/javascripts')
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        use: [
          {
            loader: 'babel-loader',
            options: {
              presets: [
                ['@babel/preset-env', { 'modules': false }],
                '@babel/preset-react'
              ]
            },
  module: {
    rules: [{
      test: /.js$/,
      exclude: /node_modules/,
      use: {
        loader: 'babel-loader',
        options: {
          presets: ['env']
        }
      }
    }]
  }
          }
        ],
        // node_modules は除外する
        exclude: /node_modules/,
      }
    ]
  },
  // ソースマップを有効にする
  devtool: 'source-map'
};
