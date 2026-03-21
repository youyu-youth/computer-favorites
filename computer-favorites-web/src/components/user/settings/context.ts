import type { InjectionKey } from 'vue'
import type { SettingsStoreState } from '@/components/user/settings/mock'

export const settingsStateKey: InjectionKey<SettingsStoreState> = Symbol('settingsState')
