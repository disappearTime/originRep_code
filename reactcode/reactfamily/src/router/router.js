import React from 'react';
import {BrowserRouter as Router,Route,Switch,Link} from 'react-router-dom'
import Home from '../components/home'
import Detail from '../components/detail'
const getRouter = () => (
    <Router>
        <div>
            <ul>
                <Link to="/">首页</Link>
                <Link to="/detail">详情</Link>
            </ul>
            <Switch>
                <Route exact path="/" component={Home} />
                <Route path="/detail" component={Detail} />
            </Switch>
        </div>
    </Router>
);
export default getRouter;
