/**
 * 中文语言包入口
 *
 * @author yyyouth zg
 * @date 2025-03-23
 */

import type { MessageSchema } from '../../types'
import { common } from './common'
import { auth } from './auth'
import { user } from './user'
import { settings } from './settings'

export const zhCN: MessageSchema = {
  common,
  auth,
  user,
  settings,
}
