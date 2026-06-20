import { ref } from 'vue'

const collapsed = ref(true)

export function useSidebar() {
  return { collapsed }
}
