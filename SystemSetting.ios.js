import {NativeModules, NativeEventEmitter} from 'react-native'

import Utils from './Utils'

const SystemSettingNative = NativeModules.SystemSetting

const eventEmitter = new NativeEventEmitter(SystemSettingNative)

export default class SystemSetting {
    static async getVolume(type='music') {
        return await SystemSettingNative.getVolume(type)
    }

    static setVolume(val, config={type: 'music'}) {
        if(typeof(config) === 'string'){
            console.log('setVolume(val, type) is deprecated since 1.2.2, use setVolume(val, config) instead');
            config = {type: config}
        }
        if(!config.type){
            config = {type: 'music'}
        }
        SystemSettingNative.setVolume(val, config)
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