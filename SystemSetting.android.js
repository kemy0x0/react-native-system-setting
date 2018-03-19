import {NativeModules, NativeEventEmitter} from 'react-native'

import Utils from './Utils'

const SystemSettingNative = NativeModules.SystemSetting

const eventEmitter = new NativeEventEmitter(SystemSettingNative)

export default class SystemSetting {    
    static grantWriteSettingPremission(){
        SystemSettingNative.openWriteSetting()
    }

    static async getVolume(type='music') {
        return await SystemSettingNative.getVolume(type)
    }

    static setVolume(val, type='music') {
        SystemSettingNative.setVolume(val, type)
    }

    static addVolumeListener(callback) {
        return eventEmitter.addListener('EventVolume', callback)
    }

    static removeVolumeListener(listener){
        listener && listener.remove()
    }

    static listenEvent(complete, androidEvent){
        const listener = eventEmitter.addListener(Utils.isAndroid ? androidEvent : 'EventEnterForeground', () => {
            listener.remove()
            complete()
        })
    }
}