import React from 'react';
export default class Home extends React.Component{
    constructor(props) {
        super(props);
        this.state = {
            count: 0
        }
    }

    _handleClick() {
        this.setState({
            count: ++this.state.count
        });
    }
    render(){
        return(
            // 不要使用class
            <div className="home">首页
                当前计数：{this.state.count}<br/>
                <button onClick={() => this._handleClick()}>自增</button>
            </div>
        )
    }
}