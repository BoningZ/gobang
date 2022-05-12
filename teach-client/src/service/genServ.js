import axios from "axios";
import { store } from "@/store/createStore.js"

function generalRequest(url, data) {
    return axios.post(
        url,
        {
            data: data
        },
        {
            headers: {
                Authorization: 'Bearer ' + store.state.jwtToken
            }
        }
    ).then(res => {
        return res.data.data
    })
}

function samplePost() {
    return generalRequest('/api/teach/admin', null)
}

function getMenuList() {
    return generalRequest('/api/teach/getMenuList', null)
}


function changePassword(data) {
    return generalRequest('/api/teach/changePassword', data)
}
function register(data){
    return generalRequest('/api/auth/signup',data)
}

function courseSubmit(data) {
    return generalRequest('/api/teach/courseSubmit', data)
}
function getProfile(data){
    return generalRequest('/api/teach/getProfile',data);
}
function submitProfile(data){
    return generalRequest('/api/teach/submitProfile',data);
}

function getRoomList(data){
    return generalRequest('/api/teach/getRoomList',data);
}

function addRoom(data){
    return generalRequest('/api/teach/addRoom',data);
}

function joinRoom(data){
    return generalRequest('/url/teach/joinRoom',data);
}


//  company


export {
    samplePost,
    getMenuList,
    changePassword,
    register,
    courseSubmit,
    getProfile,
    submitProfile,
    getRoomList,
    addRoom,
    joinRoom
}