import type { InjectionKey } from 'vue'
import type { SettingsStoreState } from './mock'

export const settingsStateKey: InjectionKey<SettingsStoreState> = Symbol('settingsState')
