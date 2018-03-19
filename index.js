import SystemSettingAndroid from "./SystemSetting.android";
import SystemSettingIos from "./SystemSetting.ios";

import {
  Platform
} from 'react-native';

const SystemSetting =
  Platform.OS === 'android' ? SystemSettingAndroid :
  Platform.OS === 'ios' ? SystemSettingIos :
  null

export default SystemSetting