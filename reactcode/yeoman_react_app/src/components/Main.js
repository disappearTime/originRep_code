require('normalize.css/normalize.css');
require('styles/App.css');

import React from 'react';

let yeomanImage = require('../images/yeoman.png');

import PageA from './pageA' // 记住首字母一定要大写 组件不要写成<Page-a />
import PageB from './PageB'


class AppComponent extends React.Component {
  render() {
    return (
      <div className="index">
        <img src={yeomanImage} alt="Yeoman Generator" />
        <div className="notice">Please edit <code>src/components/Main.js</code> to get started!</div>
        
      </div>
    );
  }
}

AppComponent.defaultProps = {
};

export default AppComponent;
