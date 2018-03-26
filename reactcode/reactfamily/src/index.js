import React from 'react';
import ReactDom from 'react-dom';
import Hello from './components/Hello' // 首字母一定要大写
import getRouter from './router/router'
import {AppContainer} from 'react-hot-loader';

/*初始化*/
renderWithHotReload(getRouter());
/*热更新*/
if (module.hot) {
    module.hot.accept('./router/router', () => {
        const getRouter = require('./router/router').default;
        renderWithHotReload(getRouter());
    });
}

function renderWithHotReload(RootElement) {
    ReactDom.render(
        <AppContainer>
            {RootElement}
        </AppContainer>,
        document.getElementById('app')
    )
}
