import React from 'react'
import {NavLink} from 'react-router-dom'
const NavBar = () => {
    <div>
        <NavLink exact to="/">首页</ NavLink>|
        <NavLink to="/PageB">详情页</NavLink>
    </div>
}
export default NavBar