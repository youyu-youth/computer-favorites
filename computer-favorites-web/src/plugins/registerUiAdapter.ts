import type { App } from 'vue'

import UAvatar from '@/components/ui-adapter/UAvatar.vue'
import UBadge from '@/components/ui-adapter/UBadge.vue'
import UBreadcrumb from '@/components/ui-adapter/UBreadcrumb.vue'
import UButton from '@/components/ui-adapter/UButton.vue'
import UCard from '@/components/ui-adapter/UCard.vue'
import UContainer from '@/components/ui-adapter/UContainer.vue'
import UFooter from '@/components/ui-adapter/UFooter.vue'
import UFormField from '@/components/ui-adapter/UFormField.vue'
import UFormGroup from '@/components/ui-adapter/UFormGroup.vue'
import UHeader from '@/components/ui-adapter/UHeader.vue'
import UIcon from '@/components/ui-adapter/UIcon.vue'
import UInput from '@/components/ui-adapter/UInput.vue'
import UInputTags from '@/components/ui-adapter/UInputTags.vue'
import UModal from '@/components/ui-adapter/UModal.vue'
import USelect from '@/components/ui-adapter/USelect.vue'
import USeparator from '@/components/ui-adapter/USeparator.vue'
import USkeleton from '@/components/ui-adapter/USkeleton.vue'
import USwitch from '@/components/ui-adapter/USwitch.vue'
import UTextarea from '@/components/ui-adapter/UTextarea.vue'

export const registerUiAdapter = (app: App): void => {
  app.component('UAvatar', UAvatar)
  app.component('UBadge', UBadge)
  app.component('UBreadcrumb', UBreadcrumb)
  app.component('UButton', UButton)
  app.component('UCard', UCard)
  app.component('UContainer', UContainer)
  app.component('UFooter', UFooter)
  app.component('UFormField', UFormField)
  app.component('UFormGroup', UFormGroup)
  app.component('UHeader', UHeader)
  app.component('UIcon', UIcon)
  app.component('UInput', UInput)
  app.component('UInputTags', UInputTags)
  app.component('UModal', UModal)
  app.component('USelect', USelect)
  app.component('USeparator', USeparator)
  app.component('USkeleton', USkeleton)
  app.component('USwitch', USwitch)
  app.component('UTextarea', UTextarea)
}
