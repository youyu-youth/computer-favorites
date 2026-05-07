import type { InjectionKey, Ref } from 'vue'
import type { SettingsStoreState } from '@/components/user/settings/mock'

export const settingsStateKey: InjectionKey<SettingsStoreState> = Symbol('settingsState')

/**
 * 设置页"保存成功"信号：父组件每次保存成功后递增此值，
 * 子组件（如 EditableInput）watch 该信号即可统一重新锁定字段。
 */
export const settingsLockSignalKey: InjectionKey<Ref<number>> = Symbol('settingsLockSignal')
